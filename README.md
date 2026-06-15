# 아빠 용돈 — Daddy Pocket

> **4050 아빠들을 위한 쉽고 간편한, 복잡하지 않은 용돈·카드 지출 정리 가계부 앱**

복잡한 자산 연동이나 어지러운 차트 대신, 아빠들이 가장 필요로 하는 **이번 달 남은 내 용돈**과 **다가오는 카드 결제일**만 직관적이고 깔끔하게 챙겨 주는 로컬 가계부 애플리케이션입니다.

---

<p align="center">
  <img src="docs/assets/feature-graphic.png" alt="아빠 용돈 Key Visual" width="700">
</p>

<p align="center">
  <a href="https://jeiel85.github.io/daddy-easy-pocket-money/">🌐 랜딩 페이지</a> ·
  <a href="https://jeiel85.github.io/daddy-easy-pocket-money/privacy.html">🔒 개인정보처리방침</a>
</p>

---

## 💵 핵심 가치 & 브랜딩

- **직관성 최우선 (Simple by Design)**: 복잡한 기능 없이, 이번 달 사용할 수 있는 남은 용돈을 메인 화면에 큼직하게 보여주어 즉각적인 지출 모니터링을 돕습니다.
- **철저한 개인정보 보호 (100% Local-First)**: 지출 내역, 신용카드 정보 등 모든 데이터는 외부 클라우드 서버로 송신되지 않고 오직 기기 내부의 보안 로컬 데이터베이스(Room/SQLite)에만 안전하게 저장됩니다.
- **놓치지 않는 카드 결제일 알림 (Payment Reminder)**: 등록한 카드 결제일 3일 전부터 홈 화면 경고 배너와 시스템 푸시 알림으로 미리 알려 연체를 예방합니다.

---

## ✨ 주요 기능

| 화면 | 기능 |
|---|---|
| **홈** | 이번 달 예산 대비 남은 용돈·오늘 지출·상위 카테고리 한눈에 보기, 카드 결제일 경고 배너 |
| **지출대장** | 금액·카테고리·결제수단(현금/체크/신용/이체)만 빠르게 입력, 월별·카테고리·카드별 필터 |
| **고정비** | 통신비·구독료·보험 등 매월 반복 지출 등록 시 매월 자동으로 지출대장에 반영(중복 방지) |
| **카드정리** | 카드별 결제일 등록 및 이번 달 카드 사용액 집계 |
| **설정/분석** | 월 예산 변경, 카테고리별 지출 분석, CSV 내보내기 |

---

## 🛠️ 기술 스택

| 영역 | 선택 기술 |
|---|---|
| **언어 (Language)** | Kotlin |
| **UI 프레임워크** | Jetpack Compose, Material 3 |
| **아키텍처 (Architecture)** | MVVM |
| **로컬 데이터베이스** | Room Database (SQLite) |
| **비동기 처리** | Kotlin Coroutines & Flow |
| **빌드 구성** | Gradle Kotlin DSL + 버전 카탈로그 (`libs.versions.toml`) |
| **의존성 주입** | 수동 DI (Manual DI) |

---

## 📦 저장소 구조

```text
daddy-easy-pocket-money/
  app/
    src/main/
      java/com/jeiel85/daddypocket/
        data/         # Room Database, DAO, Entity, Repository 데이터 레이어
        ui/
          screens/    # 홈, 지출대장, 고정비, 카드정리, 설정/분석, 지출 입력 화면
          theme/      # 디자인 시스템 (Color / Theme / Type)
          AppViewModel.kt   # 애플리케이션 상태 관리 및 알림 로직
      res/            # 리소스 및 런처 아이콘
  docs/               # 개인정보처리방침 및 랜딩 페이지 자산 (GitHub Pages 호스팅)
  store-graphics/     # Google Play 스토어 등록용 그래픽 및 소개글
```

---

## 🚀 빠른 시작 (Quick Start)

### 빌드 및 실행 요구조건

- **JDK**: 17 이상
- **Android SDK**: Platform 36
- **minSdk**: 24 / **targetSdk**: 36

### 빌드 방법

**Windows PowerShell**:
```powershell
.\gradlew.bat compileDebugSources   # 컴파일 검증
.\gradlew.bat testDebugUnitTest     # 유닛 테스트
.\gradlew.bat assembleDebug         # 디버그 APK
```

**Linux / macOS / CI**:
```bash
./gradlew compileDebugSources
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

---

## 🔐 릴리즈 빌드 및 서명 정책

릴리즈 AAB는 로컬 업로드 키 `.keystore/my-upload-key.jks`(저장소 푸시 제외)로 서명합니다. 필요 시 아래 환경 변수로 키스토어 경로·비밀번호를 주입할 수 있습니다.

- `KEYSTORE_PATH` · `STORE_PASSWORD` · `KEY_PASSWORD`

```bash
# 서명된 릴리즈 AAB 빌드
./gradlew bundleRelease

# Play Console 제출용 AAB + 다국어 릴리즈 노트를 바탕화면 Build 폴더로 내보내기
./gradlew :app:exportReleaseToDesktop
```

---

## 📄 라이선스 / 저작권

© 2026 Sitdory. All Rights Reserved.
