package g2lib;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class Main {


    private static void writePDesc(ByteBuffer data) {
        try (FileOutputStream fos = new FileOutputStream("data/patchdesc1.msg")) {
            data.rewind();
            byte[] bs = new byte[data.limit()];
            data.get(bs);
            fos.write(bs);
            fos.flush();
        } catch (Exception e) {
            throw new RuntimeException("error writing patch desc", e);
        }
    }

    private static final Logger log = Util.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        final Usb usb = Usb.initialize();

        final AtomicBoolean go = new AtomicBoolean(true);
        final AtomicInteger recd = new AtomicInteger(0);
        Thread readThread = new Thread(new Runnable() {

            @Override
            public void run() {
                log.info("Go");
                while (go.get()) {
                    Usb.ReadInterruptResult r = usb.readInterrupt(500);
                    if (!r.success()) { continue; }
                    recd.incrementAndGet();
                    if (!r.extended()) { continue; }
                    usb.readBulkRetries(r.size(),5);
                }
                log.info("Done");

            }
        });
        readThread.start();


        // init message
        usb.sendBulk("Init", Util.asBytes(0x80)); // CMD_INIT
        //usb.readExtended();  // cmd == 80 is success

        // patch version (?)
        usb.sendCmdRequest("Patch version"
                ,0x35 // Q_VERSION_CNT
                ,0x04 // perf version??
            );
        //usb.readInterrupt(2000); // 82 01 0c 40 36 04 00 , "perf version" [04] 00

        //stop comm 0x01
        usb.sendCmdRequest("Stop Comm"
                ,0x7d // S_START_STOP_COM
                ,0x01 // stop
                );
        //usb.readInterrupt(2000); //0c 00 7f: OK  x

        //synth settings
        usb.sendCmdRequest("Synth settings"
                ,0x02 // Q_SYNTH_SETTINGS
        );
        //usb.readExtended(); // 01 0c 00 03 -> 03 is S_SYNTH_SETTINGS  -- 1

        //unknown 1
        usb.sendCmdRequest("unknown 1"
                ,0x81 // M_UNKNOWN_1
        );
        //usb.readExtended(); // 01 0c 00 80 -> unknown 1

        usb.sendCmdRequest("perf settings"
                ,0x10 // Q_PERF_SETTINGS
        );
        //usb.readExtended(); // 01 0c 00 29 , 29 => C_PERF_NAME, then chunks in TG2FilePerformance.Read

        usb.sendCmdRequest("unknown 2"
            ,0x59 // M_UNKNOWN_2
        );
        //usb.readInterrupt(2000); // 1e embedded message (unknown 2)

        usb.sendCmdRequest("slot 1 version"
            ,0x35 // Q_VERSION_CNT
                ,1 // slot index
        );
        //usb.readInterrupt(2000); // 82 01 0c 40 36 00 00, slot 0 version -> 00
        // 00 08 01 29 00 3c 99 3c slot 1 version

        usb.sendBulk("slot 1 patch", Util.asBytes(
                0x01
                ,0x20 + 0x08 + 1 // CMD_REQ + CMD_SLOT + slot index
                ,0 // patch version, 0 from above
                ,0x3c // Q_PATCH
                ));


        usb.sendBulk("slot 1 name",Util.asBytes(
                0x01
                ,0x20 + 0x08 + 1 // CMD_REQ + CMD_SLOT + slot index
                ,0 // patch version, 0 from above
                ,0x28 // Q_PATCH_NAME
        ));

        usb.sendBulk("slot 1 curnote",Util.asBytes(
                0x01
                ,0x20 + 0x08 + 1 // CMD_REQ + CMD_SLOT + slot index
                ,0 // patch version, 0 from above
                ,0x68 // Q_CURRENT_NOTE
        ));



        usb.sendBulk("slot 1 text",Util.asBytes(
                0x01
                ,0x20 + 0x08 + 1 // CMD_REQ + CMD_SLOT + slot index
                ,0 // patch version, 0 from above
                ,0x6e //Q_PATCH_TEXT
        ));

        int i = 0;
        while (recd.get() < 12) {
            Thread.sleep(250);
            if (i++ > 20) { break; }
        }

        System.out.println("Received: " + recd.get());
        go.set(false);
        System.out.println("joining");
        readThread.join();

        usb.shutdown();

        System.out.println("Exit");
    }


}