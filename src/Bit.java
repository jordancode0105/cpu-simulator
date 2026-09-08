public class Bit {

    private boolean value;

    public Bit(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void assign(boolean value) {
        this.value = value;
    }

    public void and(Bit b2, Bit result) {
        and(this, b2, result);
    }

    public static void and(Bit b1, Bit b2, Bit result) {
        if (b1.value) {
            result.value = b2.value;
        } else
            result.value = false;
    }

    public void or(Bit b2, Bit result) {
        or(this, b2, result);
    }

    public static void or(Bit b1, Bit b2, Bit result) {
        if (b1.value)
            result.value = true;
        else {
            result.value = b2.value;
        }
    }

    public void xor(Bit b2, Bit result) {
        xor(this, b2, result);
    }

    public static void xor(Bit b1, Bit b2, Bit result) {
        if (b1.value) {
            result.value = !b2.value;
        } else {
            result.value = b2.value;
        }
    }

    public static void not(Bit b2, Bit result) {
        result.value = !b2.value;
    }

    public void not(Bit result) {
        not(this, result);
    }

    public String toString() {
        if (value)
            return "1";
        else
            return "0";
    }
}
