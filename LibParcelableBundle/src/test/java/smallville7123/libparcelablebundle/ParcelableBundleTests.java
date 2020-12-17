package smallville7123.libparcelablebundle;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import smallville7123.libparcelablebundle.tools.IndentingPrintWriter;
import smallville7123.libparcelablebundle.tools.LogWriter;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest=Config.NONE)
public class ParcelableBundleTests {
    static LogWriter log = new LogWriter("logWriter").toSystemOut();

    @Test
    public void creation_and_destruction_01() {
        new ParcelableBundle();
        System.gc();
    }

    @Test
    public void addition_isCorrect() {
        ParcelableBundle b = new ParcelableBundle();
        b.putInt("k", 5);
        ParcelableBundle.dumpStats(new IndentingPrintWriter(log, " "), b);
        assertEquals(b.getInt("k"), 5);
    }
}