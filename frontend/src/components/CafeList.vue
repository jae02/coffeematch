<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const cafes = ref([]);
const keyword = ref('');
const loading = ref(false);
const error = ref(null);

const api = axios.create({
    baseURL: 'http://localhost:8080/api'
});

const fetchCafes = async () => {
    loading.value = true;
    error.value = null;
    try {
        const response = await api.get('/cafes', {
            params: { keyword: keyword.value }
        });
        cafes.value = response.data;
    } catch (err) {
        error.value = "카페 목록을 불러오지 못했습니다. 백엔드가 실행 중인가요?";
        console.error(err);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchCafes();
});

const emit = defineEmits(['select-cafe']);

const selectCafe = (id) => {
    emit('select-cafe', id);
};
</script>

<template>
    <div class="space-y-0.5 bg-gray-50 pb-20"> <!-- Added padding for bottom nav -->
        <!-- Search Area (Optional, since we have a dedicated search tab, we might keep a simple trigger or remove it. Keeping it as a quick entry) -->
        <div class="p-4 bg-white sticky top-14 z-40 border-b border-gray-100 shadow-sm">
            <div class="bg-gray-100 rounded-lg p-3 flex items-center gap-2 cursor-pointer text-gray-500 text-sm" @click="$emit('switch-tab', 'search')">
                <span>🔍</span>
                <span>근처 맛집, 카페 검색</span>
            </div>
        </div>

        <!-- Curation Banners / Categories -->
        <div class="bg-white mb-2 pb-4">
             <!-- Horizontal Categories -->
             <div class="flex gap-4 overflow-x-auto px-4 py-2 custom-scrollbar">
                 <button v-for="cat in ['전체', '스페셜티', '디저트', '뷰맛집', '공부하기 좋은', '데이트']" :key="cat" class="flex-shrink-0 flex flex-col items-center gap-1 min-w-[60px]">
                     <div class="w-12 h-12 rounded-full bg-daangn-50 border border-daangn-100 flex items-center justify-center text-xl">☕</div>
                     <span class="text-xs font-medium text-gray-700">{{cat}}</span>
                 </button>
             </div>
             
             <!-- Promo Banner -->
             <div class="mx-4 mt-2 h-32 bg-daangn-500 rounded-xl flex items-center justify-center relative overflow-hidden">
                 <div class="absolute inset-0 bg-gradient-to-r from-daangn-600 to-daangn-400 opacity-90"></div>
                 <div class="relative text-white text-center">
                     <h3 class="font-bold text-lg mb-1">이번 주 핫한 카페 🔥</h3>
                     <p class="text-xs opacity-90">웨이팅 없이 즐기는 여유</p>
                 </div>
             </div>
        </div>

        <!-- Sort Filter -->
        <div class="px-4 py-3 flex items-center justify-between bg-white border-b border-gray-100">
             <span class="font-bold text-gray-900 text-sm">내 주변 추천 카페</span>
             <div class="flex items-center gap-1 text-xs text-gray-500">
                 <span>추천순</span>
                 <span>∨</span>
             </div>
        </div>

        <!-- Cafe Feed -->
        <div v-if="loading" class="p-8 text-center text-gray-500">
            로딩 중...
        </div>
        <div v-else-if="error" class="p-8 text-center text-red-500">
            {{ error }}
        </div>
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
                        <h3 class="text-base font-medium text-gray-900 mb-0.5 line-clamp-1">{{ cafe.name }}</h3>
                        <p class="text-xs text-gray-500 mb-1 line-clamp-1">{{ cafe.address ? cafe.address.split(',')[0] : '위치 정보 없음' }}</p>
                        <p class="text-sm text-gray-800 line-clamp-2 leading-snug">{{ cafe.description }}</p>
                    </div>
                    
                    <div class="flex items-center justify-end text-xs text-gray-400 gap-1 mt-2">
                         <!-- Like / Comment Counts (Mock) -->
                         <div class="flex items-center gap-0.5">
                            <span class="text-gray-400">💬</span> <span>{{ cafe.reviews ? cafe.reviews.length : 0 }}</span>
                         </div>
                         <div class="flex items-center gap-0.5 ml-2">
                            <span class="text-gray-400">🤍</span> <span>12</span>
                         </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div v-if="!loading && cafes.length === 0" class="text-center py-20">
            <p class="text-gray-500">검색 결과가 없습니다.</p>
        </div>
    </div>
</template>
