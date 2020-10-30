package smallville7123.vst;

import android.os.Bundle;
import android.util.Log;

import smallville7123.examplevstapplication.R;
import smallville7123.vstmanager.core.VstActivity;

//public class VstActivity extends ReflectionActivity implements VstCallback {
//    private static final String TAG = "VstActivity";
//}

public class apple extends VstActivity {
    // vst implementation
    private static final String TAG = "Apple";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: setting content view");
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: set content view");
    }
}