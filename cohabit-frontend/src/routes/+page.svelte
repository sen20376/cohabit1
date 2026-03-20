<script>
    import { onMount } from 'svelte';
    import { apiCall } from '$lib/api';

    let complexes = [];

    let apartments = [];
    let isSearching = false;

    let loading = true;

    let filters = {
        address: '',
        district: '',
        minSize: '',
        maxSize: ''
    };

    onMount(async () => {
        await loadComplexes();
    });

    async function loadComplexes() {
        loading = true;
        try {
            complexes = await apiCall('complex', '/api/v1/catalog/complexes');
            isSearching = false;
        } catch (e) {
            console.error(e);
        } finally {
            loading = false;
        }
    }

    async function handleSearch() {
        if (!filters.address && !filters.district && !filters.minSize && !filters.maxSize) {
            await loadComplexes();
            return;
        }

        loading = true;
        isSearching = true;

        try {
            const params = new URLSearchParams();
            if (filters.address) params.append('address', filters.address);
            if (filters.district) params.append('district', filters.district);
            if (filters.minSize) params.append('minSize', filters.minSize);
            if (filters.maxSize) params.append('maxSize', filters.maxSize);

            apartments = await apiCall('complex', `/api/v1/catalog/apartments/search?${params.toString()}`);
        } catch (e) {
            alert("Fehler bei der Suche: " + e.message);
        } finally {
            loading = false;
        }
    }

    function resetSearch() {
        filters = { address: '', district: '', minSize: '', maxSize: '' };
        loadComplexes();
    }
</script>

<div class="container mx-auto px-4 py-12 min-h-screen">

    <div class="flex flex-col md:flex-row justify-between items-end mb-8 gap-4">
        <div>
            <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">
                {isSearching ? 'Suchergebnisse' : 'Wohnanlagen in Wien'}
            </h1>
            <p class="text-slate-500 mt-1">
                {isSearching ? 'Gefundene Wohnungen basierend auf deinen Kriterien.' : 'Entdecke aktuelle Angebote und Bewertungen.'}
            </p>
        </div>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 mb-10">
        <form on:submit|preventDefault={handleSearch} class="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">

            <div class="md:col-span-4">
                <label for="filter-address" class="block text-xs font-bold text-slate-500 uppercase mb-1">Adresse / Straße</label>
                <div class="relative">
                    <span class="absolute left-3 top-2.5 text-slate-400">🔍</span>
                    <input
                            id="filter-address"
                            type="text"
                            bind:value={filters.address}
                            placeholder="z.B. Hauptstraße"
                            class="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                    />
                </div>
            </div>

            <div class="md:col-span-2">
                <label for="filter-district" class="block text-xs font-bold text-slate-500 uppercase mb-1">Bezirk</label>
                <input id="filter-district" type="text" bind:value={filters.district} placeholder="1010" class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>

            <div class="md:col-span-2">
                <label for="filter-min-size" class="block text-xs font-bold text-slate-500 uppercase mb-1">Min m²</label>
                <input id="filter-min-size" type="number" bind:value={filters.minSize} placeholder="0" class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>

            <div class="md:col-span-2">
                <label for="filter-max-size" class="block text-xs font-bold text-slate-500 uppercase mb-1">Max m²</label>
                <input id="filter-max-size" type="number" bind:value={filters.maxSize} placeholder="150" class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>

            <div class="md:col-span-2 flex gap-2">
                <button type="submit" class="flex-grow bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 rounded-lg transition-colors shadow-md">
                    Suchen
                </button>
                {#if isSearching}
                    <button type="button" on:click={resetSearch} class="bg-white border border-slate-300 text-slate-500 hover:bg-slate-50 font-bold py-2 px-3 rounded-lg transition-colors">
                        ✕
                    </button>
                {/if}
            </div>
        </form>
    </div>

    {#if loading}
        <div class="flex flex-col items-center justify-center py-20 space-y-4">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <span class="text-slate-400 font-medium">Lade Ergebnisse...</span>
        </div>

    {:else if isSearching}
        {#if apartments.length === 0}
            <div class="text-center py-16 bg-slate-50 rounded-2xl border border-dashed border-slate-300">
                <span class="text-4xl block mb-2">😕</span>
                <p class="text-slate-600 font-medium">Keine Wohnungen gefunden.</p>
                <button on:click={resetSearch} class="text-blue-600 text-sm hover:underline mt-2">Suche zurücksetzen</button>
            </div>
        {:else}
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {#each apartments as apt}
                    <a href={`/complex/${apt.complexId}/apartment/${apt.id}`} class="group bg-white rounded-xl border border-slate-200 p-5 hover:shadow-lg hover:border-blue-300 transition-all cursor-pointer relative overflow-hidden">
                        <div class="absolute top-0 left-0 w-1 h-full bg-blue-500 group-hover:w-2 transition-all"></div>

                        <div class="flex justify-between items-start mb-2 pl-2">
                            <h3 class="font-bold text-slate-800 group-hover:text-blue-600 transition-colors truncate pr-2">
                                {apt.title}
                            </h3>
                            {#if apt.avgRating}
                                <span class="text-xs font-bold bg-amber-100 text-amber-800 px-2 py-1 rounded">⭐ {apt.avgRating.toFixed(1)}</span>
                            {/if}
                        </div>

                        <p class="text-slate-500 text-sm pl-2 mb-4 line-clamp-2">{apt.description}</p>

                        <div class="flex gap-3 text-xs font-medium text-slate-500 uppercase tracking-wide pl-2 mt-auto pt-4 border-t border-slate-50">
                            <span class="flex items-center gap-1">📐 {apt.sizeSqm} m²</span>
                            <span class="flex items-center gap-1">🏢 Top {apt.doorNumber}</span>
                        </div>
                    </a>
                {/each}
            </div>
        {/if}

    {:else}
        {#if complexes.length === 0}
            <div class="text-center py-20 bg-white rounded-2xl border border-dashed border-slate-300">
                <p class="text-slate-500 text-lg">Aktuell sind keine Wohnanlagen gelistet.</p>
            </div>
        {:else}
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {#each complexes as complex}
                    <a href={`/complex/${complex.id}`} class="group bg-white rounded-2xl border border-slate-200 overflow-hidden hover:shadow-xl hover:border-blue-200 transition-all duration-300 flex flex-col h-full">

                        <div class="relative h-48 bg-slate-100 overflow-hidden">
                            {#if complex.imageUrls?.[0]}
                                <img src={complex.imageUrls[0]} alt={complex.name} class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                            {:else}
                                <div class="flex flex-col items-center justify-center h-full text-slate-400">
                                    <span class="text-2xl mb-1">🏠</span>
                                    <span class="text-xs font-medium">Kein Bild</span>
                                </div>
                            {/if}

                            {#if complex.averageRating}
                                <div class="absolute top-3 right-3 bg-white/95 backdrop-blur-sm px-2.5 py-1 rounded-full text-xs font-bold text-slate-800 shadow-sm flex items-center gap-1">
                                    <span class="text-yellow-500">⭐</span> {complex.averageRating.toFixed(1)}
                                </div>
                            {/if}
                        </div>

                        <div class="p-5 flex flex-col flex-grow">
                            <div class="flex-grow">
                                <h3 class="text-lg font-bold text-slate-800 mb-1 group-hover:text-blue-600 transition-colors line-clamp-1">
                                    {complex.name}
                                </h3>
                                <p class="text-slate-500 text-sm flex items-center gap-1.5 mb-3">
                                    📍 <span class="truncate">{complex.zipCode} {complex.district}</span>
                                </p>
                            </div>

                            <div class="pt-4 border-t border-slate-100 flex justify-between items-center mt-2">
                                <span class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Details ansehen</span>
                                <div class="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors">
                                    <span class="font-bold text-lg leading-none">&rarr;</span>
                                </div>
                            </div>
                        </div>
                    </a>
                {/each}
            </div>
        {/if}
    {/if}
</div>