package com.zpj.shouji.market.ui.fragment.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.zpj.fragmentation.BaseFragment;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.event.StartFragmentEvent;
import com.zpj.shouji.market.manager.UserManager;
import com.zpj.shouji.market.ui.adapter.FragmentsPagerAdapter;
import com.zpj.shouji.market.ui.fragment.WallpaperListFragment;
import com.zpj.shouji.market.ui.fragment.collection.CollectionListFragment;
import com.zpj.shouji.market.ui.fragment.theme.ThemeListFragment;
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

public class MyDynamicFragment extends BaseFragment {

    private static final String[] TAB_TITLES = {"全部", "发现", "评论", "应用集", "乐图"};

    private static final String KEY_ID = "key_id";
    private static final String KEY_SHOW_TOOLBAR = "key_show_toolbar";

    private ViewPager viewPager;
    private MagicIndicator magicIndicator;
    private String userId = "";
    private boolean showToolbar = true;

    public static MyDynamicFragment newInstance(String id, boolean showToolbar) {
        Bundle args = new Bundle();
        args.putString(KEY_ID, id);
        args.putBoolean(KEY_SHOW_TOOLBAR, showToolbar);
        MyDynamicFragment fragment = new MyDynamicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void start() {
        StartFragmentEvent.start(newInstance(UserManager.getInstance().getUserId(), true));
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

        if (getArguments() != null) {
            userId = getArguments().getString(KEY_ID, "");
            showToolbar = getArguments().getBoolean(KEY_SHOW_TOOLBAR, true);
        }

        viewPager = view.findViewById(R.id.view_pager);
        magicIndicator = view.findViewById(R.id.magic_indicator);

        if (showToolbar) {
            setToolbarTitle("我的动态");
            postOnEnterAnimationEnd(this::initViewPager);
        } else {
            toolbar.setVisibility(View.GONE);
            setSwipeBackEnable(false);
        }
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        if (!showToolbar) {
            postOnEnterAnimationEnd(this::initViewPager);
        }
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        AllFragment allFragment = findChildFragment(AllFragment.class);
        if (allFragment == null) {
            allFragment = AllFragment.newInstance(userId);
        }

        DiscoverFragment discoverFragment = findChildFragment(DiscoverFragment.class);
        if (discoverFragment == null) {
            discoverFragment = DiscoverFragment.newInstance(userId);
        }

        CommentFragment commentFragment = findChildFragment(CommentFragment.class);
        if (commentFragment == null) {
            commentFragment = CommentFragment.newInstance(userId);
        }

        CollectionsFragment collectionsFragment = findChildFragment(CollectionsFragment.class);
        if (collectionsFragment == null) {
            collectionsFragment = CollectionsFragment.newInstance(userId);
        }

        WallpaperFragment wallpaperFragment = findChildFragment(WallpaperFragment.class);
        if (wallpaperFragment == null) {
            wallpaperFragment = WallpaperFragment.newInstance(userId);
        }

        fragments.add(allFragment);
        fragments.add(discoverFragment);
        fragments.add(commentFragment);
        fragments.add(collectionsFragment);
        fragments.add(wallpaperFragment);
        viewPager.setAdapter(new FragmentsPagerAdapter(getChildFragmentManager(), fragments, TAB_TITLES));
        viewPager.setOffscreenPageLimit(2);

        CommonNavigator navigator = new CommonNavigator(getContext());
        navigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return TAB_TITLES.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                ColorTransitionPagerTitleView titleView = new ColorTransitionPagerTitleView(context);
                titleView.setNormalColor(getResources().getColor(R.color.color_text_major));
                titleView.setSelectedColor(getResources().getColor(R.color.colorPrimary));
                titleView.setTextSize(14);
                titleView.setText(TAB_TITLES[index]);
                titleView.setOnClickListener(view1 -> viewPager.setCurrentItem(index, true));
                return titleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(ScreenUtils.dp2px(context, 4f));
                indicator.setLineWidth(ScreenUtils.dp2px(context, 12f));
                indicator.setRoundRadius(ScreenUtils.dp2px(context, 4f));
                indicator.setColors(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimary));
                return indicator;
            }
        });
        magicIndicator.setNavigator(navigator);
        ViewPagerHelper.bind(magicIndicator, viewPager);
    }

    public static class AllFragment extends ThemeListFragment {

        public static AllFragment newInstance(String userId) {
            String url = "http://tt.shouji.com.cn/app/view_member_content_xml_v2.jsp?id=" + userId;
            Bundle args = new Bundle();
            args.putString(KEY_DEFAULT_URL, url);
            AllFragment fragment = new AllFragment();
            fragment.setArguments(args);
            return fragment;
        }

    }

    public static class DiscoverFragment extends ThemeListFragment {

        public static DiscoverFragment newInstance(String userId) {
            String url = "http://tt.shouji.com.cn/app/view_member_content_xml_v2.jsp?t=discuss&id=" + userId;
            Bundle args = new Bundle();
            args.putString(KEY_DEFAULT_URL, url);
            DiscoverFragment fragment = new DiscoverFragment();
            fragment.setArguments(args);
            return fragment;
        }

    }

    public static class CommentFragment extends ThemeListFragment {

        public static CommentFragment newInstance(String id) {
            String url = "http://tt.shouji.com.cn/app/view_member_content_xml_v2.jsp?t=review&id=" + id;
            Bundle args = new Bundle();
            args.putString(KEY_DEFAULT_URL, url);
            CommentFragment fragment = new CommentFragment();
            fragment.setArguments(args);
            return fragment;
        }

    }

    public static class CollectionsFragment extends CollectionListFragment {

        public static CollectionsFragment newInstance(String id) {
            String url = "http://tt.shouji.com.cn/androidv3/yyj_user_xml.jsp?id=" + id;
            Bundle args = new Bundle();
            args.putString(KEY_DEFAULT_URL, url);
            CollectionsFragment fragment = new CollectionsFragment();
            fragment.setArguments(args);
            return fragment;
        }

    }

    public static class WallpaperFragment extends WallpaperListFragment {

        public static WallpaperFragment newInstance(String id) {
            String url = "http://tt.shouji.com.cn/app/bizhi_list.jsp?member=" + id;
            Bundle args = new Bundle();
            args.putString(KEY_DEFAULT_URL, url);
            WallpaperFragment fragment = new WallpaperFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        protected void handleArguments(Bundle arguments) {
            defaultUrl = arguments.getString(KEY_DEFAULT_URL, "");
            nextUrl = defaultUrl;
        }

        @Override
        public void onRefresh() {
            data.clear();
            nextUrl = defaultUrl;
            recyclerLayout.notifyDataSetChanged();
        }

        @Override
        protected int getHeaderLayout() {
            return 0;
        }

    }

}
