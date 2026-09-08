public class Word32 {

    private final Bit[] bits;

    public Word32() {
        bits = new Bit[32];
        for (int i = 0; i < bits.length; i++)
            bits[i] = new Bit(false);
    }

    public Word32(Bit[] in) {
        this();
        if (in == null)
            return;
        int limit = in.length;
        if (limit > bits.length)
            limit = bits.length;
        for (int i = 0; i < limit; i++) {
            if (in[i] == null)
                bits[i].assign(false);
            else
                bits[i].assign(in[i].getValue());
        }
    }

    // sets the values in "result" to be the same as the values in this instance
    public void copy(Word32 result) {
        if (result == null)
            return;
        for (int i = 0; i < bits.length; i++)
            result.bits[i].assign(bits[i].getValue());
    }

    // sets the nth bit of this word to "source"
    public void setBitN(int n, Bit source) {
        if (n < 0 || n >= bits.length)
            throw new IndexOutOfBoundsException("Bit index out of range: " + n);
        if (source == null)
            bits[n].assign(false);
        else
            bits[n].assign(source.getValue());
    }

    // sets result to be the same value as the nth bit of this word
    public void getBitN(int n, Bit result) {
        if (n < 0 || n >= bits.length)
            throw new IndexOutOfBoundsException("Bit index out of range: " + n);
        if (result == null)
            return;
        result.assign(bits[n].getValue());
    }

    // is other equal to this
    public boolean equals(Word32 other) {
        return equals(this, other);
    }

    public static boolean equals(Word32 a, Word32 b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        for (int i = 0; i < a.bits.length; i++) {
            if (a.bits[i].getValue()) {
                if (!b.bits[i].getValue())
                    return false;
            } else {
                if (b.bits[i].getValue())
                    return false;
            }
        }
        return true;
    }

    public void and(Word32 other, Word32 result) {
        and(this, other, result);
    }

    public static void and(Word32 a, Word32 b, Word32 result) {
        if (a == null || b == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.and(a.bits[i], b.bits[i], result.bits[i]);
    }

    public void or(Word32 other, Word32 result) {
        or(this, other, result);
    }

    public static void or(Word32 a, Word32 b, Word32 result) {
        if (a == null || b == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.or(a.bits[i], b.bits[i], result.bits[i]);
    }

    public void xor(Word32 other, Word32 result) {
        xor(this, other, result);
    }

    public static void xor(Word32 a, Word32 b, Word32 result) {
        if (a == null || b == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.xor(a.bits[i], b.bits[i], result.bits[i]);
    }

    public void not(Word32 result) {
        not(this, result);
    }

    public static void not(Word32 a, Word32 result) {
        if (a == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.not(a.bits[i], result.bits[i]);
    }

    // bits 0-15 inclusive copied into result
    public void getTopHalf(Word16 result) {
        if (result == null)
            return;
        for (int i = 0; i < 16; i++)
            result.setBitN(i, bits[i]);
    }

    // bits 16-31 inclusive copied into result as bits 0-15
    public void getBottomHalf(Word16 result) {
        if (result == null)
            return;
        for (int i = 0; i < 16; i++)
            result.setBitN(i, bits[i + 16]);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bit bit : bits) {
            sb.append(bit.toString());
            sb.append(",");
        }
        return sb.toString();
    }
}
