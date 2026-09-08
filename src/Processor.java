import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Processor {
    private final Memory mem;
    private final InstructionCache instructionCache;
    public List<String> output = new LinkedList<>();

    private static final int MEMORY_CYCLES = 300;
    private static final int MULTIPLY_CYCLES = 10;
    private static final int ALU_CYCLES = 2;

    private int currentClockCycle = 0;

    private final Word32[] registers = new Word32[32];
    private final Stack<Word32> callStack = new Stack<>();
    private final ALU alu = new ALU();

    private final Word32 pc = new Word32();
    private final Word16 instruction = new Word16();
    private final Word32 op1 = new Word32();
    private final Word32 op2 = new Word32();
    private final Word32 result = new Word32();

    private final Bit statusLess = new Bit(false);
    private final Bit statusEqual = new Bit(false);


    private boolean halted = false;

    private int opcode = 0;
    private boolean immediateFormat = false;
    private int sourceRegister = 0;
    private int destinationRegister = 0;
    private int syscallNumber = 0;

    private boolean topHalf = true;
    private final Stack<Boolean> callStackTopHalf = new Stack<>();

    private final L2Cache l2Cache;

    public Processor(Memory m) {
        mem = m;
        instructionCache = new InstructionCache(mem);
        l2Cache = instructionCache.getL2Cache();
        for (int i = 0; i < 32; i++)
            registers[i] = new Word32();
    }

    public int getCurrentClockCycle() {
        return currentClockCycle;
    }

    public void run() {
        while (!halted) {
            fetch();
            decode();
            execute();
            store();
        }
    }

    private void fetch() {
        pc.copy(instructionCache.address);
        instructionCache.read();
        currentClockCycle += instructionCache.getLastReadCycles();

        if (topHalf)
            instructionCache.value.getTopHalf(instruction);
        else
            instructionCache.value.getBottomHalf(instruction);
    }

    private void decode() {
        clearWord(op1);
        clearWord(op2);
        clearWord(result);

        opcode = bitsToInt(instruction, 0, 4);
        immediateFormat = false;
        sourceRegister = 0;
        destinationRegister = 0;
        syscallNumber = 0;

        if (opcode == 0 || opcode == 8 || opcode == 9 || opcode == 10
                || opcode == 12 || opcode == 13 || opcode == 14
                || opcode == 15 || opcode == 16 || opcode == 17) {

            if (opcode == 8)
                syscallNumber = bitsToInt(instruction, 5, 15);
            else if (opcode == 9 || opcode == 12 || opcode == 13
                    || opcode == 14 || opcode == 15 || opcode == 16
                    || opcode == 17) {
                pc.copy(op1);
                signExtend11Into(op2);
            }

            return;
        }

        Bit formatBit = new Bit(false);
        instruction.getBitN(5, formatBit);
        immediateFormat = formatBit.getValue();

        destinationRegister = bitsToInt(instruction, 11, 15);

        if (!immediateFormat)
            sourceRegister = bitsToInt(instruction, 6, 10);

        switch (opcode) {
            case 1:     // add
            case 2:     // and
            case 3:     // multiply
            case 6:     // or
                if (immediateFormat)
                    signExtend5Into(op1);
                else
                    registers[sourceRegister].copy(op1);
                registers[destinationRegister].copy(op2);
                break;

            case 4:     // leftshift
            case 7:     // rightshift
                registers[destinationRegister].copy(op1);
                if (immediateFormat)
                    signExtend5Into(op2);
                else
                    registers[sourceRegister].copy(op2);
                break;

            case 5:     // subtract: destination - source
                registers[destinationRegister].copy(op1);
                if (immediateFormat)
                    signExtend5Into(op2);
                else
                    registers[sourceRegister].copy(op2);
                break;

            case 11:    // compare source to destination
                if (immediateFormat)
                    signExtend5Into(op1);
                else
                    registers[sourceRegister].copy(op1);
                registers[destinationRegister].copy(op2);
                break;

            case 18:    // load
                if (immediateFormat) {
                    registers[destinationRegister].copy(op1); // base
                    signExtend5Into(op2);                    // offset
                } else
                    registers[sourceRegister].copy(op1);     // address
                break;

            case 19:    // store
                if (immediateFormat) {
                    signExtend5Into(op1);                     // value to store
                    registers[destinationRegister].copy(op2); // address
                } else {
                    registers[sourceRegister].copy(op1);      // value
                    registers[destinationRegister].copy(op2); // address
                }
                break;

            case 20:    // copy
                if (immediateFormat)
                    signExtend5Into(op2);
                else
                    registers[sourceRegister].copy(op2);
                break;

            default:
                break;
        }
    }

    private void execute() {
        clearWord(result);

        switch (opcode) {
            case 0:     // halt
                break;

            case 1:     // add
            case 2:     // and
            case 3:     // multiply
            case 4:     // leftshift
            case 5:     // subtract
            case 6:     // or
            case 7:     // rightshift
            case 9:     // call target calc
            case 11:    // compare
            case 12:    // ble target calc
            case 13:    // blt target calc
            case 14:    // bge target calc
            case 15:    // bgt target calc
            case 16:    // beq target calc
            case 17:    // bne target calc
            case 20:    // copy
                addExecutionCyclesForCurrentOpcode();
                instruction.copy(alu.instruction);
                op1.copy(alu.op1);
                op2.copy(alu.op2);
                alu.doInstruction();
                alu.result.copy(result);
                break;

            case 8:     // syscall
                doSyscall();
                break;

            case 10:    // return
                break;

            case 18:    // load
                if (immediateFormat) {
                    currentClockCycle += ALU_CYCLES;
                    Adder.add(op1, op2, l2Cache.address);
                } else
                    op1.copy(l2Cache.address);
                l2Cache.read();
                currentClockCycle += l2Cache.getLastReadCycles();
                l2Cache.value.copy(result);
                break;

            case 19:    // store
                op2.copy(l2Cache.address);
                op1.copy(l2Cache.value);
                l2Cache.write();
                currentClockCycle += l2Cache.getLastReadCycles();
                instructionCache.invalidateIfAddress(l2Cache.address);
                break;

            default:
                break;
        }
    }

    private void store() {
        switch (opcode) {
            case 0:     // halt
                halted = true;
                System.out.println("Clock cycles: " + currentClockCycle);
                break;

            case 1:     // add
            case 2:     // and
            case 3:     // multiply
            case 4:     // leftshift
            case 5:     // subtract
            case 6:     // or
            case 7:     // rightshift
            case 18:    // load
            case 20:    // copy
                result.copy(registers[destinationRegister]);
                incrementPC();
                break;

            case 8:     // syscall
                incrementPC();
                break;

            case 9:     // call
                Word32 returnAddress = new Word32();

                if (topHalf) {
                    pc.copy(returnAddress);
                    callStackTopHalf.push(false);   // return to bottom half of same word
                } else {
                    pcPlusOneInto(returnAddress);
                    callStackTopHalf.push(true);    // return to top half of next word
                }

                callStack.push(returnAddress);
                result.copy(pc);    // branch target already computed by ALU
                topHalf = true;     // targets always start at top half
                break;

            case 10:    // return
                if (callStack.isEmpty())
                    halted = true;
                else {
                    callStack.pop().copy(pc);
                    topHalf = callStackTopHalf.pop();
                }
                break;

            case 11:    // compare
                statusLess.assign(alu.less.getValue());
                statusEqual.assign(alu.equal.getValue());
                incrementPC();
                break;

            case 12:    // ble
                if (statusLess.getValue() || statusEqual.getValue()) {
                    result.copy(pc);
                    topHalf = true;
                } else
                    incrementPC();
                break;

            case 13:    // blt
                if (statusLess.getValue()) {
                    result.copy(pc);
                    topHalf = true;
                } else
                    incrementPC();
                break;

            case 14:    // bge
                if (!statusLess.getValue()) {
                    result.copy(pc);
                    topHalf = true;
                } else
                    incrementPC();
                break;

            case 15:    // bgt
                if (!statusLess.getValue() && !statusEqual.getValue()) {
                    result.copy(pc);
                    topHalf = true;
                } else
                    incrementPC();
                break;

            case 16:    // beq
                if (statusEqual.getValue()) {
                    result.copy(pc);
                    topHalf = true;
                } else
                    incrementPC();
                break;

            case 17:    // bne
                if (!statusEqual.getValue()) {
                    result.copy(pc);
                    topHalf = true;
                } else
                    incrementPC();
                break;

            case 19:    // store
                incrementPC();
                break;

            default:
                incrementPC();
                break;
        }
    }

    private void printReg() {
        for (int i = 0; i < 32; i++) {
            var line = "r" + i + ":" + registers[i];
            output.add(line);
            System.out.println(line);
        }
    }

    private void printMem() {
        for (int i = 0; i < 1000; i++) {
            Word32 addr = new Word32();
            Word32 value = new Word32();
            TestConverter.fromInt(i, addr);
            addr.copy(mem.address);
            mem.read();
            mem.value.copy(value);
            var line = i + ":" + value + "(" + TestConverter.toInt(value) + ")";
            output.add(line);
            System.out.println(line);
        }
    }

    private void addExecutionCyclesForCurrentOpcode() {
        if (opcode == 3)
            currentClockCycle += MULTIPLY_CYCLES;
        else
            currentClockCycle += ALU_CYCLES;
    }

    private void doSyscall() {
        if (syscallNumber == 0)
            printReg();
        else if (syscallNumber == 1)
            printMem();
    }

    private void incrementPC() {
        if (topHalf)
            topHalf = false;
        else {
            Word32 next = new Word32();
            pcPlusOneInto(next);
            next.copy(pc);
            topHalf = true;
        }
    }

    private void pcPlusOneInto(Word32 target) {
        Word32 one = new Word32();
        one.setBitN(31, new Bit(true));
        Adder.add(pc, one, target);
    }

    private int bitsToInt(Word16 word, int start, int end) {
        int value = 0;

        for (int i = start; i <= end; i++) {
            value = value * 2;
            Bit bit = new Bit(false);
            word.getBitN(i, bit);
            if (bit.getValue())
                value = value + 1;
        }

        return value;
    }

    private void signExtend5Into(Word32 target) {
        Bit sign = new Bit(false);
        instruction.getBitN(6, sign);

        for (int i = 0; i < 27; i++)
            target.setBitN(i, new Bit(sign.getValue()));

        for (int i = 0; i < 5; i++) {
            Bit bit = new Bit(false);
            instruction.getBitN(6 + i, bit);
            target.setBitN(27 + i, bit);
        }
    }

    private void signExtend11Into(Word32 target) {
        Bit sign = new Bit(false);
        instruction.getBitN(5, sign);

        for (int i = 0; i < 21; i++)
            target.setBitN(i, new Bit(sign.getValue()));

        for (int i = 0; i < 11; i++) {
            Bit bit = new Bit(false);
            instruction.getBitN(5 + i, bit);
            target.setBitN(21 + i, bit);
        }
    }

    private void clearWord(Word32 word) {
        for (int i = 0; i < 32; i++)
            word.setBitN(i, new Bit(false));
    }

    private void setUnsignedWord(Word32 word, int value) {
        clearWord(word);

        int index = 31;
        int working = value;

        while (working > 0 && index >= 0) {
            int remainder = working % 2;
            if (remainder == 1)
                word.setBitN(index, new Bit(true));
            working = working / 2;
            index = index - 1;
        }
    }
}