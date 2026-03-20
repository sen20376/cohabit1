<script>
    import { page } from '$app/stores';
    import { onMount } from 'svelte';
    import { apiCall } from '$lib/api';
    import { user } from '$lib/auth';
    import { goto } from '$app/navigation';
    import { CRITERIA_LABELS, defaultRatingForm, formatDate } from '$lib/utils';

    const { complexId, apartmentId } = $page.params;

    let apartment = null;
    let loading = true;
    let error = null;
    let submitting = false;

    let editingRatingId = null;
    let isBookmarked = false;
    let bookmarkLoading = false;

    const criteriaLabels = CRITERIA_LABELS;

    let ratingForm = defaultRatingForm();

    async function loadBookmarkState() {
        if (!$user) return;
        try {
            const bookmarks = await apiCall('auth', '/api/v1/users/me/bookmarks');
            isBookmarked = bookmarks.includes(apartmentId);
        } catch (e) {
            console.error("Merkliste konnte nicht geladen werden:", e);
        }
    }

    async function toggleBookmark() {
        if (!$user) return alert("Bitte erst einloggen!");
        bookmarkLoading = true;
        try {
            if (isBookmarked) {
                await apiCall('auth', `/api/v1/users/me/bookmarks/${apartmentId}`, 'DELETE');
                isBookmarked = false;
            } else {
                await apiCall('auth', `/api/v1/users/me/bookmarks/${apartmentId}`, 'POST');
                isBookmarked = true;
            }
        } catch (e) {
            alert("Fehler: " + e.message);
        } finally {
            bookmarkLoading = false;
        }
    }

    async function loadData() {
        loading = true;
        try {
            const view = await apiCall('complex', `/api/v1/catalog/apartments/${apartmentId}`);

            const apartmentInfo = view.apartment;
            const ratingsList = view.ratings || [];

            apartment = {
                ...apartmentInfo,
                ratings: ratingsList
            };

            if ($user && apartment.ratings.length > 0) {
                apartment.ratings.sort((a, b) => {
                    if (a.userId === $user.id) return -1;
                    if (b.userId === $user.id) return 1;
                    return new Date(b.createdAt) - new Date(a.createdAt);
                });
            }

        } catch (e) {
            error = "Konnte Wohnung nicht laden: " + e.message;
            console.error(e);
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        loadData();
        loadBookmarkState();
    });

    async function handleShare() {
        const shareData = {
            title: `Cohabit: ${apartment.title}`,
            text: `Schau dir diese Wohnung an: ${apartment.title} (${apartment.sizeSqm}m²)`,
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
        ratingForm = defaultRatingForm();
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
                targetId: apartmentId,
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

    function handleEditApartment() {
        goto(`/vendor/edit-apartment/${apartmentId}`);
    }


</script>

<svelte:head>
    {#if apartment}
        <title>{apartment.title} - Cohabit</title>
        <meta property="og:title" content={apartment.title} />
        <meta property="og:description" content={`${apartment.sizeSqm}m² Wohnung im ${apartment.floor}. Stock.`} />
        {#if apartment.imageUrls && apartment.imageUrls.length > 0}
            <meta property="og:image" content={apartment.imageUrls[0]} />
        {/if}
    {/if}
</svelte:head>

<div class="container mx-auto px-4 py-8 min-h-screen">

    <a href={`/complex/${complexId}`} class="inline-flex items-center text-sm font-medium text-slate-500 hover:text-blue-600 mb-6 transition-colors">
        <span class="mr-1">←</span> Zurück zur Anlage
    </a>

    {#if loading && !apartment}
        <div class="flex justify-center py-20">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-slate-800"></div>
        </div>
    {:else if error}
        <div class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200 shadow-sm">
            <h3 class="font-bold">Fehler</h3>
            <p>{error}</p>
        </div>
    {:else if apartment}

        <div class="relative bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden mb-8">
            <div class="h-64 md:h-80 w-full bg-slate-200 relative">
                {#if apartment.imageUrls && apartment.imageUrls.length > 0}
                    <img src={apartment.imageUrls[0]} alt={apartment.title} class="w-full h-full object-cover" />
                {:else}
                    <div class="flex flex-col items-center justify-center h-full text-slate-400 bg-slate-100">
                        <span class="text-4xl mb-2">🏠</span>
                        <span>Kein Bild verfügbar</span>
                    </div>
                {/if}

                <div class="absolute inset-0 bg-gradient-to-t from-slate-900/70 to-transparent"></div>

                <div class="absolute bottom-0 left-0 p-6 md:p-8 w-full text-white">
                    <div class="flex justify-between items-end">
                        <div>
                            <h1 class="text-3xl md:text-4xl font-extrabold mb-2 text-shadow">{apartment.title}</h1>
                            <p class="text-slate-100 text-lg flex items-center gap-2">
                                <span>Wohnungs-ID: {apartment.id.substring(0,8)}...</span>
                            </p>
                        </div>

                        <div class="flex gap-2">
                            <button
                                    on:click={handleShare}
                                    class="bg-white/20 hover:bg-white/30 backdrop-blur text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors border border-white/40 flex items-center gap-2"
                            >
                                <span>📤</span> <span class="hidden sm:inline">Teilen</span>
                            </button>

                            {#if $user}
                                <button
                                        on:click={toggleBookmark}
                                        disabled={bookmarkLoading}
                                        class={`backdrop-blur px-4 py-2 rounded-lg text-sm font-bold transition-colors border flex items-center gap-2 ${isBookmarked ? 'bg-yellow-400/80 border-yellow-300 text-slate-900 hover:bg-yellow-400' : 'bg-white/20 hover:bg-white/30 border-white/40 text-white'}`}
                                        title={isBookmarked ? 'Aus Merkliste entfernen' : 'Zur Merkliste hinzufügen'}
                                >
                                    <span>{isBookmarked ? '🔖' : '🏷️'}</span>
                                    <span class="hidden sm:inline">{isBookmarked ? 'Gespeichert' : 'Merken'}</span>
                                </button>
                            {/if}

                            {#if $user && apartment.ownerId === $user.id}
                                <button on:click={handleEditApartment} class="bg-white/20 hover:bg-white/30 backdrop-blur text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors border border-white/40">
                                    ✏️ Bearbeiten
                                </button>
                            {/if}
                        </div>
                    </div>
                </div>
            </div>

            <div class="px-6 py-4 bg-white border-t border-slate-100 flex flex-wrap gap-6 text-slate-700">
                <div class="flex items-center gap-2">
                    <span class="text-xl">📐</span>
                    <div>
                        <span class="block text-xs text-slate-400 uppercase font-bold">Größe</span>
                        <span class="font-bold">{apartment.sizeSqm} m²</span>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <span class="text-xl">🚪</span>
                    <div>
                        <span class="block text-xs text-slate-400 uppercase font-bold">Top Nr.</span>
                        <span class="font-bold">{apartment.doorNumber}</span>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <span class="text-xl">🪜</span>
                    <div>
                        <span class="block text-xs text-slate-400 uppercase font-bold">Stockwerk</span>
                        <span class="font-bold">{apartment.floor}. Stock</span>
                    </div>
                </div>
                <div class="ml-auto flex items-center gap-2">
                    {#if apartment.avgRating}
                        <span class="bg-amber-400 text-slate-900 px-3 py-1 rounded-full font-bold text-sm shadow-sm">
                            ⭐ {apartment.avgRating.toFixed(1)}
                        </span>
                    {:else}
                        <span class="text-slate-400 text-sm italic">Neu</span>
                    {/if}
                </div>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

            <div class="lg:col-span-2 space-y-8">
                <div class="bg-white p-8 rounded-xl shadow-sm border border-slate-200">
                    <h2 class="text-2xl font-bold text-slate-800 mb-4 border-b border-slate-100 pb-2">Über diese Wohnung</h2>
                    <p class="text-slate-600 leading-relaxed whitespace-pre-line text-lg">
                        {apartment.description || "Keine Beschreibung vorhanden."}
                    </p>
                </div>
            </div>

            <div class="space-y-8">

                <div id="rating-form" class={`p-6 rounded-xl shadow-sm border relative overflow-hidden transition-colors duration-300 ${editingRatingId ? 'bg-blue-50 border-blue-200' : 'bg-white border-blue-100'}`}>
                    <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-400 to-indigo-500"></div>

                    <h3 class="text-xl font-bold text-slate-800 mb-4">
                        {editingRatingId ? 'Bewertung bearbeiten' : 'Wohnung bewerten'}
                    </h3>

                    {#if $user}
                        <div class="space-y-5">
                            <div>
                                <label for="apartment-rating-score" class="flex justify-between text-sm font-bold text-slate-700 mb-2">
                                    Gesamtnote
                                    <span class="bg-blue-100 text-blue-800 px-2 rounded">{ratingForm.score} / 5</span>
                                </label>
                                <input id="apartment-rating-score" type="range" min="1" max="5" bind:value={ratingForm.score}
                                       class="w-full cursor-pointer accent-blue-600">
                                <div class="flex justify-between text-xs text-slate-400 mt-1">
                                    <span>Schlecht</span><span>Exzellent</span>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 gap-3 bg-white/50 p-4 rounded-lg border border-slate-100">
                                {#each Object.keys(ratingForm.thematicRatings) as key}
                                    <div class="flex items-center justify-between gap-3">
                                        <label for={`apartment-thematic-${key}`} class="text-xs font-medium text-slate-600 w-24">{criteriaLabels[key]}</label>
                                        <input id={`apartment-thematic-${key}`} type="range" min="1" max="5" bind:value={ratingForm.thematicRatings[key]}
                                               class="flex-grow cursor-pointer accent-indigo-500">
                                        <span class="text-xs font-bold w-4 text-right">{ratingForm.thematicRatings[key]}</span>
                                    </div>
                                {/each}
                            </div>

                            <textarea
                                    bind:value={ratingForm.comment}
                                    rows="3"
                                    class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-sm resize-none"
                                    placeholder="Dein Eindruck zur Wohnung..."
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
                            <p class="text-slate-500 mb-3 text-sm">Bitte anmelden zum Bewerten.</p>
                            <a href="/login" class="inline-block px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-md hover:bg-blue-700 transition-colors">
                                Zum Login
                            </a>
                        </div>
                    {/if}
                </div>


                <div>
                    <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center justify-between">
                        Bewertungen
                        <span class="text-xs font-normal bg-slate-100 px-2 py-1 rounded-full text-slate-600">
                            {apartment.ratings ? apartment.ratings.length : 0} Beiträge
                        </span>
                    </h3>

                    {#if !apartment.ratings || apartment.ratings.length === 0}
                        <div class="p-6 text-center bg-slate-50 rounded-xl border border-dashed border-slate-200">
                            <p class="text-slate-400 text-sm italic">Noch keine Bewertungen vorhanden.</p>
                        </div>
                    {:else}
                        <div class="space-y-4">
                            {#each apartment.ratings as rating}
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
                                            {#if !$user || rating.userId !== $user.id}
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
                                            {:else if rating.helpfulVotes > 0}
                                                <span class="flex items-center gap-1.5 text-xs text-slate-400 px-3 py-1.5">
                                                    👍 {rating.helpfulVotes}
                                                </span>
                                            {/if}
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