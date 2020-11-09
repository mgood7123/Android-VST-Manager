package smallville7123.vstmanager.core.Views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import smallville7123.vstmanager.core.HandlerThread;

import static android.view.PixelCopy.ERROR_DESTINATION_INVALID;
import static android.view.PixelCopy.ERROR_SOURCE_INVALID;
import static android.view.PixelCopy.ERROR_SOURCE_NO_DATA;
import static android.view.PixelCopy.ERROR_TIMEOUT;
import static android.view.PixelCopy.ERROR_UNKNOWN;
import static android.view.PixelCopy.SUCCESS;

/**
 * composites a viewGroup to a bitmap
 */
public class ViewCompositor {
    private static final String TAG = "ViewCompositor";

    // render back to front

    private static void compositeInternalViewGroup(ViewGroup view, Canvas canvas) {
        int childCount = view.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = view.getChildAt(i);
            if (child instanceof WindowView) {
                ViewHierarchy viewHierarchy = new ViewHierarchy();
                viewHierarchy.analyze(child);
                ArrayList<ViewHierarchy> sorted = viewHierarchy.sortByDepth();
                for (ViewHierarchy hierarchy : sorted) compositeInternalView(hierarchy.view, canvas);
            } else compositeInternalView(child, canvas);
        }
    }

    private static void compositeInternalView(View view, Canvas canvas) {
        if (view != null) {
            int w = view.getWidth();
            int h = view.getHeight();
            if (w <= 0 || h <= 0) return;
            if (view instanceof TextureView) {
                Bitmap bitmap = ((TextureView) view).getBitmap();
                canvas.drawBitmap(bitmap, view.getX(), view.getY(), null);
                bitmap.recycle();
            } else {
                Bitmap bitmap = Bitmap.createBitmap(
                        w,
                        h,
                        Bitmap.Config.ARGB_8888
                );
                if (view instanceof SurfaceView) {
                    SurfaceView sv = (SurfaceView) view;
                    AtomicBoolean completed = new AtomicBoolean(false);
                    HandlerThread handle = new HandlerThread("Handler");
                    Handler handler = handle.getThreadHandler();
                    while(true) {
                        PixelCopy.request(sv.getHolder().getSurface(), null, bitmap, copyResult -> {
                            if (completed.get()) return;
                            if (copyResult == PixelCopy.SUCCESS) {
                                canvas.drawBitmap(bitmap, sv.getX(), sv.getY(), null);
                                bitmap.recycle();
                                completed.set(true);
                            } else {
                                Log.d(TAG, "compositeInternalView: pixel copy failed: " + pixelCopyResultToString(copyResult));
                            }
                        }, handler);
                        handle.waitForCurrentPost();
                        if (completed.get()) break;
                    }
                } else {
                    Canvas childCanvas = new Canvas(bitmap);
                    view.draw(childCanvas);
                    canvas.drawBitmap(bitmap, view.getX(), view.getY(), null);
                    bitmap.recycle();
                }
            }
        }
    }

    private static String pixelCopyResultToString(int copyResult) {
        switch (copyResult) {
            case ERROR_DESTINATION_INVALID:
                return "ERROR_DESTINATION_INVALID";
            case ERROR_SOURCE_INVALID:
                return "ERROR_SOURCE_INVALID";
            case ERROR_SOURCE_NO_DATA:
                return "ERROR_SOURCE_NO_DATA";
            case ERROR_TIMEOUT:
                return "ERROR_TIMEOUT";
            case ERROR_UNKNOWN:
                return "ERROR_UNKNOWN";
            case SUCCESS:
                return "SUCCESS";
            default:
                return "UNKNOWN";
        }
    }


    public static Bitmap composite(View view, ImageView background) {
        Log.d(TAG, "composite() called with: view = [" + view + "], background = [" + background + "]");
        if (view == null || background == null) return null;
        int w = view.getWidth();
        int h = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (view instanceof ViewGroup) compositeInternalViewGroup((ViewGroup) view, canvas);
        else compositeInternalView(view, canvas);
        background.setImageBitmap(bitmap);
        return bitmap;
    }
}
