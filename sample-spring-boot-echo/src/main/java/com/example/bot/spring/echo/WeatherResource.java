package com.example.bot.spring.echo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WeatherResource {
    public String publishingOffice;
    public String reportDatetime;
    public List<WeatherTimeSeries> timeSeries;

    @Data
    public static class WeatherTimeSeries {
        List<String> timeDefines;
        List<AreaInfo> areas;

        @Data
        public static class AreaInfo {
            Area area;
            List<String> weatherCodes;
            List<String> weathers;
        }

        @Data
        public static class Area {
            String name;
            String code;
        }

    }
}
