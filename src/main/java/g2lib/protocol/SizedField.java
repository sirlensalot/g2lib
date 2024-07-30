package g2lib.protocol;

import g2lib.BitBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SizedField extends AbstractField implements Field {
    private final int size;

    public <T extends Enum<T>> SizedField(Enum<T> e, int size) {
        super(e);
        this.size = size;
    }


    @Override
    public String toString() {
        return String.format("%s: %d",
                name(), size);
    }

    @Override
    public void read(BitBuffer bb, List<FieldValues> values) {
        values.getFirst().add(new FieldValue(this, bb.get(size)));
    }

}
