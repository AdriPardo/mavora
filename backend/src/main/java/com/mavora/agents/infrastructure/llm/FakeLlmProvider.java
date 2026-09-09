package com.mavora.agents.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mavora.agents.application.LlmClient;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmRequest;
import com.mavora.agents.domain.AgentType;
import com.mavora.instagram.infrastructure.profile.VapeWaveProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mavora.llm.provider", havingValue = "fake", matchIfMissing = true)
public class FakeLlmProvider implements LlmClient {

    private final ObjectMapper objectMapper;

    public FakeLlmProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmCompletion complete(LlmRequest request) {
        try {
            String content;
            if (request.userPrompt() != null && request.userPrompt().contains("TASK=BUSINESS_IMPORT")) {
                content = businessImport(request.userPrompt());
            } else {
                content = switch (request.agentType()) {
                    case CMO -> cmo(request.userPrompt());
                    case RESEARCHER -> research(request.userPrompt());
                    case CONTENT -> content(request.userPrompt());
                    case SOCIAL -> social();
                    case ANALYST -> analytics();
                    case INSTAGRAM -> instagram(request.userPrompt());
                };
            }
            int tokens = Math.max(32, content.length() / 4);
            return new LlmCompletion(content, request.model(), tokens, tokens, 1);
        } catch (Exception exception) {
            throw new IllegalStateException("Fake LLM failed to render JSON", exception);
        }
    }

    private String cmo(String prompt) throws Exception {
        String company = extract(prompt, "Company:", "Empresa:");
        String metric = extract(prompt, "Metric:", "Métrica:");
        ObjectNode node = objectMapper.createObjectNode();
        node.put("positioning", company + " es la opción clara para equipos que necesitan marketing autónomo con memoria, no otro chatbot.");
        node.put("icpSummary", "Founders y equipos de producto en España (micro-SaaS y pymes) que venden B2B y no pueden permitirse un CMO a tiempo completo.");
        node.set("channels", strings("linkedin", "blog", "newsletter"));
        node.set("pillars", strings("educación práctica", "prueba social", "producto en público"));
        ArrayNode kpis = objectMapper.createArrayNode();
        ObjectNode kpi = objectMapper.createObjectNode();
        kpi.put("name", metric.isBlank() ? "signups" : metric);
        kpi.put("target", "objetivo activo de la organización");
        kpis.add(kpi);
        node.set("kpis", kpis);
        node.put("narrative", "El CMO prioriza un posicionamiento nítido, un ICP estrecho y un ritmo semanal de contenido aprobado por humanos antes de ejecutar.");
        return objectMapper.writeValueAsString(node);
    }

    private String research(String prompt) throws Exception {
        String company = extract(prompt, "Company:", "Empresa:");
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode personas = objectMapper.createArrayNode();
        personas.add(persona("Marta, founder operativa", "Lleva producto y marketing a la vez.", "No tiene tiempo para un plan que no se ejecute.", "Conseguir pipeline predecible"));
        personas.add(persona("Luis, head of growth", "Necesita un sistema, no más tools sueltas.", "El contexto se pierde entre chats.", "Escalar contenido con control"));
        node.set("personas", personas);
        ArrayNode competitors = objectMapper.createArrayNode();
        competitors.add(competitor("Jasper", "https://www.jasper.ai", "Genera copy, no opera un equipo con memoria."));
        competitors.add(competitor("HubSpot", "https://www.hubspot.com", "CRM amplio; no es un CMO autónomo para micro-SaaS."));
        node.set("competitors", competitors);
        node.put("icpSummary", "Comprador: founder técnico-comercial de " + (company.isBlank() ? "SaaS" : company) + " en España. Trigger: acaba de lanzar y el marketing sigue siendo ad-hoc.");
        node.set("segments", strings("micro-SaaS B2B", "agencias pequeñas", "pymes digitales"));
        return objectMapper.writeValueAsString(node);
    }

    private String content(String prompt) throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode ideas = objectMapper.createArrayNode();
        ideas.add(idea("Por qué el marketing con IA no puede ser un chat", "Contraste con copilots genéricos", "educación práctica"));
        ideas.add(idea("Cómo un CMO autónomo pide aprobación", "Confianza y control humano", "producto en público"));
        ideas.add(idea("El ICP de un founder que no puede contratar marketing", "Empatía y cualificación", "prueba social"));
        node.set("ideas", ideas);
        ObjectNode piece = objectMapper.createObjectNode();
        piece.put("title", "Mavora no es un chatbot: es un equipo con memoria");
        piece.put("body", "Si tu marketing vive en un hilo de ChatGPT, cada semana empiezas de cero. Un equipo autónomo observa, propone, espera tu sí y aprende de lo publicado.");
        piece.put("ideaTitle", "Por qué el marketing con IA no puede ser un chat");
        ArrayNode variants = objectMapper.createArrayNode();
        variants.add(variant("linkedin", "Deja de reiniciar tu estrategia cada lunes. Un CMO con memoria propone, tú apruebas, el sistema ejecuta."));
        variants.add(variant("blog", piece.get("body").asText()));
        piece.set("variants", variants);
        node.set("piece", piece);
        return objectMapper.writeValueAsString(node);
    }

    private String social() throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode publications = objectMapper.createArrayNode();
        publications.add(publication("linkedin", "Un equipo de marketing que recuerda lo que funcionó la semana pasada. Eso es Mavora."));
        publications.add(publication("x", "No más chat para “hacer marketing”. Observar → proponer → aprobar → medir."));
        node.set("publications", publications);
        return objectMapper.writeValueAsString(node);
    }

    private String businessImport(String prompt) throws Exception {
        String username = extract(prompt, "Username:");
        if (username.toLowerCase(java.util.Locale.ROOT).contains("vapewave")) {
            return vapewaveImport();
        }
        String name = extract(prompt, "Name:");
        String biography = extract(prompt, "Biography:");
        String website = extract(prompt, "Website:");
        String category = extract(prompt, "Category:");
        if (name.isBlank()) {
            name = username.replace("@", "").replace('.', ' ').trim();
        }
        if (name.isBlank()) {
            name = "Marca";
        }
        ObjectNode node = objectMapper.createObjectNode();
        node.put("companyName", name);
        node.put("websiteUrl", website);
        node.put("description", biography.isBlank()
                ? "Negocio activo en Instagram (" + username + ")."
                : biography);
        node.put("market", category.isBlank() ? "Audiencia de Instagram en español" : category);
        node.put("productName", name);
        node.put("productDescription", biography);
        node.put("voice", "Directa y clara, como el perfil de " + username);
        node.put("offer", biography.isBlank() ? name + " publica con una oferta visible en el perfil." : biography);
        node.put("cta", website.isBlank() ? "Escríbenos por DM." : "Enlace en la bio.");
        node.put("audience", "Personas que siguen o descubren " + username);
        node.put("extraNotes", "Rellenado desde el perfil de Instagram " + username + ". Revisar antes de publicar.");
        return objectMapper.writeValueAsString(node);
    }

    private String vapewaveImport() throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("companyName", VapeWaveProfile.DISPLAY_NAME);
        node.put("websiteUrl", "");
        node.put("description", VapeWaveProfile.ABOUT);
        node.put("market", VapeWaveProfile.MARKET);
        node.put("productName", VapeWaveProfile.PRODUCT_NAME);
        node.put("productDescription", VapeWaveProfile.PRODUCT_DESCRIPTION);
        node.put("voice", VapeWaveProfile.VOICE);
        node.put("offer", VapeWaveProfile.OFFER);
        node.put("cta", VapeWaveProfile.CTA);
        node.put("audience", VapeWaveProfile.AUDIENCE);
        node.put("extraNotes", VapeWaveProfile.EXTRA_NOTES);
        return objectMapper.writeValueAsString(node);
    }

    private String instagram(String prompt) throws Exception {
        if (prompt != null && prompt.toLowerCase(java.util.Locale.ROOT).contains("vapewave")) {
            return vapewaveCopies();
        }
        String company = extract(prompt, "Company:", "Empresa:");
        if (company.isBlank()) {
            company = "la marca";
        }
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode copies = objectMapper.createArrayNode();
        copies.add(igCopy("STORY", "¿Sigues improvisando el feed cada lunes?",
                company + " publica con un sistema: hook, oferta y un solo CTA.",
                "Responde QUIERO por DM y te mandamos la oferta."));
        copies.add(igCopy("FEED", "El algoritmo premia constancia, no milagros.",
                "Visitantes → seguidores → conversación → venta. " + company + " se posiciona con prueba social y una oferta clara.",
                "Enlace en la bio. Hoy."));
        copies.add(igCopy("REEL", "Para si tu marketing empieza de cero cada semana.",
                "En 15 segundos: el problema, la promesa de " + company + " y qué hacer ahora. Ritmo rápido, texto nativo, CTA de venta.",
                "Guarda y entra en el perfil."));
        copies.add(igCopy("CAROUSEL", "Guarda esto: de visita a cliente.",
                "1) Hook. 2) Dolor. 3) Oferta. 4) Prueba. 5) CTA. " + company + " usa este mapa para no perder el scroll.",
                "Compártelo con tu equipo y entra en la bio."));
        copies.add(igCopy("STORY", "Una pregunta para tu audiencia.",
                "Si esto te describe, " + company + " está hecho para ti.",
                "Reacciona o escribe DEMO."));
        copies.add(igCopy("REEL", "Lo que el feed no te cuenta del cierre.",
                "El alcance no paga facturas. " + company + " diseña cada pieza para una acción: seguir, guardar o comprar.",
                "Toca el enlace de la bio."));
        copies.add(igCopy("FEED", "Prueba social > promesa vacía.",
                company + " enseña el producto en público. Menos adjetivos, más resultado.",
                "Comenta YO y te enviamos el siguiente paso."));
        copies.add(igCopy("CAROUSEL", "3 errores que matan el reach.",
                "Texto tarde, CTA flojo, cero ritmo. " + company + " evita los tres con un calendario autónomo.",
                "Guarda la guía y visita el perfil."));
        node.set("copies", copies);
        return objectMapper.writeValueAsString(node);
    }

    private String vapewaveCopies() throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode copies = objectMapper.createArrayNode();
        copies.add(igCopyVape(
                "STORY",
                "10 sabores. Una ola. Valencia.",
                "Colección 60K de VapeWave. Pedidos por DM. Solo adultos 18+. Sin teatro, sin claims de salud: sabor y recambio.",
                "Escríbenos por DM. +18."
        ));
        copies.add(igCopyVape(
                "FEED",
                "THIS IS THE WAVE. THIS IS VAPEWAVE.",
                "Valencia. Colección 60K, diez sabores (strawberry ice, watermelon blast, triple grape, grape ice + kiwi). Pedidos por DM. Contenido para adultos.",
                "Pedidos por DM. Solo +18."
        ));
        copies.add(igCopyVape(
                "REEL",
                "No es un milagro. Es un sabor.",
                "VapeWave · Valencia. Enseña el dispositivo, el sabor, el CTA. Recargable USB-C según ficha. Pregunta precio y stock por DM.",
                "DM para pedir. +18."
        ));
        copies.add(igCopyVape(
                "CAROUSEL",
                "Guarda la carta de sabores.",
                "1) Colección 60K. 2) Diez sabores. 3) Pedidos por DM. 4) Solo +18. VapeWave no promete resultados de algoritmo ni beneficios de salud.",
                "Pide por DM el sabor que quieres."
        ));
        copies.add(igCopyVape(
                "STORY",
                "¿Strawberry ice o watermelon blast?",
                "Responde en este story y te decimos stock. VapeWave Valencia. Adultos 18+.",
                "Responde el sabor por DM."
        ));
        copies.add(igCopyVape(
                "REEL",
                "Triple grape. Vera VR22K.",
                "Lo que el perfil enseña: mesh coil, airflow, USB-C. Precio y envío: pregúntalo por DM, no lo inventamos en el copy.",
                "More info: DM. +18."
        ));
        copies.add(igCopyVape(
                "FEED",
                "Pedidos por DM. Punto.",
                "VapeWave no es un anuncio genérico de vapeo: es una cuenta de Valencia con colección 60K. Si eres mayor de edad y quieres sabor, el siguiente paso es un mensaje.",
                "DM con el sabor. Solo +18."
        ));
        copies.add(igCopyVape(
                "CAROUSEL",
                "Cómo pedir en VapeWave.",
                "1) Elige sabor. 2) Escríbenos por DM. 3) Confirmamos stock y precio. 4) Solo 18+. La oferta 2x30 € de algunos posts hay que confirmarla.",
                "Empieza el DM con el sabor."
        ));
        node.set("copies", copies);
        return objectMapper.writeValueAsString(node);
    }

    private ObjectNode igCopy(String format, String hook, String caption, String cta) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("format", format);
        node.put("hook", hook);
        node.put("caption", caption);
        node.put("cta", cta);
        node.set("hashtags", strings("#pymes", "#marketing", "#instagram", "#ventas"));
        node.put("visualPrompt", "Clean Instagram visual for " + format + ", product-led, no watermark, high contrast.");
        return node;
    }

    private ObjectNode igCopyVape(String format, String hook, String caption, String cta) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("format", format);
        node.put("hook", hook);
        node.put("caption", caption);
        node.put("cta", cta);
        node.set("hashtags", strings("#VapeWave", "#VapeWaveVLC", "#Valencia", "#vapeo"));
        node.put(
                "visualPrompt",
                "Adult-only product photo of a disposable vape, dark neon wave aesthetic, Valencia night, "
                        + "no minors, no cartoons, no health claims, no watermark, " + format + " composition."
        );
        return node;
    }

    private String analytics() throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("insightTitle", "El canal con mejor señal es LinkedIn");
        node.put("insightBody", "Los snapshots muestran más tracción relativa en LinkedIn que en el resto. Conviene doblar el pilar de educación práctica ahí.");
        node.put("learningTitle", "Priorizar LinkedIn tras la primera medición");
        node.put("learningBody", "Cuando hay pocos datos, concentrar el ritmo editorial en un canal reduce el ruido y acelera el aprendizaje.");
        return objectMapper.writeValueAsString(node);
    }

    private ObjectNode persona(String name, String summary, String pains, String jobs) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("summary", summary);
        node.put("pains", pains);
        node.put("jobs", jobs);
        return node;
    }

    private ObjectNode competitor(String name, String url, String notes) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("url", url);
        node.put("notes", notes);
        return node;
    }

    private ObjectNode idea(String title, String angle, String pillar) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("angle", angle);
        node.put("pillar", pillar);
        return node;
    }

    private ObjectNode variant(String channel, String body) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("channel", channel);
        node.put("body", body);
        return node;
    }

    private ObjectNode publication(String channel, String copy) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("channel", channel);
        node.put("copy", copy);
        return node;
    }

    private ArrayNode strings(String... values) {
        ArrayNode array = objectMapper.createArrayNode();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private static String extract(String prompt, String... labels) {
        if (prompt == null) {
            return "";
        }
        for (String label : labels) {
            int index = prompt.indexOf(label);
            if (index >= 0) {
                int from = index + label.length();
                int nl = prompt.indexOf('\n', from);
                String value = (nl < 0 ? prompt.substring(from) : prompt.substring(from, nl)).trim();
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return "";
    }
}
