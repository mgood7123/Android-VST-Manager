package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
        adapter = new OverviewAdapter();
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

    int global_padding = 25;

    class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(FrameLayout itemView, View contents) {
                super(itemView);
                itemView.addView(contents, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }

        @Override
        public OverviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(mContext);
            for (int i = 0; i < columnCount; i++) {
                FrameLayout frame = new FrameLayout(mContext);
                frame.setPadding(global_padding, global_padding, global_padding, global_padding);
                frame.addView(generator.generate(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                layout.addView(
                        frame,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                );
            }
            return new ViewHolder(new FrameLayout(mContext), layout);
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(OverviewAdapter.ViewHolder holder, int position) {
            float containerHeight = getHeight();

            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            p.height = Math.round(containerHeight/rowCount);
            holder.itemView.setLayoutParams(p);

            if (onClickListener != null) {
                holder.itemView.setOnClickListener(onClickListener);
            }
        }

        @Override
        public int getItemCount() {
            return rowCount*2;
        }
    }
}
