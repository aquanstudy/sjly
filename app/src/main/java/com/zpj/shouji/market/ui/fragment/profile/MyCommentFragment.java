package com.zpj.shouji.market.ui.fragment.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.felix.atoast.library.AToast;
import com.zpj.fragmentation.BaseFragment;
import com.zpj.http.core.IHttp;
import com.zpj.http.parser.html.nodes.Document;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.constant.Keys;
import com.zpj.shouji.market.constant.UpdateFlagAction;
import com.zpj.shouji.market.event.StartFragmentEvent;
import com.zpj.shouji.market.manager.UserManager;
import com.zpj.shouji.market.ui.adapter.FragmentsPagerAdapter;
import com.zpj.shouji.market.ui.fragment.theme.ThemeListFragment;
import com.zpj.shouji.market.ui.widget.ScaleTransitionPagerTitleView;
import com.zpj.shouji.market.utils.MagicIndicatorHelper;
import com.zpj.utils.ScreenUtils;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import java.util.ArrayList;
import java.util.List;

public class MyCommentFragment extends BaseFragment {

    private static final String[] TAB_TITLES = {"评论", "发起的评论"};

    protected ViewPager viewPager;
    private MagicIndicator magicIndicator;

    public static void start() {
        StartFragmentEvent.start(new MyCommentFragment());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_discover;
    }

    @Override
    protected boolean supportSwipeBack() {
        return true;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        setToolbarTitle("我的评论");
        viewPager = view.findViewById(R.id.view_pager);
        magicIndicator = view.findViewById(R.id.magic_indicator);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        List<Fragment> fragments = new ArrayList<>();
        MyRelatedCommentFragment myRelatedCommentFragment = findChildFragment(MyRelatedCommentFragment.class);
        if (myRelatedCommentFragment == null) {
            myRelatedCommentFragment = MyRelatedCommentFragment.newInstance();
        }
        MyPublishCommentFragment myPublishCommentFragment = findChildFragment(MyPublishCommentFragment.class);
        if (myPublishCommentFragment == null) {
            myPublishCommentFragment = MyPublishCommentFragment.newInstance();
        }
        fragments.add(myRelatedCommentFragment);
        fragments.add(myPublishCommentFragment);
        viewPager.setAdapter(new FragmentsPagerAdapter(getChildFragmentManager(), fragments, TAB_TITLES));
        viewPager.setOffscreenPageLimit(2);

        MagicIndicatorHelper.bindViewPager(context, magicIndicator, viewPager, TAB_TITLES);
    }

    @Override
    public void onDestroy() {
        HttpApi.updateFlagApi(UpdateFlagAction.COMMENT);
        super.onDestroy();
    }

    public static class MyRelatedCommentFragment extends ThemeListFragment {

        public static MyRelatedCommentFragment newInstance() {
            String url = "http://tt.tljpxm.com/app/user_content_list_xml_v2.jsp?t=review";
            Bundle args = new Bundle();
            args.putString(Keys.DEFAULT_URL, url);
            MyRelatedCommentFragment fragment = new MyRelatedCommentFragment();
            fragment.setArguments(args);
            return fragment;
        }

    }

    public static class MyPublishCommentFragment extends ThemeListFragment {

        public static MyPublishCommentFragment newInstance() {
            String url = "http://tt.tljpxm.com/app/user_content_list_xml_v2.jsp?t=review&thread=thread";
            Bundle args = new Bundle();
            args.putString(Keys.DEFAULT_URL, url);
            MyPublishCommentFragment fragment = new MyPublishCommentFragment();
            fragment.setArguments(args);
            return fragment;
        }

    }

}
