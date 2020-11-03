package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        init(context, null);
    }

    public VstView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public VstView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    Bitmap bm = null;
    ImageView background = null;

    void init(Context context, AttributeSet attrs) {
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
        setOnLongClickListener(v -> {
            Log.d(TAG, "onLongClick() called with: v = [" + v + "]");
            // last index is child at front
            if (bm != null) bm.recycle();
            View child = ((ViewGroup) getParent()).getChildAt(0);
            if (child instanceof ImageView) background = (ImageView) child;
            if (background != null) {
                ViewRenderer.getBitmapFromView(this, background);
            }
//            View child = getChildAt(getChildCount()-1);
//            if (child instanceof WindowView) {
//                bm = ((WindowView) child).getBitmapFromView(background);
//            }
            return false;
        });
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

    static class Internal {};
    static Internal Internal = new Internal();

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child.getTag() == Internal) super.addView(child, index, params);
        else if (child instanceof WindowView) {
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