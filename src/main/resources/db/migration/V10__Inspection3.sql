-- pgVector extension 제거됨 (Hugging Face로 전환)

ALTER TABLE weather
    ADD CONSTRAINT uq_weather_forecast UNIQUE (created_at, forecast_at, location_id);

-- embedding 컬럼 제거됨 (Hugging Face 사용으로 불필요)