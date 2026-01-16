<script setup>
import { ref, onMounted, computed } from 'vue';
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

const api = axios.create({
    baseURL: 'http://localhost:8080/api'
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
                <img :src="cafe.imageUrl?.startsWith('/') ? 'http://localhost:8080' + cafe.imageUrl : (cafe.imageUrl || 'https://placehold.co/800x600/e0cec7/5d4037?text=Cafe')" class="w-full h-full object-cover" />
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

                <!-- Platform Comparison Card -->
                <div class="bg-gray-50 rounded-xl p-4 mb-4 border border-gray-100">
                    <h3 class="text-sm font-bold text-gray-700 mb-3">🔥 플랫폼 별점 비교</h3>
                    <div class="flex gap-2 overflow-x-auto pb-1">
                        <!-- Internal -->
                        <div class="flex-1 min-w-[100px] bg-white p-3 rounded-lg shadow-sm border border-gray-100 text-center">
                            <div class="text-[10px] text-gray-400 font-bold mb-1">COFFEEMATCH</div>
                            <div class="text-xl font-bold text-daangn-500">{{ (cafe.internalRatingAvg || 0).toFixed(1) }}</div>
                            <div class="text-[10px] text-gray-400">리뷰 {{ cafe.reviewCount }}</div>
                        </div>
                        <!-- External Platforms -->
                        <div v-for="platform in platformData" :key="platform.id" 
                             class="flex-1 min-w-[100px] bg-white p-3 rounded-lg shadow-sm border border-gray-100 text-center relative group cursor-pointer"
                             @click="platform.link && window.open(platform.link, '_blank')">
                            <div class="text-[10px] font-bold mb-1" :class="platform.platformType === 'NAVER' ? 'text-green-500' : 'text-yellow-500'">
                                {{ platform.platformType }}
                            </div>
                            <div class="text-xl font-bold text-gray-800">{{ platform.rating }}</div>
                            <div class="text-[10px] text-gray-400">리뷰 {{ platform.reviewCount }}</div>
                            <div class="absolute inset-0 bg-black/5 opacity-0 group-hover:opacity-100 transition rounded-lg"></div>
                        </div>
                    </div>
                </div>

                <!-- Keyword Vote Section -->
                <div class="mb-4">
                    <div class="flex justify-between items-center mb-2">
                        <h3 class="text-sm font-bold text-gray-700">이 카페의 특징</h3>
                        <button @click="showVoteModal = true" class="text-xs text-daangn-500 font-bold underline">투표하기</button>
                    </div>
                    <div class="flex flex-wrap gap-2">
                        <div v-for="stat in topKeywords" :key="stat.id" class="bg-daangn-50 text-daangn-700 px-3 py-1 rounded-full text-xs font-bold border border-daangn-100">
                            #{{ stat.keyword.name }} <span class="text-daangn-400 ml-1">{{ stat.count }}</span>
                        </div>
                        <div v-if="topKeywords.length === 0" class="text-xs text-gray-400">아직 투표된 키워드다 없습니다. 첫 투표를 해주세요!</div>
                    </div>
                </div>

                <div class="py-2">
                     <p class="text-gray-800 text-sm leading-relaxed whitespace-pre-line">{{ cafe.description }}</p>
                </div>
            </div>

            <!-- Tab Navigation -->
            <div class="sticky top-[48px] md:top-0 z-40 bg-white border-b border-gray-200">
                <div class="flex">
                    <button @click="activeTab = 'home'" class="flex-1 py-3 text-sm font-bold transition border-b-2" :class="activeTab === 'home' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-400'">홈</button>
                    <button @click="activeTab = 'photos'" class="flex-1 py-3 text-sm font-bold transition border-b-2" :class="activeTab === 'photos' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-400'">사진</button>
                    <button @click="activeTab = 'reviews'" class="flex-1 py-3 text-sm font-bold transition border-b-2" :class="activeTab === 'reviews' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-400'">리뷰</button>
                </div>
            </div>

            <!-- Menu Section (Home Tab) -->
            <div v-show="activeTab === 'home'" class="p-4 border-b border-gray-100">
                <h3 class="font-bold text-gray-900 mb-4">메뉴</h3>
                <div class="space-y-3">
                    <div v-for="menu in sortedMenus" :key="menu.id" class="flex justify-between items-center">
                        <div class="flex items-center gap-2">
                             <span v-if="menu.recommended" class="text-daangn-500 text-[10px] border border-daangn-500 px-1 rounded font-bold">추천</span>
                             <span class="text-gray-900 text-sm">{{ menu.itemName }}</span>
                        </div>
                        <span class="font-bold text-gray-900 text-sm">{{ menu.price.toLocaleString() }}원</span>
                    </div>
                </div>
            </div>

            <!-- Photos Section (Photos Tab) -->
            <div v-show="activeTab === 'photos'" class="bg-white min-h-[300px]">
                <!-- Photo Filter -->
                <div class="flex gap-2 p-4 overflow-x-auto no-scrollbar">
                    <button @click="activePhotoFilter = 'ALL'" 
                            class="px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap border transition"
                            :class="activePhotoFilter === 'ALL' ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-500 border-gray-200'">
                        전체
                    </button>
                    <button @click="activePhotoFilter = 'GENERAL'" 
                            class="px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap border transition"
                            :class="activePhotoFilter === 'GENERAL' ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-500 border-gray-200'">
                        일반
                    </button>
                    <button @click="activePhotoFilter = 'STORE'" 
                            class="px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap border transition"
                            :class="activePhotoFilter === 'STORE' ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-500 border-gray-200'">
                        매장
                    </button>
                    <button @click="activePhotoFilter = 'MENU'" 
                            class="px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap border transition"
                            :class="activePhotoFilter === 'MENU' ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-500 border-gray-200'">
                        메뉴
                    </button>
                </div>

                <!-- Photo Grid -->
                <div class="grid grid-cols-3 gap-0.5 relative min-h-[300px]">
                    <!-- Upload Tile (Always First) -->
                    <div @click="showPhotoUploadModal = true" class="aspect-square bg-gray-50 flex flex-col items-center justify-center cursor-pointer hover:bg-gray-100 transition group border border-dashed border-gray-300">
                        <div class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center mb-1 group-hover:bg-gray-300 transition">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                            </svg>
                        </div>
                        <span class="text-[10px] text-gray-500 font-medium">사진 추가</span>
                    </div>

                    <template v-if="filteredPhotos.length > 0">
                        <div v-for="photo in filteredPhotos" :key="photo.id" class="aspect-square bg-gray-100 relative group cursor-pointer">
                            <img :src="photo.imageUrl.startsWith('/') ? 'http://localhost:8080' + photo.imageUrl : photo.imageUrl" class="w-full h-full object-cover" />
                        </div>
                    </template>
                    <!-- Empty State Placeholders (Reduced count since we have upload tile) -->
                    <template v-else>
                        <div v-for="i in 8" :key="i" class="aspect-square bg-gray-50 border border-gray-100 flex items-center justify-center">
                             <svg v-if="i === 4" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-100" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                 <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                        </div>
                    </template>
                </div>
            </div>

            <!-- Reviews Section (Reviews Tab) -->
            <div v-show="activeTab === 'reviews'" class="p-4 bg-gray-50 pb-20">
                 <div class="flex items-center justify-between mb-4">
                    <h3 class="font-bold text-gray-900">리뷰 {{ reviews.length }}</h3>
                 </div>
                 
                 <!-- Review List -->
                 <div class="space-y-3 mb-6">
                    <div v-for="review in reviews" :key="review.id" class="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                        <div class="flex justify-between items-start mb-2">
                            <div class="flex items-center gap-2">
                                <div class="w-6 h-6 rounded-full bg-gray-200 overflow-hidden">
                                     <img :src="`https://ui-avatars.com/api/?name=${review.author}&background=random`" alt="User" />
                                </div>
                                <span class="font-bold text-sm text-gray-900">{{ review.author }}</span>
                            </div>
                            <div class="flex text-yellow-400 text-xs">
                                {{ '★'.repeat(review.rating) }}<span class="text-gray-300">{{ '★'.repeat(5 - review.rating) }}</span>
                            </div>
                        </div>
                        <p class="text-gray-600 text-sm pl-8 mb-2">{{ review.content }}</p>
                        
                        <!-- Review Image -->
                        <div v-if="review.imageUrl" class="pl-8 mb-2 relative inline-block">
                             <img :src="review.imageUrl.startsWith('/') ? 'http://localhost:8080' + review.imageUrl : review.imageUrl" class="rounded-lg max-h-40 object-cover border border-gray-100" />
                             <span v-if="review.imageCategory" class="absolute top-2 right-2 bg-black/50 text-white text-[10px] px-1.5 py-0.5 rounded backdrop-blur-sm">
                                {{ review.imageCategory }}
                             </span>
                        </div>

                        <span class="text-xs text-gray-400 pl-8 mt-1 block">{{ review.createdAt ? new Date(review.createdAt).toLocaleDateString() : '방금 전' }}</span>
                    </div>
                 </div>

                 <!-- Review Form -->
                 <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <h4 class="text-sm font-bold mb-3 text-gray-700">리뷰 남기기</h4>
                    <form @submit.prevent="submitReview" class="space-y-3">
                        <div class="flex gap-2">
                             <input v-model="newReview.author" placeholder="닉네임" class="w-1/3 bg-gray-50 rounded-lg p-2 text-sm border-none focus:ring-1 focus:ring-gray-300" required />
                             <select v-model="newReview.rating" class="w-2/3 bg-gray-50 rounded-lg p-2 text-sm border-none focus:ring-1 focus:ring-gray-300">
                                <option :value="5">⭐⭐⭐⭐⭐ 최고예요</option>
                                <option :value="4">⭐⭐⭐⭐ 좋아요</option>
                                <option :value="3">⭐⭐⭐ 보통이에요</option>
                            </select>
                        </div>
                        <div class="flex gap-2 items-center">
                            <select v-model="reviewCategory" class="w-1/3 bg-gray-50 rounded-lg p-2 text-sm border-none focus:ring-1 focus:ring-gray-300">
                                <option value="GENERAL">일반</option>
                                <option value="STORE">매장</option>
                                <option value="MENU">메뉴</option>
                            </select>
                            
                            <!-- Custom File Input with Preview Trigger -->
                            <div class="flex-1">
                                <label for="review-file-input" class="flex items-center gap-2 cursor-pointer bg-gray-50 rounded-lg p-2 hover:bg-gray-100 transition">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                    </svg>
                                    <span class="text-xs text-gray-500">{{ reviewImage ? '사진 변경' : '사진 추가' }}</span>
                                </label>
                                <input id="review-file-input" type="file" accept="image/*" @change="handleFileChange" class="hidden" />
                            </div>
                        </div>

                        <!-- Image Preview in Review Form -->
                        <div v-if="reviewImagePreview" class="relative inline-block mt-2">
                            <img :src="reviewImagePreview" class="h-24 w-auto rounded-lg border border-gray-200 object-cover" />
                            <button type="button" @click="removeReviewImage" class="absolute -top-2 -right-2 bg-gray-600 text-white rounded-full w-5 h-5 flex items-center justify-center shadow-md hover:bg-gray-800 transition">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                                </svg>
                            </button>
                        </div>

                        <textarea v-model="newReview.content" rows="2" placeholder="솔직한 후기를 남겨주세요." class="w-full bg-gray-50 rounded-lg p-2 text-sm border-none focus:ring-1 focus:ring-gray-300" required></textarea>
                        <button type="submit" class="w-full bg-gray-100 text-gray-600 hover:bg-daangn-100 hover:text-daangn-600 font-bold py-2 rounded-lg transition text-sm">
                            등록하기
                        </button>
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
                     <button class="bg-daangn-500 text-white font-bold px-6 py-2.5 rounded-lg hover:bg-daangn-600 transition shadow-lg shadow-daangn-200">
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

                    <button @click="submitPhotoUpload" class="w-full bg-daangn-500 text-white font-bold py-3 rounded-xl disabled:bg-gray-300 disabled:cursor-not-allowed transition shadow-md shadow-daangn-200" :disabled="!photoUploadFile">
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
                            class="px-3 py-2 rounded-lg bg-gray-50 text-gray-700 text-sm font-medium hover:bg-daangn-50 hover:text-daangn-600 hover:border-daangn-200 border border-transparent transition">
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
</style>
