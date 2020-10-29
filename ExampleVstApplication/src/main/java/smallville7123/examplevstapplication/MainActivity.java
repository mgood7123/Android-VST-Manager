package smallville7123.examplevstapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import smallville7123.vst.apple;
import smallville7123.vstmanager.core.ReflectionActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    ReflectionActivity reflectionActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        Log.i(TAG, "onCreate: creating new reflection activity");
        reflectionActivity = new ReflectionActivity(getPackageName(), this, apple.class, root);
        Log.i(TAG, "onCreate: created new reflection activity");
        Log.i(TAG, "onCreate: setting content view");
        setContentView(root, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        Log.i(TAG, "onCreate: set content view");
    }
}