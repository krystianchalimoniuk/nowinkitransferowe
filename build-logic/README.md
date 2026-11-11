# Convention Plugins

The `build-logic` folder defines the convention plugins of Nowinki Transferowe. They keep one
source of truth for how modules are configured, so the `build.gradle.kts` of a module only lists
the plugins it needs and its own dependencies.

The setup follows [Now in Android](https://github.com/android/nowinandroid), which is based on
[Herding Elephants](https://developer.squareup.com/blog/herding-elephants/) and
[idiomatic-gradle](https://github.com/jjohannes/idiomatic-gradle).

`build-logic` is an included build, configured in the root
[`settings.gradle.kts`](../settings.gradle.kts). Its `convention` module defines the plugins, and
the Kotlin files in [`pl/nowinkitransferowe/convention`](convention/src/main/kotlin/pl/nowinkitransferowe/convention)
hold the logic the plugins share (Android and Kotlin options, Compose, flavors, Jacoco, badging,
Spotless, module graphs).

The plugins are additive and each does one thing, so a module combines the ones it needs. Logic
used by a single module belongs in that module's `build.gradle.kts`, not in a new plugin.

All plugins are declared in the [version catalog](../gradle/libs.versions.toml) and applied by
alias, for example `alias(libs.plugins.nowinkitransferowe.android.library)`.

## Plugins

### Modules

- [`nowinkitransferowe.android.application`](convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt):
  the `app` module. Android and Kotlin options, target SDK, lint, Spotless, Dependency Guard,
  badging tasks, Gradle managed devices and test APK printing.
- [`nowinkitransferowe.android.library`](convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt):
  Android library modules. Android and Kotlin options, the `demo` and `prod` flavors, a resource
  prefix from the module path (for example `feature_news_impl_`), lint and Spotless.
- [`nowinkitransferowe.jvm.library`](convention/src/main/kotlin/JvmLibraryConventionPlugin.kt):
  pure Kotlin modules such as `core:datastore-proto`.
- [`nowinkitransferowe.android.test`](convention/src/main/kotlin/AndroidTestConventionPlugin.kt):
  test-only modules such as `benchmarks`.

### Features

- [`nowinkitransferowe.android.feature.api`](convention/src/main/kotlin/AndroidFeatureApiConventionPlugin.kt):
  `feature:*:api` modules with the Navigation 3 keys of a feature. Adds Kotlin serialization and
  `core:navigation`.
- [`nowinkitransferowe.android.feature.impl`](convention/src/main/kotlin/AndroidFeatureImplConventionPlugin.kt):
  `feature:*:impl` modules with screens, view models and entry providers. Adds Hilt,
  `core:ui`, `core:designsystem`, Lifecycle, Hilt view models and Navigation 3.

### Capabilities

- [`nowinkitransferowe.android.application.compose`](convention/src/main/kotlin/AndroidApplicationComposeConventionPlugin.kt),
  [`nowinkitransferowe.android.library.compose`](convention/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt):
  Jetpack Compose with the Compose BOM and compiler options.
- [`nowinkitransferowe.android.application.flavors`](convention/src/main/kotlin/AndroidApplicationFlavorsConventionPlugin.kt):
  the `demo` (local JSON data) and `prod` (nowinkitransferowe.pl) flavors.
- [`nowinkitransferowe.android.application.firebase`](convention/src/main/kotlin/AndroidApplicationFirebaseConventionPlugin.kt):
  Firebase Analytics, Performance and Crashlytics for the app.
- [`nowinkitransferowe.hilt`](convention/src/main/kotlin/HiltConventionPlugin.kt):
  Hilt with KSP.
- [`nowinkitransferowe.android.room`](convention/src/main/kotlin/AndroidRoomConventionPlugin.kt):
  Room with KSP and exported schemas.
- [`nowinkitransferowe.android.application.jacoco`](convention/src/main/kotlin/AndroidApplicationJacocoConventionPlugin.kt),
  [`nowinkitransferowe.android.library.jacoco`](convention/src/main/kotlin/AndroidLibraryJacocoConventionPlugin.kt):
  Jacoco coverage reports for unit and instrumented tests.
- [`nowinkitransferowe.android.lint`](convention/src/main/kotlin/AndroidLintConventionPlugin.kt):
  shared lint settings, also for JVM modules.

### Root project

- [`nowinkitransferowe.root`](convention/src/main/kotlin/RootPlugin.kt):
  applied in the root `build.gradle.kts`. Configures Spotless for the root files and registers the
  `graphUpdate` tasks that draw the module graphs in each module's `README.md`. The graph tasks
  read other projects, so they run with
  `./gradlew graphUpdate -Dorg.gradle.isolated-projects=false`.
