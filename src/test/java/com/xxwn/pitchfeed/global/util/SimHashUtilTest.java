package com.xxwn.pitchfeed.global.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SimHashUtilTest {

    private static final Logger log = LoggerFactory.getLogger(SimHashUtilTest.class);

    // ── 임계값 튜닝용 ─────────────────────────────────────────────────────────

    @Test
    void 실제_Hamming_distance_출력() {
        String[][] pairs = {
            {"고우석 퓨처스 강등 결정 kt 위즈", "고우석 퓨처스 강등 확정 kt 위즈"},  // 한 단어 차이
            {"고우석 퓨처스강등 결정",           "고우석 퓨처스 강등 결정"},           // 띄어쓰기 차이
            {"미연 시구 KBO 개막전",             "아이브 미연 KBO 개막전 시구 나서"}, // 같은 이벤트, 다른 표현
            {"고우석 퓨처스 강등 결정",           "삼성 라이온즈 외인 방출 루머"},     // 완전히 다른 이벤트
        };
        for (String[] pair : pairs) {
            int d = SimHashUtil.hammingDistance(SimHashUtil.compute(pair[0]), SimHashUtil.compute(pair[1]));
            log.info("distance={} | \"{}\" ↔ \"{}\"", d, pair[0], pair[1]);
        }
    }

    // ── compute() ───────────────────────────────────────────────────────────

    @Test
    void compute_같은_텍스트는_항상_같은_fingerprint를_반환한다() {
        String title = "고우석 퓨처스 강등 결정";
        assertThat(SimHashUtil.compute(title)).isEqualTo(SimHashUtil.compute(title));
    }

    @Test
    void compute_null_또는_빈_텍스트는_0을_반환한다() {
        assertThat(SimHashUtil.compute(null)).isEqualTo(0L);
        assertThat(SimHashUtil.compute("")).isEqualTo(0L);
        assertThat(SimHashUtil.compute("   ")).isEqualTo(0L);
    }

    @Test
    void compute_완전히_다른_텍스트는_다른_fingerprint를_반환한다() {
        long a = SimHashUtil.compute("고우석 퓨처스 강등");
        long b = SimHashUtil.compute("롯데 자이언츠 홈경기 취소");
        assertThat(a).isNotEqualTo(b);
    }

    // ── hammingDistance() ────────────────────────────────────────────────────

    @Test
    void hammingDistance_같은_hash는_0을_반환한다() {
        long h = SimHashUtil.compute("미연 시구 행사");
        assertThat(SimHashUtil.hammingDistance(h, h)).isEqualTo(0);
    }

    @Test
    void hammingDistance_비슷한_제목은_낮은_거리를_반환한다() {
        // 실제 운영 중 발견된 중복 케이스 유형: 같은 이벤트, 다른 언론사
        long a = SimHashUtil.compute("고우석 퓨처스 강등 결정 kt 위즈");
        long b = SimHashUtil.compute("고우석 퓨처스 강등 확정 kt 위즈");

        int distance = SimHashUtil.hammingDistance(a, b);
        assertThat(distance).isLessThanOrEqualTo(SimHashUtil.DUPLICATE_THRESHOLD);
    }

    @Test
    void hammingDistance_다른_이벤트_제목은_높은_거리를_반환한다() {
        long a = SimHashUtil.compute("고우석 퓨처스 강등 결정");
        long b = SimHashUtil.compute("삼성 라이온즈 외인 방출 루머");

        int distance = SimHashUtil.hammingDistance(a, b);
        assertThat(distance).isGreaterThan(SimHashUtil.DUPLICATE_THRESHOLD);
    }

    @Test
    void hammingDistance_띄어쓰기_차이는_중복으로_판정된다() {
        // 한국어 띄어쓰기 불일치 → 바이그램 기반이므로 robust
        long a = SimHashUtil.compute("고우석 퓨처스강등 결정");
        long b = SimHashUtil.compute("고우석 퓨처스 강등 결정");

        int distance = SimHashUtil.hammingDistance(a, b);
        assertThat(distance).isLessThanOrEqualTo(SimHashUtil.DUPLICATE_THRESHOLD);
    }

    // ── isDuplicateInSet() ───────────────────────────────────────────────────

    @Test
    void isDuplicateInSet_빈_set에서는_중복이_없다() {
        long hash = SimHashUtil.compute("미연 시구 행사 개막식");
        assertThat(SimHashUtil.isDuplicateInSet(hash, Set.of())).isFalse();
    }

    @Test
    void isDuplicateInSet_hash가_0이면_항상_false() {
        assertThat(SimHashUtil.isDuplicateInSet(0L, List.of(0L, 123L))).isFalse();
    }

    @Test
    void isDuplicateInSet_동일_hash는_중복으로_판정된다() {
        long hash = SimHashUtil.compute("미연 시구 행사 개막식");
        assertThat(SimHashUtil.isDuplicateInSet(hash, List.of(hash))).isTrue();
    }

    @Test
    void isDuplicateInSet_유사_제목은_중복으로_판정된다() {
        // 같은 이벤트를 다룬 기사 제목이 set에 이미 있는 상황
        long existing = SimHashUtil.compute("고우석 퓨처스 강등 결정 kt 위즈");
        long incoming = SimHashUtil.compute("고우석 퓨처스 강등 확정 kt 위즈");

        assertThat(SimHashUtil.isDuplicateInSet(incoming, List.of(existing))).isTrue();
    }

    @Test
    void isDuplicateInSet_다른_이벤트는_중복으로_판정되지_않는다() {
        long existing = SimHashUtil.compute("고우석 퓨처스 강등 결정");
        long incoming = SimHashUtil.compute("삼성 라이온즈 외인 방출 루머");

        assertThat(SimHashUtil.isDuplicateInSet(incoming, List.of(existing))).isFalse();
    }

    @Test
    void isDuplicateInSet_여러_기사_중_하나만_유사해도_중복_판정된다() {
        long unrelated1 = SimHashUtil.compute("롯데 자이언츠 홈경기 취소");
        long unrelated2 = SimHashUtil.compute("KIA 타이거즈 우승 기원");
        long similar    = SimHashUtil.compute("고우석 퓨처스 강등 결정 kt 위즈");

        long incoming = SimHashUtil.compute("고우석 퓨처스 강등 확정 kt 위즈");

        assertThat(SimHashUtil.isDuplicateInSet(incoming, List.of(unrelated1, unrelated2, similar))).isTrue();
    }

    @Test
    void isDuplicateInSet_intra_batch_중복_방지_시나리오() {
        // 배치 저장 중 set에 추가하면서 이후 항목을 걸러내는 시나리오
        long first  = SimHashUtil.compute("고우석 퓨처스 강등 결정 kt 위즈");
        long second = SimHashUtil.compute("고우석 퓨처스 강등 확정 kt 위즈");
        long third  = SimHashUtil.compute("롯데 자이언츠 홈경기 취소 우천");

        java.util.Set<Long> saved = new java.util.HashSet<>();

        // 첫 번째는 저장
        assertThat(SimHashUtil.isDuplicateInSet(first, saved)).isFalse();
        saved.add(first);

        // 두 번째는 유사하므로 중복 판정
        assertThat(SimHashUtil.isDuplicateInSet(second, saved)).isTrue();

        // 세 번째는 무관한 기사 → 저장
        assertThat(SimHashUtil.isDuplicateInSet(third, saved)).isFalse();
    }
}
