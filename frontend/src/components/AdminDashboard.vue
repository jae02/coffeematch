<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const stats = ref({ totalCafes: 0, totalReviews: 0 });
const activeMenu = ref('dashboard');

// Data
const cafes = ref([]);
const reviews = ref([]);
const selectedCafeForMenu = ref(null);
const cafeMenus = ref([]);

// Forms
const showCafeForm = ref(false);
const editingCafe = ref(null);
const cafeForm = ref({ name: '', address: '', phone: '', imageUrl: '', description: '' });

const menuForm = ref({ itemName: '', price: '', isRecommended: false });
const editingMenuId = ref(null);

const emit = defineEmits(['close']);

// Fetch Logic
const fetchStats = async () => {
    try {
        const token = localStorage.getItem('token');
        const res = await axios.get('/api/admin/stats', { headers: { Authorization: `Bearer ${token}` } });
        stats.value = res.data;
    } catch (err) { console.error(err); }
};

const fetchCafes = async () => {
    try {
        const res = await axios.get('/api/cafes');
        cafes.value = res.data;
    } catch (err) { console.error(err); }
};

const fetchReviews = async () => {
    try {
        const token = localStorage.getItem('token');
        const res = await axios.get('/api/admin/reviews', { headers: { Authorization: `Bearer ${token}` } });
        reviews.value = res.data;
    } catch (err) { console.error(err); }
};

onMounted(() => { fetchStats(); fetchCafes(); fetchReviews(); });

// Handlers
const openCreateCafe = () => { editingCafe.value = null; cafeForm.value = { name: '', address: '', phone: '', imageUrl: '', description: '' }; showCafeForm.value = true; };
const editCafe = (cafe) => { editingCafe.value = cafe; cafeForm.value = { ...cafe }; showCafeForm.value = true; };
const deleteCafe = async (id) => {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
        const token = localStorage.getItem('token');
        await axios.delete(`/api/admin/cafes/${id}`, { headers: { Authorization: `Bearer ${token}` } });
        fetchCafes(); fetchStats();
    } catch { alert('실패'); }
};
const submitCafeForm = async () => {
    try {
        const token = localStorage.getItem('token');
        const config = { headers: { Authorization: `Bearer ${token}` } };
        editingCafe.value ? await axios.put(`/api/admin/cafes/${editingCafe.value.id}`, cafeForm.value, config) : await axios.post('/api/admin/cafes', cafeForm.value, config);
        showCafeForm.value = false; fetchCafes(); fetchStats();
    } catch { alert('저장 실패'); }
};

const openMenuModal = (cafe) => {
    selectedCafeForMenu.value = cafe;
    cafeMenus.value = cafe.menus || [];
    menuForm.value = { itemName: '', price: '', isRecommended: false };
    editingMenuId.value = null;
};
const closeMenuModal = () => { selectedCafeForMenu.value = null; fetchCafes(); };
const submitMenuForm = async () => {
    try {
        const token = localStorage.getItem('token');
        const config = { headers: { Authorization: `Bearer ${token}` } };
        const payload = { ...menuForm.value, price: parseInt(menuForm.value.price) };
        editingMenuId.value ? await axios.put(`/api/admin/menus/${editingMenuId.value}`, payload, config) : await axios.post(`/api/admin/cafes/${selectedCafeForMenu.value.id}/menus`, payload, config);
        await fetchCafes();
        const updatedCafe = cafes.value.find(c => c.id === selectedCafeForMenu.value.id);
        if (updatedCafe) cafeMenus.value = updatedCafe.menus;
        menuForm.value = { itemName: '', price: '', isRecommended: false };
        editingMenuId.value = null;
    } catch { alert('메뉴 저장 실패'); }
};
const deleteMenu = async (menuId) => {
    if(!confirm('삭제하시겠습니까?')) return;
    try {
        const token = localStorage.getItem('token');
        await axios.delete(`/api/admin/menus/${menuId}`, { headers: { Authorization: `Bearer ${token}` } });
        await fetchCafes();
        const updatedCafe = cafes.value.find(c => c.id === selectedCafeForMenu.value.id);
        if (updatedCafe) cafeMenus.value = updatedCafe.menus;
    } catch { alert('메뉴 삭제 실패'); }
};
const editMenu = (menu) => { editingMenuId.value = menu.id; menuForm.value = { itemName: menu.itemName, price: menu.price, isRecommended: menu.recommended }; };
const deleteReview = async (id) => {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
        const token = localStorage.getItem('token');
        await axios.delete(`/api/admin/reviews/${id}`, { headers: { Authorization: `Bearer ${token}` } });
        fetchReviews(); fetchStats();
    } catch { alert('리뷰 삭제 실패'); }
};
</script>

<template>
    <div class="flex h-screen bg-gray-100 font-sans">
        <!-- Sidebar -->
        <aside class="w-64 bg-slate-800 text-white flex flex-col shadow-xl z-20">
            <div class="h-16 flex items-center justify-center border-b border-slate-700 bg-slate-900">
                <h1 class="text-lg font-black tracking-wider uppercase">Creama Admin</h1>
            </div>
            
            <nav class="flex-1 p-4 space-y-2">
                <button @click="activeMenu = 'dashboard'" class="w-full flex items-center px-4 py-3 text-left rounded-lg transition-colors"
                    :class="activeMenu === 'dashboard' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-300 hover:bg-slate-700 hover:text-white'">
                    <span class="mr-3">📊</span> 대시보드
                </button>
                <button @click="activeMenu = 'cafes'" class="w-full flex items-center px-4 py-3 text-left rounded-lg transition-colors"
                    :class="activeMenu === 'cafes' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-300 hover:bg-slate-700 hover:text-white'">
                    <span class="mr-3">☕</span> 카페 관리
                </button>
                <button @click="activeMenu = 'reviews'" class="w-full flex items-center px-4 py-3 text-left rounded-lg transition-colors"
                    :class="activeMenu === 'reviews' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-300 hover:bg-slate-700 hover:text-white'">
                    <span class="mr-3">💬</span> 리뷰 관리
                </button>
            </nav>

            <div class="p-4 border-t border-slate-700">
                <button @click="$emit('close')" class="w-full flex items-center justify-center px-4 py-3 rounded-lg text-slate-300 hover:bg-red-600 hover:text-white transition-colors text-sm font-bold">
                    로그아웃 (Exit)
                </button>
            </div>
        </aside>

        <!-- Main Content -->
        <main class="flex-1 flex flex-col overflow-hidden">
            <!-- Top Header -->
            <header class="h-16 bg-white shadow-sm border-b border-gray-200 flex items-center justify-between px-6 z-10">
                <h2 class="text-xl font-bold text-gray-800">{{ activeMenu === 'dashboard' ? 'Dashboard Overview' : (activeMenu === 'cafes' ? 'Cafe Management' : 'Review Management') }}</h2>
                <div class="flex items-center gap-4">
                    <div class="flex items-center gap-2">
                        <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xs">AD</div>
                        <span class="text-sm font-medium text-gray-600">Administrator</span>
                    </div>
                </div>
            </header>

            <!-- Content Body -->
            <div class="flex-1 overflow-auto p-8">
                
                <!-- DASHBOARD VIEW -->
                <div v-if="activeMenu === 'dashboard'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <!-- Stat Card 1 -->
                    <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100 flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-gray-500 mb-1">Total Cafes</p>
                            <p class="text-3xl font-black text-gray-800">{{ stats.totalCafes }}</p>
                        </div>
                        <div class="w-12 h-12 bg-blue-50 rounded-lg flex items-center justify-center text-blue-600 text-2xl">☕</div>
                    </div>
                    <!-- Stat Card 2 -->
                    <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100 flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-gray-500 mb-1">Total Reviews</p>
                            <p class="text-3xl font-black text-gray-800">{{ stats.totalReviews }}</p>
                        </div>
                        <div class="w-12 h-12 bg-purple-50 rounded-lg flex items-center justify-center text-purple-600 text-2xl">💬</div>
                    </div>
                </div>

                <!-- CAFES VIEW -->
                <div v-if="activeMenu === 'cafes'" class="bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col h-full max-h-[calc(100vh-8rem)]">
                    <div class="p-4 border-b border-gray-200 flex justify-between items-center bg-gray-50/50">
                        <h3 class="font-bold text-gray-700">Cafe List</h3>
                        <button @click="openCreateCafe" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-bold shadow-sm transition-colors">
                            + Add New Cafe
                        </button>
                    </div>
                    <div class="overflow-auto flex-1">
                        <table class="w-full text-sm text-left text-gray-600 relative">
                            <thead class="text-xs text-gray-700 uppercase bg-gray-100 sticky top-0 z-10">
                                <tr>
                                    <th class="px-6 py-3">ID</th>
                                    <th class="px-6 py-3">Name</th>
                                    <th class="px-6 py-3">Address</th>
                                    <th class="px-6 py-3 text-center">Reviews</th>
                                    <th class="px-6 py-3 text-center">Menus</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-gray-200">
                                <tr v-for="cafe in cafes" :key="cafe.id" class="hover:bg-blue-50/30 transition-colors">
                                    <td class="px-6 py-4 font-medium text-gray-900">#{{ cafe.id }}</td>
                                    <td class="px-6 py-4 font-bold text-gray-800">{{ cafe.name }}</td>
                                    <td class="px-6 py-4 text-gray-500 truncate max-w-xs">{{ cafe.address }}</td>
                                    <td class="px-6 py-4 text-center">
                                        <span class="bg-purple-100 text-purple-800 px-2 py-1 rounded-full text-xs font-bold">{{ cafe.reviewCount || 0 }}</span>
                                    </td>
                                    <td class="px-6 py-4 text-center">
                                        <span class="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs font-bold">{{ cafe.menus ? cafe.menus.length : 0 }}</span>
                                    </td>
                                    <td class="px-6 py-4 text-right space-x-2">
                                        <button @click="openMenuModal(cafe)" class="font-medium text-blue-600 hover:underline">Menus</button>
                                        <button @click="editCafe(cafe)" class="font-medium text-gray-600 hover:text-gray-900">Edit</button>
                                        <button @click="deleteCafe(cafe.id)" class="font-medium text-red-600 hover:text-red-900">Delete</button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- REVIEWS VIEW -->
                <div v-if="activeMenu === 'reviews'" class="bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col h-full max-h-[calc(100vh-8rem)]">
                     <div class="p-4 border-b border-gray-200 bg-gray-50/50">
                        <h3 class="font-bold text-gray-700">Review Management</h3>
                    </div>
                    <div class="overflow-auto flex-1">
                        <table class="w-full text-sm text-left text-gray-600 relative">
                            <thead class="text-xs text-gray-700 uppercase bg-gray-100 sticky top-0 z-10">
                                <tr>
                                    <th class="px-6 py-3">ID</th>
                                    <th class="px-6 py-3">Cafe</th>
                                    <th class="px-6 py-3">User</th>
                                    <th class="px-6 py-3">Content</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-gray-200">
                                <tr v-for="review in reviews" :key="review.id" class="hover:bg-gray-50 transition-colors">
                                    <td class="px-6 py-4">#{{ review.id }}</td>
                                    <td class="px-6 py-4 font-medium text-gray-800">{{ review.cafe ? review.cafe.name : '-' }}</td>
                                    <td class="px-6 py-4 font-medium text-gray-600">{{ review.user ? review.user.nickname : 'Unknown' }}</td>
                                    <td class="px-6 py-4 text-gray-600 max-w-sm">{{ review.content }}</td>
                                    <td class="px-6 py-4 text-right">
                                        <button @click="deleteReview(review.id)" class="bg-red-50 text-red-600 hover:bg-red-100 px-3 py-1 rounded text-xs font-bold transition-colors">
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </main>

        <!-- Modals (Overlay) -->
        <div v-if="showCafeForm" class="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
             <div class="bg-white w-full max-w-lg rounded-xl shadow-2xl overflow-hidden animate-fade-in-up">
                <div class="bg-slate-800 p-4 flex justify-between items-center text-white">
                    <h3 class="font-bold text-lg">{{ editingCafe ? 'Edit Cafe' : 'Add Cafe' }}</h3>
                    <button @click="showCafeForm = false" class="text-gray-400 hover:text-white">✕</button>
                </div>
                 <div class="p-6 space-y-4">
                    <input v-model="cafeForm.name" placeholder="Cafe Name" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition" />
                    <input v-model="cafeForm.address" placeholder="Address" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition" />
                    <div class="grid grid-cols-2 gap-4">
                         <input v-model="cafeForm.phone" placeholder="Phone" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition" />
                         <input v-model="cafeForm.imageUrl" placeholder="Image URL" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition" />
                    </div>
                    <textarea v-model="cafeForm.description" placeholder="Description" class="w-full p-3 border border-gray-300 rounded-lg h-32 focus:ring-2 focus:ring-blue-500 outline-none transition"></textarea>
                </div>
                <div class="p-4 bg-gray-50 flex justify-end gap-3 border-t">
                    <button @click="showCafeForm = false" class="px-5 py-2.5 text-gray-600 font-bold hover:bg-gray-200 rounded-lg transition">Cancel</button>
                    <button @click="submitCafeForm" class="px-5 py-2.5 bg-blue-600 text-white font-bold rounded-lg hover:bg-blue-700 shadow-lg transition">Save Cafe</button>
                </div>
            </div>
        </div>

        <div v-if="selectedCafeForMenu" class="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
            <div class="bg-white w-full max-w-2xl rounded-xl shadow-2xl overflow-hidden flex flex-col h-[600px] animate-fade-in-up">
                 <div class="bg-slate-800 p-4 flex justify-between items-center text-white shrink-0">
                    <h3 class="font-bold text-lg">Menu Manager - {{ selectedCafeForMenu.name }}</h3>
                    <button @click="closeMenuModal" class="text-gray-400 hover:text-white">✕</button>
                </div>
                <div class="flex-1 flex flex-col md:flex-row overflow-hidden">
                    <div class="w-full md:w-1/2 border-r border-gray-200 overflow-y-auto bg-gray-50 p-2">
                        <div v-if="cafeMenus.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400">
                             <span>📭 No Menus</span>
                        </div>
                        <ul class="space-y-2">
                             <li v-for="menu in cafeMenus" :key="menu.id" 
                                class="bg-white p-3 rounded shadow-sm border border-gray-100 flex justify-between group hover:border-blue-300 transition cursor-pointer"
                                @click="editMenu(menu)"
                             >
                                <div>
                                    <div class="font-bold text-gray-800 text-sm">
                                        <span v-if="menu.recommended" class="text-red-500 mr-1">★</span>{{ menu.itemName }}
                                    </div>
                                    <div class="text-xs text-gray-500">{{ menu.price.toLocaleString() }} Won</div>
                                </div>
                                <button @click.stop="deleteMenu(menu.id)" class="text-gray-300 group-hover:text-red-500 p-1">🗑</button>
                             </li>
                        </ul>
                    </div>
                    <div class="w-full md:w-1/2 p-6 flex flex-col bg-white">
                         <h4 class="font-bold text-gray-700 mb-4 border-b pb-2">{{ editingMenuId ? 'Edit Menu' : 'Add New Menu' }}</h4>
                         <div class="space-y-4 flex-1">
                            <div>
                                <label class="block text-xs font-bold text-gray-500 uppercase mb-1">Menu Name</label>
                                <input v-model="menuForm.itemName" class="w-full p-2 border rounded bg-gray-50 focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none" placeholder="e.g. Iced Americano" />
                            </div>
                             <div>
                                <label class="block text-xs font-bold text-gray-500 uppercase mb-1">Price</label>
                                <input v-model="menuForm.price" type="number" class="w-full p-2 border rounded bg-gray-50 focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none" placeholder="0" />
                            </div>
                            <div class="flex items-center gap-2 pt-2">
                                <input type="checkbox" v-model="menuForm.isRecommended" id="rec-modal" class="w-4 h-4 text-blue-600 rounded" />
                                <label for="rec-modal" class="text-sm font-medium text-gray-700">Recommended Item</label>
                            </div>
                         </div>
                         <div class="pt-4 flex gap-2">
                             <button v-if="editingMenuId" @click="editingMenuId=null; menuForm={itemName:'', price:'', isRecommended:false}" class="flex-1 py-2 bg-gray-100 text-gray-600 rounded font-bold hover:bg-gray-200">Cancel</button>
                             <button @click="submitMenuForm" class="flex-1 py-2 bg-blue-600 text-white rounded font-bold hover:bg-blue-700 shadow-md">
                                {{ editingMenuId ? 'Update' : 'Add Menu' }}
                             </button>
                         </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</template>
