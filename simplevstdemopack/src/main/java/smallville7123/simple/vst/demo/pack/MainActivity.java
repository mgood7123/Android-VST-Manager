package smallville7123.simple.vst.demo.pack;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    Sequencer app = new Sequencer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = new FrameLayout(this);
        ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(frameLayout, matchParent);

        app.setup(frameLayout, this);
        app.onCreate(savedInstanceState);
    }
}