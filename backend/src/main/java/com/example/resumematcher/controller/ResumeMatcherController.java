package com.example.resumematcher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Enable CORS for development
public class ResumeMatcherController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/analyze", consumes = {"multipart/form-data"})
    public ResponseEntity<?> analyzeResume(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription) {

        // 1. Validate inputs
        if (resume == null || resume.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Resume file is missing or empty."));
        }
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job description is missing or empty."));
        }

        // Validate content type
        String contentType = resume.getContentType();
        boolean isPdf = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        if (!isPdf) {
            String filename = resume.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Only PDF resumes are supported."));
            }
        }

        // 2. Read Gemini API Key
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Gemini API Key is not configured on the server. Please set the GEMINI_API_KEY environment variable."
            ));
        }

        // 3. Extract text from PDF
        String resumeText;
        try (PDDocument document = Loader.loadPDF(resume.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            resumeText = stripper.getText(document);
            if (resumeText == null || resumeText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Could not extract text from the resume PDF. Ensure it contains selectable text, not scanned images."));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to parse PDF resume: " + e.getMessage()));
        }

        // 4. Send request to Gemini API
        try {
            String geminiResponse = callGeminiAPI(apiKey, resumeText, jobDescription);
            String parsedResult = extractJsonFromGeminiResponse(geminiResponse);
            return ResponseEntity.ok().header("Content-Type", "application/json").body(parsedResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze resume with Gemini API: " + e.getMessage()));
        }
    }

    private String callGeminiAPI(String apiKey, String resumeText, String jobDescription) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

        // System prompt to instruct Gemini on format and persona
        String systemInstruction = "You are an expert recruiter and ATS evaluator. Analyze the user's resume text against the job description. " +
                "Evaluate matching elements and identify missing keywords/skills, strengths, and suggestions.\n\n" +
                "You MUST respond ONLY with a single JSON object. Do not include markdown codeblocks (```json), comments, or introductory/concluding text.\n" +
                "The JSON must have this exact shape:\n" +
                "{\n" +
                "  \"matchScore\": <integer between 0 and 100>,\n" +
                "  \"missingKeywords\": [<string>],\n" +
                "  \"strengths\": [<string>],\n" +
                "  \"suggestions\": [<string>]\n" +
                "}";

        String userContent = "Here is my resume text:\n" +
                "=== START RESUME ===\n" +
                resumeText + "\n" +
                "=== END RESUME ===\n\n" +
                "Here is the job description:\n" +
                "=== START JOB DESCRIPTION ===\n" +
                jobDescription + "\n" +
                "=== END JOB DESCRIPTION ===\n";

        // Construct payload maps for Jackson serialization
        Map<String, Object> systemInstructionMap = Map.of(
                "parts", java.util.List.of(Map.of("text", systemInstruction))
        );

        Map<String, Object> contentMap = Map.of(
                "parts", java.util.List.of(Map.of("text", userContent))
        );

        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json"
        );

        Map<String, Object> payloadMap = Map.of(
                "systemInstruction", systemInstructionMap,
                "contents", java.util.List.of(contentMap),
                "generationConfig", generationConfig
        );

        String payload = objectMapper.writeValueAsString(payloadMap);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API returned status " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    private String extractJsonFromGeminiResponse(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode candidatesNode = rootNode.path("candidates");

        if (candidatesNode.isArray() && candidatesNode.size() > 0) {
            JsonNode partsNode = candidatesNode.get(0).path("content").path("parts");
            if (partsNode.isArray() && partsNode.size() > 0) {
                String text = partsNode.get(0).path("text").asText();
                return cleanJsonText(text);
            }
        }
        throw new RuntimeException("Invalid response structure from Gemini API (missing content parts text).");
    }

    private String cleanJsonText(String text) {
        if (text == null) {
            return "{}";
        }
        text = text.trim();

        // Strip ```json or ``` code block markers if present
        if (text.startsWith("```")) {
            int firstNewLine = text.indexOf('\n');
            if (firstNewLine != -1) {
                text = text.substring(firstNewLine + 1);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();
        }

        // Find the outer JSON object brackets to strip any surrounding explanation text
        int startBracket = text.indexOf('{');
        int endBracket = text.lastIndexOf('}');
        if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
            text = text.substring(startBracket, endBracket + 1);
        }

        return text;
    }
}
