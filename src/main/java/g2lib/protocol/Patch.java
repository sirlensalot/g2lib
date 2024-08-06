package g2lib.protocol;

import g2lib.BitBuffer;
import g2lib.CRC16;
import g2lib.Util;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

import static g2lib.Protocol.*;

public class Patch {

    private static final Logger log = Util.getLogger(Patch.class);

    public static ByteBuffer patchHeader() {
        ByteBuffer header = ByteBuffer.allocate(80);
        for (String s : new String[]{
                "Version=Nord Modular G2 File Format 1",
                "Type=Patch",
                "Version=23",
                "Info=BUILD 320"
        }) {
            for (char c : s.toCharArray()) {
                header.put((byte) c);
            }
            header.put((byte)0x0d).put((byte)0x0a);
        }
        header.put((byte)0);
        header.rewind();
        return header.asReadOnlyBuffer();
    }

    public static final ByteBuffer HEADER = patchHeader();

    public static record Section(Sections sections, FieldValues values) {

    }

    public static enum Sections {

        SPatchDescription(PatchDescription.FIELDS,0x21  ),
        SModuleList1(ModuleList.FIELDS,0x4a,1),
        SModuleList0(ModuleList.FIELDS,0x4a,0),
        SCurrentNote(CurrentNote.FIELDS,0x69  ),
        SCableList1(CableList.FIELDS,0x52,1),
        SCableList0(CableList.FIELDS,0x52,0),
        SPatchParams(PatchParams.FIELDS,0x4d,2),
        SModuleParams1(ModuleParams.FIELDS,0x4d,1),
        SModuleParams0(ModuleParams.FIELDS,0x4d,0),
        SMorphParameters(MorphParameters.FIELDS,0x65  ),
        SKnobAssignments(KnobAssignments.FIELDS,0x62  ),
        SControlAssignments(ControlAssignments.FIELDS,0x60  ),
        SMorphLabels(MorphLabels.FIELDS,0x5b,2),
        SModuleLabels1(ModuleLabels.FIELDS,0x5b,1),
        SModuleLabels0(ModuleLabels.FIELDS,0x5b,0),
        SModuleNames1(ModuleNames.FIELDS,0x5a,1),
        SModuleNames0(ModuleNames.FIELDS,0x5a,0),
        STextPad(TextPad.FIELDS,0x6f  );

        private final Fields fields;
        public final int type;
        public final Integer location;
        Sections(Fields fields, int type, int location) {
            this.fields = fields;
            this.type = type;
            this.location = location;
        }
        Sections(Fields fields, int type) {
            this.fields = fields;
            this.type = type;
            this.location = null;
        }

        @Override
        public String toString() {
            return String.format("%s[%x%s]",
                    name(),
                    type,
                    location != null ? (":" + location) : "");
        }
    }

    public static final Sections[] FILE_SECTIONS = Sections.values();

    public static final Sections[] MSG_SECTIONS = new Sections[] {
            Sections.SPatchDescription,
            Sections.SModuleList1,
            Sections.SModuleList0,
            Sections.SCableList1,
            Sections.SCableList0,
            Sections.SPatchParams,
            Sections.SModuleParams1,
            Sections.SModuleParams0,
            Sections.SMorphParameters,
            Sections.SKnobAssignments,
            Sections.SControlAssignments,
            Sections.SModuleNames1,
            Sections.SModuleNames0,
            Sections.SMorphLabels,
            Sections.SModuleLabels1,
            Sections.SModuleLabels0
    };

    public final LinkedHashMap<Sections,Section> sections = new LinkedHashMap<>();
    public String text;
    public String name;
    public int slot = -1;

    public static <T> T withSliceAhead(ByteBuffer buf, int length, Function<ByteBuffer,T> f) {
        return f.apply(Util.sliceAhead(buf,length));
    }

    public static void expectWarn(ByteBuffer buf,int expected,String filePath, String msg) {
        byte b = buf.get();
        if (b != expected) {
            log.warning(String.format("%s: expected %x, found %x at %s:%d",msg,expected,filePath,buf.position()-1));
        }
    }

    public static Patch readFromMessage(ByteBuffer buf) throws Exception {
        Patch patch = new Patch();
        patch.slot = readMessageHeader(buf);

        for (Sections ss : MSG_SECTIONS) {
            patch.readSection(buf,ss);
            if (ss == Sections.SPatchDescription) {
                expectWarn(buf,0x2d,"Message","USB extra 1");
                expectWarn(buf,0x00,"Message","USB extra 2");
            }
        }
        return patch;
    }

    public static int readMessageHeader(ByteBuffer buf) throws Exception {
        expectWarn(buf,0x01,"Message","Cmd");
        int slot = buf.get();
        expectWarn(buf,0x00,"Message","PatchVersion");
        return slot;
    }

    public static Patch readFromFile(String filePath) throws Exception {
        ByteBuffer fileBuffer = Util.readFile(filePath);
        withSliceAhead(fileBuffer,HEADER.limit(),buf -> {
            if (!HEADER.rewind().equals(buf.rewind())) {
                throw new RuntimeException("Unexpected file header: " + Util.dumpBufferString(buf));
            }
            return true;
        });

        ByteBuffer slice = fileBuffer.slice();
        int crc = CRC16.crc16(slice,0,slice.limit()-2);

        expectWarn(fileBuffer,0x17,filePath,"header");
        expectWarn(fileBuffer,0x00,filePath,"header");
        Patch patch = new Patch();

        for (Sections ss : FILE_SECTIONS) {
            patch.readSection(fileBuffer,ss);
        }

        int fcrc = Util.getShort(fileBuffer);
        if (fcrc != crc) {
            throw new RuntimeException(String.format("CRC mismatch: %x %x",crc,fcrc));
        }

        return patch;
    }


    public static BitBuffer sliceSection(int type, ByteBuffer buf) {
        int t = buf.get();
        if (t != type) {
            throw new IllegalArgumentException(String.format("Section incorrect %x %x",type,t));
        }
        return BitBuffer.sliceAhead(buf,Util.getShort(buf));
    }

    public void writeSection(ByteBuffer buf, Sections s) throws  Exception {
        Section ss = getSection(s);
        if (ss == null) {
            throw new IllegalArgumentException("No section in patch: " + s);
        }
        BitBuffer bb = new BitBuffer(1024);
        if (s.location != null) {
            bb.put(2,s.location);
        }
        FieldValues fvs = ss.values;
        for (FieldValue fv : fvs.values) {
            fv.write(bb);
        }
        ByteBuffer bbuf = bb.toBuffer();
//        log.info(String.format("Wrote: %s, len=%x, crc=%x: %s\n",s,bb.limit(),CRC16.crc16(bbuf),Util.dumpBufferString(bbuf)));
//        if (s == Sections.SMorphLabels || s == Sections.SModuleLabels0) {
//            Util.dumpAllShifts(bbuf.rewind());
//        }

        buf.put((byte) s.type);
        Util.putShort(buf,bbuf.limit());
        bbuf.rewind();
        while(bbuf.hasRemaining()) {
            buf.put(bbuf.get());
        }


    }

    public void readSection(ByteBuffer buf, Sections s) throws Exception {
        BitBuffer bb = sliceSection(s.type,buf);
        //log.info(s + ": length " + bb.limit());
        if (s.location != null) {
            Integer loc = bb.get(2);
            if (!loc.equals(s.location)) {
                throw new IllegalArgumentException(String.format("Bad location: %x, %s",loc,s));
            }
        }
        FieldValues fvs = s.fields.read(bb);
//        log.info(String.format("Read: %s, len=%x, crc=%x: %s\n",s,bb.limit(),CRC16.crc16(bb.toBuffer()),
//                Util.dumpBufferString(bb.toBuffer())));
//        if (s == Sections.SMorphLabels || s == Sections.SModuleLabels0) {
//            Util.dumpAllShifts(bb.toBuffer());
//        }
        sections.put(s,new Section(s,fvs));
    }

    public void readSectionMessage(ByteBuffer buf, Sections s) throws Exception {
        int slot = readMessageHeader(buf);
        if (this.slot != slot) {
            throw new IllegalArgumentException(String.format("Slot mismatch: %d, %d: %s",this.slot,slot,s));
        }
        readSection(buf,s);
    }

    public Section getSection(Sections key) {
        return sections.get(key);
    }


}
