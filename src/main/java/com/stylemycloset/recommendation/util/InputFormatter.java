package com.stylemycloset.recommendation.util;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.stream.Collectors;

public class InputFormatter {

    public static String buildPrompt(Clothes clothes, Weather weather, User user) {
        String attributes = clothes.getSelectedValues() == null ? ""
            : clothes.getSelectedValues().stream()
                .map(v -> v.getSelectableValue().getDefinition().getName() + ":" + v.getSelectableValue().getValue())
                .collect(Collectors.joining(", "));

        // 더 자연스러운 날씨 설명 생성
        String weatherDescription = buildWeatherDescription(weather);
        String userDescription = buildUserDescription(user);
        
        // 더 구체적인 의상-날씨 매칭 프롬프트
        return String.format(
            "Weather: %s. User: %s. " +
            "Clothing item: %s. " +
            "Fashion rule: Linen and cotton are perfect for hot weather (25°C+). " +
            "Short sleeves are ideal for warm/hot weather. " +
            "Down jackets are only for cold weather (below 15°C). " +
            "Is this clothing appropriate for the current weather?", 
            weatherDescription, userDescription, attributes
        );
    }
    
    private static String buildWeatherDescription(Weather weather) {
        if (weather == null) return "unknown weather";
        
        double temp = weather.getTemperature() != null ? weather.getTemperature().getCurrent() : 20.0;
        double humidity = weather.getHumidity() != null ? weather.getHumidity().getCurrent() : 50.0;
        double windSpeed = weather.getWindSpeed() != null ? weather.getWindSpeed().getCurrent() : 0.0;
        
        StringBuilder desc = new StringBuilder();
        
        // 온도 설명
        if (temp <= 0) desc.append("very cold weather (").append(temp).append("°C)");
        else if (temp <= 10) desc.append("cold weather (").append(temp).append("°C)");
        else if (temp <= 20) desc.append("cool weather (").append(temp).append("°C)");
        else if (temp <= 25) desc.append("mild weather (").append(temp).append("°C)");
        else if (temp <= 30) desc.append("warm weather (").append(temp).append("°C)");
        else desc.append("hot weather (").append(temp).append("°C)");
        
        // 습도 추가
        if (humidity > 70) desc.append(", high humidity");
        else if (humidity < 30) desc.append(", low humidity");
        
        // 바람 추가
        if (windSpeed > 10) desc.append(", windy conditions");
        
        // 하늘 상태 추가
        if (weather.getSkyStatus() != null) {
            switch (weather.getSkyStatus()) {
                case CLEAR -> desc.append(", clear sky");
                case MOSTLY_CLOUDY -> desc.append(", mostly cloudy");
                case CLOUDY -> desc.append(", cloudy");
            }
        }
        
        return desc.toString();
    }
    
    private static String buildUserDescription(User user) {
        if (user == null) return "average user preferences";
        
        StringBuilder desc = new StringBuilder();
        if (user.getGender() != null) {
            desc.append(user.getGender().toString().toLowerCase()).append(" user");
        }
        
        if (user.getTemperatureSensitivity() != null) {
            int sensitivity = user.getTemperatureSensitivity();
            if (sensitivity < -1) desc.append(" who feels cold easily");
            else if (sensitivity > 1) desc.append(" who feels warm easily");
            else desc.append(" with normal temperature sensitivity");
        }
        
        return desc.toString().isEmpty() ? "average user preferences" : desc.toString();
    }
}


