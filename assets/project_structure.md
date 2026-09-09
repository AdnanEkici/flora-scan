app/
└── src/
└── main/
├── java/
│   └── com/
│       └── example/
│           └── florascan/
│               │
│               ├── ui/
│               │   ├── SplashActivity.java
│               │   ├── MainActivity.java
│               │   ├── PreviewActivity.java
│               │   ├── LoadingActivity.java
│               │   └── ResultActivity.java
│               │
│               ├── repository/
│               │   └── PlantRepository.java
│               │
│               ├── service/
│               │   ├── PlantIdentifier.java
│               │   └── PlantNetIdentifier.java
│               │
│               ├── network/
│               │   ├── PlantNetApi.java
│               │   └── RetrofitClient.java
│               │
│               ├── model/
│               │   ├── PlantResponse.java
│               │   └── PlantResult.java
│               │
│               └── utils/
│                   ├── ImagePreprocessor.java
│                   └── CameraImageProvider.java
│
├── res/
│   ├── drawable/
│   │   ├── application_logo.png
│   │   ├── splash_screenbackground.png
│   │   ├── main_screenbackground.png
│   │   ├── home_banner.jpg
│   │   ├── preview_screenbackground.png
│   │   ├── loading_screenbackground.png
│   │   ├── result_screenbackground.png
│   │   ├── ic_camera.xml
│   │   ├── ic_gallery.xml
│   │   ├── ic_arrow_right.xml
│   │   ├── ic_back.xml
│   │   ├── ic_leaf.xml
│   │   └── ...
│   │
│   ├── layout/
│   │   ├── activity_splash.xml
│   │   ├── activity_main.xml
│   │   ├── activity_preview.xml
│   │   ├── activity_loading.xml
│   │   └── activity_result.xml
│   │
│   ├── mipmap/
│   ├── values/
│   └── xml/
│
└── AndroidManifest.xml
