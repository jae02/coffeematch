<script setup>
import { ref, onMounted } from 'vue';
import Login from './Login.vue';
import Signup from './Signup.vue';

const isLoggedIn = ref(false);
const user = ref(null);
const view = ref('login'); // 'login', 'signup', 'profile'

onMounted(() => {
    checkLoginStatus();
});

const checkLoginStatus = () => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    
    if (token && userData) {
        isLoggedIn.value = true;
        user.value = JSON.parse(userData);
        view.value = 'profile';
    } else {
        isLoggedIn.value = false;
        user.value = null;
        view.value = 'login';
    }
};

const handleLoginSuccess = (userData) => {
    checkLoginStatus();
};

const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    checkLoginStatus();
};
</script>

<template>
    <div class="pb-20 bg-gray-50 min-h-screen">
        <header class="bg-white p-4 sticky top-0 z-10 flex justify-between items-center border-b border-gray-100">
             <h1 class="text-xl font-bold text-crema-espresso">마이페이지</h1>
             <div v-if="isLoggedIn" class="flex gap-4">
                 <button class="text-gray-500 font-bold text-sm">설정</button>
                 <button @click="handleLogout" class="text-red-500 font-bold text-sm">로그아웃</button>
             </div>
        </header>

        <div class="p-4">
            <!-- Auth Views -->
            <div v-if="!isLoggedIn">
                <Login 
                    v-if="view === 'login'" 
                    @login-success="handleLoginSuccess" 
                    @go-signup="view = 'signup'" 
                />
                <Signup 
                    v-else-if="view === 'signup'" 
                    @signup-success="view = 'login'" 
                    @go-login="view = 'login'" 
                />
            </div>

            <!-- Profile View -->
            <div v-else class="space-y-2">
                <!-- Profile -->
                <div class="bg-white p-6 rounded-xl flex items-center gap-4 mb-4 shadow-sm border border-gray-100">
                    <div class="w-16 h-16 bg-gray-200 rounded-full overflow-hidden">
                        <img :src="`https://ui-avatars.com/api/?name=${user.nickname}&background=A67B5B&color=fff`" alt="Profile" />
                    </div>
                    <div class="flex-grow">
                        <h2 class="text-xl font-bold text-gray-900">{{ user.nickname }}님</h2>
                        <p class="text-sm text-gray-500">환영합니다! ☕</p>
                    </div>
                </div>

                <!-- Dashboard Stats -->
                <div class="bg-white p-4 rounded-xl flex justify-around text-center shadow-sm border border-gray-100">
                    <div>
                        <div class="text-lg font-bold text-gray-800">0</div>
                        <div class="text-xs text-gray-500">예약/웨이팅</div>
                    </div>
                    <div class="w-px bg-gray-100 h-10"></div>
                    <div>
                        <div class="text-lg font-bold text-gray-800">12</div>
                        <div class="text-xs text-gray-500">저장</div>
                    </div>
                    <div class="w-px bg-gray-100 h-10"></div>
                    <div>
                        <div class="text-lg font-bold text-gray-800">3</div>
                        <div class="text-xs text-gray-500">리뷰</div>
                    </div>
                </div>

                <!-- Banner -->
                <div class="bg-crema-latte p-4 rounded-xl flex items-center justify-between border border-crema-latte">
                    <div>
                        <h3 class="font-bold text-crema-espresso">Crema VIP 되기</h3>
                        <p class="text-xs text-crema-espresso opacity-80">더 많은 혜택을 누려보세요!</p>
                    </div>
                    <span class="text-2xl">☕</span>
                </div>

                <!-- Menu List -->
                <div class="bg-white rounded-xl overflow-hidden border border-gray-100 mt-4">
                    <button @click="$emit('open-admin')" class="w-full py-4 text-left px-4 hover:bg-gray-50 text-gray-500 font-medium border-t border-gray-100 flex justify-between items-center group">
                        <span>🔐 관리자 전용 (Admin Access)</span>
                        <span class="text-gray-300 group-hover:text-gray-500 transition-colors">→</span>
                    </button>
                    <button class="w-full flex justify-between items-center p-4 border-b border-gray-50 hover:bg-gray-50">
                        <span class="text-gray-800">이벤트</span>
                        <span class="text-gray-400">></span>
                    </button>
                    <button class="w-full flex justify-between items-center p-4 border-b border-gray-50 hover:bg-gray-50">
                        <span class="text-gray-800">공지사항</span>
                        <span class="text-gray-400">></span>
                    </button>
                    <button class="w-full flex justify-between items-center p-4 hover:bg-gray-50">
                        <span class="text-gray-800">고객센터</span>
                        <span class="text-gray-400">></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
