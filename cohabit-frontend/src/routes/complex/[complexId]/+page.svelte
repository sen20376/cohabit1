<script>
    import { page } from '$app/stores';
    import { onMount } from 'svelte';
    import { apiCall } from '$lib/api';
    import { user } from '$lib/auth';

    const complexId = $page.params.complexId;

    let view = null;
    let loading = true;
    let error = null;
    let submitting = false;

    let editingRatingId = null;

    let thematicAverages = {};

    const criteriaLabels = {
        noise: 'Lautstärke',
        light: 'Helligkeit',
        cleanliness: 'Sauberkeit',
        transport: 'Öffis',
        neighborhood: 'Gegend'
    };

    let ratingForm = {
        score: 5,
        comment: '',
        thematicRatings: {
            noise: 3, light: 3, cleanliness: 3, transport: 3, neighborhood: 3
        }
    };

    async function loadData() {
        loading = true;
        try {
            view = await apiCall('complex', `/api/v1/catalog/complexes/${complexId}`);

            if ($user && view.ratings && view.ratings.length > 0) {
                view.ratings.sort((a, b) => {
                    if (a.userId === $user.id) return -1;
                    if (b.userId === $user.id) return 1;
                    return new Date(b.createdAt) - new Date(a.createdAt);
                });
            }

            calculateThematicAverages();

        } catch (e) {
            error = e.message;
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        loadData();
    });

    function calculateThematicAverages() {
        if (!view || !view.ratings || view.ratings.length === 0) return;

        const totals = { noise: 0, light: 0, cleanliness: 0, transport: 0, neighborhood: 0 };
        const counts = { noise: 0, light: 0, cleanliness: 0, transport: 0, neighborhood: 0 };

        view.ratings.forEach(r => {
            if (r.thematicRatings) {
                Object.entries(r.thematicRatings).forEach(([key, val]) => {
                    if (totals[key] !== undefined) {
                        totals[key] += val;
                        counts[key]++;
                    }
                });
            }
        });

        Object.keys(totals).forEach(key => {
            thematicAverages[key] = counts[key] > 0 ? (totals[key] / counts[key]).toFixed(1) : 0;
        });
    }

    async function handleShare() {
        if (!view || !view.residentialComplex) return;

        const complex = view.residentialComplex;
        const shareData = {
            title: `Cohabit: ${complex.name}`,
            text: `Schau dir diese Wohnanlage an: ${complex.name} in ${complex.city}`,
            url: window.location.href
        };

        try {
            if (navigator.share) {
                await navigator.share(shareData);
            } else {
                await navigator.clipboard.writeText(window.location.href);
                alert("Link in die Zwischenablage kopiert! 📋");
            }
        } catch (err) {
            console.error("Fehler beim Teilen:", err);
        }
    }

    function handleEditMode(rating) {
        ratingForm = {
            score: rating.score,
            comment: rating.comment,
            thematicRatings: { ...rating.thematicRatings }
        };
        editingRatingId = rating.id;

        const formElement = document.getElementById('rating-form');
        if(formElement) formElement.scrollIntoView({ behavior: 'smooth' });
    }

    function cancelEdit() {
        editingRatingId = null;
        ratingForm = {
            score: 5,
            comment: '',
            thematicRatings: { noise: 3, light: 3, cleanliness: 3, transport: 3, neighborhood: 3 }
        };
    }

    async function handleDeleteRating(ratingId) {
        if(!confirm("Möchtest du deine Bewertung wirklich löschen?")) return;

        try {
            await apiCall('rating', `/api/v1/ratings/${ratingId}`, 'DELETE');
            if (editingRatingId === ratingId) cancelEdit();
            await loadData();
        } catch (e) {
            alert("Löschen fehlgeschlagen: " + e.message);
        }
    }

    async function submitRating() {
        if (!$user) return alert("Bitte erst einloggen!");

        submitting = true;
        try {
            const payload = {
                targetId: complexId,
                score: ratingForm.score,
                comment: ratingForm.comment,
                thematicRatings: ratingForm.thematicRatings
            };

            if (editingRatingId) {
                await apiCall('rating', `/api/v1/ratings/${editingRatingId}`, 'PUT', payload);
                alert("Bewertung aktualisiert!");
                editingRatingId = null;
            } else {
                await apiCall('rating', '/api/v1/ratings', 'POST', payload);
            }

            ratingForm.comment = '';
            ratingForm.score = 5;

            await loadData();
        } catch (e) {
            alert("Fehler: " + e.message);
        } finally {
            submitting = false;
        }
    }

    async function handleVote(ratingId) {
        const votedKey = `voted_${ratingId}`;
        if (localStorage.getItem(votedKey)) {
            alert("Du hast hierfür bereits abgestimmt!");
            return;
        }
        try {
            await apiCall('rating', `/api/v1/ratings/${ratingId}/vote`, 'POST');
            localStorage.setItem(votedKey, '1');
            await loadData();
        } catch (e) {
            if (e.message.includes('400')) {
                alert("Du hast hierfür bereits abgestimmt!");
                localStorage.setItem(votedKey, '1');
            } else {
                alert("Fehler beim Abstimmen: " + e.message);
            }
        }
    }

    function formatDate(isoString) {
        if (!isoString) return 'Gerade eben';
        return new Date(isoString).toLocaleDateString('de-DE');
    }
</script>

<svelte:head>
    {#if view && view.residentialComplex}
        <title>{view.residentialComplex.name} - Cohabit</title>
        <meta property="og:title" content={view.residentialComplex.name} />
        <meta property="og:description" content={`Wohnanlage in ${view.residentialComplex.zipCode} ${view.residentialComplex.city}, ${view.residentialComplex.address}`} />
        {#if view.residentialComplex.imageUrls && view.residentialComplex.imageUrls.length > 0}
            <meta property="og:image" content={view.residentialComplex.imageUrls[0]} />
        {/if}
    {/if}
</svelte:head>

<div class="container mx-auto px-4 py-8 min-h-screen">

    <a href="/" class="inline-flex items-center text-sm font-medium text-slate-500 hover:text-blue-600 mb-6 transition-colors">
        <span class="mr-1">←</span> Zurück zur Übersicht
    </a>

    {#if loading && !view}
        <div class="flex justify-center py-20">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-slate-800"></div>
        </div>
    {:else if error}
        <div class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
            Fehler: {error}
        </div>
    {:else if view}

        <div class="relative bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden mb-8">
            <div class="h-64 md:h-80 w-full bg-slate-200 relative">
                {#if view.residentialComplex.imageUrls && view.residentialComplex.imageUrls.length > 0}
                    <img src={view.residentialComplex.imageUrls[0]} alt={view.residentialComplex.name} class="w-full h-full object-cover" />
                {:else}
                    <div class="flex items-center justify-center h-full text-slate-400">Kein Bild verfügbar</div>
                {/if}

                <div class="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent"></div>

                <div class="absolute bottom-0 left-0 p-6 md:p-8 w-full text-white">
                    <div class="flex justify-between items-end">
                        <div>
                            <h1 class="text-3xl md:text-4xl font-extrabold mb-2 text-shadow">{view.residentialComplex.name}</h1>
                            <p class="text-slate-100 text-lg flex items-center gap-2">
                                📍 {view.residentialComplex.address}, {view.residentialComplex.zipCode} {view.residentialComplex.district}
                            </p>
                        </div>

                        <div>
                            <button
                                    on:click={handleShare}
                                    class="bg-white/20 hover:bg-white/30 backdrop-blur text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors border border-white/40 flex items-center gap-2"
                            >
                                <span>📤</span> <span class="hidden sm:inline">Teilen</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="px-6 py-4 flex items-center justify-between bg-white">
                <div class="flex items-center gap-2">
                    {#if view.residentialComplex.averageRating}
                        <span class="bg-amber-400 text-slate-900 px-3 py-1 rounded-full font-bold text-sm">
                            ⭐ {view.residentialComplex.averageRating.toFixed(1)}
                        </span>
                        <span class="text-slate-500 text-sm">Durchschnittsbewertung</span>
                    {:else}
                        <span class="text-slate-400 text-sm">Noch keine Bewertungen</span>
                    {/if}
                </div>
                <div class="text-slate-500 text-sm font-medium">
                    {view.apartments.length} Wohnungen verfügbar
                </div>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

            <div class="lg:col-span-2 space-y-6">
                <h2 class="text-2xl font-bold text-slate-800">Verfügbare Wohnungen</h2>

                {#if view.apartments.length === 0}
                    <div class="p-8 bg-slate-50 rounded-xl border border-dashed border-slate-300 text-center text-slate-500">
                        Momentan sind keine Wohnungen in dieser Anlage frei.
                    </div>
                {:else}
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {#each view.apartments as apt}
                            <a href="/complex/{complexId}/apartment/{apt.id}" class="group block bg-white border border-slate-200 rounded-xl p-5 hover:shadow-lg hover:border-blue-300 transition-all duration-200 cursor-pointer relative overflow-hidden">
                                <div class="absolute top-0 left-0 w-1 h-full bg-blue-500 group-hover:w-2 transition-all"></div>

                                <div class="flex justify-between items-start mb-2 pl-2">
                                    <h3 class="text-lg font-bold text-slate-800 group-hover:text-blue-600 transition-colors">
                                        {apt.title}
                                    </h3>
                                    {#if apt.avgRating}
                                        <span class="text-xs font-bold bg-slate-100 text-slate-600 px-2 py-1 rounded">
                                            ⭐ {apt.avgRating}
                                        </span>
                                    {/if}
                                </div>

                                <div class="pl-2 mb-4">
                                    <p class="text-slate-600 text-sm line-clamp-2">{apt.description}</p>
                                </div>

                                <div class="pl-2 flex items-center justify-between mt-auto pt-4 border-t border-slate-50">
                                    <div class="flex gap-3 text-xs font-medium text-slate-500 uppercase tracking-wide">
                                        <span class="flex items-center gap-1">🏢 Top {apt.doorNumber}</span>
                                        <span class="flex items-center gap-1">📐 {apt.sizeSqm} m²</span>
                                        <span class="flex items-center gap-1">🪜 Stock {apt.floor}</span>
                                    </div>
                                    <span class="text-blue-600 font-bold text-sm">Details &rarr;</span>
                                </div>
                            </a>
                        {/each}
                    </div>
                {/if}
            </div>

            <div class="space-y-8">

                <div id="rating-form" class={`p-6 rounded-xl shadow-sm border relative overflow-hidden transition-colors duration-300 ${editingRatingId ? 'bg-blue-50 border-blue-200' : 'bg-white border-blue-100'}`}>
                    <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-400 to-indigo-500"></div>

                    <h3 class="text-xl font-bold text-slate-800 mb-4">
                        {editingRatingId ? 'Bewertung bearbeiten' : 'Deine Meinung zählt'}
                    </h3>

                    {#if $user}
                        <div class="space-y-5">
                            <div>
                                <label for="rating-score" class="flex justify-between text-sm font-bold text-slate-700 mb-2">
                                    Gesamtnote
                                    <span class="bg-blue-100 text-blue-800 px-2 rounded">{ratingForm.score} / 5</span>
                                </label>
                                <input
                                    id="rating-score"
                                        type="range"
                                        min="1"
                                        max="5"
                                        bind:value={ratingForm.score}
                                        class="w-full cursor-pointer accent-blue-600"
                                >
                                <div class="flex justify-between text-xs text-slate-400 mt-1">
                                    <span>Schlecht</span><span>Exzellent</span>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 gap-3 bg-white/50 p-4 rounded-lg border border-slate-100">
                                {#each Object.keys(ratingForm.thematicRatings) as key}
                                    <div class="flex items-center justify-between gap-3">
                                        <label for={`thematic-${key}`} class="text-xs font-medium text-slate-600 w-24">{criteriaLabels[key]}</label>
                                        <input
                                                id={`thematic-${key}`}
                                                type="range"
                                                min="1"
                                                max="5"
                                                bind:value={ratingForm.thematicRatings[key]}
                                                class="flex-grow cursor-pointer accent-indigo-500"
                                        >
                                        <span class="text-xs font-bold w-4 text-right">{ratingForm.thematicRatings[key]}</span>
                                    </div>
                                {/each}
                            </div>

                            <textarea
                                    bind:value={ratingForm.comment}
                                    rows="3"
                                    class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-sm resize-none"
                                    placeholder="Was hat dir gefallen? Was nicht?"
                            ></textarea>

                            <div class="flex gap-2">
                                {#if editingRatingId}
                                    <button on:click={cancelEdit} class="flex-1 py-2.5 bg-white border border-slate-300 text-slate-600 font-bold rounded-lg hover:bg-slate-50 transition-colors">
                                        Abbrechen
                                    </button>
                                {/if}
                                <button
                                        on:click={submitRating}
                                        disabled={submitting}
                                        class="flex-1 py-2.5 bg-slate-800 text-white font-bold rounded-lg hover:bg-slate-700 transition-colors shadow-md disabled:bg-slate-400"
                                >
                                    {submitting ? 'Speichert...' : (editingRatingId ? 'Aktualisieren' : 'Bewertung abgeben')}
                                </button>
                            </div>
                        </div>
                    {:else}
                        <div class="text-center py-6 bg-slate-50 rounded-lg border border-dashed border-slate-200">
                            <p class="text-slate-500 mb-3 text-sm">Melde dich an, um eine Bewertung zu schreiben.</p>
                            <a href="/login" class="inline-block px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-md hover:bg-blue-700 transition-colors">
                                Zum Login
                            </a>
                        </div>
                    {/if}
                </div>

                {#if view.ratings && view.ratings.length > 0}
                    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
                        <h3 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-2">
                            Detaillierte Bewertung
                        </h3>
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-3">
                            {#each Object.entries(thematicAverages) as [key, avg]}
                                <div class="flex items-center justify-between group">
                                    <span class="text-sm font-medium text-slate-500 w-24 group-hover:text-slate-800 transition-colors">
                                        {criteriaLabels[key]}
                                    </span>

                                    <div class="flex-grow mx-3 h-2.5 bg-slate-100 rounded-full overflow-hidden">
                                        <div
                                                class={`h-full rounded-full transition-all duration-1000 ease-out ${avg >= 4 ? 'bg-emerald-500' : avg >= 2.5 ? 'bg-amber-400' : 'bg-red-400'}`}
                                                style="width: {(avg / 5) * 100}%"
                                        ></div>
                                    </div>

                                    <span class="text-sm font-bold text-slate-700 w-8 text-right">{avg}</span>
                                </div>
                            {/each}
                        </div>
                    </div>
                {/if}

                <div>
                    <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center justify-between">
                        Bewertungen
                        <span class="text-xs font-normal bg-slate-100 px-2 py-1 rounded-full text-slate-600">{view.ratings.length} Beiträge</span>
                    </h3>

                    {#if view.ratings.length === 0}
                        <p class="text-slate-400 text-sm italic">Noch keine Bewertungen vorhanden.</p>
                    {:else}
                        <div class="space-y-4">
                            {#each view.ratings as rating}
                                <div class={`p-4 rounded-xl border shadow-sm relative group transition-all ${rating.userId === $user?.id ? 'bg-blue-50/50 border-blue-200' : 'bg-white border-slate-100'}`}>

                                    <div class="flex justify-between items-start mb-3">
                                        <div class="flex items-center gap-2">
                                            <div class="w-8 h-8 rounded-full bg-slate-800 text-white flex items-center justify-center text-xs font-bold shadow-sm">
                                                {rating.score}
                                            </div>

                                            <div class="flex flex-col">
                                                <span class="text-xs text-slate-400 font-medium">{formatDate(rating.createdAt)}</span>
                                                {#if rating.userId === $user?.id}
                                                    <span class="text-[10px] font-bold text-blue-600 uppercase tracking-wider">Du</span>
                                                {/if}
                                            </div>
                                        </div>

                                        {#if $user && rating.userId === $user.id}
                                            <div class="flex gap-2 opacity-100 lg:opacity-0 group-hover:opacity-100 transition-opacity">
                                                <button
                                                        on:click={() => handleEditMode(rating)}
                                                        class="text-xs text-blue-600 hover:text-blue-800 font-bold bg-white border border-blue-100 px-2 py-1 rounded shadow-sm"
                                                >
                                                    ✏️
                                                </button>
                                                <button
                                                        on:click={() => handleDeleteRating(rating.id)}
                                                        class="text-xs text-red-500 hover:text-red-700 font-bold bg-white border border-red-100 px-2 py-1 rounded shadow-sm"
                                                >
                                                    🗑️
                                                </button>
                                            </div>
                                        {/if}
                                    </div>

                                    {#if rating.comment}
                                        <p class="text-slate-700 text-sm mb-4 leading-relaxed bg-slate-50 p-3 rounded-lg border border-slate-100 italic">
                                            "{rating.comment}"
                                        </p>
                                    {/if}

                                    <div class="flex flex-wrap items-end justify-between gap-4 border-t border-slate-200/50 pt-3 mt-2">

                                        {#if rating.thematicRatings}
                                            <div class="flex-grow grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-2 w-full sm:w-auto">
                                                {#each Object.entries(rating.thematicRatings) as [key, val]}
                                                    <div class="flex items-center gap-2">
                                                        <span class="text-[10px] text-slate-500 w-16 truncate font-medium uppercase">{criteriaLabels[key] || key}</span>
                                                        <div class="w-16 h-1.5 bg-slate-200 rounded-full overflow-hidden">
                                                            <div class={`h-full rounded-full ${val >= 4 ? 'bg-green-500' : val >= 3 ? 'bg-yellow-400' : 'bg-red-400'}`} style="width: {(val / 5) * 100}%"></div>
                                                        </div>
                                                    </div>
                                                {/each}
                                            </div>
                                        {/if}

                                        <div class="ml-auto">
                                            <button
                                                    on:click={() => handleVote(rating.id)}
                                                    class="flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-blue-600 transition-colors bg-white border border-slate-200 hover:border-blue-300 px-3 py-1.5 rounded-full shadow-sm"
                                                    title="War diese Bewertung hilfreich?"
                                            >
                                                <span>👍</span>
                                                <span>Hilfreich</span>
                                                {#if rating.helpfulVotes > 0}
                                                <span class="ml-1 bg-slate-100 text-slate-700 px-1.5 py-0.5 rounded-full text-[10px]">
                                                    {rating.helpfulVotes}
                                                </span>
                                                {/if}
                                            </button>
                                        </div>
                                    </div>

                                </div>
                            {/each}
                        </div>
                    {/if}
                </div>

            </div>
        </div>
    {/if}
</div>