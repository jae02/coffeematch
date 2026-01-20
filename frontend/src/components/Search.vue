<template>
    <div class="pb-20 bg-gray-50 min-h-screen">
        <!-- Search Header -->
        <div class="bg-white p-4 sticky top-0 z-10 border-b border-gray-100">
            <h1 class="text-xl font-bold text-crema-espresso mb-4">검색</h1>
            <div class="relative">
                 <input 
                    type="text" 
                    placeholder="지역, 음식, 식당명 검색" 
                    class="w-full bg-gray-100 rounded-lg p-3 pl-10 text-sm focus:outline-none focus:ring-1 focus:ring-crema-crema"
                />
                <span class="absolute left-3 top-3 text-gray-400">🔍</span>
            </div>
        </div>

        <!-- Recent / Recommendations -->
        <div class="p-4 space-y-6">
            <!-- Recent Searches -->
            <section>
                <div class="flex justify-between items-center mb-3">
                    <h3 class="font-bold text-gray-800">최근 검색어</h3>
                    <button class="text-xs text-gray-400">지우기</button>
                </div>
                <div class="flex gap-2 overflow-x-auto">
                    <span class="bg-white border border-gray-200 px-3 py-1.5 rounded-full text-sm text-gray-600">강남역</span>
                    <span class="bg-white border border-gray-200 px-3 py-1.5 rounded-full text-sm text-gray-600">스타벅스</span>
                </div>
            </section>

            <!-- Recommended Categories -->
            <section>
                <h3 class="font-bold text-gray-800 mb-3">추천 검색어</h3>
                <div class="flex flex-wrap gap-2">
                     <span class="bg-crema-latte text-crema-espresso px-3 py-1.5 rounded-full text-sm font-medium">☕ 분위기 좋은 카페</span>
                     <span class="bg-crema-latte text-crema-espresso px-3 py-1.5 rounded-full text-sm font-medium">💻 카공하기 좋은</span>
                     <span class="bg-crema-latte text-crema-espresso px-3 py-1.5 rounded-full text-sm font-medium">🍰 디저트 맛집</span>
                </div>
            </section>
        </div>
    </div>
</template>
