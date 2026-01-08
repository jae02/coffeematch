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
    <div class="space-y-6">
        <!-- Search Bar -->
        <div class="flex gap-2 justify-center">
            <input 
                v-model="keyword" 
                @keyup.enter="fetchCafes"
                placeholder="카페 검색..." 
                class="border border-coffee-300 rounded-lg px-4 py-2 w-full max-w-md focus:outline-none focus:ring-2 focus:ring-coffee-500"
            />
            <button @click="fetchCafes" class="bg-coffee-600 text-white px-6 py-2 rounded-lg hover:bg-coffee-700 transition">
                검색
            </button>
        </div>

        <!-- Filter / Status -->
        <div v-if="loading" class="text-center text-coffee-500">커피 내리는 중... ☕</div>
        <div v-if="error" class="text-center text-red-500">{{ error }}</div>

        <!-- Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div 
                v-for="cafe in cafes" 
                :key="cafe.id" 
                class="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow cursor-pointer border border-coffee-100"
                @click="selectCafe(cafe.id)"
            >
                <img :src="cafe.imageUrl || 'https://placehold.co/600x400/e0cec7/5d4037?text=No+Image'" alt="Cafe Image" class="w-full h-48 object-cover"/>
                <div class="p-5">
                    <h3 class="text-xl font-bold text-coffee-800 mb-2">{{ cafe.name }}</h3>
                    <p class="text-coffee-600 text-sm mb-4 line-clamp-2">{{ cafe.description }}</p>
                    <div class="flex items-center text-sm text-coffee-500">
                        <span>📍 {{ cafe.address }}</span>
                    </div>
                </div>
            </div>
        </div>
        
        <div v-if="!loading && cafes.length === 0" class="text-center text-gray-400 py-10">
            취향에 맞는 카페가 없습니다.
        </div>
    </div>
</template>
