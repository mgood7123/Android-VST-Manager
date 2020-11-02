package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class VstView extends RelativeLayout {
    private static final String TAG = "VstView";
    Context mContext;
    int defaultWindowWidth;
    int defaultWindowHeight;
    int getDefaultWindowWidthDP = 200;
    int getDefaultWindowHeightDP = 200;
    public VstView(Context context) {
        super(context);
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
    }

    public VstView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
    }

    public VstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
    }

    public VstView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
    }

    boolean childHasBeenBroughtToFront = false;
    WindowView currentTop = null;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // scan children and make each clickable
        // do fifo pattern
        // first child to be brought to front should make other children not respond
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            childHasBeenBroughtToFront = false;
            int childCount = getChildCount();
            if (childCount != 0) {
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child instanceof WindowView) {
                        ((WindowView) child).broughtToFront = false;
                    }
                }
            }
        }
        // process input
        return false;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof WindowView) {
            Log.d(TAG, "addView() called with WINDOW: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            WindowView x = (WindowView) child;
            x.setDrag(this);
            super.addView(x, index, params);
        } else {
            Log.d(TAG, "addView() called with NON WINDOW: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            // wrap view in WindowView
            WindowView window = new WindowView(mContext);
            window.setDrag(this);
            window.addView(child, params);
            super.addView(window, -1, new LayoutParams(defaultWindowWidth, defaultWindowHeight));
        }
    }

    public static int toDP(Resources resources, float val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, resources.getDisplayMetrics());
    }
}