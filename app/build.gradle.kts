import com.android.build.api.variant.BuildConfigField
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.File
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

// Placeholder values that must never be treated as a real Gemini key.
val geminiKeyPlaceholders =
    setOf(
        "MY_GEMINI_API_KEY",
        "YOUR_GEMINI_API_KEY",
        "your_gemini_api_key",
    )

fun readDotEnv(key: String): String? {
  val envFile = rootProject.file(".env")
  if (!envFile.exists()) return null
  val properties = Properties()
  envFile.reader().use { properties.load(it) }
  return properties.getProperty(key)?.trim()?.removeSurrounding("\"")?.takeIf { it.isNotEmpty() }
}

fun configuredGeminiKey(): String? {
  val fromEnv =
      System.getenv("GEMINI_API_KEY")?.trim()?.removeSurrounding("\"")?.takeIf { it.isNotEmpty() }
  val value = fromEnv ?: readDotEnv("GEMINI_API_KEY")
  if (value.isNullOrBlank() || value in geminiKeyPlaceholders) return null
  return value
}

fun String.toBuildConfigStringLiteral(): String {
  val escaped =
      replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n")
  return "\"$escaped\""
}

val includeGeminiKeyInRelease =
    System.getenv("INCLUDE_GEMINI_KEY_IN_RELEASE")?.equals("true", ignoreCase = true) == true

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
  keystorePropertiesFile.reader().use { keystoreProperties.load(it) }
}

fun signingSetting(envName: String, propertyName: String): String? {
  val fromEnv = System.getenv(envName)?.takeIf { it.isNotBlank() }
  if (fromEnv != null) return fromEnv
  return keystoreProperties.getProperty(propertyName)?.takeIf { it.isNotBlank() }
}

val releaseStorePath = signingSetting("ANDROID_KEYSTORE_FILE", "storeFile")
val releaseStorePassword = signingSetting("ANDROID_KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingSetting("ANDROID_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingSetting("ANDROID_KEY_PASSWORD", "keyPassword")

val releaseStoreFile: File? =
    releaseStorePath?.let { path ->
      val candidate = File(path)
      val resolved = if (candidate.isAbsolute) candidate else rootProject.file(path)
      resolved.takeIf { it.isFile }
    }

val releaseSigningReady =
    releaseStoreFile != null &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

if (releaseSigningReady) {
  logger.lifecycle("Release signing configured from environment variables or keystore.properties.")
} else {
  logger.lifecycle(
      "Release signing material not found. bundleRelease will produce an unsigned bundle."
  )
}

android {
  namespace = "com.codyforbes.agentos"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.codyforbes.agentos"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    if (releaseSigningReady) {
      create("release") {
        storeFile = releaseStoreFile
        storePassword = releaseStorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      isDebuggable = false
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro",
      )
      if (releaseSigningReady) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

// The secrets plugin is told to ignore GEMINI_API_KEY (below). This is the only path that
// copies a real key into the release BuildConfig, and only when the opt-in is explicit.
androidComponents {
  onVariants(selector().withBuildType("release")) { variant ->
    if (!includeGeminiKeyInRelease) return@onVariants
    val key = configuredGeminiKey()
    if (key == null) {
      logger.warn(
          "INCLUDE_GEMINI_KEY_IN_RELEASE=true but GEMINI_API_KEY is missing or still a placeholder. " +
              "The release bundle will not contain a Gemini key."
      )
      return@onVariants
    }
    variant.buildConfigFields?.put(
        "GEMINI_API_KEY",
        BuildConfigField(
            "String",
            key.toBuildConfigStringLiteral(),
            "Opted in with INCLUDE_GEMINI_KEY_IN_RELEASE",
        ),
    )
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  // GEMINI_API_KEY must not be copied into BuildConfig or manifest placeholders.
  // A release build includes it only when INCLUDE_GEMINI_KEY_IN_RELEASE=true.
  ignoreList.add("GEMINI_API_KEY")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Firebase Auth with Google Sign-In requires all of the following to be uncommented together.
  // If you are using Firebase Auth with other providers (e.g. Email/Password), you may only need
  // firebase-auth.
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
