<script>
	import { user, logout } from '$lib/auth';
	import { goto } from '$app/navigation';
	import '../app.css';

	function handleLogout() {
		logout();
		goto('/');
	}
</script>

<div class="min-h-screen flex flex-col font-sans">
	<nav class="sticky top-0 z-50 w-full bg-white/95 backdrop-blur border-b border-gray-200 shadow-sm">
		<div class="container mx-auto px-4 h-16 flex justify-between items-center">

			<a href="/" class="text-xl font-bold text-slate-800 hover:text-blue-600 transition-colors flex items-center gap-2">
				<span>🏘️</span> Cohabit
			</a>

			<div class="flex items-center gap-3 md:gap-4">
				{#if $user}
					<div class="hidden md:flex flex-col items-end mr-2">
						<span class="text-sm font-semibold text-slate-700">{$user?.firstName || $user?.email}</span>
						<span class="text-xs font-bold text-slate-500 uppercase tracking-wide">
                          {$user.roles?.includes('VENDOR') ? 'Anbieter' : 'Mieter'}
                      </span>
					</div>

					{#if $user.roles && $user.roles.includes('VENDOR')}
						<a href="/vendor/dashboard"
						   class="inline-flex items-center justify-center px-4 py-2 text-sm font-bold text-white bg-amber-500 rounded-full hover:bg-amber-600 hover:scale-105 transition-all shadow-md">
							Dashboard
						</a>
					{/if}

					{#if $user.roles && $user.roles.includes('USER')}
						<a href="/bookmarks"
						   class="inline-flex items-center gap-1 px-4 py-2 text-sm font-medium text-slate-600 border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors">
							🔖 Merkliste
						</a>
					{/if}

					<button on:click={handleLogout}
							class="px-4 py-2 text-sm font-medium text-slate-600 border border-slate-300 rounded-lg hover:bg-slate-50 hover:text-slate-900 transition-colors">
						Logout
					</button>
				{:else}
					<a href="/login"
					   class="px-5 py-2 text-sm font-bold text-white bg-slate-800 rounded-lg hover:bg-slate-700 transition-colors shadow-md">
						Anmelden
					</a>
				{/if}
			</div>
		</div>
	</nav>

	<main class="flex-grow w-full">
		<slot />
	</main>

</div>