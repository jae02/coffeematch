package com.coffeematch.backend.service;

import com.coffeematch.backend.dto.CrawlDataDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
public class CsvExportService {

    public File createCsv(List<CrawlDataDto> dataList, String fileName) throws IOException {
        File file = new File(fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header
            writer.println("Source,Name,Address,Phone,Rating,ReviewCount,BizHour,Title,URL,Content,ImageURL");

            // Data
            for (CrawlDataDto data : dataList) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escape(data.getSource()),
                        escape(data.getName()),
                        escape(data.getAddress()),
                        escape(data.getPhone()),
                        escape(data.getRating()),
                        escape(data.getReviewCount()),
                        escape(data.getBizHour()),
                        escape(data.getTitle()),
                        escape(data.getUrl()),
                        escape(data.getContent()),
                        escape(data.getImageUrl()));
            }
        }
        return file;
    }

    private String escape(String input) {
        if (input == null)
            return "";
        return "\"" + input.replace("\"", "\"\"").replace("\n", " ") + "\"";
    }

    public File createDetailCsv(List<com.coffeematch.backend.dto.CrawlerCafeDetailDto> dataList, String fileName)
            throws IOException {
        File file = new File(fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header
            writer.println("CafeName,Address,Category,BizHour,CafeURL,Reviewer,Rating,Date,Content,ReviewImageURL");

            // Data
            for (com.coffeematch.backend.dto.CrawlerCafeDetailDto cafe : dataList) {
                if (cafe.getReviews() == null || cafe.getReviews().isEmpty()) {
                    // Write cafe info even if no reviews
                    writer.printf("%s,%s,%s,%s,%s,,,,,%n",
                            escape(cafe.getName()),
                            escape(cafe.getAddress()),
                            escape(cafe.getCategory()),
                            escape(cafe.getBusinessHours()),
                            escape(cafe.getUrl()));
                } else {
                    for (com.coffeematch.backend.dto.ReviewDetailDto review : cafe.getReviews()) {
                        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                                escape(cafe.getName()),
                                escape(cafe.getAddress()),
                                escape(cafe.getCategory()),
                                escape(cafe.getBusinessHours()),
                                escape(cafe.getUrl()),
                                escape(review.getNickname()),
                                escape(review.getRating()),
                                escape(review.getDate()),
                                escape(review.getContent()),
                                escape(review.getImageUrl()));
                    }
                }
            }
        }
        return file;
    }
}
