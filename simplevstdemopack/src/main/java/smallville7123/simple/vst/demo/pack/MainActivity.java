package smallville7123.simple.vst.demo.pack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.DrawableRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqClip;
import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig;
import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage;
import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqPianoRollItem;
import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqRunnable;
import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset;
import smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTrack;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.ID;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.ONGOING_NOTIF_ID;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.TIME_SIG_LOWER;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqConfig.TIME_SIG_UPPER;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.MESSAGE_TYPE_ALERT;
import static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqMessage.MESSAGE_TYPE_ERROR;

//public class MainActivity extends Activity {
//    SequencerApp app = new SequencerApp();
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        FrameLayout frameLayout = new FrameLayout(this);
//        ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        setContentView(frameLayout, matchParent);
//        app.setup(frameLayout, this);
//        app.onCreate(savedInstanceState);
//    }
//}

public class MainActivity extends Activity {
    final String CONFIG_ID = "metronome";

    static ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);

    void addToggleButton(Pair<LinearLayout, ArrayList<ToggleButton>> pair, @DrawableRes int id, LinearLayout.LayoutParams params) {
        ToggleButton button = new ToggleButton(this);
        button.setBackgroundResource(id);
        button.setTextOn("");
        button.setTextOff("");
        button.setText("");
        pair.second.add(button);
        pair.first.addView(button, params);
    }

    void addToggleButton(Pair<LinearLayout, ArrayList<ToggleButton>> pair, @DrawableRes int id) {
        addToggleButton(pair, id, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
    }

    ArrayList<Pair<LinearLayout, ArrayList<ToggleButton>>> rows = new ArrayList<>();

    void addRow(LinearLayout linearLayout) {
        Pair<LinearLayout, ArrayList<ToggleButton>> pair = new Pair<>(
                new LinearLayout(this),
                new ArrayList<>());
        pair.first.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(pair.first, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        for (int i = 0; i < 8; i++) {
            addToggleButton(pair, R.drawable.toggle);
        }
        rows.add(pair);
    }

    PFSeqRunnable seq;

    PFSeqRunnable getSeq() {
        return seq;
    }

    void play() {
        getSeq().play();
    }

    void pause() {
        getSeq().stop();
    }

    ToggleButton playback;

    private void initLayout() {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.sequencer, null, false);
        playback = linearLayout.findViewById(R.id.playbackButton);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, matchParent);
        LinearLayout sequencer = linearLayout.findViewById(R.id.sequenceLayout);
        addRow(sequencer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sequencer);
        initLayout();
        seq = new PFSeqRunnable();
        new Thread(seq).start();
        seq.waitForInitialization();
        playback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                play();
            } else {
                pause();
            }
        });

        if (getSeq().isSetUp()) {
            if (!getSeq().getConfig().getString(ID).equals(CONFIG_ID)) {
                getSeq().unSetUpSequencer();
                boolean success = configureSequecer(getSeq());
                if (success) {
                    setUpTracks(getSeq());
                }
            }
        } else {
            boolean success = configureSequecer(getSeq());
            if (success) {
                setUpTracks(getSeq());
            }
        }

        getSeq().setBpm(140);
    }


    private boolean configureSequecer(PFSeqRunnable seq) {
        HashMap<String, Boolean> myConfigBools = new HashMap<String, Boolean>() {{
//            put(RUN_IN_FOREGROUND, false);
//            put(REPEATING, false);
        }};
        HashMap<String, Integer> myConfigInts = new HashMap<String, Integer>() {{
            put(ONGOING_NOTIF_ID, 4346);
            put(TIME_SIG_UPPER, 1);
            put(TIME_SIG_LOWER, 4);
        }};
        HashMap<String, String> myConfigStrings = new HashMap<String, String>() {{
            put(ID, CONFIG_ID);
        }};

        PFSeqConfig exampleConfig = new PFSeqConfig(myConfigInts, myConfigBools, null, myConfigStrings);

        boolean seqSetupSuccess = seq.setUpSequencer(exampleConfig);

        if (!seqSetupSuccess) {
            seq.sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ALERT, "failed to set Up Sequencer"));
            return false;
        }
        return true;
    }

    private boolean setUpTracks(PFSeqRunnable seq) {
        File audFile;
        try {
            audFile = File.createTempFile("demo_app_file", "");
            InputStream ins = getResources().openRawResource(R.raw.kick);
            OutputStream out = new FileOutputStream(audFile);

            byte[] buffer = new byte[1024];
            int read;
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            seq.sendMessageToActivity(new PFSeqMessage(MESSAGE_TYPE_ERROR, "error creating file object \n" + e.getStackTrace().toString()));
            return false;
        }
        PFSeqTrack metronomeTrack = new PFSeqTrack(seq, "metronome");
        PFSeqClip clip = new PFSeqClip(seq, audFile);

        PFSeqTimeOffset timeOffset;

        timeOffset = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 0,false, 0);
        PFSeqPianoRollItem item1 = new PFSeqPianoRollItem(seq, clip, "item 1", timeOffset);
        metronomeTrack.addPianoRollItem(item1);

        seq.addTrack(metronomeTrack);

        return true;
    }
}