package com.example.bot.spring.echo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WeatherResource {
    public String publishingOffice;
    public String reportDatetime;
    public List<WeatherTimeSeries> timeSeries;
    public Object tempAverage;
    public Object precipAverage;

    @Data
    public static class WeatherTimeSeries {
        List<String> timeDefines;
        List<AreaInfo> areas;

        @Data
        public static class AreaInfo {
            Area area;
            Object temps;
            List<String> weatherCodes;
            List<String> weathers;
            List<String> winds;
            List<String> waves;
            List<String> pops;
            List<String> reliabilities;

            // temperature
            List<String> tempsMin;
            List<String> tempsMinUpper;
            List<String> tempsMinLower;
            List<String> tempsMax;
            List<String> tempsMaxUpper;
            List<String> tempsMaxLower;
        }

        @Data
        public static class Area {
            String name;
            String code;
        }

    }
}
