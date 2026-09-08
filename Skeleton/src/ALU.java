public class ALU {
    public Word16 instruction = new Word16();
    public Word32 op1 = new Word32();
    public Word32 op2 = new Word32();
    public Word32 result = new Word32();
    public Bit less = new Bit(false);
    public Bit equal = new Bit(false);

    public void doInstruction() {
        clearResult();
        less.assign(false);
        equal.assign(false);

        int opcode = getOpcode();

        switch (opcode) {
            case 1:     // Add
            case 9:     // Call uses PC + immediate
            case 12:    // BLE target calc
            case 13:    // BLT target calc
            case 14:    // BGE target calc
            case 15:    // BGT target calc
            case 16:    // BEQ target calc
            case 17:    // BNE target calc
                Adder.add(op1, op2, result);
                break;

            case 2:     // And
                Word32.and(op1, op2, result);
                break;

            case 3:     // Multiply
                Multiplier.multiply(op1, op2, result);
                break;

            case 4:     // LeftShift
                Shifter.LeftShift(op1, lowFiveBitsAsInt(op2), result);
                break;

            case 5:     // Subtract
                Adder.subtract(op1, op2, result);
                break;

            case 6:     // Or
                Word32.or(op1, op2, result);
                break;

            case 7:     // RightShift
                Shifter.RightShift(op1, lowFiveBitsAsInt(op2), result);
                break;

            case 11:    // Compare
                doCompare();
                break;

            case 18:    // Load
                if (isImmediateFormat()) {
                    Adder.add(op1, op2, result);
                }
                break;

            case 20:    // Copy
                op2.copy(result);
                break;

            default:
                break;
        }
    }

    private void doCompare() {
        if (Word32.equals(op1, op2)) {
            equal.assign(true);
            less.assign(false);
            return;
        }

        Bit sign1 = new Bit(false);
        Bit sign2 = new Bit(false);
        op1.getBitN(0, sign1); // MSB / sign bit
        op2.getBitN(0, sign2);

        // Signed compare negative < non-negative
        if (sign1.getValue() && !sign2.getValue()) {
            less.assign(true);
            equal.assign(false);
            return;
        }

        if (!sign1.getValue() && sign2.getValue()) {
            less.assign(false);
            equal.assign(false);
            return;
        }

        // Same sign, compare from MSB to LSB
        for (int i = 0; i < 32; i++) {
            Bit b1 = new Bit(false);
            Bit b2 = new Bit(false);
            op1.getBitN(i, b1);
            op2.getBitN(i, b2);

            if (b1.getValue() != b2.getValue()) {
                less.assign(!b1.getValue() && b2.getValue());
                equal.assign(false);
                return;
            }
        }

        equal.assign(true);
        less.assign(false);
    }

    private int getOpcode() {
        int opcode = 0;

        for (int i = 0; i < 5; i++) {
            opcode = opcode * 2;

            Bit bit = new Bit(false);
            instruction.getBitN(i, bit);

            if (bit.getValue()) {
                opcode = opcode + 1;
            }
        }

        return opcode;
    }

    private boolean isImmediateFormat() {
        Bit format = new Bit(false);
        instruction.getBitN(5, format);
        return format.getValue();
    }

    // lowest 5 bits of op2 as unsigned shift amount
    private int lowFiveBitsAsInt(Word32 word) {
        int amount = 0;
        int place = 1;

        for (int i = 31; i >= 27; i--) {
            Bit bit = new Bit(false);
            word.getBitN(i, bit);

            if (bit.getValue()) {
                amount = amount + place;
            }

            place = place * 2;
        }

        return amount;
    }

    private void clearResult() {
        for (int i = 0; i < 32; i++) {
            result.setBitN(i, new Bit(false));
        }
    }
}