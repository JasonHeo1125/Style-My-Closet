package com.stylemycloset.recommendation.service;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final WeatherRepository weatherRepository;
  private final ClothesRepository clothRepository;
  private final UserRepository userRepository;
  private final HuggingFaceRecommendationService huggingFaceRecommendationService;

  @Transactional
  public RecommendationDto recommendation(Long weatherId) {
    ClosetUserDetails userDetails = getCurrentUser();
    User user = null;
    if (userDetails != null) {
      user = userRepository.findById(userDetails.getUserId()).orElseThrow(
          () -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND)
      );
    } else {
      return new RecommendationDto(0L, 0L, null);
    }

    Weather weather = weatherRepository.findById(weatherId).orElseThrow(
        () -> new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "null"))
    );

    List<Clothes> clothes = clothRepository.findAllByOwnerIdFetch(user.getId());

    // Hugging Face 추천 엔진만 사용 (pgVector와 XGBoost 제거됨)
    return huggingFaceRecommendationService.recommend(clothes, weather, user);
  }


  public ClosetUserDetails getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null; // 로그인 안 된 상태
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof ClosetUserDetails) {
      return (ClosetUserDetails) principal;
    }

    return null;
  }
}
