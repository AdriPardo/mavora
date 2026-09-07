package com.mavora.instagram.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Horarios y mezcla de formatos pensados para cuentas en España.
 * No promete resultados del algoritmo: concentra el esfuerzo en hooks, ritmo y CTA de venta.
 */
public final class InstagramPlaybook {

    public static final ZoneId ZONE = ZoneId.of("Europe/Madrid");

    public static final String PRINCIPLES = """
            1. Hook en el primer segundo / primera línea: el feed decide en menos de un segundo.
            2. Un objetivo por pieza: visita al perfil, seguimiento o venta (bio / DM / enlace).
            3. Reels para alcance; carruseles para guardados; historias para conversación diaria; feed para prueba social.
            4. CTA explícito de conversión (oferta, demo, reserva, “escribe QUIERO”).
            5. 3–8 hashtags de nicho, no 30 genéricos. Texto nativo, no watermark.
            6. Publicar cuando la audiencia ES está activa: mañana, mediodía y prime time nocturno.
            7. Ritmo constante supera picos aislados. Mavora ejecuta el calendario sin espera humana.
            """.stripIndent();

    private InstagramPlaybook() {
    }

    public static List<SlotBlueprint> weekMix() {
        return List.of(
                new SlotBlueprint(DayOfWeek.MONDAY, LocalTime.of(8, 15), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.MONDAY, LocalTime.of(13, 0), InstagramFormat.FEED),
                new SlotBlueprint(DayOfWeek.MONDAY, LocalTime.of(19, 0), InstagramFormat.REEL),
                new SlotBlueprint(DayOfWeek.TUESDAY, LocalTime.of(8, 15), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.TUESDAY, LocalTime.of(13, 30), InstagramFormat.CAROUSEL),
                new SlotBlueprint(DayOfWeek.TUESDAY, LocalTime.of(21, 10), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.WEDNESDAY, LocalTime.of(8, 15), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), InstagramFormat.REEL),
                new SlotBlueprint(DayOfWeek.THURSDAY, LocalTime.of(13, 0), InstagramFormat.FEED),
                new SlotBlueprint(DayOfWeek.THURSDAY, LocalTime.of(21, 10), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.FRIDAY, LocalTime.of(13, 30), InstagramFormat.CAROUSEL),
                new SlotBlueprint(DayOfWeek.FRIDAY, LocalTime.of(19, 0), InstagramFormat.REEL),
                new SlotBlueprint(DayOfWeek.SATURDAY, LocalTime.of(11, 0), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.SATURDAY, LocalTime.of(13, 0), InstagramFormat.FEED),
                new SlotBlueprint(DayOfWeek.SUNDAY, LocalTime.of(18, 0), InstagramFormat.STORY),
                new SlotBlueprint(DayOfWeek.SUNDAY, LocalTime.of(19, 0), InstagramFormat.REEL)
        );
    }

    public record SlotBlueprint(DayOfWeek day, LocalTime time, InstagramFormat format) {
    }
}
