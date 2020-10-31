package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import smallville7123.vstmanager.core.R;

public class WindowView extends RelativeLayout {

    private OnDragTouchListener draggable;

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

    public void setDrag(VstView parent) {
        draggable = new OnDragTouchListener(this, parent);
        draggable.widthLeft = widthLeft;
        draggable.widthRight = widthRight;
        draggable.heightTop = heightTop;
        draggable.heightBottom = heightBottom;
        setOnTouchListener(draggable);
    }

    private static class Internal {}
    Internal Internal = new Internal();
    public float widthLeft = 20.0f;
    public float widthRight = 20.0f;
    public float heightTop = 20.0f;
    public float heightBottom = 20.0f;
    public float titlebarHeight = 60.0f;

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

    FrameLayout.LayoutParams windowContentLayout;

    private static final String TAG = "WindowView";
    Paint highlightPaint;
    Paint highlightCornerPaint;
    Paint regionPaint;
    Paint titleBarPaint;


    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Paint rp = regionPaint;
        if (draggable.resizing) {
            if (draggable.corner) {
                rp = highlightCornerPaint;
            } else {
                rp = highlightPaint;
            }
            drawHighlight(canvas, width, height, rp);
        }
        drawTitleBar(canvas, width, height, titleBarPaint);
        drawBorders(canvas, width, height, rp);
    }

    void drawHighlight(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(0, 0, width, height, paint);
    }

    void drawTitleBar(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(0, 0, width, titlebarHeight, paint);
    }

    void drawBorders(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(0, 0, widthLeft, height, paint);
        canvas.drawRect(0, 0, width, heightTop, paint);
        canvas.drawRect(width - widthRight, 0, width, height, paint);
        canvas.drawRect(0, height - heightBottom, width, height, paint);
    }

    private void init(Context context, AttributeSet attrs) {
        Resources.Theme theme = context.getTheme();
        root = (FrameLayout) inflate(context, R.layout.window, null);
        root.setTag(Internal);
        frame = root.findViewById(R.id.window_frame);
        windowContentLayout = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        windowContentLayout.topMargin = (int) Math.max(heightTop, titlebarHeight);
        windowContentLayout.bottomMargin = (int) heightBottom;
        windowContentLayout.leftMargin = (int) widthLeft;
        windowContentLayout.rightMargin = (int) widthRight;

        content = root.findViewById(R.id.window_content);
        content.setLayoutParams(windowContentLayout);

        addView(root, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        highlightPaint = new Paint();
        highlightCornerPaint = new Paint();
        regionPaint = new Paint();
        titleBarPaint = new Paint();

        highlightPaint.setARGB(200, 0, 0, 255);
        highlightCornerPaint.setARGB(200, 255, 90, 0);
        regionPaint.setARGB(255, 168, 168, 168);
        titleBarPaint.setARGB(255, 0,0,255);
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Object tag = child.getTag();
        if (tag instanceof Internal) {
            Log.d(TAG, "addView() called with INTERNAL: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            super.addView(child, index, params);
        } else {
            Log.d(TAG, "addView() called with EXTERNAL: child = [" + child + "], index = [" + index + "], params = [" + params + "]");
            content.addView(child, index, params);
        }
    }
}