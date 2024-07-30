package g2lib.protocol;

import g2lib.BitBuffer;

import java.util.List;

public class StringField extends AbstractField implements Field {

    public <T extends Enum<T>> StringField(Enum<T> e) {
        super(e);
    }


    @Override
    public String toString() {
        return String.format("%s: (String)",
                name());
    }

    @Override
    public void read(BitBuffer bb, List<FieldValues> values) {
        StringBuilder sb = new StringBuilder();
        int c = -1;
        for (int j = 0; j < 16 && (c=bb.get())!=0; j++) {
            sb.append(Character.valueOf((char) c));
        }
        values.getFirst().add(new FieldValue(this, sb.length()));
    }

}
