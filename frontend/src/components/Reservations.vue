<template>
    <div class="pb-20 bg-gray-50 min-h-screen">
        <header class="bg-white p-4 sticky top-0 z-10 border-b border-gray-100 flex justify-between items-center">
            <h1 class="text-xl font-bold text-creama-espresso">예약/웨이팅</h1>
            <span class="text-gray-400">📅</span>
        </header>

        <div class="p-4 space-y-4">
             <!-- Not signed in / No reservations state -->
             <div class="bg-white rounded-xl p-6 text-center shadow-sm border border-gray-100">
                 <div class="text-4xl mb-2">🍽️</div>
                 <h3 class="text-lg font-bold text-gray-800 mb-1">예정된 일정이 없어요</h3>
                 <p class="text-gray-500 text-sm mb-4">맛있는 커피 한 잔 예약해보시는 건 어때요?</p>
                 <button class="bg-creama-espresso text-white w-full py-3 rounded-lg font-bold hover:bg-creama-espresso/90 transition">
                     맛집 찾아보기
                 </button>
             </div>

             <!-- Waiting Hotplaces -->
             <section class="mt-8">
                 <h2 class="text-lg font-bold text-gray-800 mb-4 flex items-center gap-1">
                     🔥 지금 핫한 웨이팅 카페
                 </h2>
                 <div class="space-y-3">
                     <div v-for="i in 3" :key="i" class="flex gap-4 bg-white p-3 rounded-xl border border-gray-100">
                         <div class="w-20 h-20 bg-gray-200 rounded-lg shrink-0"></div>
                         <div>
                             <h4 class="font-bold text-gray-900">카페 모닝듀 {{i}}호점</h4>
                             <p class="text-xs text-gray-500 mb-2">서울 강남구 • 카페</p>
                             <div class="flex items-center gap-1 text-xs text-red-500 font-bold">
                                 <span>👥 현재 대기 1{{i}}팀</span>
                             </div>
                         </div>
                     </div>
                 </div>
             </section>
        </div>
    </div>
</template>
