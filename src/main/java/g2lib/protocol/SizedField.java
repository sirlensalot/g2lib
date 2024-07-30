package g2lib.protocol;

import g2lib.BitBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SizedField implements Field {
    private final int size;
    public final Enum<?> enum_;

    public <T extends Enum<T>> SizedField(Enum<T> e, int size) {
        this.size = size;
        this.enum_ = e;
    }


    @Override
    public String toString() {
        return String.format("%s: %d",
                enum_.name(), size);
    }

    @Override
    public String name() {
        return String.format("%s.%s",enum_.getDeclaringClass().getSimpleName(),enum_.name());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SizedField && ((SizedField) obj).enum_ == enum_;
    }

    @Override
    public int hashCode() {
        return enum_.hashCode();
    }

    @Override
    public void read(BitBuffer bb, List<FieldValues> values) {
        values.getFirst().add(new FieldValue(this, bb.get(size)));
    }

}
