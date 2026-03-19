
// this file is generated — do not edit it


declare module "svelte/elements" {
	export interface HTMLAttributes<T> {
		'data-sveltekit-keepfocus'?: true | '' | 'off' | undefined | null;
		'data-sveltekit-noscroll'?: true | '' | 'off' | undefined | null;
		'data-sveltekit-preload-code'?:
			| true
			| ''
			| 'eager'
			| 'viewport'
			| 'hover'
			| 'tap'
			| 'off'
			| undefined
			| null;
		'data-sveltekit-preload-data'?: true | '' | 'hover' | 'tap' | 'off' | undefined | null;
		'data-sveltekit-reload'?: true | '' | 'off' | undefined | null;
		'data-sveltekit-replacestate'?: true | '' | 'off' | undefined | null;
	}
}

export {};


declare module "$app/types" {
	export interface AppTypes {
		RouteId(): "/" | "/bookmarks" | "/complex" | "/complex/[complexId]" | "/complex/[complexId]/apartment" | "/complex/[complexId]/apartment/[apartmentId]" | "/login" | "/vendor" | "/vendor/create-apartment" | "/vendor/create-complex" | "/vendor/dashboard" | "/vendor/edit-apartment" | "/vendor/edit-apartment/[id]" | "/vendor/edit-complex" | "/vendor/edit-complex/[id]";
		RouteParams(): {
			"/complex/[complexId]": { complexId: string };
			"/complex/[complexId]/apartment": { complexId: string };
			"/complex/[complexId]/apartment/[apartmentId]": { complexId: string; apartmentId: string };
			"/vendor/edit-apartment/[id]": { id: string };
			"/vendor/edit-complex/[id]": { id: string }
		};
		LayoutParams(): {
			"/": { complexId?: string; apartmentId?: string; id?: string };
			"/bookmarks": Record<string, never>;
			"/complex": { complexId?: string; apartmentId?: string };
			"/complex/[complexId]": { complexId: string; apartmentId?: string };
			"/complex/[complexId]/apartment": { complexId: string; apartmentId?: string };
			"/complex/[complexId]/apartment/[apartmentId]": { complexId: string; apartmentId: string };
			"/login": Record<string, never>;
			"/vendor": { id?: string };
			"/vendor/create-apartment": Record<string, never>;
			"/vendor/create-complex": Record<string, never>;
			"/vendor/dashboard": Record<string, never>;
			"/vendor/edit-apartment": { id?: string };
			"/vendor/edit-apartment/[id]": { id: string };
			"/vendor/edit-complex": { id?: string };
			"/vendor/edit-complex/[id]": { id: string }
		};
		Pathname(): "/" | "/bookmarks" | "/bookmarks/" | "/complex" | "/complex/" | `/complex/${string}` & {} | `/complex/${string}/` & {} | `/complex/${string}/apartment` & {} | `/complex/${string}/apartment/` & {} | `/complex/${string}/apartment/${string}` & {} | `/complex/${string}/apartment/${string}/` & {} | "/login" | "/login/" | "/vendor" | "/vendor/" | "/vendor/create-apartment" | "/vendor/create-apartment/" | "/vendor/create-complex" | "/vendor/create-complex/" | "/vendor/dashboard" | "/vendor/dashboard/" | "/vendor/edit-apartment" | "/vendor/edit-apartment/" | `/vendor/edit-apartment/${string}` & {} | `/vendor/edit-apartment/${string}/` & {} | "/vendor/edit-complex" | "/vendor/edit-complex/" | `/vendor/edit-complex/${string}` & {} | `/vendor/edit-complex/${string}/` & {};
		ResolvedPathname(): `${"" | `/${string}`}${ReturnType<AppTypes['Pathname']>}`;
		Asset(): "/robots.txt" | string & {};
	}
}