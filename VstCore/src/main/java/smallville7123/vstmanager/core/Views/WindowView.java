package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import smallville7123.taggable.Taggable;
import smallville7123.vstmanager.core.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class WindowView extends FrameLayout {

    private OnDragTouchListener draggable;
    private TextView title;

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
    public float touchZoneWidthLeft = 80.0f;
    public float touchZoneWidthRight = 80.0f;
    public float touchZoneHeightTop = 80.0f;
    public float touchZoneHeightBottom = 80.0f;
    public float touchZoneWidth;
    public float touchZoneHeight;
    private float titlebarOffset;
    public int titlebarHeight = 170;

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
        drawBorders(canvas, width, height, rp);
//        drawTouchZones(canvas, width, height, touchZonePaint);
    }

    void drawHighlight(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(offsetLeft, offsetTop, width-offsetRight, height-offsetBottom, paint);
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
        Resources.Theme theme = context.getTheme();
        FrameLayout root = (FrameLayout) inflate(context, R.layout.window, null);
        setBackgroundColor(Color.TRANSPARENT);
        root.setTag(Internal);

        touchZoneWidth = touchZoneWidthLeft+touchZoneWidthRight;
        touchZoneHeight = touchZoneHeightTop+touchZoneHeightBottom;

        LinearLayout titleBar = new LinearLayout(context);
        titleBar.setTag(Internal);
        titleBar.setBackgroundColor(Color.BLUE);
        LinearLayout.LayoutParams titleBarLayout = new LinearLayout.LayoutParams(MATCH_PARENT, titlebarHeight);
        titleBarLayout.leftMargin = (int) (touchZoneWidthLeft);
        titleBarLayout.rightMargin = (int) (touchZoneWidth+touchZoneWidthRight);

        ViewGroup.LayoutParams titleLayout = new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        title = new TextView(context);
        title.setText("Window Title");
        titleBar.addView(title, titleLayout);
        title.setTextSize(20.0f);

        content = new FrameLayout(context);
        content.setTag(Internal);
        content.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams contentLayout = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        contentLayout.bottomMargin = (int) (touchZoneHeightBottom);
        contentLayout.leftMargin = (int) (touchZoneWidthLeft);
        contentLayout.rightMargin = (int) (touchZoneWidth+touchZoneWidthRight);

        LinearLayout ll = root.findViewById(R.id.window_sub_frame);
        ll.addView(titleBar, titleBarLayout);
        ll.addView(content, contentLayout);

        titlebarOffset = touchZoneHeightTop;
        titleBar.setY(titlebarOffset);

        marginTop = (int) (titlebarOffset+titlebarHeight);
        marginLeft = (int) titlebarHeight;


        offsetTop = touchZoneHeightTop-heightTop;
        offsetBottom = touchZoneHeightBottom-heightBottom;
        offsetLeft = touchZoneWidthLeft-widthLeft;
        offsetRight = touchZoneWidthRight-widthRight;

        setX(-offsetLeft);
        setY(-offsetTop);

        addView(root, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

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