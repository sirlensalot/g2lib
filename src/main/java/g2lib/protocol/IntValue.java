package g2lib.protocol;

public record IntValue(Field field, int value) implements FieldValue {

    @Override
    public String toString() {
        return field.name() + ": " + value;
    }

    public static int intValue(FieldValue f) {
        if (f instanceof IntValue) {
            return ((IntValue) f).value();
        }
        throw new UnsupportedOperationException("Not IntValue: " + f);
    }

}
