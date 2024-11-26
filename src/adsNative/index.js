import React, { Component } from "react";
import {
  findNodeHandle,
  Platform,
  requireNativeComponent,
  UIManager,
} from "react-native";
import { defaultAd, NativeAdContext } from "./context";
import { AdOptions } from "./utils";
import Wrapper from "./Wrapper";

const testNativeAd = {
  headline: "Test Ad: Lorem ipsum dolor ",
  tagline:
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod",
  advertiser: "Laboris Nisi",
  store: Platform.OS === "ios" ? "AppStore" : "Google Play",
  video: false,
  rating: 4.5,
  price: "$1",
  icon: "https://dummyimage.com/300.png/09f/fff",
  images: [{ url: "https://dummyimage.com/qvga" }],
};

const LogOnSlack = async (paramsLog) => {
  const payload = {
    text: JSON.stringify(paramsLog, null, 2),
  };

  await fetch(
    "https://hooks.slack.com/services/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",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  );
};

export class NativeAdView extends Component {
  constructor(props) {
    super(props);
    this.state = {
      nativeAd: defaultAd,
      nativeAdView: null,
    };
    this.nativeAdRef;
    this.currentId = 0;
    this.delayDuration = 0;
    this.componentMounted = false;
    this.ad = defaultAd;
    this.timeload = new Date();
  }

  _onAdFailedToLoad = (event) => {
    if (this.props.onAdFailedToLoad) {
      this.props.onAdFailedToLoad(event.nativeEvent?.error);
    }
  };

  _onAdLoaded = (event) => {
    if (this.props.onAdLoaded) {
      this.props.onAdLoaded(event.nativeEvent);
      LogOnSlack({
        title: "Tải xong quảng cáo",
        time: new Date(),
        interval: new Date() - this.timeload,
      });
    }
  };

  _onAdClicked = (event) => {
    if (this.props.onAdClicked) this.props.onAdClicked(event.nativeEvent);
  };

  _onAdImpression = (event) => {
    if (this.props.onAdImpression) {
      this.props.onAdImpression(event.nativeEvent);
      LogOnSlack({
        title: "Hiển thị xong quảng cáo",
        time: new Date(),
        interval: new Date() - this.timeload,
      });
    }
  };

  _onAdClosed = (event) => {
    if (this.props.onAdClosed) this.props.onAdClosed(event.nativeEvent);
  };

  _onAdOpened = (event) => {
    if (this.props.onAdOpened) this.props.onAdOpened(event.nativeEvent);
  };

  onNativeAdLoaded = (event) => {
    this.ad = event.nativeEvent;
    if (this.ad.aspectRatio) {
      this.ad.aspectRatio = parseFloat(this.ad.aspectRatio);
    }
    if (this.componentMounted) {
      this.updateAd();
      if (this.props.onUnifiedNativeAdLoaded) {
        this.props.onUnifiedNativeAdLoaded(this.ad);
        console.warn(
          "[DEPRECATED] onUnifiedNativeAdLoaded is deprecated and will be removed in future versions. Use onNativeAdLoaded instead."
        );
      }
      if (this.props.onNativeAdLoaded) {
        this.props.onNativeAdLoaded(this.ad);
      }
    }
  };

  _onAdLefApplication = (event) => {
    if (this.props.onAdLeftApplication)
      this.props.onAdLeftApplication(event.nativeEvent);
  };

  updateAd() {
    if (this.componentMounted) {
      this.setState({
        nativeAd: this.ad,
      });
    }
  }

  componentDidMount() {
    try {
      this.componentMounted = true;
      if (this.props.enableTestMode) {
        this.updateAd(testNativeAd);
      } else {
        this.updateAd(this.ad);
      }
    } catch (e) {}
  }

  componentWillUnmount() {
    this.componentMounted = false;
  }

  _getRef = (ref) => {
    this.nativeAdRef = ref;
    return this.nativeAdRef;
  };

  loadAd = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.nativeAdRef),
      UIManager.getViewManagerConfig("RNGADNativeView").Commands.loadAd,
      undefined
    );
    LogOnSlack({
      title: "Bắt đầu tải quảng cáo",
      time: new Date(),
    });
    this.timeload = new Date();
  };

  render() {
    const { nativeAd, nativeAdView } = this.state;
    return (
      <NativeAdContext.Provider value={{ nativeAd, nativeAdView }}>
        <RNGADNativeView
          ref={this._getRef}
          adUnitID={this.props.adUnitID}
          repository={this.props.repository}
          onAdLoaded={this._onAdLoaded}
          onAdFailedToLoad={this._onAdFailedToLoad}
          onAdClicked={this._onAdClicked}
          onAdLeftApplication={this._onAdLefApplication}
          onAdOpened={this._onAdOpened}
          onAdClosed={this._onAdClosed}
          onAdImpression={this._onAdImpression}
          style={this.props.style}
          mediaAspectRatio={
            AdOptions.mediaAspectRatio[this.props.mediaAspectRatio]
          }
          onNativeAdLoaded={this.onNativeAdLoaded}
          requestNonPersonalizedAdsOnly={
            this.props.requestNonPersonalizedAdsOnly
          }
          videoOptions={this.props.videoOptions}
          mediationOptions={this.props.mediationOptions}
          targetingOptions={this.props.targetingOptions}
          adChoicesPlacement={
            AdOptions.adChoicesPlacement[this.props.adChoicesPlacement]
          }
        >
          <Wrapper
            onLayout={(event) => {
              this.setState({
                nativeAdView: this.nativeAdRef,
              });
            }}
          >
            {this.props.children}
          </Wrapper>
        </RNGADNativeView>
      </NativeAdContext.Provider>
    );
  }
}

NativeAdView.defaultProps = {
  mediaAspectRatio: "any",
  adChoicesPlacement: "topRight",
  requestNonPersonalizedAdsOnly: false,
  videoOptions: {
    muted: false,
    clickToExpand: false,
  },
  mediationOptions: {
    nativeBanner: false,
  },
};

NativeAdView.simulatorId = "SIMULATOR";

const RNGADNativeView = requireNativeComponent("RNGADNativeView");

export default NativeAdView;
