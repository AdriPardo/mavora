-- Dueño de VapeWave: 15 €/ud, solo 60K, ventas por DM o WhatsApp.
-- KPIs: alcance y seguidores. No inventa cifra, fecha ni presupuesto.

UPDATE companies
SET description = 'VapeWave en Valencia (@vapewave.vlc). Solo colección 60K, diez sabores, 15 €/ud. Ventas por DM o WhatsApp. Solo adultos 18+.',
    market = 'Vapeo · Valencia (VLC)',
    updated_at = now()
WHERE name = 'VapeWave';

UPDATE products p
SET description = 'Única línea: colección 60K, diez sabores. 15 € la unidad. Pedidos por DM o WhatsApp. No hay tienda física.',
    url = NULL
FROM companies c
WHERE p.company_id = c.id
  AND c.name = 'VapeWave'
  AND p.name = 'Colección 60K';

UPDATE brand_briefs b
SET voice = 'Directa, enérgica, tuteo. Eslogan: THIS IS THE WAVE. THIS IS VAPEWAVE. Sin promesas de salud ni de algoritmo.',
    offer = 'Colección 60K · 10 sabores · 15 €/ud. Solo esta línea. Pedidos por DM o WhatsApp. Solo +18.',
    cta = 'Pedidos por DM o WhatsApp. 15 €. Solo +18.',
    audience = 'Adultos 18+ en Valencia y alrededores que ya vaporizan o buscan recambio de sabor. Nunca menores.',
    extra_notes = 'Dueño: ventas solo por DM o WhatsApp (falta el número para pegarlo en el copy), PVP 15 €, solo colección 60K. KPIs: alcance y seguidores de Instagram. Aún no hay cifra objetivo, fecha ni presupuesto: no los inventamos. Contenido 18+. No prometemos envío ni tienda física.',
    updated_at = now()
FROM companies c
WHERE b.organization_id = c.organization_id
  AND c.name = 'VapeWave';

UPDATE knowledge_items k
SET body = 'Cuenta @vapewave.vlc (VapeWave). Valencia. Dueño confirma: solo colección 60K, 15 €/ud, ventas por DM o WhatsApp, sin tienda física. KPIs: alcance y seguidores. Sin cifra, fecha ni presupuesto todavía. Bio pública: 60K COLLECTION, 10 sabores, THIS IS THE WAVE. Contenido 18+.',
    confidence = 85
WHERE k.source = 'instagram:@vapewave.vlc'
  AND k.kind = 'FACT'
  AND k.title = 'Perfil @vapewave.vlc';

UPDATE knowledge_items k
SET kind = 'FACT',
    title = 'Canal de venta VapeWave',
    body = 'El dueño confirma ventas solo por DM o WhatsApp. No hay tienda física. No prometemos envío en el copy.',
    confidence = 90
WHERE k.source = 'instagram:@vapewave.vlc'
  AND k.title IN ('Envío a domicilio @vapewave.vlc', 'Canal de venta VapeWave');

INSERT INTO knowledge_items (
    id, organization_id, kind, title, body, source, confidence, created_at, version
)
SELECT gen_random_uuid(),
       c.organization_id,
       'DECISION',
       'KPIs Instagram VapeWave',
       'Medimos alcance y seguidores. Registrar snapshots reales en Analítica (métricas: alcance, seguidores). Reels para alcance; feed e historias para seguimiento. No hay cifra objetivo, fecha ni presupuesto todavía: no se inventan.',
       'owner:vapewave.vlc',
       80,
       now(),
       0
FROM companies c
WHERE c.name = 'VapeWave'
  AND NOT EXISTS (
      SELECT 1
      FROM knowledge_items k
      WHERE k.organization_id = c.organization_id
        AND k.source = 'owner:vapewave.vlc'
        AND k.title = 'KPIs Instagram VapeWave'
  );

INSERT INTO knowledge_items (
    id, organization_id, kind, title, body, source, confidence, created_at, version
)
SELECT gen_random_uuid(),
       c.organization_id,
       'FACT',
       'PVP VapeWave 15 €',
       'El dueño confirma 15 € la unidad. Solo colección 60K. No usar 65fls ni la oferta 2x30 € de captions viejos hasta que el dueño la reactive.',
       'owner:vapewave.vlc',
       90,
       now(),
       0
FROM companies c
WHERE c.name = 'VapeWave'
  AND NOT EXISTS (
      SELECT 1
      FROM knowledge_items k
      WHERE k.organization_id = c.organization_id
        AND k.source = 'owner:vapewave.vlc'
        AND k.title = 'PVP VapeWave 15 €'
  );

