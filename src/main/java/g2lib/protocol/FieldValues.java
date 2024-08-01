package g2lib.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FieldValues {
    public final List<FieldValue> values;

    public FieldValues(int count) {
        this.values = new ArrayList<>(count);
    }

    public FieldValues add(FieldValue v) {
        if (!values.isEmpty()) {
            values.getLast().field().guardAdd(v.field());
        }
        values.add(v);
        return this;
    }

    public FieldValues addAll(FieldValue... vs) {
        for (FieldValue f : vs) {
            add(f);
        }
        return this;
    }

    public Optional<FieldValue> get(FieldEnum f) {
        int idx = f.ordinal();
        if (values.size() > idx) {
            FieldValue fv = values.get(f.ordinal());
            if (fv.field() == f.field()) {
                return Optional.of(fv);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FieldValues && ((FieldValues) obj).values.equals(values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
