package com.example.bot.spring.echo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WeatherResource {
    String publishingOffice;
    LocalDateTime reportDatetime;
    List<WeatherTimeSeries> timeSeries;

    @Data
    public static class WeatherTimeSeries {
        List<LocalDateTime> timeDefines;
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
