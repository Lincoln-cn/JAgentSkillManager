package org.unreal.agent.skill.example;

import org.springframework.stereotype.Component;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Example skill implementation that provides date and time information.
 * This skill can provide current date, time, and perform simple date calculations.
 */
@Component
public class DateTimeSkill implements AgentSkill {
    
    @Override
    public String getName() {
        return "datetime";
    }
    
    @Override
    public String getDescription() {
        return "Provides current date, time, and simple date calculations";
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
        return lowerRequest.contains("time") || 
               lowerRequest.contains("date") || 
               lowerRequest.contains("current") ||
               lowerRequest.contains("now") ||
               lowerRequest.contains("today");
    }
    
    @Override
    public AgentSkillResult execute(String request, Map<String, Object> parameters) {
        try {
            String operation = determineOperation(request, parameters);
            Map<String, Object> result = performDateTimeOperation(operation, parameters);
            
            return AgentSkillResult.success()
                    .message("Date/time operation completed successfully")
                    .data(result)
                    .skillName(getName())
                    .build();
                    
        } catch (Exception e) {
            return AgentSkillResult.failure()
                    .message("Failed to perform date/time operation: " + e.getMessage())
                    .skillName(getName())
                    .build();
        }
    }
    
    @Override
    public Map<String, String> getRequiredParameters() {
        return new HashMap<>(); // No required parameters
    }
    
    @Override
    public Map<String, String> getOptionalParameters() {
        Map<String, String> optional = new HashMap<>();
        optional.put("operation", "Type of operation: current_time, current_date, current_datetime, format");
        optional.put("format", "Date/time format pattern (e.g., 'yyyy-MM-dd HH:mm:ss')");
        optional.put("timezone", "Timezone for the result");
        optional.put("days", "Number of days to add/subtract from current date");
        return optional;
    }
    
    /**
     * Determine the operation to perform based on request and parameters.
     */
    private String determineOperation(String request, Map<String, Object> parameters) {
        // Check if operation is specified in parameters
        if (parameters != null && parameters.containsKey("operation")) {
            return parameters.get("operation").toString().toLowerCase();
        }
        
        // Determine operation from request text
        if (request == null) {
            return "current_datetime";
        }
        
        String lowerRequest = request.toLowerCase();
        if (lowerRequest.contains("time") && !lowerRequest.contains("date")) {
            return "current_time";
        } else if (lowerRequest.contains("date") && !lowerRequest.contains("time")) {
            return "current_date";
        } else {
            return "current_datetime";
        }
    }
    
    /**
     * Perform the date/time operation.
     */
    private Map<String, Object> performDateTimeOperation(String operation, Map<String, Object> parameters) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> result = new HashMap<>();
        
        switch (operation) {
            case "current_time":
                result.put("time", now.format(DateTimeFormatter.ISO_LOCAL_TIME));
                result.put("hour", now.getHour());
                result.put("minute", now.getMinute());
                result.put("second", now.getSecond());
                break;
                
            case "current_date":
                result.put("date", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
                result.put("year", now.getYear());
                result.put("month", now.getMonthValue());
                result.put("day", now.getDayOfMonth());
                result.put("dayOfWeek", now.getDayOfWeek().toString());
                break;
                
            case "format":
                String format = getFormat(parameters);
                DateTimeFormatter formatter;
                try {
                    formatter = DateTimeFormatter.ofPattern(format);
                } catch (Exception e) {
                    formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                }
                result.put("formatted", now.format(formatter));
                result.put("formatUsed", format);
                break;
                
            case "add_days":
            case "subtract_days":
                int days = getDays(parameters);
                LocalDateTime calculatedDate = operation.equals("add_days") ? 
                    now.plusDays(days) : now.minusDays(days);
                result.put("originalDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
                result.put("calculatedDate", calculatedDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                result.put("daysModified", days);
                result.put("operation", operation);
                break;
                
            default: // current_datetime
                result.put("datetime", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                result.put("date", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
                result.put("time", now.format(DateTimeFormatter.ISO_LOCAL_TIME));
                result.put("timestamp", System.currentTimeMillis());
                break;
        }
        
        // Add timezone info (simplified)
        result.put("timezone", java.util.TimeZone.getDefault().getID());
        
        return result;
    }
    
    /**
     * Get format string from parameters or use default.
     */
    private String getFormat(Map<String, Object> parameters) {
        if (parameters != null && parameters.containsKey("format")) {
            return parameters.get("format").toString();
        }
        return "yyyy-MM-dd HH:mm:ss";
    }
    
    /**
     * Get days parameter for date calculations.
     */
    private int getDays(Map<String, Object> parameters) {
        if (parameters != null && parameters.containsKey("days")) {
            try {
                return Integer.parseInt(parameters.get("days").toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}