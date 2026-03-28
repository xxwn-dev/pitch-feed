# pitch-feed

스포츠 뉴스 RSS를 자동으로 수집하고, AI로 요약하여 Discord로 알림을 보내는 뉴스 큐레이션 서비스입니다.

## 주요 기능

- **RSS 수집**: 등록된 피드 URL에서 야구 뉴스 자동 파싱 (KBO, MLB 등)
- **AI 요약**: Gemini 2.5 Flash를 활용한 기사 요약 및 태그 추출
- **중복 필터**: 같은 날 태그 2개 이상 겹치는 기사 자동 제외 (동일 사건 유사 기사 방지)
- **Discord 알림**: GitHub Actions가 Render로부터 payload를 받아 Discord 웹훅으로 전송
- **기사 미리보기**: Thymeleaf 기반 프리뷰 페이지 (Open Graph 메타태그 포함, 접근 시 React 페이지로 자동 이동)
- **REST API**: 기사 및 피드 조회/추가/삭제

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| AI | Spring AI (OpenAI compatible) |
| DB | PostgreSQL |
| ORM | Spring Data JPA |
| View | Thymeleaf |
| RSS Parser | Rome |
| Deploy | Render |

## 아키텍처

```
[GitHub Actions]
   스케줄 (매일 오후 2시 KST)
        ↓
   POST /api/rss/trigger
        ↓
[RssFetchService]
   RSS 파싱 → URL 중복 체크 → AI 요약/태그 추출(병렬) → 태그 중복 체크 → DB 저장
        ↓
   Discord payload 반환
        ↓
[GitHub Actions]
   Discord 웹훅 전송
```

## API

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/articles` | 기사 목록 조회 (category 필터 가능) |
| GET | `/api/articles/{id}` | 기사 단건 조회 |
| DELETE | `/api/articles/{id}` | 기사 삭제 |
| GET | `/articles/{id}/preview` | 기사 미리보기 페이지 (React 페이지로 자동 이동) |
| GET | `/api/feeds` | 피드 목록 조회 |
| POST | `/api/feeds` | 피드 등록 |
| DELETE | `/api/feeds/{id}` | 피드 삭제 (연결된 기사 함께 삭제) |
| POST | `/api/rss/trigger` | RSS 수집 수동 실행 (X-Batch-Secret 헤더 필요) |

## GitHub Actions

- **batch-trigger.yml**: 매일 오후 2시 KST에 배포된 앱에 수집 실행 요청 후 Discord 알림 전송
  - `RENDER_APP_URL`, `RSS_SECRET`, `DISCORD_WEBHOOK_URL` 시크릿 필요
