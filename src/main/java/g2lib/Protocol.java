package g2lib;

import g2lib.protocol.Field;
import g2lib.protocol.FieldEnum;
import g2lib.protocol.Fields;

import java.nio.ByteBuffer;

public class Protocol {

    public static BitBuffer section(int type, ByteBuffer buf) {
        int t = buf.get();
        if (t != type) {
            throw new IllegalArgumentException(String.format("Section incorrect %x %x",type,t));
        }
        return BitBuffer.sliceAhead(buf,buf.getShort());
    }

    public enum Cable implements FieldEnum {
        Color         (3),
        ModuleFrom    (8),
        ConnectorFrom (6),
        LinkType      (1),
        ModuleTo      (8),
        ConnectorTo   (6);
        Cable(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Cable.class,values());
    }

    public enum ModuleModes implements FieldEnum {
        Data;
        final private Field f = new Field(this,6);
        @Override
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ModuleModes.class,values());
    }


    public enum Module_ implements FieldEnum {

        Id        (8),
        Index     (8),
        Horiz     (7),
        Vert      (7),
        Color     (8),
        Uprate    (1),
        Leds      (1),
        Reserved  (6),
        ModeCount (4),
        Modes     (ModuleModes.FIELDS,ModeCount);

        Module_(int size) { f = new Field(this,size); }
        Module_(Fields fs, Module_ ixField) { f = new Field(this,fs,Field.fieldCount(ixField)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Module_.class,values());

    }

    public enum PatchDescription implements FieldEnum {
        //skip 7
        Reserved1(7*8),
        Reserved2(5),
        Voices(5),
        Height(14),
        Unk2(3),
        Red(1),
        Blue(1),
        Yellow(1),
        Orange(1),
        Green(1),
        Purple(1),
        White(1),
        Monopoly(2),
        Variation(8),
        Category(8);

        PatchDescription(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(PatchDescription.class,values());
    }

    public enum PatchParams implements FieldEnum {

        SectionCount(8),
        VariationCount(8),
        S1(),
        Morphs(MorphSettings.FIELDS,VariationCount),
        S2(),
        Section2(Settings2.FIELDS,VariationCount),
        S3(),
        Section3(Settings3.FIELDS,VariationCount),
        S4(),
        Section4(Settings4.FIELDS,VariationCount),
        S5(),
        Section5(Settings5.FIELDS,VariationCount),
        S6(),
        Section6(Settings6.FIELDS,VariationCount),
        S7(),
        Section7(Settings7.FIELDS,VariationCount);

        PatchParams(int size) { f = new Field(this,size); }
        PatchParams(Fields fs, PatchParams p) { f = new Field(this,fs,Field.fieldCount(p)); }
        PatchParams() { f = new Field(this,SectionHeader.FIELDS,Field.constant(1)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(PatchParams.class,values());
    }


    public enum Settings2 implements FieldEnum {
        Variation(8),
        PatchVol(7),
        ActiveMuted(7);
        Settings2(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Settings2.class,values());
    }

    public enum Settings3 implements FieldEnum {
        Variation(8),
        Glide(7),
        GlideTime(7);
        Settings3(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Settings3.class,values());
    }

    public enum Settings4 implements FieldEnum {
        Variation(8),
        Bend(7),
        Semi(7);
        Settings4(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Settings4.class,values());
    }


    public enum Settings5 implements FieldEnum {
        Variation(8),
        Vibrato(7),
        Cents(7),
        Rate(7);
        Settings5(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Settings5.class,values());
    }


    public enum Settings6 implements FieldEnum {
        Variation(8),
        Arpeggiator(7),
        Time(7),
        Type(7),
        Octaves(7);
        Settings6(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Settings6.class,values());
    }


    public enum Settings7 implements FieldEnum {
        Variation(8),
        OctShift(7),
        Sustain(7);
        Settings7(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Settings7.class,values());
    }

    public enum SectionHeader implements FieldEnum {
        Section(8),
        Entries(7);
        SectionHeader(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(SectionHeader.class,values());
    }


    public enum MorphSettings implements FieldEnum {
        Variation(8),
        Dials(),
        Modes();
        MorphSettings(int size) { f = new Field(this,size); }
        MorphSettings() { f = new Field(this, Data.FIELDS,Field.constant(8)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(MorphSettings.class,values());
    }

    public enum Data implements FieldEnum {
        Datum;
        Data() { this.f = new Field(this,7); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(Data.class,values());
    }

    public enum ModuleParams implements FieldEnum {
        SetCount(8),
        VariationCount(8),
        ParamSet(ModuleParams.SetCount);
        ModuleParams(int size) { f = new Field(this,size); }
        ModuleParams(ModuleParams ix) { f = new Field(this,ModuleParamSet.FIELDS,Field.fieldCount(ix)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ModuleParams.class,values());
    }

    public enum ModuleParamSet implements FieldEnum {
        ModIndex(8),
        ParamCount(7),
        ModParams;

        ModuleParamSet(int size) { f = new Field(this,size); }
        ModuleParamSet() { f = new Field(this,VarParams.FIELDS,Field.fieldCount(ModuleParams.VariationCount)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ModuleParamSet.class,values());
    }

    public enum VarParams implements FieldEnum {
        Variation(8),
        Params;
        VarParams(int size) { f = new Field(this,size); }
        VarParams() { f = new Field(this,Data.FIELDS,Field.fieldCount(ModuleParamSet.ParamCount)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(VarParams.class,values());
    }

    public enum MorphParameters implements FieldEnum {
        VariationCount(8),
        MorphCount(4),
        Reserved(20),
        VarMorphs(MorphParameters.VariationCount);
        MorphParameters(int size) { f = new Field(this,size); }
        MorphParameters(MorphParameters ix) { f = new Field(this,VarMorph.FIELDS,Field.fieldCount(ix)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(MorphParameters.class,values());
    }

    public enum VarMorph implements FieldEnum {
        Variation(4),
        Reserved0(24),
        Reserved1(24),
        Reserved2(8),
        MorphCount(8),
        VarMorphParams(VarMorph.MorphCount),
        Reserved3(4);
        VarMorph(int size) { f = new Field(this,size); }
        VarMorph(VarMorph ix) {
            f = new Field(this,VarMorphParam.FIELDS,Field.fieldCount(ix));
        }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(VarMorph.class,values());
    }

    public enum VarMorphParam implements FieldEnum {
        Location   (2),
        ModuleIndex(8),
        ParamIndex (7),
        Morph      (4),
        Range      (8);
        VarMorphParam(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(VarMorphParam.class,values());
    }

    public enum KnobAssignments implements FieldEnum {
        KnobCount(16),
        Knobs(KnobAssignments.KnobCount);
        KnobAssignments(int size) { f = new Field(this,size); }
        KnobAssignments(KnobAssignments ix) { f = new Field(this,KnobAssignment.FIELDS,Field.fieldCount(ix)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(KnobAssignments.class,values());
    }

    public enum KnobAssignment implements FieldEnum {
        Assigned(1),
        Params(KnobAssignment.Assigned);
        KnobAssignment(int size) { f = new Field(this,size); }
        KnobAssignment(KnobAssignment ix) { f = new Field(this,KnobParams.FIELDS,Field.fieldCount(ix)); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(KnobAssignment.class,values());
    }

    public enum KnobParams implements FieldEnum {
        Location(2),
        Index(8),
        IsLed(7),
        Param(4);
        KnobParams(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(KnobParams.class,values());
    }

    public enum ControlAssignments implements FieldEnum {
        NumControls(7),
        Assignments(ControlAssignment.FIELDS,ControlAssignments.NumControls);
        ControlAssignments(int size) { f = new Field(this,size); }
        ControlAssignments(Fields fs,ControlAssignments ix) { f = new Field(this,fs,ix); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ControlAssignments.class,values());
    }

    public enum ControlAssignment implements FieldEnum {
        MidiCC(7),
        Location(2),
        Index(8),
        Param(7);
        ControlAssignment(int size) { f = new Field(this,size); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ControlAssignment.class,values());
    }

    public enum ModuleNames implements FieldEnum {
        Reserved(6),
        NameCount(8),
        Names(ModuleName.FIELDS,ModuleNames.NameCount);
        ModuleNames(int size) { f = new Field(this,size); }
        ModuleNames(Fields fs,ModuleNames ix) { f = new Field(this,fs,ix); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ModuleNames.class,values());
    }

    public enum ModuleName implements FieldEnum {
        Name;
        ModuleName() { f = new Field(this,8); }
        private final Field f;
        public Field field() { return f; }
        public static final Fields FIELDS = new Fields(ModuleName.class,values());
    }

}