-- ====================================================================================
-- 0. CLEANUP
-- ====================================================================================
TRUNCATE TABLE image, apartment, residential_complex, point_of_interest, geo_region CASCADE;

-- ====================================================================================
-- 1. GEO REGION
-- ====================================================================================
INSERT INTO geo_region (id, code, name, level, created_at, geometry)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           '1020',
           'Leopoldstadt',
           'DISTRICT',
           NOW(),
           ST_GeomFromText('POLYGON((16.37 48.21, 16.42 48.21, 16.42 48.23, 16.37 48.23, 16.37 48.21))', 4326)
       );

-- ====================================================================================
-- 2. RESIDENTIAL COMPLEXES
-- ====================================================================================
INSERT INTO residential_complex (
    id, name, street_name, house_number, zip_code, city,
    location, attributes, geo_region_id, created_at, avg_rating,
    owner_id
)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           'Nordbahn-Hof',
           'Bruno-Marek-Allee', '5', '1020', 'Wien',
           ST_SetSRID(ST_MakePoint(16.388, 48.226), 4326),
           '{"year_built": 2021, "heating": "district", "parking": true}'::jsonb,
           '11111111-1111-1111-1111-111111111111',
           NOW(),
           4.5,
           'fff0e4d7-aebe-4978-a212-89404a92c117'
       );

INSERT INTO residential_complex (
    id, name, street_name, house_number, zip_code, city,
    location, attributes, geo_region_id, created_at, avg_rating,
    owner_id
)
VALUES (
           '33333333-3333-3333-3333-333333333333',
           'Karmeliter-Residenz',
           'Taborstraße', '24a', '1020', 'Wien',
           ST_SetSRID(ST_MakePoint(16.381, 48.215), 4326),
           '{"year_built": 1910, "elevator": true, "high_ceilings": true}'::jsonb,
           '11111111-1111-1111-1111-111111111111',
           NOW(),
           3.8,
           'fff0e4d7-aebe-4978-a212-89404a92c117'
       );

-- ====================================================================================
-- 3. APARTMENTS
-- Neu: view_count hinzugefügt für S-03
-- ====================================================================================
INSERT INTO apartment (
    id, complex_id, owner_id, title, description,
    door_number, floor, size_sqm, avg_rating, active, created_at,
    view_count -- <--- NEU
)
VALUES (
           '44444444-4444-4444-4444-444444444444',
           '22222222-2222-2222-2222-222222222222',
           'fff0e4d7-aebe-4978-a212-89404a92c117',
           'Modernes Loft mit Balkon',
           'Helle 2-Zimmer Wohnung direkt am Park. Fußbodenheizung und Smart-Home Ausstattung.',
           '12', 3, 65.5,
           4.8, true, NOW(),
           15 -- <--- Startwert Aufrufe
       );

INSERT INTO apartment (
    id, complex_id, owner_id, title, description,
    door_number, floor, size_sqm, avg_rating, active, created_at,
    view_count -- <--- NEU
)
VALUES (
           '55555555-5555-5555-5555-555555555555',
           '22222222-2222-2222-2222-222222222222',
           'fff0e4d7-aebe-4978-a212-89404a92c117',
           'Exklusives Penthouse',
           'Riesige Dachterrasse mit Blick über ganz Wien. Erstbezug.',
           'Top 45', 7, 120.0,
           5.0, true, NOW(),
           42 -- <--- Startwert Aufrufe
       );

INSERT INTO apartment (
    id, complex_id, owner_id, title, description,
    door_number, floor, size_sqm, avg_rating, active, created_at,
    view_count -- <--- NEU
)
VALUES (
           '66666666-6666-6666-6666-666666666666',
           '33333333-3333-3333-3333-333333333333',
           'fff0e4d7-aebe-4978-a212-89404a92c117',
           'Charmante Altbauwohnung',
           'Hohe Räume, Fischgrätparkett, Flügeltüren. Perfekt für WG geeignet.',
           '4', 1, 85.0,
           4.2, true, NOW(),
           8 -- <--- Startwert Aufrufe
       );

-- ====================================================================================
-- 4. IMAGES
-- ====================================================================================

-- Bilder für Residential Complex
INSERT INTO image (id, complex_id, url, caption, isprimary, created_at) VALUES
                                                                            (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'https://placehold.co/600x400/2c3e50/ffffff?text=Nordbahn+Fassade', 'Außenansicht', true, NOW()),
                                                                            (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'https://placehold.co/600x400/27ae60/ffffff?text=Innenhof+Park', 'Der Park', false, NOW()),
                                                                            (gen_random_uuid(), '33333333-3333-3333-3333-333333333333', 'https://placehold.co/600x400/8e44ad/ffffff?text=Altbau+Fassade', 'Front', true, NOW());

-- Bilder für Apartments
INSERT INTO image (id, apartment_id, url, caption, isprimary, created_at) VALUES
                                                                              (gen_random_uuid(), '44444444-4444-4444-4444-444444444444', 'https://placehold.co/600x400/e67e22/ffffff?text=Wohnzimmer+Loft', 'Wohnbereich', true, NOW()),
                                                                              (gen_random_uuid(), '44444444-4444-4444-4444-444444444444', 'https://placehold.co/600x400/d35400/ffffff?text=Kueche', 'Küche', false, NOW());


-- ====================================================================================
-- 5. POINTS OF INTEREST
-- ====================================================================================
INSERT INTO point_of_interest (id, name, category, subcategory, location, properties, created_at) VALUES
    (gen_random_uuid(), 'Praterstern (U1, U2)', 'TRANSPORT', 'subway_station', ST_SetSRID(ST_MakePoint(16.391, 48.218), 4326), '{"lines": ["U1", "U2", "S-Bahn"]}'::jsonb, NOW());

INSERT INTO point_of_interest (id, name, category, subcategory, location, properties, created_at) VALUES
    (gen_random_uuid(), 'Billa Plus', 'SUPPLY', 'supermarket', ST_SetSRID(ST_MakePoint(16.389, 48.227), 4326), '{"brand": "Billa", "opening_hours": "07:15-19:30"}'::jsonb, NOW());

INSERT INTO point_of_interest (id, name, category, subcategory, location, properties, created_at) VALUES
    (gen_random_uuid(), 'Augarten', 'LEISURE', 'park', ST_SetSRID(ST_MakePoint(16.375, 48.225), 4326), '{"access": "public", "dogs_allowed": false}'::jsonb, NOW());