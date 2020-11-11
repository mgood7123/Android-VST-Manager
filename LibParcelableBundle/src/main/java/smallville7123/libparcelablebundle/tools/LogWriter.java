package smallville7123.libparcelablebundle.tools;

import android.util.Log;

import java.io.Writer;

public class LogWriter extends Writer {
    private final String mTag;
    private StringBuilder mBuilder = new StringBuilder(128);
    private boolean toSystemOut;

    /**
     * Create a new Writer that sends to the log with the given priority
     * and tag.
     *
     * @param tag A string tag to associate with each printed log statement.
     */
    public LogWriter(String tag) {
        mTag = tag;
    }

    @Override public void close() {
        flushBuilder();
    }

    @Override public void flush() {
        flushBuilder();
    }

    @Override public void write(char[] buf, int offset, int count) {
        for(int i = 0; i < count; i++) {
            char c = buf[offset + i];
            if ( c == '\n') {
                flushBuilder();
            }
            else {
                mBuilder.append(c);
            }
        }
    }

    private void flushBuilder() {
        if (mBuilder.length() > 0) {
            String message = mBuilder.toString();
            Log.d(mTag, message);
            System.out.println(mTag + ": " + message);
            mBuilder.delete(0, mBuilder.length());
        }
    }

    public LogWriter toSystemOut() {
        LogWriter log = new LogWriter(mTag);
        log.mBuilder.append(mBuilder);
        log.toSystemOut = true;
        return log;
    }
}