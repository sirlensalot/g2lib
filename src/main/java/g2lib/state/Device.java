package g2lib.state;

import g2lib.Util;
import g2lib.usb.Usb;
import g2lib.usb.UsbMessage;
import g2lib.usb.UsbReadThread;

import java.util.logging.Logger;

public class Device {

    private final Logger log = Util.getLogger(Device.class);

    private final Usb usb;
    private final UsbReadThread readThread;

    private Performance perf;

    public Device(Usb usb) {
        this.usb = usb;
        readThread = new UsbReadThread(usb);
    }

    public void initialize() throws Exception {
        readThread.thread.start();

        // init message
        usb.sendBulk("Init", Util.asBytes(0x80)); // CMD_INIT
        readThread.expect("Init response", msg -> msg.head(0x80));


        // perf version
        usb.sendSystemCmd("perf version"
                ,0x35 // Q_VERSION_CNT
                ,0x04 // perf version??
        );
        UsbMessage perfInitMsg = readThread.expect("perf version",
                msg -> msg.head(0x82, 0x01, 0x0c, 0x40, 0x36, 0x04) && msg.size() > 6);
        perf = new Performance(perfInitMsg.buffer().get(6));

        usb.sendSystemCmd("Stop Comm"
                ,0x7d // S_START_STOP_COM
                ,0x01 // stop
        );
        readThread.expect("Stop Comm",m -> m.head(0x62,0x01));

        //synth settings
        usb.sendSystemCmd("Synth settings"
                ,0x02 // Q_SYNTH_SETTINGS
        );
        //extended: 01 0c 00 03 -- synth settings [03]
        UsbMessage synthSettingMsg = readThread.expect("Synth settings", m -> m.head(0x01, 0x0c, 0x00, 0x03));


    }
}
