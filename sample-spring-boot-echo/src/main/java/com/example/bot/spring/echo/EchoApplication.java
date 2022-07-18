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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        final String[] weatherKeywordArr = {"weather", "天気", "暑い", "寒い", "雨", "晴"};
        final List<String> weatherKeywords = Arrays.asList(weatherKeywordArr);
        if (weatherKeywords.stream().anyMatch(originalMessageText::contains)) {
            // access to api
            String urlString = "https://www.jma.go.jp/bosai/forecast/data/forecast/130000.json";
            try {
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.connect(); // URL接続
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String tmp;

                while ((tmp = in.readLine()) != null) {
                    response.append(tmp);
                }

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(response);
                List<WeatherResource> result = mapper.readValue(json, new TypeReference<List<WeatherResource>>() {
                });
                in.close();
                con.disconnect();

                String tokyoWeather = findTokyo(result.get(0));
                return new TextMessage(tokyoWeather);
            } catch (IOException e) {
                e.printStackTrace();
                return new TextMessage("取得に失敗したTT");
            }
        }
        return new TextMessage(originalMessageText);
    }

    private String findTokyo(WeatherResource result) {
        String w = "";
        for (WeatherResource.WeatherTimeSeries ts : result.getTimeSeries()) {
            for (WeatherResource.WeatherTimeSeries.AreaInfo areaInfo : ts.getAreas()) {
                if (Objects.equals(areaInfo.getArea().getCode(), "130010")) {

                    w = String.join("¥n", areaInfo.getWeathers());
                }
            }
        }
        return w;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
}
