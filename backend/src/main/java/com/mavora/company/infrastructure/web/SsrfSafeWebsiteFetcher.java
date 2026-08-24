package com.mavora.company.infrastructure.web;

import com.mavora.company.application.FetchedPage;
import com.mavora.company.application.WebsiteFetcher;
import com.mavora.company.domain.HttpUrl;
import com.mavora.shared.domain.DomainException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SsrfSafeWebsiteFetcher implements WebsiteFetcher {

    private static final int MAX_REDIRECTS = 3;
    private final HttpClient httpClient;
    private final int maxBytes;
    private final Duration timeout;

    public SsrfSafeWebsiteFetcher(
            @Value("${mavora.website.timeout:8s}") Duration timeout,
            @Value("${mavora.website.max-bytes:524288}") int maxBytes
    ) {
        this.timeout = timeout;
        this.maxBytes = maxBytes;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(timeout)
                .build();
    }

    @Override
    public FetchedPage fetch(String rawUrl) {
        String current = HttpUrl.normalize(rawUrl);
        try {
            for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
                URI uri = URI.create(current);
                assertPublicHttp(uri);
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(timeout)
                        .header("User-Agent", "MavoraWebsiteIngest/0.1")
                        .GET()
                        .build();
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                int status = response.statusCode();
                if (status >= 300 && status < 400) {
                    String location = response.headers().firstValue("Location").orElse(null);
                    if (location == null || hop == MAX_REDIRECTS) {
                        throw new DomainException("Website fetch failed: too many redirects");
                    }
                    current = uri.resolve(location).toString();
                    continue;
                }
                if (status < 200 || status >= 300) {
                    throw new DomainException("Website fetch failed with HTTP " + status);
                }
                byte[] body = response.body() == null ? new byte[0] : response.body();
                if (body.length > maxBytes) {
                    throw new DomainException("Website is larger than the allowed size");
                }
                String html = new String(body, StandardCharsets.UTF_8);
                return new FetchedPage(current, extractTitle(html), extractText(html));
            }
            throw new DomainException("Website fetch failed: too many redirects");
        } catch (DomainException exception) {
            throw exception;
        } catch (IOException | InterruptedException | IllegalArgumentException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new DomainException("Website could not be fetched");
        }
    }

    static void assertPublicHttp(URI uri) {
        String normalized = HttpUrl.normalize(uri.toString());
        URI safe = URI.create(normalized);
        String host = safe.getHost();
        if (host == null) {
            throw new DomainException("Website URL is invalid");
        }
        String lower = host.toLowerCase(Locale.ROOT);
        if (lower.equals("localhost") || lower.endsWith(".localhost") || lower.endsWith(".internal")
                || lower.endsWith(".local") || lower.equals("metadata.google.internal")) {
            throw new DomainException("Website URL is not allowed");
        }
        int port = safe.getPort();
        if (port != -1 && port != 80 && port != 443) {
            throw new DomainException("Website URL port is not allowed");
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (PrivateNetworkAddresses.isBlocked(address)) {
                    throw new DomainException("Website URL is not allowed");
                }
            }
        } catch (java.net.UnknownHostException exception) {
            throw new DomainException("Website host could not be resolved");
        }
    }

    static String extractTitle(String html) {
        var matcher = java.util.regex.Pattern.compile("(?is)<title[^>]*>(.*?)</title>").matcher(html);
        if (matcher.find()) {
            String title = stripTags(matcher.group(1)).trim();
            return title.length() > 200 ? title.substring(0, 200) : title;
        }
        return "Website";
    }

    static String extractText(String html) {
        String withoutScripts = html.replaceAll("(?is)<script[^>]*>.*?</script>", " ");
        String withoutStyles = withoutScripts.replaceAll("(?is)<style[^>]*>.*?</style>", " ");
        String text = stripTags(withoutStyles)
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.isBlank()) {
            throw new DomainException("Website did not contain readable text");
        }
        return text.length() > 8000 ? text.substring(0, 8000) : text;
    }

    private static String stripTags(String value) {
        return value.replaceAll("(?is)<[^>]+>", " ");
    }
}
