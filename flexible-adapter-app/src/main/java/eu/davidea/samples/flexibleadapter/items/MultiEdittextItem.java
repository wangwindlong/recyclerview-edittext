package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.fragments.ItemEditChangeListener;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * <b>Tip:</b> Consider to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class MultiEdittextItem extends AbstractItem<MultiEdittextItem.SimpleViewHolder>
        implements ISectionable<MultiEdittextItem.SimpleViewHolder, HeaderItem>, IFilterable<String>, Serializable {

    /* The header of this item */
    HeaderItem header;
    private String total1 = "0";
    private String total2 = "0";

    private MultiEdittextItem(String id) {
        super(id);
        setDraggable(true);
        setSwipeable(true);
    }

    public MultiEdittextItem(String id, HeaderItem header) {
        this(id);
        this.header = header;
    }

    @Override
    public String getSubtitle() {
        return getId()
                + (getHeader() != null ? " - " + getHeader().getId() : "")
                + (getUpdates() > 0 ? " - u" + getUpdates() : "");
    }

    @Override
    public HeaderItem getHeader() {
        return header;
    }

    @Override
    public void setHeader(HeaderItem header) {
        this.header = header;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_edittext_item;
    }

    @Override
    public SimpleViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new SimpleViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, SimpleViewHolder holder, int position, List<Object> payloads) {
        Context context = holder.itemView.getContext();

        // Background, when bound the first time
        if (payloads.size() == 0) {
            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE, Color.parseColor("#dddddd"), //Same color of divider
                    DrawableUtils.getColorControlHighlight(context));
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
            DrawableUtils.setBackgroundCompat(holder.frontView, drawable);
        }

        // Display the current flip status
        holder.mFlipView.flipSilently(adapter.isSelected(position));

        // In case of any Words in the searchText matches with Title this will be highlighted
        if (adapter.hasFilter()) {
            String filter = adapter.getFilter(String.class);
            FlexibleUtils.highlightWords(holder.mTitle, getTitle(), filter);
            FlexibleUtils.highlightWords(holder.mSubtitle, total1, filter);
            FlexibleUtils.highlightWords(holder.mSubtitle2, total2, filter);
        } else {
            holder.mTitle.setText(getTitle());
            holder.mSubtitle.setText(total1);
            holder.mSubtitle2.setText(total2);
        }
    }

    public void setTotal(String total, int type) {
        if (type == 0) total1 = total;
        else total2 = total;
    }

    public Float getTotalPrice(int type) {
        try {
            String price = type == 0 ? total1 : total2;
            return Float.valueOf(price);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    @Override
    public boolean filter(String constraint) {
        for (String word : constraint.split(FlexibleUtils.SPLIT_EXPRESSION)) {
            if (getTitle().toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }

    static final class SimpleViewHolder extends FlexibleViewHolder {

        FlipView mFlipView;
        TextView mTitle;
        EditText mSubtitle;
        EditText mSubtitle2;
        ImageView mHandleView;
        Context mContext;
        View frontView;
        View rearLeftView;
        View rearRightView;


        boolean swiped = false;

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mContext = view.getContext();
            this.mTitle = view.findViewById(R.id.title);
            this.mSubtitle = view.findViewById(R.id.subtitle);
            this.mSubtitle2 = view.findViewById(R.id.subtitle2);
            this.mFlipView = view.findViewById(R.id.image);
            this.mFlipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter.mItemLongClickListener != null) {
                        mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
                        Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                        toggleActivation();
                    }
                }
            });
            this.mHandleView = view.findViewById(R.id.row_handle);
            setDragHandleView(mHandleView);

            this.frontView = view.findViewById(R.id.front_view);
            this.rearLeftView = view.findViewById(R.id.rear_left_view);
            this.rearRightView = view.findViewById(R.id.rear_right_view);
            if (adapter instanceof ExampleAdapter) {
                mSubtitle.addTextChangedListener(new ItemTextWatcher(adapter, this, 0));
                mSubtitle2.addTextChangedListener(new ItemTextWatcher(adapter, this, 1));
            }
        }



        @Override
        protected void setDragHandleView(@NonNull View view) {
            if (mAdapter.isHandleDragEnabled()) {
                view.setVisibility(View.VISIBLE);
                super.setDragHandleView(view);
            } else {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(mContext, "Click on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            super.onClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(mContext, "LongClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            return super.onLongClick(view);
        }

        @Override
        public void toggleActivation() {
            super.toggleActivation();
            // Here we use a custom Animation inside the ItemView
            mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
        }

        @Override
        public float getActivationElevation() {
            return eu.davidea.utils.Utils.dpToPx(itemView.getContext(), 4f);
        }

        @Override
        protected boolean shouldActivateViewWhileSwiping() {
            return false;//default=false
        }

        @Override
        protected boolean shouldAddSelectionInActionMode() {
            return false;//default=false
        }

        @Override
        public View getFrontView() {
            return frontView;
        }

        @Override
        public View getRearLeftView() {
            return rearLeftView;
        }

        @Override
        public View getRearRightView() {
            return rearRightView;
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager ||
                    mAdapter.getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager) {
                if (position % 2 != 0)
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            } else {
                //Linear layout
                if (mAdapter.isSelected(position))
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            }
        }

        @Override
        public void onItemReleased(int position) {
            swiped = (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE);
            super.onItemReleased(position);
        }
    }

    static class ItemTextWatcher implements TextWatcher {
        FlexibleAdapter adapter;
        FlexibleViewHolder holder;
        int type;
        String content;

        Handler mHandler = new Handler();
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                ItemEditChangeListener listener = ((ExampleAdapter) adapter).getEditChangeListener();
                AbstractFlexibleItem item = ((ExampleAdapter) adapter).getItem(holder.getAdapterPosition());
                if (item instanceof MultiEdittextItem) {
                    ((MultiEdittextItem) item).setTotal(content, type);
                }
                if (listener != null) {
                    listener.onItemEditChanged(holder.getAdapterPosition(), type);
                }
            }
        };

        public ItemTextWatcher(FlexibleAdapter adapter, FlexibleViewHolder holder, int type) {
            this.adapter = adapter;
            this.holder = holder;
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            content = s.toString();
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 1000);
        }
    }

    @Override
    public String toString() {
        return "SimpleItem[" + super.toString() + "]";
    }

}