package com.coffeematch.backend.service.impl;

import com.coffeematch.backend.dto.CrawlDataDto;
import java.util.List;

public class NaverBlogCrawlerManualTest {
    public static void main(String[] args) {
        NaverBlogCrawler crawler = new NaverBlogCrawler();
        System.out.println("Starting crawl test...");
        List<CrawlDataDto> results = crawler.crawl("startup", 1);
        System.out.println("Found " + results.size() + " results");
        for (CrawlDataDto dto : results) {
            System.out.println("Title: " + dto.getTitle());
            System.out.println("Cafe: " + dto.getName());
            System.out.println("Link: " + dto.getUrl());
            System.out.println("---");
        }
    }
}
