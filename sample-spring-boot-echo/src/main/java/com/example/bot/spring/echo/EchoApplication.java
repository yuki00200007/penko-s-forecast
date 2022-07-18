/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring.echo;

import com.example.bot.spring.echo.WeatherResource.WeatherTimeSeries.AreaInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {
    private final Logger log = LoggerFactory.getLogger(EchoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EchoApplication.class, args);
    }

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        log.info("event: " + event);
        final String originalMessageText = event.getMessage().getText();

        // 天気に関する問い合わせなのか
        final String[] weatherKeywordArr = {"weather", "てんき", "テンキ", "天気", "暑い", "寒い", "雨", "晴"};
        final List<String> weatherKeywords = Arrays.asList(weatherKeywordArr);
        if (weatherKeywords.stream().anyMatch(originalMessageText::contains)) {
            // access to api
            String urlString = "https://www.jma.go.jp/bosai/forecast/data/forecast/130000.json";
            try {
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("accept", "application/json");

                String response = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String tmp;
                while ((tmp = in.readLine()) != null) {
                    response += tmp;
                }

                ObjectMapper mapper = new ObjectMapper();
                List<WeatherResource> result = mapper.readValue(response, new TypeReference<List<WeatherResource>>() {
                });

                con.disconnect();

                List<WeatherInfo> tokyoWeather = findTokyo(result.get(0));
                if (tokyoWeather == null) return new TextMessage("取得に失敗したTT");
                String weatherMessage = tokyoWeather.stream().map(tw -> String.format("%sは %s", tw.getTimeDef(), tw.getWeather())).collect(Collectors.joining(" ¥n"));
                return new TextMessage(weatherMessage);
            } catch (IOException e) {
                e.printStackTrace();
                return new TextMessage("取得に失敗したTT");
            }
        }
        return new TextMessage("天気のことしかわからないTT");
    }

    private List<WeatherInfo> findTokyo(WeatherResource result) {
        final List<WeatherInfo> wis = new ArrayList<>();

        for (WeatherResource.WeatherTimeSeries ts : result.getTimeSeries()) {
            final List<AreaInfo> areaInfos = ts.getAreas().stream().filter(area -> Objects.equals(area.getArea().getCode(), "130010")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(areaInfos)) {
                continue;
            }
            final List<String> timeDefines = ts.getTimeDefines();
            final AreaInfo areaInfo = areaInfos.get(0);
            final List<String> weathers = areaInfo.getWeathers();
            for (int i = 0; i < timeDefines.size(); i++) {
                final String timeDef = timeDefines.get(i);
                // "2022-07-18T11:00:00+09:00"
                final ZonedDateTime dt = ZonedDateTime.parse(timeDef);
                final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
                final String day = getDay(dt, now);
                if (day == null) return null;
                final String weather = weathers.get(i);

                WeatherInfo wi = new WeatherInfo();
                wi.setTimeDef(day);
                wi.setWeather(weather);
                wis.add(wi);
            }
            break;
        }
        return wis;
    }

    @Nullable
    private String getDay(ZonedDateTime dt, ZonedDateTime now) {
        int target = dt.getDayOfMonth();
        int today = now.getDayOfMonth();
        if (today == target) return "今日これから";
        if (today == target - 1) return "明日";
        if (today == target - 2) return "明後日";
        return null;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
}
