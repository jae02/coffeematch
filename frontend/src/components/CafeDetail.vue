<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const props = defineProps({
    id: [Number, String]
});

const emit = defineEmits(['back']);

const cafe = ref(null);
const loading = ref(true);
const error = ref(null);

// Review Form
const newReview = ref({
    author: '',
    rating: 5,
    content: ''
});

const api = axios.create({
    baseURL: 'http://localhost:8080/api'
});

const fetchCafe = async () => {
    loading.value = true;
    try {
        const response = await api.get(`/cafes/${props.id}`);
        cafe.value = response.data;
    } catch (err) {
        error.value = "카페 상세 정보를 불러오지 못했습니다.";
        console.error(err);
    } finally {
        loading.value = false;
    }
};

const submitReview = async () => {
    if (!newReview.value.author || !newReview.value.content) return alert("모든 필드를 입력해 주세요");
    
    try {
        await api.post(`/cafes/${props.id}/reviews`, newReview.value);
        // Refresh data
        await fetchCafe();
        // Reset form
        newReview.value = { author: '', rating: 5, content: '' };
        alert("리뷰가 등록되었습니다!");
    } catch (err) {
        alert("리뷰 등록에 실패했습니다");
    }
};

onMounted(() => {
    fetchCafe();
});

// Computed properties for sorting menus
const sortedMenus = computed(() => {
    if (!cafe.value || !cafe.value.menus) return [];
    return [...cafe.value.menus].sort((a, b) => a.price - b.price);
});
</script>

<template>
    <div class="bg-white min-h-screen pb-24 relative">
        <!-- Top Navigation (Transparent or Absolute) -->
        <div class="fixed top-0 left-0 right-0 z-50 flex items-center justify-between p-3 bg-transparent/50 backdrop-blur-sm md:hidden">
            <button @click="$emit('back')" class="text-white drop-shadow-md p-1">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                </svg>
            </button>
            <div class="flex gap-4 text-white drop-shadow-md p-1">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
                </svg>
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                </svg>
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
            <div class="w-full aspect-square md:aspect-video bg-gray-200 relative">
                <img :src="cafe.imageUrl || 'https://placehold.co/800x600/e0cec7/5d4037?text=Cafe'" class="w-full h-full object-cover" />
            </div>

            <!-- Profile / Info Section -->
            <div class="p-4 border-b border-gray-100">
                <div class="flex items-center justify-between mb-4">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-gray-200 overflow-hidden">
                             <!-- Profile Avatar Placeholder -->
                             <img :src="`https://ui-avatars.com/api/?name=${cafe.name}&background=random`" alt="Profile" />
                        </div>
                        <div>
                            <h2 class="text-lg font-bold text-gray-900 leading-none">{{ cafe.name }}</h2>
                            <p class="text-xs text-gray-500 mt-1">{{ cafe.address }}</p>
                        </div>
                    </div>
                    
                    <!-- Manner Temp -->
                    <div class="flex flex-col items-end">
                        <div class="flex items-center gap-1 text-daangn-500 font-bold text-base">
                            36.5°C 
                            <span class="text-lg">😊</span>
                        </div>
                        <div class="w-16 h-1 bg-gray-200 rounded-full mt-1 overflow-hidden">
                            <div class="h-full bg-daangn-500 w-[40%] rounded-full"></div>
                        </div>
                        <span class="text-[10px] text-gray-400 mt-1 underline">매너온도</span>
                    </div>
                </div>

                <div class="py-4">
                     <p class="text-gray-800 text-base leading-relaxed whitespace-pre-line">{{ cafe.description }}</p>
                </div>
            </div>

            <!-- Menu Section -->
            <div class="p-4 border-b border-gray-100">
                <h3 class="font-bold text-gray-900 mb-4">메뉴</h3>
                <div class="space-y-3">
                    <div v-for="menu in sortedMenus" :key="menu.id" class="flex justify-between items-center">
                        <div class="flex items-center gap-2">
                             <span v-if="menu.recommended" class="text-daangn-500 text-xs border border-daangn-500 px-1 rounded font-bold">추천</span>
                             <span class="text-gray-900">{{ menu.itemName }}</span>
                        </div>
                        <span class="font-bold text-gray-900">{{ menu.price.toLocaleString() }}원</span>
                    </div>
                </div>
            </div>

            <!-- Reviews Section -->
            <div class="p-4 bg-gray-50 pb-20">
                 <div class="flex items-center justify-between mb-4">
                    <h3 class="font-bold text-gray-900">리뷰 {{ cafe.reviews ? cafe.reviews.length : 0 }}</h3>
                 </div>
                 
                 <!-- Review List -->
                 <div class="space-y-3 mb-6">
                    <div v-for="review in cafe.reviews" :key="review.id" class="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
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
                        <p class="text-gray-600 text-sm pl-8">{{ review.content }}</p>
                        <span class="text-xs text-gray-400 pl-8 mt-1 block">{{ review.createdAt ? new Date(review.createdAt).toLocaleDateString() : '방금 전' }}</span>
                    </div>
                 </div>

                 <!-- Simple Review Form (Collapsible or just inline) -->
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
                        <textarea v-model="newReview.content" rows="2" placeholder="솔직한 후기를 남겨주세요." class="w-full bg-gray-50 rounded-lg p-2 text-sm border-none focus:ring-1 focus:ring-gray-300" required></textarea>
                        <button type="submit" class="w-full bg-gray-100 text-gray-600 hover:bg-daangn-100 hover:text-daangn-600 font-bold py-2 rounded-lg transition text-sm">
                            등록하기
                        </button>
                    </form>
                 </div>
            </div>

            <!-- Bottom Fixed Action Bar -->
            <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-3 flex justify-between items-center max-w-2xl mx-auto z-50 safe-area-bottom">
                 <div class="flex items-center gap-3 pl-2">
                     <button class="text-gray-400 flex flex-col items-center">
                         <span class="text-xl">🤍</span>
                         <span class="text-[10px]">12</span>
                     </button>
                 </div>
                 <div class="flex items-center gap-2">
                     <span class="font-bold text-lg text-gray-900 pr-4 border-r border-gray-200 mr-4">가격 문의</span>
                     <button class="bg-daangn-500 text-white font-bold px-6 py-2.5 rounded-lg hover:bg-daangn-600 transition">
                         채팅하기
                     </button>
                 </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1; 
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #a1887f; 
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #5d4037; 
}
</style>
