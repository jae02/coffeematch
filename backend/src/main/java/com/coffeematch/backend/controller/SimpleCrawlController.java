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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/simplecrawl")
public class SimpleCrawlController {

    private final CrawlerService naverBlogCrawler;
    private final CsvExportService csvExportService;

    @Autowired
    public SimpleCrawlController(@Qualifier("naverBlogCrawler") CrawlerService naverBlogCrawler,
            CsvExportService csvExportService) {
        this.naverBlogCrawler = naverBlogCrawler;
        this.csvExportService = csvExportService;
    }

    @GetMapping
    public ResponseEntity<Resource> generateSimple(@RequestParam String keyword) {
        try {
            // Debug Probe
            try {
                FileWriter probe = new FileWriter("/tmp/simple_probe.txt");
                probe.write("Probe Success");
                probe.close();
            } catch (Exception e) {
            }

            List<CrawlDataDto> allData = new ArrayList<>();
            // Use Jsoup crawler
            allData.addAll(naverBlogCrawler.crawl(keyword, 5));

            // Generate CSV
            String filePath = "/tmp/simple_crawl_" + System.currentTimeMillis() + ".csv";
            File excelFile = csvExportService.createCsv(allData, filePath);

            Resource resource = new FileSystemResource(excelFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFile.getName())
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (Exception e) {
            try {
                FileWriter fw = new FileWriter("/tmp/simple_error.txt");
                fw.write(e.toString());
                fw.close();
            } catch (Exception ex) {
            }
            return ResponseEntity.internalServerError().build();
        }
    }
}
