package com.zpj.shouji.market.ui.widget.recommend;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZViewHolder;
import com.zpj.http.parser.html.nodes.Element;
import com.zpj.http.parser.html.select.Elements;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpPreLoader;
import com.zpj.shouji.market.glide.blur.CropBlurTransformation;
import com.zpj.shouji.market.model.AppInfo;
import com.zpj.shouji.market.ui.fragment.SubjectRecommendListFragment;
import com.zpj.shouji.market.ui.fragment.ToolBarListFragment;
import com.zpj.shouji.market.ui.fragment.collection.CollectionRecommendListFragment;
import com.zpj.shouji.market.ui.fragment.detail.AppDetailFragment;
import com.zpj.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class RecommendBanner extends LinearLayout implements View.OnClickListener {

    private final List<AppInfo> bannerItemList = new ArrayList<>();
    private final BannerViewHolder bannerViewHolder = new BannerViewHolder();

    private final ImageView ivBg;
    private final MZBannerView<AppInfo> mMZBanner;

    private int currentPosition = 0;

    public RecommendBanner(Context context) {
        this(context, null);
    }

    public RecommendBanner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecommendBanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_recommend_header, this, true);
        ivBg = findViewById(R.id.iv_bg);
        mMZBanner = findViewById(R.id.banner);
        mMZBanner.setDelayedTime(5 * 1000);
        mMZBanner.setBannerPageClickListener(new MZBannerView.BannerPageClickListener() {
            @Override
            public void onPageClick(View view, int i) {
                AppDetailFragment.start(bannerItemList.get(i));
            }
        });
        mMZBanner.addPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                setBg(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        ViewGroup.LayoutParams params = mMZBanner.getLayoutParams();
        int screenWidth = ScreenUtils.getScreenWidth(context);

//        params.height = (int) ((float) screenWidth * screenWidth / ScreenUtils.getScreenHeight(context));
        params.height = (int) ((float) screenWidth / 2f);

        HttpPreLoader.getInstance().setLoadListener(HttpPreLoader.HOME_BANNER, document -> {
            Elements elements = document.select("item");
            bannerItemList.clear();
            for (Element element : elements) {
                AppInfo info = AppInfo.parse(element);
                if (info == null) {
                    continue;
                }
                bannerItemList.add(info);
                if (bannerItemList.size() >= 8) {
                    break;
                }
            }
            if (!bannerItemList.isEmpty()) {
                setBg(0);
            }
            mMZBanner.setPages(bannerItemList, () -> bannerViewHolder);

            mMZBanner.start();
        });

        findViewById(R.id.tv_common_app).setOnClickListener(this);
        findViewById(R.id.tv_recent_download).setOnClickListener(this);
        findViewById(R.id.tv_subjects).setOnClickListener(this);
        findViewById(R.id.tv_collections).setOnClickListener(this);
    }

    public void onResume() {
        if (mMZBanner != null) {
            mMZBanner.start();
        }
//        setBg(currentPosition);
    }

    public void onPause() {
        if (mMZBanner != null) {
            mMZBanner.pause();
        }
    }

    public void onStop() {
        if (mMZBanner != null) {
            mMZBanner.pause();
        }
    }

    private void setBg(int position) {
        currentPosition = position;
        Glide.with(ivBg.getContext())
                .asBitmap()
                .load(bannerItemList.get(position).getAppIcon())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation()))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        getColor(resource);
                        ivBg.setImageBitmap(resource);
//                        llContainer.setBackground(new BitmapDrawable(getResources(), resource));
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_common_app:
                ToolBarListFragment.startRecommendSoftList();
                break;
            case R.id.tv_recent_download:
                ToolBarListFragment.startRecentDownload();
                break;
            case R.id.tv_subjects:
                SubjectRecommendListFragment.start("http://tt.shouji.com.cn/androidv3/special_index_xml.jsp?jse=yes");
                break;
            case R.id.tv_collections:
                CollectionRecommendListFragment.start("http://tt.shouji.com.cn/androidv3/yyj_tj_xml.jsp");
                break;
        }
    }

//    public void getColor(Bitmap bitmap) {
//        // Palette的部分
//        Palette.Builder builder = Palette.from(bitmap);
//        builder.generate(palette -> {
//            //获取到充满活力的这种色调
////                Palette.Swatch s = palette.getMutedSwatch();
//            //获取图片中充满活力的色调
////                Palette.Swatch s = palette.getVibrantSwatch();
//            Palette.Swatch s = palette.getDominantSwatch();//独特的一种
//            Palette.Swatch s1 = palette.getVibrantSwatch();       //获取到充满活力的这种色调
//            Palette.Swatch s2 = palette.getDarkVibrantSwatch();    //获取充满活力的黑
//            Palette.Swatch s3 = palette.getLightVibrantSwatch();   //获取充满活力的亮
//            Palette.Swatch s4 = palette.getMutedSwatch();           //获取柔和的色调
//            Palette.Swatch s5 = palette.getDarkMutedSwatch();      //获取柔和的黑
//            Palette.Swatch s6 = palette.getLightMutedSwatch();    //获取柔和的亮
//            if (s != null) {
//                boolean isDark = ColorUtils.calculateLuminance(s.getRgb()) <= 0.5;
//                ColorChangeEvent.post(isDark);
//            }
//        });
//
//    }

    private static class BannerViewHolder implements MZViewHolder<AppInfo> {
        private ImageView mImageView;

        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.item_banner, null, false);
            mImageView = view.findViewById(R.id.img_view);
            return view;
        }

        @Override
        public void onBind(Context context, int position, AppInfo item) {
            Glide.with(context).load(item.getAppIcon()).into(mImageView);
        }
    }

}
