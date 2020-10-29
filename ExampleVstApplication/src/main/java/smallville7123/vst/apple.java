package smallville7123.vst;

import android.content.Context;

import smallville7123.vstmanager.core.EventThread;
import smallville7123.vstmanager.core.VstActivity;

//public class VstActivity extends ReflectionActivity implements VstCallback {
//    private static final String TAG = "VstActivity";
//}

public class apple extends VstActivity {
    // vst implementation

    @Override
    public void setEventThread(String packageName, Context context, EventThread eventThread) {
        super.setEventThread(packageName, context, eventThread);
    }

    private static final String TAG = "Apple";


}