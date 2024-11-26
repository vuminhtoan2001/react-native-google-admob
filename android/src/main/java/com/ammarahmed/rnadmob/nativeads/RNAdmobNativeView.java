package com.ammarahmed.rnadmob.nativeads;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

import android.util.Log;
import io.invertase.googlemobileads.R;

public class RNAdmobNativeView extends LinearLayout {

    private final Runnable measureAndLayout = () -> {
        measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
        layout(getLeft(), getTop(), getRight(), getBottom());
    };
    public int adRefreshInterval = 60000;
    protected @Nullable
    ReactContext mContext;
    NativeAdView nativeAdView;
    NativeAd nativeAd;
    VideoOptions.Builder videoOptions;
    AdManagerAdRequest.Builder adRequest;
    NativeAdOptions.Builder adOptions;
    AdLoader.Builder builder;
    AdLoader adLoader;
    RNAdmobMediaView mediaView;
    RNAdMobUnifiedAdContainer unifiedNativeAdContainer;
    CatalystInstance mCatalystInstance;
    private int mediaAspectRatio = 1;
    private boolean loadingAd = false;
    private Runnable retryRunnable;
    private String adRepo;
    private int adChoicesPlacement = 1;
    private boolean requestNonPersonalizedAdsOnly = false;
    private String admobAdUnitId = "";
    private Handler handler;

    AdListener adListener = new AdListener() {
        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            WritableMap event = Arguments.createMap();
            WritableMap error = Arguments.createMap();
            error.putString("message", loadAdError.getMessage());
            error.putInt("code", loadAdError.getCode());
            error.putString("domain", loadAdError.getDomain());
            event.putMap("error", error);
            loadingAd = false;
            if (adRepo != null) {
                 CacheManager.instance.detachAdListener(adRepo,adListener);
            }
            sendEvent(RNAdmobNativeViewManager.EVENT_AD_FAILED_TO_LOAD, event);
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            sendEvent(RNAdmobNativeViewManager.EVENT_AD_CLOSED, null);
        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();
            sendEvent(RNAdmobNativeViewManager.EVENT_AD_OPENED, null);
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
            sendEvent(RNAdmobNativeViewManager.EVENT_AD_CLICKED, null);

        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            if (adRepo != null) {
                CacheManager.instance.detachAdListener(adRepo,adListener);
                loadAd();
            }
            loadingAd = false;
            sendEvent(RNAdmobNativeViewManager.EVENT_AD_LOADED, null);
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            sendEvent(RNAdmobNativeViewManager.EVENT_AD_IMPRESSION, null);
        }
    };
    NativeAd.OnNativeAdLoadedListener onNativeAdLoadedListener = new NativeAd.OnNativeAdLoadedListener() {
        @Override
        public void onNativeAdLoaded(NativeAd ad) {
            if (nativeAd != null) {
                nativeAd.destroy();
            }
            loadingAd = false;
            setNativeAdToJS(ad);
        }
    };

    public RNAdmobNativeView(ReactContext context) {
        super(context);
        mContext = context;
        createView(context);
        handler = new Handler();
        mCatalystInstance = mContext.getCatalystInstance();
        setId(UUID.randomUUID().hashCode() + this.getId());
        videoOptions = new VideoOptions.Builder();
        adRequest = new AdManagerAdRequest.Builder();
        adOptions = new NativeAdOptions.Builder();

    }

    public void createView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewRoot = layoutInflater.inflate(R.layout.rn_ad_unified_native_ad, this, true);
        nativeAdView = (NativeAdView) viewRoot.findViewById(R.id.native_ad_view);

    }

    public String getAdRepo() {
        return adRepo;
    }

    public void addMediaView(int id) {

        try {
            mediaView = nativeAdView.findViewById(id);
            if (mediaView != null) {
                Objects.requireNonNull(nativeAd.getMediaContent()).getVideoController().setVideoLifecycleCallbacks(mediaView.videoLifecycleCallbacks);
                nativeAdView.setMediaView(nativeAdView.findViewById(id));
                mediaView.requestLayout();
                setNativeAd();

            }
        } catch (Exception e) {

        }
    }

    private Method getDeclaredMethod(Object obj, String name) {
        try {
            return obj.getClass().getDeclaredMethod(name);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setNativeAdToJS(NativeAd ad) {
        try {
            nativeAd = ad;
            WritableMap args = Arguments.createMap();
            args.putString("headline", nativeAd.getHeadline());
            args.putString("tagline", nativeAd.getBody());
            args.putString("advertiser", nativeAd.getAdvertiser());
            args.putString("callToAction", nativeAd.getCallToAction());
            args.putBoolean("video", Objects.requireNonNull(nativeAd.getMediaContent()).hasVideoContent());

            if (nativeAd.getPrice() != null) {
                args.putString("price", nativeAd.getPrice());
            }

            if (nativeAd.getStore() != null) {
                args.putString("store", nativeAd.getStore());
            }
            if (nativeAd.getStarRating() != null) {
                args.putInt("rating", nativeAd.getStarRating().intValue());
            }

            float aspectRatio = 1.0f;

            MediaContent mediaContent = nativeAd.getMediaContent();
            if (nativeAdView.getMediaView() != null) {
                nativeAdView.getMediaView().setMediaContent(nativeAd.getMediaContent());
            }


            if (mediaContent != null && null != getDeclaredMethod(mediaContent, "getAspectRatio")) {
                aspectRatio = nativeAd.getMediaContent().getAspectRatio();
                if (aspectRatio > 0) {
                    args.putString("aspectRatio", String.valueOf(aspectRatio));
                } else {
                    args.putString("aspectRatio", String.valueOf(1.0f));
                }

            } else {
                args.putString("aspectRatio", String.valueOf(1.0f));
            }


            WritableArray images = new WritableNativeArray();

            if (!nativeAd.getImages().isEmpty()) {

                for (int i = 0; i < nativeAd.getImages().size(); i++) {
                    WritableMap map = Arguments.createMap();
                    if (nativeAd.getImages().get(i) != null) {
                        map.putString("url", Objects.requireNonNull(nativeAd.getImages().get(i).getUri()).toString());
                        map.putInt("width", 0);
                        map.putInt("height", 0);
                        images.pushMap(map);
                    }
                }
            }

            args.putArray("images", images);

            if (nativeAd.getIcon() != null) {
                if (nativeAd.getIcon().getUri() != null) {
                    args.putString("icon", nativeAd.getIcon().getUri().toString());
                } else {
                    args.putString("icon", "empty");
                }
            } else {
                args.putString("icon", "noicon");
            }

            sendEvent(RNAdmobNativeViewManager.EVENT_NATIVE_AD_LOADED,args);
            setNativeAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loadingAd = false;
    }

    public void sendEvent(String name, @Nullable WritableMap event) {

        ReactContext reactContext = (ReactContext) mContext;
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                name,
                event);
    }

    public void loadAd() {
         Log.d("AdMobDeviceId", "adRepo: " + adRepo);
        if (adRepo != null) {
            getAdFromRepository();
        } else {
            try {
                if (loadingAd) return;
                loadingAd = true;
                adLoader.loadAd(adRequest.build());

            } catch (Exception e) {
                loadingAd = false;
            }
        }

    }

    private void getAdFromRepository() {
        try {
            if (!CacheManager.instance.isRegistered(adRepo)) {
                if (adListener != null)
                    adListener.onAdFailedToLoad(new LoadAdError(3, "The requested repo is not registered", "", null, null));
            } else {
                CacheManager.instance.attachAdListener(adRepo, adListener);
                if (CacheManager.instance.numberOfAds(adRepo) != 0) {
                    unifiedNativeAdContainer = CacheManager.instance.getNativeAd(adRepo);

                    if (unifiedNativeAdContainer != null) {
                        nativeAd = unifiedNativeAdContainer.unifiedNativeAd;
                        if (mediaView != null) {
                            nativeAdView.setMediaView(mediaView);
                            mediaView.requestLayout();
                        }
                        setNativeAdToJS(nativeAd);
                    }
                } else {
                    if (!CacheManager.instance.isLoading(adRepo)) {
                        CacheManager.instance.requestAds(adRepo);
                    }
                }
            }

        } catch (Exception e) {
            adListener.onAdFailedToLoad(new LoadAdError(3, e.toString(), "", null, null));
        }
    }

    private void resetView(View view) {
        if (view.getParent() != null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }

    public void addNewView(View child, int index) {
        try {
            resetView(child);
            nativeAdView.addView(child, index);
            requestLayout();
            nativeAdView.requestLayout();
        } catch (Exception e) {

        }
    }

    @Override
    public void addView(View child) {
        resetView(child);
        super.addView(child);
        requestLayout();
    }

    public void setAdRefreshInterval(int interval) {
        adRefreshInterval = interval;
    }

    public void setAdRepository(String repo) {
        adRepo = repo;
    }

    public void setAdUnitId(String id) {
        admobAdUnitId = id;
        if (id == null) return;
        loadAdBuilder();
    }

    public void loadAdBuilder() {
        builder = new AdLoader.Builder(mContext, admobAdUnitId);
        builder.forNativeAd(onNativeAdLoadedListener);
        builder.withNativeAdOptions(adOptions.build());
        adLoader = builder.withAdListener(adListener)
                .build();
    }

    public void setAdChoicesPlacement(int location) {
        adChoicesPlacement = location;
        adOptions.setAdChoicesPlacement(adChoicesPlacement);
    }

    public void setRequestNonPersonalizedAdsOnly(boolean npa) {
        Utils.setRequestNonPersonalizedAdsOnly(npa, adRequest);
    }

    public void setMediaAspectRatio(int type) {
        mediaAspectRatio = type;
        adOptions.setMediaAspectRatio(mediaAspectRatio);
    }
    public void setSwipeGestureOptions(int direction, boolean tapsAllowed) {
        adOptions.enableCustomClickGestureDirection(direction, tapsAllowed);
    }

    public void setNativeAd() {
        try {
            
            if (nativeAdView != null && nativeAd != null) {
                nativeAdView.setNativeAd(nativeAd);

                if (nativeAdView.getMediaView() != null) {
                    nativeAdView.getMediaView().setMediaContent(nativeAd.getMediaContent());
                    if (Objects.requireNonNull(nativeAd.getMediaContent()).hasVideoContent()) {
                        mediaView.setVideoController(nativeAd.getMediaContent().getVideoController());
                        mediaView.setMedia(nativeAd.getMediaContent());
                    }
                }
                handler.postDelayed(() -> {
                    nativeAdView.getRootView().requestLayout();
                }, 1000);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void setVideoOptions(ReadableMap options) {
        Utils.setVideoOptions(options, videoOptions, adOptions);
    }

    public void setTargetingOptions(ReadableMap options) {
        Utils.setTargetingOptions(options, adRequest);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    public void removeHandler() {
        loadingAd = false;
        if (handler != null) {
            handler = null;
        }
    }
}
