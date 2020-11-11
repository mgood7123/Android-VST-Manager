package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Random;

import smallville7123.bitmapview.BitmapVector;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class OverviewGrid extends RecyclerView {
    private static final String TAG = "OverviewGrid";
    public boolean rainbow = false;
    private Context mContext;
    OverviewAdapter adapter;
    GridLayoutManager manager;
    OnClickListener onClickListener;
    int rowCount = 2;
    int columnCount = 2;
    BitmapVector Items = new BitmapVector();
    int global_padding = 75;


    public OverviewGrid(@NonNull Context context) {
        super(context);
        init(context);
    }

    public OverviewGrid(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OverviewGrid(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        adapter = new OverviewAdapter();
        setAdapter(adapter);
        manager = new GridLayoutManager(mContext, 1);
        setLayoutManager(manager);
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
        manager.setSpanCount(count);
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

    class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {
        Random random = new Random();

        public class ViewHolder extends RecyclerView.ViewHolder {

            FrameLayout frameLayout;
            FrameLayout contentFrame;
            ImageView content;

            public ViewHolder(FrameLayout itemView) {
                super(itemView);
                frameLayout = itemView;
                contentFrame = new FrameLayout(mContext);
                content = new ImageView(mContext);

                content.setBackgroundColor(Color.BLACK);
                frameLayout.addView(contentFrame, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                contentFrame.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }

            void setRainbow() {
                int r = random.nextInt(255);
                int g = random.nextInt(255);
                int b = random.nextInt(255);
                frameLayout.setBackgroundColor(Color.rgb(r, g, b));
            }

            public void adjustHeightByRowCount() {
                float containerHeight = getHeight();
                ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
                p.height = Math.round(containerHeight/rowCount);
                frameLayout.setLayoutParams(p);
            }

            public void adjustForPadding() {
                contentFrame.setPadding(global_padding, global_padding, global_padding, global_padding);
            }

            public void setItem(int position) {
                // add item if we can
                if (position < Items.size()) {
                    Log.d(TAG, "setItem() called with: position = [" + position + "]");
                    content.setImageBitmap(Items.get(position));
                }
            }

            public void setOnClickListener() {
                if (onClickListener != null) {
                    frameLayout.setOnClickListener(onClickListener);
                } else {
                    frameLayout.setOnClickListener(null);
                    frameLayout.setClickable(false);
                }
            }
        }

        @Override
        public OverviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new FrameLayout(mContext));
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (rainbow) holder.setRainbow();
            holder.adjustHeightByRowCount();
            holder.adjustForPadding();
            holder.setItem(position);
            holder.setOnClickListener();
        }

        @Override
        public int getItemCount() {
            // ensure there is at least rowCount items
            int itemSize = Items.size();
            return itemSize + (itemSize % columnCount);
        }
    }
}
