import java.util.HashMap;
import java.util.LinkedList;

public class Assembler {
    public static String[] assemble(String[] input) {
        if (input == null)
            return new String[0];

        LinkedList<String> output = new LinkedList<>();

        for (String line : input) {
            if (line == null)
                continue;

            line = stripComments(line).trim();
            if (line.length() == 0)
                continue;

            String[] parts = line.split("\\s+");
            String opcodeName = parts[0].toLowerCase();

            if (opcodeName.equals("halt")) {
                requireCount(parts, 1);
                output.add(buildCallReturn(opcodeBits(opcodeName), 0));
            }
            else if (opcodeName.equals("return")) {
                requireCount(parts, 1);
                output.add(buildCallReturn(opcodeBits(opcodeName), 0));
            }
            else if (opcodeName.equals("syscall")) {
                requireCount(parts, 2);
                int imm = parseNumber(parts[1]);
                output.add(buildCallReturn(opcodeBits(opcodeName), imm));
            }
            else if (opcodeName.equals("call") || opcodeName.equals("ble") || opcodeName.equals("blt")
                    || opcodeName.equals("bge") || opcodeName.equals("bgt") || opcodeName.equals("beq")
                    || opcodeName.equals("bne")) {
                requireCount(parts, 2);
                int imm = parseNumber(parts[1]);
                output.add(buildCallReturn(opcodeBits(opcodeName), imm));
            }
            else {
                requireCount(parts, 3);

                if (isRegister(parts[1])) {
                    int source = parseRegister(parts[1]);
                    int dest = parseRegister(parts[2]);
                    output.add(build2R(opcodeBits(opcodeName), source, dest));
                }
                else {
                    int immediate = parseNumber(parts[1]);
                    int dest = parseRegister(parts[2]);
                    output.add(buildImmediate(opcodeBits(opcodeName), immediate, dest));
                }
            }
        }

        return output.toArray(new String[0]);
    }

    public static String[] finalOutput(String[] input) {
        String[] assembled;

        if (input == null)
            return new String[0];

        // If the input is already assembled 16-bit binary, do not assemble again
        if (isBinaryInstructionArray(input))
            assembled = input;
        else
            assembled = assemble(input);

        LinkedList<String> output = new LinkedList<>();

        int i = 0;
        while (i < assembled.length) {
            String first = assembled[i];
            String second;

            if (i + 1 < assembled.length)
                second = assembled[i + 1];
            else
                second = "0000000000000000";

            output.add(first + second);
            i = i + 2;
        }

        return output.toArray(new String[0]);
    }

    private static String stripComments(String line) {
        int slash = line.indexOf("//");
        int hash = line.indexOf('#');

        int cut = -1;
        if (slash >= 0)
            cut = slash;
        if (hash >= 0) {
            if (cut == -1 || hash < cut)
                cut = hash;
        }

        if (cut >= 0)
            return line.substring(0, cut);
        return line;
    }

    private static void requireCount(String[] parts, int expected) {
        if (parts.length != expected)
            throw new IllegalArgumentException("Wrong number of tokens for instruction: " + String.join(" ", parts));
    }

    private static boolean isRegister(String token) {
        if (token == null || token.length() < 2)
            return false;

        char first = token.charAt(0);
        if (first != 'r' && first != 'R')
            return false;

        for (int i = 1; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c < '0' || c > '9')
                return false;
        }

        return true;
    }

    private static int parseRegister(String token) {
        if (!isRegister(token))
            throw new IllegalArgumentException("Expected register, found: " + token);

        int value = Integer.parseInt(token.substring(1));
        if (value < 0 || value > 31)
            throw new IllegalArgumentException("Register out of range: " + token);

        return value;
    }

    private static int parseNumber(String token) {
        return Integer.parseInt(token);
    }

    private static String build2R(String opcode, int source, int dest) {
        return opcode + "0" + toUnsignedBits(source, 5) + toUnsignedBits(dest, 5);
    }

    private static String buildImmediate(String opcode, int immediate, int dest) {
        return opcode + "1" + toSignedBits(immediate, 5) + toUnsignedBits(dest, 5);
    }

    private static String buildCallReturn(String opcode, int immediate) {
        if (immediate < 0)
            return opcode + toSignedBits(immediate, 11);
        return opcode + toUnsignedBits(immediate, 11);
    }

    private static String toUnsignedBits(int value, int width) {
        int max = 1;
        for (int i = 0; i < width; i++)
            max = max * 2;

        if (value < 0 || value >= max)
            throw new IllegalArgumentException("Unsigned value out of range for " + width + " bits: " + value);

        StringBuilder bits = new StringBuilder();
        int divisor = max / 2;

        while (divisor > 0) {
            if (value >= divisor) {
                bits.append('1');
                value = value - divisor;
            } else
                bits.append('0');
            divisor = divisor / 2;
        }

        return bits.toString();
    }

    private static String toSignedBits(int value, int width) {
        int maxPositive = 1;
        for (int i = 0; i < width - 1; i++)
            maxPositive = maxPositive * 2;

        int minNegative = -maxPositive;
        int maxValue = maxPositive - 1;

        if (value < minNegative || value > maxValue)
            throw new IllegalArgumentException("Signed value out of range for " + width + " bits: " + value);

        if (value >= 0)
            return toUnsignedBits(value, width);

        int full = 1;
        for (int i = 0; i < width; i++)
            full = full * 2;

        return toUnsignedBits(full + value, width);
    }

    private static String opcodeBits(String opcodeName) {
        HashMap<String, Integer> opcodes = new HashMap<>();
        opcodes.put("halt", 0);
        opcodes.put("add", 1);
        opcodes.put("and", 2);
        opcodes.put("multiply", 3);
        opcodes.put("leftshift", 4);
        opcodes.put("subtract", 5);
        opcodes.put("or", 6);
        opcodes.put("rightshift", 7);
        opcodes.put("syscall", 8);
        opcodes.put("call", 9);
        opcodes.put("return", 10);
        opcodes.put("compare", 11);
        opcodes.put("ble", 12);
        opcodes.put("blt", 13);
        opcodes.put("bge", 14);
        opcodes.put("bgt", 15);
        opcodes.put("beq", 16);
        opcodes.put("bne", 17);
        opcodes.put("load", 18);
        opcodes.put("store", 19);
        opcodes.put("copy", 20);

        Integer value = opcodes.get(opcodeName.toLowerCase());
        if (value == null)
            throw new IllegalArgumentException("Unknown opcode: " + opcodeName);

        return toUnsignedBits(value, 5);
    }

    private static boolean isBinaryInstructionArray(String[] input) {
        for (String line : input) {
            if (line == null)
                continue;

            String trimmed = line.trim();
            if (trimmed.length() == 0)
                continue;

            if (trimmed.length() != 16)
                return false;

            for (int i = 0; i < 16; i++) {
                char c = trimmed.charAt(i);
                if (c != '0' && c != '1')
                    return false;
            }
        }
        return true;
    }
}