package com.zpj.shouji.market.ui.multidata;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zpj.recyclerview.EasyRecyclerView;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.shouji.market.R;

import java.util.List;

public abstract class RecyclerMultiData<T> extends BaseHeaderMultiData<T> {

    protected EasyRecyclerView<T> recyclerView;

    public RecyclerMultiData(String title) {
        super(title);
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public int getChildViewType(int position) {
        return hashCode();
    }

    @Override
    public boolean hasChildViewType(int viewType) {
        return viewType == hashCode();
    }

    @Override
    public int getChildLayoutId(int viewType) {
        return R.layout.layout_recycler_view;
    }

    @Override
    public void onBindChild(EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {
        if (recyclerView == null) {
            RecyclerView view = holder.getView(R.id.recycler_view);
            view.setNestedScrollingEnabled(false);
            recyclerView = new EasyRecyclerView<>(view);
            recyclerView.setData(list)
                    .setItemRes(getItemRes())
                    .setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
            buildRecyclerView(recyclerView);
            recyclerView.build();
            recyclerView.showContent();
        }
    }

    public abstract int getItemRes();

    public abstract void buildRecyclerView(EasyRecyclerView<T> recyclerView);


}
