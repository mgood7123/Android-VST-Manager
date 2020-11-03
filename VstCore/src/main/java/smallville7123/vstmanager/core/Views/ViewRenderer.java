package smallville7123.vstmanager.core.Views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

/**
 * renders a view and its complete hierarchy to a bitmap
 */
public class ViewRenderer {
    public static Bitmap getBitmapFromView(View view, ImageView background) {
        if (view != null) {
            if (view instanceof TextureView) {
                Bitmap bm = ((TextureView) view).getBitmap();
                background.setImageBitmap(bm);
                return bm;
            } else {
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                if (view instanceof SurfaceView) {
                    PixelCopy.request((SurfaceView) view, bitmap, copyResult -> {
                        if (copyResult == PixelCopy.SUCCESS) {
                            background.setImageBitmap(bitmap);
                        }
                    }, new Handler(Looper.getMainLooper()));
                } else {
                    Canvas canvas = new Canvas(bitmap);
                    view.draw(canvas);
                    background.setImageBitmap(bitmap);
                    return bitmap;
                }
            }
        }
        return null;
    }
}
