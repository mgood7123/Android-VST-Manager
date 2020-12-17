package smallville7123.simple.vst.demo.pack.sequencer;

import android.os.Bundle;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.DrawableRes;

import java.util.ArrayList;

import smallville7123.simple.vst.demo.pack.R;
import smallville7123.vstmanager.core.VstActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SequencerApp extends VstActivity {
    private static final String TAG = "Sequencer";

    Sequencer sequencer;
    AudioDeck audioDeck;
    int sound1;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initSequencer();
    }

    private void initLayout() {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.sequencer, null, false);
        playback = linearLayout.findViewById(R.id.playbackButton);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, matchParent);
        LinearLayout sequencer = linearLayout.findViewById(R.id.sequenceLayout);
        addRow(sequencer);
    }

    private void initSequencer() {
        playback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                play();
            } else {
                pause();
            }
        });

        audioDeck = new AudioDeck(this);

        sound1 = audioDeck.load(R.raw.kick, "wav");
//        audioDeck.getTrack(sound1).loadIntoMemory();

        Instrument [] instruments = {
                new Instrument(rows.get(0).second, "kick", sound1)
        };

        sequencer = new Sequencer(audioDeck, instruments);

        sequencer.setIsPlaying(false);
        new Thread(sequencer).start();
    }

    void play() {
        sequencer.setIsPlaying(true);
    }

    void pause() {
        sequencer.setIsPlaying(false);
    }

    ToggleButton playback;
}
