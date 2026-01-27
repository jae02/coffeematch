<script setup>
import { ref } from 'vue';
import axios from 'axios';

const email = ref('admin@example.com');
const password = ref('password123');
const errorMsg = ref('');

const emit = defineEmits(['login-success', 'back']);

const handleLogin = async () => {
    try {
        const response = await axios.post('/api/auth/login', {
            email: email.value,
            password: password.value
        });
        
        const { token, role, nickname } = response.data;
        
        if (role !== 'ROLE_ADMIN') {
            errorMsg.value = '관리자 계정이 아닙니다.';
            return;
        }

        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify({ nickname, role }));
        
        emit('login-success');
    } catch (err) {
        errorMsg.value = '로그인 실패: 아이디, 비밀번호를 확인하세요.';
    }
};
</script>

<template>
    <div class="min-h-screen bg-slate-900 flex items-center justify-center p-4">
        <div class="bg-slate-800 w-full max-w-md p-8 rounded-2xl shadow-2xl border border-slate-700 animate-fade-in-up">
            <div class="text-center mb-8">
                <div class="inline-block p-3 rounded-full bg-blue-600/20 mb-4">
                    <span class="text-3xl">🛡️</span>
                </div>
                <h1 class="text-2xl font-black text-white tracking-widest uppercase mb-1">Creama Admin</h1>
                <p class="text-slate-400 text-sm">Authorized Personnel Only</p>
            </div>

            <form @submit.prevent="handleLogin" class="space-y-6">
                <div>
                    <label class="block text-slate-300 text-xs font-bold uppercase mb-2">Admin ID</label>
                    <input v-model="email" type="email" required 
                        class="w-full bg-slate-900 border border-slate-600 text-white p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all placeholder-slate-600" 
                        placeholder="admin@example.com" />
                </div>
                <div>
                    <label class="block text-slate-300 text-xs font-bold uppercase mb-2">Password</label>
                    <input v-model="password" type="password" required 
                        class="w-full bg-slate-900 border border-slate-600 text-white p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all placeholder-slate-600" 
                        placeholder="••••••••" />
                </div>

                <div v-if="errorMsg" class="p-3 bg-red-500/10 border border-red-500/50 rounded-lg text-red-500 text-sm font-bold text-center">
                    {{ errorMsg }}
                </div>

                <button type="submit" class="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-4 rounded-lg shadow-lg shadow-blue-600/30 transition-all transform hover:scale-[1.02]">
                    ACCESS DASHBOARD
                </button>
            </form>

            <div class="mt-8 text-center border-t border-slate-700 pt-6">
                <button @click="$emit('back')" class="text-slate-500 hover:text-slate-300 text-sm font-bold transition-colors">
                    ← Back to Creama
                </button>
            </div>
        </div>
    </div>
</template>
