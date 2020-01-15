package com.zpj.shouji.market.ui.fragment.main.homepage;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.felix.atoast.library.AToast;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.ui.adapter.PageAdapter;
import com.zpj.shouji.market.ui.fragment.base.BaseFragment;
import com.zpj.shouji.market.ui.fragment.manager.AppManagerFragment;
import com.zpj.shouji.market.ui.widget.DotPagerIndicator;
import com.zpj.shouji.market.ui.widget.ScaleTransitionPagerTitleView;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;

import java.util.ArrayList;

import me.yokeyword.fragmentation.SupportActivity;

public class HomeFragment extends BaseFragment {

    private static final String[] TAB_TITLES = {"推荐", "发现", "乐图"};

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected boolean supportSwipeBack() {
        return false;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        CommonTitleBar titleBar = view.findViewById(R.id.title_bar);
        View rightCustomView = titleBar.getRightCustomView();
        rightCustomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AToast.normal("rightCustomView");
            }
        });
        ArrayList<Fragment> list = new ArrayList<>();
        list.add(new RecommendFragment());
        list.add(ExploreFragment.newInstance("http://tt.shouji.com.cn/app/faxian.jsp?index=faxian&versioncode=198"));
        list.add(new Fragment());

        PageAdapter adapter = new PageAdapter(getChildFragmentManager(), list, TAB_TITLES);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        MagicIndicator magicIndicator = (MagicIndicator) titleBar.getCenterCustomView();
        CommonNavigator navigator = new CommonNavigator(getContext());
        navigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return TAB_TITLES.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                ScaleTransitionPagerTitleView titleView = new ScaleTransitionPagerTitleView(context);
                titleView.setNormalColor(Color.WHITE);
                titleView.setSelectedColor(Color.WHITE);
                titleView.setTextSize(14);
                titleView.setText(TAB_TITLES[index]);
                titleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewPager.setCurrentItem(index);
                    }
                });
                return titleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                return new DotPagerIndicator(context);
            }
        });
        magicIndicator.setNavigator(navigator);
        ViewPagerHelper.bind(magicIndicator, viewPager);


        ImageView manageBtn = titleBar.getRightCustomView().findViewById(R.id.btn_manage);
        manageBtn.setOnClickListener(v -> {
            if (getActivity() instanceof SupportActivity) {
                ((SupportActivity) getActivity()).start(new AppManagerFragment());
            }
        });
    }
}