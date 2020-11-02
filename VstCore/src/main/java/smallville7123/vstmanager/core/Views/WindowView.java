package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import smallville7123.taggable.Taggable;
import smallville7123.vstmanager.core.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WindowView extends FrameLayout {

    private OnDragTouchListener draggable;
    private Context mContext;
    private Rect region = new Rect();

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

    public float offsetLeft;
    public float offsetRight;
    public float offsetTop;
    public float offsetBottom;
    public int marginTop;
    public int marginLeft;


    public void setDrag(VstView parent) {
        draggable = new OnDragTouchListener(this, parent);
        draggable.widthLeft = touchZoneWidthLeft;
        draggable.widthRight = touchZoneWidthRight;
        draggable.heightTop = touchZoneHeightTop;
        draggable.heightBottom = touchZoneHeightBottom;
        draggable.marginTop = marginTop;
        draggable.marginLeft = marginLeft;

        draggable.offsetTop = offsetTop;
        draggable.offsetBottom = offsetBottom;
        draggable.offsetLeft = offsetLeft;
        draggable.offsetRight = offsetRight;
        draggable.onlyDragWithinWidthAndHeightRegions = true;
    }

    private static class Internal {}
    Internal Internal = new Internal();
    public float widthLeft = 10.0f;
    public float widthRight = 10.0f;
    public float heightTop = 10.0f;
    public float heightBottom = 10.0f;
    public float touchZoneWidthLeft = 100.0f;
    public float touchZoneWidthRight = 100.0f;
    public float touchZoneHeightTop = 100.0f;
    public float touchZoneHeightBottom = 100.0f;
    public float touchZoneWidth;
    public float touchZoneHeight;
    private float titlebarOffset;
    public float titlebarHeight = 100.0f;

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

    public String TAG = Taggable.getTag(this);

    Paint highlightPaint;
    Paint highlightCornerPaint;
    Paint touchZonePaint;
    Paint regionPaint;
    Paint titleBarPaint;


    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Paint rp = regionPaint;
        if (draggable != null) {
            if (draggable.resizing) {
                if (draggable.corner) {
                    rp = highlightCornerPaint;
                } else {
                    rp = highlightPaint;
                }
                drawHighlight(canvas, width, height, rp);
            }
        }
        drawTitleBar(canvas, width, height, titleBarPaint);
        drawBorders(canvas, width, height, rp);
//        drawTouchZones(canvas, width, height, touchZonePaint);
    }

    void drawHighlight(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(offsetLeft, offsetTop, width-offsetRight, height-offsetBottom, paint);
    }

    void drawTitleBar(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(touchZoneWidthLeft, titlebarOffset, width-touchZoneWidthRight, titlebarOffset+titlebarHeight, paint);
    }

    void drawBorders(Canvas canvas, int width, int height, Paint paint) {
        // left
        canvas.drawRect(touchZoneWidthLeft, touchZoneHeightTop, touchZoneWidthLeft-widthLeft, height-touchZoneHeightBottom, paint);
        // right
        canvas.drawRect(width - touchZoneWidthRight, touchZoneHeightTop, width-touchZoneWidthRight+widthRight, height-touchZoneHeightBottom, paint);
        // top
        canvas.drawRect(touchZoneWidthLeft-widthLeft, touchZoneHeightTop, width-touchZoneWidthRight+widthRight, touchZoneHeightTop-heightTop, paint);
        // bottom
        canvas.drawRect(touchZoneWidthLeft-widthLeft, height-touchZoneHeightBottom, width-touchZoneWidthRight+widthRight, height-touchZoneHeightBottom+heightBottom, paint);
    }

    void drawTouchZones(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(0, 0, touchZoneWidthLeft, height, paint);
        canvas.drawRect(width - touchZoneWidthRight, 0, width, height, paint);
        canvas.drawRect(0, 0, width, touchZoneHeightTop, paint);
        canvas.drawRect(0, height - touchZoneHeightBottom, width, height, paint);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        Resources.Theme theme = context.getTheme();
        root = (FrameLayout) inflate(context, R.layout.window, null);
        setBackgroundColor(Color.TRANSPARENT);
        root.setTag(Internal);
        frame = root.findViewById(R.id.window_frame);
        touchZoneWidth = touchZoneWidthLeft+touchZoneWidthRight;
        touchZoneHeight = touchZoneHeightTop+touchZoneHeightBottom;
        titlebarOffset = touchZoneHeightTop;
        marginTop = (int) (titlebarOffset+titlebarHeight);
        marginLeft = (int) titlebarHeight;
        windowContentLayout = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        windowContentLayout.topMargin = (int) (titlebarOffset+titlebarHeight);
        windowContentLayout.bottomMargin = (int) (touchZoneHeightBottom);
        windowContentLayout.leftMargin = (int) (touchZoneWidthLeft);
        windowContentLayout.rightMargin = (int) (touchZoneWidth+touchZoneWidthRight);

        offsetTop = touchZoneHeightTop-heightTop;
        offsetBottom = touchZoneHeightBottom-heightBottom;
        offsetLeft = touchZoneWidthLeft-widthLeft;
        offsetRight = touchZoneWidthRight-widthRight;
        setX(-offsetLeft);
        setY(-offsetTop);

        content = root.findViewById(R.id.window_content);
        content.setLayoutParams(windowContentLayout);

        addView(root, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        highlightPaint = new Paint();
        highlightCornerPaint = new Paint();
        regionPaint = new Paint();
        touchZonePaint = new Paint();
        titleBarPaint = new Paint();

        highlightPaint.setARGB(200, 0, 0, 255);
        highlightCornerPaint.setARGB(200, 255, 90, 0);
        touchZonePaint.setARGB(60, 0, 90, 0);
        regionPaint.setARGB(255, 168, 168, 168);
        titleBarPaint.setARGB(255, 0,0,255);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(widthMeasureSpec)+(int)(touchZoneWidth),
                        MeasureSpec.getMode(widthMeasureSpec)
                ),
                MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(heightMeasureSpec)+(int)(touchZoneHeight),
                        MeasureSpec.getMode(heightMeasureSpec)
                )
        );
    }

    boolean broughtToFront = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (broughtToFront) {
            // process input
            return false;
        }
        ViewParent parent = getParent();
        if (parent instanceof VstView) {
            VstView vstView = (VstView) parent;
            if (
                       vstView.currentTop == this
                    || vstView.currentTop == null
                    || !vstView.childHasBeenBroughtToFront
            ) {
                bringToFront();
                broughtToFront = true;
                vstView.childHasBeenBroughtToFront = true;
                vstView.currentTop = this;
                // process input
                return false;
            } else {
                // do not process input
                return true;
            }
        } else throw new RuntimeException("invalid parent");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return broughtToFront && draggable.onTouch(event);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Object tag = child.getTag();
        if (tag instanceof Internal) super.addView(child, index, params);
        else content.addView(child, -1, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }
}