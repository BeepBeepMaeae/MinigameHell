# MiniGameHell (미니게임지옥)

Android용 미니게임 모음 앱으로, 카드 게임·랜덤 퀴즈·반응 속도 테스트 3종의 미니게임을 제공합니다.  
사용자 닉네임·프로필 이미지 설정, 배경음·효과음 볼륨 조정, Firebase 연동 랭킹 저장, 외부 앱(갤러리·공유) 연동 등을 지원합니다.

---

## 📌 주요 기능

### 1. 게임 목록 & 네비게이션
- **MainActivity**:  
  - 앱 시작, 설정 화면 진입  
  - 프로필 이미지 실시간 반영 (`fragmentResultListener`) :contentReference[oaicite:0]{index=0}
- **GameSelectActivity**:  
  - ViewPager2 + `GamePagerAdapter` 로 3종 게임 슬라이드 표시 :contentReference[oaicite:1]{index=1}  
  - 게임 선택 시 BGM 유지 재생  

### 2. 게임 콘텐츠
| 게임명           | Activity / Fragment                   | 설명                                                         |
| ---------------- | ------------------------------------- | ------------------------------------------------------------ |
| 카드 게임        | `CardGameActivity`                   | Deck of Cards API 이용<br>21 최대 점수, 5회 기회, 22 이상 시 버스트 처리 및 0점 :contentReference[oaicite:2]{index=2} |
| 랜덤 퀴즈        | `RandomQuizActivity`                 | Open Trivia DB API 이용<br>5문제 랜덤, 타이머(15초), 빠른 정답 시 보너스 점수 :contentReference[oaicite:3]{index=3} |
| 반응 속도 테스트 | `ReactionTestActivity`               | 5회 라운드, 지연 무작위 지점 클릭 시간 측정, 평균 반응속도 기반 점수 산출 :contentReference[oaicite:4]{index=4} |

### 3. 공통 지원 기능
- **설정** (`SettingsFragment`):  
  - 닉네임/프로필 이미지 변경 (`SharedPrefManager`) :contentReference[oaicite:5]{index=5}  
  - 배경음·효과음 볼륨 실시간 조정 (`BgmManager`, `SoundEffectManager`)   
- **결과 & 랭킹**  
  - `GameResultFragment` 을 통한 점수 확인·재시도·랭킹 조회·공유 :contentReference[oaicite:6]{index=6}  
  - `RankingFragment` 에서 Firebase Firestore 연동 5위 랭킹 표시 :contentReference[oaicite:7]{index=7}  
  - 점수 업로드: `FirebaseManager.uploadScore()` :contentReference[oaicite:8]{index=8}

### 4. 기술 스택
- Kotlin + AndroidX  
- Jetpack ViewPager2, Fragment  
- Coroutines (API 호출 & Firebase)  
- Retrofit2 + Gson (Trivia & Deck of Cards API)  
- Firebase Firestore (랭킹 저장)  
- Glide (이미지 로딩)  

---

## 🚧 미구현 / 진행 중
- **Machine Learning** 모델 기반 사용자 표정 인식 → 문제 난이도 조정 `(진행 중)` :contentReference[oaicite:9]{index=9}

---

## ⚙️ 설치 및 실행

1. 이 저장소를 클론하고 Android Studio로 열기  
2. `app/build.gradle`에서 필요한 종속성 확인  
3. 디바이스 또는 에뮬레이터에서 실행  

---

## 디렉토리 구조

com/example/minigame/
├─ activities/
│ ├─ MainActivity.kt
│ ├─ GameSelectActivity.kt
│ ├─ CardGameActivity.kt
│ ├─ RandomQuizActivity.kt
│ └─ ReactionTestActivity.kt
├─ fragments/
│ ├─ SettingsFragment.kt
│ ├─ GameResultFragment.kt
│ ├─ RankingFragment.kt
│ └─ PauseMenuFragment.kt
├─ data/
│ ├─ GameInfo.kt
│ ├─ Question.kt
│ └─ TriviaModels.kt
├─ net/
│ ├─ TriviaAPI.kt
│ └─ DeckOfCardsApi.kt
├─ util/
│ ├─ SharedPrefManager.kt
│ ├─ FirebaseManager.kt
│ ├─ BgmManager.kt
│ └─ SoundEffectManager.kt
└─ adapter/
└─ GamePagerAdapter.kt

---

## 라이선스
MIT License

Copyright (c) 2025 이천서, 김동현
...
