<script setup>
import { ref } from 'vue';
import CafeList from './components/CafeList.vue';
import CafeDetail from './components/CafeDetail.vue';

const currentView = ref('list');
const selectedCafeId = ref(null);

const showDetail = (id) => {
  selectedCafeId.value = id;
  currentView.value = 'detail';
};

const showList = () => {
  currentView.value = 'list';
  selectedCafeId.value = null;
};
</script>

<template>
  <div class="min-h-screen bg-coffee-50 font-sans text-coffee-900">
    <header class="bg-coffee-700 text-white p-4 shadow-md">
      <div class="container mx-auto flex justify-between items-center">
        <h1 class="text-2xl font-bold cursor-pointer" @click="showList">커피매치 ☕</h1>
        <nav>
          <button @click="showList" class="hover:text-coffee-200">홈</button>
        </nav>
      </div>
    </header>

    <main class="container mx-auto p-4">
      <CafeList v-if="currentView === 'list'" @select-cafe="showDetail" />
      <CafeDetail v-else :id="selectedCafeId" @back="showList" />
    </main>
    
    <footer class="bg-coffee-800 text-coffee-200 p-4 text-center mt-8">
      &copy; 2024 커피매치 서비스
    </footer>
  </div>
</template>
