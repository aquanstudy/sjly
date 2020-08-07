package com.zpj.shouji.market.ui.fragment.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.felix.atoast.library.AToast;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;
import com.shehuan.niv.NiceImageView;
import com.zpj.fragmentation.BaseFragment;
import com.zpj.popup.ZPopup;
import com.zpj.popup.impl.AttachListPopup;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.event.StartFragmentEvent;
import com.zpj.shouji.market.manager.UserManager;
import com.zpj.shouji.market.ui.adapter.FragmentsPagerAdapter;
import com.zpj.shouji.market.ui.fragment.WebFragment;
import com.zpj.shouji.market.ui.fragment.chat.ChatFragment2;
import com.zpj.shouji.market.ui.widget.JudgeNestedScrollView;
import com.zpj.shouji.market.utils.MagicIndicatorHelper;
import com.zpj.utils.ScreenUtils;
import com.zpj.widget.statelayout.StateLayout;
import com.zpj.widget.tinted.TintedImageView;

import net.lucode.hackware.magicindicator.MagicIndicator;

import java.util.ArrayList;
import java.util.List;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class ProfileFragment extends BaseFragment
        implements View.OnClickListener {

    private static final String USER_ID = "user_id";
    public static final String DEFAULT_URL = "http://tt.shouji.com.cn/app/view_member_xml_v4.jsp?id=5636865";

    private static final String[] TAB_TITLES = {"动态", "收藏", "下载", "好友"};

    private StateLayout stateLayout;
    private LinearLayout headerLayout;
    private ImageView ivHeader;
    private NiceImageView ivAvater;
    private NiceImageView ivToolbarAvater;
    private TextView tvFollow;
    private TintedImageView ivChat;
    private TextView tvName;
    private TextView tvToolbarName;
    private TextView tvInfo;
    private ViewPager mViewPager;
    private View buttonBarLayout;
    private MagicIndicator magicIndicator;

    private String userId;
    private boolean isMe;
    private boolean isFriend;

    public static void start(String userId, boolean shouldLazyLoad) {
        ProfileFragment profileFragment = new ProfileFragment();
//        profileFragment.setShouldLazyLoad(shouldLazyLoad);
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userId);
        profileFragment.setArguments(bundle);
        StartFragmentEvent.start(profileFragment);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile3;
    }

    @Override
    protected boolean supportSwipeBack() {
        return true;
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString(USER_ID);
        } else {
            userId = null;
        }


        stateLayout = view.findViewById(R.id.state_layout);

        if (TextUtils.isEmpty(userId)) {
            stateLayout.showErrorView("用户不存在！");
            return;
        }
        isMe = userId.equals(UserManager.getInstance().getUserId());

        headerLayout = view.findViewById(R.id.layout_header);

        tvFollow = view.findViewById(R.id.tv_follow);
        tvFollow.setOnClickListener(this);
        ivChat = view.findViewById(R.id.iv_chat);
        ivChat.setOnClickListener(this);
        if (isMe) {
            tvFollow.setText("编辑");
            tvFollow.setBackgroundResource(R.drawable.bg_button_round_gray);
//            tvFollow.setBackground(new DrawableBuilder()
//                    .rectangle()
//                    .rounded()
//                    .strokeColor(getResources().getColor(R.color.colorPrimary))
//                    .solidColor(getResources().getColor(R.color.colorPrimary))
//                    .build());
        }

        ivHeader = view.findViewById(R.id.iv_header);
        ivAvater = view.findViewById(R.id.iv_avatar);
        ivToolbarAvater = view.findViewById(R.id.toolbar_avatar);
        tvName = view.findViewById(R.id.tv_name);
        tvToolbarName = view.findViewById(R.id.toolbar_name);
        tvInfo = view.findViewById(R.id.tv_info);

        mViewPager = view.findViewById(R.id.view_pager);
        buttonBarLayout = toolbar.getCenterCustomView();
        magicIndicator = view.findViewById(R.id.magic_indicator);

        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
            float alpha = (float) Math.abs(i) / appBarLayout1.getTotalScrollRange();
            alpha = Math.min(1f, alpha);
            buttonBarLayout.setAlpha(alpha);
            headerLayout.setAlpha(1f -alpha);
        });

        buttonBarLayout.setAlpha(0);

        stateLayout.showLoadingView();
        getMemberInfo();
    }

    @Override
    public void toolbarRightImageButton(@NonNull ImageButton imageButton) {
        super.toolbarRightImageButton(imageButton);
        imageButton.setOnClickListener(v -> {
            AttachListPopup<String> popup = ZPopup.attachList(context);
            popup.addItem("分享主页");
            if (!isMe) {
                popup.addItem("加入黑名单");
                popup.addItem("举报Ta");
            }
            popup.setOnSelectListener((position, title) -> {
                        switch (position) {
                            case 0:
                                WebFragment.shareHomepage(userId);
                                break;
                            case 1:
                                HttpApi.addBlacklistApi(userId);
                                break;
                            case 2:
                                AToast.warning("TODO");
                                break;
                        }
                    })
                    .show(imageButton);
        });
    }

    private void getMemberInfo() {
        HttpApi.getMemberInfoApi(userId)
                .onSuccess(element -> {
                    Log.d("onGetUserItem", "element=" + element);
                    isFriend = "1".equals(element.selectFirst("isfriend").text());
                    if (isFriend) {
                        tvFollow.setText("已关注");
                    } else {
                        ivChat.setVisibility(View.GONE);
                    }
                    String memberBackground = element.selectFirst("memberbackground").text();
                    if (!TextUtils.isEmpty(memberBackground)) {
                        Glide.with(context).load(memberBackground)
                                .apply(new RequestOptions()
                                        .error(R.drawable.bg_member_default)
                                        .placeholder(R.drawable.bg_member_default)
                                )
                                .into(ivHeader);
                    }
                    String url = element.selectFirst("memberavatar").text();
                    RequestOptions options = new RequestOptions()
                            .error(R.drawable.ic_user_head)
                            .placeholder(R.drawable.ic_user_head);
                    Glide.with(context)
                            .load(url)
                            .apply(options)
                            .into(ivAvater);
                    Glide.with(context)
                            .load(url)
                            .apply(options)
                            .into(ivToolbarAvater);

                    String nickName = element.selectFirst("nickname").text();
                    tvName.setText(nickName);
                    tvToolbarName.setText(nickName);
                    tvInfo.setText(element.selectFirst("membersignature").text());

                    postOnEnterAnimationEnd(() -> {
                        stateLayout.showContentView();
                        initViewPager();
                        lightStatusBar();
                    });
                })
                .onError(throwable -> {
                    AToast.error(throwable.getMessage());
                    stateLayout.showErrorView(throwable.getMessage());
                })
                .subscribe();
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        MyDynamicFragment dynamicFragment = findChildFragment(MyDynamicFragment.class);
        if (dynamicFragment == null) {
            dynamicFragment = MyDynamicFragment.newInstance(userId, false);
        }
        fragments.add(dynamicFragment);
        MyCollectionFragment collectionFragment = findChildFragment(MyCollectionFragment.class);
        if (collectionFragment == null) {
            collectionFragment = MyCollectionFragment.newInstance(userId, false);
        }
        fragments.add(collectionFragment);
        UserDownloadedFragment userDownloadedFragment = findChildFragment(UserDownloadedFragment.class);
        if (userDownloadedFragment == null) {
            userDownloadedFragment = UserDownloadedFragment.newInstance(userId);
        }
        fragments.add(userDownloadedFragment);

        MyFriendsFragment friendsFragment = findChildFragment(MyFriendsFragment.class);
        if (friendsFragment == null) {
            friendsFragment = MyFriendsFragment.newInstance(userId, false);
        }
        fragments.add(friendsFragment);
        FragmentsPagerAdapter adapter = new FragmentsPagerAdapter(getChildFragmentManager(), fragments, TAB_TITLES);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragments.size());

        MagicIndicatorHelper.bindViewPager(context, magicIndicator, mViewPager, TAB_TITLES, true);
    }

    @Override
    public void onClick(View v) {
        if (v == ivChat) {
            ChatFragment2.start(userId, tvName.getText().toString());
        } else if (v == tvFollow) {
            if (isMe) {
                AToast.normal("编辑");
            } else if (isFriend) {
                ZPopup.alert(context)
                        .setTitle("取消关注")
                        .setContent("确定取消关注该用户？")
                        .setConfirmButton(popup -> HttpApi.deleteFriendApi(userId)
                                .onSuccess(data -> {
                                    Log.d("deleteFriendApi", "data=" + data);
                                    String result = data.selectFirst("result").text();
                                    if ("success".equals(result)) {
                                        AToast.success("取消关注成功");
                                        tvFollow.setText("关注");
                                        ivChat.setVisibility(View.GONE);
                                        isFriend = false;
                                    } else {
                                        AToast.error(data.selectFirst("info").text());
                                    }
                                })
                                .onError(throwable -> AToast.error(throwable.getMessage()))
                                .subscribe())
                        .show();
            } else {
                HttpApi.addFriendApi(userId)
                        .onSuccess(data -> {
                            Log.d("addFriendApi", "data=" + data);
                            String result = data.selectFirst("result").text();
                            if ("success".equals(result)) {
                                AToast.success("关注成功");
                                tvFollow.setText("已关注");
                                ivChat.setVisibility(View.VISIBLE);
                                isFriend = true;
                            } else {
                                AToast.error(data.selectFirst("info").text());
                            }
                        })
                        .onError(throwable -> AToast.error(throwable.getMessage()))
                        .subscribe();
            }
        }
    }

}
