package smallville7123.simple.vst.demo.pack.sequencer.pfseq;

/*
Copyright (C) 2019 People's Feelings
Licensed under GPL v2
https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html
*/

import android.app.Notification;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.os.Process.THREAD_PRIORITY_URGENT_AUDIO;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.CONTROL_THREAD_POLLING_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.FADE_LENGTH_FRAMES;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.MAX_BPM;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.MAX_TRACKS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.MIN_BPM;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.MIN_MILLIS_AHEAD_TO_WRITE;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.MIN_WRITABLE_CONTENT_NANO;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.RUN_IN_FOREGROUND;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.SAMPLE_RATE;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.SMALLEST_STOPGAP_SILENCE_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.SYNC_POLLING_SLEEP_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.SYNC_TIME_OUT_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.TEMPO;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.TIMESTAMP_POLLING_DELAY_MILLIS;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.ALERT_MSG_PREFIX;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.ERROR_MSG_PREFIX;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.MESSAGE_TYPE_ALERT;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.MESSAGE_TYPE_ERROR;

public class PFSeqRunnable implements Runnable {
    public static final String LOG_TAG = "**pf seq**";
    static final String CONTROL_THREAD_NAME = "control thread";
    static final String LOG_EOL = "\n";
    static final double NANO_PER_SECOND = 1000000000.0;
    static final double NANO_PER_MILLIS = 1000000;
    static final double NANO_PER_MICROS = 1000.0;
    static final double MICROS_PER_SECOND = 1000000.0;
    static final double MILLIS_PER_SECOND = 1000.0;
    static final BigDecimal NANOSECONDS_PER_MINUTE = new BigDecimal(60000000000.0);

    private PFSeqMessage stashedMessage;
    private Notification notification;
    private PFSeqActivity pFSeqActivity;
    private PFSeqConfig _config;
    private ArrayList<PFSeqTrack> tracks;
    private HandlerThread controlThread;
    private Handler controlThreadHandler;
    private Runnable syncTracks;
    private Runnable contentWriting;
    private Runnable silenceUntilMapped;

    private AtomicBoolean isPlaying;
    private AtomicBoolean isSetUp;

    private BigDecimal bpm;
    private BigDecimal nanosecondsPerBeat;
    private BigDecimal tempoStartNanotime;

    // service life cycle stuff

    AtomicBoolean running = new AtomicBoolean(false);
    AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void run() {
        running.set(true);
        isPlaying = new AtomicBoolean(false);
        isSetUp = new AtomicBoolean(false);
        controlThread = new HandlerThread(CONTROL_THREAD_NAME, THREAD_PRIORITY_URGENT_AUDIO);
        controlThread.start();
        controlThreadHandler = new Handler(controlThread.getLooper());
        createRunnables();
        initialized.set(true);
        while(running.get());
        stopAllWorkThreads();
    }

    public void waitForInitialization() {
        while(!initialized.get());
    }

    // messages for PFSeqActivity stuff
    public void setStashedMessage(PFSeqMessage message) {
        stashedMessage = message;
    }
    public PFSeqMessage getStashedMessage() {
        return stashedMessage;
    }
    public void sendMessageToActivity(PFSeqMessage message) {
        String prefix = "";

        switch (message.getType()) {
            case MESSAGE_TYPE_ALERT: prefix = ALERT_MSG_PREFIX;
                break;
            case MESSAGE_TYPE_ERROR: prefix = ERROR_MSG_PREFIX;
                break;
        }

        Log.d(LOG_TAG, "receiveMessage call - " + prefix + message.getMessage());
    }

    // core sequencer stuff
    public boolean setUpSequencer(PFSeqConfig config) {
        if (isPlaying()) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "Cannot set up sequencer while playing"));
            return false;
        }

        if (!config.isValid()) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "Sequencer config not valid"));
            return false;
        }
        this._config = config;
        tracks = new ArrayList<PFSeqTrack>();
        isSetUp.set(true);
        setBpm(getConfig().getDouble(TEMPO));
        Log.d(LOG_TAG, "Sequencer set up");
        return true;
    }
    public void play() {
        Log.d(LOG_TAG, "play() called");
        if (!isSetUp() || tracks == null || tracks.size() == 0 || !allTracksInitialized()) {
            Log.d(LOG_TAG, "couldn't play");
            return;
        }
        tempoStartNanotime = null;

        // start AudioTracks
        Log.d(LOG_TAG, "starting");
        isPlaying.set(true);
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).startAT();
        }

        if (getConfig().getBool(RUN_IN_FOREGROUND)) {
            Log.d(LOG_TAG, "started foreground");
        }

        // start imprecise silence writing until nanotime is mapped to frames written
        controlThreadHandler.post(silenceUntilMapped);
    }
    private boolean sync(long contentStartNanotime) {
        Log.d(LOG_TAG, "syncing tracks");
        Log.d(LOG_TAG, "content start time: " + (contentStartNanotime - System.nanoTime()) / PFSeqRunnable.NANO_PER_SECOND + " s from now");

        for (PFSeqTrack track : tracks) {
            boolean trackSyncSuccess = track.sync(contentStartNanotime);
            if (!trackSyncSuccess) {
                sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ALERT, "track " + track.getName() + " failed to sync"));
                return false;
            }
        }

        this.tempoStartNanotime = new BigDecimal(contentStartNanotime);
        return true;
    }
    private void createRunnables() {
        /*
        after user hits play, write silence until all tracks are playing and have received their AudioTimestamps.
        then post the syncTracks runnable and exit this one.
        */
        silenceUntilMapped = new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "silenceUntilMapped started");

                int pollingMillis = getConfig().getInt(TIMESTAMP_POLLING_DELAY_MILLIS);
                final int millisAheadToWrite = getConfig().getInt(MIN_MILLIS_AHEAD_TO_WRITE);
                long referenceTime = System.currentTimeMillis() - pollingMillis;
                int tracksSize = tracks.size();
                outerloop:
                while (isPlaying()) {
                    long currentTime = System.currentTimeMillis();
                    // sleep until it's time to check if nanoIsMapped
                    if (currentTime < referenceTime + pollingMillis) {
                        try {
                            Thread.sleep(referenceTime + pollingMillis - currentTime);
                        } catch (InterruptedException e) {
                            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "failed to sleep"));
                        }
                    }

                    for (int i = 0; i < tracksSize; i++) {
                        final PFSeqTrack track = tracks.get(i);
                        if (!track.isPlaying()) {
                            continue;
                        }
                        if (track.nanoIsMapped()) {
                            // check the others
                            boolean allTracksMapped = true;
                            for (int j = 0; j < tracksSize; j++) {
                                if (i == j) {
                                    continue;
                                }
                                if (!tracks.get(j).isPlaying() || !tracks.get(j).nanoIsMapped()) {
                                    allTracksMapped = false;
                                    break;
                                }
                            }

                            if (allTracksMapped) {
                                Log.d(LOG_TAG, "all tracks mapped");

                                // post the next runnable and exit this one
                                controlThreadHandler.post(syncTracks);
                                break outerloop;
                            }
                        }

                        // write silence
                        if (!track.isWriteLocked() && track.isPlaying()) {
                            long currentMillis = System.currentTimeMillis();
                            long soonestWritableMillis = track.soonestWritableMillisImprecise();
                            if (soonestWritableMillis < (currentMillis + millisAheadToWrite)) {
                                long silenceMillis = (currentMillis + millisAheadToWrite) - track.soonestWritableMillisImprecise();
                                int silenceFrames = nanoToFrames(silenceMillis * (long) NANO_PER_MILLIS);
                                if (silenceFrames > 0) {
                                    short[] silence = makeSilence(silenceFrames);
                                    Log.d(LOG_TAG, "posting write - silenceUntilMapped " + silenceMillis  + " ms");
                                    track.postWrite(silence, true);
                                }
                            }
                        }
                    }

                    referenceTime = System.currentTimeMillis();
                }
            }
        };

        /*
        wait until all tracks have enough written that we can stop writing briefly without an underrun.
        then determine soonest time that is writable for all tracks, and write silence up to that point.
        then post contentWriting runnable and exit this one
        */
        syncTracks = new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "syncTracks started");

                long startWaitingTime = System.currentTimeMillis();
                int tracksSize = tracks.size();
                int i = 0;
                while (isPlaying()) {
                    // check for timeout
                    if (System.currentTimeMillis() > startWaitingTime + getConfig().getInt(SYNC_TIME_OUT_MILLIS)) {
                        sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "timeout reached"));
                        return;
                    }
                    final PFSeqTrack track = tracks.get(i);

                    // if no writes are expected on the track
                    if (!track.isWriteLocked()) {
                        // check if we can sync the tracks and start the content
                        if (track.enoughWrittenToSync()) {
                            // check the others
                            boolean allTracksCanSync = true;
                            for (int j = 0; j < tracksSize; j++) {
                                if (i == j) {
                                    continue;
                                }

                                if (tracks.get(j).isWriteLocked() || !track.enoughWrittenToSync()) {
                                    allTracksCanSync = false;
                                    break;
                                }
                            }

                            // sync
                            if (allTracksCanSync) {
                                long soonestWritableNano = Long.MAX_VALUE;
                                long tempNano;

                                for (PFSeqTrack theTrack : tracks) {
                                    tempNano = theTrack.soonestWritableNanotime();
                                    if (tempNano < soonestWritableNano) {
                                        soonestWritableNano = tempNano;
                                    }
                                }

                                int framesToLeaveBeforeNextItem = getConfig().getInt(FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM);
                                long contentStartNano = soonestWritableNano + framesToNano(framesToLeaveBeforeNextItem);

                                if (sync(contentStartNano)) {
                                    Log.d(LOG_TAG, "sync successful");

                                    // post the next runnable and exit this one
                                    controlThreadHandler.post(contentWriting);
                                    break;
                                } else {
                                    Log.d(LOG_TAG, "failed to sync");
                                    continue;
                                }
                            }
                        }

                        // write silence because we can't sync yet
                        long soonestWritableNano = tracks.get(i).soonestWritableNanotime();
                        Log.d(LOG_TAG, "soonestWritableNano: " + soonestWritableNano);
                        long nanoWeWantWrittenUntil = System.nanoTime() + (getConfig().getInt(MIN_MILLIS_AHEAD_TO_WRITE) * (long) NANO_PER_MILLIS);
                        int smallestStopgapSilenceNano = getConfig().getInt(SMALLEST_STOPGAP_SILENCE_MILLIS) * (int) NANO_PER_MILLIS;
                        if (soonestWritableNano < nanoWeWantWrittenUntil) {
                            final long spaceToFillNano = nanoWeWantWrittenUntil - soonestWritableNano;

                            if (spaceToFillNano > smallestStopgapSilenceNano) {
                                int spaceToFillFrames = nanoToFrames(spaceToFillNano);
                                Log.d(LOG_TAG, "spaceToFillFrames: " + spaceToFillFrames);
                                track.postWrite(makeSilence(spaceToFillFrames), false);
                            }
                        }
                    }

                    // sleep
                    try {
                        Thread.sleep(getConfig().getInt(SYNC_POLLING_SLEEP_MILLIS));
                    } catch (InterruptedException e) {
                        Log.d(LOG_TAG, "failed to sleep");
                    }

                    i++;
                    if (i == tracksSize) {
                        i = 0;
                    }
                }
            }
        };

        /*
        assumes all tracks have silence written up to the "content start nanotime".
        this runnable wheels and deals segments of PCM specified by the app/user
        in the track pianoroll items, throwing silence where needed to prevent underrun.
        */
        contentWriting = new Runnable() {
            @Override
            public void run(){
                Log.d(LOG_TAG, "contentWriting started");

                int pollingMillis = getConfig().getInt(CONTROL_THREAD_POLLING_MILLIS);
                int minMillisAheadToWrite = getConfig().getInt(MIN_MILLIS_AHEAD_TO_WRITE);
                int framesToLeaveBeforeNextItem = getConfig().getInt(FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM);
                int minWritableContentNano = getConfig().getInt(MIN_WRITABLE_CONTENT_NANO);
                long referenceTime = System.currentTimeMillis() - pollingMillis;

                outerloop:
                while (true) {
                    long currentMillis = System.currentTimeMillis();
                    // if the polling time has not yet passed
                    if (currentMillis < referenceTime + pollingMillis) {
                        try {
                            // sleep until then
                            Thread.sleep(referenceTime + pollingMillis - currentMillis);
                        } catch (InterruptedException e) {
                            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "failed to sleep"));
                        }
                    }
                    referenceTime = System.currentTimeMillis();

                    for (final PFSeqTrack track : tracks) {
                        if (!isPlaying() || track.getWorkThreadHandler() == null) {
                            break outerloop;
                        }
                        if (track.hasUnderrun()) {
                            stopSelf("track has underrun");
                            break outerloop;
                        }
                        // don't write to tracks that already have stuff to write
                        if (track.isWriteLocked()) {
                            Log.d(LOG_TAG, "waiting on write-lock");
                            continue;
                        }

                        Log.d(LOG_TAG, "-------");

                        final long soonestWritableNano = track.soonestWritableNanotime();
                        long nowNano = System.nanoTime();
                        long nanoAheadToWrite = minMillisAheadToWrite * (long) NANO_PER_MILLIS;
                        long nanoWeWantWrittenUntil = nowNano + nanoAheadToWrite;
//                        Log.d(LOG_TAG, "nowNano: " + nowNano);
//                        Log.d(LOG_TAG, "nanoAheadToWrite: " + nanoAheadToWrite);
//                        Log.d(LOG_TAG, "nanoWeWantWrittenUntil: " + nanoWeWantWrittenUntil);
//                        Log.d(LOG_TAG, "soonestWritableNano: " + soonestWritableNano);
//                        Log.d(LOG_TAG, "written out " + ((soonestWritableNano - System.nanoTime()) / NANO_PER_MILLIS) + " ms");

                        // if it's time to
                        if (soonestWritableNano < nanoWeWantWrittenUntil) {
                            if (track.getPianoRoll().size() > 0) {
                                PFSeqPianoRollItem nextPRItem = track.nextPianoRollItemAfter(soonestWritableNano);
                                if (nextPRItem == null) {
                                    if (isPlaying()) {
                                        // nextPianoRollItemAfter returned null. write silence
                                        Log.d(LOG_TAG, "posting write - silence only: " + (nanoWeWantWrittenUntil - soonestWritableNano) + " ns. nextPRItem is null");
                                        writeSilenceToTrack(track, nanoWeWantWrittenUntil - soonestWritableNano);
                                    } else {
                                        // sequencer stopping
                                        break outerloop;
                                    }
                                } else {
                                    Log.d(LOG_TAG, "nextPRItem: " + nextPRItem.getName());
                                    long nextPRItemNano = nextPRItem.soonestNanoAfter(soonestWritableNano);

                                    if (nextPRItemNano > nanoWeWantWrittenUntil) {
                                        // next item is after time we want written until. write silence
                                        Log.d(LOG_TAG, "posting write - silence only: " + (nanoWeWantWrittenUntil - soonestWritableNano) + " ns. next item is after time we want written until");
                                        writeSilenceToTrack(track, nanoWeWantWrittenUntil - soonestWritableNano);
                                    } else {
                                        short[] itemPcm = nextPRItem.getPcm();
                                        final short[] leadingSilence = makeSilence((int) nanoToFrames(nextPRItemNano - soonestWritableNano));

                                        // abridge stuff
                                        int originalLengthFrames = itemPcm.length / 2;
                                        int neededLengthFrames = originalLengthFrames;
                                        // get length needed due to subsequent item
                                        long nextItemNanoPlusMinWritableNano = nextPRItemNano + minWritableContentNano;
                                        PFSeqPianoRollItem itemAfterNext = track.nextPianoRollItemAfter(nextItemNanoPlusMinWritableNano);
                                        if (itemAfterNext != null) {
                                            long itemAfterNextPosNano = itemAfterNext.soonestNanoAfter(nextItemNanoPlusMinWritableNano);
                                            long nextItemLengthNano = framesToNano(nextPRItem.lengthInFrames());
                                            long nextItemEndNano = nextPRItemNano + nextItemLengthNano;
                                            long nanotoLeaveBeforeNextItem = framesToNano(framesToLeaveBeforeNextItem);
                                            if (nextItemEndNano + nanotoLeaveBeforeNextItem > itemAfterNextPosNano) {
                                                neededLengthFrames = nanoToFrames(itemAfterNextPosNano - nextPRItemNano) - framesToLeaveBeforeNextItem;
                                                if (neededLengthFrames < 0) {
                                                    neededLengthFrames = 0;
                                                }
                                            }
                                        }
                                        // get length needed due to length property
                                        if (nextPRItem.getLength() != null) {
                                            long maxLengthFrames = nextPRItem.getLength().getLengthFrames();

                                            if (maxLengthFrames < neededLengthFrames) {
                                                neededLengthFrames = (int) maxLengthFrames;
                                            }
                                        }
                                        // abridge
                                        if (neededLengthFrames < originalLengthFrames) {
                                            itemPcm = abridge(itemPcm, neededLengthFrames);
                                        }

                                        Log.d(LOG_TAG, "posting write - silence and item. nano: " + (framesToNano((leadingSilence.length + itemPcm.length) / 2) ) );
                                        track.postWrite(combineShortArrays(leadingSilence, itemPcm), true);
                                    }
                                }
                            } else {
                                // piano roll is empty - write silence
                                Log.d(LOG_TAG, "posting write - silence only: " + (nanoWeWantWrittenUntil - soonestWritableNano) + " ns. piano roll is empty");
                                writeSilenceToTrack(track, nanoWeWantWrittenUntil - soonestWritableNano);
                            }
                        }
                    }
                }
                Log.d(LOG_TAG, "contentWriting done");
            }
        };
    }
    public boolean stop() {
        Log.d(LOG_TAG, "stop() called");

        if (controlThreadHandler != null) {
            // clear all messages (includes posts)
            controlThreadHandler.removeCallbacks(null);
        }

        if (isPlaying()) {
            int underrunCount = 0;
            for (int i = 0; i < tracks.size(); i++) {
                if (tracks.get(i).hasUnderrun()) {
                    underrunCount++;
                }
                tracks.get(i).stopAT();
            }
            if (underrunCount == 0) {
                Log.d(LOG_TAG, "no underrun occurred");
            } else {
                Log.d(LOG_TAG, "tracks with underrun: " + underrunCount);
            }

            isPlaying.set(false);
            tempoStartNanotime = null;
        }

        return true;
    }

    public void stopSelf(String msg) {
        /* for error handling */

        sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "Had to stop sequencer. Cause: " + msg));
        stop();
        running.set(false);
    }
    public double setBpm(double newBpm) {
        if (getConfig() == null || !getConfig().isValid()) {
            return -1;
        }
        int configMax = getConfig().getInt(MAX_BPM);
        int configMin = getConfig().getInt(MIN_BPM);

        if (newBpm < configMin) {
            newBpm = configMin;
        }
        if (newBpm > configMax) {
            newBpm = configMax;
        }

        if (isPlaying() && tempoStartNanotime != null) {
            /*
                this is where we rewrite the past, as though it always were the new tempo.
                we change the tempo start time to a different point in the past.
                this allows changing bpm while playing.

                doubling the bpm will halve the amount of time needed for the same amount of beats. so:

                    old tempo   new duration
                    --------- = ------------
                    new tempo   old duration

                so:
                                    ( old tempo / new tempo )
                    new duration = ---------------------------
                                          old duration
            * */
            BigDecimal nowNano = new BigDecimal(System.nanoTime());
            BigDecimal newBpmBD = new BigDecimal(newBpm);
            BigDecimal oldElapsed = nowNano.subtract(this.tempoStartNanotime);
            // adjust to taste
            BigDecimal newElapsed = getBpm().divide(newBpmBD, 10, RoundingMode.HALF_DOWN).divide(oldElapsed, 0, RoundingMode.HALF_DOWN);
            this.tempoStartNanotime = nowNano.subtract(newElapsed);
        }

        this.bpm = new BigDecimal(newBpm);

        // adjust to taste
        this.nanosecondsPerBeat = NANOSECONDS_PER_MINUTE.divide(getBpm(), 10, BigDecimal.ROUND_HALF_DOWN);
        /*
        *       used by:
                    soonestNanoAfter(long nanotime)
                    beatsSinceTempoStart(long nanotime)
                    beatStartNanotime(int beats)
        * */
        Log.d(LOG_TAG, "nanosecondsPerBeat: " + nanosecondsPerBeat.toString());

        return newBpm;
    }
    private void writeSilenceToTrack(PFSeqTrack track, long silenceNano) {
        final short[] silence = makeSilence(nanoToFrames(silenceNano));
//        Log.d(LOG_TAG, "posting write - silence only: " + silenceNano + " ns");
        track.postWrite(silence, true);
    }
    public boolean unSetUpSequencer() {
        stop();

        isSetUp.set(false);
        _config = null;

        Log.d(LOG_TAG, "unsetup");
        return true;
    }

    // track stuff
    public boolean addTrack(PFSeqTrack track) {
        // sequencer must be set up
        if (!isSetUp()) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ALERT, "could not add track because sequencer has no bee set up"));
            return false;
        }
        if (tracks.size() == getConfig().getInt(MAX_TRACKS)) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ALERT, "could not add track because track limit reached"));
            return false;
        }
        if (isPlaying()) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ALERT, "cannot add track while playing"));
            return false;
        }

        tracks.add(track);
        Log.d(LOG_TAG, "track " + track.getName() + " - added to sequencer");
        return true;
    }
    public PFSeqTrack getTrack(String name) {
        for (PFSeqTrack track : tracks) {
            if (track.getName().equals(name)) {
                return track;
            }
        }

        return null;
    }
    private boolean allTracksInitialized() {
        if (tracks == null) {
            Log.d(LOG_TAG, "tracks is null");
            return false;
        }

        for (PFSeqTrack track : tracks) {
            if (!track.isInitialized()) {
                return false;
            }
        }
        return true;
    }
    private void stopAllWorkThreads() {
        Log.d(LOG_TAG, "stopAllWorkThreads() called");
        if (tracks != null) {
            for (PFSeqTrack track : tracks) {
                track.stopWorkThread();
            }
        }
    }

    // audio stuff
    public short[] makeSilence(int lengthFrames) {
//        Log.d(LOG_TAG, "makeSilence() - lengthFrames: " + lengthFrames + " shortsPerFrame(): " + shortsPerFrame());
        if (lengthFrames < 0) {
            return makeSilence(_config.getInt(MIN_MILLIS_AHEAD_TO_WRITE));
//            stopSelf("negative frame length");
        }

        short[] shortArray = new short[lengthFrames * 2];

        for (int i = 0; i < lengthFrames; i++) {
            shortArray[i] = 0;
        }

        return shortArray;
    }

    private short[] combineShortArrays(short[] array1, short[] array2) {
        if (array1 == null || array2 == null) {
            return null;
        }
        short[] joinedArray = new short[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public short[] abridge(short[] pcm, int newLengthFrames) {
        int oldLengthFrames = pcm.length / 2;
        int fadeOutLengthFrames = getConfig().getInt(FADE_LENGTH_FRAMES);

        if (newLengthFrames > oldLengthFrames) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "can't abridge clip to longer length"));
            return pcm;
        }

        short[] arrayWithNewLength = Arrays.copyOfRange(pcm, 0, newLengthFrames * 2);

        if (fadeOutLengthFrames > 0) {
            arrayWithNewLength = applyFadeOut(arrayWithNewLength, fadeOutLengthFrames);
        }

        return arrayWithNewLength;
    }
    private short[] applyFadeOut(short[] pcm, int fadeLengthFrames) {
        // this method definition assumes channel count is 2

        int pcmLengthFrames = pcm.length / 2;

//        long startingClock = System.currentTimeMillis();

        // in case config value fade length is greater than clip length
        int fadeLengthFramesSafe = fadeLengthFrames;
        if (fadeLengthFrames > pcmLengthFrames) {
            fadeLengthFramesSafe = pcmLengthFrames;
        }

        int startingFrame = pcmLengthFrames - fadeLengthFramesSafe;
        int startingShort = startingFrame * 2;
        int i;
        double distanceFromEndFrames;
        double positionInFade = 1;

        for (i = startingShort; i < pcm.length; i += 2) {
            distanceFromEndFrames = ((pcm.length - 2) - i) / 2;
            // starts at (below) 1 and goes to 0
            positionInFade = (double) distanceFromEndFrames / fadeLengthFramesSafe;

            // first channel of frame
            pcm[i] = (short) (pcm[i] * positionInFade);

            // second channel of frame
            pcm[i+1] = (short) (pcm[i+1] * positionInFade);
        }
//        Log.d(LOG_TAG, "time spent doing fade: " + (System.currentTimeMillis() - startingClock) + " ms. ");

        return pcm;
    }
    public int bytesPerFrame() {
        // bit depth multiplied by the number of channels
        if (!isSetUp()) {
            return -1;
        }

        return 2 * 2;
    }

    // timing stuff
    public int beatsSinceTempoStart(long nano) {
        if (tempoStartNanotime == null) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "tempoStartNanotime not set"));
            return -1;
        }

        BigDecimal elapsedNanotime = new BigDecimal(nano - tempoStartNanotime.longValue());
        /* adjust to taste */
        BigDecimal beatsElapsed = elapsedNanotime.divide(nanosecondsPerBeat, 0, BigDecimal.ROUND_DOWN);

        /*
        *                                NANOSECONDS_PER_MINUTE
        *       nanosecondsPerBeat   =   ----------------------
        *                                       getBpm()
        *
        *
         *
         *                                            elapsedNanotime
         *                       beatsElapsed   =   --------------------
         *                                           nanosecondsPerBeat
         *
         *
        *
        *
    *                                                      elapsedNanotime
        *                       beatsElapsed   =   ---------------------------------
        *                                          NANOSECONDS_PER_MINUTE / getBpm()
        *
        * */

        return beatsElapsed.intValue();
    }
    public long beatStartNanotime(int beatPos) {
        if (tempoStartNanotime == null) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "tempoStartNanotime not set"));
            return -1;
        }

        return tempoStartNanotime.longValue() + (nanosecondsPerBeat.longValue() * beatPos);
    }

    // conversions
    public int nanoToFrames(long durationNano) {
        if (!isSetUp()) {
            stopSelf("unexpected state. call to nanoToFrames when seq not set up");
            return -1;
        }

        // adjust to taste
        return (int) ( durationNano / NANO_PER_SECOND * getConfig().getInt(SAMPLE_RATE));
    }
    public int millisToFrames(int durationMillis) {
        if (!isSetUp()) {
            sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "unexpected state. call to millisToFrames when seq not set up"));
            return -1;
        }

        // adjust to taste
        return (int) ( durationMillis / MILLIS_PER_SECOND * getConfig().getInt(SAMPLE_RATE));
    }
    public int framesToMillis(int frames) {
        if (!isSetUp()) {
            stopSelf("unexpected state. call to framesToMillis when seq not set up");
            return -1;
        }

        // adjust to taste
        double framesPerMillis = (getConfig().getInt(SAMPLE_RATE) / MILLIS_PER_SECOND);
        return (int) (frames / framesPerMillis);
    }
    public long framesToNano(int frames) {
        if (!isSetUp()) {
            stopSelf("unexpected state. call to framesToNano when seq not set up");
            return -1;
        }

        // adjust to taste
        double framesPerNano = (getConfig().getInt(SAMPLE_RATE) / NANO_PER_SECOND);
        return (long) (frames / framesPerNano);
    }

    // accessors
    public BigDecimal getBpm() {
        return bpm;
    }
    public PFSeqActivity getActivity() {
        return pFSeqActivity;
    }
    public PFSeqConfig getConfig() {
        return _config;
    }
    public BigDecimal getTempoStartNanotime() {
        return tempoStartNanotime;
    }
    public boolean isSetUp() {
        return isSetUp.get();
    }
    public boolean isPlaying() {
        return isPlaying.get();
    }
    public BigDecimal getNanosecondsPerBeat() {
        return nanosecondsPerBeat;
    }
    public ArrayList<PFSeqTrack> getTracks() {
        return tracks;
    }
}
