public class Multiplier {

    public static void multiply(Word32 a, Word32 b, Word32 result) {
        if (a == null || b == null || result == null)
            return;

        // accumulator = 0
        Word32 acc = new Word32();

        // For each k in [0..31], if b's bit (2^k) is 1, add (a << k) into acc.
        for (int k = 0; k < 32; k++) {
            Bit bk = new Bit(false);
            int bIndex = 31 - k;
            b.getBitN(bIndex, bk);

            if (bk.getValue()) {
                Word32 shifted = new Word32();
                leftShiftByK(a, k, shifted);

                Word32 newAcc = new Word32();
                Adder.add(acc, shifted, newAcc);
                acc = newAcc; // keep low 32 bits
            }
        }

        acc.copy(result);
    }

    // out = source << k
    private static void leftShiftByK(Word32 source, int k, Word32 out) {
        // clear out
        for (int i = 0; i < 32; i++)
            out.setBitN(i, new Bit(false));

        if (k <= 0) {
            source.copy(out);
            return;
        }
        if (k >= 32)
            return; // all zeroes already

        // result[i] = source[i + k] when i+k in range
        for (int i = 0; i < 32; i++) {
            int from = i + k;
            if (from <= 31) {
                Bit tmp = new Bit(false);
                source.getBitN(from, tmp);
                out.setBitN(i, tmp);
            }
        }
    }
}