<template>
    <div class="pb-20 bg-gray-50 min-h-screen">
        <header class="bg-white p-4 sticky top-0 z-10 border-b border-gray-100">
            <h1 class="text-xl font-bold text-daangn-500">저장</h1>
        </header>

        <div class="p-4">
            <!-- Filter Tabs -->
            <div class="flex gap-2 overflow-x-auto mb-6">
                <button class="bg-gray-800 text-white px-4 py-2 rounded-full text-sm font-bold whitespace-nowrap">전체</button>
                <button class="bg-white border border-gray-200 text-gray-600 px-4 py-2 rounded-full text-sm whitespace-nowrap">가고싶어요</button>
                <button class="bg-white border border-gray-200 text-gray-600 px-4 py-2 rounded-full text-sm whitespace-nowrap">가봤어요</button>
                <button class="bg-white border border-gray-200 text-gray-600 px-4 py-2 rounded-full text-sm whitespace-nowrap">나의 리스트</button>
            </div>

            <!-- List -->
            <div class="grid grid-cols-2 gap-4">
                 <div v-for="i in 4" :key="i" class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100">
                     <div class="h-32 bg-gray-200 relative">
                         <button class="absolute top-2 right-2 text-red-500 drop-shadow-sm">❤️</button>
                     </div>
                     <div class="p-3">
                         <h4 class="font-bold text-gray-900 truncate">에스프레소 랩</h4>
                         <p class="text-xs text-gray-500 mb-2">서울 서초구</p>
                         <div class="flex items-center gap-1 text-xs">
                             <span class="text-yellow-400">★</span> <span class="font-bold">4.8</span> <span class="text-gray-400">(120)</span>
                         </div>
                     </div>
                 </div>
            </div>
        </div>
    </div>
</template>
