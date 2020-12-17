package smallville7123.simple.vst.demo.pack.sequencer.pfseq;

import android.media.AudioTimestamp;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.media.AudioFormat.CHANNEL_OUT_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.os.Process.THREAD_PRIORITY_URGENT_AUDIO;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeq.LOG_TAG;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.BUFFER_SIZE_BYTES;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.MIN_MILLIS_AHEAD_TO_WRITE;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.SAMPLE_RATE;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.SYNC_MARGIN_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.TIMESTAMP_POLLING_DELAY_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.MESSAGE_TYPE_ALERT;

public class PFSeqTrack {
    static final String WORK_THREAD_NAME = "PFSeq work thread";

    private String TRACK_LOG_PREFIX;
    private PFSeqRunnable seq;
    private AudioTrack at;
    private boolean initialized;
    private ArrayList<PFSeqPianoRollItem> pianoRoll;
    private String name;
    private HandlerThread workThread;
    private Handler workThreadHandler;
    private int totalFramesWritten;
    private int bufferSizeFrames;
    private int bufferSizeBytes;
    private long atStartNanotime;
    private long atStartMillisImprecise;
    private boolean nanoIsMapped;
    private AtomicBoolean isWriteLocked;
    private long writeStart;
    private long otherWriteStart;
    private AtomicBoolean isPlaying;

    public PFSeqTrack(PFSeqRunnable seq, String name) {
        this.seq = (PFSeqRunnable) seq;
        this.name = name;
        this.TRACK_LOG_PREFIX = "track " + name + " - ";
        this.isWriteLocked = new AtomicBoolean(false);
        this.isPlaying = new AtomicBoolean(false);

        pianoRoll = new ArrayList<PFSeqPianoRollItem>();
        initializeAT();
    }

    // core sequencer stuff
    boolean initializeAT() {
        initialized = false;
        bufferSizeFrames = 0;
        bufferSizeBytes = 0;

        int bufferSizeBytesRequested = seq.getConfig().getInt(BUFFER_SIZE_BYTES);
        int sampleRate = seq.getConfig().getInt(SAMPLE_RATE);

        at = new AudioTrack(STREAM_MUSIC, sampleRate, CHANNEL_OUT_STEREO, ENCODING_PCM_16BIT, bufferSizeBytesRequested, AudioTrack.MODE_STREAM);

        if (at == null) {
            return false;
        }

        initialized = true;
        bufferSizeFrames = at.getBufferSizeInFrames();
        bufferSizeBytes = bufferSizeFrames * seq.bytesPerFrame();
        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "track initialized");
        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "bufferSizeFrames: " + bufferSizeFrames);
        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "bufferSizeBytes: " + bufferSizeBytes);

        return true;
    }
    void startAT() {
        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "hasUnderrun() is " + ( hasUnderrun() ? "true" : "false" ) );
        if (!isInitialized()) {
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "failed to start playing because track not initialized");
            return;
        }
        if (!startWorkThread()) {
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "startWorkThread failed");
            return;
        }

        totalFramesWritten = 0;
        nanoIsMapped = false;
        atStartMillisImprecise = -1;
        nanoIsMapped = false;

        /*
           calculate start time after started from an AudioTimestamp
           this is where we map the dimension of frames to the dimension of nanotime
           the accuracy is only limited by the native accuracy returned in the audiotimestamp instance
        */
        final AudioTimestamp timestamp = new AudioTimestamp();
        at.setNotificationMarkerPosition(1);
        at.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack audioTrack) {
                Log.d(LOG_TAG, TRACK_LOG_PREFIX + "onMarkerReached");

                final Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        boolean timestampSuccess = at.getTimestamp(timestamp);
                        if(!timestampSuccess) {
                            handler.postDelayed(this, seq.getConfig().getInt(TIMESTAMP_POLLING_DELAY_MILLIS));
                            return;
                        }
                        atStartNanotime = calculateStartNanotime(timestamp);
                        nanoIsMapped = true;
                    }
                };
                handler.post(runnable);
            }

            @Override
            public void onPeriodicNotification(AudioTrack audioTrack) {

            }
        }, workThreadHandler);

        // write preliminary block of silence and start
        final short[] preliminarySilence = seq.makeSilence(getSeq().nanoToFrames(getSeq().getConfig().getInt(MIN_MILLIS_AHEAD_TO_WRITE) * (int) PFSeq.NANO_PER_MILLIS));
        Runnable runnable_ = new Runnable() {
            @Override
            public void run() {
                writeToAt(preliminarySilence, true);
                at.play();
                isPlaying.set(true);
                atStartMillisImprecise = System.currentTimeMillis();
                Log.d(LOG_TAG, TRACK_LOG_PREFIX + "preliminary silence written and audiotrack status is " + getPlaystateString());
                Log.d(LOG_TAG, TRACK_LOG_PREFIX + "hasUnderrun() is " + ( hasUnderrun() ? "true" : "false" ) );
            }
        };
        isWriteLocked.set(true);
//        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "setting write locked true");
        workThreadHandler.post(runnable_);
    }
    boolean stopAT() {
        Log.d(LOG_TAG, "stopAT() called");
        if (!isPlaying()) {
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "failed to stop audiotrack");
            return false;
        }

        isPlaying.set(false);
        at.stop();
        Log.d(LOG_TAG, "stopAT() stopping working thread");
        stopWorkThread();
        at.release();
        initializeAT();
        nanoIsMapped = false;
        isWriteLocked.set(false);

        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "end of stopAT()");
        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "hasUnderrun() is " + ( hasUnderrun() ? "true" : "false" ) );
        return true;
    }
    boolean sync(long contentStartNanotime) {
        if (!isPlaying()) {
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "failed to start synced playing because audiotrack not playing");
            return false;
        }

        int framesToLeaveBeforeNextItem = getSeq().getConfig().getInt(FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM);
        long silenceNeededBeforeStartNano = (long) (contentStartNanotime - soonestWritableNanotime());
        int silenceNeededBeforeStartFrames = seq.nanoToFrames(silenceNeededBeforeStartNano) - framesToLeaveBeforeNextItem;
        if (silenceNeededBeforeStartFrames >= 0) {
            // fill gap between last buffered data and content start time
            postWrite(seq.makeSilence(silenceNeededBeforeStartFrames),true);
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "posted write - silence written up to content start time. " + silenceNeededBeforeStartNano / seq.NANO_PER_SECOND + " s of silence");
        }

        return true;
    }
    boolean postWrite(final short[] pcm, final boolean blocking) {
        // writes array of pcm arrays, in order

        if (workThreadHandler == null || !workThreadIsRunning()) {
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "failed to post to work thread");
            return false;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                otherWriteStart = System.currentTimeMillis();
                writeToAt(pcm, blocking);
//                Log.d(LOG_TAG, "wrote " + (pcm.length / 2)  + " frames - " + (blocking ? "blocking" : "not blocking") + " - frames written: " + framesWritten);
//                Log.d(LOG_TAG, "time spent: " + (System.currentTimeMillis() - writeStart) + " ms" );
//                Log.d(LOG_TAG, "other time spent: " + (System.currentTimeMillis() - otherWriteStart) + " ms" );
            }
        };
        isWriteLocked.set(true);
        workThreadHandler.post(runnable);
//        Log.d(LOG_TAG, "setting write locked true");
        writeStart = System.currentTimeMillis();

        return true;
    }
    private int writeToAt(short[] pcm, boolean blocking) {
        // method not meant to be called directly. use postWrite()

        int transferCountShorts = -1;
        if (blocking) {
            transferCountShorts = at.write(pcm, 0, pcm.length);
        } else {
            transferCountShorts = at.write(pcm, 0, pcm.length, AudioTrack.WRITE_NON_BLOCKING);
        }

        if (transferCountShorts > 0) {
            totalFramesWritten += transferCountShorts / 2;
        } else {
            if (transferCountShorts < 0) {
                Log.d(LOG_TAG, "write error occurred: " + transferCountShorts);
            }
        }

//        Log.d(LOG_TAG, "setting write locked false");
        isWriteLocked.set(false);
//        Log.d(LOG_TAG, "write locked: " + (isWriteLocked.get() ? "true" : "false"));

        return transferCountShorts;
    }
    public void addPianoRollItem (PFSeqPianoRollItem item) {
        if (!item.getClip().isLoadedSuccessfully()) {
            getSeq().sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ALERT, "clip not loaded successfully. pr item disabled. " + item.getClip().getFile().getName() + " - " + item.getClip().getErrorMsg()));
        }
        pianoRoll.add(item);
    }
    public PFSeqPianoRollItem nextPianoRollItemAfter(long nano) {
        // may return null if pfseq no longer playing or in non-repeating mode and no more items

        if (!getSeq().isPlaying()) {
            Log.d(LOG_TAG, "can't get next piano roll item after. not playing");
            return null;
        }

        long soonestNextNano = Long.MAX_VALUE;
        PFSeqPianoRollItem soonestItem = null;
        long thisItemNextNano;
        for (PFSeqPianoRollItem item : pianoRoll) {
            if (!item.isEnabled()) {
                continue;
            }
            if (!item.getClip().isLoadedSuccessfully()) {
                continue;
            }
            thisItemNextNano = item.soonestNanoAfter(nano);
            if (thisItemNextNano < soonestNextNano && thisItemNextNano > nano) {
                soonestNextNano = thisItemNextNano;
                soonestItem = item;
            }
        }

        return soonestItem;
    }

    public PFSeqPianoRollItem getPrItem(String name) {
        for (PFSeqPianoRollItem item : pianoRoll) {
            if (item.getName().equals(name)) {
                return item;
            }
        }

        return null;
    }

    // timing stuff
    public long soonestWritableNanotime() {
        // don't call if isPlaying(), nanoIsMapped(), or isWriteLocked() is true

        if (!isPlaying()) {
            Log.d(LOG_TAG, "soonestWritableNanotime() should not be called if track not playing");
            return -1;
        }
        if (!nanoIsMapped()) {
            getSeq().stopSelf( TRACK_LOG_PREFIX + "soonestWritableNanotime() should not be called if nano is not mapped");
            return -1;
        }
        if (isWriteLocked()) {
            getSeq().stopSelf(TRACK_LOG_PREFIX + "soonestWritableNanotime() should not be called if track is write-locked");
            return -1;
        }

        return getAtStartNanotime() + getSeq().framesToNano(totalFramesWritten);
    }
    long soonestWritableMillisImprecise() {
        if (atStartMillisImprecise < 0) {
            return -1;
        }

        return atStartMillisImprecise + getSeq().framesToMillis(totalFramesWritten);
    }
    public boolean enoughWrittenToSync() {
        // should not be called if write-lock is true.

        long syncMarginNano = getSeq().getConfig().getInt(SYNC_MARGIN_MILLIS) * (long) PFSeq.NANO_PER_MILLIS;
        long nanoWeWantWrittenUntil = System.nanoTime() + (getSeq().getConfig().getInt(MIN_MILLIS_AHEAD_TO_WRITE) * (long) PFSeq.NANO_PER_MILLIS);

        if (soonestWritableNanotime() > nanoWeWantWrittenUntil - syncMarginNano) {
            return true;
        }
        return false;
    }
    private long calculateStartNanotime(AudioTimestamp timestamp) {
        long nanoSecondsFromFirstFrame = getSeq().framesToNano((int) timestamp.framePosition);

        long nowNano = timestamp.nanoTime;
//        long nowMillis = System.currentTimeMillis();
        long atStartNanotime = nowNano - nanoSecondsFromFirstFrame;

        /* dev */
//        long atStartMillisFromTimestamp = nowMillis - (long) (nanoSecondsFromFirstFrame / PFSeq.NANO_PER_MILLIS);
//        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "atStartMillisFromTimestamp: " + atStartMillisFromTimestamp);

        return atStartNanotime;
    }

    // work thread stuff
    private boolean startWorkThread() {
        if (workThread != null && workThread.isAlive()) {
            return false;
        }
        workThread = new HandlerThread(WORK_THREAD_NAME, THREAD_PRIORITY_URGENT_AUDIO);
        workThread.start();
        workThreadHandler = new Handler(workThread.getLooper());

        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "work thread started");
        return true;
    }
    public boolean stopWorkThread() {
        if (!workThreadIsRunning()) {
            return false;
        }
        workThread.quit();
        workThread = new HandlerThread(WORK_THREAD_NAME);
        workThreadHandler = null;
        Log.d(LOG_TAG, TRACK_LOG_PREFIX + "work thread stopped");
        return true;
    }
    public boolean workThreadIsRunning() {
        if (workThread == null) {
            return false;
        }
        return workThread.isAlive();
    }

    // audiotrack stuff
    public String getPlaystateString() {
        if (!isInitialized()) {
            return "not initialized";
        }

        int playstateConstant = at.getPlayState();

        switch (playstateConstant) {
            case AudioTrack.PLAYSTATE_PLAYING:  return "playing";
            case AudioTrack.PLAYSTATE_PAUSED:   return "paused";
            case AudioTrack.PLAYSTATE_STOPPED:  return "stopped";
            default:                            return "playstate not found";
        }
    }
    public boolean hasUnderrun() {
        if (at == null) {
            return false;
        }
        if (at.getUnderrunCount() > 0) {
            Log.d(LOG_TAG, TRACK_LOG_PREFIX + "underruns:" + at.getUnderrunCount());
            return true;
        }
        return false;
    }

    // accessors
    public boolean isPlaying() {
        return isPlaying.get();
    }
    public ArrayList<PFSeqPianoRollItem> getPianoRoll() { return pianoRoll; }
    public boolean isInitialized() { return initialized; }
    public PFSeqRunnable getSeq() {
        return seq;
    }
    public Handler getWorkThreadHandler() {
        return workThreadHandler;
    }
    public String getName() {
        return name;
    }
    public long getAtStartNanotime() {
        return atStartNanotime;
    }
    public boolean nanoIsMapped() {
        return nanoIsMapped;
    }
    public int getTotalFramesWritten() {
        return totalFramesWritten;
    }
    public boolean isWriteLocked() {
        return isWriteLocked.get();
    }
}
