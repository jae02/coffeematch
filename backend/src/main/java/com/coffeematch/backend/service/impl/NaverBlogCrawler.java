package com.coffeematch.backend.service.impl;

import com.coffeematch.backend.dto.CrawlDataDto;
import com.coffeematch.backend.service.CrawlerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service("naverBlogCrawler")
public class NaverBlogCrawler implements CrawlerService {

    private static final String NAVER_BLOG_SEARCH_URL = "https://search.naver.com/search.naver?where=blog&query=";

    @Override
    public List<CrawlDataDto> crawl(String keyword, int limit) {
        List<CrawlDataDto> results = new ArrayList<>();
        try {
            String query = URLEncoder.encode(keyword + " 카페", StandardCharsets.UTF_8);
            String url = NAVER_BLOG_SEARCH_URL + query;

            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .get();

            Elements posts = doc.select(".view_wrap");

            for (Element post : posts) {
                if (results.size() >= limit)
                    break;

                try {
                    String title = post.select(".title_link").text();
                    String link = post.select(".title_link").attr("href");
                    String snippet = post.select(".dsc_link").text();
                    String date = post.select(".sub").text();

                    // Image might be in a thumb container
                    String imageUrl = "";
                    Element imgEl = post.select(".thumb img").first();
                    if (imgEl != null) {
                        imageUrl = imgEl.attr("src");
                    }

                    // Attempt to extract Cafe Name from Title (Naive approach: take first 2 words
                    // or just title)
                    // In a real scenario, NLP or specific parsing logic is better.
                    String cafeName = title;

                    results.add(CrawlDataDto.builder()
                            .source("NAVER_BLOG")
                            .name(cafeName) // Blog title often contains the name
                            .title(title)
                            .content(snippet)
                            .url(link)
                            .imageUrl(imageUrl)
                            .build());

                } catch (Exception e) {
                    System.err.println("Error parsing blog post: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
