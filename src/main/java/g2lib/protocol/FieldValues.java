package g2lib.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FieldValues {
    public final List<FieldValue> values;

    public FieldValues(int count) {
        this.values = new ArrayList<>(count);
    }

    public void add(FieldValue v) {
        values.add(v);
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
}
