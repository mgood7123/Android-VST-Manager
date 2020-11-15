package smallville7123.simple.vst.demo.pack;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.DrawableRes;

import smallville7123.vstmanager.core.VstActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class Sequencer extends VstActivity {
    static ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);

    void addToggleButton(LinearLayout row, @DrawableRes int id, LinearLayout.LayoutParams params) {
        ToggleButton button = new ToggleButton(this);
        button.setBackgroundResource(id);
        button.setTextOn("");
        button.setTextOff("");
        button.setText("");
        row.addView(button, params);
    }

    void addToggleButton(LinearLayout row, @DrawableRes int id) {
        addToggleButton(row, id, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
    }

    void addRow(LinearLayout linearLayout) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        for (int i = 0; i < 8; i++) addToggleButton(row, R.drawable.toggle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.sequencer, null, false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, matchParent);
        LinearLayout sequencer = linearLayout.findViewById(R.id.sequenceLayout);
        addRow(sequencer);
        addRow(sequencer);
        addRow(sequencer);
        addRow(sequencer);
    }
}
