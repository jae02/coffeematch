package com.coffeematch.backend.service;

import com.coffeematch.backend.dto.AdminStatsDto;
import com.coffeematch.backend.dto.CafeDetailDto;
import com.coffeematch.backend.dto.CafeDto;
import com.coffeematch.backend.dto.CafeRequestDto;
import com.coffeematch.backend.dto.MenuDto;
import com.coffeematch.backend.dto.MenuRequestDto;
import com.coffeematch.backend.dto.ReviewRequestDto;
import com.coffeematch.backend.entity.*;
import com.coffeematch.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class CafeService {

    private final CafeRepository cafeRepository;
    private final KeywordRepository keywordRepository;
    private final CafeKeywordStatRepository cafeKeywordStatRepository;
    private final PlatformDataRepository platformDataRepository;
    private final UserCafeBookmarkRepository userCafeBookmarkRepository;
    private final UserKeywordVoteRepository userKeywordVoteRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;

    public CafeService(CafeRepository cafeRepository, ReviewRepository reviewRepository, UserRepository userRepository,
            PlatformDataRepository platformDataRepository, KeywordRepository keywordRepository,
            UserKeywordVoteRepository userKeywordVoteRepository, UserCafeBookmarkRepository userCafeBookmarkRepository,
            CafeKeywordStatRepository cafeKeywordStatRepository, MenuRepository menuRepository) {
        this.cafeRepository = cafeRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.platformDataRepository = platformDataRepository;
        this.keywordRepository = keywordRepository;
        this.userKeywordVoteRepository = userKeywordVoteRepository;
        this.userCafeBookmarkRepository = userCafeBookmarkRepository;
        this.cafeKeywordStatRepository = cafeKeywordStatRepository;
        this.menuRepository = menuRepository;
    }

    public List<Cafe> getAllCafes(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return cafeRepository.findByNameContainingIgnoreCase(keyword);
        }
        return cafeRepository.findAll();
    }

    public CafeDetailDto getCafeDetails(String email, Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));

        List<PlatformData> platformData = platformDataRepository.findByCafeId(cafeId);
        List<CafeKeywordStat> keywordStats = cafeKeywordStatRepository.findByCafeId(cafeId);
        List<Review> reviews = reviewRepository.findByCafeId(cafeId);

        boolean isBookmarked = false;
        if (email != null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                isBookmarked = userCafeBookmarkRepository.existsByUserIdAndCafeId(user.getId(), cafeId);
            }
        }

        return new CafeDetailDto(new CafeDto(cafe), platformData, keywordStats, reviews, isBookmarked);
    }

    public List<Keyword> getAllKeywords() {
        return keywordRepository.findAll();
    }

    @Transactional
    public void voteKeyword(String email, Long cafeId, Long keywordId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // 0. 기존 투표 확인 (카페당 1인 1투표 정책)
        Optional<UserKeywordVote> existingVote = userKeywordVoteRepository.findByUserIdAndCafeId(user.getId(), cafeId);
        if (existingVote.isPresent()) {
            UserKeywordVote oldVote = existingVote.get();
            // 기존 투표 키워드 통계 감소
            cafeKeywordStatRepository.findByCafeIdAndKeywordId(cafeId, oldVote.getKeyword().getId())
                    .ifPresent(stat -> {
                        stat.setCount(Math.max(0, stat.getCount() - 1));
                        cafeKeywordStatRepository.save(stat);
                    });
            // 기존 투표 삭제
            userKeywordVoteRepository.delete(oldVote);
        }

        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow(() -> new RuntimeException("Cafe not found"));
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new RuntimeException("Keyword not found"));

        // 1. 투표 기록 저장
        UserKeywordVote vote = new UserKeywordVote(user, cafe, keyword);
        userKeywordVoteRepository.save(vote);

        // 2. 통계 업데이트
        CafeKeywordStat stat = cafeKeywordStatRepository.findByCafeIdAndKeywordId(cafeId, keywordId)
                .orElseGet(() -> {
                    CafeKeywordStat newStat = new CafeKeywordStat(cafe, keyword, 0);
                    return cafeKeywordStatRepository.save(newStat);
                });

        stat.incrementCount();
        cafeKeywordStatRepository.save(stat);
    }

    @Transactional
    public boolean toggleBookmark(String email, Long cafeId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow(() -> new RuntimeException("Cafe not found"));

        Optional<UserCafeBookmark> existingBookmark = userCafeBookmarkRepository.findByUserIdAndCafeId(user.getId(),
                cafeId);

        if (existingBookmark.isPresent()) {
            // 삭제
            userCafeBookmarkRepository.delete(existingBookmark.get());
            cafe.setBookmarkCount(Math.max(0, cafe.getBookmarkCount() - 1));
            cafeRepository.save(cafe);
            return false; // Unbookmarked
        } else {
            // 생성
            UserCafeBookmark bookmark = new UserCafeBookmark(user, cafe);
            userCafeBookmarkRepository.save(bookmark);
            cafe.setBookmarkCount(cafe.getBookmarkCount() + 1);
            cafeRepository.save(cafe);
            return true; // Bookmarked
        }
    }

    @Transactional
    public void addReview(String email, Long cafeId, ReviewRequestDto reviewDto, MultipartFile image, String category) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow(() -> new RuntimeException("Cafe not found"));

        Review review = new Review(cafe, user.getNickname(), reviewDto.getRating(), reviewDto.getContent());

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveFile(image);
            review.setImageUrl(imageUrl);
        }
        review.setImageCategory(category);

        reviewRepository.save(review);

        // Update cafe review count
        cafe.setReviewCount(cafe.getReviewCount() + 1);
        cafeRepository.save(cafe);
    }

    @Transactional
    public void updateCafeImage(Long cafeId, MultipartFile image) {
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow(() -> new RuntimeException("Cafe not found"));
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveFile(image);
            cafe.setImageUrl(imageUrl);
            cafeRepository.save(cafe);
        }
    }

    private String saveFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    // Admin Methods

    @Transactional
    public CafeDto createCafe(CafeRequestDto request) {
        Cafe cafe = new Cafe();
        cafe.setName(request.getName());
        cafe.setAddress(request.getAddress());
        cafe.setPhone(request.getPhone());
        cafe.setDescription(request.getDescription());
        cafe.setImageUrl(request.getImageUrl());

        Cafe savedCafe = cafeRepository.save(cafe);
        return new CafeDto(savedCafe);
    }

    @Transactional
    public CafeDto updateCafe(Long id, CafeRequestDto request) {
        Cafe cafe = cafeRepository.findById(id).orElseThrow(() -> new RuntimeException("Cafe not found"));
        cafe.setName(request.getName());
        cafe.setAddress(request.getAddress());
        cafe.setPhone(request.getPhone());
        cafe.setDescription(request.getDescription());
        cafe.setImageUrl(request.getImageUrl());

        return new CafeDto(cafeRepository.save(cafe));
    }

    @Transactional
    public void deleteCafe(Long id) {
        cafeRepository.deleteById(id);
    }

    // Admin Stats
    public AdminStatsDto getAdminStats() {
        long cafeCount = cafeRepository.count();
        long reviewCount = reviewRepository.count();
        return new AdminStatsDto(cafeCount, reviewCount);
    }

    // Admin Menu Management
    @Transactional
    public MenuDto addMenu(Long cafeId, MenuRequestDto request) {
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow(() -> new RuntimeException("Cafe not found"));
        Menu menu = new Menu();
        menu.setItemName(request.getItemName());
        menu.setPrice(request.getPrice());
        if (request.getIsRecommended() != null) {
            menu.setRecommended(request.getIsRecommended());
        }
        menu.setCafe(cafe);

        return new MenuDto(menuRepository.save(menu));
    }

    @Transactional
    public MenuDto updateMenu(Long menuId, MenuRequestDto request) {
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("Menu not found"));
        menu.setItemName(request.getItemName());
        menu.setPrice(request.getPrice());
        if (request.getIsRecommended() != null) {
            menu.setRecommended(request.getIsRecommended());
        }

        return new MenuDto(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        menuRepository.deleteById(menuId);
    }

    // Admin Review Management
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        Cafe cafe = review.getCafe();

        // Decrease review count
        if (cafe != null && cafe.getReviewCount() > 0) {
            cafe.setReviewCount(cafe.getReviewCount() - 1);
            cafeRepository.save(cafe);
        }

        reviewRepository.deleteById(reviewId);
    }
}
