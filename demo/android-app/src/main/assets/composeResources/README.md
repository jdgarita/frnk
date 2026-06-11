# Compose-resources workaround assets (POC)

These XML files duplicate the bundled Compose-resource drawables that the **adaptive-nav-bar** bottom-bar
engine (POC, see `:shared-ui-nav`) needs on Android.

**Why they live here.** Under the current toolchain (AGP 9.2.1 `com.android.kotlin.multiplatform.library`
+ Compose Multiplatform 1.11.1), the Compose-resources Gradle plugin does **not** package
`DrawableResource`s from a `shared-*` KMP **library** module into the Android APK
(`copyAndroidMainComposeResourcesToAndroidAssets` fails — `outputDirectory` is unconfigured;
`prepareComposeResourcesTaskForAndroidMain` is `NO-SOURCE`). The drawables assemble for iOS but are
absent from Android assets, so switching to the adaptive-nav-bar engine crashes with
`MissingResourceException`.

As a POC workaround, the **application** module ships the same files at the exact asset path the generated
`Res` reads from, so the runtime `AssetManager` resolves them:

- `dev.jdgarita.frnk.ui.bottomnav.generated.resources/drawable/` — toolkit nav icons
  (home/settings/primary-action), copied from `frnk/ui/bottom-nav/src/commonMain/composeResources/drawable/`.
- `dev.jdgarita.frnk.demo.generated.resources/drawable/` — the demo's Components tab icon, copied from
  `demo/shared/src/commonMain/composeResources/drawable/`.

**This is a hack for the POC only.** Keep these in sync with the source drawables if they change. The
proper fix is an AGP/Compose-resources update that packages library Compose resources for the KMP-Android
variant; until then this is a strike against adopting the adaptive-nav-bar engine in the toolkit. The Calf
engine and the `ImageVector`-based atoms have no such requirement.
