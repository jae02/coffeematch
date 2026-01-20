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

    public List<com.coffeematch.backend.dto.CrawlerCafeDetailDto> crawlDetail(String keyword, int limit) {
        List<com.coffeematch.backend.dto.CrawlerCafeDetailDto> results = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        // Random User-Agent could be added here
        options.addArguments(
                "user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            // 1. Search Phase
            driver.get(KAKAO_MAP_URL);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            WebElement searchBox = driver.findElement(By.id("search.keyword.query"));
            searchBox.sendKeys(keyword + " 베이커리"); // Keyword adjustment
            driver.findElement(By.id("search.keyword.submit")).click();

            Thread.sleep(2000);

            // Collect URLs first
            List<String> detailUrls = new ArrayList<>();
            List<WebElement> items = driver
                    .findElements(By.cssSelector("#info\\.search\\.place\\.list > li.PlaceItem"));

            for (WebElement item : items) {
                if (detailUrls.size() >= limit)
                    break;

                try {
                    String category = item.findElement(By.cssSelector("div.head_item > span.subcategory")).getText();
                    if (!category.contains("카페") && !category.contains("커피") && !category.contains("베이커리"))
                        continue;

                    String url = item.findElement(By.cssSelector("div.contact > a.moreview")).getAttribute("href");
                    detailUrls.add(url);
                } catch (Exception ignored) {
                }
            }

            // 2. Detail Phase
            for (String url : detailUrls) {
                if (url == null || url.isEmpty())
                    continue;

                try {
                    // Random delay between requests
                    Thread.sleep((long) (1000 + Math.random() * 2000));

                    driver.get(url);
                    Thread.sleep(1500);

                    com.coffeematch.backend.dto.CrawlerCafeDetailDto cafe = new com.coffeematch.backend.dto.CrawlerCafeDetailDto();
                    cafe.setUrl(url);

                    // Basic Info
                    try {
                        String name = driver.findElement(By.cssSelector(".place_details .tit_location")).getText();
                        cafe.setName(name);
                    } catch (Exception e) {
                    }

                    try {
                        String address = driver.findElement(By.cssSelector(".location_detail .txt_address")).getText();
                        cafe.setAddress(address);
                    } catch (Exception e) {
                    }

                    try {
                        // Biz hour might be hidden or complex, try simple extraction
                        String hours = driver.findElement(By.cssSelector(".location_detail .txt_operation")).getText()
                                .replace("\n", " ");
                        cafe.setBusinessHours(hours);
                    } catch (Exception e) {
                        cafe.setBusinessHours("");
                    }

                    try {
                        String cat = driver.findElement(By.cssSelector(".place_details .txt_location")).getText();
                        cafe.setCategory(cat);
                    } catch (Exception e) {
                    }

                    // Reviews
                    // Click "More" on reviews if available. The selector for review list is usually
                    // #mArticle > div.cont_evaluation > ...
                    // There is a "more" button often defined as `.link_more` inside the review
                    // section

                    // Note: Kakao Map review structure is complex. We'll try to scroll and load.
                    // For simplicity in this v1, we extract currently visible reviews.
                    // To click "more", we look for "후기 더보기" button.

                    List<com.coffeematch.backend.dto.ReviewDetailDto> reviews = new ArrayList<>();

                    // Try parsing reviews
                    // Selector for review items: .list_evaluation > li

                    int reviewLimit = 5; // limit per cafe for now

                    List<WebElement> reviewEls = driver.findElements(By.cssSelector(".list_evaluation > li"));
                    for (WebElement el : reviewEls) {
                        if (reviews.size() >= reviewLimit)
                            break;
                        try {
                            com.coffeematch.backend.dto.ReviewDetailDto review = new com.coffeematch.backend.dto.ReviewDetailDto();

                            String nickname = el.findElement(By.cssSelector(".link_user")).getText();
                            review.setNickname(nickname);

                            String rating = el.findElement(By.cssSelector(".grade_star")).getText().replace("점", "");
                            review.setRating(rating);

                            String content = el.findElement(By.cssSelector(".txt_comment")).getText();
                            review.setContent(content);

                            String date = el.findElement(By.cssSelector(".time_write")).getText();
                            // Transform date if necessary (sometimes it's relative like "2달전", need parsing
                            // logic or keep as is)
                            review.setDate(date);

                            // Photo
                            try {
                                String img = el.findElement(By.cssSelector(".link_photo img")).getAttribute("src");
                                review.setImageUrl(img);
                            } catch (Exception noImg) {
                                review.setImageUrl("");
                            }

                            reviews.add(review);
                        } catch (Exception e) {
                            // ignore broken review
                        }
                    }

                    cafe.setReviews(reviews);
                    results.add(cafe);

                } catch (Exception e) {
                    System.err.println("Error parsing detail: " + url + " - " + e.getMessage());
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
