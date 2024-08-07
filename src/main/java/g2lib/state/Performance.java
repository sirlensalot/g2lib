package g2lib.state;

import g2lib.Util;

public class Performance {
    private final int version;

    public Performance(byte version) {
        this.version = Util.b2i(version);
    }
}
