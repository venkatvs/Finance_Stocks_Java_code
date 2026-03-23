package com.aistocks.universe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

/**
 * Mirrors {@code src/universe/ai_stocks.py} using bundled {@code ai_universe.json}
 * (generated from the Python source).
 */
public final class AiUniverseRegistry {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();

    static {
        try (InputStream in = AiUniverseRegistry.class.getResourceAsStream("/ai_universe.json")) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource ai_universe.json");
            }
            JsonNode root = MAPPER.readTree(in);
            JsonNode cats = root.get("categories");
            Iterator<String> fn = cats.fieldNames();
            while (fn.hasNext()) {
                String k = fn.next();
                List<String> tickers = new ArrayList<>();
                for (JsonNode t : cats.get(k)) {
                    tickers.add(t.asText());
                }
                CATEGORIES.put(k, List.copyOf(tickers));
            }
            JsonNode desc = root.get("categoryDescriptions");
            if (desc != null) {
                Iterator<String> dfn = desc.fieldNames();
                while (dfn.hasNext()) {
                    String k = dfn.next();
                    DESCRIPTIONS.put(k, desc.get(k).asText());
                }
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AiUniverseRegistry() {}

    public static Map<String, List<String>> categories() {
        return Collections.unmodifiableMap(CATEGORIES);
    }

    public static Map<String, String> categoryDescriptions() {
        return Collections.unmodifiableMap(DESCRIPTIONS);
    }

    public static List<String> categoryNames() {
        return List.copyOf(CATEGORIES.keySet());
    }

    public static List<String> allTickersSorted() {
        TreeSet<String> u = new TreeSet<>();
        for (List<String> ts : CATEGORIES.values()) {
            u.addAll(ts);
        }
        return List.copyOf(u);
    }

    public static List<String> tickersForCategories(Collection<String> categories) {
        TreeSet<String> u = new TreeSet<>();
        for (String c : categories) {
            List<String> ts = CATEGORIES.get(c);
            if (ts == null) {
                throw new IllegalArgumentException("Unknown category: " + c);
            }
            u.addAll(ts);
        }
        return List.copyOf(u);
    }

    public static Optional<String> categoryForTicker(String ticker) {
        for (var e : CATEGORIES.entrySet()) {
            if (e.getValue().contains(ticker)) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }
}
