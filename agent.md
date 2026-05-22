# Agent Guide: KMP Template

Welcome, AI Agent! This guide outlines the tech stack, build commands, and architectural patterns of this Kotlin Multiplatform (KMP) repository. Refer to this file to maintain codebase consistency, use correct build tools, and implement changes according to the project's design.

---

## 🚀 Tech Stack & Core Libraries

- **Kotlin**: `2.3.21`
- **Gradle**: `9.4.1` (requires **JVM 17 or higher** to run locally)
- **Compose Multiplatform**: `1.11.0`
- **Navigation**: JetBrains **Navigation 3** (`1.1.1`)
- **Adaptive Layout**: JetBrains Compose Material 3 Adaptive (`1.3.0-alpha05`)
- **Dependency Injection**: Koin (`4.2.1`)
- **Networking**: Ktor Client (`3.4.3`)
- **Image Loading**: Coil 3 (`3.4.0`)

---

## 🛠️ Core Commands

Always use the Gradle wrapper (`./gradlew`) to build and verify changes.

### Build & Verification Commands
- **Assemble All Targets**: `./gradlew assemble`
- **Build Android Debug App**: `./gradlew :androidApp:assembleDebug`
- **Compile iOS Simulator Library**: `./gradlew :shared:compileKotlinIosSimulatorArm64`
- **Clean Build Directories**: `./gradlew clean`

### Local JVM Requirement
The project wrapper uses Gradle 9.4.1. If you encounter an error stating that Gradle requires JVM 17 or later, verify that your active shell or IDE Gradle JDK is configured to use **JDK 17** or **JDK 24** (both of which are installed on this macOS workstation).

---

## 🧭 Navigation 3 & Adaptive Architecture

The application has been fully migrated to **Jetpack Navigation 3** with a responsive, multi-pane **Material 3 Adaptive Scaffold**.

### Rules for Adding Routes / Destinations
1. **Marker Interface**: All routes/destinations must be `@Serializable` classes or objects and implement the `NavKey` interface from `androidx.navigation3.runtime.NavKey`.
   ```kotlin
   @Serializable
   data object MyDestination : NavKey

   @Serializable
   data class MyParamDestination(val id: String) : NavKey
   ```

2. **Polymorphic Serialization**: Because reflection-based serialization is unsupported on native targets (like iOS), any new route **must** be explicitly registered inside the `SavedStateConfiguration` polymorphic block within [App.kt](shared/src/commonMain/kotlin/com/jetbrains/kmpapp/App.kt):
   ```kotlin
   private val savedStateConfig = SavedStateConfiguration {
       serializersModule = SerializersModule {
           polymorphic(NavKey::class) {
               subclass(ListDestination::class, ListDestination.serializer())
               subclass(DetailDestination::class, DetailDestination.serializer())
               subclass(MyDestination::class, MyDestination.serializer()) // Add here!
           }
       }
   }
   ```

3. **Adaptive Panes**:
   - The project uses `rememberListDetailSceneStrategy<NavKey>()` for side-by-side adaptive layouts.
   - Use `ListDetailSceneStrategy.listPane()` and `ListDetailSceneStrategy.detailPane()` metadata to map routes to their respective adaptive panes inside `entryProvider`.
   - Remember to apply the `@OptIn(ExperimentalMaterial3AdaptiveApi::class)` annotation to composables calling these APIs.

---

## 🤖 Continuous Integration (CI)

A parallelized **GitHub Actions pipeline** is configured in [.github/workflows/ci.yml](.github/workflows/ci.yml):
- **`build-android`**: Compiles the Android target on a fast Linux (`ubuntu-latest`) runner.
- **`build-ios`**: Compiles the iOS simulator framework on a macOS (`macos-latest`) runner.
- Both use JDK 21 and utilize Gradle setup caching for optimal performance.
