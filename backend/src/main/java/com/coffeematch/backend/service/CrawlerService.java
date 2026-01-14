package com.coffeematch.backend.service;

import com.coffeematch.backend.dto.CrawlDataDto;
import java.util.List;

public interface CrawlerService {
    List<CrawlDataDto> crawl(String keyword, int limit);
}
