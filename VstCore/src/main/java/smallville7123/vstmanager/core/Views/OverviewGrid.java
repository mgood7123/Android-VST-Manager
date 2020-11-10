package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OverviewGrid extends RecyclerView {
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

    private Context mContext;
    OverviewAdapter adapter;

    private void init(Context context) {
        mContext = context;
        adapter = new OverviewAdapter(this);
        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(mContext));
    }

    OnClickListener onClickListener;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
        super.setOnClickListener(l);
    }

    int rowCount = 0;
    int columnCount = 0;

    public void setRows(int count) {
        rowCount = count;
    }

    public void setColumns(int count) {
        columnCount = count;
    }

    PlaceholderGenerator generator;

    public void setPlaceholder(PlaceholderGenerator placeholderGenerator) {
        generator = placeholderGenerator;
    }

    public static abstract class PlaceholderGenerator {
        public abstract View generate();
    }

    class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View contents;

            public ViewHolder(FrameLayout itemView, View contents) {
                super(itemView);
                this.contents = contents;
                itemView.addView(contents, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }

        private OverviewGrid mOverviewGrid;

        public OverviewAdapter(OverviewGrid recyclerView) {
            mOverviewGrid = recyclerView;
        }

        @Override
        public OverviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new FrameLayout(mContext), mOverviewGrid.generator.generate());
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(OverviewAdapter.ViewHolder holder, int position) {
            float containerHeight = mOverviewGrid.getHeight();

            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            p.height = Math.round(containerHeight/mOverviewGrid.rowCount);
            holder.itemView.setLayoutParams(p);
            holder.itemView.setPadding(100,100,100,100);

            if (mOverviewGrid.onClickListener != null) {
                holder.itemView.setOnClickListener(mOverviewGrid.onClickListener);
            }
        }

        @Override
        public int getItemCount() {
            return mOverviewGrid.rowCount*2;
        }
    }
}
