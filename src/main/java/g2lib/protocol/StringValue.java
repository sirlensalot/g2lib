package g2lib.protocol;

public record StringValue(Field field, String value) implements FieldValue {

    @Override
    public String toString() {
        return field.name() + ": " + value;
    }

    public static String stringValue(FieldValue f) {
        if (f instanceof StringValue) {
            return ((StringValue) f).value();
        }
        throw new UnsupportedOperationException("Not StringValue: " + f);
    }

}
