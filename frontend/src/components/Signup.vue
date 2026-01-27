<script setup>
import { ref } from 'vue';
import axios from 'axios';

const email = ref('');
const password = ref('');
const nickname = ref('');
const error = ref(null);

const emit = defineEmits(['signup-success', 'go-login']);

const handleSignup = async () => {
    try {
        await axios.post('/api/auth/signup', {
            email: email.value,
            password: password.value,
            nickname: nickname.value
        });
        
        alert('회원가입이 완료되었습니다! 로그인해주세요.');
        emit('signup-success');
    } catch (err) {
        if (err.response && err.response.data) {
            error.value = err.response.data; // e.g. "Email already exists"
        } else {
            error.value = '회원가입 중 오류가 발생했습니다.';
        }
        console.error(err);
    }
};
</script>

<template>
    <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-100 max-w-sm mx-auto mt-10">
        <h2 class="text-2xl font-bold text-creama-espresso mb-6 text-center">회원가입</h2>
        
        <form @submit.prevent="handleSignup" class="space-y-4">
             <div>
                <label class="block text-sm font-bold text-gray-700 mb-1">닉네임</label>
                <input v-model="nickname" type="text" required 
                    class="w-full bg-gray-50 border border-gray-200 rounded-lg p-3 focus:outline-none focus:ring-1 focus:ring-daangn-500 transition" 
                    placeholder="멋진 닉네임" />
            </div>

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
                    placeholder="비밀번호 (6자리 이상)" />
            </div>

            <div v-if="error" class="text-red-500 text-sm font-medium">{{ error }}</div>

            <button type="submit" class="w-full bg-creama-espresso text-white font-bold py-3 rounded-lg hover:bg-creama-espresso/90 transition shadow-md">
                가입하기
            </button>
        </form>

        <div class="mt-6 text-center">
            <p class="text-sm text-gray-500">이미 계정이 있으신가요?</p>
            <button @click="$emit('go-login')" class="text-creama-espresso font-bold text-sm hover:underline mt-1">
                로그인하러 가기
            </button>
        </div>
    </div>
</template>
