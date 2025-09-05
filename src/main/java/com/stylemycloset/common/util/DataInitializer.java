package com.stylemycloset.common.util;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectableRepository;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.Length;
import com.stylemycloset.recommendation.entity.Material;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClothesAttributeDefinitionRepository definitionRepository;
    private final ClothesAttributeDefinitionSelectableRepository valueRepository;

    @Override
    public void run(String... args) {
        // pgVector/XGBoost 더미 데이터 생성 제거됨 (Hugging Face로 전환)
        
        // enum 기반 속성 초기화
        initializeAttribute("color", Color.values());
        initializeAttribute("length", Length.values());
        initializeAttribute("material", Material.values());
    }

    private <E extends Enum<E>> void initializeAttribute(String attributeName, E[] values) {
        // definition 조회 또는 생성
        ClothesAttributeDefinition definition = definitionRepository.findByName(attributeName)
            .orElseGet(() -> {
                ClothesAttributeDefinition def = new ClothesAttributeDefinition(attributeName,
                    new ArrayList<String>());
                return definitionRepository.save(def);
            });

        // 선택값 초기화
        for (E val : values) {
            String valueStr = val.name();
            boolean exists = valueRepository.existsByDefinitionAndValue(definition, valueStr);
            if (!exists) {
                ClothesAttributeSelectableValue newValue = new ClothesAttributeSelectableValue(definition, valueStr);
                valueRepository.save(newValue);
            }
        }
    }
}