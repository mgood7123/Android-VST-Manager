package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class VstView extends RelativeLayout {
    private static final String TAG = "VstView";
    public VstView(Context context) {
        super(context);
    }

    public VstView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VstView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Log.d(TAG, "addView() called with: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
        child.setOnTouchListener(new OnDragTouchListener(child, this));
            super.addView(child, index, params);
    }
}