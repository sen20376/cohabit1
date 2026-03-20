<script>
    import { onMount } from 'svelte';
    import { user } from '$lib/auth';
    import { apiCall } from '$lib/api';
    import { goto } from '$app/navigation';

    let apartments = [];
    let complexes = [];
    let loading = true;
    let errorMsg = '';

    onMount(async () => {
        if (!$user || !$user.roles.includes('VENDOR')) {
            goto('/');
            return;
        }

        try {
            loading = true;

            const [aptsData, complexData] = await Promise.all([
                apiCall('complex', '/api/v1/catalog/apartments/my', 'GET'),
                apiCall('complex', '/api/v1/catalog/complexes', 'GET')
            ]);

            apartments = aptsData;
            complexes = complexData.filter(c => c.ownerId === $user.id);

        } catch (e) {
            errorMsg = "Fehler beim Laden der Daten: " + e.message;
            console.error(e);
        } finally {
            loading = false;
        }
    });

    async function handleDeleteApartment(id) {
        if(!confirm("Möchtest du diese Wohnung wirklich unwiderruflich löschen?")) return;

        try {
            await apiCall('complex', `/api/v1/catalog/apartments/${id}`, 'DELETE');
            apartments = apartments.filter(a => a.id !== id);
        } catch (e) {
            alert("Fehler beim Löschen: " + e.message);
        }
    }

    async function handleToggleActive(id) {
        try {
            await apiCall('complex', `/api/v1/catalog/apartments/${id}/active`, 'PATCH');
            apartments = apartments.map(a => a.id === id ? { ...a, active: !a.active } : a);
        } catch (e) {
            alert("Fehler beim Ändern des Status: " + e.message);
        }
    }

    async function handleDeleteComplex(id) {
        if(!confirm("ACHTUNG: Möchtest du diese Anlage wirklich löschen?")) return;

        try {
            await apiCall('complex', `/api/v1/catalog/complexes/${id}`, 'DELETE');
            complexes = complexes.filter(c => c.id !== id);
        } catch (e) {
            alert("Fehler beim Löschen der Anlage: " + e.message);
        }
    }
</script>

<div class="container mx-auto px-4 py-8">

    <div class="flex flex-col md:flex-row justify-between items-center mb-8 gap-4 border-b border-gray-200 pb-6">
        <div>
            <h1 class="text-3xl font-bold text-slate-800">Anbieter Dashboard</h1>
            <p class="text-slate-500 mt-1">Willkommen zurück, {$user?.firstName || $user?.email}. Hier ist dein Immobilien-Überblick.</p>
        </div>

        <div class="flex gap-3">
            <a href="/vendor/create-complex"
               class="flex items-center gap-2 px-4 py-2 bg-white border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 transition-colors shadow-sm font-medium">
                🏢 Anlage erstellen
            </a>
            <a href="/vendor/create-apartment"
               class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-md font-medium">
                🔑 Wohnung hinzufügen
            </a>
        </div>
    </div>

    {#if errorMsg}
        <div class="bg-red-50 text-red-700 p-4 rounded-lg mb-6 border border-red-200">
            {errorMsg}
        </div>
    {/if}

    {#if loading}
        <div class="flex justify-center items-center py-20">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
    {:else}

        <section class="mb-12">
            <h2 class="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
                🏠 Deine Wohnungen
                <span class="bg-slate-100 text-slate-600 text-xs px-2 py-1 rounded-full">{apartments.length}</span>
            </h2>

            {#if apartments.length === 0}
                <div class="text-center py-12 bg-gray-50 rounded-xl border border-dashed border-gray-300">
                    <p class="text-gray-500 mb-4">Du hast noch keine Wohnungen angelegt.</p>
                    <a href="/vendor/create-apartment" class="text-blue-600 hover:underline font-medium">Erste Wohnung anlegen &rarr;</a>
                </div>
            {:else}
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {#each apartments as apt}
                        <div class="bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md transition-all group">
                            <div class="p-5 border-b border-gray-100 flex justify-between items-start">
                                <div>
                                    <h3 class="font-bold text-lg text-slate-800 line-clamp-1">{apt.title}</h3>
                                    <p class="text-sm text-gray-500 flex items-center gap-1 mt-1">
                                        📍 {apt.city || 'Unbekannter Ort'}
                                    </p>
                                </div>
                                <span class={`text-xs font-bold px-2 py-1 rounded-full ${apt.active !== false ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                                    {apt.active !== false ? 'Aktiv' : 'Inaktiv'}
                                </span>
                            </div>

                            <div class="p-5 grid grid-cols-2 gap-4 text-sm text-gray-600">
                                <div>
                                    <span class="block text-gray-400 text-xs uppercase">Größe</span>
                                    <span class="font-semibold">{apt.sizeSqm} m²</span>
                                </div>
                                <div>
                                    <span class="block text-gray-400 text-xs uppercase">Etage</span>
                                    <span class="font-semibold">{apt.floor}. Stock</span>
                                </div>
                                <div>
                                    <span class="block text-gray-400 text-xs uppercase">Top Nr.</span>
                                    <span class="font-semibold">{apt.doorNumber}</span>
                                </div>
                                <div>
                                    <span class="block text-gray-400 text-xs uppercase">Aufrufe</span>
                                    <span class="font-bold text-blue-600 flex items-center gap-1">
                                        {apt.viewCount || 0}
                                    </span>
                                </div>
                            </div>

                            <div class="p-4 bg-gray-50 rounded-b-xl flex gap-2 opacity-100 md:opacity-0 group-hover:opacity-100 transition-opacity">
                                <a href={`/vendor/edit-apartment/${apt.id}`} class="flex-1 text-center py-2 bg-white border border-gray-300 rounded text-slate-700 hover:bg-slate-50 text-sm font-medium">
                                    Bearbeiten
                                </a>
                                <button on:click={() => handleToggleActive(apt.id)} class={`flex-1 text-center py-2 bg-white border rounded text-sm font-medium ${apt.active !== false ? 'border-yellow-200 text-yellow-700 hover:bg-yellow-50' : 'border-green-200 text-green-700 hover:bg-green-50'}`}>
                                    {apt.active !== false ? 'Deaktivieren' : 'Aktivieren'}
                                </button>
                                <button on:click={() => handleDeleteApartment(apt.id)} class="flex-1 text-center py-2 bg-white border border-red-200 text-red-600 hover:bg-red-50 text-sm font-medium">
                                    Löschen
                                </button>
                            </div>
                        </div>
                    {/each}
                </div>
            {/if}
        </section>

        <section>
            <h2 class="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
                🏢 Deine Wohnanlagen
                <span class="bg-slate-100 text-slate-600 text-xs px-2 py-1 rounded-full">{complexes.length}</span>
            </h2>

            <div class="overflow-x-auto bg-white border border-gray-200 rounded-xl shadow-sm">
                <table class="w-full text-left text-sm text-gray-600">
                    <thead class="bg-gray-50 text-gray-900 font-semibold border-b">
                    <tr>
                        <th class="p-4">Name</th>
                        <th class="p-4">Stadt</th>
                        <th class="p-4">Straße</th>
                        <th class="p-4 text-right">Aktion</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each complexes as comp}
                        <tr class="border-b last:border-0 hover:bg-slate-50">
                            <td class="p-4 font-medium text-slate-800">{comp.name}</td>
                            <td class="p-4">{comp.city}</td>
                            <td class="p-4">{comp.street} {comp.houseNumber}</td>
                            <td class="p-4 text-right">
                                <div class="flex justify-end items-center gap-3">
                                    <a href={`/complex/${comp.id}`} class="text-blue-600 hover:text-blue-800" title="Vorschau" target="_blank">
                                        Vorschau
                                    </a>

                                    <a href={`/vendor/edit-complex/${comp.id}`} class="text-blue-600 hover:text-blue-800 font-medium">
                                        Bearbeiten
                                    </a>

                                    <button on:click={() => handleDeleteComplex(comp.id)} class="text-red-500 hover:text-red-700 font-medium">
                                        Löschen
                                    </button>
                                </div>
                            </td>
                        </tr>
                    {/each}
                    {#if complexes.length === 0}
                        <tr>
                            <td colspan="4" class="p-8 text-center text-gray-500 italic">
                                Keine Wohnanlagen gefunden.
                            </td>
                        </tr>
                    {/if}
                    </tbody>
                </table>
            </div>
        </section>

    {/if}
</div>