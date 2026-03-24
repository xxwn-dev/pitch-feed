package com.xxwn.pitchfeed.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleSummaryService {

    private final ChatClient chatClient;

    public SummaryResult summarize(String title, String content) {
        if(content == null || content.isBlank()) {
            return new SummaryResult(null, null);
        }
        String prompt = """
                  다음 야구 뉴스 기사를 분석해줘.

                  제목: %s
                  내용: %s

                  아래 형식으로만 응답해:
                  요약: (3문장 이내로 핵심 내용 요약)
                  태그: (관련 키워드 3~5개, 콤마로 구분. 예: KBO,한화 이글스,홈런)
                  """.formatted(title, content);
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseResponse(response);
        } catch (Exception e) {
            log.error("AI Summary Failed - Summary: {}, error: {}", title, e.getMessage());
            return new SummaryResult(null, null);
        }
    }

    private SummaryResult parseResponse(String response) {
        String summary = null;
        String tags = null;

        for(String line : response.split("\n")) {
            if(line.startsWith("요약:")) {
                summary = line.replace("요약:", "").trim();
            } else if (line.startsWith("태그:")) {
                tags = line.replace("태그:", "").trim();
            }
        }
        return new SummaryResult(summary, tags);
    }
}
