package com.mavora.instagram.infrastructure.profile;

import java.util.List;

/**
 * Hechos de {@code @vapewave.vlc} más lo que confirma el dueño.
 * No inventa cifra de alcance, seguidores, fecha ni presupuesto.
 */
public final class VapeWaveProfile {

    public static final String USERNAME = "vapewave.vlc";
    public static final String DISPLAY_NAME = "VapeWave";
    public static final String BIOGRAPHY =
            "60K COLLECTION. 10 sabores. Valencia. Pedidos por DM. Solo adultos 18+.";
    public static final String CATEGORY = "Tienda de vapeo";
    public static final String ABOUT =
            "VapeWave en Valencia (@vapewave.vlc). Solo colección 60K, diez sabores, 15 €/ud. "
                    + "Ventas por DM o WhatsApp. THIS IS THE WAVE. THIS IS VAPEWAVE. Sin claims de salud.";
    public static final String MARKET = "Vapeo · Valencia (VLC)";
    public static final String PRODUCT_NAME = "Colección 60K";
    public static final String PRODUCT_DESCRIPTION =
            "Única línea: colección 60K, diez sabores (strawberry ice, watermelon blast, triple grape / Vera VR22K, "
                    + "grape ice + kiwi). 15 € la unidad. Recargable USB-C según ficha del post. "
                    + "Pedidos por DM o WhatsApp. No hay tienda física.";
    public static final String VOICE =
            "Directa, enérgica, tuteo. Eslogan: THIS IS THE WAVE. THIS IS VAPEWAVE. "
                    + "Sin promesas de salud ni de algoritmo.";
    public static final String OFFER =
            "Colección 60K · 10 sabores · 15 €/ud. Solo esta línea. Pedidos por DM o WhatsApp. Solo +18.";
    public static final String CTA = "Pedidos por DM o WhatsApp. 15 €. Solo +18.";
    public static final String AUDIENCE =
            "Adultos 18+ en Valencia y alrededores que ya vaporizan o buscan recambio de sabor. Nunca menores.";
    public static final String EXTRA_NOTES =
            "Dueño: ventas solo por DM o WhatsApp (falta el número para pegarlo en el copy), PVP 15 €, "
                    + "solo colección 60K. KPIs a medir: alcance y seguidores de Instagram. "
                    + "Aún no hay cifra objetivo, fecha ni presupuesto: no los inventamos. "
                    + "Contenido 18+: sin menores, sin claims de salud, sin ‘deja el tabaco’. "
                    + "No prometemos envío ni tienda física.";
    public static final List<String> CAPTIONS = List.of(
            "60K COLLECTION. 10 sabores. Una misma ola. Valencia. Pedidos DM #VapeWave",
            "THIS IS THE WAVE. THIS IS VAPEWAVE. Valencia. Pedidos DM #VapeWave #VapeWaveVLC #60KCollection",
            "Vera VR22K — triple grape. more info dm. #VapeWave #Valencia #VLC",
            "strawberry ice. inclui envío. more info dm.",
            "watermelon blast. more info dm.",
            "Grape Ice + Kiwi. Pedidos DM."
    );

    public static final String KNOWLEDGE_BODY = """
            Cuenta @vapewave.vlc (VapeWave). Valencia.
            Dueño confirma: solo colección 60K, 15 €/ud, ventas por DM o WhatsApp, sin tienda física.
            KPIs: alcance y seguidores. Sin cifra, fecha ni presupuesto todavía.
            Bio pública: 60K COLLECTION, 10 sabores, pedidos por DM, THIS IS THE WAVE.
            Sabores citados: strawberry ice, watermelon blast, triple grape (Vera VR22K), grape ice + kiwi.
            Contenido 18+. No prometemos algoritmo ni salud.
            """.stripIndent();

    private VapeWaveProfile() {
    }
}
