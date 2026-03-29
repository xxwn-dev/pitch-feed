package com.xxwn.pitchfeed.ai;

import com.xxwn.pitchfeed.ai.service.ArticleSummaryService;
import com.xxwn.pitchfeed.ai.service.SummaryResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Disabled("로컬 수동 테스트 전용")
public class ArticleSummaryServiceTest {

    @Autowired
    private ArticleSummaryService summaryService;

    @Test
    void KBO_기사는_요약된다() {
        String title = "강백호, 한화 이적 후 첫 홈런…키움전 3회 투런포";
        String content = "프로야구 한화 이글스의 강백호 선수가 키움 히어로즈와의 경기 3회에 투런 홈런을 기록했다. KBO리그 개막 후 첫 홈런이다.";

        SummaryResult result = summaryService.summarize(title, content, "KBO 뉴스");

        log.info("skip: {}", result.skip());
        log.info("summary: {}", result.summary());
        log.info("tags: {}", result.tags());
        assertThat(result.skip()).isFalse();
        assertThat(result.summary()).isNotBlank();
    }

    @Test
    void MLB_기사는_스킵된다() {
        String title = "이정후, 2루타로 시즌 첫 안타…김혜성은 트리플A서 5안타";
        String content = "미국프로야구 메이저리그(MLB) 샌프란시스코 자이언츠 소속 이정후가 2026시즌 첫 안타를 2루타로 기록했다.";

        SummaryResult result = summaryService.summarize(title, content, "KBO 뉴스");

        log.info("skip: {}", result.skip());
        assertThat(result.skip()).isTrue();
    }
}
