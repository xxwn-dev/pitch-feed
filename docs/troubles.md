# Trouble Shooting

## 2026-03-20

### 1. 포트 8080 충돌
- **증상**: 로컬 서버 실행 시 `Port 8080 was already in use`
- **원인**: Docker 컨테이너 `ticket-app`이 8080 점유 중
- **해결**: `docker stop ticket-app`

---

### 2. 비로그인 상태에서 삭제 버튼 클릭 시 /login 미이동
- **증상**: 삭제 버튼 클릭 시 로그인 페이지로 이동하지 않음
- **원인 1**: axios 인터셉터가 401만 처리 → Spring Security가 403 반환하는 경우 미처리
  - **해결**: 인터셉터에서 401, 403 모두 처리하도록 수정
- **원인 2**: Spring Security `formLogin`이 기본 활성화되어 있어 401/403 대신 302 redirect를 반환 → axios 에러 인터셉터를 우회해 브라우저가 직접 이동
  - **해결**: `SecurityConfig`에 `.formLogin(disable)`, `.httpBasic(disable)`, 커스텀 `authenticationEntryPoint`(401 반환) 추가
- **원인 3**: CORS 설정이 Spring Security 필터보다 늦게 적용되어 preflight 차단
  - **해결**: `WebConfig`에 `CorsConfigurationSource` 빈 등록 → Spring Security가 MVC보다 먼저 CORS 처리

---

### 3. axios 에러 인터셉터 console.log 미출력
- **증상**: `[auth interceptor]` 로그가 콘솔에 안 보임
- **원인**: `window.location.href = '/login'` 실행으로 페이지 이동 시 콘솔이 초기화됨
- **해결**: 정상 동작. DevTools Console의 **Preserve log** 옵션 활성화 시 확인 가능

---

### 4. GET /api/feeds 두 번 호출
- **증상**: /feeds 접속 시 네트워크 탭에서 API 요청이 2회 발생
- **원인**: React `StrictMode`가 개발 환경에서 `useEffect`를 의도적으로 2회 실행
- **해결**: 프로덕션 빌드에서는 1회만 실행되므로 정상. 개발 중 무시해도 무방

---

### 5. Article not found 500 에러
- **증상**: 존재하지 않는 기사 ID 요청 시 500 Internal Server Error 발생
- **원인**: `ArticleService.getArticle()`에서 `RuntimeException`을 던져 500으로 처리됨
- **해결**: `ResponseStatusException(HttpStatus.NOT_FOUND)`으로 변경 → 404 반환

---

### 6. Discord 알림 미수신
- **증상**: GitHub Actions 배치 트리거 성공, 기사 저장 정상, Discord 알림 미수신
- **원인**: Render 무료 티어 서버 IP를 Cloudflare가 차단 (429 error code 1015)
  - Render 무료 티어는 IP를 여러 고객과 공유하며, 타 사용자의 남용으로 IP 자체가 차단될 수 있음
  - 이틀 이상 경과해도 차단 해제되지 않음 (단순 일시 차단이 아닌 지속 차단)
  - 웹훅 URL을 새로 생성해도 동일 IP이므로 해결 안 됨
- **해결**: Discord 호출을 Render에서 GitHub Actions로 이동
  - Render는 RSS 수집·AI 요약·DB 저장만 담당, Discord payload를 JSON으로 반환
  - GitHub Actions가 payload를 받아 Discord webhook 직접 호출 (GitHub IP는 차단 대상 아님)
  - 로컬 curl 테스트로 로컬 IP는 정상(204), Render IP만 차단임을 사전 확인

---

## 2026-03-23

### 10. @WebMvcTest에서 Spring Security 403 Forbidden
- **증상**: `AuthControllerTest` 전체가 기대 상태코드(200/401) 대신 403 반환
- **원인 1**: `@MockBean` deprecated (Spring Boot 3.4+)
  - **해결**: `org.springframework.boot.test.mock.mockito.MockBean` → `org.springframework.test.context.bean.override.mockito.MockitoBean` 으로 교체
- **원인 2**: `@WebMvcTest`는 `SecurityConfig`를 자동 로드하지 않아 Spring 기본 보안(CSRF 활성화)이 적용됨
  - **해결**: 테스트에 `@Import(SecurityConfig.class)` 추가
- **원인 3**: `JwtAuthenticationFilter` mock이 `chain.doFilter()`를 호출하지 않아 필터 체인이 끊김
  - **해결**: `@MockitoBean JwtAuthenticationFilter` 추가 후 `@BeforeEach`에서 `doAnswer`로 체인 위임 설정
    ```java
    doAnswer(inv -> {
        ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
        return null;
    }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    ```

---

## 2026-03-24

### 11. GitHub Actions curl timeout — AI 직렬 호출로 인한 배치 시간 초과

- **증상**: batch-trigger workflow가 지속적으로 실패 (`--max-time` 초과)
- **원인**: `RssFetchService`에서 기사별 AI 요약 호출이 순차적으로 실행됨
  - 피드 1개 × 기사 5건 × AI 응답 ~30초 = 총 ~150초 (Render 콜드 스타트 포함 시 ~250초+)
- **해결**: AI 호출을 `ExecutorService.invokeAll()` + Virtual Thread로 병렬화
  - URL 중복 필터링(1단계) → AI 병렬 호출(2단계) → 순차 저장(3단계)으로 분리
  - 5건이 동시 실행되어 가장 느린 1건만 기다리면 됨 → 총 시간 대폭 감소
- **추가**: `SummaryResult`를 `ArticleSummaryService` 내부 record에서 별도 클래스로 분리

---

## 2026-04-21

### 13. Neon 무료 티어 100 CU-hour 소진

- **증상**: Neon으로부터 "pitch-feed 프로젝트가 월 100 CU-hour 컴퓨트 허용량의 100%를 사용했다"는 알림 수신. DB 연결 실패로 앱 동작 불가
- **원인**: HikariCP 기본 설정(`minimum-idle=10`)이 항상 idle 커넥션을 유지 → Neon compute가 auto-suspend되지 못하고 24/7 활성 상태 유지
  - Neon free tier: 100 CU-hours/월, 24/7 활성 시 ~720 CU-hours/월 소모
  - pitch-feed(Render)와 bb-rules-bot(Oracle Cloud)이 동일한 Neon compute endpoint(`ep-winter-forest-a1mjc5u4`) 공유 → 둘 중 하나라도 커넥션을 유지하면 suspend 불가
  - bb-rules-bot은 이미 `-pooler` URL 사용 중이었으나 pitch-feed는 미사용
- **해결**: 두 프로젝트 모두 `application.yaml`에 HikariCP 설정 추가
  ```yaml
  spring:
    datasource:
      hikari:
        minimum-idle: 0      # idle 커넥션 0 → Neon auto-suspend 허용
        maximum-pool-size: 5
        idle-timeout: 10000  # 10초 후 idle 커넥션 반환
        connection-timeout: 30000
  ```
  - 두 프로젝트 모두 이미 pooler URL(`-pooler.`) 사용 중이었으므로 DB_URL 변경 불필요
    - pooler(PgBouncer)는 트랜잭션이 없을 때 실제 DB 커넥션을 반환 → `minimum-idle: 0`과 이중으로 작용
  - bb-rules-bot은 Discord 봇 특성상 질문이 없는 유휴 시간에 커넥션 반환 → Neon suspend → 멘션 시 콜드스타트 500ms~1s 추가되나 RAG+LLM 응답 시간에 묻혀 체감 없음

---

## 2026-04-07

### 12. Gradle integrationTest — No matching tests found

- **증상**: `./gradlew integrationTest` 실행 시 `No matching tests found` + `The tag 'integration' is both included and excluded`
- **원인**: `tasks.withType<Test>`는 프로젝트 내 **모든** Test 타입 태스크에 일괄 적용된다. `integrationTest`도 Test 타입이므로 여기에 포함되어, 같은 태그 `"integration"`에 대해 exclude(withType에서)와 include(integrationTest 자체 설정에서)가 동시에 걸렸다. Gradle은 충돌 시 exclude를 우선 적용하므로 결과적으로 모든 integration 태그 테스트가 제외됐다.
- **해결**: `tasks.withType<Test>` → `tasks.named<Test>("test")`로 교체. 기본 `test` 태스크에만 exclude를 적용하고 `integrationTest`는 영향받지 않도록 분리. 아울러 `integrationTest` 태스크에 `testClassesDirs`와 `classpath`를 명시적으로 지정해 컴파일된 테스트 클래스를 확실히 참조하도록 수정.
  ```kotlin
  // 변경 전
  tasks.withType<Test> {
      useJUnitPlatform { excludeTags("integration") }
  }

  // 변경 후
  tasks.named<Test>("test") {
      useJUnitPlatform { excludeTags("integration") }
  }
  tasks.register<Test>("integrationTest") {
      testClassesDirs = sourceSets["test"].output.classesDirs
      classpath = sourceSets["test"].runtimeClasspath
      useJUnitPlatform { includeTags("integration") }
  }
  ```

---

## 2026-03-22

### 7. GitHub Actions curl timeout (exit code 28)
- **증상**: batch-trigger workflow 실행 시 `Process completed with exit code 28`
- **원인**: `--max-time 180` 설정이 실제 실행 시간보다 짧음
  - Render 콜드 스타트 ~100초 + AI 요약 (5건 × ~30초) ~150초 = 총 ~250초
- **해결**: `--max-time 360`으로 증가

---

### 8. Discord webhook invalid JSON (error code 50109)
- **증상**: Discord 전송 시 `{"message": "The request body contains invalid JSON.", "code": 50109}`
- **원인**: GitHub Actions outputs의 `echo "key=value"` 방식으로 JSON 전달 시 한글·이모지(`⚾`, `📋`) 등 특수문자가 깨짐
- **해결**: curl `-o` 옵션으로 payload를 파일(`/tmp/discord_payload.json`)에 저장 후 `-d @file`로 전송
  - `echo` 방식은 단순 문자열에만 안전, JSON 같은 복잡한 데이터는 파일 경유가 표준

---

### 9. jq parse error (exit code 5)
- **증상**: `jq: parse error: Invalid numeric literal at EOF at line 1, column 9`
- **원인**: Render 재배포 완료 전 workflow를 수동 실행하여 구 코드(`"Triggered"` 문자열)가 응답됨
  - jq는 파일 전체가 유효한 JSON이어야 동작하므로 비JSON 응답 시 즉시 실패
- **해결**:
  - jq 대신 `grep -q '"embeds"'`로 payload 유효성 체크 (파싱 불필요, 문자열 탐색만)
  - Render 재배포 완료("Deploy succeeded") 확인 후 workflow 실행
