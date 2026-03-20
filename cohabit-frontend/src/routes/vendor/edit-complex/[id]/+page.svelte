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
        name: '', street: '', houseNumber: '',
        zipCode: '', city: '', district: '',
    };

    onMount(async () => {
        if (!$user || !$user.roles.includes('VENDOR')) {
            goto('/');
            return;
        }

        try {
            const data = await apiCall('complex', `/api/v1/catalog/complexes/${id}`);
            const c = data.residentialComplex || data;

            dto = {
                name: c.name,
                street: c.street,
                houseNumber: c.houseNumber,
                zipCode: c.zipCode,
                city: c.city,
                district: c.district
            };

        } catch (e) {
            error = "Konnte Daten nicht laden: " + e.message;
        } finally {
            loading = false;
        }
    });

    async function submit() {
        try {
            await apiCall('complex', `/api/v1/catalog/complexes/${id}`, 'PUT', dto);
            alert('Änderungen gespeichert!');
            goto('/vendor/dashboard');
        } catch (e) {
            alert('Fehler beim Speichern: ' + e.message);
        }
    }
</script>

<div class="container mx-auto px-4 py-8 max-w-2xl">
    <a href="/vendor/dashboard" class="text-slate-500 hover:text-blue-600 mb-4 inline-block">&larr; Zurück</a>

    <h1 class="text-2xl font-bold text-slate-800 mb-6">Wohnanlage bearbeiten</h1>

    {#if loading}
        <p>Lade Daten...</p>
    {:else if error}
        <div class="bg-red-100 text-red-700 p-4 rounded">{error}</div>
    {:else}
        <form on:submit|preventDefault={submit} class="bg-white p-6 rounded-xl shadow-sm border border-gray-200 space-y-6">

            <div>
                <label for="edit-complex-name" class="block text-sm font-bold text-slate-700 mb-2">Name der Anlage</label>
                <input id="edit-complex-name" type="text" bind:value={dto.name} required
                       class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
            </div>

            <div class="grid grid-cols-3 gap-4">
                <div class="col-span-2">
                    <label for="edit-complex-street" class="block text-sm font-bold text-slate-700 mb-2">Straße</label>
                    <input id="edit-complex-street" type="text" bind:value={dto.street} required
                           class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label for="edit-complex-house-number" class="block text-sm font-bold text-slate-700 mb-2">Hausnr.</label>
                    <input id="edit-complex-house-number" type="text" bind:value={dto.houseNumber} required
                           class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                </div>
            </div>

            <div class="grid grid-cols-3 gap-4">
                <div>
                    <label for="edit-complex-zip-code" class="block text-sm font-bold text-slate-700 mb-2">PLZ</label>
                    <input id="edit-complex-zip-code" type="text" bind:value={dto.zipCode} required
                           class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label for="edit-complex-city" class="block text-sm font-bold text-slate-700 mb-2">Stadt</label>
                    <input id="edit-complex-city" type="text" bind:value={dto.city} required
                           class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label for="edit-complex-district" class="block text-sm font-bold text-slate-700 mb-2">Bezirk</label>
                    <input id="edit-complex-district" type="text" bind:value={dto.district}
                           class="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                </div>
            </div>

            <div class="pt-4">
                <button type="submit" class="w-full bg-blue-600 text-white font-bold py-3 rounded-lg hover:bg-blue-700 transition-colors shadow-md">
                    Speichern
                </button>
            </div>
        </form>
    {/if}
</div>
