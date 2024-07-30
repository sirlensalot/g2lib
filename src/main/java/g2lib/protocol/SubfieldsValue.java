package g2lib.protocol;

import java.util.List;

public record SubfieldsValue(Field field, List<FieldValues> value) implements FieldValue {

    @Override
    public String toString() {
        return field.name() + ": " + value;
    }
    public static List<FieldValues> subfieldsValue(FieldValue f) {
        if (f instanceof SubfieldsValue) {
            return ((SubfieldsValue) f).value();
        }
        throw new UnsupportedOperationException("Not SubfieldsValue: " + f);
    }
}
