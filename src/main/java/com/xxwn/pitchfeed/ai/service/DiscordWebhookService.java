package com.xxwn.pitchfeed.ai.service;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordWebhookService {

    private final RestClient restClient;

    @Value("${discord.webhook-url}")
    private String webhookUrl;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.react-url}")
    private String reactUrl;

    public void send(List<Article> articles) {
        if(articles.isEmpty()) return;

        List<Article> limited = articles.size() > 25
                ? articles.subList(0,25)
                : articles;

        List<Article> preview = limited.size() > 3
                ? limited.subList(0,3)
                : limited;

        List<Map<String, Object>> fields = new ArrayList<>();

        preview.forEach(article -> {
            Map<String, Object> field = new LinkedHashMap<>();
            field.put("name", article.getTitle());
            field.put("value", "%s\n[자세히 보기](%s/articles/%d/preview)".formatted(
                    article.getSummary() != null ? article.getSummary() : "",
                    baseUrl,
                    article.getId()
            ));
            field.put("inline", false);
            fields.add(field);
        });

        if(limited.size() > 3){
            Map<String, Object> moreField = new LinkedHashMap<>();
            moreField.put("name", "📋 더보기");
            moreField.put("value", "[전체 %d건 보기](%s)".formatted(limited.size(),
                    reactUrl));
            moreField.put("inline", false);
            fields.add(moreField);
        }
        Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("title", "⚾ 새로운 야구 뉴스 %d건".formatted(limited.size()));
        embed.put("color", 0x1a73e8);
        embed.put("fields", fields);

        Map<String, Object> body = Map.of("embeds", List.of(embed));

        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Discord 전송 실패", e);
        }

    }
}
