package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Random;

import smallville7123.liboverview.Overview;

public class VstView extends RelativeLayout {
    private static final String TAG = "VstView";
    Context mContext;
    int defaultWindowWidth;
    int defaultWindowHeight;
    int getDefaultWindowWidthDP = 200;
    int getDefaultWindowHeightDP = 200;
    Overview overview = null;
    boolean overviewShown = false;

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

    void init(Context context, AttributeSet attrs) {
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
        setOnClickListener(v -> {
            Log.d(TAG, "VSTVIEW onClick() called with: v = [" + v + "]");
            showOverview();
        });

        overview = new Overview(mContext);
        overview.setTag(Internal);
        overview.setRows(2);
        overview.setColumns(2);
        overview.setBackgroundColor(Color.argb(168, 128,128,128));
        overview.setOnClickListener(v -> {
            Log.d(TAG, "OVERVIEW onClick() called with: v = [" + v + "]");
            hideOverview();
        });

        hideOverview();
        addView(overview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void showOverview() {
        if (overview != null) {
            int childCount = getChildCount();
            if (childCount != 0) {
                for (int count = childCount; count > -1; count--) {
                    View child = getChildAt(count);
                    if (child instanceof WindowView) {
                        overview.addItem(
                                ViewCompositor.composite(
                                        ((WindowView) child).window_content
                                )
                        );
                    }
                }
            }
            overview.bringToFront();
            overview.setVisibility(VISIBLE);
            overviewShown = true;
        }
    }

    void hideOverview() {
        if (overview != null) {
            overview.clear();
            overview.setVisibility(GONE);
            overviewShown = false;
        }
    }

    boolean childHasBeenBroughtToFront = false;
    WindowView currentTop = null;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (overviewShown) return false;
        // scan children and make each clickable
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

    static class Internal {}
    static Internal Internal = new Internal();

    Random xGen = new Random();
    Random yGen = new Random();

    boolean randomizeChildren = true;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout() called with: changed = [" + changed + "], l = [" + l + "], t = [" + t + "], r = [" + r + "], b = [" + b + "]");
        super.onLayout(changed, l, t, r, b);
        if (randomizeChildren) {
            int childCount = getChildCount();
            if (childCount != 0) {
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child instanceof WindowView) {
                        WindowView window = ((WindowView) child);
                        if (!window.randomized) {
                            int maxX = (int) ((r+window.offsetRight) - (window.getWidth()-window.offsetRight));
                            int maxY = (int) ((b+window.offsetBottom) - (window.getHeight()-window.offsetBottom));
                            int x = xGen.nextInt(maxX);
                            int y = yGen.nextInt(maxY);
                            window.setX(x-window.offsetLeft);
                            window.setY(y-window.offsetTop);
                            window.randomized = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child.getTag() == Internal) super.addView(child, index, params);
        else if (child instanceof WindowView) {
            Log.d(TAG, "addView() called with WINDOW: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            WindowView window = (WindowView) child;
            window.setDrag(this);
            super.addView(window, index, params);
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