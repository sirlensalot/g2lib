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

    default FieldValue value(int v) {
        return new IntValue((SizedField) field().guardType(Field.Type.IntType),v);
    }

    default FieldValue value(String v) {
        return new StringValue((StringField) field().guardType(Field.Type.StringType),v);
    }

    default FieldValue value(List<FieldValues> v) {
        return new SubfieldsValue((SubfieldsField) field().guardType(Field.Type.SubfieldType),v);
    }
}
