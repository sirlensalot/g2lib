package g2lib.usb;

import g2lib.Util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class UsbReadThread implements Runnable {

    private final Usb usb;
    private final Logger log = Util.getLogger(UsbReadThread.class);
    public final Thread thread;

    public UsbReadThread(Usb usb) {
        this.usb = usb;
        thread = new Thread(this);
    }

    public final AtomicBoolean go = new AtomicBoolean(true);
    public final AtomicInteger recd = new AtomicInteger(0);
    public final LinkedBlockingQueue<UsbMessage> q = new LinkedBlockingQueue<>();

    public void start() { thread.start(); }

    @Override
    public void run() {
        log.info("Go");
        while (go.get()) {
            UsbMessage r = usb.readInterrupt(500);
            if (!r.success()) {
                continue;
            }
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

    public interface MsgP extends Predicate<UsbMessage> {

    }

    public UsbMessage expect(String msg, MsgP filter) throws InterruptedException {
        UsbMessage m = q.take();
        if (filter.test(m)) {
            log.fine("expect: received " + msg + ": " + m.dump());
            return m;
        } else {
            log.warning("expect: " + msg + ": did not receive: " + m.dump());
            return null;
        }
    }


}
