package g2lib;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class Main {


    private static final Logger log = Util.getLogger(Main.class);

    public static class UsbReadThread implements Runnable {

        private final Usb usb;
        private final Logger log = Util.getLogger(UsbReadThread.class);
        public final Thread thread;
        public UsbReadThread(Usb usb) {
            this.usb = usb;
            thread = new Thread(this);
        }

        public final AtomicBoolean go = new AtomicBoolean(true);
        public final AtomicInteger recd = new AtomicInteger(0);
        public final LinkedBlockingQueue<Usb.UsbMessage> q = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            log.info("Go");
            while (go.get()) {
                Usb.UsbMessage r = usb.readInterrupt(500);
                if (!r.success()) { continue; }
                recd.incrementAndGet();
                if (r.extended()) {
                    r = usb.readBulkRetries(r.size(), 5);
                    if (r.success()) {
                        try {
                            q.put(r);
                        } catch (Exception e) {
                            log.severe("extended put failed" + e);
                        }
                    }
                } else {
                    try {
                        q.put(r);
                    } catch (Exception e) {
                        log.severe("embedded put failed" + e);
                    }
                }
            }
            log.info("Done");

        }
    }

    public static void main(String[] args) throws Exception {

        Usb usb;
        int retry = 0;
        while (true) {
            try {
                usb = Usb.initialize();
                break;
            } catch (Exception e) {
                if (retry++ < 10) {
                    log.info("Failed to acquire USB device, retrying...");
                    Thread.sleep(2000);
                } else {
                    throw e;
                }
            }
        }

        UsbReadThread readThread = new UsbReadThread(usb);
        readThread.thread.start();


        // init message
        usb.sendBulk("Init", Util.asBytes(0x80)); // CMD_INIT
        //extended: 80 0a 03 00 -- 80/hello machine

        // patch version (?)
        usb.sendSystemCmd("Patch version"
                ,0x35 // Q_VERSION_CNT
                ,0x04 // perf version??
            );
        //embedded: 82 01 0c 40 36 04 00 , "perf version" [04] 00

        //stop comm 0x01
        usb.sendSystemCmd("Stop Comm"
                ,0x7d // S_START_STOP_COM
                ,0x01 // stop
                );
        //embedded: 62 01 0c 00 7f -- 62 01 (stop message/OK)

        //synth settings
        usb.sendSystemCmd("Synth settings"
                ,0x02 // Q_SYNTH_SETTINGS
        );
        //extended: 01 0c 00 03 -- synth settings [03]

        //unknown 1
        usb.sendSystemCmd("unknown 1"
                ,0x81 // M_UNKNOWN_1
        );
        //extended: 01 0c 00 80 -- 80/"unknown 1" (slot hello?)

        usb.sendSystemCmd("perf settings"
                ,0x10 // Q_PERF_SETTINGS
        );
        //extended: 01 0c 00 29 -- perf settings [29 "perf name"]
        //  then chunks in TG2FilePerformance.Read

        usb.sendSystemCmd("unknown 2"
                ,0x59 // M_UNKNOWN_2
        );
        //embedded: 72 01 0c 00 1e -- "unknown 2" [1e]

        usb.sendSystemCmd("slot 1 version"
                ,0x35 // Q_VERSION_CNT
                ,1 // slot index
        );
        //embedded: 82 01 0c 40 36 01 -- slot version

        usb.sendSlotCmd(1,0,"slot 1 patch",
                0x3c // Q_PATCH
        );
        //extended: 01 09 00 21 -- patch description, slot 1

        usb.sendSlotCmd(1,0,"slot 1 name",
                0x28 // Q_PATCH_NAME
        );
        //extended: 01 09 00 27 -- patch name, slot 1

        usb.sendSlotCmd(1,0,"slot 1 note",
                0x68 // Q_CURRENT_NOTE
        );
        //extended: 01 09 00 69 -- cable list, slot 1

        usb.sendSlotCmd(1,0,"slot 1 text",
                0x6e //Q_PATCH_TEXT
        );
        //extended: 01 09 00 6f -- textpad, slot 1

        int i = 0;
        while (readThread.recd.get() < 12) {
            Thread.sleep(250);
            if (i++ > 20) { break; }
        }

        System.out.println("Received: " + readThread.recd.get());
        System.out.println("Message count: " + readThread.q.size());
        i = 0;
        while (!readThread.q.isEmpty()) {
            Usb.UsbMessage b = readThread.q.poll();
            log.info(String.format("========= MESSAGE: size=%x extended=%s ============ %s",
                    b.size(),b.extended(),Util.dumpBufferString(b.buffer())));
            String fn = String.format("msg%02d_%04x.msg",i,b.crc());
            Util.writeBuffer(b.buffer(),fn);
            i++;
        }
        readThread.go.set(false);
        System.out.println("joining");
        readThread.thread.join();

        usb.shutdown();

        System.out.println("Exit");
    }


}