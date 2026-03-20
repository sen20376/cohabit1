<script>
    import { page } from '$app/stores';
    import { onMount } from 'svelte';
    import { apiCall } from '$lib/api';
    import { goto } from '$app/navigation';
    import { user } from '$lib/auth';

    const id = $page.params.id;
    let loading = true;
    let error = '';

    let dto = {
        title: '',
        description: '',
        doorNumber: '',
        floor: 0,
        sizeSqm: 0,
    };

    onMount(async () => {
        if (!$user || !$user.roles.includes('VENDOR')) {
            goto('/');
            return;
        }

        try {
            const response = await apiCall('complex', `/api/v1/catalog/apartments/${id}`);

            const apt = response.apartment || response;
            dto = {
                title: apt.title || '',
                description: apt.description || '',
                doorNumber: apt.doorNumber || '',
                floor: apt.floor || 0,
                sizeSqm: apt.sizeSqm || 0
            };

        } catch (e) {
            error = "Fehler beim Laden: " + e.message;
        } finally {
            loading = false;
        }
    });

    async function submit() {
        try {
            await apiCall('complex', `/api/v1/catalog/apartments/${id}`, 'PUT', dto);
            alert('Wohnung aktualisiert!');
            goto('/vendor/dashboard');
        } catch (e) {
            alert('Fehler beim Speichern: ' + e.message);
        }
    }
</script>

<div class="container mx-auto px-4 py-8 max-w-2xl">
    <a href="/vendor/dashboard" class="text-slate-500 hover:text-blue-600 mb-4 inline-block">&larr; Zurück</a>

    <h1 class="text-2xl font-bold text-slate-800 mb-6">Wohnung bearbeiten</h1>

    {#if loading}
        <div class="animate-pulse flex space-x-4"><div class="h-4 bg-slate-200 rounded w-3/4"></div></div>
    {:else if error}
        <div class="bg-red-100 text-red-700 p-4 rounded">{error}</div>
    {:else}

        <form on:submit|preventDefault={submit} class="bg-white p-6 rounded-xl shadow-sm border border-gray-200 space-y-6">

            <div>
                <label for="edit-apartment-title" class="block text-sm font-bold text-slate-700 mb-2">Titel</label>
                <input
                    id="edit-apartment-title"
                        type="text"
                        bind:value={dto.title}
                        required
                        class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                />
            </div>

            <div>
                <label for="edit-apartment-description" class="block text-sm font-bold text-slate-700 mb-2">Beschreibung</label>
                <textarea
                    id="edit-apartment-description"
                        bind:value={dto.description}
                        rows="4"
                        class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                ></textarea>
            </div>

            <div class="grid grid-cols-3 gap-4">
                <div>
                    <label for="edit-apartment-door-number" class="block text-sm font-bold text-slate-700 mb-2">Top Nr.</label>
                    <input
                        id="edit-apartment-door-number"
                            type="text"
                            bind:value={dto.doorNumber}
                            required
                            class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                    />
                </div>
                <div>
                    <label for="edit-apartment-floor" class="block text-sm font-bold text-slate-700 mb-2">Stockwerk</label>
                    <input
                        id="edit-apartment-floor"
                            type="number"
                            bind:value={dto.floor}
                            required
                            class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                    />
                </div>
                <div>
                    <label for="edit-apartment-size-sqm" class="block text-sm font-bold text-slate-700 mb-2">Größe (m²)</label>
                    <input
                        id="edit-apartment-size-sqm"
                            type="number"
                            step="0.1"
                            bind:value={dto.sizeSqm}
                            required
                            class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                    />
                </div>
            </div>

            <button type="submit" class="w-full bg-blue-600 text-white font-bold py-3 rounded-lg hover:bg-blue-700 transition-colors shadow-md mt-4">
                Speichern
            </button>
        </form>
    {/if}
</div>