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
                    case ANALYST -> analytics(request.userPrompt());
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
                "10 sabores. 15 €. Valencia.",
                "Colección 60K de VapeWave. Pedidos por DM o WhatsApp. Solo adultos 18+. Sin tienda física, sin claims de salud.",
                "DM o WhatsApp. 15 €. +18."
        ));
        copies.add(igCopyVape(
                "FEED",
                "THIS IS THE WAVE. THIS IS VAPEWAVE.",
                "Valencia. Solo colección 60K, diez sabores, 15 €/ud. Pedidos por DM o WhatsApp. Síguenos si eres +18: medimos alcance y seguidores, no milagros de algoritmo.",
                "Pedidos por DM o WhatsApp. 15 €. Solo +18."
        ));
        copies.add(igCopyVape(
                "REEL",
                "No es un milagro. Es un sabor a 15 €.",
                "VapeWave · Valencia. Reel para que te encuentren (alcance). El pedido va por DM o WhatsApp. Recargable USB-C según ficha. Solo 60K.",
                "DM o WhatsApp. +18."
        ));
        copies.add(igCopyVape(
                "CAROUSEL",
                "Guarda la carta: 15 €.",
                "1) Solo colección 60K. 2) Diez sabores. 3) 15 €/ud. 4) DM o WhatsApp. 5) Solo +18. Sin envío prometido. Sin claims de salud.",
                "Pide el sabor por DM o WhatsApp."
        ));
        copies.add(igCopyVape(
                "STORY",
                "¿Strawberry ice o watermelon blast?",
                "15 €. Responde y te decimos stock. VapeWave Valencia. Adultos 18+.",
                "Sabor por DM o WhatsApp."
        ));
        copies.add(igCopyVape(
                "REEL",
                "Triple grape. Vera VR22K.",
                "Mesh coil, airflow, USB-C según el post. 15 €. Alcance sí, promesa de algoritmo no.",
                "More info: DM o WhatsApp. +18."
        ));
        copies.add(igCopyVape(
                "FEED",
                "15 €. DM o WhatsApp.",
                "VapeWave no tiene tienda a pie de calle. Solo 60K. Si eres mayor de edad, el siguiente paso es un mensaje. Síguenos para no perder los sabores.",
                "DM o WhatsApp. 15 €. Solo +18."
        ));
        copies.add(igCopyVape(
                "CAROUSEL",
                "Cómo pedir en VapeWave.",
                "1) Elige sabor. 2) Escríbenos por DM o WhatsApp. 3) 15 €/ud. 4) Solo 18+. 5) Solo colección 60K.",
                "Empieza el mensaje por DM o WhatsApp con el sabor."
        ));
        copies.add(igCopyVape(
                "STORY",
                "Colección 60K. Diez sabores.",
                "Si eres +18, el pedido es por DM o WhatsApp. 15 €. VapeWave no publica para menores.",
                "DM o WhatsApp. +18."
        ));
        copies.add(igCopyVape(
                "STORY",
                "Grape ice + kiwi. 15 €.",
                "Sabor de la 60K. Stock por mensaje. No hay otra línea de producto.",
                "Escribe el sabor por DM o WhatsApp."
        ));
        copies.add(igCopyVape(
                "STORY",
                "THIS IS THE WAVE.",
                "VapeWave · VLC. 15 €. Pedidos por DM o WhatsApp. Adultos 18+. Síguenos.",
                "Pide por DM o WhatsApp."
        ));
        copies.add(igCopyVape(
                "STORY",
                "¿Repites sabor o pruebas otro?",
                "Strawberry ice, watermelon blast, triple grape. 15 €. Solo +18.",
                "Responde el sabor por DM o WhatsApp."
        ));
        copies.add(igCopyVape(
                "STORY",
                "Valencia. Sin tienda física.",
                "El canal de venta es DM o WhatsApp. 15 € la 60K.",
                "Abre DM o WhatsApp. +18."
        ));
        copies.add(igCopyVape(
                "STORY",
                "Solo adultos.",
                "Si no tienes 18, esto no es para ti. Si sí: 60K, diez sabores, 15 €, VapeWave.",
                "DM o WhatsApp si eres +18."
        ));
        copies.add(igCopyVape(
                "FEED",
                "Diez sabores. Una cuenta.",
                "Colección 60K. 15 €. Valencia. Pedidos por DM o WhatsApp. El feed es para que nos sigas; el reel, para alcance. Sin cifras inventadas.",
                "Pedidos por DM o WhatsApp. 15 €. Solo +18."
        ));
        copies.add(igCopyVape(
                "REEL",
                "USB-C. Mesh. 15 €.",
                "Ficha del post, no un claim de salud. VapeWave Valencia. Solo 60K.",
                "DM o WhatsApp. +18."
        ));
        copies.add(igCopyVape(
                "REEL",
                "Una misma ola. 60K.",
                "Dispositivo, sabor, precio 15 €, CTA. Medimos alcance y seguidores. No prometemos el algoritmo.",
                "Pide el sabor por DM o WhatsApp."
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

    private String analytics(String prompt) throws Exception {
        String lower = prompt == null ? "" : prompt.toLowerCase(java.util.Locale.ROOT);
        boolean instagramKpis = lower.contains("alcance")
                || lower.contains("seguidores")
                || lower.contains("reach=")
                || lower.contains("followers=");
        ObjectNode node = objectMapper.createObjectNode();
        if (instagramKpis) {
            node.put("insightTitle", "Alcance y seguidores son las dos señales");
            node.put(
                    "insightBody",
                    "Los snapshots de alcance y seguidores son la única evidencia. No se inventan cifras ni se promete el algoritmo. Relacionar reels con alcance y el ritmo de feed/historias con seguidores."
            );
            node.put("learningTitle", "Medir las dos métricas cada semana");
            node.put(
                    "learningBody",
                    "Registrar alcance y seguidores a la vez evita optimizar solo una. El pedido sigue siendo DM o WhatsApp: el KPI de venta no está en el snapshot hasta que se anote."
            );
        } else {
            node.put("insightTitle", "El canal con mejor señal es LinkedIn");
            node.put("insightBody", "Los snapshots muestran más tracción relativa en LinkedIn que en el resto. Conviene doblar el pilar de educación práctica ahí.");
            node.put("learningTitle", "Priorizar LinkedIn tras la primera medición");
            node.put("learningBody", "Cuando hay pocos datos, concentrar el ritmo editorial en un canal reduce el ruido y acelera el aprendizaje.");
        }
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
