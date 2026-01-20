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
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1")
                    .get();

            // Try multiple container selectors
            Elements posts = doc.select("li.bx, .view_wrap, .detail_box");

            for (Element post : posts) {
                if (results.size() >= limit)
                    break;

                try {
                    // Try multiple title selectors
                    Element titleEl = post.select(".title_link, .total_tit, .tit, .api_txt_lines").first();
                    if (titleEl == null)
                        continue;

                    String title = titleEl.text();
                    String link = titleEl.attr("href");

                    // If href is empty on the text element, try finding parent 'a' or 'a' inside
                    if (link.isEmpty()) {
                        if (titleEl.tagName().equals("a")) {
                            link = titleEl.attr("href");
                        } else {
                            Element parentLink = titleEl.parent();
                            if (parentLink != null && parentLink.tagName().equals("a")) {
                                link = parentLink.attr("href");
                            }
                        }
                    }

                    // Fallback: search for any 'a' tag in the post
                    if (link.isEmpty()) {
                        Element anyLink = post.select("a").first();
                        if (anyLink != null)
                            link = anyLink.attr("href");
                    }

                    // Skip invalid results (UI elements like 'Sort', 'Filter')
                    if (link.isEmpty() || title.length() < 3 || link.equals("#"))
                        continue;

                    // Try multiple snippet selectors
                    String snippet = post.select(".dsc_link, .dsc_txt, .api_txt_lines.dsc_txt, .total_dsc").text();

                    // Image
                    String imageUrl = "";
                    Element imgEl = post.select(".thumb img, .thumb_area img").first();
                    if (imgEl != null) {
                        imageUrl = imgEl.attr("src");
                    }

                    String cafeName = title;

                    results.add(CrawlDataDto.builder()
                            .source("NAVER_BLOG")
                            .name(cafeName)
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
