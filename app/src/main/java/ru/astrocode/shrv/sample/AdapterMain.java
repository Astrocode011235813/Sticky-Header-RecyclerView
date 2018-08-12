package ru.astrocode.shrv.sample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import ru.astrocode.shrv.library.SHRVItemType;
import ru.astrocode.shrv.library.SHRVLinearLayoutManager;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Astrocode on 27.03.2017.
 */

public class AdapterMain extends RecyclerView.Adapter<AdapterMain.ViewHolderMain> implements SHRVItemType {
    private final static int TYPE_ITEM = 1;

    private final Context mContext;

    private final int mHeaderSize, mItemSize;
    private final int mItemPadding;

    private final int mHeaderColor, mHeaderTextColor;
    private final int mItemColor, mItemTextColor;

    private ArrayList<String> mData;

    public AdapterMain(Context context) {
        mContext = context;

        mData = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.Countries)));

        Resources res = mContext.getResources();

        mHeaderSize = res.getDimensionPixelSize(R.dimen.list_header_item_size);
        mItemSize = res.getDimensionPixelSize(R.dimen.list_item_size);
        mItemPadding = res.getDimensionPixelOffset(R.dimen.list_item_padding);

        mHeaderColor = res.getColor(R.color.list_header_item);
        mHeaderTextColor = res.getColor(R.color.list_header_item_text);

        mItemColor = res.getColor(R.color.list_item);
        mItemTextColor = res.getColor(R.color.list_item_text);
    }

    @Override
    public AdapterMain.ViewHolderMain onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout view = new RelativeLayout(mContext);

        ViewGroup.LayoutParams lp = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        RelativeLayout.LayoutParams textViewLp = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);

        TextView textView = new TextView(mContext);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setLines(1);
        textView.setPadding(mItemPadding, mItemPadding, mItemPadding, mItemPadding);
        textView.setGravity(Gravity.START | Gravity.CENTER);

        if (viewType == TYPE_HEADER) {
            view.setBackgroundColor(mHeaderColor);
            textView.setTextColor(mHeaderTextColor);

            lp.height = mHeaderSize;
        } else {
            view.setBackgroundColor(mItemColor);
            textView.setTextColor(mItemTextColor);

            lp.height = mItemSize;
        }
        lp.width = MATCH_PARENT;

        view.setLayoutParams(lp);


        view.addView(textView, textViewLp);

        return new ViewHolderMain(view, textView);
    }

    @Override
    public void onBindViewHolder(AdapterMain.ViewHolderMain holder, int position) {
        holder.mTextView.setText(mData.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).length() == 1) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolderMain extends RecyclerView.ViewHolder {
        TextView mTextView;

        public ViewHolderMain(View itemView, TextView textView) {
            super(itemView);
            mTextView = textView;
        }
    }
}
