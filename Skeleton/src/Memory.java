public class Memory {
    public Word32 address = new Word32();
    public Word32 value = new Word32();
    private final Word32[] dram = new Word32[1000];

    public int addressAsInt() {
        int result = 0;
        int placeValue = 1;
        for (int i = 31; i >= 0; i--) {
            Bit bit = new Bit(false);
            address.getBitN(i, bit);
            if (bit.getValue())
                result = result + placeValue;
            placeValue = placeValue * 2;
        }

        if (result < 0 || result >= 1000)
            throw new IndexOutOfBoundsException("Memory address out of range: " + result);

        return result;
    }

    public Memory() {
        for (int i = 0; i < dram.length; i++)
            dram[i] = new Word32();
    }

    public void read() {
        int index = addressAsInt();
        dram[index].copy(value);
    }

    public void write() {
        int index = addressAsInt();
        value.copy(dram[index]);
    }

    public void load(String[] data) {
        if (data == null)
            return;

        if (data.length > dram.length)
            throw new IllegalArgumentException("Too many words for memory: " + data.length);

        for (int i = 0; i < data.length; i++) {
            String line = data[i];

            if (line == null || line.length() != 32)
                throw new IllegalArgumentException("Each memory line must be exactly 32 bits");

            for (int j = 0; j < 32; j++) {
                char c = line.charAt(j);

                if (c == '0')
                    dram[i].setBitN(j, new Bit(false));
                else if (c == '1')
                    dram[i].setBitN(j, new Bit(true));
                else
                    throw new IllegalArgumentException("Memory line contains non-binary character: " + c);
            }
        }
    }
}