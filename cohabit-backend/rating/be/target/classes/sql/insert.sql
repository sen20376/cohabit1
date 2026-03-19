-- ====================================================================================
-- 0. CLEANUP (Wichtig beim Neustart)
-- ====================================================================================
TRUNCATE TABLE ratings, rating_criteria_definition CASCADE;

-- ====================================================================================
-- 1. RATING CRITERIA (Das Vokabular für deine Map)
-- ====================================================================================
INSERT INTO rating_criteria_definition (key, label, active, created_at, updated_at)
VALUES
    ('noise', 'Lautstärke', true, NOW(), NOW()),
    ('light', 'Helligkeit', true, NOW(), NOW()),
    ('transport', 'Öffentliche Anbindung', true, NOW(), NOW()),
    ('neighborhood', 'Nachbarschaft', true, NOW(), NOW()),
    ('cleanliness', 'Sauberkeit', true, NOW(), NOW());

-- ====================================================================================
-- 2. RATINGS (Beispiel-Bewertungen)
-- Wichtig: Die 'target_id' muss zu einer ID im Catalog-Service passen!
-- ====================================================================================

-- 2.1 Bewertung für "Nordbahn-Hof" (Complex ID aus deinem Catalog SQL)
INSERT INTO ratings (
    id,
    target_id,
    user_id,
    total_score,
    comment,
    helpful_votes,
    thematic_ratings,
    created_at,
    updated_at
)
VALUES (
           gen_random_uuid(),
           '22222222-2222-2222-2222-222222222222', -- Target: Nordbahn-Hof
           '11111111-1111-1111-1111-111111111111', -- Dummy User ID
           5,
           'Super moderne Anlage. Sehr ruhig im Innenhof.',
           12,
           '{"noise": 5, "light": 4, "transport": 5}'::jsonb, -- JSONB Map
           NOW(),
           NOW()
       );

-- 2.2 Bewertung für "Karmeliter-Residenz"
INSERT INTO ratings (
    id,
    target_id,
    user_id,
    total_score,
    comment,
    helpful_votes,
    thematic_ratings,
    created_at,
    updated_at
)
VALUES (
           gen_random_uuid(),
           '33333333-3333-3333-3333-333333333333', -- Target: Karmeliter-Residenz
           '99999999-9999-9999-9999-999999999999', -- Anderer Dummy User
           4,
           'Schöner Altbau, aber etwas hellhörig.',
           3,
           '{"noise": 2, "light": 5, "cleanliness": 4}'::jsonb,
           NOW() - INTERVAL '2 days',
           NOW() - INTERVAL '2 days'
       );

-- 2.3 Bewertung für das "Modernes Loft" (Apartment ID)
INSERT INTO ratings (
    id,
    target_id,
    user_id,
    total_score,
    comment,
    helpful_votes,
    thematic_ratings,
    created_at,
    updated_at
)
VALUES (
           gen_random_uuid(),
           '44444444-4444-4444-4444-444444444444', -- Target: Apartment
           '11111111-1111-1111-1111-111111111111',
           5,
           'Perfekte Wohnung für Singles. Die Aussicht ist grandios.',
           0,
           '{"noise": 5, "light": 5, "neighborhood": 5}'::jsonb,
           NOW(),
           NOW()
       );