package com.zpj.shouji.market.ui.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;

import com.felix.atoast.library.AToast;
import com.zpj.fragmentation.BaseFragment;
import com.zpj.fragmentation.anim.DefaultHorizontalAnimator;
import com.zpj.fragmentation.anim.FragmentAnimator;
import com.zpj.matisse.CaptureMode;
import com.zpj.matisse.Matisse;
import com.zpj.matisse.MimeType;
import com.zpj.matisse.engine.impl.GlideEngine;
import com.zpj.matisse.entity.Item;
import com.zpj.matisse.listener.OnSelectedListener;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.manager.UserManager;
import com.zpj.shouji.market.model.MessageInfo;
import com.zpj.shouji.market.ui.adapter.FragmentsPagerAdapter;
import com.zpj.shouji.market.ui.fragment.homepage.HomeFragment;
import com.zpj.shouji.market.ui.fragment.profile.MyFragment;
import com.zpj.shouji.market.ui.fragment.profile.MyPrivateLetterFragment;
import com.zpj.shouji.market.ui.fragment.recommond.GameRecommendFragment2;
import com.zpj.shouji.market.ui.fragment.recommond.SoftRecommendFragment2;
import com.zpj.shouji.market.ui.widget.BottomBar;
import com.zpj.shouji.market.ui.widget.BottomBarTab;
import com.zpj.shouji.market.ui.widget.ZViewPager;
import com.zpj.shouji.market.ui.widget.popup.MorePopup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment
        implements MorePopup.OnItemClickListener {
//    IHttp.OnSuccessListener<MessageInfo>

    private final List<BaseFragment> fragments = new ArrayList<>();
    private ZViewPager viewPager;
    private BottomBar mBottomBar;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected boolean supportSwipeBack() {
        return false;
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultHorizontalAnimator();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        HomeFragment homeFragment = findChildFragment(HomeFragment.class);
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }

        SoftRecommendFragment2 softFragment = findChildFragment(SoftRecommendFragment2.class);
        if (softFragment == null) {
            softFragment = new SoftRecommendFragment2();
        }

        GameRecommendFragment2 game = findChildFragment(GameRecommendFragment2.class);
        if (game == null) {
            game = new GameRecommendFragment2();
        }

        MyFragment profileFragment = findChildFragment(MyFragment.class);
        if (profileFragment == null) {
            profileFragment = new MyFragment();
        }
        fragments.clear();
        fragments.add(homeFragment);
        fragments.add(softFragment);
        fragments.add(game);
        fragments.add(profileFragment);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab);

        mBottomBar = view.findViewById(R.id.bottom_bar);

        BottomBarTab emptyTab = new BottomBarTab(context);
        emptyTab.setClickable(false);
        mBottomBar.addItem(BottomBarTab.build(context, "主页", R.drawable.ic_home_normal, R.drawable.ic_home_checked))
                .addItem(BottomBarTab.build(context, "应用", R.drawable.ic_software_normal, R.drawable.ic_software_checked))
                .addItem(emptyTab)
                .addItem(BottomBarTab.build(context, "游戏", R.drawable.ic_game_normal, R.drawable.ic_game_checked))
                .addItem(BottomBarTab.build(context, "我的", R.drawable.ic_me_normal, R.drawable.ic_me_checked));

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                if (position == 2) {
                    floatingActionButton.performClick();
                    return;
                }
                if (position > 2) {
                    position -= 1;
                }
                if(viewPager.getCurrentItem() != position) {
                    viewPager.setCurrentItem(position, true);
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
            }
        });


        viewPager = view.findViewById(R.id.vp);
        viewPager.setScrollerSpeed(500);
        viewPager.setCanScroll(false);
        viewPager.setOffscreenPageLimit(fragments.size());
        FragmentsPagerAdapter adapter = new FragmentsPagerAdapter(getChildFragmentManager(), fragments, null);
        viewPager.setAdapter(adapter);

        floatingActionButton.setOnClickListener(v -> {
            postDelayed(this::darkStatusBar, 300);
            MorePopup.with((ViewGroup) view)
                    .setListener(this)
                    .show();
        });

        mBottomBar.setCurrentItem(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        AToast.success("onResume");
//        UserManager.getInstance().rsyncMessage(this);
        UserManager.getInstance().rsyncMessage(false);
    }

    @Override
    public void onSupportVisible() {
        if (viewPager != null && !fragments.isEmpty()) {
            fragments.get(viewPager.getCurrentItem()).onSupportVisible();
        } else {
            darkStatusBar();
        }
    }

    @Override
    public void onSupportInvisible() {
        if (viewPager != null && !fragments.isEmpty()) {
            fragments.get(viewPager.getCurrentItem()).onSupportInvisible();
        } else {
            darkStatusBar();
        }
    }

    @Override
    public void onDiscoverItemClick() {
        DiscoverEditorFragment2.start();
    }

    @Override
    public void onCollectionItemClick() {

    }

    @Override
    public void onWallpaperItemClick() {
        Matisse.from(_mActivity)
                .choose(MimeType.ofImage())//照片视频全部显示MimeType.allOf()
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(3)//最大选择数量为9
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
//                .gridExpectedSize(this.getResources().getDimensionPixelSize(R.dimen.photo))//图片显示表格的大小
                .spanCount(3)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                .thumbnailScale(0.85f)//缩放比例
                .imageEngine(new GlideEngine())//图片加载方式，Glide4需要自定义实现
                .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .capture(true, CaptureMode.All)//存储到哪里
                .setOnSelectedListener(new OnSelectedListener() {
                    @Override
                    public void onSelected(@NonNull List<Item> itemList) {

                    }
                })
                .start();
    }

    @Override
    public void onChatWithFriendItemClick() {
//        ChatFragment2.start();
        MyPrivateLetterFragment.start();
    }

//    @Override
//    public void onSuccess(MessageInfo info) throws Exception {
//
//    }

    @Subscribe
    public void onUpdateMessageInfoEvent(MessageInfo info) {
        mBottomBar.getItem(4).setUnreadCount(info.getTotalCount());
    }

}
