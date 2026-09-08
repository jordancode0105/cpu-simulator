public class Adder {

    public static void subtract(Word32 a, Word32 b, Word32 result) {
        if (a == null || b == null || result == null)
            return;

        // nb = ~b
        Word32 nb = new Word32();
        Word32.not(b, nb);

        Word32 temp = new Word32();
        add(a, nb, temp);

        Word32 one = new Word32();
        one.setBitN(31, new Bit(true));

        add(temp, one, result);
    }

    public static void add(Word32 a, Word32 b, Word32 result) {
        if (a == null || b == null || result == null)
            return;

        // carry starts at 0
        Bit carry = new Bit(false);

        for (int i = 31; i >= 0; i--) {
            Bit ai = new Bit(false);
            Bit bi = new Bit(false);

            a.getBitN(i, ai);
            b.getBitN(i, bi);

            Bit axb = new Bit(false);
            Bit.xor(ai, bi, axb);

            Bit sum = new Bit(false);
            Bit.xor(axb, carry, sum);

            // write sum bit
            result.setBitN(i, sum);

            // carryOut = (ai AND bi) OR (carry AND (ai XOR bi))
            Bit c1 = new Bit(false);
            Bit.and(ai, bi, c1);

            Bit c2 = new Bit(false);
            Bit.and(axb, carry, c2);

            Bit carryOut = new Bit(false);
            Bit.or(c1, c2, carryOut);

            carry = new Bit(carryOut.getValue());
        }
        // overflow carry ignored
    }
}