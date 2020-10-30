package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import smallville7123.vstmanager.core.R;

public class WindowView extends RelativeLayout {

    public WindowView(Context context) {
        super(context);
        init(context, null);
    }

    public WindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public WindowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private static class Internal {}
    Internal Internal = new Internal();

    void getAttributeParameters(Context context, AttributeSet attrs, Resources.Theme theme) {
        if (attrs != null) {
//            TypedArray attributes = theme.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout, 0, 0);
//            text = attributes.getString(R.styleable.ExpandableLayout_android_text);
//            textSize = LayoutUtils.getTextSizeAttributesSuitableForTextView(attributes, R.styleable.ExpandableLayout_android_textSize, 30f);
//            textColor = attributes.getColor(R.styleable.ExpandableLayout_android_textColor, Color.WHITE);
//            expanded = attributes.getBoolean(R.styleable.ExpandableLayout_android_state_expanded, false);
//            background = attributes.getDrawable(R.styleable.ExpandableLayout_android_background);
//            chevronColor = attributes.getColor(R.styleable.ExpandableLayout_chevron_color, -1);
//            attributes.recycle();
        }
    }

    FrameLayout root = null;
    FrameLayout frame = null;
    FrameLayout content = null;

    private void init(Context context, AttributeSet attrs) {
        Resources.Theme theme = context.getTheme();
        root = (FrameLayout) inflate(context, R.layout.window, null);
        root.setTag(Internal);
        frame = root.findViewById(R.id.window_frame);
        content = root.findViewById(R.id.window_content);
        addView(root, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private static final String TAG = "WindowView";

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Object tag = child.getTag();
        if (tag instanceof Internal) {
            Log.d(TAG, "addView() called with INTERNAL: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            super.addView(child, index, params);
        } else {
            Log.d(TAG, "addView() called with EXTERNAL: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
//            child.setOnTouchListener(new OnDragTouchListener(child, content));
            content.addView(child, index, params);
        }
    }
}