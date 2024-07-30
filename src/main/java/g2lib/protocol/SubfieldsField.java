package g2lib.protocol;

import g2lib.BitBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SubfieldsField implements Field {
    private final Fields subfields;
    private final SubfieldCount subfieldCount;
    public final Enum<?> enum_;

    public interface SubfieldCount {
        int getCount(List<FieldValues> values);
    }

    public static class ConstantSubfieldCount implements SubfieldCount {
        private final int count;
        public ConstantSubfieldCount(int count) {
            this.count = count;
        }
        @Override
        public int getCount(List<FieldValues> values) {
            return count;
        }
    }

    public static SubfieldCount constant(int count) {
        return new ConstantSubfieldCount(count);
    }

    public record FieldCount(FieldEnum f) implements SubfieldCount {
        @Override
        public int getCount(List<FieldValues> values) {
            for (FieldValues fv : values) {
                Optional<FieldValue> v = fv.get(f);
                if (v.isPresent()) { return v.get().getValue(); }
            }
            throw new NoSuchElementException(f.field().name());
        }
    }

    public static SubfieldCount fieldCount(FieldEnum e) {
        return new FieldCount(e);
    }

    public <T extends Enum<T>> SubfieldsField(Enum<T> e, Fields subfields, FieldEnum subfieldIxField) {
        this(e,subfields,fieldCount(subfieldIxField));
    }

    public <T extends Enum<T>> SubfieldsField(Enum<T> e, Fields subfields, int size) {
        this(e,subfields,constant(size));
    }

    private <T extends Enum<T>> SubfieldsField(Enum<T> e, Fields subfields, SubfieldCount subfieldCount) {
        this.subfields = subfields;
        this.subfieldCount = subfieldCount;
        this.enum_ = e;
    }

    @Override
    public String toString() {
        return String.format("%s: %s",
                enum_.name(), subfields);
    }

    @Override
    public String name() {
        return String.format("%s.%s",enum_.getDeclaringClass().getSimpleName(),enum_.name());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SubfieldsField && ((SubfieldsField) obj).enum_ == enum_;
    }

    @Override
    public int hashCode() {
        return enum_.hashCode();
    }

    @Override
    public void read(BitBuffer bb, List<FieldValues> values) {
        int count = subfieldCount.getCount(values);
        List<FieldValues> vs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            vs.add(subfields.read(bb,values));
        }
        values.getFirst().add(new FieldValue(this, vs));
    }

}
