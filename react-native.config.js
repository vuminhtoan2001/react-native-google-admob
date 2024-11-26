module.exports = {
  dependency: {
    platforms: {
      android: {
        packageImportPath: `import io.invertase.googlemobileads.ReactNativeGoogleMobileAdsPackage;
import com.ammarahmed.rnadmob.nativeads.RNAdMobNativePackage;`,
        packageInstance: `new ReactNativeGoogleMobileAdsPackage(), 
        new RNAdMobNativePackage()
        `,
      },
      ios: {
        scriptPhases: [
          {
            name: '[RNGoogleMobileAds] Configuration',
            path: './ios_config.sh',
            execution_position: 'after_compile',
            input_files: ['$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)'],
          },
        ],
      },
    },
  },
};
