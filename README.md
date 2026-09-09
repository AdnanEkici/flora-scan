# FloraScan
[![Tests](https://github.com/AdnanEkici/flora-scan/actions/workflows/tests.yml/badge.svg)](https://github.com/AdnanEkici/flora-scan/actions/workflows/tests.yml)
[![Documentation](https://github.com/AdnanEkici/flora-scan/actions/workflows/docs.yml/badge.svg?branch=main&event=push)](https://github.com/AdnanEkici/flora-scan/actions/workflows/docs.yml)


FloraScan is a native Android application that identifies plants from photographs.

Users can capture a plant using the device camera or select an existing image from the gallery, review the image before submission, and receive an identification result containing the plant's common name, scientific name, taxonomy, and confidence score.

The application is implemented in **Java with XML layouts** and uses the **Pl@ntNet API** as its plant-identification provider.

**[APP Documentation](https://adnanekici.github.io/flora-scan/)**
## Features

- Capture a plant image using the device camera
- Select an existing image from the device gallery
- Preview the selected image before identification
- Retake a photograph or choose another image
- Preprocess images before upload
- Correct common EXIF image rotations
- Resize large images while preserving aspect ratio
- Compress images before network transmission
- Identify plants through the Pl@ntNet API
- Display:
    - common name
    - scientific name
    - family
    - genus
    - identification confidence
- Share identification results using the Android share sheet
- Start another identification directly from the result screen
- Handle camera, gallery, preprocessing, network, and identification failures
- Unit and Android integration test coverage
- Java static analysis with Checkstyle and PMD
- Android-specific analysis with Android Lint
- Generated Javadoc documentation


## Application Flow

```text
Splash
  ↓
Home
  ├── Take a Photo
  │       ↓
  │    Camera
  │
  └── Choose from Gallery
          ↓
       Gallery
          ↓
        Preview
          ↓
     Identify Plant
          ↓
        Loading
          ↓
      Pl@ntNet API
          ↓
         Result
```

The preview screen also allows the user to replace the selected image before starting identification.


## Screens

The main application flow consists of five screens:

1. **Splash** — displays FloraScan branding while the application starts.
2. **Home** — allows the user to capture or select a plant image.
3. **Preview** — displays the selected image and allows it to be changed.
4. **Loading** — performs image preparation and plant identification.
5. **Result** — displays identification details and confidence.

### Screenshots

Add final application screenshots here before submission.

```text
docs/screenshots/
├── splash.png
├── home.png
├── preview.png
├── loading.png
└── result.png
```

Example Markdown once screenshots are added:

```markdown
| Home | Preview | Result |
| --- | --- | --- |
| ![Home](docs/screenshots/home.png) | ![Preview](docs/screenshots/preview.png) | ![Result](docs/screenshots/result.png) |
```

## Architecture

FloraScan uses a lightweight layered architecture designed to keep UI logic separate from provider-specific identification logic.

```text
┌─────────────────────────────────────────────┐
│                 UI Layer                    │
│                                             │
│ MainActivity                                │
│ PreviewActivity                             │
│ LoadingActivity                             │
│ ResultActivity                              │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              Repository Layer               │
│                                             │
│ PlantRepository                             │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│               Service Layer                 │
│                                             │
│ PlantIdentifier                             │
│ PlantNetIdentifier                          │
│ PlantNetResultMapper                        │
│ PlantIdentificationCallback                 │
└───────────────┬─────────────────────────────┘
                │
                ├─────────────────────┐
                ▼                     ▼
┌────────────────────────┐  ┌────────────────────────┐
│     Network Layer      │  │      Utility Layer     │
│                        │  │                        │
│ PlantNetApi            │  │ ImagePreprocessor      │
│ RetrofitClient         │  │ CameraImageProvider    │
└────────────┬───────────┘  └────────────────────────┘
             │
             ▼
┌────────────────────────┐
│      Pl@ntNet API      │
└────────────────────────┘
```

### Provider Abstraction

The application does not make the UI depend directly on Pl@ntNet.

Plant identification is defined through:

```java
public interface PlantIdentifier
{
    void identifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback
    );
}
```

`PlantNetIdentifier` implements this interface.

This allows the identification provider to be replaced later without requiring changes throughout the UI layer.

For example:

```text
PlantIdentifier
    ├── PlantNetIdentifier
    ├── FutureBackendIdentifier
    └── FutureOnDeviceIdentifier
```



## Image Processing

Images are processed locally before being uploaded for identification.

`ImagePreprocessor` performs the following operations:

```text
Selected image URI
        ↓
Read image dimensions
        ↓
Downsample large source image
        ↓
Decode Bitmap
        ↓
Read EXIF orientation
        ↓
Correct rotation
        ↓
Resize if necessary
        ↓
Maximum dimension: 1600 px
        ↓
JPEG compression: quality 85
        ↓
Temporary upload file
```

This reduces:

- memory consumption
- upload size
- network bandwidth
- request latency
- unnecessary load on the identification provider

Image preprocessing is executed away from the main UI thread.

Images are passed between activities using `Uri` references rather than large `Bitmap` objects, reducing memory pressure and avoiding large Binder transactions.


## Plant Identification

FloraScan uses the Pl@ntNet identification API through Retrofit.

The current implementation requests the highest-ranked result:

```text
Image
  ↓
ImagePreprocessor
  ↓
Multipart JPEG
  ↓
PlantNetApi
  ↓
Pl@ntNet
  ↓
PlantNetResponse
  ↓
PlantNetResultMapper
  ↓
PlantResult
  ↓
ResultActivity
```

Provider-specific response objects are not exposed directly to the UI.

`PlantNetResultMapper` converts the Pl@ntNet response into the application's provider-independent `PlantResult` domain model.


## Requirements

Development requires:

- Android Studio
- JDK 21 for the Gradle environment
- Android SDK 36
- Android device or emulator
- Internet connection for plant identification
- Pl@ntNet API key

The application currently targets:

```text
Minimum SDK: 24
Target SDK: 36
Compile SDK: 36
Java source compatibility: 11
```


## Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd flora-scan
```


### 2. Configure the Pl@ntNet API Key

Create or update the root-level:

```text
local.properties
```

Add:

```properties
PLANTNET_API_KEY=your_api_key_here
```

Do not commit this file or expose the API key in source code.

The Gradle build reads the value and exposes it to the prototype application through:

```java
BuildConfig.PLANTNET_API_KEY
```


### 3. Open the Project

Open the repository in Android Studio and allow Gradle synchronization to complete.


### 4. Start a Device

Either:

- start an Android emulator through Device Manager, or
- connect a physical Android device with USB debugging enabled.


### 5. Run the Application

Select the `app` run configuration and start the application from Android Studio.


## API Key Security

The current application reads the Pl@ntNet key from `local.properties`, preventing the credential from being committed directly to the repository.

However, the key is ultimately included in the Android application through `BuildConfig`.

This is acceptable for the scope of this prototype but is **not considered secure storage for a production API credential** because values packaged inside a client application can potentially be extracted.

### Production Architecture

A production version should move provider communication behind an application-owned backend:

```text
Android Application
        ↓
FloraScan Backend
        ↓
Plant Identification Provider
```

The backend would be responsible for:

- protecting provider credentials
- authentication
- rate limiting
- caching
- request validation
- retry policies
- provider quotas
- cost controls
- observability
- provider failover
- API versioning

A stateless backend could then be scaled horizontally as request volume increases.

The current client architecture already isolates provider communication behind `PlantIdentifier`, allowing a backend-based implementation to replace `PlantNetIdentifier` without rewriting the UI or domain layers.


## Testing

The project separates JVM unit tests from Android instrumentation tests.

### Unit Tests

Located under:

```text
app/src/test/java/com/example/florascan/unit/
```

Coverage includes:

```text
CameraImageProviderTest
ImagePreprocessorTest
PlantNetResultMapperTest
PlantRepositoryTest
PlantNetIdentifierTest
```

Run all unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```


### Integration Tests

Located under:

```text
app/src/androidTest/java/com/example/florascan/integration/
```

Integration coverage includes:

```text
CameraImageProviderIntegrationTest
PreviewActivityIntegrationTest
ResultActivityIntegrationTest
```

An emulator or physical device must be running.

Run all integration tests:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```


### Run Unit and Integration Tests Together

```powershell
.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest
```


## Manual Verification

Some behavior is intentionally verified manually because it depends on an external API, Android system applications, or network state.

Recommended final verification:

- capture a real plant photograph
- select an existing plant photograph
- cancel camera capture
- cancel gallery selection
- replace the image from the preview screen
- identify a real plant successfully
- test a non-plant image
- test without an internet connection
- test an invalid API credential
- test a large source image
- test a rotated camera image
- verify result sharing
- verify the Identify Another flow

Automated tests do not directly call the production Pl@ntNet API to avoid network-dependent tests, API quota consumption, and flaky CI behavior.


## Static Analysis and Code Quality

The project uses several complementary quality tools.

```text
AStyle
    ↓
Java formatting

Checkstyle
    ↓
Import validation
Visibility rules
Structural conventions

PMD
    ↓
Unused variables
Unused fields
Unused methods
Unused parameters
General Java quality checks

Android Lint
    ↓
Android-specific correctness
Performance
Resources
Platform usage
```

### Checkstyle

Run:

```powershell
.\gradlew.bat :app:checkstyleMain
```

Checkstyle currently validates areas including:

- unused imports
- redundant imports
- wildcard imports
- import ordering
- field visibility


### PMD

Run:

```powershell
.\gradlew.bat :app:pmdMain
```

PMD scans:

```text
src/main/java
src/test/java
src/androidTest/java
```

and checks for unused and suspicious Java code.


### Android Lint

Run:

```powershell
.\gradlew.bat :app:lintDebug
```


## Pre-commit Hooks

The project uses `pre-commit` to run repository and Java quality checks before changes are committed.

Install hooks:

```powershell
pre-commit install
pre-commit install --hook-type pre-push
```

Run all configured hooks manually:

```powershell
pre-commit run --all-files
```

## API Documentation

The project uses Javadoc for generated Java API documentation.

Generate documentation with:

```powershell
.\gradlew.bat :app:generateJavadoc
```

Generated documentation is written to:

```text
app/build/docs/javadoc/
```

Open the generated documentation on Windows:

```powershell
Start-Process .\app\build\docs\javadoc\index.html
```

Javadoc covers the application's primary:

- activities
- domain models
- API response models
- repository
- service abstractions
- Pl@ntNet implementation
- network interface
- image utilities


## Build Verification

Before producing a final build, run:

```powershell
.\gradlew.bat testDebugUnitTest
```

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

```powershell
.\gradlew.bat :app:checkstyleMain
```

```powershell
.\gradlew.bat :app:pmdMain
```

```powershell
.\gradlew.bat :app:lintDebug
```

```powershell
.\gradlew.bat :app:generateJavadoc
```

Then verify a release build:

```powershell
.\gradlew.bat :app:assembleRelease
```

A convenient combined verification command for unit and integration tests is:

```powershell
.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest
```
