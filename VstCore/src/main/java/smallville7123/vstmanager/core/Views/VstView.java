package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    // see https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the

    OverviewGrid overview = null;
    boolean overviewShown = false;

    void addItem(LinearLayout row) {
        TextView item = new TextView(mContext);
        item.setTextSize(30.0f);
        item.setText("Hello!");
        row.addView(item);
    }

    void init(Context context, AttributeSet attrs) {
        mContext = context;
        defaultWindowWidth = toDP(getResources(), getDefaultWindowWidthDP);
        defaultWindowHeight = toDP(getResources(), getDefaultWindowHeightDP);
        setOnClickListener(v -> {
            Log.d(TAG, "VSTVIEW onClick() called with: v = [" + v + "]");
            showOverview();
        });

        overview = new OverviewGrid(mContext);
        overview.setTag(Internal);
        overview.setRows(2);
        overview.setColumns(2);
        overview.setPlaceholder(new OverviewGrid.PlaceholderGenerator() {
            @Override
            public View generate() {
                Button b = new Button(mContext);
                b.setText("Button");
                return b;
            }
        });
        overview.setBackgroundColor(Color.rgb(128,128,128));

        overview.setOnClickListener(v -> {
            Log.d(TAG, "OVERVIEW onClick() called with: v = [" + v + "]");
            hideOverview();
        });

        hideOverview();
        addView(overview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void showOverview() {
        if (overview != null) {
            overview.bringToFront();
            overview.setVisibility(VISIBLE);
            overviewShown = true;
        }
    }

    void hideOverview() {
        if (overview != null) {
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

    void drawBitmap() {
        if (background == null) {
            View child = ((ViewGroup) getParent()).getChildAt(0);
            if (child instanceof ImageView) background = (ImageView) child;
        }
        if (bm != null) bm.recycle();
        bm = ViewCompositor.composite(this, background);
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