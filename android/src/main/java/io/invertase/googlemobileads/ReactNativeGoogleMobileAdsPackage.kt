package io.invertase.googlemobileads

/*
 * Copyright (c) 2016-present Invertase Limited & Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this library except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

import com.ammarahmed.rnadmob.nativeads.RNAdmobNativeAdsManager;
import com.ammarahmed.rnadmob.nativeads.RNAdmobNativeViewManager;
import com.ammarahmed.rnadmob.nativeads.RNAdmobMediaViewManager;
import com.ammarahmed.rnadmob.nativeads.RNAdmobComponentsWrapperManager;
import com.ammarahmed.rnadmob.nativeads.RNAdmobAdChoicesManager;
import com.ammarahmed.rnadmob.nativeads.RNAdmobButtonManager;


@SuppressWarnings("unused")
class ReactNativeGoogleMobileAdsPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext) = listOf(
    ReactNativeAppModule(reactContext),
    ReactNativeGoogleMobileAdsModule(reactContext),
    ReactNativeGoogleMobileAdsConsentModule(reactContext),
    ReactNativeGoogleMobileAdsAppOpenModule(reactContext),
    ReactNativeGoogleMobileAdsInterstitialModule(reactContext),
    ReactNativeGoogleMobileAdsRewardedModule(reactContext),
    ReactNativeGoogleMobileAdsRewardedInterstitialModule(reactContext),
    RNAdmobNativeAdsManager(reactContext)
  )

  override fun createViewManagers(
    reactContext: ReactApplicationContext
  ): List<ViewManager<*, *>> {
    return listOf(
      ReactNativeGoogleMobileAdsBannerAdViewManager(),
      RNAdmobNativeViewManager(),
      RNAdmobMediaViewManager(),
      RNAdmobComponentsWrapperManager(),
      RNAdmobAdChoicesManager(),
      RNAdmobButtonManager()
    )
  }
}
