<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

const cafes = ref([]);
const keyword = ref('');
const loading = ref(false);
const loadingMore = ref(false);
const error = ref(null);

// Pagination state
const page = ref(0);
const hasMore = ref(true);
const totalElements = ref(0);

// Location state
const userLocation = ref(null);
const locationEnabled = ref(false);
const radius = ref(5); // km

// API Base URL
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: `${API_BASE_URL}/api`
});

// Infinite scroll observer
let observer = null;
const loadMoreTrigger = ref(null);

const fetchCafes = async (reset = false) => {
    if (reset) {
        page.value = 0;
        cafes.value = [];
        hasMore.value = true;
    }
    
    if (!hasMore.value) return;
    
    loading.value = reset;
    loadingMore.value = !reset;
    error.value = null;
    
    try {
        const params = {
            keyword: keyword.value,
            page: page.value,
            size: 20
        };
        
        // Add location params if available
        if (locationEnabled.value && userLocation.value) {
            params.latitude = userLocation.value.latitude;
            params.longitude = userLocation.value.longitude;
            params.radius = radius.value * 1000; // Convert km to meters
        }
        
        const response = await api.get('/cafes', { params });
        
        const data = response.data;
        const newCafes = data.content || data;
        
        cafes.value.push(...newCafes);
        hasMore.value = !data.last;
        totalElements.value = data.totalElements || 0;
        page.value++;
        
    } catch (err) {
        error.value = "카페 목록을 불러오지 못했습니다.";
        console.error(err);
    } finally {
        loading.value = false;
        loadingMore.value = false;
    }
};

// Get user location
const getUserLocation = () => {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                userLocation.value = {
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                };
                locationEnabled.value = true;
                fetchCafes(true);
            },
            (err) => {
                console.error('Location error:', err);
                locationEnabled.value = false;
                fetchCafes(true);
            }
        );
    } else {
        fetchCafes(true);
    }
};

// Toggle location filter
const toggleLocation = () => {
    if (!locationEnabled.value) {
        getUserLocation();
    } else {
        locationEnabled.value = false;
        fetchCafes(true);
    }
};

// Change radius
const changeRadius = (newRadius) => {
    radius.value = newRadius;
    if (locationEnabled.value) {
        fetchCafes(true);
    }
};

// Setup intersection observer for infinite scroll
const setupObserver = () => {
    observer = new IntersectionObserver(
        (entries) => {
            if (entries[0].isIntersecting && hasMore.value && !loadingMore.value) {
                fetchCafes();
            }
        },
        { threshold: 0.1 }
    );
    
    if (loadMoreTrigger.value) {
        observer.observe(loadMoreTrigger.value);
    }
};

onMounted(() => {
    fetchCafes();
    setupObserver();
});

onUnmounted(() => {
    if (observer) {
        observer.disconnect();
    }
});

const emit = defineEmits(['select-cafe', 'switch-tab']);

const selectCafe = (id) => {
    emit('select-cafe', id);
};

// Calculate distance if location available
const getDistance = (cafe) => {
    if (!userLocation.value || !cafe.latitude || !cafe.longitude) return null;
    
    const R = 6371; // Earth radius in km
    const dLat = (cafe.latitude - userLocation.value.latitude) * Math.PI / 180;
    const dLon = (cafe.longitude - userLocation.value.longitude) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(userLocation.value.latitude * Math.PI / 180) * 
              Math.cos(cafe.latitude * Math.PI / 180) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    const distance = R * c;
    
    return distance < 1 ? `${Math.round(distance * 1000)}m` : `${distance.toFixed(1)}km`;
};
</script>

<template>
    <div class="space-y-0.5 bg-gray-50 pb-20">
        <!-- Search Area -->
        <div class="p-4 bg-white sticky top-14 z-40 border-b border-gray-100 shadow-sm">
            <div class="bg-gray-100 rounded-lg p-3 flex items-center gap-2 cursor-pointer text-gray-500 text-sm" @click="$emit('switch-tab', 'search')">
                <span>🔍</span>
                <span>근처 맛집, 카페 검색</span>
            </div>
            
            <!-- Location Filter -->
            <div class="flex items-center gap-2 mt-3">
                <button 
                    @click="toggleLocation"
                    :class="[
                        'flex items-center gap-1 px-3 py-1.5 rounded-full text-xs font-medium transition',
                        locationEnabled ? 'bg-creama-espresso text-white' : 'bg-gray-100 text-gray-600'
                    ]"
                >
                    <span>📍</span>
                    <span>{{ locationEnabled ? '내 주변' : '위치 사용' }}</span>
                </button>
                
                <!-- Radius selector -->
                <div v-if="locationEnabled" class="flex items-center gap-1">
                    <button 
                        v-for="r in [1, 3, 5, 10]" 
                        :key="r"
                        @click="changeRadius(r)"
                        :class="[
                            'px-2 py-1 rounded-full text-xs transition',
                            radius === r ? 'bg-creama-latte text-creama-espresso font-bold' : 'bg-gray-100 text-gray-500'
                        ]"
                    >
                        {{ r }}km
                    </button>
                </div>
            </div>
        </div>

        <!-- Curation Banners / Categories -->
        <div class="bg-white mb-2 pb-4">
             <!-- Horizontal Categories -->
             <div class="flex gap-4 overflow-x-auto px-4 py-2 custom-scrollbar">
                 <button v-for="cat in ['전체', '스페셜티', '디저트', '뷰맛집', '공부하기 좋은', '데이트']" :key="cat" class="flex-shrink-0 flex flex-col items-center gap-1 min-w-[60px]">
                     <div class="w-12 h-12 rounded-full bg-creama-latte border border-creama-latte flex items-center justify-center text-xl">☕</div>
                     <span class="text-xs font-medium text-gray-700">{{cat}}</span>
                 </button>
             </div>
             
             <!-- Promo Banner -->
             <div class="mx-4 mt-2 h-32 bg-creama-espresso rounded-xl flex items-center justify-center relative overflow-hidden">
                 <div class="absolute inset-0 bg-gradient-to-r from-creama-espresso to-creama-crema opacity-90"></div>
                 <div class="relative text-white text-center">
                     <h3 class="font-bold text-lg mb-1">이번 주 핫한 카페 🔥</h3>
                     <p class="text-xs opacity-90">웨이팅 없이 즐기는 여유</p>
                 </div>
             </div>
        </div>

        <!-- Sort Filter & Count -->
        <div class="px-4 py-3 flex items-center justify-between bg-white border-b border-gray-100">
             <div class="flex items-center gap-2">
                 <span class="font-bold text-gray-900 text-sm">
                     {{ locationEnabled ? '내 주변 카페' : '추천 카페' }}
                 </span>
                 <span v-if="totalElements > 0" class="text-xs text-gray-400">
                     ({{ totalElements.toLocaleString() }}개)
                 </span>
             </div>
             <div class="flex items-center gap-1 text-xs text-gray-500">
                 <span>{{ locationEnabled ? '거리순' : '추천순' }}</span>
                 <span>∨</span>
             </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="p-8 text-center text-gray-500">
            <div class="animate-spin w-8 h-8 border-2 border-creama-espresso border-t-transparent rounded-full mx-auto mb-2"></div>
            로딩 중...
        </div>
        
        <!-- Error State -->
        <div v-else-if="error" class="p-8 text-center text-red-500">
            {{ error }}
        </div>
        
        <!-- Cafe Feed -->
        <div v-else class="bg-white divide-y divide-gray-100">
            <div 
                v-for="cafe in cafes" 
                :key="cafe.id" 
                class="flex p-4 border-b border-gray-100 cursor-pointer active:bg-gray-50 transition"
                @click="selectCafe(cafe.id)"
            >
                <!-- Image -->
                <div class="w-28 h-28 flex-shrink-0 bg-gray-200 rounded-xl overflow-hidden mr-4 border border-gray-100">
                    <img :src="cafe.imageUrl || 'https://placehold.co/200x200/e0cec7/5d4037?text=No+Image'" alt="Cafe Image" class="w-full h-full object-cover"/>
                </div>

                <!-- Text Content -->
                <div class="flex flex-col justify-between py-0.5 flex-grow">
                    <div>
                        <div class="flex items-center gap-2 mb-0.5">
                            <h3 class="text-base font-medium text-gray-900 line-clamp-1">{{ cafe.name }}</h3>
                            <span v-if="getDistance(cafe)" class="text-xs text-creama-espresso font-medium">
                                📍 {{ getDistance(cafe) }}
                            </span>
                        </div>
                        <p class="text-xs text-gray-500 mb-1 line-clamp-1">{{ cafe.address ? cafe.address.split(',')[0] : '위치 정보 없음' }}</p>
                        <p class="text-sm text-gray-800 line-clamp-2 leading-snug">{{ cafe.description || cafe.businessType }}</p>
                    </div>
                    
                    <div class="flex items-center justify-end text-xs text-gray-400 gap-1 mt-2">
                         <div class="flex items-center gap-0.5">
                            <span class="text-gray-400">💬</span> <span>{{ cafe.reviewCount || 0 }}</span>
                         </div>
                         <div class="flex items-center gap-0.5 ml-2">
                            <span class="text-gray-400">🤍</span> <span>{{ cafe.bookmarkCount || 0 }}</span>
                         </div>
                    </div>
                </div>
            </div>
            
            <!-- Infinite Scroll Trigger -->
            <div ref="loadMoreTrigger" class="py-4 text-center">
                <div v-if="loadingMore" class="flex items-center justify-center gap-2 text-gray-500">
                    <div class="animate-spin w-5 h-5 border-2 border-creama-espresso border-t-transparent rounded-full"></div>
                    <span class="text-sm">더 불러오는 중...</span>
                </div>
                <div v-else-if="!hasMore && cafes.length > 0" class="text-gray-400 text-sm">
                    모든 카페를 불러왔습니다 ☕
                </div>
            </div>
        </div>
        
        <!-- Empty State -->
        <div v-if="!loading && cafes.length === 0" class="text-center py-20">
            <p class="text-4xl mb-4">☕</p>
            <p class="text-gray-500">검색 결과가 없습니다.</p>
            <p v-if="locationEnabled" class="text-gray-400 text-sm mt-2">
                반경을 넓혀보세요
            </p>
        </div>
    </div>
</template>
