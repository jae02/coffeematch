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
    <div class="max-w-4xl mx-auto space-y-8">
        <button @click="$emit('back')" class="text-coffee-600 hover:text-coffee-800 flex items-center gap-2 mb-4 font-semibold">
            &larr; 목록으로 돌아가기
        </button>

        <div v-if="loading" class="text-center py-10">카페 상세 정보 로딩 중...</div>
        <div v-else-if="error" class="text-red-500 text-center">{{ error }}</div>
        
        <div v-else class="space-y-8 animate-fade-in">
            <!-- Header Section -->
            <div class="bg-white rounded-2xl shadow-xl overflow-hidden">
                <div class="relative h-64 md:h-80">
                    <img :src="cafe.imageUrl || 'https://placehold.co/800x400/e0cec7/5d4037?text=Cafe'" class="w-full h-full object-cover" />
                    <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-6 text-white">
                        <h2 class="text-4xl font-bold mb-2">{{ cafe.name }}</h2>
                        <p class="text-lg opacity-90">{{ cafe.address }} | 📞 {{ cafe.phone }}</p>
                    </div>
                </div>
                <div class="p-8">
                    <p class="text-coffee-800 text-lg leading-relaxed">{{ cafe.description }}</p>
                </div>
            </div>

            <!-- Menu Section -->
            <div class="bg-white rounded-xl shadow-lg p-6 border border-coffee-100">
                <h3 class="text-2xl font-bold text-coffee-800 mb-6 border-b border-coffee-200 pb-2">메뉴</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div v-for="menu in sortedMenus" :key="menu.id" 
                        class="flex justify-between items-center p-3 rounded-lg hover:bg-coffee-50 transition"
                        :class="{'border-l-4 border-yellow-400 pl-2': menu.recommended}">
                        <div class="flex items-center gap-2">
                             <span v-if="menu.recommended" title="추천" class="text-yellow-500">⭐</span>
                             <span class="font-medium text-coffee-900">{{ menu.itemName }}</span>
                        </div>
                        <span class="font-bold text-coffee-700 w-24 text-right">{{ menu.price.toLocaleString() }} 원</span>
                    </div>
                </div>
            </div>

            <!-- Reviews Section -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <!-- Review List -->
                <div class="bg-white rounded-xl shadow-lg p-6 h-fit">
                    <h3 class="text-2xl font-bold text-coffee-800 mb-4">리뷰 ({{ cafe.reviews ? cafe.reviews.length : 0 }})</h3>
                    <div class="space-y-4 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
                        <div v-if="cafe.reviews.length === 0" class="text-gray-400 italic">아직 리뷰가 없습니다. 첫 번째 리뷰를 남겨보세요!</div>
                        <div v-for="review in cafe.reviews" :key="review.id" class="border-b border-coffee-100 pb-4 last:border-0">
                            <div class="flex justify-between items-center mb-1">
                                <span class="font-bold text-coffee-800">{{ review.author }}</span>
                                <div class="text-yellow-500">
                                    {{ '★'.repeat(review.rating) }}{{ '☆'.repeat(5 - review.rating) }}
                                </div>
                            </div>
                            <p class="text-coffee-600 text-sm">{{ review.content }}</p>
                            <span class="text-xs text-gray-400 block mt-1">{{ review.createdAt ? new Date(review.createdAt).toLocaleDateString() : '방금 전' }}</span>
                        </div>
                    </div>
                </div>

                <!-- Add Review Form -->
                <div class="bg-coffee-50 rounded-xl shadow-inner p-6 h-fit">
                    <h3 class="text-xl font-bold text-coffee-800 mb-4">리뷰 작성</h3>
                    <form @submit.prevent="submitReview" class="space-y-4">
                        <div>
                            <label class="block text-sm font-bold text-coffee-700 mb-1">이름</label>
                            <input v-model="newReview.author" type="text" class="w-full rounded border-coffee-300 p-2 focus:ring-coffee-500" required />
                        </div>
                        <div>
                            <label class="block text-sm font-bold text-coffee-700 mb-1">별점</label>
                            <select v-model="newReview.rating" class="w-full rounded border-coffee-300 p-2 focus:ring-coffee-500">
                                <option :value="5">⭐⭐⭐⭐⭐ (최고예요)</option>
                                <option :value="4">⭐⭐⭐⭐ (좋아요)</option>
                                <option :value="3">⭐⭐⭐ (보통이에요)</option>
                                <option :value="2">⭐⭐ (별로예요)</option>
                                <option :value="1">⭐ (최악이에요)</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-sm font-bold text-coffee-700 mb-1">내용</label>
                            <textarea v-model="newReview.content" rows="3" class="w-full rounded border-coffee-300 p-2 focus:ring-coffee-500" required></textarea>
                        </div>
                        <button type="submit" class="w-full bg-coffee-700 text-white font-bold py-2 rounded hover:bg-coffee-800 transition">
                            리뷰 등록
                        </button>
                    </form>
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
