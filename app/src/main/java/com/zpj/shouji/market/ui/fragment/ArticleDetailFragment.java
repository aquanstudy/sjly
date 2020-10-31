package com.zpj.shouji.market.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.felix.atoast.library.AToast;
import com.zpj.fragmentation.BaseFragment;
import com.zpj.fragmentation.dialog.imagetrans.ImageItemView;
import com.zpj.fragmentation.dialog.imagetrans.listener.SourceImageViewGet;
import com.zpj.fragmentation.dialog.impl.AttachListDialogFragment;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.constant.Keys;
import com.zpj.shouji.market.event.StartFragmentEvent;
import com.zpj.shouji.market.glide.GlideUtils;
import com.zpj.shouji.market.model.AppInfo;
import com.zpj.shouji.market.model.article.ArticleDetailInfo;
import com.zpj.shouji.market.model.article.ArticleInfo;
import com.zpj.shouji.market.model.article.HtmlElement;
import com.zpj.shouji.market.model.article.ImageElement;
import com.zpj.shouji.market.model.article.LinkElement;
import com.zpj.shouji.market.model.article.TextElement;
import com.zpj.shouji.market.ui.fragment.detail.AppDetailFragment;
import com.zpj.shouji.market.ui.fragment.dialog.CommonImageViewerDialogFragment2;
import com.zpj.shouji.market.ui.widget.DownloadButton;
import com.zpj.shouji.market.ui.widget.selection.SelectableTextView;
import com.zpj.utils.ScreenUtils;
import com.zpj.widget.statelayout.StateLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArticleDetailFragment extends BaseFragment {

    private String url;
    private StateLayout stateLayout;
    private LinearLayout contentWrapper;
    private ArticleDetailInfo articleDetailInfo;
    private AtomicBoolean isEnterAnimationEnd = new AtomicBoolean(false);

    public static void start(String url) {
        Bundle args = new Bundle();
        args.putString(Keys.URL, url);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(args);
        StartFragmentEvent.start(fragment);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_article_detail;
    }

    @Override
    protected boolean supportSwipeBack() {
        return true;
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        lightStatusBar();
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() == null) {
            pop();
            return;
        }
        url = getArguments().getString(Keys.URL);
        setToolbarTitle(url);
        setToolbarSubTitle(url);
        stateLayout = view.findViewById(R.id.state_layout);
        stateLayout.showLoadingView();
        contentWrapper = view.findViewById(R.id.content_wrapper);
        parseHtml(url);
    }

    @Override
    public void toolbarRightImageButton(@NonNull ImageButton imageButton) {
        imageButton.setOnClickListener(v -> {
            new AttachListDialogFragment<String>()
                    .addItem("网页中打开")
                    .addItem("收藏")
                    .setOnSelectListener((fragment, position, title) -> {
                        switch (position) {
                            case 0:
//                                fragment.dismissWithStart(WebFragment.newInstance(url, url));
                                WebFragment.start(url);
                                break;
                            case 1:
                                AToast.warning("TODO");
                                break;
                        }
                        fragment.dismiss();
                    })
                    .setAttachView(imageButton)
                    .show(this);
        });
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        if (articleDetailInfo != null) {
            parseArticleInfo(articleDetailInfo);
        }
        isEnterAnimationEnd.set(true);
    }

    private void parseHtml(final String url) {
        HttpApi.get(url)
                .onSuccess(data -> {
                    articleDetailInfo = ArticleDetailInfo.parse(url.startsWith("https://soft.shouji.com.cn/") ? "soft" : "game", data);
                    if (isEnterAnimationEnd.get()) {
                        parseArticleInfo(articleDetailInfo);
                    }
                })
                .subscribe();
    }

    private synchronized void parseArticleInfo(ArticleDetailInfo info) {
        if (info == null) {
            AToast.error("文章解析失败！即将跳转至网页");
            pop();
            WebFragment.start(url);
            return;
        }
        stateLayout.showContentView();
        Log.d("parseArticleInfo", "parseArticleInfo");
        initHeaderView(info);
        initAppView(info);
        initContent(info);
        initRelateArticle(info);
        initRelateApp(info);
    }

    private void initHeaderView(ArticleDetailInfo info) {
        View header = LayoutInflater.from(context).inflate(R.layout.layout_article_header, null, false);
        TextView tvTitle = header.findViewById(R.id.tv_title);
        TextView tvInfo = header.findViewById(R.id.tv_info);
        setToolbarTitle(info.getTitle());
        tvTitle.setText(info.getTitle());
        tvInfo.setText(info.getArticleInfo());
        contentWrapper.addView(header);
    }

    private void initAppView(ArticleDetailInfo info) {
        AppInfo appInfo = info.getAppInfo();
        if (appInfo != null) {
            View app = LayoutInflater.from(context).inflate(R.layout.item_app_linear, null, false);
            contentWrapper.addView(app);
            app.setBackgroundResource(R.drawable.bg_gray_little);
            app.setOnClickListener(v -> AppDetailFragment.start(appInfo.getAppType(), appInfo.getAppId()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = ScreenUtils.dp2pxInt(context, 20f);
            lp.setMargins(margin, 0, margin, 0);
            app.setLayoutParams(lp);
            Glide.with(context).load(appInfo.getAppIcon()).into((ImageView) app.findViewById(R.id.iv_icon));
            ((TextView) app.findViewById(R.id.tv_title)).setText(appInfo.getAppTitle());
            ((TextView) app.findViewById(R.id.tv_desc)).setText(appInfo.getAppComment());
            TextView i = app.findViewById(R.id.tv_info);
            if (TextUtils.isEmpty(appInfo.getAppInfo())) {
                i.setVisibility(View.GONE);
                ((TextView) app.findViewById(R.id.tv_desc)).setTextColor(getResources().getColor(R.color.color_text_normal));
            } else {
                i.setText(appInfo.getAppInfo());
            }
            DownloadButton downloadButton = app.findViewById(R.id.tv_download);
            downloadButton.bindApp(appInfo);
        }
    }

    private void initContent(ArticleDetailInfo info) {
        for (HtmlElement element : info.getContentElementList()) {
            if (element instanceof LinkElement) {
                SelectableTextView tvText = (SelectableTextView) LayoutInflater.from(context).inflate(R.layout.layout_article_text, null, false);

                LinkElement linkElement = (LinkElement) element;
                SpannableStringBuilder builder = new SpannableStringBuilder(linkElement.getText());
                builder.clearSpans();
                int spanStart = linkElement.getText().indexOf(linkElement.getLinkText());
                builder.setSpan(new URLSpan(linkElement.getLinkUrl()),
                        spanStart,
                        spanStart + linkElement.getLinkText().length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                tvText.setText(builder);
                tvText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (linkElement.getLinkUrl().startsWith("http://sjsoft.wozaiai.cn/down/")) {
                            String id = linkElement.getLinkUrl().replace("http://sjsoft.wozaiai.cn/down/", "").replace(".html", "");
                            AppDetailFragment.start("soft", id);
                        } else {
                            AToast.normal(linkElement.getLinkUrl());
                        }
                    }
                });
                contentWrapper.addView(tvText);
            } else if (element instanceof TextElement) {
                if (TextUtils.isEmpty(((TextElement) element).getText())) {
                    continue;
                }
                SelectableTextView tvText = (SelectableTextView) LayoutInflater.from(context).inflate(R.layout.layout_article_text, null, false);

                tvText.setText("        " + ((TextElement) element).getText());
//                tvText.setText(Html.fromHtml(element.getSourceCode()));
                contentWrapper.addView(tvText);
            } else if (element instanceof ImageElement) {
                String url = ((ImageElement) element).getImageUrl();
                View view = LayoutInflater.from(context).inflate(R.layout.layout_article_image, null, false);
                ImageView ivImage = view.findViewById(R.id.iv_image);
                ivImage.setOnClickListener(v -> {
                    List<String> objects = new ArrayList<>();
                    objects.add(url);
//                    ImageViewer.with(context)
//                            .setImageList(objects)
//                            .setNowIndex(0)
//                            .setSourceImageView(new SourceImageViewGet() {
//                                @Override
//                                public ImageView getImageView(int pos) {
//                                    return ivImage;
//                                }
//                            })
//                            .show();
//                    CommonImageViewerPopup.with(context)
//                            .setImageUrls(objects)
//                            .setSrcView(ivImage, 0)
//                            .setSrcViewUpdateListener((popup, pos) -> {
//                                popup.updateSrcView(ivImage);
//                            })
//                            .show();
                    new CommonImageViewerDialogFragment2()
                            .setNowIndex(0)
                            .setImageList(objects)
                            .setSourceImageView(new SourceImageViewGet<String>() {
                                @Override
                                public void updateImageView(ImageItemView<String> imageItemView, int pos, boolean isCurrent) {
                                    imageItemView.update(ivImage);
                                }
                            })
                            .show(context);
//                    new CommonImageViewerDialogFragment()
//                            .setImageUrls(objects)
//                            .setSrcView(ivImage, 0)
//                            .setSrcViewUpdateListener((popup, pos) -> {
//                                popup.updateSrcView(ivImage);
//                            })
//                            .show(context);
                });
                contentWrapper.addView(view);
                Glide.with(context)
                        .load(url)
                        .apply(GlideUtils.REQUEST_OPTIONS)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                int width = resource.getIntrinsicWidth();
                                int height = resource.getIntrinsicHeight();
                                int margin = ScreenUtils.dp2pxInt(context, 20f);
                                int scaledW = ScreenUtils.getScreenWidth(context) - 2 * margin;
                                int scaledH = (height * scaledW) / width;
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(scaledW, scaledH);
                                lp.setMargins(margin, margin / 2, margin, margin / 2);
                                ivImage.setLayoutParams(lp);
                                ivImage.setImageDrawable(resource);
                            }
                        });

            }
        }
    }

    private void initRelateArticle(ArticleDetailInfo info) {
        if (!info.getRelateArticleList().isEmpty()) {
            View title = LayoutInflater.from(context).inflate(R.layout.item_recommend_header, null, false);
            TextView tvHead = title.findViewById(R.id.tv_title);
            tvHead.setText("相关文章");
            contentWrapper.addView(title);
            for (ArticleInfo articleInfo : info.getRelateArticleList()) {
                SelectableTextView tvText = (SelectableTextView) LayoutInflater.from(context).inflate(R.layout.layout_article_text, null, false);


                tvText.setText(articleInfo.getTitle());
                tvText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArticleDetailFragment.start(articleInfo.getUrl());
                    }
                });
                contentWrapper.addView(tvText);
            }
        }
    }

    private void initRelateApp(ArticleDetailInfo info) {
        if (!info.getRelateAppList().isEmpty()) {
            View title = LayoutInflater.from(context).inflate(R.layout.item_recommend_header, null, false);
            TextView tvHead = title.findViewById(R.id.tv_title);
            tvHead.setText("相关软件");
            contentWrapper.addView(title);
            for (AppInfo app : info.getRelateAppList()) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_app_linear, null, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppDetailFragment.start(app);
                    }
                });
                ImageView ivIcon = view.findViewById(R.id.iv_icon);
                Glide.with(context).load(app.getAppIcon()).into(ivIcon);
                TextView tvName = view.findViewById(R.id.tv_title);
                tvName.setText(app.getAppTitle());
                TextView tvInfo1 = view.findViewById(R.id.tv_info);
                tvInfo1.setText(app.getAppSize());
                TextView tvDesc = view.findViewById(R.id.tv_desc);
                tvDesc.setText(app.getAppComment());
                DownloadButton downloadButton = view.findViewById(R.id.tv_download);
                downloadButton.bindApp(app);
                contentWrapper.addView(view);
            }
        }
    }

}
