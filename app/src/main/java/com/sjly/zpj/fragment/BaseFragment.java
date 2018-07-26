package com.sjly.zpj.fragment;


import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment{
    protected boolean isVisible;
    private boolean isFragmentVisible;
    private boolean isFirst = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
            lazyLoadData();
        } else {
            isVisible = false;
        }
    }

    public abstract void lazyLoadData();
}
