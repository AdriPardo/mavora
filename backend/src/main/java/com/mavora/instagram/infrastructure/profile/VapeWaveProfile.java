package com.mavora.instagram.infrastructure.profile;

import java.util.List;

/**
 * Hechos públicos de {@code @vapewave.vlc}. Nada de métricas, presupuesto,
 * dirección o PVP inventados: si no está en el perfil, no está aquí.
 */
public final class VapeWaveProfile {

    public static final String USERNAME = "vapewave.vlc";
    public static final String DISPLAY_NAME = "VapeWave";
    public static final String BIOGRAPHY =
            "60K COLLECTION. 10 sabores. Valencia. Pedidos por DM. Solo adultos 18+.";
    public static final String CATEGORY = "Tienda de vapeo";
    public static final String ABOUT =
            "VapeWave en Valencia (@vapewave.vlc). Colección 60K, diez sabores. Pedidos por DM. "
                    + "THIS IS THE WAVE. THIS IS VAPEWAVE. Sin claims de salud.";
    public static final String MARKET = "Vapeo · Valencia (VLC)";
    public static final String PRODUCT_NAME = "Colección 60K";
    public static final String PRODUCT_DESCRIPTION =
            "Línea de vapers que el perfil presenta como colección 60K (10 sabores). Captions citan "
                    + "strawberry ice, watermelon blast, triple grape (Vera VR22K) y grape ice + kiwi. "
                    + "Dispositivo recargable USB-C según ficha del post. Pedidos por DM.";
    public static final String VOICE =
            "Directa, enérgica, tuteo. Eslogan de los captions: THIS IS THE WAVE. THIS IS VAPEWAVE. "
                    + "Sin promesas de salud ni de algoritmo.";
    public static final String OFFER =
            "Colección 60K · 10 sabores. Pedidos por DM. En captions aparece una oferta 2 uds / 30 € "
                    + "(confirmar vigencia). El PVP suelto sale como 65fls: moneda no clara, no la usamos.";
    public static final String CTA = "Pedidos por DM. Solo +18.";
    public static final String AUDIENCE =
            "Adultos 18+ en Valencia y alrededores que ya vaporizan o buscan recambio de sabor. Nunca menores.";
    public static final String EXTRA_NOTES =
            "Fuente: captions públicos de @vapewave.vlc. No hay ficha de Google Maps ni web en la bio. "
                    + "No inventamos dirección, horario, WhatsApp, seguidores, presupuesto ni resultados. "
                    + "Contenido 18+: sin caras de menores, sin claims de salud, sin ‘deja el tabaco’. "
                    + "El icono 🚚 en posts sugiere envío: tratarlo como hipótesis, no como hecho.";
    public static final List<String> CAPTIONS = List.of(
            "60K COLLECTION. 10 sabores. Una misma ola. Valencia. Pedidos DM #VapeWave",
            "THIS IS THE WAVE. THIS IS VAPEWAVE. Valencia. Pedidos DM #VapeWave #VapeWaveVLC #60KCollection",
            "Vera VR22K — triple grape. more info dm. #VapeWave #Valencia #VLC",
            "strawberry ice. inclui envío. more info dm.",
            "watermelon blast. more info dm.",
            "Grape Ice + Kiwi. Oferta especial 2 uds / 30 € (confirmar). Pedidos DM."
    );

    public static final String KNOWLEDGE_BODY = """
            Cuenta @vapewave.vlc (VapeWave). Valencia.
            Bio / eslogan visible: 60K COLLECTION, 10 sabores, pedidos por DM, THIS IS THE WAVE.
            Productos citados en captions: colección 60K; Vera VR22K triple grape; strawberry ice; \
            watermelon blast; grape ice + kiwi. Ficha de post: mesh coil, airflow, batería recargable USB-C.
            CTA: Pedidos por DM. Solo adultos.
            No hay dirección, web ni teléfono verificados. No hay métrica de seguidores fiable.
            No prometemos resultados de algoritmo ni beneficios de salud.
            """.stripIndent();

    private VapeWaveProfile() {
    }
}
