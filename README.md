# ☕ CoffeeMatch

카페 검색 및 리뷰 관리 서비스

## 📋 프로젝트 소개

**"흩어진 카페 정보를 한곳에 모아, 목적에 맞는 진짜 카페를 찾으세요."**

CoffeeMatch는 네이버 지도, 카카오맵, 블로그 등에 흩어진 카페 정보를 한곳에 모아 비교하고, 사용자들의 **'키워드 투표'**를 통해 목적(카공, 분위기, 맛 등)에 맞는 최적의 카페를 추천하는 서비스입니다.

### 💡 핵심 가치
- **정보 통합**: 여러 플랫폼의 정보를 한눈에 비교하여 정보 불일치 해결
- **키워드 투표**: 단순 별점이 아닌, "조용한", "작업하기 좋은" 등 구체적인 키워드로 카페 특성 파악
- **목적 맞춤 추천**: 사용자의 방문 목적에 딱 맞는 카페를 빠르게 매칭

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **ORM**: JPA/Hibernate

### Frontend
- **Framework**: Vue 3
- **Build Tool**: Vite
- **Styling**: Tailwind CSS 4.x
- **HTTP Client**: Axios

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Database**: MySQL 8.0 (포트 3307)

## 🚀 시작하기

### 사전 요구사항
- Docker & Docker Compose
- Java 17+ (백엔드 실행 시)
- Maven (백엔드 빌드 시)
- Node.js 18+ (프론트엔드 실행 시)

### 1. 데이터베이스 실행

```bash
cd docker
docker-compose up -d
```

### 2. 백엔드 실행

```bash
cd backend
mvn spring-boot:run
```

백엔드는 `http://localhost:8080`에서 실행됩니다.

### 3. 프론트엔드 실행

```bash
cd frontend
npm install  # 최초 1회만 실행
npm run dev
```

프론트엔드는 `http://localhost:5173`에서 실행됩니다.

## 📁 프로젝트 구조

```
coffeematch/
├── backend/                    # Spring Boot 백엔드
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/coffeematch/backend/
│   │       │       ├── controller/      # REST API 컨트롤러
│   │       │       ├── entity/          # JPA 엔티티
│   │       │       ├── repository/      # JPA 리포지토리
│   │       │       └── dto/             # 데이터 전송 객체
│   │       └── resources/
│   │           └── application.properties
│   └── pom.xml
├── frontend/                   # Vue 3 프론트엔드
│   ├── src/
│   │   ├── components/         # Vue 컴포넌트
│   │   │   ├── CafeList.vue   # 카페 목록
│   │   │   └── CafeDetail.vue # 카페 상세
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── tailwind.config.js
├── docker/                     # Docker 설정
│   ├── docker-compose.yml
│   └── init.sql               # 초기 데이터
├── .gitignore
└── README.md
```

## 🎯 주요 기능

### 1. 카페 검색 및 목록 조회
- 키워드로 카페 검색
- 카페 카드 형식의 목록 표시
- 카페 이미지, 이름, 설명, 주소 표시

### 2. 카페 상세 정보
- 카페 상세 정보 조회
- 메뉴 리스트 및 가격 표시
- 추천 메뉴 표시 (⭐ 아이콘)

### 3. 리뷰 관리
- 리뷰 목록 조회
- 리뷰 작성 (작성자, 별점, 내용)
- 별점 시각화 (★☆)

## 🔌 API 엔드포인트

### 카페 관련
- `GET /api/cafes` - 카페 목록 조회 (검색 가능)
  - Query Parameter: `keyword` (선택)
- `GET /api/cafes/{id}` - 카페 상세 조회

### 리뷰 관련
- `POST /api/cafes/{id}/reviews` - 리뷰 작성
  - Request Body: `{ author, rating, content }`

## 💾 데이터베이스 스키마

### Cafe (카페)
- `id` (PK)
- `name` - 카페 이름
- `address` - 주소
- `phone` - 전화번호
- `description` - 설명
- `image_url` - 이미지 URL

### Menu (메뉴)
- `id` (PK)
- `cafe_id` (FK)
- `item_name` - 메뉴 이름
- `price` - 가격
- `is_recommended` - 추천 여부

### Review (리뷰)
- `id` (PK)
- `cafe_id` (FK)
- `author` - 작성자
- `rating` - 별점 (1-5)
- `content` - 내용
- `created_at` - 작성일시

## 🎨 UI/UX 특징

- **현대적인 디자인**: Tailwind CSS를 활용한 깔끔한 UI
- **카페 테마**: 커피 색상 팔레트 (브라운, 베이지 계열)
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 지원
- **직관적인 네비게이션**: 카드 클릭으로 상세 페이지 이동

## 📝 초기 데이터

프로젝트에는 5개의 샘플 카페와 각 카페당 3개의 메뉴가 포함되어 있습니다:
1. Starbrew
2. Espresso Lab
3. Morning Dew
4. Cafe Noir
5. Golden Mug

## 🔧 개발 환경 설정

### CORS 설정
백엔드는 `http://localhost:5173`에서의 요청을 허용하도록 CORS가 설정되어 있습니다.

### 데이터베이스 연결
- Host: `localhost`
- Port: `3307`
- Database: `coffeematch`
- Username: `root`
- Password: `1234`

## 📝 Git 설정

### 원격 저장소 연결
```bash
git remote add origin https://github.com/jae02/coffeematch.git
git branch -M main
git push -u origin main
```

### .gitignore 포함 항목
- Java/Gradle 빌드 파일
- Node.js 의존성 (node_modules)
- 데이터베이스 데이터 파일 (mysql_data)
- IDE 설정 파일
- OS 임시 파일

## 🚀 배포

프로덕션 배포 시:
1. 프론트엔드 빌드: `npm run build`
2. 백엔드 JAR 생성: `mvn clean package`
3. Docker 이미지 생성 및 배포

## 📄 라이선스

MIT License

## 👤 개발자

- GitHub: [@jae02](https://github.com/jae02)

---

**CoffeeMatch** - 당신의 완벽한 카페를 찾아보세요! ☕
