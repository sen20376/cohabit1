<script>
    import { apiCall } from '$lib/api';
    import { token, user } from '$lib/auth';
    import { goto } from '$app/navigation';

    let email = '';
    let password = '';
    let isRegister = false;
    let loading = false;
    let errorMsg = '';

    let regData = {
        email: '', password: '', requestedRole: 'USER',
        address: { street: '', city: '', postalCode: '', country: '' }
    };

    async function handleLogin() {
        loading = true; errorMsg = '';
        try {
            const res = await apiCall('auth', '/api/v1/login', 'POST', { email, password });
            token.set(res.token);
            const me = await apiCall('auth', '/api/v1/users/me', 'GET');
            user.set(me);
            me.roles && me.roles.includes('VENDOR') ? goto('/vendor/dashboard') : goto('/');
        } catch (e) {
            errorMsg = "Fehler: " + e.message;
        } finally {
            loading = false;
        }
    }

    async function handleRegister() {
        loading = true; errorMsg = '';
        try {
            await apiCall('auth', '/api/v1/register', 'POST', regData);
            alert("Erfolg! Bitte einloggen.");
            isRegister = false;
        } catch (e) {
            errorMsg = "Fehler: " + e.message;
        } finally {
            loading = false;
        }
    }
</script>

<div class="min-h-[calc(100vh-64px)] w-full flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8 bg-white p-10 rounded-2xl shadow-xl border border-gray-100">

        <div class="text-center">
            <h2 class="mt-2 text-3xl font-extrabold text-gray-900">
                {isRegister ? 'Konto erstellen' : 'Willkommen zurück'}
            </h2>
            <p class="mt-2 text-sm text-gray-600">
                {isRegister ? 'Werde Teil von Cohabit' : 'Logge dich ein, um fortzufahren'}
            </p>
        </div>

        {#if errorMsg}
            <div class="bg-red-50 border-l-4 border-red-500 p-4 text-red-700 text-sm rounded">
                {errorMsg}
            </div>
        {/if}

        <div class="mt-8 space-y-6">
            {#if !isRegister}
                <div class="space-y-4">
                    <div>
                        <label for="email" class="sr-only">Email</label>
                        <input id="email" type="email" bind:value={email} required placeholder="Email Adresse"
                               class="appearance-none rounded-lg relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm" />
                    </div>
                    <div>
                        <label for="password" class="sr-only">Passwort</label>
                        <input id="password" type="password" bind:value={password} required placeholder="Passwort"
                               class="appearance-none rounded-lg relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm" />
                    </div>
                </div>

                <button on:click={handleLogin} disabled={loading}
                        class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-slate-800 hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-slate-500 transition-colors disabled:bg-gray-400">
                    {loading ? 'Lade...' : 'Einloggen'}
                </button>
            {:else}
                <div class="space-y-4">
                    <div class="bg-blue-50 p-4 rounded-lg border border-blue-100">
                        <label for="requested-role" class="block text-sm font-bold text-blue-900 mb-2">Ich bin:</label>
                        <select id="requested-role" bind:value={regData.requestedRole} class="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                            <option value="USER">👤 Mieter (Suchend)</option>
                            <option value="VENDOR">🏠 Anbieter (Vermieter)</option>
                        </select>
                    </div>

                    <input type="email" bind:value={regData.email} placeholder="Email" class="block w-full px-3 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
                    <input type="password" bind:value={regData.password} placeholder="Passwort" class="block w-full px-3 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />

                    <div class="border-t border-gray-100 pt-4">
                        <p class="text-xs text-gray-400 mb-2 uppercase font-bold tracking-wider">Adresse (Optional)</p>
                        <div class="grid grid-cols-1 gap-3">
                            <input type="text" placeholder="Straße" bind:value={regData.address.street} class="px-3 py-2 border rounded-md text-sm" />
                            <div class="grid grid-cols-3 gap-3">
                                <input type="text" placeholder="PLZ" bind:value={regData.address.postalCode} class="col-span-1 px-3 py-2 border rounded-md text-sm" />
                                <input type="text" placeholder="Stadt" bind:value={regData.address.city} class="col-span-2 px-3 py-2 border rounded-md text-sm" />
                            </div>
                            <input type="text" placeholder="Land" bind:value={regData.address.country} class="px-3 py-2 border rounded-md text-sm" />
                        </div>
                    </div>
                </div>

                <button on:click={handleRegister} disabled={loading}
                        class="w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none transition-colors">
                    {loading ? 'Registriere...' : 'Konto erstellen'}
                </button>
            {/if}
        </div>

        <div class="text-center">
            <button on:click={() => {isRegister = !isRegister; errorMsg='';}} class="text-sm font-medium text-blue-600 hover:text-blue-500 underline">
                {isRegister ? 'Zurück zum Login' : 'Noch kein Konto? Jetzt registrieren'}
            </button>
        </div>
    </div>
</div>