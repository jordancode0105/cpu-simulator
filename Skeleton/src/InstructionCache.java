public class InstructionCache {
    public Word32 address = new Word32();
    public Word32 value = new Word32();

    private static final int LINE_SIZE = 8;
    private static final int HIT_CYCLES = 10;
    private static final int L1_CHECK_CYCLES = 10;

    private final L2Cache l2Cache;
    private final Word32 lineAddress = new Word32();
    private final Word32[] line = new Word32[LINE_SIZE];

    private boolean valid = false;
    private int baseAddress = 0;
    private int lastReadCycles = 0;

    public InstructionCache(Memory memory) {
        l2Cache = new L2Cache(memory);

        for (int i = 0; i < LINE_SIZE; i++) {
            line[i] = new Word32();
        }
    }

    public int getLastReadCycles() {
        return lastReadCycles;
    }

    public L2Cache getL2Cache() {
        return l2Cache;
    }

    public void read() {
        int requestedAddress = addressAsInt(address);

        if (isHit(requestedAddress)) {
            lastReadCycles = HIT_CYCLES;
        } else {
            fillFromL2(requestedAddress);
        }

        line[requestedAddress - baseAddress].copy(value);
    }

    public void invalidate() {
        valid = false;
        l2Cache.invalidate();
    }

    public void invalidateIfAddress(Word32 possibleInstructionAddress) {
        if (valid) {
            int writtenAddress = addressAsInt(possibleInstructionAddress);
            if (writtenAddress >= baseAddress && writtenAddress < baseAddress + LINE_SIZE) {
                valid = false;
            }
        }
    }

    private boolean isHit(int requestedAddress) {
        return valid && requestedAddress >= baseAddress && requestedAddress < baseAddress + LINE_SIZE;
    }

    private void fillFromL2(int requestedAddress) {
        baseAddress = (requestedAddress / LINE_SIZE) * LINE_SIZE;
        setUnsignedWord(lineAddress, baseAddress);

        int l2Cycles = l2Cache.readLineInto(lineAddress, line);
        lastReadCycles = L1_CHECK_CYCLES + l2Cycles;
        valid = true;
    }

    private int addressAsInt(Word32 word) {
        int result = 0;
        int placeValue = 1;

        for (int i = 31; i >= 0; i--) {
            Bit bit = new Bit(false);
            word.getBitN(i, bit);

            if (bit.getValue()) {
                result = result + placeValue;
            }

            placeValue = placeValue * 2;
        }

        if (result < 0 || result >= 1000) {
            throw new IndexOutOfBoundsException("Instruction cache address out of range: " + result);
        }

        return result;
    }

    private void setUnsignedWord(Word32 word, int value) {
        clearWord(word);

        int index = 31;
        int working = value;

        while (working > 0 && index >= 0) {
            int remainder = working % 2;
            if (remainder == 1) {
                word.setBitN(index, new Bit(true));
            }
            working = working / 2;
            index = index - 1;
        }
    }

    private void clearWord(Word32 word) {
        for (int i = 0; i < 32; i++) {
            word.setBitN(i, new Bit(false));
        }
    }
}
