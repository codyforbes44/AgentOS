# AgentOS release build

This produces an Android App Bundle for the Play internal testing track. The repo does not contain a keystore or passwords. If signing material is missing, `bundleRelease` still succeeds and writes an **unsigned** `.aab` (Play will reject that upload until you sign it).

## 1. Create an upload keystore

Run this from your machine. Pick your own store password, key password, and certificate name. Do not commit the `.jks` file.

```bash
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=AgentOS, OU=Mobile, O=Cody Forbes, C=US"
```

`keytool` prompts for the store password and key password. Keep the file outside the repo, or leave it untracked. `.gitignore` already ignores `*.jks`, `*.keystore`, `keystore.properties`, and `.env`.

## 2. Sign locally (optional)

Either export environment variables:

| Variable | Meaning |
| --- | --- |
| `ANDROID_KEYSTORE_FILE` | Path to the `.jks` / `.keystore` file |
| `ANDROID_KEYSTORE_PASSWORD` | Store password |
| `ANDROID_KEY_ALIAS` | Key alias (`upload` if you used the command above) |
| `ANDROID_KEY_PASSWORD` | Key password |

Or create a gitignored `keystore.properties` in the project root (environment variables win when both are set):

```properties
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=your-store-password
keyAlias=upload
keyPassword=your-key-password
```

`storeFile` may be absolute or relative to the project root.

Then:

```bash
./gradlew bundleRelease
```

The bundle is `app/build/outputs/bundle/release/app-release.aab`.

## 3. GitHub Actions secrets

In the repo: **Settings → Secrets and variables → Actions → New repository secret**.

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | `base64 -w 0 upload-keystore.jks` (one line, no spaces) |
| `ANDROID_KEYSTORE_PASSWORD` | Store password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |

If any of the four is missing, the release workflow builds an unsigned bundle instead of failing.

The Gemini API key is not read from these secrets and is not put in the release bundle. To opt in on a local release build, set `INCLUDE_GEMINI_KEY_IN_RELEASE=true` and a real `GEMINI_API_KEY` (environment variable or `.env`). The `.env.example` placeholder is never packaged. The project builds with no `.env` file.

## 4. Run the release workflow

Workflow file: `.github/workflows/android-release.yml`.

- **Manual:** Actions → **Android Release** → **Run workflow**.
- **Tag:** push a tag matching `v*`, for example `git tag v1.0.0 && git push origin v1.0.0`.

The job runs `testDebugUnitTest` and `bundleRelease`, then uploads the `.aab`.

## 5. Download the bundle

Open the workflow run and download the **app-release** artifact (`app-release.aab`). Artifacts are kept for 30 days.

Upload that file in Play Console → your app → **Test and release** → **Internal testing**. The first upload must be signed with the upload key from step 1. Enroll in Play App Signing when prompted.
