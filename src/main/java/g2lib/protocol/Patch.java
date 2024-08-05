package g2lib.protocol;

import g2lib.BitBuffer;
import g2lib.Protocol;
import g2lib.Util;

import java.util.List;

import static g2lib.Protocol.*;

public class Patch {

    public static record Section<T extends FieldEnum>(FieldValues values,int type,Integer location) {

    }
    public static <T extends FieldEnum> Section<T> prototype(int type) {
        return new Section<>(null,type,null);
    }
    public static <T extends FieldEnum> Section<T> prototype(int type,Integer location) {
        return new Section<>(null,type,location);
    }


    Section<PatchDescription> patchDescription =     prototype(0x21  );
    Section<ModuleList> moduleList1 =                prototype(0x4a,1);
    Section<ModuleList> moduleList0 =                prototype(0x4a,0);
    Section<CurrentNote> currentNote =               prototype(0x69  );
    Section<CableList> cableList1 =                  prototype(0x52,1);
    Section<CableList> cableList0 =                  prototype(0x52,0);
    Section<PatchParams> patchParams =               prototype(0x4d,2);
    Section<ModuleParams> moduleParams1 =            prototype(0x4d,1);
    Section<ModuleParams> moduleParams0 =            prototype(0x4d,0);
    Section<MorphParameters> morphParameters =       prototype(0x65  );
    Section<KnobAssignments> knobAssignments =       prototype(0x62  );
    Section<ControlAssignments> controlAssignments = prototype(0x60  );
    Section<MorphLabels> morphLabels =               prototype(0x5b,2);
    Section<ModuleLabels> moduleLabels1 =            prototype(0x5b,1);
    Section<ModuleLabels> moduleLabels0 =            prototype(0x5b,0);
    Section<ModuleNames> moduleNames1 =              prototype(0x5a,1);
    Section<ModuleNames> moduleNames0 =              prototype(0x5a,0);

    String text;
    String name;


}
