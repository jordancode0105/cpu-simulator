public class Word16 {

    private final Bit[] bits;

    public Word16() {
        bits = new Bit[16];
        for (int i = 0; i < bits.length; i++)
            bits[i] = new Bit(false);
    }

    public Word16(Bit[] in) {
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
    public void copy(Word16 result) {
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
    public boolean equals(Word16 other) {
        return equals(this, other);
    }

    public static boolean equals(Word16 a, Word16 b) {
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

    public void and(Word16 other, Word16 result) {
        and(this, other, result);
    }

    public static void and(Word16 a, Word16 b, Word16 result) {
        if (a == null || b == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.and(a.bits[i], b.bits[i], result.bits[i]);
    }

    public void or(Word16 other, Word16 result) {
        or(this, other, result);
    }

    public static void or(Word16 a, Word16 b, Word16 result) {
        if (a == null || b == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.or(a.bits[i], b.bits[i], result.bits[i]);

    }

    public void xor(Word16 other, Word16 result) {
        xor(this, other, result);
    }

    public static void xor(Word16 a, Word16 b, Word16 result) {
        if (a == null || b == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.xor(a.bits[i], b.bits[i], result.bits[i]);
    }

    public void not(Word16 result) {
        not(this, result);
    }

    public static void not(Word16 a, Word16 result) {
        if (a == null || result == null)
            return;
        for (int i = 0; i < a.bits.length; i++)
            Bit.not(a.bits[i], result.bits[i]);
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
