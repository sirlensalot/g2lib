package g2lib.protocol;

import java.util.List;

public class FieldValue {
    public final Field field;
    public final Integer value;
    public final List<FieldValues> array;

    public FieldValue(Field field, Integer value) {
        this.field = field;
        this.value = value;
        this.array = null;
    }

    public FieldValue(Field field, List<FieldValues> array) {
        this.field = field;
        this.array = array;
        this.value = null;
    }

    public int getValue() {
        if (value != null) {
            return value;
        }
        throw new RuntimeException("not value: " + this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(field.name()).append(": ");
        if (value != null) {
            return sb.append(value).toString();
        }
        return sb.append(array).toString();
    }

    public List<FieldValues> getValues() {
        if (array != null) {
            return array;
        }
        throw new RuntimeException("not array: " + this);
    }
}
