package com.coffeematch.backend.service.impl;

import com.coffeematch.backend.dto.CrawlDataDto;
import com.coffeematch.backend.service.CrawlerService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service("kakaoMapCrawler")
public class KakaoMapCrawler implements CrawlerService {

    private static final String KAKAO_MAP_URL = "https://map.kakao.com/";

    @Override
    public List<CrawlDataDto> crawl(String keyword, int limit) {
        List<CrawlDataDto> results = new ArrayList<>();

        // Setup WebDriver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(KAKAO_MAP_URL);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // Search
            WebElement searchBox = driver.findElement(By.id("search.keyword.query"));
            searchBox.sendKeys(keyword + " 카페");
            driver.findElement(By.id("search.keyword.submit")).click();

            Thread.sleep(2000); // Wait for results

            // Determine if more results button exists, but for now just scrape first page
            List<WebElement> items = driver
                    .findElements(By.cssSelector("#info\\.search\\.place\\.list > li.PlaceItem"));

            for (WebElement item : items) {
                if (results.size() >= limit)
                    break;

                try {
                    String name = item.findElement(By.cssSelector("div.head_item > strong.tit_name > a.link_name"))
                            .getText();
                    String category = item.findElement(By.cssSelector("div.head_item > span.subcategory")).getText();

                    if (!category.contains("카페") && !category.contains("커피"))
                        continue;

                    String address = item.findElement(By.cssSelector("div.info_item > div.addr > p")).getText();

                    String phone = "";
                    try {
                        phone = item.findElement(By.cssSelector("div.info_item > div.contact > span.phone")).getText();
                    } catch (Exception ignored) {
                    }

                    String rating = "0.0";
                    try {
                        rating = item.findElement(By.cssSelector("div.rating > span.score > em")).getText();
                    } catch (Exception ignored) {
                    }

                    String reviewCount = "0";
                    try {
                        reviewCount = item.findElement(By.cssSelector("div.rating > a.numberofscore")).getText()
                                .replace("건", "");
                    } catch (Exception ignored) {
                    }

                    String detailUrl = item.findElement(By.cssSelector("div.contact > a.moreview"))
                            .getAttribute("href");

                    results.add(CrawlDataDto.builder()
                            .source("KAKAO_MAP")
                            .name(name)
                            .address(address)
                            .phone(phone)
                            .rating(rating)
                            .reviewCount(reviewCount)
                            .url(detailUrl)
                            .build());

                } catch (Exception e) {
                    System.err.println("Error parsing map item: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return results;
    }
}
