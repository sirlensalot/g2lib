package g2lib.protocol;

import java.util.List;
import java.util.Optional;

public interface FieldEnum {
    Field field();
    int ordinal();

    default Optional<FieldValue> get(FieldValues values) {
        return values.get(this);
    }

    default Optional<Integer> value(FieldValues values) {
        return get(values).flatMap(fv -> Optional.of(fv.getValue()));
    }

    default Optional<List<FieldValues>> values(FieldValues values) {
        return get(values).flatMap(fv -> Optional.of(fv.getValues()));
    }
}
