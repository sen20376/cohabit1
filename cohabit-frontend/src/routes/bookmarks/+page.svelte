<script>
    import { onMount } from 'svelte';
    import { user } from '$lib/auth';
    import { apiCall } from '$lib/api';
    import { goto } from '$app/navigation';

    let apartments = [];
    let loading = true;
    let errorMsg = '';

    onMount(async () => {
        if (!$user) {
            goto('/login');
            return;
        }

        try {
            const bookmarkIds = await apiCall('auth', '/api/v1/users/me/bookmarks');

            if (bookmarkIds.length === 0) {
                apartments = [];
                return;
            }

            const results = await Promise.allSettled(
                bookmarkIds.map(id => apiCall('complex', `/api/v1/catalog/apartments/${id}`))
            );

            apartments = results
                .filter(r => r.status === 'fulfilled')
                .map(r => r.value.apartment ?? r.value);

        } catch (e) {
            errorMsg = "Fehler beim Laden der Merkliste: " + e.message;
        } finally {
            loading = false;
        }
    });

    async function removeBookmark(apartmentId) {
        try {
            await apiCall('auth', `/api/v1/users/me/bookmarks/${apartmentId}`, 'DELETE');
            apartments = apartments.filter(a => a.id !== apartmentId);
        } catch (e) {
            alert("Fehler: " + e.message);
        }
    }
</script>

<div class="container mx-auto px-4 py-8">

    <div class="mb-8 border-b border-gray-200 pb-6">
        <h1 class="text-3xl font-bold text-slate-800">Meine Merkliste</h1>
        <p class="text-slate-500 mt-1">Gespeicherte Wohnungsanzeigen</p>
    </div>

    {#if errorMsg}
        <div class="bg-red-50 text-red-700 p-4 rounded-lg mb-6 border border-red-200">{errorMsg}</div>
    {/if}

    {#if loading}
        <div class="flex justify-center py-20">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
    {:else if apartments.length === 0}
        <div class="text-center py-20 bg-gray-50 rounded-xl border border-dashed border-gray-300">
            <p class="text-4xl mb-4">🏷️</p>
            <p class="text-gray-500 mb-4">Noch keine Wohnungen gespeichert.</p>
            <a href="/" class="text-blue-600 hover:underline font-medium">Wohnungen entdecken &rarr;</a>
        </div>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {#each apartments as apt}
                <div class="bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md transition-all group">
                    <div class="p-5 border-b border-gray-100">
                        <h3 class="font-bold text-lg text-slate-800 line-clamp-1">{apt.title}</h3>
                        <p class="text-sm text-gray-500 mt-1">
                            {apt.sizeSqm} m² · {apt.floor}. Stock · Top {apt.doorNumber}
                        </p>
                    </div>

                    <div class="p-5 text-sm text-gray-600">
                        <p class="line-clamp-2 text-slate-500 italic">
                            {apt.description || 'Keine Beschreibung vorhanden.'}
                        </p>
                        {#if apt.avgRating}
                            <span class="mt-3 inline-block bg-amber-100 text-amber-700 px-2 py-1 rounded-full text-xs font-bold">
                                ⭐ {apt.avgRating.toFixed(1)}
                            </span>
                        {/if}
                    </div>

                    <div class="p-4 bg-gray-50 rounded-b-xl flex gap-2 opacity-100 md:opacity-0 group-hover:opacity-100 transition-opacity">
                        <a href={`/complex/${apt.complexId}/apartment/${apt.id}`} class="flex-1 text-center py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700">
                            Ansehen
                        </a>
                        <button on:click={() => removeBookmark(apt.id)} class="flex-1 text-center py-2 bg-white border border-red-200 text-red-600 hover:bg-red-50 text-sm font-medium rounded">
                            Entfernen
                        </button>
                    </div>
                </div>
            {/each}
        </div>
    {/if}
</div>
