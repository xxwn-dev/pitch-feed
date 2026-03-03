package com.xxwn.pitchfeed.domain.article.controller;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticlePreviewController {
    private final ArticleRepository articleRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.react-url}")
    private String reactUrl;

    @GetMapping("/{id}/preview")
    public String preview(@PathVariable Long id, Model model){
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        model.addAttribute("article", article);
        model.addAttribute("previewUrl", baseUrl + "/articles/" + id + "/preview");
        model.addAttribute("reactUrl", reactUrl);
        return "article-preview";
    }
}
