public class Shifter {

    // result = source << amount
    // Uses only the lowest 5 bits of amount
    public static void LeftShift(Word32 source, int amount, Word32 result) {
        if (source == null || result == null)
            return;

        // normalize amount to [0, 31]
        while (amount < 0)
            amount += 32;
        while (amount >= 32)
            amount -= 32;

        // clear result
        for (int i = 0; i < 32; i++)
            result.setBitN(i, new Bit(false));

        if (amount == 0) {
            source.copy(result);
            return;
        }

        // new[i] = old[i + amount] when in range, else 0
        for (int i = 0; i < 32; i++) {
            int from = i + amount;
            if (from <= 31) {
                Bit tmp = new Bit(false);
                source.getBitN(from, tmp);
                result.setBitN(i, tmp);
            }
        }
    }

    // result = source >> amount
    // Uses only the lowest 5 bits of amount
    public static void RightShift(Word32 source, int amount, Word32 result) {
        if (source == null || result == null)
            return;

        // normalize amount to [0, 31]
        while (amount < 0)
            amount += 32;
        while (amount >= 32)
            amount -= 32;

        // clear result
        for (int i = 0; i < 32; i++)
            result.setBitN(i, new Bit(false));

        if (amount == 0) {
            source.copy(result);
            return;
        }

        // new[i] = old[i - amount] when in range, else 0
        for (int i = 0; i < 32; i++) {
            int from = i - amount;
            if (from >= 0) {
                Bit tmp = new Bit(false);
                source.getBitN(from, tmp);
                result.setBitN(i, tmp);
            }
        }
    }
}