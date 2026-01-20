<script setup>
import { ref, computed, onMounted } from 'vue';
import CafeList from './components/CafeList.vue';
import CafeDetail from './components/CafeDetail.vue';
import Search from './components/Search.vue';
import Saved from './components/Saved.vue';
import MyPage from './components/MyPage.vue';
import AdminDashboard from './components/AdminDashboard.vue';
import AdminLogin from './components/AdminLogin.vue'; // [NEW]

const currentView = ref('list');
const selectedCafeId = ref(null);

const showDetail = (id) => {
  selectedCafeId.value = id;
  currentView.value = 'detail';
};

const showList = () => {
  currentView.value = 'list';
};

const changeTab = (tab) => {
  currentView.value = tab;
};

// Handle Admin Navigation
const showAdminLogin = () => {
  currentView.value = 'admin-login';
};

const handleAdminLoginSuccess = () => {
  currentView.value = 'admin';
};

// Check for #admin hash on load
onMounted(() => {
  if (window.location.hash === '#admin') {
    currentView.value = 'admin-login';
  }
  window.addEventListener('hashchange', () => {
    if (window.location.hash === '#admin') {
      currentView.value = 'admin-login';
    }
  });
});
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-900">
    <!-- Header (Hidden in Admin Mode) -->
    <header v-if="currentView !== 'admin' && currentView !== 'admin-login'" class="bg-white sticky top-0 z-10 border-b border-gray-100 shadow-sm safe-area-top">
      <div class="max-w-2xl mx-auto px-4 h-14 flex items-center justify-between">
        <h1 class="text-xl font-black tracking-tighter text-crema-espresso cursor-pointer" @click="currentView = 'list'">Crema</h1>
        <div class="flex gap-4">
            <button @click="currentView = 'search'" :class="currentView === 'search' ? 'text-gray-900' : 'text-gray-400'">🔍</button>
        </div>
      </div>
    </header>

    <!-- Content Area -->
    <main :class="(currentView === 'admin' || currentView === 'admin-login') ? 'w-full h-screen' : 'max-w-2xl mx-auto'">
      <CafeList v-if="currentView === 'list'" @select-cafe="showDetail" @switch-tab="changeTab" />
      <CafeDetail v-else-if="currentView === 'detail'" :id="selectedCafeId" @back="showList" />
      <Search v-else-if="currentView === 'search'" />
      <Saved v-else-if="currentView === 'saved'" />
      <MyPage v-else-if="currentView === 'mypage'" @open-admin="showAdminLogin" />
      <AdminDashboard v-else-if="currentView === 'admin'" @close="changeTab('list')" />
      <AdminLogin v-else-if="currentView === 'admin-login'" @login-success="handleAdminLoginSuccess" @back="changeTab('mypage')" />
    </main>
    
    <!-- CatchTable Style Bottom Nav -->
    <nav v-if="currentView !== 'detail' && currentView !== 'admin' && currentView !== 'admin-login'" class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 h-16 flex justify-around items-center max-w-2xl mx-auto z-40 safe-area-bottom">
        <!-- Home -->
        <button 
            @click="changeTab('list')" 
            class="flex flex-col items-center justify-center w-full h-full"
            :class="currentView === 'list' ? 'text-crema-espresso' : 'text-gray-400'"
        >
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <span class="text-[10px] mt-1 font-medium">홈</span>
        </button>

        <!-- Search -->
        <button 
            @click="changeTab('search')" 
            class="flex flex-col items-center justify-center w-full h-full"
            :class="currentView === 'search' ? 'text-crema-espresso' : 'text-gray-400'"
        >
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <span class="text-[10px] mt-1 font-medium">검색</span>
        </button>

        <!-- Saved -->
        <button 
            @click="changeTab('saved')" 
            class="flex flex-col items-center justify-center w-full h-full"
            :class="currentView === 'saved' ? 'text-crema-espresso' : 'text-gray-400'"
        >
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
            </svg>
            <span class="text-[10px] mt-1 font-medium">저장</span>
        </button>

        <!-- MY -->
        <button 
            @click="changeTab('mypage')" 
            class="flex flex-col items-center justify-center w-full h-full"
            :class="currentView === 'mypage' ? 'text-crema-espresso' : 'text-gray-400'"
        >
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <span class="text-[10px] mt-1 font-medium">MY</span>
        </button>
    </nav>
  </div>
</template>
