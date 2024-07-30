package g2lib.protocol;

import g2lib.BitBuffer;

import java.util.List;

public interface Field {
    String name();

    void read(BitBuffer bb, List<FieldValues> values);
}
