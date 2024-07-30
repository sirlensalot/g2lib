package g2lib.protocol;

import java.util.List;
import java.util.Optional;

public interface FieldEnum {
    Field field();
    int ordinal();

    default Optional<FieldValue> get(FieldValues values) {
        return values.get(this);
    }

    default Optional<Integer> intValue(FieldValues values) {
        return get(values).flatMap(fv -> Optional.of(IntValue.intValue(fv)));
    }

    default Optional<String> stringValue(FieldValues values) {
        return get(values).flatMap(fv -> Optional.of(StringValue.stringValue(fv)));
    }

    default Optional<List<FieldValues>> subfieldsValue(FieldValues values) {
        return get(values).flatMap(fv -> Optional.of(SubfieldsValue.subfieldsValue(fv)));
    }
}
