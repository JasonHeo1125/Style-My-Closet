package com.stylemycloset.recommendation.service;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.recommendation.client.HuggingFaceClient;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.mapper.ClothesMapper;
import com.stylemycloset.recommendation.mapper.RecommendationMapper;
import com.stylemycloset.recommendation.util.InputFormatter;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HuggingFaceRecommendationService {

    private final HuggingFaceClient client;
    private final RecommendationMapper recommendationMapper;
    private final ClothesMapper clothesMapper;

    private static final String DEFAULT_ZERO_SHOT_MODEL = "facebook/bart-large-mnli";

    public RecommendationDto recommend(List<Clothes> clothes, Weather weather, User user) {
        // 빈 결과로 시작해서 적합한 옷만 추가하는 방식으로 변경
        List<com.stylemycloset.recommendation.dto.ClothesDto> recommendedClothes = new ArrayList<>();

        for (Clothes c : clothes) {
            String text = InputFormatter.buildPrompt(c, weather, user);
            
            // 날씨 조건을 고려한 더 구체적인 분류 라벨 사용
            List<String> candidateLabels = List.of(
                "perfect for this weather", 
                "suitable for this weather", 
                "not suitable for this weather",
                "completely inappropriate for this weather"
            );
            
            Map<String, Object> out = client.zeroShotClassify(DEFAULT_ZERO_SHOT_MODEL, text, candidateLabels);
            
            // 응답에서 가장 높은 점수의 라벨 확인
            Object labelsObj = out.get("labels");
            Object scoresObj = out.get("scores");
            boolean shouldRecommend = false;
            
            if (labelsObj instanceof List<?> labels && scoresObj instanceof List<?> scores && 
                labels.size() == scores.size() && !labels.isEmpty()) {
                
                String bestLabel = String.valueOf(labels.get(0)); // 첫 번째가 가장 높은 점수
                double bestScore = ((Number) scores.get(0)).doubleValue();
                
                // 날씨에 적합한 의상으로 분류된 경우만 추천 (임계값 0.5로 낮춤)
                shouldRecommend = (bestLabel.contains("perfect") || bestLabel.contains("suitable")) && 
                                 !bestLabel.contains("not suitable") && bestScore > 0.5;
                
                // 규칙 기반 백업 로직: AI가 잘못 판단한 경우 수정
                if (!shouldRecommend && bestScore < 0.7) { // 신뢰도가 낮은 경우에만 백업 로직 적용
                    shouldRecommend = applyFashionRules(c, weather);
                    if (shouldRecommend) {
                        System.out.println("Fashion rule override applied for: " + c.getName());
                    }
                }
                
                // 강제 규칙: AI 판단과 관계없이 적용
                Boolean forceRule = applyForceRules(c, weather);
                if (forceRule != null) {
                    shouldRecommend = forceRule;
                    System.out.println("Force rule applied for: " + c.getName() + " -> " + shouldRecommend);
                }
                
                System.out.println("Clothing analysis - Item: " + c.getName() + 
                                 ", Best label: " + bestLabel + 
                                 ", Score: " + bestScore + 
                                 ", Recommended: " + shouldRecommend);
            }

            // 적합한 옷만 결과에 추가
            if (shouldRecommend) {
                recommendedClothes.add(clothesMapper.toClothesDto(c));
            }
        }
        
        return new RecommendationDto(weather.getId(), user.getId(), recommendedClothes);
    }
    
    /**
     * 규칙 기반 패션 로직: AI가 잘못 판단한 경우를 보정
     */
    private boolean applyFashionRules(Clothes clothes, Weather weather) {
        if (weather == null || weather.getTemperature() == null) return false;
        
        double temp = weather.getTemperature().getCurrent();
        String material = getMaterial(clothes);
        String length = getLength(clothes);
        String clothesType = clothes.getClothesType().name();
        String clothesName = clothes.getName().toLowerCase();
        
        // 더운 날씨 (25°C 이상) 규칙
        if (temp >= 25.0) {
            // 린넨, 면 소재 + 반팔 = 추천
            if (("LINEN".equals(material) || "COTTON".equals(material)) && "SHORT".equals(length)) {
                return true;
            }
            
            // 다운 자켓/패딩 = 강제 비추천 (이름으로 판단)
            if (clothesName.contains("다운") || clothesName.contains("down") || 
                clothesName.contains("패딩") || clothesName.contains("padding") ||
                material != null && material.contains("DOWN")) {
                System.out.println("Fashion rule: Down jacket blocked for hot weather (" + temp + "°C)");
                return false;
            }
        }
        
        // 추운 날씨 (15°C 이하) 규칙  
        if (temp <= 15.0) {
            // 다운 자켓/패딩 = 추천 (이름으로 판단)
            if (clothesName.contains("다운") || clothesName.contains("down") || 
                clothesName.contains("패딩") || clothesName.contains("padding") ||
                ("DOWN".equals(material) || "WOOL".equals(material)) && "OUTER".equals(clothesType)) {
                return true;
            }
            // 반팔 = 비추천
            if ("SHORT".equals(length) && "TOP".equals(clothesType)) {
                return false;
            }
        }
        
        return false; // 규칙에 해당하지 않으면 AI 판단 유지
    }
    
    /**
     * 강제 규칙: AI 판단과 관계없이 무조건 적용되는 패션 규칙
     * @return null이면 AI 판단 유지, true/false면 강제 적용
     */
    private Boolean applyForceRules(Clothes clothes, Weather weather) {
        if (weather == null || weather.getTemperature() == null) return null;
        
        double temp = weather.getTemperature().getCurrent();
        String clothesName = clothes.getName().toLowerCase();
        
        // 더운 날씨 (25°C 이상)에서 다운 자켓/패딩은 무조건 비추천
        if (temp >= 25.0) {
            if (clothesName.contains("다운") || clothesName.contains("down") || 
                clothesName.contains("패딩") || clothesName.contains("padding")) {
                return false; // 강제 비추천
            }
        }
        
        return null; // 강제 규칙에 해당하지 않으면 AI 판단 유지
    }
    
    private String getMaterial(Clothes clothes) {
        return clothes.getSelectedValues().stream()
            .filter(v -> "material".equals(v.getSelectableValue().getDefinition().getName()))
            .map(v -> v.getSelectableValue().getValue())
            .findFirst()
            .orElse(null);
    }
    
    private String getLength(Clothes clothes) {
        return clothes.getSelectedValues().stream()
            .filter(v -> "length".equals(v.getSelectableValue().getDefinition().getName()))
            .map(v -> v.getSelectableValue().getValue())
            .findFirst()
            .orElse(null);
    }
}


