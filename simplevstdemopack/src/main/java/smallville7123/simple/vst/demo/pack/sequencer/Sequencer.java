package smallville7123.simple.vst.demo.pack.sequencer;

import android.util.Log;

public class Sequencer implements Runnable {
    private static final String TAG = "Sequencer";

    private int tempo = 140;
    private boolean playing = false;
    private AudioDeck audioDeck;
    public Instrument[] instruments;

    public Sequencer(AudioDeck sp, Instrument[] instruments) {
        this.audioDeck = sp;
        this.instruments = instruments;
    }

    public void setIsPlaying(boolean playing) {
        this.playing = playing;
    }

    private void startSequence(Instrument[] instruments) {
        Log.e(TAG, "run: loop sequence start");
        main:
        while (true) {
            for (int i = 0; i < 8; i++) {
                if (!playing) {
                    for (Instrument instrument : instruments) {
                        AudioDeck.AudioTrack audioDeckTrack = audioDeck.getTrack(instrument.sound);
                        audioDeckTrack.pause();
                    }
                    break main;
                }
                for (Instrument instrument : instruments) {
                    if (instrument.getButtonAt(i).isChecked()) {
                        AudioDeck.AudioTrack audioDeckTrack = audioDeck.getTrack(instrument.sound);
                        audioDeckTrack.reset();
                        audioDeckTrack.play();
                    }
                }
                try {
                    Thread.sleep((1000 * 60) / 140);
                    System.out.println(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "run: loop sequence end");
    }

    @Override
    public void run() {
        while (true) {
            Log.e(TAG, "run: loop start");
            while (!playing) ;
            startSequence(this.instruments);
            Log.e(TAG, "run: loop end");
        }
    }
}