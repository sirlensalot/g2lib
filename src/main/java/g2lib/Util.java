package g2lib;

import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class Util {
    public static void dumpBuffer(ByteBuffer buffer) {
        StringBuilder hex = new StringBuilder();
        StringBuilder ascii = new StringBuilder();
        int pos = buffer.position();
        buffer.rewind();
        int i = 0;
        while (buffer.hasRemaining()) {
            byte d = buffer.get();
            hex.append(String.format("%02x ", d));
            ascii.append((d >= 33 && d < 126) ? String.format("%c ", d) : ". ");
            if (i % 16 == 15) {
                System.out.printf("%s  %s\n", hex, ascii);
                hex = new StringBuilder();
                ascii = new StringBuilder();
            }
            i++;
        }
        if (i % 16 > 0) {
            int pad = 3 * (16 - (i % 16));
            //System.out.printf("pad %d %d\n",pad,i % 16);
            System.out.printf("%s %" + pad + "s %s\n", hex, "", ascii);
        }
        buffer.position(pos);
    }

    public static int b2i(byte b) {
        return b & 0xff;
    }

    public static int addb(byte msb, byte lsb) {
        return (b2i(msb) << 8) + b2i(lsb);
    }

    public static int getShort(ByteBuffer buffer) {
        return addb(buffer.get(),buffer.get());
    }

    public static byte[] asBytes(int... vals) {
        byte[] bytes = new byte[vals.length];
        for (int i = 0; i < vals.length; i++) {
            bytes[i] = (byte) vals[i];
        }
        return bytes;
    }

    public static String asBinary(int i) {
        StringBuilder b = new StringBuilder(Integer.toBinaryString(i));
        while (b.length() < 8) {
            b.insert(0, '0');
        }
        return b.toString();
    }

    public static ByteBuffer readFile(String path) throws Exception {
        ByteBuffer buf;
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] bs = fis.readAllBytes();
            buf = ByteBuffer.wrap(bs);
        }
        return buf;
    }

}
