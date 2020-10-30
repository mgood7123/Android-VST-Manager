package smallville7123.vstmanager.core.Views;

import android.view.MotionEvent;
import android.view.View;

public class OnDragTouchListener implements View.OnTouchListener {


    private static final float CLICK_DRAG_TOLERANCE = 30.0f;

    /**
     * Callback used to indicate when the drag is finished
     */
    public interface OnDragActionListener {
        /**
         * Called when drag event is started
         *
         * @param view The view dragged
         */
        void onDragStart(View view);

        /**
         * Called when drag event is completed
         *
         * @param view The view dragged
         */
        void onDragEnd(View view);
    }

    private View mView;
    private View mParent;
    private boolean isDragging;
    private boolean isInitialized = false;

    private int width;
    private float xWhenAttached;
    private float maxLeft;
    private float maxRight;

    private int height;
    private float yWhenAttached;
    private float maxTop;
    private float maxBottom;

    public float downDX, downDY, newX, newY;
    public float downX, downRawX, downY, downRawY, upX, upRawX, upY, upRawY, upDX, upDY;
    float originalX;
    float originalY;

    private OnDragActionListener mOnDragActionListener;

    public OnDragTouchListener(View view) {
        this(view, (View) view.getParent(), null);
    }

    public OnDragTouchListener(View view, View parent) {
        this(view, parent, null);
    }

    public OnDragTouchListener(View view, OnDragActionListener onDragActionListener) {
        this(view, (View) view.getParent(), onDragActionListener);
    }

    public OnDragTouchListener(View view, View parent, OnDragActionListener onDragActionListener) {
        initListener(view, parent);
        setOnDragActionListener(onDragActionListener);
    }

    public void setOnDragActionListener(OnDragActionListener onDragActionListener) {
        mOnDragActionListener = onDragActionListener;
    }

    public void initListener(View view, View parent) {
        mView = view;
        mParent = parent;
        isDragging = false;
        isInitialized = false;
    }

    public void updateBounds() {
        updateViewBounds();
        updateParentBounds();
        isInitialized = true;
    }

    public void updateViewBounds() {
        width = mView.getWidth();
        xWhenAttached = mView.getX();
        downDX = 0;

        height = mView.getHeight();
        yWhenAttached = mView.getY();
        downDY = 0;
    }

    public void updateParentBounds() {
        maxLeft = 0;
        maxRight = maxLeft + mParent.getWidth();

        maxTop = 0;
        maxBottom = maxTop + mParent.getHeight();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isDragging) {
            float[] bounds = new float[4];
            // LEFT
            bounds[0] = event.getRawX() + downDX;
            if (bounds[0] < maxLeft) {
                bounds[0] = maxLeft;
            }
            // RIGHT
            bounds[2] = bounds[0] + width;
            if (bounds[2] > maxRight) {
                bounds[2] = maxRight;
                bounds[0] = bounds[2] - width;
            }
            // TOP
            bounds[1] = event.getRawY() + downDY;
            if (bounds[1] < maxTop) {
                bounds[1] = maxTop;
            }
            // BOTTOM
            bounds[3] = bounds[1] + height;
            if (bounds[3] > maxBottom) {
                bounds[3] = maxBottom;
                bounds[1] = bounds[3] - height;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    upRawX = event.getRawX();
                    upRawY = event.getRawY();
                    upDX = upRawX - downRawX;
                    upDY = upRawY - downRawY;
                    if ((Math.abs(upDX) < CLICK_DRAG_TOLERANCE) && (Math.abs(upDY) < CLICK_DRAG_TOLERANCE)) {
                        mView.animate().x(originalX).y(originalY).setDuration(0).start();
                    }
                    onDragFinish();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mView.animate().x(bounds[0]).y(bounds[1]).setDuration(0).start();
                    break;
            }
            return true;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDragging = true;
                    if (!isInitialized) {
                        updateBounds();
                    }
                    originalX = v.getX();
                    originalY = v.getY();
                    downRawX = event.getRawX();
                    downRawY = event.getRawY();
                    downDX = originalX - downRawX;
                    downDY = originalY - downRawY;
                    if (mOnDragActionListener != null) {
                        mOnDragActionListener.onDragStart(mView);
                    }
                    return true;
            }
        }
        return false;
    }

    private void onDragFinish() {
        if (mOnDragActionListener != null) {
            mOnDragActionListener.onDragEnd(mView);
        }

        downDX = 0;
        downDY = 0;
        isDragging = false;
    }
}