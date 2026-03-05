# pitch-feed

스포츠 뉴스 RSS를 자동으로 수집하고, AI로 요약하여 Discord로 알림을 보내는 뉴스 큐레이션 서비스입니다.

## 주요 기능

- **RSS 수집**: 등록된 피드 URL에서 야구 뉴스 자동 파싱 (KBO, MLB 등)
- **AI 요약**: Gemini 2.5 Flash를 활용한 기사 요약 및 태그 추출
- **Discord 알림**: 수집된 새 기사를 Discord 웹훅으로 전송
- **기사 미리보기**: 기사별 Open Graph 이미지 추출 및 Thymeleaf 기반 프리뷰 페이지
- **REST API**: 기사 및 피드 조회/관리

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Batch | Spring Batch |
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
   POST /api/batch/trigger
        ↓
[Spring Batch]
   RSS 파싱 → AI 요약 → DB 저장 → Discord 알림
```

## API

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/articles` | 기사 목록 조회 (category 필터 가능) |
| GET | `/api/articles/{id}` | 기사 단건 조회 |
| GET | `/api/articles/{id}/preview` | 기사 미리보기 페이지 |
| GET | `/api/feeds` | 피드 목록 조회 |
| POST | `/api/feeds` | 피드 등록 |
| DELETE | `/api/feeds/{id}` | 피드 삭제 |
| POST | `/api/batch/trigger` | 배치 수동 실행 (X-Batch-Secret 헤더 필요) |

## GitHub Actions

- **batch-trigger.yml**: 매일 오후 2시 KST에 배포된 앱에 배치 실행 요청
  - `RENDER_APP_URL`, `BATCH_SECRET` 시크릿 필요
