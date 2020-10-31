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
        if (child instanceof WindowView) {
            Log.d(TAG, "addView() called with: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            WindowView x = (WindowView) child;
            x.setDrag(this);
            super.addView(x, index, params);
        } else {
            throw new RuntimeException("VstView can only accept view's of type WindowView");
        }
    }
}