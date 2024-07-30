package g2lib;

import g2lib.protocol.FieldEnum;
import g2lib.protocol.FieldValues;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static g2lib.Protocol.*;
import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {

    public static int assertFieldEquals(FieldValues values, int expected, FieldEnum field) {
        int actual = assertValue(values, field);
        assertEquals(String.format("%#02x",expected),
                String.format("%#02x",actual)
                ,field.toString());
        return actual;
    }


    private void xassertEquals(int expected, int actual, Object... msgs) {
        String msg = String.join(".",Arrays.stream(msgs).map(Object::toString).toList());
        System.out.printf("%-16s: %#02x\n",msg,actual);
    }


    private static int assertValue(FieldValues values, FieldEnum field) {
        assertTrue(field.value(values).isPresent(),"value found: " + field);
        return field.value(values).get();
    }

    public static List<FieldValues> assertSubfields(FieldValues fv, int size, FieldEnum field) {
        Optional<List<FieldValues>> o = field.values(fv);
        assertTrue(o.isPresent(),"subfields not found: " + field);
        List<FieldValues> fvs = o.get();
        assertEquals(size,fvs.size(),"size: " + field);
        return fvs;
    }



    private static List<FieldValues> assertVarParams(List<FieldValues> mps, int vc, int modIndex, int paramCount) {
        FieldValues mps1 = mps.removeFirst();
        assertFieldEquals(mps1, modIndex,ModuleParamSet.ModIndex);
        assertFieldEquals(mps1, paramCount, ModuleParamSet.ParamCount);
        return assertSubfields(mps1, vc, ModuleParamSet.ModParams);
    }

    private static void assertModParams(int variation, List<FieldValues> vps, Integer... expecteds) {
        FieldValues vp = vps.get(variation);
        //System.out.println(vp);
        assertFieldEquals(vp, variation,VarParams.Variation);
        List<Integer> el = Arrays.asList(expecteds);
        //System.out.println(el);
        List<FieldValues> ps = assertSubfields(vp, el.size(), VarParams.Params);
        List<Integer> al = ps.stream().map(fv -> assertValue(fv,Data.Datum)).toList();
        assertEquals(el,al,"mod params var " + variation);
    }

    private void sectionHeader(FieldValues vs, PatchParams f, int section, int entries) {
        assertTrue(f.values(vs).isPresent(),"header present: " + f);
        FieldValues h = f.values(vs).get().getFirst();
        assertFieldEquals(h,section,SectionHeader.Section);
        assertFieldEquals(h,entries,SectionHeader.Entries);
    }


    @Test
    void patchDesc() throws Exception {

        ByteBuffer buf;
        try (FileInputStream fis = new FileInputStream("data/patchdesc.msg")) {
            byte[] bs = fis.readAllBytes();
            buf = ByteBuffer.wrap(bs);
            //Util.dumpBuffer(buf);
        }

        assertEquals(0x01,buf.get()); // cmd
        assertEquals(0x09,buf.get()); // slot 1
        assertEquals(0x00,buf.get()); // patch version
        BitBuffer bb;
        bb = section(0x21,buf);
        assertEquals(0x000f,bb.limit()); //length

        FieldValues pd = PatchDescription.FIELDS.read(bb);
        assertFieldEquals(pd,0x10000, PatchDescription.Reserved1);
        assertFieldEquals(pd,0x04, PatchDescription.Reserved2);
        assertFieldEquals(pd,0x05, PatchDescription.Voices);
        assertFieldEquals(pd,374, PatchDescription.Height);
        assertFieldEquals(pd,0x01, PatchDescription.Unk2);
        assertFieldEquals(pd,0x01, PatchDescription.Red);
        assertFieldEquals(pd,0x01, PatchDescription.Blue);
        assertFieldEquals(pd,0x01, PatchDescription.Yellow);
        assertFieldEquals(pd,0x01, PatchDescription.Orange);
        assertFieldEquals(pd,0x01, PatchDescription.Green);
        assertFieldEquals(pd,0x01, PatchDescription.Purple);
        assertFieldEquals(pd,0x01, PatchDescription.White);
        assertFieldEquals(pd,0x00, PatchDescription.Monopoly);
        assertFieldEquals(pd,0x01, PatchDescription.Variation);
        assertFieldEquals(pd,0x00, PatchDescription.Category);


        assertEquals(0x2d,buf.get(),"USB extra 1");
        assertEquals(0x00,buf.get(), "USB extra 2");

        bb = section(0x4a,buf);
        assertEquals(27,bb.limit(), "module list len");
        assertEquals(1,bb.get(2),"location");
        assertEquals(4,bb.get(),"num modules");

        FieldValues module;
        List<FieldValues> modes;

        //Util.dumpBuffer(b2);
        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0x5c, Module_.Id); //filter classic
        assertFieldEquals(module,0x01, Module_.Index);
        assertFieldEquals(module,0x00, Module_.Horiz);
        assertFieldEquals(module,0x09, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x00, Module_.Uprate);
        assertFieldEquals(module,0x00, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x00, Module_.ModeCount);
        assertSubfields(module, 0, Module_.Modes);

        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0x09, Module_.Id); //osc c
        assertFieldEquals(module,0x02, Module_.Index);
        assertFieldEquals(module,0x00, Module_.Horiz);
        assertFieldEquals(module,0x06, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x00, Module_.Uprate);
        assertFieldEquals(module,0x00, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x01, Module_.ModeCount);
        modes = assertSubfields(module, 1, Module_.Modes);
        assertFieldEquals(modes.getFirst(),0x02, ModuleModes.Data);

        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0x17, Module_.Id);  // ModADSR
        assertFieldEquals(module,0x03, Module_.Index);
        assertFieldEquals(module,0x00, Module_.Horiz);
        assertFieldEquals(module,0x0d, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x01, Module_.Uprate);
        assertFieldEquals(module,0x00, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x00, Module_.ModeCount);
        assertSubfields(module, 0, Module_.Modes);


        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0x04, Module_.Id); // 2-out
        assertFieldEquals(module,0x04, Module_.Index);
        assertFieldEquals(module,0x00, Module_.Horiz);
        assertFieldEquals(module,0x12, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x00, Module_.Uprate);
        assertFieldEquals(module,0x01, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x00, Module_.ModeCount);
        assertSubfields(module, 0, Module_.Modes);

        bb = section(0x4a,buf);
        assertEquals(21,bb.limit(), "module list0 len");
        assertEquals(0,bb.get(2),"location");
        assertEquals(3,bb.get(),"num modules");


        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0x7f, Module_.Id); // FX Input
        assertFieldEquals(module,0x01, Module_.Index);
        assertFieldEquals(module,0x01, Module_.Horiz);
        assertFieldEquals(module,0x02, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x00, Module_.Uprate);
        assertFieldEquals(module,0x01, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x00, Module_.ModeCount);
        assertSubfields(module, 0, Module_.Modes);


        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0xb6, Module_.Id);//Delay Stereo
        assertFieldEquals(module,0x02, Module_.Index);
        assertFieldEquals(module,0x01, Module_.Horiz);
        assertFieldEquals(module,0x04, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x00, Module_.Uprate);
        assertFieldEquals(module,0x00, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x01, Module_.ModeCount);
        modes = assertSubfields(module, 1, Module_.Modes);
        assertFieldEquals(modes.getFirst(),0x00, ModuleModes.Data);


        module = Module_.FIELDS.read(bb);
        assertFieldEquals(module,0x04, Module_.Id); //2-out
        assertFieldEquals(module,0x03, Module_.Index);
        assertFieldEquals(module,0x01, Module_.Horiz);
        assertFieldEquals(module,0x09, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x00, Module_.Uprate);
        assertFieldEquals(module,0x01, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x00, Module_.ModeCount);
        assertSubfields(module, 0, Module_.Modes);

        //52 should be next, CABLE_LIST
        bb = section(0x52,buf);
        assertEquals(0xf,bb.limit(), "cable list1 len");
        assertEquals(1,bb.get(2),"location");
        assertEquals(0,bb.get(12),"unknown");
        assertEquals(3,bb.get(10),"num cables");

        FieldValues cable = Cable.FIELDS.read(bb);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x02, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x01, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        cable = Cable.FIELDS.read(bb);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x01, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x03, Cable.ModuleTo);
        assertFieldEquals(cable,0x05, Cable.ConnectorTo);

        cable = Cable.FIELDS.read(bb);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x03, Cable.ModuleFrom);
        assertFieldEquals(cable,0x01, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x04, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        bb = section(0x52,buf);
        assertEquals(0xf,bb.limit(), "cable list0 len");
        assertEquals(0,bb.get(2),"location");
        assertEquals(0,bb.get(12),"unknown");
        assertEquals(3,bb.get(10),"num cables");

        cable = Cable.FIELDS.read(bb);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x01, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x02, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        cable = Cable.FIELDS.read(bb);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x02, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x03, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        cable = Cable.FIELDS.read(bb);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x02, Cable.ModuleFrom);
        assertFieldEquals(cable,0x01, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x03, Cable.ModuleTo);
        assertFieldEquals(cable,0x01, Cable.ConnectorTo);

        bb = section(0x4d,buf); //param list
        assertEquals(357,bb.limit(), "parameters2 len");
        assertEquals(2,bb.get(2),"location"); //patch parameters


        FieldValues patchSettings = PatchParams.FIELDS.read(bb);
        //System.out.println(patchSettings);
        int vc = assertFieldEquals(patchSettings,0x0a, PatchParams.VariationCount);
        assertFieldEquals(patchSettings,0x07, PatchParams.SectionCount);
        sectionHeader(patchSettings, PatchParams.S1,1,16);
        sectionHeader(patchSettings, PatchParams.S2,2,2);
        sectionHeader(patchSettings, PatchParams.S3,3,2);
        sectionHeader(patchSettings, PatchParams.S4,4,2);
        sectionHeader(patchSettings, PatchParams.S5,5,3);
        sectionHeader(patchSettings, PatchParams.S6,6,4);
        sectionHeader(patchSettings, PatchParams.S7,7,2);
        List<FieldValues> morphs = assertSubfields(patchSettings, vc, PatchParams.Morphs);
        List<FieldValues> s2 = assertSubfields(patchSettings, vc, PatchParams.Section2);
        List<FieldValues> s3 = assertSubfields(patchSettings, vc, PatchParams.Section3);
        List<FieldValues> s4 = assertSubfields(patchSettings, vc, PatchParams.Section4);
        List<FieldValues> s5 = assertSubfields(patchSettings, vc, PatchParams.Section5);
        List<FieldValues> s6 = assertSubfields(patchSettings, vc, PatchParams.Section6);
        List<FieldValues> s7 = assertSubfields(patchSettings, vc, PatchParams.Section7);
        for (int i = 0; i < vc; i++) {
            FieldValues ms = morphs.get(i);
            assertFieldEquals(ms,i,MorphSettings.Variation);
            List<FieldValues> mdials = assertSubfields(ms, 8, MorphSettings.Dials);
            List<FieldValues> mmodes = assertSubfields(ms, 8, MorphSettings.Modes);
            for (int j = 0; j < 8; j++) {
                assertFieldEquals(mdials.get(j),0x00, Data.Datum);
                assertFieldEquals(mmodes.get(j),0x01, Data.Datum);
            }
            assertFieldEquals(s2.get(i),i,Settings2.Variation);
            assertFieldEquals(s3.get(i),i,Settings3.Variation);
            assertFieldEquals(s4.get(i),i,Settings4.Variation);
            assertFieldEquals(s5.get(i),i,Settings5.Variation);
            assertFieldEquals(s6.get(i),i,Settings6.Variation);
            assertFieldEquals(s7.get(i),i,Settings7.Variation);

            assertFieldEquals(s3.get(i),0x00,Settings3.Glide);
            assertFieldEquals(s3.get(i),0x1c,Settings3.GlideTime);

            assertFieldEquals(s5.get(i),0x00,Settings5.Vibrato);
            assertFieldEquals(s5.get(i),0x32,Settings5.Cents);
            assertFieldEquals(s5.get(i),0x40,Settings5.Rate);

            assertFieldEquals(s6.get(i),0x00,Settings6.Arpeggiator);
            assertFieldEquals(s6.get(i),0x03,Settings6.Time);
            assertFieldEquals(s6.get(i),0x00,Settings6.Type);
            assertFieldEquals(s6.get(i),0x00,Settings6.Octaves);

            if (i == 1) {
                assertFieldEquals(s2.get(i),0x00,Settings2.PatchVol);
            } else {
                assertFieldEquals(s2.get(i),0x64,Settings2.PatchVol);
            }
            assertFieldEquals(s2.get(i),0x01,Settings2.ActiveMuted);
            if (i == 0 || i == 1) {
                assertFieldEquals(s4.get(i),0x05,Settings4.Semi);
                assertFieldEquals(s7.get(i),0x01,Settings7.OctShift);
            } else {
                assertFieldEquals(s4.get(i),0x01,Settings4.Semi);
                assertFieldEquals(s7.get(i),0x02,Settings7.OctShift);
            }

            assertFieldEquals(s4.get(i),0x01,Settings4.Bend);

            assertFieldEquals(s7.get(i),0x01,Settings7.Sustain);

        }

        bb = section(0x4d,buf); //param list
        assertEquals(286,bb.limit(), "parameters1 len");
        assertEquals(1,bb.get(2),"location"); //loc1 parameters

        FieldValues modParams = ModuleParams.FIELDS.read(bb);

        assertFieldEquals(modParams,0x04,ModuleParams.SetCount);
        assertFieldEquals(modParams,vc,ModuleParams.VariationCount);
        List<FieldValues> mps = assertSubfields(modParams, 4, ModuleParams.ParamSet);

        List<FieldValues> vps;
        int v = 0;
        vps = assertVarParams(mps, vc, 1, 6); //filter classic
        assertModParams(v++,vps,58,0,1,11,0,1);
        assertModParams(v++,vps,58,0,1,11,0,1);
        while (v < vc) {
            assertModParams(v++,vps,75,0,0,0,2,1);
        }

        vps = assertVarParams(mps, vc, 2, 8); //Osc C
        v = 0;
        assertModParams(v++,vps,76,64,1,0,0,1,0,0);
        assertModParams(v++,vps,76, 0,1,0,0,1,0,0);
        while (v < vc) {
            assertModParams(v++,vps,64,64,1,0,0,1,0,0);
        }

        vps = assertVarParams(mps, vc, 3, 10); //ADSR
        v = 0;
        while (v < vc) {
            assertModParams(v++,vps,0, 54, 100, 14, 0, 0, 0, 0, 0, 1);
        }

        vps = assertVarParams(mps, vc, 4, 3); //2-out
        v = 0;
        assertModParams(v++,vps,3,1,1);
        assertModParams(v++,vps,3,1,1);
        while (v < vc) {
            assertModParams(v++,vps,0,1,0);
        }

        bb = section(0x4d,buf); //param list
        assertEquals(187,bb.limit(), "parameters0 len");
        assertEquals(0,bb.get(2),"location"); //loc0 parameters

        modParams = ModuleParams.FIELDS.read(bb);

        assertFieldEquals(modParams,3,ModuleParams.SetCount);
        assertFieldEquals(modParams,vc,ModuleParams.VariationCount);
        mps = assertSubfields(modParams, 3, ModuleParams.ParamSet);

        vps = assertVarParams(mps, vc, 1, 3); //FX in
        v = 0;
        assertModParams(v++,vps,1,1,2);
        assertModParams(v++,vps,1,1,2);
        while (v < vc) {
            assertModParams(v++,vps,0,1,1);
        }

        vps = assertVarParams(mps, vc, 2, 11); //Delay Stereo
        v = 0;
        while (v < vc) {
            assertModParams(v++,vps,64, 64, 64, 64, 0, 0, 0, 127, 64, 1, 0);
        }

        vps = assertVarParams(mps, vc, 3, 3); //2 out
        v = 0;
        while (v < vc) {
            assertModParams(v++,vps,0,1,0);
        }

        bb = section(0x65,buf); //morph parameters
        FieldValues morphParams = MorphParameters.FIELDS.read(bb);

        assertFieldEquals(morphParams,vc,MorphParameters.VariationCount);
        assertFieldEquals(morphParams,8 ,MorphParameters.MorphCount);
        assertFieldEquals(morphParams,0 ,MorphParameters.Reserved);
        List<FieldValues> vms = assertSubfields(morphParams, vc, MorphParameters.VarMorphs);
        for (int i = 0; i < vc; i++) {
            FieldValues vm = vms.get(i);
            assertFieldEquals(vm,i,VarMorph.Variation);
            assertFieldEquals(vm,0,VarMorph.Reserved0);
            assertFieldEquals(vm,0,VarMorph.Reserved1);
            assertFieldEquals(vm,0,VarMorph.Reserved2);
            assertFieldEquals(vm,0,VarMorph.Reserved3);
            int mc = i == 1 ? 1 : 0;
            assertFieldEquals(vm,mc,VarMorph.MorphCount);
            List<FieldValues> vmps = assertSubfields(vm, mc, VarMorph.VarMorphParams);
            if (i == 1) {
                FieldValues vmp = vmps.getFirst();
                assertFieldEquals(vmp,0x01,VarMorphParam.Location);
                assertFieldEquals(vmp,0x02,VarMorphParam.ModuleIndex);
                assertFieldEquals(vmp,0x01,VarMorphParam.ParamIndex );
                assertFieldEquals(vmp,0x01,VarMorphParam.Morph      );
                assertFieldEquals(vmp,0x7f,VarMorphParam.Range      );
            }

        }

        bb = section(0x62,buf); //knob assignments
        FieldValues knobs = KnobAssignments.FIELDS.read(bb);
        int kc = assertFieldEquals(knobs,0x78,KnobAssignments.KnobCount);
        List<FieldValues> kas = assertSubfields(knobs, kc, KnobAssignments.Knobs);
        for (int i = 0; i < kc; i++) {
            FieldValues ka = kas.get(i);
            int a = i == 0 ? 1 : 0;
            assertFieldEquals(ka,a,KnobAssignment.Assigned);
            List<FieldValues> kps = assertSubfields(ka, a, KnobAssignment.Params);
            if (i == 0) {
                FieldValues kp = kps.getFirst();
                assertFieldEquals(kp,1,KnobParams.Location);
                assertFieldEquals(kp,1,KnobParams.Index);
                assertFieldEquals(kp,0,KnobParams.IsLed);
                assertFieldEquals(kp,0,KnobParams.Param);
                //yikes, reads slot (2) if "performance" !!!
            }
        }

        bb = section(0x60,buf); //Control Assignments
        FieldValues cass = ControlAssignments.FIELDS.read(bb);
        System.out.println(cass);
        assertFieldEquals(cass,0x02,ControlAssignments.NumControls);
        List<FieldValues> cas = assertSubfields(cass, 2, ControlAssignments.Assignments);
        FieldValues ca = cas.removeFirst();
        assertFieldEquals(ca,0x07,ControlAssignment.MidiCC);
        assertFieldEquals(ca,0x02,ControlAssignment.Location);
        assertFieldEquals(ca,0x02,ControlAssignment.Index);
        assertFieldEquals(ca,0x00,ControlAssignment.Param);
        ca = cas.removeFirst();
        assertFieldEquals(ca,0x11,ControlAssignment.MidiCC);
        assertFieldEquals(ca,0x02,ControlAssignment.Location);
        assertFieldEquals(ca,0x07,ControlAssignment.Index);
        assertFieldEquals(ca,0x00,ControlAssignment.Param);

    }


}
