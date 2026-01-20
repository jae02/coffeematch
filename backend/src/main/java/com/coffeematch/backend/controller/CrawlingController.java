package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.CrawlDataDto;
import com.coffeematch.backend.service.CrawlerService;
import com.coffeematch.backend.service.CsvExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/crawl")
public class CrawlingController {

    private final CrawlerService naverBlogCrawler;
    private final com.coffeematch.backend.service.impl.KakaoMapCrawler kakaoMapCrawler;
    private final CsvExportService csvExportService;

    @Autowired
    public CrawlingController(@Qualifier("naverBlogCrawler") CrawlerService naverBlogCrawler,
            @Qualifier("kakaoMapCrawler") com.coffeematch.backend.service.impl.KakaoMapCrawler kakaoMapCrawler,
            CsvExportService csvExportService) {
        this.naverBlogCrawler = naverBlogCrawler;
        this.kakaoMapCrawler = kakaoMapCrawler;
        this.csvExportService = csvExportService;
    }

    @GetMapping("/sample")
    public ResponseEntity<Resource> generateSampleExcel(@RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit) {
        String errorPath = "/tmp/crawl_error.txt";

        try {
            // Debug Probe
            try {
                java.io.FileWriter probe = new java.io.FileWriter("/tmp/probe.txt");
                probe.write("Probe Success");
                probe.close();
            } catch (Exception e) {
            }

            List<CrawlDataDto> allData = new ArrayList<>();
            allData.addAll(naverBlogCrawler.crawl(keyword, limit));

            // Generate dummy data
            // allData.add(new CrawlDataDto("TEST", "Test Cafe", "Addr", "123", "5.0", "10",
            // "9-6", "Url", "Title", "Img",
            // "Content"));

            // 3. Generate CSV
            String filePath = "/tmp/crawled_sample_" + System.currentTimeMillis() + ".csv";
            File excelFile = csvExportService.createCsv(allData, filePath);

            // 4. Return as Download
            Resource resource = new FileSystemResource(excelFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFile.getName())
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Throwable t) {
            try {
                java.io.FileWriter fw = new java.io.FileWriter(errorPath);
                fw.write(t.getClass().getName() + ": " + t.getMessage() + "\n");
                for (StackTraceElement elem : t.getStackTrace()) {
                    fw.write(elem.toString() + "\n");
                }
                fw.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/kakao")
    public ResponseEntity<Resource> crawlKakaoDetail(@RequestParam String keyword,
            @RequestParam(defaultValue = "3") int limit) {

        try {
            List<com.coffeematch.backend.dto.CrawlerCafeDetailDto> details = kakaoMapCrawler.crawlDetail(keyword,
                    limit);

            String filePath = "/tmp/kakao_detail_" + System.currentTimeMillis() + ".csv";
            File csvFile = csvExportService.createDetailCsv(details, filePath);

            Resource resource = new FileSystemResource(csvFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFile.getName())
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
