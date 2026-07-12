package com.surplusfood.marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surplusfood.marketplace.dto.ChatbotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKeyEnv;

    public ChatbotResponse extractInformation(String description) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = geminiApiKeyEnv;
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return callGeminiApi(description, apiKey.trim());
            } catch (Exception e) {
                log.error("Error calling Gemini API, falling back to local extractor", e);
            }
        } else {
            log.info("No GEMINI_API_KEY configured. Using local fallback extractor.");
        }

        return extractLocally(description);
    }

    private ChatbotResponse callGeminiApi(String description, String apiKey) throws Exception {
        String systemInstruction = "You are an AI Food Donation Assistant for a Surplus Food Marketplace. "
                + "Your task is to read a food donation description written by a business owner and extract structured information. "
                + "Extract the following fields:\n"
                + "- Food Name\n"
                + "- Food Category\n"
                + "- Quantity (with unit)\n"
                + "- Preparation Time\n"
                + "- Expiry/Pickup Deadline (if mentioned)\n"
                + "- Storage Type (Room Temperature/Refrigerated/Frozen)\n"
                + "- Vegetarian or Non-Vegetarian\n"
                + "- Packaging Status (Packed/Not Packed)\n"
                + "- Special Instructions\n"
                + "- Confidence Score (0-100)\n\n"
                + "Rules:\n"
                + "1. Do not invent information.\n"
                + "2. If a field is not mentioned, return \"Not Provided\".\n"
                + "3. Return only valid JSON.\n"
                + "4. Keep extracted values short and accurate.\n"
                + "5. Identify food names even if written informally.\n"
                + "6. Understand natural language, abbreviations, and spelling mistakes.\n\n"
                + "Output format:\n"
                + "{\n"
                + "  \"foodName\": \"\",\n"
                + "  \"foodCategory\": \"\",\n"
                + "  \"quantity\": \"\",\n"
                + "  \"preparationTime\": \"\",\n"
                + "  \"pickupDeadline\": \"\",\n"
                + "  \"storageType\": \"\",\n"
                + "  \"foodType\": \"\",\n"
                + "  \"packagingStatus\": \"\",\n"
                + "  \"specialInstructions\": \"\",\n"
                + "  \"confidence\": \"\"\n"
                + "}";

        String prompt = systemInstruction + "\n\nDescription to analyze:\n\"" + description + "\"";

        // Construct Request Body
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> parts = Map.of("parts", java.util.List.of(textPart));
        Map<String, Object> contents = Map.of("contents", java.util.List.of(parts));
        
        Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");
        
        Map<String, Object> requestBody = new HashMap<>(contents);
        requestBody.put("generationConfig", generationConfig);

        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API call failed with status code " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String responseText = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

        if (responseText == null || responseText.trim().isEmpty()) {
            throw new RuntimeException("Empty response received from Gemini");
        }

        // Deserialize response JSON back into ChatbotResponse
        return objectMapper.readValue(responseText.trim(), ChatbotResponse.class);
    }

    private ChatbotResponse extractLocally(String description) {
        String descLower = description.toLowerCase();

        // 1. Food Name Extraction
        String foodName = "Not Provided";
        
        Pattern ofPattern = Pattern.compile("(?i)\\b(?:portions|kg|g|lbs|pieces|pcs|trays|boxes|items|units|servings|plates|cups|bags|loaves|liters|l|ml)\\s+of\\s+([a-zA-Z\\s]+?)(?=\\s+(?:cooked|prepared|made|baked|stored|refrigerated|kept|in|best|before|at|room|temp|veg|non|packed|loose|need|pick|expires|expire|expiry|by|until|today|tomorrow|around|\\b\\d)|,|\\.|;|$)");
        Matcher ofMatcher = ofPattern.matcher(description);
        
        if (ofMatcher.find()) {
            foodName = ofMatcher.group(1).trim();
        } else {
            Pattern havePattern = Pattern.compile("(?i)\\b(?:have|got|has|donate|donating|surplus)\\s+([a-zA-Z\\s]+?)(?=\\s+(?:cooked|prepared|made|baked|stored|refrigerated|kept|in|best|before|at|room|temp|veg|non|packed|loose|need|pick|expires|expire|expiry|by|until|today|tomorrow|around|\\b\\d)|,|\\.|;|$)");
            Matcher haveMatcher = havePattern.matcher(description);
            if (haveMatcher.find()) {
                foodName = haveMatcher.group(1).trim();
            } else {
                String cleanedDesc = description.replaceAll("(?i)\\b(\\d+\\s*(?:kg|g|lbs|pieces|pcs|portions|trays|boxes|items|units|servings|plates|cups|bags|loaves|liters|l|ml))\\b", "").trim();
                cleanedDesc = cleanedDesc.replaceAll("(?i)^\\b(?:we|i|have|got|there|is|are|a|some|of)\\b\\s*", "");
                String[] words = cleanedDesc.split("\\s+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    int length = Math.min(3, words.length);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < length; i++) {
                        String w = words[i].replaceAll("[^a-zA-Z]", "");
                        if (!w.isEmpty()) {
                            sb.append(w).append(" ");
                        }
                    }
                    String est = sb.toString().trim();
                    if (!est.isEmpty()) {
                        foodName = est;
                    }
                }
            }
        }

        // 2. Category Extraction
        String foodCategory = "Prepared Meals"; // default fallback
        if (containsAny(descLower, "bread", "cake", "pastry", "cookie", "muffin", "bagel", "croissant", "bakery", "doughnut", "bun")) {
            foodCategory = "Bakery";
        } else if (containsAny(descLower, "milk", "cheese", "butter", "yogurt", "paneer", "dairy", "cream")) {
            foodCategory = "Dairy";
        } else if (containsAny(descLower, "apple", "banana", "tomato", "vegetable", "fruit", "lettuce", "onion", "potato", "produce", "salad", "carrot", "orange", "lemon")) {
            foodCategory = "Produce";
        } else if (containsAny(descLower, "juice", "soda", "coke", "tea", "coffee", "drink", "beverage", "water", "smoothie")) {
            foodCategory = "Beverages";
        } else if (containsAny(descLower, "grocery", "groceries", "flour", "rice bag", "lentils", "pasta box", "oil", "can", "canned", "sugar", "salt")) {
            foodCategory = "Groceries";
        } else if (containsAny(descLower, "rice", "pasta", "curry", "biryani", "soup", "pizza", "burger", "meal", "cooked", "chicken", "meat", "beef", "pork", "fish", "gravy")) {
            foodCategory = "Prepared Meals";
        }

        // 3. Quantity Extraction
        String quantity = "Not Provided";
        Pattern qtyPattern = Pattern.compile("(?i)\\b(\\d+\\s*(?:kg|g|lbs|pieces|pcs|portions|trays|boxes|items|units|servings|plates|cups|bags|loaves|liters|l|ml))\\b");
        Matcher qtyMatcher = qtyPattern.matcher(description);
        if (qtyMatcher.find()) {
            quantity = qtyMatcher.group(1);
        } else {
            // Check for isolated numbers
            Pattern numPattern = Pattern.compile("\\b(\\d+)\\b");
            Matcher numMatcher = numPattern.matcher(description);
            if (numMatcher.find()) {
                quantity = numMatcher.group(1) + " items";
            }
        }

        // 4. Preparation Time
        String preparationTime = "Not Provided";
        Pattern prepPattern = Pattern.compile("(?i)\\b(cooked|prepared|made|baked)\\s+(?:around\\s+)?([^,.]+)\\b");
        Matcher prepMatcher = prepPattern.matcher(description);
        if (prepMatcher.find()) {
            preparationTime = prepMatcher.group(0);
        }

        // 5. Expiry/Pickup Deadline
        String pickupDeadline = "Not Provided";
        Pattern deadlinePattern = Pattern.compile("(?i)\\b(?:best before|expiry|expire|expires|pickup by|before|by|until)\\s+([^,.]+)\\b");
        Matcher deadlineMatcher = deadlinePattern.matcher(description);
        if (deadlineMatcher.find()) {
            pickupDeadline = deadlineMatcher.group(0);
        }

        // 6. Storage Type
        String storageType = "Room Temperature"; // default
        if (containsAny(descLower, "refrigerated", "fridge", "cold", "refrigerate", "keep cold")) {
            storageType = "Refrigerated";
        } else if (containsAny(descLower, "frozen", "freezer", "freeze", "keep frozen")) {
            storageType = "Frozen";
        } else if (containsAny(descLower, "room temperature", "ambient", "room temp", "shelf", "cupboard")) {
            storageType = "Room Temperature";
        }

        // 7. Vegetarian or Non-Vegetarian
        String foodType = "Vegetarian"; // default
        if (containsAny(descLower, "non-veg", "non-vegetarian", "chicken", "meat", "beef", "pork", "fish", "mutton", "egg", "seafood")) {
            foodType = "Non-Vegetarian";
        } else if (containsAny(descLower, "veg", "vegetarian", "vegan", "plant-based")) {
            foodType = "Vegetarian";
        }

        // 8. Packaging Status
        String packagingStatus = "Not Provided";
        if (containsAny(descLower, "unpacked", "not packed", "loose", "open")) {
            packagingStatus = "Not Packed";
        } else if (containsAny(descLower, "packed", "packaged", "container", "box", "boxed", "pack", "bagged", "sealed")) {
            packagingStatus = "Packed";
        }

        // 9. Special Instructions
        String specialInstructions = "Not Provided";
        Pattern instrPattern = Pattern.compile("(?i)\\b(?:note|instructions|details|please|warning):?\\s+([^,.]+)\\b");
        Matcher instrMatcher = instrPattern.matcher(description);
        if (instrMatcher.find()) {
            specialInstructions = instrMatcher.group(1);
        }

        // 10. Confidence
        String confidence = "65"; // lower confidence for heuristic extraction

        return new ChatbotResponse(
                foodName,
                foodCategory,
                quantity,
                preparationTime,
                pickupDeadline,
                storageType,
                foodType,
                packagingStatus,
                specialInstructions,
                confidence
        );
    }

    private boolean containsAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
