public class L2Cache {
    public Word32 address = new Word32();
    public Word32 value = new Word32();

    private static final int LINE_SIZE = 8;
    private static final int LINE_COUNT = 4;
    private static final int HIT_CYCLES = 20;
    private static final int INSTRUCTION_MISS_CYCLES = 340;
    private static final int DATA_MISS_CYCLES = 360;

    private final Memory mainMemory;
    private final Word32[][] lines = new Word32[LINE_COUNT][LINE_SIZE];
    private final int[] baseAddresses = new int[LINE_COUNT];
    private final boolean[] valid = new boolean[LINE_COUNT];

    private int lastReadCycles = 0;

    public L2Cache(Memory memory) {
        mainMemory = memory;

        for (int i = 0; i < LINE_COUNT; i++) {
            baseAddresses[i] = 0;
            valid[i] = false;

            for (int j = 0; j < LINE_SIZE; j++) {
                lines[i][j] = new Word32();
            }
        }
    }

    public int getLastReadCycles() {
        return lastReadCycles;
    }

    public void read() {
        int requestedAddress = addressAsInt(address);
        int lineIndex = getLineIndex(requestedAddress);
        int baseAddress = getBaseAddress(requestedAddress);

        if (isHit(lineIndex, baseAddress)) {
            lastReadCycles = HIT_CYCLES;
        } else {
            lastReadCycles = DATA_MISS_CYCLES;
            fillLine(lineIndex, baseAddress);
        }

        lines[lineIndex][requestedAddress - baseAddress].copy(value);
    }

    public void write() {
        int requestedAddress = addressAsInt(address);
        int lineIndex = getLineIndex(requestedAddress);
        int baseAddress = getBaseAddress(requestedAddress);

        if (isHit(lineIndex, baseAddress)) {
            lastReadCycles = HIT_CYCLES;
        } else {
            lastReadCycles = DATA_MISS_CYCLES;
            fillLine(lineIndex, baseAddress);
        }

        // Update L2 cache line
        value.copy(lines[lineIndex][requestedAddress - baseAddress]);

        // update main memory too
        address.copy(mainMemory.address);
        value.copy(mainMemory.value);
        mainMemory.write();
    }

    public int readLineInto(Word32 requestedAddressWord, Word32[] destinationLine) {
        int requestedAddress = addressAsInt(requestedAddressWord);
        int lineIndex = getLineIndex(requestedAddress);
        int baseAddress = getBaseAddress(requestedAddress);

        if (isHit(lineIndex, baseAddress)) {
            lastReadCycles = HIT_CYCLES;
        } else {
            lastReadCycles = INSTRUCTION_MISS_CYCLES;
            fillLine(lineIndex, baseAddress);
        }

        for (int i = 0; i < LINE_SIZE; i++) {
            lines[lineIndex][i].copy(destinationLine[i]);
        }

        return lastReadCycles;
    }

    public void invalidate() {
        for (int i = 0; i < LINE_COUNT; i++) {
            valid[i] = false;
        }
    }

    public void invalidateIfAddress(Word32 possibleInstructionAddress) {
        int writtenAddress = addressAsInt(possibleInstructionAddress);
        int baseAddress = getBaseAddress(writtenAddress);
        int lineIndex = getLineIndex(writtenAddress);

        if (valid[lineIndex] && baseAddresses[lineIndex] == baseAddress) {
            valid[lineIndex] = false;
        }
    }

    private boolean isHit(int lineIndex, int baseAddress) {
        return valid[lineIndex] && baseAddresses[lineIndex] == baseAddress;
    }

    private void fillLine(int lineIndex, int baseAddress) {
        baseAddresses[lineIndex] = baseAddress;

        for (int i = 0; i < LINE_SIZE; i++) {
            int currentAddress = baseAddress + i;

            if (currentAddress < 1000) {
                Word32 addr = new Word32();
                setUnsignedWord(addr, currentAddress);
                addr.copy(mainMemory.address);
                mainMemory.read();
                mainMemory.value.copy(lines[lineIndex][i]);
            } else {
                clearWord(lines[lineIndex][i]);
            }
        }

        valid[lineIndex] = true;
    }

    private int getBaseAddress(int address) {
        return (address / LINE_SIZE) * LINE_SIZE;
    }

    private int getLineIndex(int address) {
        int lineNumber = address / LINE_SIZE;
        return lineNumber % LINE_COUNT;
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
            throw new IndexOutOfBoundsException("L2 cache address out of range: " + result);
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
