package smallville7123.vstmanager.core.Views;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid extends RecyclerView.Adapter<Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid.ViewHolder> {
    public GridLayoutManager manager;
    Overview overview;
    int global_padding = 75;

    public Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid(Overview overview) {
        this.overview = overview;
    }

    public void setManager(Overview overview) {
        manager = new GridLayoutManager(overview.mContext, 1);
        overview.setLayoutManager(manager);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        FrameLayout contentFrame;
        ImageView content;

        public ViewHolder(FrameLayout itemView) {
            super(itemView);
            frameLayout = itemView;
            contentFrame = new FrameLayout(overview.mContext);
            content = new ImageView(overview.mContext);

            content.setBackgroundColor(Color.BLACK);
            frameLayout.addView(contentFrame, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            contentFrame.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }

        public void adjustHeightByRowCount() {
            float containerHeight = overview.getHeight();
            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            p.height = Math.round(containerHeight/overview.rowCount);
            frameLayout.setLayoutParams(p);
        }

        public void adjustForPadding() {
            contentFrame.setPadding(global_padding, global_padding, global_padding, global_padding);
        }

        public void setItem(int position) {
            // add item if we can
            if (position < overview.Items.size()) {
                content.setImageBitmap(overview.Items.get(position));
            }
        }

        public void setOnClickListener() {
            if (overview.onClickListener != null) {
                frameLayout.setOnClickListener(overview.onClickListener);
            } else {
                frameLayout.setOnClickListener(null);
                frameLayout.setClickable(false);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new FrameLayout(overview.mContext));
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.adjustHeightByRowCount();
        holder.adjustForPadding();
        holder.setItem(position);
        holder.setOnClickListener();
    }

    @Override
    public int getItemCount() {
        // ensure there is at least rowCount items
        int itemSize = overview.Items.size();
        return itemSize + (itemSize % overview.columnCount);
    }
}
