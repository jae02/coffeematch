<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import axios from 'axios';

const props = defineProps({
    id: [Number, String]
});

const emit = defineEmits(['back']);

const cafe = ref(null);
const platformData = ref([]);
const keywordStats = ref([]);
const reviews = ref([]);
const isBookmarked = ref(false);

const loading = ref(true);
const error = ref(null);

// Keywords for voting
const keywords = ref([]);
const showVoteModal = ref(false);

const activeTab = ref('home'); // home, photos, reviews
const activePhotoFilter = ref('ALL'); // ALL, GENERAL, STORE, MENU

// Review Form
const newReview = ref({
    author: '',
    rating: 5,
    content: ''
});
const reviewImage = ref(null);
const reviewImagePreview = ref(null); // Preview URL
const reviewCategory = ref('GENERAL');

// Photo Upload Modal (Standalone)
const showPhotoUploadModal = ref(false);
const photoUploadFile = ref(null);
const photoUploadPreview = ref(null);
const photoUploadCategory = ref('GENERAL');

// Kakao Map
const mapContainer = ref(null);
const mapInstance = ref(null);

// API Base URL (for templates and axios)
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: `${API_BASE_URL}/api`
});

// Helper to get headers
const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return token ? { Authorization: `Bearer ${token}` } : {};
};

const fetchCafe = async () => {
    loading.value = true;
    try {
        const token = localStorage.getItem('token');
        const headers = token ? { Authorization: `Bearer ${token}` } : {};
        
        const response = await api.get(`/cafes/${props.id}`, { headers });
        const data = response.data;
        
        cafe.value = data.cafe;
        platformData.value = data.platformData || [];
        keywordStats.value = data.keywordStats || [];
        reviews.value = data.reviews || [];
        isBookmarked.value = data.bookmarked;
        
    } catch (err) {
        error.value = "카페 상세 정보를 불러오지 못했습니다.";
        console.error(err);
    } finally {
        loading.value = false;
    }
};

const fetchKeywords = async () => {
    try {
        const response = await api.get('/cafes/keywords');
        keywords.value = response.data;
    } catch (err) {
        console.error("키워드 로드 실패", err);
    }
};

const submitReview = async () => {
    if (!localStorage.getItem('token')) return alert("로그인이 필요합니다.");
    if (!newReview.value.author || !newReview.value.content) return alert("모든 필드를 입력해 주세요");
    
    try {
        const formData = new FormData();
        formData.append('review', new Blob([JSON.stringify(newReview.value)], { type: "application/json" }));
        if (reviewImage.value) {
            formData.append('image', reviewImage.value);
        }
        formData.append('category', reviewCategory.value);

        await api.post(`/cafes/${props.id}/reviews`, formData, { 
            headers: { 
                ...getAuthHeader(),
                'Content-Type': 'multipart/form-data'
            } 
        });
        await fetchCafe();
        newReview.value = { author: '', rating: 5, content: '' };
        reviewImage.value = null;
        reviewCategory.value = 'GENERAL';
        alert("리뷰가 등록되었습니다!");
    } catch (err) {
        console.error(err);
        alert("리뷰 등록에 실패했습니다");
    }
};

const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        reviewImage.value = file;
        reviewImagePreview.value = URL.createObjectURL(file);
    }
};

const removeReviewImage = () => {
    reviewImage.value = null;
    reviewImagePreview.value = null;
    // Reset file input if needed (requires ref to input)
    const input = document.getElementById('review-file-input');
    if (input) input.value = '';
};

// Photo Upload Modal Handlers
const handlePhotoUploadFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        photoUploadFile.value = file;
        photoUploadPreview.value = URL.createObjectURL(file);
    }
};

const removePhotoUploadImage = () => {
    photoUploadFile.value = null;
    photoUploadPreview.value = null;
    const input = document.getElementById('photo-upload-input');
    if (input) input.value = '';
};

const submitPhotoUpload = async () => {
    if (!localStorage.getItem('token')) return alert("로그인이 필요합니다.");
    if (!photoUploadFile.value) return alert("사진을 선택해주세요.");

    try {
        const formData = new FormData();
        // Send default values for text-less photo upload
        const reviewData = {
            author: 'Anonymous', // Will be overwritten by backend user info
            rating: 5,
            content: '사진 리뷰' 
        };
        formData.append('review', new Blob([JSON.stringify(reviewData)], { type: "application/json" }));
        formData.append('image', photoUploadFile.value);
        formData.append('category', photoUploadCategory.value);

        await api.post(`/cafes/${props.id}/reviews`, formData, { 
            headers: { 
                ...getAuthHeader(),
                'Content-Type': 'multipart/form-data'
            } 
        });
        await fetchCafe();
        
        // Reset
        showPhotoUploadModal.value = false;
        photoUploadFile.value = null;
        photoUploadPreview.value = null;
        photoUploadCategory.value = 'GENERAL';
        alert("사진이 등록되었습니다!");
    } catch (err) {
        console.error(err);
        alert("사진 등록에 실패했습니다");
    }
};

const handleCafeImageChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!localStorage.getItem('token')) return alert("로그인이 필요합니다.");

    try {
        const formData = new FormData();
        formData.append('image', file);

        await api.post(`/cafes/${props.id}/image`, formData, {
            headers: {
                ...getAuthHeader(),
                'Content-Type': 'multipart/form-data'
            }
        });
        await fetchCafe();
        alert("대표 사진이 변경되었습니다!");
    } catch (err) {
        console.error(err);
        alert("사진 변경에 실패했습니다.");
    }
};

const toggleBookmark = async () => {
    if (!localStorage.getItem('token')) return alert("로그인이 필요합니다.");
    
    try {
        const response = await api.post(`/cafes/${props.id}/bookmark`, {}, { headers: getAuthHeader() });
        isBookmarked.value = response.data; // true or false
        // Update local count intentionally for immediate feedback
        if (cafe.value) {
            cafe.value.bookmarkCount += isBookmarked.value ? 1 : -1;
        }
    } catch (err) {
        console.error(err);
        alert("북마크 처리에 실패했습니다.");
    }
};

const voteKeyword = async (keywordId) => {
    if (!localStorage.getItem('token')) return alert("로그인이 필요합니다.");
    
    try {
        await api.post(`/cafes/${props.id}/vote`, { keywordId }, { headers: getAuthHeader() });
        await fetchCafe(); // Refresh stats
        showVoteModal.value = false;
        alert("투표가 완료되었습니다!");
    } catch (err) {
        alert(err.response?.data || "투표에 실패했습니다.");
    }
};

onMounted(() => {
    fetchCafe();
    fetchKeywords();
});

// Initialize Kakao Map
const initMap = () => {
    if (!cafe.value || !mapContainer.value) return;
    if (!window.kakao || !window.kakao.maps) {
        console.error('Kakao Maps SDK not loaded');
        return;
    }

    const lat = cafe.value.latitude;
    const lng = cafe.value.longitude;

    // If coordinates exist, use them directly
    if (lat && lng) {
        createMap(lat, lng);
    } else if (cafe.value.address) {
        // Use Geocoder to convert address to coordinates
        const geocoder = new window.kakao.maps.services.Geocoder();
        geocoder.addressSearch(cafe.value.address, (result, status) => {
            if (status === window.kakao.maps.services.Status.OK) {
                createMap(result[0].y, result[0].x);
            } else {
                console.warn('Geocoding failed for address:', cafe.value.address);
            }
        });
    }
};

const createMap = (lat, lng) => {
    const options = {
        center: new window.kakao.maps.LatLng(lat, lng),
        level: 3
    };
    mapInstance.value = new window.kakao.maps.Map(mapContainer.value, options);

    // Add marker
    const marker = new window.kakao.maps.Marker({
        position: new window.kakao.maps.LatLng(lat, lng),
        map: mapInstance.value
    });

    // Add info window
    const infoContent = `<div style="padding:8px 12px;font-size:13px;font-weight:bold;">${cafe.value.name}</div>`;
    const infowindow = new window.kakao.maps.InfoWindow({
        content: infoContent
    });
    infowindow.open(mapInstance.value, marker);
};

// Watch for cafe data and initialize map when available
watch(() => cafe.value, async (newCafe) => {
    if (newCafe) {
        await nextTick();
        initMap();
    }
});

const sortedMenus = computed(() => {
    if (!cafe.value || !cafe.value.menus) return [];
    return [...cafe.value.menus].sort((a, b) => a.price - b.price);
});

const topKeywords = computed(() => {
    return [...keywordStats.value].sort((a, b) => b.count - a.count).slice(0, 3);
});

const filteredPhotos = computed(() => {
    const photos = reviews.value.filter(r => r.imageUrl);
    if (activePhotoFilter.value === 'ALL') return photos;
    return photos.filter(p => p.imageCategory === activePhotoFilter.value);
});
</script>

<template>
    <div class="bg-white min-h-screen pb-24 relative">
        <!-- Top Navigation -->
        <div class="fixed top-0 left-0 right-0 z-50 flex items-center justify-between p-3 bg-transparent/50 backdrop-blur-sm md:hidden">
            <button @click="$emit('back')" class="text-white drop-shadow-md p-1">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                </svg>
            </button>
            <div class="flex gap-4 text-white drop-shadow-md p-1">
                <button @click="toggleBookmark">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 transition-colors" :class="isBookmarked ? 'text-red-500 fill-current' : 'text-white'" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                </button>
            </div>
        </div>
        
        <!-- Desktop Back Button -->
        <div class="hidden md:block p-4 border-b border-gray-100">
             <button @click="$emit('back')" class="text-gray-800 flex items-center gap-2 hover:bg-gray-100 rounded-lg px-3 py-2 transition">
                ← 돌아가기
            </button>
        </div>

        <div v-if="loading" class="text-center py-20 text-gray-400">정보 불러오는 중...</div>
        <div v-if="error" class="text-center py-20 text-red-500">{{ error }}</div>
        
        <div v-else-if="cafe" class="animate-fade-in">
            <!-- Hero Image -->
            <div class="w-full aspect-square md:aspect-video bg-gray-200 relative group">
                <img :src="cafe.imageUrl?.startsWith('/') ? API_BASE_URL + cafe.imageUrl : (cafe.imageUrl || 'https://placehold.co/800x600/e0cec7/5d4037?text=Cafe')" class="w-full h-full object-cover" />
                <div class="absolute bottom-4 right-4 bg-black/60 text-white px-3 py-1 rounded-full text-xs backdrop-blur-sm z-10">
                    ⭐ {{ (cafe.internalRatingAvg || 0).toFixed(1) }} ({{ cafe.reviewCount }}명)
                </div>
                
                <!-- Change Image Button -->
                <label class="absolute top-4 right-4 bg-white/80 p-2 rounded-full cursor-pointer hover:bg-white shadow-md transition opacity-0 group-hover:opacity-100 z-20">
                    <input type="file" accept="image/*" class="hidden" @change="handleCafeImageChange" />
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                </label>
            </div>

            <!-- Profile / Info Section (Always Visible) -->
            <div class="p-4 border-b border-gray-100">
                <div class="flex items-center justify-between mb-4">
                    <div>
                        <h2 class="text-2xl font-bold text-gray-900 leading-none mb-1">{{ cafe.name }}</h2>
                        <p class="text-sm text-gray-500">{{ cafe.address }}</p>
                    </div>
                </div>

                <!-- Rating Card -->
                <div class="bg-gray-50 rounded-xl p-4 mb-4 border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="text-3xl font-bold text-creama-espresso">{{ (cafe.internalRatingAvg || 0).toFixed(1) }}</div>
                        <div>
                            <div class="flex gap-0.5 text-lg">
                                <span v-for="i in 5" :key="i" :class="i <= Math.round(cafe.internalRatingAvg || 0) ? 'opacity-100' : 'opacity-30 grayscale'">☕</span>
                            </div>
                            <div class="text-xs text-gray-400">리뷰 {{ cafe.reviewCount }}개</div>
                        </div>
                    </div>
                </div>

                <!-- Keyword Vote Section -->
                <div class="mb-4">
                    <div class="flex justify-between items-center mb-2">
                        <h3 class="text-sm font-bold text-gray-700">이 카페의 특징</h3>
                        <button @click="showVoteModal = true" class="text-xs text-creama-espresso font-bold underline">투표하기</button>
                    </div>
                    <div class="flex flex-wrap gap-2">
                        <div v-for="stat in topKeywords" :key="stat.id" class="bg-creama-latte text-creama-espresso px-3 py-1 rounded-full text-xs font-bold border border-creama-latte">
                            #{{ stat.keyword.name }} <span class="text-creama-crema ml-1">{{ stat.count }}</span>
                        </div>
                        <div v-if="topKeywords.length === 0" class="text-xs text-gray-400">아직 투표된 키워드다 없습니다. 첫 투표를 해주세요!</div>
                    </div>
                </div>

                <div class="py-2">
                     <p class="text-gray-800 text-sm leading-relaxed whitespace-pre-line">{{ cafe.description }}</p>
                </div>
            </div>

            <!-- Menu Section -->
            <div class="p-4 border-b border-gray-100">
                <h3 class="font-bold text-gray-900 mb-4">메뉴</h3>
                <div class="space-y-3">
                    <div v-for="menu in sortedMenus" :key="menu.id" class="flex justify-between items-center">
                        <div class="flex items-center gap-2">
                             <span v-if="menu.recommended" class="text-creama-espresso text-[10px] border border-creama-espresso px-1 rounded font-bold">추천</span>
                             <span class="text-gray-900 text-sm">{{ menu.itemName }}</span>
                        </div>
                        <span class="font-bold text-gray-900 text-sm">{{ menu.price.toLocaleString() }}원</span>
                    </div>
                </div>
            </div>

            <!-- Map Section -->
            <div class="p-4 border-b border-gray-100">
                <h3 class="font-bold text-gray-900 mb-4">📍 위치</h3>
                <div ref="mapContainer" class="w-full h-48 rounded-xl bg-gray-100 overflow-hidden"></div>
                <p class="text-xs text-gray-400 mt-2">{{ cafe?.address }}</p>
            </div>

            <!-- Photos Section (Musinsa Style) -->
            <div class="bg-white border-b border-gray-100 py-4">
                <div class="flex items-center justify-between px-4 mb-3">
                    <h3 class="font-bold text-gray-900">스타일 포토 <span class="text-gray-400 font-normal">{{ filteredPhotos.length }}</span></h3>
                    <button @click="showPhotoUploadModal = true" class="text-xs text-gray-500 hover:text-gray-900 transition">+ 사진 등록</button>
                </div>
                
                <!-- Horizontal Scroll Photo Grid -->
                <div class="flex gap-2 px-4 overflow-x-auto no-scrollbar pb-2">
                    <template v-if="filteredPhotos.length > 0">
                        <div v-for="photo in filteredPhotos.slice(0, 8)" :key="photo.id" 
                             class="flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden bg-gray-100 cursor-pointer hover:opacity-80 transition">
                            <img :src="photo.imageUrl.startsWith('/') ? API_BASE_URL + photo.imageUrl : photo.imageUrl" 
                                 class="w-full h-full object-cover" />
                        </div>
                        <div v-if="filteredPhotos.length > 8" 
                             class="flex-shrink-0 w-20 h-20 rounded-lg bg-gray-100 flex items-center justify-center cursor-pointer hover:bg-gray-200 transition">
                            <span class="text-xs text-gray-500 font-bold">+{{ filteredPhotos.length - 8 }}</span>
                        </div>
                    </template>
                    <template v-else>
                        <div @click="showPhotoUploadModal = true" 
                             class="flex-shrink-0 w-20 h-20 rounded-lg bg-gray-50 border border-dashed border-gray-300 flex flex-col items-center justify-center cursor-pointer hover:bg-gray-100 transition">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-300 mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                            </svg>
                            <span class="text-[10px] text-gray-400">첫 사진</span>
                        </div>
                    </template>
                </div>
            </div>

            <!-- Reviews Section (Musinsa Style) -->
            <div class="bg-white pb-24">
                <!-- Review Summary Header -->
                <div class="p-4 border-b border-gray-100">
                    <div class="flex items-start gap-6">
                        <!-- Average Rating -->
                        <div class="text-center">
                            <div class="text-4xl font-bold text-gray-900">{{ (cafe.internalRatingAvg || 0).toFixed(1) }}</div>
                            <div class="flex justify-center gap-0.5 mt-1 text-sm">
                                <span v-for="i in 5" :key="i" :class="i <= Math.round(cafe.internalRatingAvg || 0) ? 'opacity-100' : 'opacity-30 grayscale'">☕</span>
                            </div>
                            <div class="text-xs text-gray-400 mt-1">{{ reviews.length }}개 리뷰</div>
                        </div>
                        
                        <!-- Rating Distribution Bars -->
                        <div class="flex-1 space-y-1">
                            <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="flex items-center gap-2">
                                <span class="text-xs text-gray-400 w-6">{{ star }}점</span>
                                <div class="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                                    <div class="h-full bg-gray-900 rounded-full transition-all" 
                                         :style="{ width: reviews.length ? ((reviews.filter(r => r.rating === star).length / reviews.length) * 100) + '%' : '0%' }"></div>
                                </div>
                                <span class="text-xs text-gray-400 w-6 text-right">{{ reviews.filter(r => r.rating === star).length }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Review Filters -->
                <div class="flex gap-2 px-4 py-3 border-b border-gray-100 overflow-x-auto no-scrollbar">
                    <button class="px-3 py-1.5 text-xs font-bold rounded border border-gray-900 bg-gray-900 text-white">전체</button>
                    <button class="px-3 py-1.5 text-xs font-medium rounded border border-gray-200 text-gray-500 hover:border-gray-400 transition">포토 리뷰</button>
                    <button class="px-3 py-1.5 text-xs font-medium rounded border border-gray-200 text-gray-500 hover:border-gray-400 transition">최신순</button>
                </div>

                <!-- Review List -->
                <div class="divide-y divide-gray-100">
                    <div v-for="review in reviews" :key="review.id" class="p-4">
                        <div class="flex gap-4">
                            <!-- Left: User Info -->
                            <div class="flex-shrink-0 w-20">
                                <div class="flex items-center gap-1.5 mb-1">
                                    <div class="w-5 h-5 rounded-full bg-gray-100 overflow-hidden">
                                        <img :src="`https://ui-avatars.com/api/?name=${review.author}&background=e5e5e5&color=666&size=20`" alt="" />
                                    </div>
                                    <span class="text-xs font-bold text-gray-700 truncate">{{ review.author }}</span>
                                </div>
                                <div class="text-[10px] text-gray-400 leading-tight">
                                    {{ review.createdAt ? new Date(review.createdAt).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }) : '오늘' }}
                                </div>
                            </div>
                            
                            <!-- Right: Review Content -->
                            <div class="flex-1 min-w-0">
                                <!-- Crema Rating -->
                                <div class="flex items-center gap-2 mb-2">
                                    <div class="flex gap-0.5 text-xs">
                                        <span v-for="i in 5" :key="i" :class="i <= review.rating ? 'opacity-100' : 'opacity-30 grayscale'">☕</span>
                                    </div>
                                </div>
                                
                                <!-- Content -->
                                <p class="text-sm text-gray-700 leading-relaxed mb-3">{{ review.content }}</p>
                                
                                <!-- Review Image -->
                                <div v-if="review.imageUrl" class="mb-2">
                                    <img :src="review.imageUrl.startsWith('/') ? API_BASE_URL + review.imageUrl : review.imageUrl" 
                                         class="w-24 h-24 rounded-lg object-cover" />
                                </div>
                                
                                <!-- Helpful Button -->
                                <button class="text-xs text-gray-400 hover:text-gray-700 transition flex items-center gap-1">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                                    </svg>
                                    도움이 돼요
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Empty State -->
                    <div v-if="reviews.length === 0" class="py-12 text-center">
                        <div class="text-gray-300 text-4xl mb-2">📝</div>
                        <p class="text-gray-400 text-sm">아직 리뷰가 없어요</p>
                        <p class="text-gray-400 text-xs">첫 번째 리뷰를 남겨주세요!</p>
                    </div>
                </div>

                <!-- Review Form -->
                <div class="p-4 border-t border-gray-100 bg-gray-50">
                    <form @submit.prevent="submitReview" class="space-y-3">
                        <div class="flex gap-2 items-center">
                            <input v-model="newReview.author" placeholder="닉네임" 
                                   class="flex-1 bg-white rounded-lg px-3 py-2.5 text-sm border border-gray-200 focus:border-gray-400 focus:outline-none" required />
                            <div class="flex gap-1 items-center text-xl">
                                <span v-for="i in 5" :key="i" 
                                     @click="newReview.rating = i" 
                                     class="cursor-pointer transition-transform hover:scale-125" 
                                     :class="[i <= newReview.rating ? 'opacity-100 animate-wiggle' : 'opacity-30 grayscale']"
                                     :style="{ animationDelay: (i - 1) * 0.1 + 's' }">☕</span>
                            </div>
                        </div>
                        
                        <textarea v-model="newReview.content" rows="3" placeholder="이 카페에 대한 솔직한 후기를 남겨주세요." 
                                  class="w-full bg-white rounded-lg px-3 py-2.5 text-sm border border-gray-200 focus:border-gray-400 focus:outline-none resize-none" required></textarea>
                        
                        <div class="flex items-center justify-between">
                            <label for="review-file-input" class="flex items-center gap-1.5 text-xs text-gray-500 cursor-pointer hover:text-gray-700 transition">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                                {{ reviewImage ? '사진 변경' : '사진 첨부' }}
                            </label>
                            <input id="review-file-input" type="file" accept="image/*" @change="handleFileChange" class="hidden" />
                            
                            <button type="submit" class="bg-gray-900 text-white text-sm font-bold px-6 py-2 rounded-lg hover:bg-gray-800 transition">
                                등록
                            </button>
                        </div>
                        
                        <!-- Image Preview -->
                        <div v-if="reviewImagePreview" class="relative inline-block">
                            <img :src="reviewImagePreview" class="h-16 rounded-lg object-cover" />
                            <button type="button" @click="removeReviewImage" 
                                    class="absolute -top-1.5 -right-1.5 bg-gray-600 text-white rounded-full w-4 h-4 flex items-center justify-center text-xs hover:bg-gray-800">×</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Bottom Action Bar (Mobile) -->
            <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-3 flex justify-between items-center md:hidden z-50 safe-area-bottom">
                 <div class="flex items-center gap-3 pl-2">
                     <button @click="toggleBookmark" class="flex flex-col items-center transition-colors" :class="isBookmarked ? 'text-red-500' : 'text-gray-400'">
                         <span class="text-xl">{{ isBookmarked ? '♥' : '♡' }}</span>
                         <span class="text-[10px]">{{ cafe.bookmarkCount }}</span>
                     </button>
                 </div>
                 <div class="flex items-center gap-2">
                     <button class="bg-creama-espresso text-white font-bold px-6 py-2.5 rounded-lg hover:bg-creama-espresso transition shadow-lg shadow-creama-crema">
                         채팅하기
                     </button>
                 </div>
            </div>
        </div>

        <!-- Photo Upload Modal -->
        <div v-if="showPhotoUploadModal" class="fixed inset-0 z-[70] flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showPhotoUploadModal = false"></div>
            <div class="bg-white rounded-2xl p-6 w-full max-w-sm relative z-10 animate-fade-in-up">
                <h3 class="text-lg font-bold text-gray-900 mb-4 text-center">사진 올리기</h3>
                
                <div class="space-y-4">
                    <!-- Category Select -->
                    <div>
                        <label class="block text-xs font-bold text-gray-500 mb-1">카테고리</label>
                        <div class="flex gap-2">
                            <button v-for="cat in ['GENERAL', 'STORE', 'MENU']" :key="cat"
                                    type="button"
                                    @click="photoUploadCategory = cat"
                                    class="flex-1 py-2 rounded-lg text-sm font-bold border transition"
                                    :class="photoUploadCategory === cat ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-400 border-gray-200'">
                                {{ cat === 'GENERAL' ? '일반' : cat === 'STORE' ? '매장' : '메뉴' }}
                            </button>
                        </div>
                    </div>

                    <!-- File Input -->
                    <div class="border-2 border-dashed border-gray-200 rounded-xl p-4 flex flex-col items-center justify-center bg-gray-50 min-h-[160px] relative">
                         <div v-if="!photoUploadPreview" class="text-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-gray-300 mx-auto mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            <label for="photo-upload-input" class="bg-white border border-gray-300 text-gray-600 px-4 py-2 rounded-lg text-sm font-bold cursor-pointer hover:bg-gray-50 transition shadow-sm">
                                사진 선택하기
                            </label>
                         </div>
                         <div v-else class="w-full h-full relative">
                             <img :src="photoUploadPreview" class="w-full h-40 object-contain rounded-lg" />
                             <button @click="removePhotoUploadImage" class="absolute -top-2 -right-2 bg-gray-600 text-white rounded-full p-1 shadow-md hover:bg-gray-800">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                                </svg>
                             </button>
                         </div>
                         <input id="photo-upload-input" type="file" accept="image/*" @change="handlePhotoUploadFileChange" class="hidden" />
                    </div>

                    <button @click="submitPhotoUpload" class="w-full bg-creama-espresso text-white font-bold py-3 rounded-xl disabled:bg-gray-300 disabled:cursor-not-allowed transition shadow-md shadow-creama-crema" :disabled="!photoUploadFile">
                        등록 완료
                    </button>
                    <button @click="showPhotoUploadModal = false" class="w-full text-gray-400 text-xs underline">취소</button>
                </div>
            </div>
        </div>

        <!-- Vote Modal -->
        <div v-if="showVoteModal" class="fixed inset-0 z-[60] flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showVoteModal = false"></div>
            <div class="bg-white rounded-2xl p-6 w-full max-w-sm relative z-10 animate-fade-in-up">
                <h3 class="text-lg font-bold text-gray-900 mb-1 text-center">이 카페의 매력을 알려주세요!</h3>
                <p class="text-xs text-center text-gray-400 mb-4">카페당 1개의 키워드에만 투표할 수 있으며,<br/>새로 투표하면 기존 투표는 변경됩니다.</p>
                <div class="flex flex-wrap gap-2 justify-center">
                    <button v-for="keyword in keywords" :key="keyword.id" 
                            @click="voteKeyword(keyword.id)"
                            class="px-3 py-2 rounded-lg bg-gray-50 text-gray-700 text-sm font-medium hover:bg-creama-latte hover:text-creama-espresso hover:border-creama-crema border border-transparent transition">
                        #{{ keyword.name }}
                    </button>
                </div>
                <button @click="showVoteModal = false" class="mt-6 w-full py-3 text-gray-500 font-medium text-sm">닫기</button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.animate-fade-in-up {
    animation: fadeInUp 0.3s ease-out;
}
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.animate-wiggle {
    animation: wiggle 0.6s ease-in-out infinite;
}

@keyframes wiggle {
    0%, 100% {
        transform: rotate(0deg);
    }
    25% {
        transform: rotate(-10deg);
    }
    75% {
        transform: rotate(10deg);
    }
}
</style>
