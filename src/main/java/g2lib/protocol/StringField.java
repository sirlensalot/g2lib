package g2lib.protocol;

import g2lib.BitBuffer;

import java.util.List;

public class StringField extends AbstractField implements Field {

    private final int length;

    public <T extends Enum<T>> StringField(Enum<T> e) {
        super(e);
        this.length = 0;
    }
    public <T extends Enum<T>> StringField(Enum<T> e, int length) {
        super(e);
        this.length = length;
    }

    @Override
    public String toString() {
        return String.format("%s: (String)",
                name());
    }

    @Override
    public void read(BitBuffer bb, List<FieldValues> values) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (bb.getBitsRemaining() >= 8) {
            if (length > 0 && i++ > length) { break; }
            int c = bb.get(8);
            if (c != 0) {
                sb.append(Character.valueOf((char) c));
            } else {
                if (length <= 0) { break; }
            }
        }
        values.getFirst().add(new StringValue(this, sb.toString()));
    }

    @Override
    public Type type() {
        return Type.StringType;
    }
}
