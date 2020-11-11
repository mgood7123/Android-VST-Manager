package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import smallville7123.vstmanager.core.BitmapVector;

public class Overview extends RecyclerView {
    private static final String TAG = "Overview";
    public int type = Types.Samsung_GoodLock_TaskChanger_Grid;
    public static class Types {
        static int Samsung_GoodLock_TaskChanger_Grid = 0;
        static int AndroidPie = 1;
        static int Zen_X_OS_AndroidPie = 2;
    }

    public Context mContext;
    Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid adapter;
    OnClickListener onClickListener;
    int rowCount = 2;
    int columnCount = 2;
    BitmapVector Items = new BitmapVector();


    public Overview(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Overview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Overview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        adapter = new Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid(this);
        setAdapter(adapter);
        adapter.setManager(this);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
        super.setOnClickListener(l);
    }

    public void setRows(int count) {
        rowCount = count;
    }

    public void setColumns(int count) {
        columnCount = count;
        adapter.manager.setSpanCount(count);
    }

    public void addItem(Bitmap item) {
        Log.d(TAG, "addItem() called with: item = [" + item + "]");
        Items.add(item);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void clear() {
        // remove all items from this RecycleView so they can be garbage collected if needed
        Items.recycleAndRemoveAllElements();
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
