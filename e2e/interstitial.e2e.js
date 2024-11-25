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

let InterstitialAd;

describe('googleAds InterstitialAd', function () {
  before(function () {
    InterstitialAd = jet.require('lib/ads/InterstitialAd');
  });

  describe('createForAdRequest', function () {
    it('loads with requestOptions', async function () {
      // Ads on Android in CI load a webview and a bunch of other things so slowly the app ANRs.
      if (device.getPlatform() === 'android' && global.isCI == true) {
        return;
      }

      const spy = sinon.spy();

      const i = InterstitialAd.createForAdRequest(googleAds.TestIds.INTERSTITIAL, {
        requestNonPersonalizedAdsOnly: true,
        networkExtras: {
          foo: 'bar',
        },
        keywords: ['foo', 'bar'],
        contentUrl: 'https://invertase.io',
        location: [0, 0],
        locationAccuracy: 10,
        requestAgent: 'CoolAds',
      });

      i.addAdEventListener(googleAds.AdEventType.LOADED, spy);
      i.load();
      await Utils.spyToBeCalledOnceAsync(spy, 20000);
      i.loaded.should.eql(true);
    });
  });

  describe('addAdEventListener', function () {
    it('unsubscribe should prevent events', async function () {
      // Ads on Android in CI load a webview and a bunch of other things so slowly the app ANRs.
      if (device.getPlatform() === 'android' && global.isCI == true) {
        return;
      }

      const spy = sinon.spy();
      const i = InterstitialAd.createForAdRequest('abc');
      const unsub = i.addAdEventListener(googleAds.AdEventType.LOADED, spy);
      unsub();
      i.load();
      await Utils.sleep(2000);
      spy.callCount.should.be.eql(0);
    });

    it('loads with a valid ad unit id', async function () {
      // Ads on Android in CI load a webview and a bunch of other things so slowly the app ANRs.
      if (device.getPlatform() === 'android' && global.isCI == true) {
        return;
      }

      const spy = sinon.spy();

      const i = InterstitialAd.createForAdRequest(googleAds.TestIds.INTERSTITIAL);

      i.addAdEventListener(googleAds.AdEventType.LOADED, spy);
      i.load();
      await Utils.spyToBeCalledOnceAsync(spy, 20000);
      i.loaded.should.eql(true);
    });

    it('errors with an invalid ad unit id', async function () {
      // Ads on Android in CI load a webview and a bunch of other things so slowly the app ANRs.
      if (device.getPlatform() === 'android' && global.isCI == true) {
        return;
      }

      const spy = sinon.spy();

      const i = InterstitialAd.createForAdRequest('123');

      i.addAdEventListener(googleAds.AdEventType.ERROR, spy);
      i.load();
      await Utils.spyToBeCalledOnceAsync(spy);

      const e = spy.getCall(0).args[0];
      e.code.should.containEql('googleAds/'); // android/ios different errors
    });
  });
});
