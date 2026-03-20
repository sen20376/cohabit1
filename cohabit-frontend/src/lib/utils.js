export const CRITERIA_LABELS = {
    noise: 'Lautstärke',
    light: 'Helligkeit',
    cleanliness: 'Sauberkeit',
    transport: 'Öffis',
    neighborhood: 'Gegend'
};

export function defaultRatingForm() {
    return {
        score: 5,
        comment: '',
        thematicRatings: { noise: 3, light: 3, cleanliness: 3, transport: 3, neighborhood: 3 }
    };
}

export function formatDate(isoString) {
    if (!isoString) return 'Gerade eben';
    return new Date(isoString).toLocaleDateString('de-DE');
}
