-- Marca VapeWave (@vapewave.vlc). Solo hechos del perfil público.
-- No inventa métricas, presupuesto, dirección, web ni PVP.
-- Solo rellena organizaciones que aún no tienen empresa / brief.

INSERT INTO companies (
    id, organization_id, name, website_url, description, market, created_at, updated_at, version
)
SELECT gen_random_uuid(),
       o.id,
       'VapeWave',
       NULL,
       'VapeWave en Valencia (@vapewave.vlc). Colección 60K, diez sabores. Pedidos por DM. Solo adultos 18+.',
       'Vapeo · Valencia (VLC)',
       now(),
       now(),
       0
FROM organizations o
WHERE NOT EXISTS (
    SELECT 1 FROM companies c WHERE c.organization_id = o.id
);

INSERT INTO products (
    id, organization_id, company_id, name, description, url, created_at, version
)
SELECT gen_random_uuid(),
       c.organization_id,
       c.id,
       'Colección 60K',
       'Línea de vapers que el perfil presenta como colección 60K (10 sabores). Captions citan strawberry ice, watermelon blast, triple grape (Vera VR22K) y grape ice + kiwi. Pedidos por DM.',
       NULL,
       now(),
       0
FROM companies c
WHERE c.name = 'VapeWave'
  AND NOT EXISTS (
      SELECT 1 FROM products p WHERE p.company_id = c.id
  );

INSERT INTO brand_briefs (
    id, organization_id, voice, offer, cta, audience, extra_notes, created_at, updated_at, version
)
SELECT gen_random_uuid(),
       o.id,
       'Directa, enérgica, tuteo. Eslogan de los captions: THIS IS THE WAVE. THIS IS VAPEWAVE. Sin promesas de salud ni de algoritmo.',
       'Colección 60K · 10 sabores. Pedidos por DM. En captions aparece una oferta 2 uds / 30 € (confirmar vigencia). El PVP suelto sale como 65fls: moneda no clara, no la usamos.',
       'Pedidos por DM. Solo +18.',
       'Adultos 18+ en Valencia y alrededores que ya vaporizan o buscan recambio de sabor. Nunca menores.',
       'Fuente: captions públicos de @vapewave.vlc. No hay ficha de Google Maps ni web en la bio. No inventamos dirección, horario, WhatsApp, seguidores, presupuesto ni resultados. Contenido 18+: sin caras de menores, sin claims de salud, sin «deja el tabaco». El icono de envío en posts es hipótesis, no hecho.',
       now(),
       now(),
       0
FROM organizations o
WHERE NOT EXISTS (
    SELECT 1 FROM brand_briefs b WHERE b.organization_id = o.id
);

INSERT INTO knowledge_items (
    id, organization_id, kind, title, body, source, confidence, created_at, version
)
SELECT gen_random_uuid(),
       o.id,
       'FACT',
       'Perfil @vapewave.vlc',
       'Cuenta @vapewave.vlc (VapeWave). Valencia. Bio / eslogan: 60K COLLECTION, 10 sabores, pedidos por DM, THIS IS THE WAVE. Productos citados: colección 60K; Vera VR22K triple grape; strawberry ice; watermelon blast; grape ice + kiwi. CTA: Pedidos por DM. Solo adultos. No hay dirección, web ni teléfono verificados. No hay métrica de seguidores fiable. No prometemos resultados de algoritmo ni beneficios de salud.',
       'instagram:@vapewave.vlc',
       70,
       now(),
       0
FROM organizations o
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge_items k
    WHERE k.organization_id = o.id
      AND k.source = 'instagram:@vapewave.vlc'
);

INSERT INTO knowledge_items (
    id, organization_id, kind, title, body, source, confidence, created_at, version
)
SELECT gen_random_uuid(),
       o.id,
       'ASSUMPTION',
       'Envío a domicilio @vapewave.vlc',
       'Algunos captions llevan icono de envío y texto de entrega. No está confirmado cobertura, coste ni plazo. Preguntar al dueño antes de prometer envío en el copy.',
       'instagram:@vapewave.vlc',
       40,
       now(),
       0
FROM organizations o
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge_items k
    WHERE k.organization_id = o.id
      AND k.kind = 'ASSUMPTION'
      AND k.source = 'instagram:@vapewave.vlc'
);
