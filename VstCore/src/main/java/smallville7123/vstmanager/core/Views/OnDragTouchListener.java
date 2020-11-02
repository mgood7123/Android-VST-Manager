package smallville7123.vstmanager.core.Views;

import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;

public class OnDragTouchListener {

    private static final String TAG = "OnDragTouchListener";

    private static final float CLICK_DRAG_TOLERANCE = 30.0f;
    public float widthLeft = 20.0f;
    public float widthRight = 20.0f;
    public float heightTop = 20.0f;
    public float heightBottom = 20.0f;
    public int marginTop;
    public int marginLeft;
    public float offsetLeft;
    public float offsetRight;
    public float offsetTop;
    public float offsetBottom;
    public boolean onlyDragWithinWidthAndHeightRegions;
    private float relativeToViewX;
    private float relativeToViewY;
    public boolean resizing = false;
    private RectF leftBounds = new RectF();
    public boolean corner = false;

    boolean resizingTop = false;
    boolean resizingTopLeft = false;
    boolean resizingTopRight = false;
    boolean resizingBottom = false;
    boolean resizingBottomLeft = false;
    boolean resizingBottomRight = false;
    boolean resizingLeft = false;
    boolean resizingRight = false;
    private int originalRight;
    private int originalBottom;
    private int downWidth;
    private int downHeight;
    private int minWidth;
    private int minHeight;
    private float minX;
    private float minY;

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

    public OnDragTouchListener(View view, OnDragActionListener onDragActionListener) {
        this(view, (View) view.getParent(), onDragActionListener);
    }

    public OnDragTouchListener(View view, View parent) {
        this(view, parent, null);
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

    @CallSuper
    public void updateOtherBounds(ViewGroup.LayoutParams layoutParams) {
        updateOtherBounds(layoutParams.width, layoutParams.height);
    }

    @CallSuper
    public void updateOtherBounds(int width, int height) {

    }

    public boolean onTouch(MotionEvent event) {
        Log.d(TAG, "MotionEvent.actionToString(event.getAction()) = [" + MotionEvent.actionToString(event.getAction()) + "]");
        float currentRawX = event.getRawX();
        float currentRawY = event.getRawY();
        relativeToViewX = event.getX();
        relativeToViewY = event.getY();
        if (isDragging) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (resizing) {
                        resizing = false;
                        mView.invalidate();
                    }
                    upRawX = currentRawX;
                    upRawY = currentRawY;
                    upDX = upRawX - downRawX;
                    upDY = upRawY - downRawY;
                    if ((Math.abs(upDX) < CLICK_DRAG_TOLERANCE) && (Math.abs(upDY) < CLICK_DRAG_TOLERANCE)) {
                        mView.animate().x(originalX).y(originalY).setDuration(0).start();
                    }
                    onDragFinish();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!resizing) {
                        float[] bounds = new float[4];
                        // LEFT
                        bounds[0] = currentRawX + downDX;
                        if (bounds[0] < maxLeft-offsetLeft) {
                            bounds[0] = maxLeft-offsetLeft;
                        }
                        // RIGHT
                        bounds[2] = bounds[0] + originalRight;
                        if (bounds[2] > maxRight+offsetRight) {
                            bounds[2] = maxRight+offsetRight;
                            bounds[0] = (maxRight+offsetRight) - originalRight;
                        }
                        // TOP
                        bounds[1] = currentRawY + downDY;
                        if (bounds[1] < maxTop-offsetTop) {
                            bounds[1] = maxTop-offsetTop;
                        }
                        // BOTTOM
                        bounds[3] = bounds[1] + originalBottom;
                        if (bounds[3] > maxBottom+offsetBottom) {
                            bounds[3] = maxBottom+offsetBottom;
                            bounds[1] = (maxBottom+offsetBottom) - originalBottom;
                        }
                        mView.animate().x(bounds[0]).y(bounds[1]).setDuration(0).start();
                    } else {
                        ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
                        if (resizingLeft) {
                            resizeLeft(currentRawX, layoutParams);
                        } else if (resizingRight) {
                            resizeRight(currentRawX, layoutParams);
                        } else if (resizingTop) {
                            resizeTop(currentRawY, layoutParams);
                        } else if (resizingBottom) {
                            resizeBottom(currentRawY, layoutParams);
                        } else if (resizingTopRight) {
                            resizeTop(currentRawY, layoutParams);
                            resizeRight(currentRawX, layoutParams);
                        } else if (resizingTopLeft) {
                            resizeTop(currentRawY, layoutParams);
                            resizeLeft(currentRawX, layoutParams);
                        } else if (resizingBottomRight) {
                            resizeBottom(currentRawY, layoutParams);
                            resizeRight(currentRawX, layoutParams);
                        } else if (resizingBottomLeft) {
                            resizeBottom(currentRawY, layoutParams);
                            resizeLeft(currentRawX, layoutParams);
                        }
                        width = mView.getWidth();
                        height = mView.getHeight();
                    }
                    break;
            }
            return true;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    originalRight = mView.getRight();
                    originalBottom = mView.getBottom();
                    originalX = mView.getX();
                    originalY = mView.getY();
                    ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
                    downWidth = layoutParams.width;
                    downHeight = layoutParams.height;
                    RectF r = new RectF(
                            marginLeft,
                            marginTop,
                            downWidth+widthRight,
                            downHeight+heightBottom
                    );
                    boolean c = r.contains(
                            relativeToViewX,
                            relativeToViewY
                    );

                    if (c) return false;
                    downRawX = currentRawX;
                    downRawY = currentRawY;
                    minHeight = 100 + marginTop;
                    minWidth = 100 + marginLeft;
                    corner = false;
                    resizingTop = false;
                    resizingTopLeft = false;
                    resizingTopRight = false;
                    resizingBottom = false;
                    resizingBottomLeft = false;
                    resizingBottomRight = false;
                    resizingLeft = false;
                    resizingRight = false;
                    minX = originalX + (originalRight-widthRight-minWidth-widthLeft);
                    minY = originalY + (originalBottom-heightTop-minHeight-heightTop);
                    if (relativeToViewX < widthLeft) {
                        if (relativeToViewY < heightTop) {
                            resizingTopLeft = true;
                            corner = true;
                        } else if ((mView.getBottom()-relativeToViewY) < heightBottom) {
                            resizingBottomLeft = true;
                            corner = true;
                        } else {
                            resizingLeft = true;
                        }
                        resizing = true;
                        mView.invalidate();
                    } else if ((mView.getRight()-relativeToViewX) < widthRight) {
                        if (relativeToViewY < heightTop) {
                            resizingTopRight = true;
                            corner = true;
                        } else if ((mView.getBottom()-relativeToViewY) < heightBottom) {
                            resizingBottomRight = true;
                            corner = true;
                        } else {
                            resizingRight = true;
                        }
                        resizing = true;
                        mView.invalidate();
                    } else if (relativeToViewY < heightTop) {
                        resizingTop = true;
                        resizing = true;
                        mView.invalidate();
                    } else if ((mView.getBottom()-relativeToViewY) < heightBottom) {
                        resizingBottom = true;
                        resizing = true;
                        mView.invalidate();
                    }
                    isDragging = true;
                    if (!isInitialized) {
                        updateBounds();
                    }
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

    private void resizeLeft(float currentRawX, ViewGroup.LayoutParams layoutParams) {
        float bounds = currentRawX + downDX;
        if (bounds < maxLeft-offsetLeft) bounds = maxLeft-offsetLeft;
        if (layoutParams.width > minWidth) {
            mView.animate().x(bounds).setDuration(0).start();
        }
        if (bounds > maxLeft-offsetLeft) {
            layoutParams.width = (int) (downWidth + (downRawX - currentRawX));
            if (layoutParams.width < minWidth) {
                layoutParams.width = minWidth;
                mView.animate().x(minX).setDuration(0).start();
            }
            updateOtherBounds(layoutParams);
            mView.setLayoutParams(layoutParams);
        }
    }

    private void resizeTop(float currentRawY, ViewGroup.LayoutParams layoutParams) {
        float bounds = currentRawY + downDY;
        if (bounds < (maxTop-offsetTop)) bounds = maxTop-offsetTop;
        if (layoutParams.height > minHeight) {
            mView.animate().y(bounds).setDuration(0).start();
        }
        if (bounds > maxTop-offsetTop) {
            layoutParams.height = (int) (downHeight + (downRawY - currentRawY));
            if (layoutParams.height < minHeight) {
                layoutParams.height = minHeight;
                mView.animate().y(minY).setDuration(0).start();
            }
            updateOtherBounds(layoutParams);
            mView.setLayoutParams(layoutParams);
        }
    }

    private void resizeRight(float currentRawX, ViewGroup.LayoutParams layoutParams) {
        layoutParams.width = (int) (downWidth + (currentRawX-downRawX));
        if (layoutParams.width < minWidth) {
            layoutParams.width = minWidth;
        } else {
            if ((originalX + widthLeft + layoutParams.width + widthRight) > maxRight+offsetRight) {
                layoutParams.width = (int) ((maxRight+offsetRight) - widthRight - (originalX+widthLeft));
            }
        }
        updateOtherBounds(layoutParams);
        mView.setLayoutParams(layoutParams);
    }

    private void resizeBottom(float currentRawY, ViewGroup.LayoutParams layoutParams) {
        layoutParams.height = (int) (downHeight + (currentRawY-downRawY));
        if (layoutParams.height < minHeight) {
            layoutParams.height = minHeight;
        } else {
            if ((originalY + heightTop + layoutParams.height + heightBottom) > maxBottom+offsetBottom) {
                layoutParams.height = (int) ((maxBottom+offsetBottom) - heightBottom - (originalY+heightTop));
            }
        }
        updateOtherBounds(layoutParams);
        mView.setLayoutParams(layoutParams);
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