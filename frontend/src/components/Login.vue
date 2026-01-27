<script setup>
import { ref } from 'vue';
import axios from 'axios';

const email = ref('');
const password = ref('');
const error = ref(null);

const emit = defineEmits(['login-success', 'go-signup']);

const handleLogin = async () => {
    try {
        const response = await axios.post('/api/auth/login', {
            email: email.value,
            password: password.value
        });
        
        // Save token
        const token = response.data.token;
        const user = {
            nickname: response.data.nickname,
            role: response.data.role
        };
        
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        
        emit('login-success', user);
    } catch (err) {
        console.error(err);
        if (err.response && err.response.data) {
            const data = err.response.data;
            error.value = typeof data === 'string' ? data : JSON.stringify(data);
        } else if (err.message) {
             error.value = err.message;
        } else {
            error.value = '로그인 중 오류가 발생했습니다.';
        }
    }
};
</script>

<template>
    <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-100 max-w-sm mx-auto mt-10">
        <h2 class="text-2xl font-bold text-creama-espresso mb-6 text-center">로그인</h2>
        
        <form @submit.prevent="handleLogin" class="space-y-4">
            <div>
                <label class="block text-sm font-bold text-gray-700 mb-1">이메일</label>
                <input v-model="email" type="email" required 
                    class="w-full bg-gray-50 border border-gray-200 rounded-lg p-3 focus:outline-none focus:ring-1 focus:ring-daangn-500 transition" 
                    placeholder="example@email.com" />
            </div>
            
            <div>
                <label class="block text-sm font-bold text-gray-700 mb-1">비밀번호</label>
                <input v-model="password" type="password" required 
                    class="w-full bg-gray-50 border border-gray-200 rounded-lg p-3 focus:outline-none focus:ring-1 focus:ring-creama-espresso transition" 
                    placeholder="비밀번호 입력" />
            </div>

            <div v-if="error" class="text-red-500 text-sm font-medium">{{ error }}</div>

            <button type="submit" class="w-full bg-creama-espresso text-white font-bold py-3 rounded-lg hover:bg-creama-espresso/90 transition shadow-md">
                로그인하기
            </button>
        </form>

        <div class="mt-6 text-center">
            <p class="text-sm text-gray-500">아직 회원이 아니신가요?</p>
            <button @click="$emit('go-signup')" class="text-creama-espresso font-bold text-sm hover:underline mt-1">
                이메일로 회원가입
            </button>
        </div>
    </div>
</template>
