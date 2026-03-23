package com.aistocks.data.fmp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Minimal FMP client aligned with Python {@code FMPClient}: base URL, {@code apikey} query param,
 * same stable endpoints for smoke tests.
 */
public class FmpClient {

    public static final String BASE_URL = "https://financialmodelingprep.com/stable";

    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path cacheDir;
    private final boolean useCache;
    private final Duration cacheTtl;

    public FmpClient(String apiKey, Path cacheDir, boolean useCache, Duration cacheTtlHours) {
        this.apiKey = apiKey;
        this.cacheDir = cacheDir;
        this.useCache = useCache;
        this.cacheTtl = cacheTtlHours;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new FmpException("Cannot create cache dir: " + cacheDir, e);
        }
    }

    public JsonNode getQuote(String symbol) throws IOException, InterruptedException {
        return request("quote", Map.of("symbol", symbol), false);
    }

    public JsonNode getHistoricalPriceEodFull(String symbol, String from, String to)
            throws IOException, InterruptedException {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("symbol", symbol);
        if (from != null) {
            p.put("from", from);
        }
        if (to != null) {
            p.put("to", to);
        }
        return request("historical-price-eod/full", p, true);
    }

    private JsonNode request(String endpoint, Map<String, String> params, boolean cacheable)
            throws IOException, InterruptedException {
        Map<String, String> qp = new LinkedHashMap<>(params);
        qp.put("apikey", apiKey);

        if (useCache && cacheable) {
            Path cacheFile = cachePath(endpoint, qp);
            if (Files.isRegularFile(cacheFile)) {
                Instant mtime = Files.getLastModifiedTime(cacheFile).toInstant();
                if (Instant.now().isBefore(mtime.plus(cacheTtl))) {
                    return mapper.readTree(Files.readString(cacheFile));
                }
            }
        }

        String q = qp.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(Collectors.joining("&"));
        URI uri = URI.create(BASE_URL + "/" + endpoint + "?" + q);
        HttpRequest req = HttpRequest.newBuilder(uri).GET().timeout(Duration.ofSeconds(45)).build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 401) {
            throw new FmpException("Invalid FMP API key (401)");
        }
        if (resp.statusCode() == 429) {
            throw new FmpException("FMP rate limit (429)");
        }
        if (resp.statusCode() != 200) {
            throw new FmpException("FMP HTTP " + resp.statusCode() + ": " + resp.body().substring(0, Math.min(200, resp.body().length())));
        }
        JsonNode node = mapper.readTree(resp.body());
        if (node.isObject() && node.has("Error Message")) {
            throw new FmpException(node.get("Error Message").asText());
        }
        if (useCache && cacheable) {
            Files.writeString(cachePath(endpoint, qp), mapper.writeValueAsString(node));
        }
        return node;
    }

    private Path cachePath(String endpoint, Map<String, String> params) {
        StringJoiner sj = new StringJoiner("_");
        params.entrySet().stream()
                .filter(e -> !"apikey".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sj.add(e.getKey() + "=" + e.getValue()));
        String name = endpoint.replace('/', '_').replace('-', '_') + "_" + sj + ".json";
        return cacheDir.resolve(name);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
