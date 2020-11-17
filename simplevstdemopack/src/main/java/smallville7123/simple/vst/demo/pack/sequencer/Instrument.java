package smallville7123.simple.vst.demo.pack.sequencer;

import android.widget.ToggleButton;

import java.util.ArrayList;

public class Instrument {

    public ToggleButton[] buttons;
    public String instrumentName;
    public int sound;

    public Instrument(ToggleButton[] buttons, String instrumentName) {
        this.buttons = buttons;
        this.instrumentName = instrumentName;
    }

    public Instrument(ToggleButton[] buttons, String instrumentName, int sound) {
        this.buttons = buttons;
        this.instrumentName = instrumentName;
        this.sound = sound;
    }

    public Instrument(ArrayList<ToggleButton> buttons, String instrumentName, int sound) {
        this(buttons.toArray(new ToggleButton[0]), instrumentName, sound);
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setButtons(ToggleButton[] buttons) {
        this.buttons = buttons;
    }

    public void setSound(int sound) {
        this.sound = sound;
    }

    public String getInstrumentName() {
        return this.instrumentName;
    }

    public ToggleButton[] getButtons() {
        return this.buttons;
    }

    public int getSound(){
        return this.sound;
    }

    public ToggleButton getButtonAt(int i) {
        return this.buttons[i];
    }

}
