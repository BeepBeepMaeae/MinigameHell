# MiniGameHell (미니게임지옥)

Android용 미니게임 모음 앱으로, 카드 게임·랜덤 퀴즈·반응 속도 테스트 3종의 미니게임을 제공합니다.  
사용자 닉네임·프로필 이미지 설정, 배경음·효과음 볼륨 조정, Firebase 연동 랭킹 저장, 외부 앱(갤러리·공유) 연동 등을 지원합니다.

---

## 📌 주요 기능

### 1. 게임 목록 & 네비게이션
- **MainActivity**  
  - 앱 시작, 설정 화면 진입  
  - 프로필 이미지 실시간 반영 (`fragmentResultListener`)
- **GameSelectActivity**  
  - ViewPager2 + `GamePagerAdapter` 로 3종 게임 슬라이드 표시  
  - 게임 선택 시 BGM 끊김 없이 유지 재생  

### 2. 게임 콘텐츠
| 게임명           | Activity / Fragment          | 설명                                                                                                                                                             |
|--------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 블랙 잭         | `CardGameActivity`          | Deck of Cards API 이용<br>21 최대 점수, 5회 기회, 22 이상 시 버스트 처리 및 0점                                                                                    |
| 랜덤 퀴즈        | `RandomQuizActivity`        | Open Trivia DB API 이용<br>10문제 랜덤, 타이머(15초)<br>**표정 인식 기반 난이도 조정**<br>• 웃음: 오답 1개 제거 + 타이머 +15초<br>• 슬픔/화남: 타이머 +5초                  |
| 반응 속도 테스트   | `ReactionTestActivity`      | 5회 라운드 수행<br>기본 점수 200점, 반응 지연 0.01초당 1점 차감<br>너무 빨리 누르면 해당 라운드 점수 0점 처리<br>2초 이상 반응 시 음수 점수 허용<br>5회 라운드 합산이 최종 점수                      |

### 3. 공통 지원 기능
- 닉네임/프로필 이미지 변경 (`SharedPrefManager`)
- 배경음·효과음 볼륨 실시간 조정 (`BgmManager`, `SoundEffectManager`)
- Machine Learning 모델 기반 얼굴 표정 인식 → 난이도 조정
- `GameResultFragment` 을 통한 점수 확인·재시도·랭킹 조회·공유
- `RankingFragment` 에서 Firebase Firestore 연동 5위 랭킹 표
- 점수 업로드: `FirebaseManager.uploadScore()`

### 4. 기술 스택
- Kotlin + AndroidX  
- Jetpack ViewPager2, Fragment  
- Coroutines (API 호출 & Firebase)  
- Retrofit2 + Gson (Trivia & Deck of Cards API)  
- Firebase Firestore (랭킹 저장)  
- Glide (이미지 로딩)  
- TensorFlow Lite (얼굴 표정 인식 모델)

---

## ⚙️ 설치 및 실행

1. 이 저장소를 클론하고 Android Studio로 열기  
2. `app/build.gradle`에서 필요한 종속성 확인  
3. 디바이스 또는 에뮬레이터에서 실행  

---

## 라이선스
MIT License  
Copyright (c) 2025 이천서, 김동현  
