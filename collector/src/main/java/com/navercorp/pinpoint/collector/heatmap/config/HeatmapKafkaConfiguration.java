/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.heatmap.config;

import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStat;
import com.navercorp.pinpoint.pinot.kafka.KafkaConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * @author minwoo-jung
 */
@Configuration
@Import({KafkaConfiguration.class})
public class HeatmapKafkaConfiguration {

    @Bean
    public KafkaTemplate<String, HeatmapStat> kafkaHeatmapStatTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
