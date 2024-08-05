package g2lib;

import g2lib.protocol.FieldEnum;
import g2lib.protocol.FieldValue;
import g2lib.protocol.FieldValues;
import g2lib.protocol.SubfieldsValue;
import org.junit.jupiter.api.Test;

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

    public static void assertFieldEquals(FieldValues values, String expected, FieldEnum field) {
        String actual = assertString(values, field);
        assertEquals(expected,actual,field.toString());
    }


    private static int assertValue(FieldValues values, FieldEnum field) {
        Optional<Integer> i = field.intValue(values);
        assertTrue(i.isPresent(),"value found: " + field);
        return i.get();
    }

    private static String assertString(FieldValues values, FieldEnum field) {
        Optional<String> s = field.stringValue(values);
        assertTrue(s.isPresent(),"value found: " + field);
        return s.get();
    }

    public static List<FieldValues> assertSubfields(FieldValues fv, int size, FieldEnum field) {
        Optional<List<FieldValues>> o = field.subfieldsValue(fv);
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
        String message = "mod params var " + variation;
        List<Integer> el = Arrays.asList(expecteds);
        List<FieldValues> ps = assertSubfields(vp, el.size(), VarParams.Params);
        List<Integer> al = ps.stream().map(fv -> assertValue(fv, Data7.Datum)).toList();
        assertEquals(el,al, message);
    }

    private void sectionHeader(FieldValues vs, PatchParams f, int section, int entries) {
        assertTrue(f.subfieldsValue(vs).isPresent(),"header present: " + f);
        FieldValues h = f.subfieldsValue(vs).get().getFirst();
        assertFieldEquals(h,section,SectionHeader.Section);
        assertFieldEquals(h,entries,SectionHeader.Entries);
    }



    private void testMorphLabels(ByteBuffer buf) {
        BitBuffer bb;
        bb = section(0x5b, buf); //Labels
        assertEquals(0x02,bb.get(2),"Location"); // settings/morph labels
        FieldValues mls = MorphLabels.FIELDS.read(bb);
        assertFieldEquals(mls,0x01,MorphLabels.LabelCount);
        assertFieldEquals(mls,0x01,MorphLabels.Entry);
        assertFieldEquals(mls,80,MorphLabels.Length);
        List<FieldValues> ls = assertSubfields(mls, 8, MorphLabels.Labels);
        String[] labels = {"Wheel","Vel","Keyb1","Aft.Tch","Sust.Pd","Ctrl.Pd","P.Stick", "G.Wh 2"};
        for (int i = 0; i < 8; i++) {
            FieldValues l = ls.removeFirst();
            assertFieldEquals(l,1,MorphLabel.Index);
            assertFieldEquals(l,8,MorphLabel.Length);
            assertFieldEquals(l,8+i,MorphLabel.Entry);
            assertFieldEquals(l,labels[i],MorphLabel.Label);
        }

        testEndPadding(bb,6);
    }

    private void testModuleNames(ByteBuffer buf) {
        BitBuffer bb;
        bb = section(0x5a, buf); //Module Names
        assertEquals(0x01,bb.get(2),"Location");

        FieldValues mns = ModuleNames.FIELDS.read(bb);
        assertFieldEquals(mns,0x00,ModuleNames.Reserved);
        assertFieldEquals(mns,0x04,ModuleNames.NameCount);
        List<FieldValues> ns = assertSubfields(mns, 4, ModuleNames.Names);

        FieldValues n = ns.removeFirst();
        assertFieldEquals(n,0x01,ModuleName.ModuleIndex);
        assertFieldEquals(n,"FltClassic1",ModuleName.Name);

        n = ns.removeFirst();
        assertFieldEquals(n,0x02,ModuleName.ModuleIndex);
        assertFieldEquals(n,"OscC1",ModuleName.Name);

        n = ns.removeFirst();
        assertFieldEquals(n,0x03,ModuleName.ModuleIndex);
        assertFieldEquals(n,"ModADSR1",ModuleName.Name);

        n = ns.removeFirst();
        assertFieldEquals(n,0x04,ModuleName.ModuleIndex);
        assertFieldEquals(n,"2-Out1",ModuleName.Name);

        testEndPadding(bb,8);


        bb = section(0x5a, buf); //Module Names
        assertEquals(0x00,bb.get(2),"Location");
        mns = ModuleNames.FIELDS.read(bb);
        assertFieldEquals(mns,0x00,ModuleNames.Reserved);
        assertFieldEquals(mns,0x03,ModuleNames.NameCount);
        ns = assertSubfields(mns, 3, ModuleNames.Names);

        n = ns.removeFirst();
        assertFieldEquals(n,0x01,ModuleName.ModuleIndex);
        assertFieldEquals(n,"Fx-In1",ModuleName.Name);

        n = ns.removeFirst();
        assertFieldEquals(n,0x02,ModuleName.ModuleIndex);
        assertFieldEquals(n,"Mix2-1A1",ModuleName.Name);

        n = ns.removeFirst();
        assertFieldEquals(n,0x03,ModuleName.ModuleIndex);
        assertFieldEquals(n,"2-Out1",ModuleName.Name);

        testEndPadding(bb,8);
    }

    private void testKnobAssignments(ByteBuffer buf) {
        BitBuffer bb;
        bb = section(0x62, buf); //knob assignments
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

        testEndPadding(bb,3);
    }

    private void testControlAssignments(ByteBuffer buf) {
        BitBuffer bb;
        bb = section(0x60, buf); //Control Assignments
        FieldValues cass = ControlAssignments.FIELDS.read(bb);
        //System.out.println(cass);
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


        testEndPadding(bb,1);
    }

    private void testMorphParams(ByteBuffer buf, int vc) {
        BitBuffer bb;
        bb = section(0x65, buf); //morph parameters
        FieldValues morphParams = MorphParameters.FIELDS.read(bb);

        assertFieldEquals(morphParams, vc,MorphParameters.VariationCount);
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
        testEndPadding(bb,3);
    }

    private void testModParams0(ByteBuffer buf, int vc, int remaining) {


        BitBuffer bb = section(0x4d, buf); //param list
        assertEquals(0,bb.get(2),"location"); //loc0 parameters

        FieldValues modParams = ModuleParams.FIELDS.read(bb);

        assertFieldEquals(modParams,3,ModuleParams.SetCount);
        assertFieldEquals(modParams, vc,ModuleParams.VariationCount);
        List<FieldValues> mps = assertSubfields(modParams, 3, ModuleParams.ParamSet);

        List<FieldValues> vps = assertVarParams(mps, vc, 1, 3); //FX in
        int v = 0;
        assertModParams(v++,vps,1,1,2);
        assertModParams(v++,vps,1,1,2);
        while (v < vc) {
            assertModParams(v++,vps,0,1,1);
        }

        //dumpFieldValues(modParams);
        vps = assertVarParams(mps, vc, 2, 5); //Mixer 2-1A
        v = 0;
        while (v < vc) {
            assertModParams(v++,vps,100,1,100,v==2?0:1,0);
        }

        vps = assertVarParams(mps, vc, 3, 3); //2 out
        v = 0;
        while (v < vc) {
            assertModParams(v++,vps,0,1,0);
        }

        testEndPadding(bb, remaining);
    }

    private void testModParams1(ByteBuffer buf, int vc, int remaining) {
        BitBuffer bb;
        bb = section(0x4d, buf); //param list
        assertEquals(1,bb.get(2),"location"); //loc1 parameters

        FieldValues modParams = ModuleParams.FIELDS.read(bb);

        assertFieldEquals(modParams,0x04,ModuleParams.SetCount);
        assertFieldEquals(modParams, vc,ModuleParams.VariationCount);
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

        testEndPadding(bb,remaining);

    }

    @SuppressWarnings("unused")
    private static void dumpFieldValues(FieldValues fv) {
        dumpFieldValues(fv,0);
    }
    private static void dumpFieldValues(FieldValues fv,int indent) {
        Runnable ind = () -> {
            for (int i = 0; i < indent; i++) {
                System.out.print("  ");
            }
        };
        ind.run();
        for (int i = 0; i < fv.values.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            FieldValue v = fv.values.get(i);
            if (v instanceof SubfieldsValue) {
                System.out.println(v.field().name() + ": ");
                for (FieldValues sfv : ((SubfieldsValue) v).value()) {
                    dumpFieldValues(sfv, indent + 1);
                }
                System.out.print("  ");
            } else {
                System.out.print(v);
            }
        }
        System.out.println();
    }

    private int testPatchSettings(ByteBuffer buf, int vce, int remaining) {
        BitBuffer bb;
        bb = section(0x4d, buf); //param list
        assertEquals(2,bb.get(2),"location"); //patch parameters

        FieldValues patchSettings = PatchParams.FIELDS.read(bb);
        int vc = assertFieldEquals(patchSettings,vce, PatchParams.VariationCount);
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
                if (i == 1 && j == 2) {
                    assertFieldEquals(mdials.get(j),25, Data7.Datum);
                } else {
                    assertFieldEquals(mdials.get(j),0x00, Data7.Datum);
                }
                assertFieldEquals(mmodes.get(j),0x01, Data7.Datum);
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

        testEndPadding(bb,remaining);

        return vc;
    }

    private void testCableLists(ByteBuffer buf,int... indexes) {
        BitBuffer bb;
        bb = section(0x52, buf);
        assertEquals(0xf,bb.limit(), "cable list1 len");
        assertEquals(1,bb.get(2),"location");

        FieldValues cl = CableList.FIELDS.read(bb);
        //dumpFieldValues(cl);
        assertFieldEquals(cl,0,CableList.Reserved);
        assertFieldEquals(cl,3,CableList.CableCount);
        List<FieldValues> cs = assertSubfields(cl, 3, CableList.Cables);


        FieldValues cable = cs.get(indexes[0]);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x03, Cable.ModuleFrom);
        assertFieldEquals(cable,0x01, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x04, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        cable = cs.get(indexes[1]);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x01, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x03, Cable.ModuleTo);
        assertFieldEquals(cable,0x05, Cable.ConnectorTo);

        cable = cs.get(indexes[2]);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x02, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x01, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        assertEquals(0,bb.getBitsRemaining());


        bb = section(0x52, buf);
        assertEquals(0xb,bb.limit(), "cable list0 len");
        assertEquals(0,bb.get(2),"location");
        cl = CableList.FIELDS.read(bb);
        //dumpFieldValues(cl);
        assertFieldEquals(cl,0,CableList.Reserved);
        assertFieldEquals(cl,2,CableList.CableCount);
        cs = assertSubfields(cl, 2, CableList.Cables);

        cable = cs.get(indexes[3]);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x02, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x03, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        cable = cs.get(indexes[4]);
        assertFieldEquals(cable,0x00, Cable.Color);
        assertFieldEquals(cable,0x01, Cable.ModuleFrom);
        assertFieldEquals(cable,0x00, Cable.ConnectorFrom);
        assertFieldEquals(cable,0x01, Cable.LinkType);
        assertFieldEquals(cable,0x02, Cable.ModuleTo);
        assertFieldEquals(cable,0x00, Cable.ConnectorTo);

        assertEquals(0,bb.getBitsRemaining());


    }

    private void testModules(ByteBuffer buf, int... indexes) {
        BitBuffer bb;
        bb = section(0x4a, buf);
        assertEquals(27,bb.limit(), "module list len");
        FieldValues modl = ModuleList.FIELDS.read(bb);
        assertFieldEquals(modl,1,ModuleList.Location);
        List<FieldValues> mods = assertSubfields(modl, 4, ModuleList.Modules);

        FieldValues module;
        List<FieldValues> modes;

        //Util.dumpBuffer(b2);
        module = mods.removeFirst();
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

        module = mods.removeFirst();
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

        module = mods.removeFirst();
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


        module = mods.removeFirst();
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

        assertEquals(0,bb.getBitsRemaining());

        bb = section(0x4a, buf);
        assertEquals(20,bb.limit(), "module list0 len");
        modl = ModuleList.FIELDS.read(bb);
        assertFieldEquals(modl,0,ModuleList.Location);
        mods = assertSubfields(modl, 3, ModuleList.Modules);


        module = mods.get(indexes[0]);
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


        module = mods.get(indexes[1]);
        //dumpFieldValues(module);
        assertFieldEquals(module,0xc2, Module_.Id);//Mixer 2-1A
        assertFieldEquals(module,0x02, Module_.Index);
        assertFieldEquals(module,0x01, Module_.Horiz);
        assertFieldEquals(module,0x04, Module_.Vert);
        assertFieldEquals(module,0x00, Module_.Color);
        assertFieldEquals(module,0x01, Module_.Uprate);
        assertFieldEquals(module,0x00, Module_.Leds);
        assertFieldEquals(module,0x00, Module_.Reserved);
        assertFieldEquals(module,0x00, Module_.ModeCount);
        assertSubfields(module, 0, Module_.Modes);


        module = mods.get(indexes[2]);
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
        assertEquals(0,bb.getBitsRemaining());
    }


    private void testEndPadding(BitBuffer bb, int remaining) {
        assertEquals(remaining,bb.getBitsRemaining(),"remaining");
        if (remaining > 0) {
            assertEquals(0, bb.get(remaining), "end padding");
            assertEquals(0, bb.getBitsRemaining(), "no bits remaining");
        }
    }



    @Test
    void patchDesc0() throws Exception {

        ByteBuffer buf = Util.readFile("data/patchdesc0.msg");

        assertEquals(0x01,buf.get()); // cmd
        assertEquals(0x08,buf.get()); // slot 0
        assertEquals(0x00,buf.get()); // patch version
        BitBuffer bb;

        bb = section(0x21,buf);
        PatchDescription.FIELDS.read(bb);


        assertEquals(0x2d,buf.get(),"USB extra 1");
        assertEquals(0x00,buf.get(), "USB extra 2");

        bb = section(0x4a,buf);
        FieldValues modl = ModuleList.FIELDS.read(bb);
        assertFieldEquals(modl,1,ModuleList.Location);

        bb = section(0x4a,buf);
        modl = ModuleList.FIELDS.read(bb);
        assertFieldEquals(modl,0,ModuleList.Location);

        //52 should be next, CABLE_LIST
        bb = section(0x52,buf);
        assertEquals(1,bb.get(2),"location");
        CableList.FIELDS.read(bb);

        bb = section(0x52,buf);
        assertEquals(0,bb.get(2),"location");
        CableList.FIELDS.read(bb);

        bb = section(0x4d,buf); //param list
        assertEquals(2,bb.get(2),"location"); //patch parameters
        PatchParams.FIELDS.read(bb);

        bb = section(0x4d,buf); //param list
        assertEquals(1,bb.get(2),"location"); //loc1 parameters
        ModuleParams.FIELDS.read(bb);

        bb = section(0x4d,buf); //param list
        assertEquals(0,bb.get(2),"location"); //loc0 parameters
        ModuleParams.FIELDS.read(bb);

        bb = section(0x65,buf); //morph parameters
        MorphParameters.FIELDS.read(bb);

        bb = section(0x62,buf); //knob assignments
        KnobAssignments.FIELDS.read(bb);

        bb = section(0x60,buf); //Control Assignments
        ControlAssignments.FIELDS.read(bb);

        bb = section(0x5a,buf); //Module Names
        assertEquals(0x01,bb.get(2),"Location");
        ModuleNames.FIELDS.read(bb);

        bb = section(0x5a,buf); //Module Names
        assertEquals(0x00,bb.get(2),"Location");
        ModuleNames.FIELDS.read(bb);

        bb = section(0x5b,buf); //Labels
        assertEquals(0x02,bb.get(2),"Location"); // settings/morph labels
        MorphLabels.FIELDS.read(bb);

        bb = section(0x5b,buf); //Labels
        assertEquals(0x01,bb.get(2),"Location"); // module labels
        assertEquals(0x00,bb.get(2),"NumModules");

        bb = section(0x5b,buf); //Labels
        assertEquals(0x00,bb.get(2),"Location"); // module labels
        assertEquals(0x00,bb.get(2),"NumModules");

        assertEquals(0x17da,Util.getShort(buf),"CRC");
        assertFalse(buf.hasRemaining(),"Buf done");

    }


    @Test
    void patchDesc() throws Exception {

        ByteBuffer buf = Util.readFile("data/patchdesc1.msg");

        assertEquals(0x01,buf.get()); // cmd
        assertEquals(0x09,buf.get()); // slot 1
        assertEquals(0x00,buf.get()); // patch version
        BitBuffer bb;
        bb = section(0x21,buf);

        FieldValues pd = PatchDescription.FIELDS.values(
                PatchDescription.Reserved.value(Data8.asSubfield(1, 0xfc, 0, 0, 1, 0, 0)),
                PatchDescription.Reserved2.value(0x04),
                PatchDescription.Voices.value(0x05),
                PatchDescription.Height.value(374),
                PatchDescription.Unk2.value(0x01),
                PatchDescription.Red.value(0x01),
                PatchDescription.Blue.value(0x01),
                PatchDescription.Yellow.value(0x01),
                PatchDescription.Orange.value(0x01),
                PatchDescription.Green.value(0x01),
                PatchDescription.Purple.value(0x01),
                PatchDescription.White.value(0x01),
                PatchDescription.MonoPoly.value(0x00),
                PatchDescription.Variation.value(0x01),
                PatchDescription.Category.value(0x00)
        );
        assertEquals(pd,PatchDescription.FIELDS.read(bb),"PatchDescription");
        testEndPadding(bb,12);

        assertEquals(0x2d,buf.get(),"USB extra 1");
        assertEquals(0x00,buf.get(), "USB extra 2");

        testModules(buf,0,1,2);

        testCableLists(buf,0,1,2,0,1);

        int vc = testPatchSettings(buf,10,3);

        testModParams1(buf, vc, 0);
        testModParams0(buf, vc, 7);

        testMorphParams(buf, vc);

        testKnobAssignments(buf);

        testControlAssignments(buf);

        testModuleNames(buf);

        testMorphLabels(buf);



        testModuleLabels(buf);


        assertEquals(0xed77,Util.getShort(buf),"CRC");
        assertFalse(buf.hasRemaining(),"Buf done");

    }

    private void testModuleLabels(ByteBuffer buf) {
        BitBuffer bb = section(0x5b,buf); //Labels
        assertEquals(0x01,bb.get(2),"Location");
        FieldValues mlss = ModuleLabels.FIELDS.read(bb);
        assertFieldEquals(mlss,0x00,ModuleLabels.ModuleCount);
        testEndPadding(bb,6);

        bb = section(0x5b, buf); //Labels
        assertEquals(0x00,bb.get(2),"Location");
        mlss = ModuleLabels.FIELDS.read(bb);
        assertFieldEquals(mlss,0x01,ModuleLabels.ModuleCount);
        FieldValues mls = assertSubfields(mlss,1,ModuleLabels.ModLabels).removeFirst();
        assertFieldEquals(mls,0x02,ModuleLabel.ModuleIndex);
        assertFieldEquals(mls,0x14,ModuleLabel.ModLabelLen);
        List<FieldValues> ls = assertSubfields(mls, 2, ModuleLabel.Labels);

        FieldValues l = ls.removeFirst();
        assertFieldEquals(l,0x01,ParamLabel.IsString);
        assertFieldEquals(l,0x08,ParamLabel.ParamLen);
        assertFieldEquals(l,0x01,ParamLabel.ParamIndex);
        assertFieldEquals(l,"Ch 1",ParamLabel.Label);

        l = ls.removeFirst();
        assertFieldEquals(l,0x01,ParamLabel.IsString);
        assertFieldEquals(l,0x08,ParamLabel.ParamLen);
        assertFieldEquals(l,0x03,ParamLabel.ParamIndex);
        assertFieldEquals(l,"Ch Two",ParamLabel.Label);

        testEndPadding(bb,6);
    }


    private static ByteBuffer patchHeader() {
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
        return header;
    }


    @Test
    public void readPatch() throws Exception {
        ByteBuffer buf = Util.readFile("data/simplesynth001-20240802.pch2");
        ByteBuffer header = patchHeader();
        while (header.hasRemaining()) {
            assertEquals(header.get(),buf.get(),"header check");
        }

        // crc starts here
        ByteBuffer slice = buf.slice();
        int crc = CRC16.crc16(slice,0,slice.limit()-2);
        assertEquals(0x17,buf.get(),"Unknown");
        assertEquals(0x00,buf.get(),"PatchVersion");

        BitBuffer bb = section(0x21, buf);
        FieldValues pd = PatchDescription.FIELDS.values(
                PatchDescription.Reserved.value(Data8.asSubfield(0, 0, 0, 0, 0, 0, 0)), //!USB
                PatchDescription.Reserved2.value(0x00), //!USB
                PatchDescription.Voices.value(0x05),
                PatchDescription.Height.value(374),
                PatchDescription.Unk2.value(0x01),
                PatchDescription.Red.value(0x01),
                PatchDescription.Blue.value(0x01),
                PatchDescription.Yellow.value(0x01),
                PatchDescription.Orange.value(0x01),
                PatchDescription.Green.value(0x01),
                PatchDescription.Purple.value(0x01),
                PatchDescription.White.value(0x01),
                PatchDescription.MonoPoly.value(0x00),
                PatchDescription.Variation.value(0x01),
                PatchDescription.Category.value(0x00)
        );
        FieldValues pd_ = PatchDescription.FIELDS.read(bb);
        assertEquals(pd,pd_,"PatchDescription");
        testEndPadding(bb,12);

        testModules(buf,0,2,1);

        bb = section(0x69,buf); //CurrentNote
        assertEquals(0x13,bb.limit(),"CurrentNote length");
        Util.dumpBuffer(bb.toBuffer());
        /*
        80 00 01 60 00 01 00 00 08 00 00 40 00 02 00 00   . . . ` . . . . . . . @ . . . .
        10 00 00
        */
        FieldValues cns = CurrentNote.FIELDS.read(bb);

        assertFieldEquals(cns,0x40,CurrentNote.Note);
        assertFieldEquals(cns,0x00,CurrentNote.Attack);
        assertFieldEquals(cns,0x00,CurrentNote.Release);
        assertFieldEquals(cns,0x05,CurrentNote.NoteCount); //note that this stores actual count - 1
        List<FieldValues> ns = assertSubfields(cns, 6, CurrentNote.Notes);
        for (int i = 0; i < 6; i++) {
            FieldValues n = ns.removeFirst();
            assertFieldEquals(n,0x40,NoteData.Note);
            assertFieldEquals(n,0x00,NoteData.Attack);
            assertFieldEquals(n,0x00,NoteData.Release);
        }
        testEndPadding(bb,0);

        testCableLists(buf,2,1,0,1,0); //LOL reversed in patch!!!

        int vc = testPatchSettings(buf,9,4);

        testModParams1(buf,vc, 5);

        testModParams0(buf,vc, 4);

        testMorphParams(buf,vc);

        testKnobAssignments(buf);

        testControlAssignments(buf);

        testMorphLabels(buf);



        testModuleLabels(buf);

        testModuleNames(buf);

        bb = section(0x6f,buf);
        assertEquals(0x11,bb.limit(),"TextPad"); //empty
        Util.dumpBuffer(bb.toBuffer());
        /*
        57 72 69 74 69 6e 67 20 6e 6f 74 65 73 20 2e 2e   W r i t i n g . n o t e s . . .
        2e                                                .
        */
        for (char c : "Writing notes ...".toCharArray()) {
            assertEquals(c,bb.get());
        }
        testEndPadding(bb,0);


        int fcrc = Util.getShort(buf);
        assertEquals(crc,fcrc,"CRC");
        assertFalse(buf.hasRemaining(),"Buf done");


    }



}
