package ru.astrocode.shrv.sample;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.astrocode.shrv.library.SHRVItemType;

/**
 * Created by Astrocode on 27.03.2017.
 */

public class AdapterMain extends RecyclerView.Adapter<AdapterMain.ViewHolderMain> implements SHRVItemType {
    private final static int TYPE_ITEM = 1;

    private final Context mContext;
    private String[] mData;

    public AdapterMain(Context context) {
        mContext = context;
        mData = context.getResources().getStringArray(R.array.Countries);
    }

    @Override
    public AdapterMain.ViewHolderMain onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview, parent, false);

        if (viewType == TYPE_HEADER) {
            view.setBackgroundColor(Color.parseColor("#808080"));
        }

        return new ViewHolderMain(view);
    }

    @Override
    public void onBindViewHolder(AdapterMain.ViewHolderMain holder, int position) {
        holder.mTextView.setText(mData[position]);
    }

    @Override
    public int getItemViewType(int position) {
        if (mData[position].length() == 1) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    public class ViewHolderMain extends RecyclerView.ViewHolder {
        TextView mTextView;

        public ViewHolderMain(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.textView);
        }
    }
}
