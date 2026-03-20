<script>
    import { apiCall } from '$lib/api';
    import { goto } from '$app/navigation';

    let dto = {
        name: '', street: '', houseNumber: '',
        zipCode: '', city: 'Wien', district: '',
        latitude: 48.2082, longitude: 16.3738,
        imageUrls: []
    };

    let imageUrlInput = '';

    function addImageUrl() {
        const url = imageUrlInput.trim();
        if (url) {
            dto.imageUrls = [...dto.imageUrls, url];
            imageUrlInput = '';
        }
    }

    function removeImageUrl(index) {
        dto.imageUrls = dto.imageUrls.filter((_, i) => i !== index);
    }

    async function submit() {
        try {
            await apiCall('complex', '/api/v1/catalog/complexes', 'POST', dto);
            alert('Wohnanlage erstellt!');
            goto('/vendor/dashboard');
        } catch (e) {
            alert('Fehler: ' + e.message);
        }
    }
</script>

<div class="container">
    <h2>Neue Wohnanlage</h2>
    <form on:submit|preventDefault={submit}>
        <div class="form-group">
            <label for="complex-name">Name der Anlage</label>
            <input id="complex-name" type="text" bind:value={dto.name} required placeholder="z.B. Donau-View" />
        </div>

        <div class="row">
            <div class="form-group">
                <label for="complex-street">Straße</label>
                <input id="complex-street" type="text" bind:value={dto.street} required />
            </div>
            <div class="form-group sm">
                <label for="complex-house-number">Nr.</label>
                <input id="complex-house-number" type="text" bind:value={dto.houseNumber} required />
            </div>
        </div>

        <div class="row">
            <div class="form-group sm">
                <label for="complex-zip-code">PLZ</label>
                <input id="complex-zip-code" type="text" bind:value={dto.zipCode} required />
            </div>
            <div class="form-group">
                <label for="complex-city">Stadt</label>
                <input id="complex-city" type="text" bind:value={dto.city} required />
            </div>
        </div>

        <div class="form-group">
            <label for="complex-district">Bezirk (optional)</label>
            <input id="complex-district" type="text" bind:value={dto.district} placeholder="z.B. Leopoldstadt" />
        </div>

        <div class="form-group">
            <label for="complex-image-url">Bilder (URLs)</label>
            <div class="image-input-row">
                <input id="complex-image-url" type="url" bind:value={imageUrlInput} placeholder="https://..." />
                <button type="button" class="btn-add" on:click={addImageUrl}>+</button>
            </div>
            {#each dto.imageUrls as url, i}
                <div class="image-tag">
                    <span>{url}</span>
                    <button type="button" on:click={() => removeImageUrl(i)}>×</button>
                </div>
            {/each}
        </div>

        <button type="submit" class="btn-save">Anlage Speichern</button>
    </form>
</div>

<style>
    .container { max-width: 600px; margin: 40px auto; background: white; padding: 30px; border-radius: 12px; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 0.9rem; }
    input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; }
    .row { display: flex; gap: 15px; }
    .sm { flex: 1; } .form-group { flex: 3; }
    .image-input-row { display: flex; gap: 8px; }
    .image-input-row input { flex: 1; }
    .btn-add { padding: 10px 16px; background: #2980b9; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 1.2rem; flex-shrink: 0; }
    .image-tag { display: flex; align-items: center; justify-content: space-between; margin-top: 6px; padding: 6px 10px; background: #f0f4f8; border-radius: 6px; font-size: 0.8rem; word-break: break-all; }
    .image-tag button { background: none; border: none; color: #e74c3c; cursor: pointer; font-size: 1.1rem; margin-left: 8px; flex-shrink: 0; }
    .btn-save { width: 100%; background: #d35400; color: white; padding: 12px; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; margin-top: 10px;}
</style>
