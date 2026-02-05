package org.unreal.agent.skill.example;

import org.springframework.stereotype.Component;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Example skill implementation that performs simple text analysis.
 * This skill can analyze text for word count, character count, and basic sentiment.
 */
@Component
public class TextAnalysisSkill implements AgentSkill {
    
    @Override
    public String getName() {
        return "text-analysis";
    }
    
    @Override
    public String getDescription() {
        return "Analyzes text for word count, character count, and basic sentiment analysis";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean canHandle(String request) {
        if (request == null) {
            return false;
        }
        String lowerRequest = request.toLowerCase();
        return lowerRequest.contains("analyze") || 
               lowerRequest.contains("count") || 
               lowerRequest.contains("word count") ||
               lowerRequest.contains("character count") ||
               lowerRequest.contains("sentiment");
    }
    
    @Override
    public AgentSkillResult execute(String request, Map<String, Object> parameters) {
        try {
            // Extract text from request or parameters
            String textToAnalyze = extractTextToAnalyze(request, parameters);
            
            if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
                return AgentSkillResult.failure()
                        .message("No text provided for analysis")
                        .skillName(getName())
                        .build();
            }
            
            // Perform analysis
            Map<String, Object> analysisResult = performAnalysis(textToAnalyze);
            
            return AgentSkillResult.success()
                    .message("Text analysis completed successfully")
                    .data(analysisResult)
                    .skillName(getName())
                    .metadata(Map.of("textLength", textToAnalyze.length()))
                    .build();
                    
        } catch (Exception e) {
            return AgentSkillResult.failure()
                    .message("Failed to analyze text: " + e.getMessage())
                    .skillName(getName())
                    .build();
        }
    }
    
    @Override
    public Map<String, String> getRequiredParameters() {
        Map<String, String> required = new HashMap<>();
        required.put("text", "The text to analyze");
        return required;
    }
    
    @Override
    public Map<String, String> getOptionalParameters() {
        Map<String, String> optional = new HashMap<>();
        optional.put("analysisType", "Type of analysis: word_count, char_count, sentiment, or all");
        optional.put("includeDetails", "Whether to include detailed analysis results (true/false)");
        return optional;
    }
    
    /**
     * Extract text to analyze from request or parameters.
     */
    private String extractTextToAnalyze(String request, Map<String, Object> parameters) {
        // First try to get text from parameters
        if (parameters != null && parameters.containsKey("text")) {
            return parameters.get("text").toString();
        }
        
        // If not in parameters, try to extract from request
        if (request != null) {
            // Simple extraction: look for text in quotes after keywords
            String lowerRequest = request.toLowerCase();
            String[] keywords = {"analyze", "count", "text:", "\""};
            
            for (String keyword : keywords) {
                int index = lowerRequest.indexOf(keyword);
                if (index != -1) {
                    String afterKeyword = request.substring(index + keyword.length());
                    // Remove quotes and trim
                    String text = afterKeyword.replace("\"", "").replace("'", "").trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Perform the actual text analysis.
     */
    private Map<String, Object> performAnalysis(String text) {
        Map<String, Object> result = new HashMap<>();
        
        // Basic counts
        int wordCount = countWords(text);
        int charCount = text.length();
        int charCountNoSpaces = text.replaceAll("\\s", "").length();
        int sentenceCount = countSentences(text);
        
        result.put("wordCount", wordCount);
        result.put("characterCount", charCount);
        result.put("characterCountNoSpaces", charCountNoSpaces);
        result.put("sentenceCount", sentenceCount);
        
        // Basic sentiment analysis (simplified)
        String sentiment = analyzeSentiment(text);
        result.put("sentiment", sentiment);
        
        // Additional metrics
        result.put("averageWordsPerSentence", sentenceCount > 0 ? (double) wordCount / sentenceCount : 0);
        result.put("averageWordLength", wordCount > 0 ? (double) charCountNoSpaces / wordCount : 0);
        
        return result;
    }
    
    /**
     * Count words in text.
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
    
    /**
     * Count sentences in text.
     */
    private int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // Simple sentence detection by punctuation
        String[] sentences = text.split("[.!?]+");
        int count = 0;
        for (String sentence : sentences) {
            if (!sentence.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Simple sentiment analysis.
     */
    private String analyzeSentiment(String text) {
        String lowerText = text.toLowerCase();
        
        // Simple positive and negative word lists
        String[] positiveWords = {"good", "great", "excellent", "amazing", "wonderful", "fantastic", 
                                 "happy", "joy", "love", "best", "awesome", "perfect", "nice"};
        String[] negativeWords = {"bad", "terrible", "awful", "horrible", "worst", "hate", 
                                 "sad", "angry", "poor", "disappointed", "negative", "ugly"};
        
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : positiveWords) {
            if (lowerText.contains(word)) {
                positiveCount++;
            }
        }
        
        for (String word : negativeWords) {
            if (lowerText.contains(word)) {
                negativeCount++;
            }
        }
        
        if (positiveCount > negativeCount) {
            return "positive";
        } else if (negativeCount > positiveCount) {
            return "negative";
        } else {
            return "neutral";
        }
    }
}