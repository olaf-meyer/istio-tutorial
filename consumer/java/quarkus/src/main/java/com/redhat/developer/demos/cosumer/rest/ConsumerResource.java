package com.redhat.developer.demos.cosumer.rest;

import java.time.Duration;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

import io.quarkus.kafka.client.serialization.JsonbSerde;

@Traced
@ApplicationScoped
public class ConsumerResource {

    @ConfigProperty(name = "stream-topics")
    List<String> topics;
    
    @Produces
    public Topology getCalledServices() {
        Duration windowSizeMs = Duration.ofMinutes(5);
        Duration gracePeriodMs = Duration.ofMinutes(1);

        final StreamsBuilder builder = new StreamsBuilder();
            JsonbSerde<RestServiceCall> restServiceCallSerde = new JsonbSerde<>(RestServiceCall.class);
        
        builder.stream(                                                       
            topics,
            Consumed.with(Serdes.Integer(), restServiceCallSerde)
        )
        .groupBy(
            (key, value) -> value.emptyComment().getKey(),
            Grouped.with(
                Serdes.String(), /* key (note: type was modified) */
                restServiceCallSerde)  /* value */)
        .windowedBy(TimeWindows.of(windowSizeMs).grace(gracePeriodMs).advanceBy(windowSizeMs))
        .reduce((aggValue, newValue) -> aggValue /* adder */)
        .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
        .toStream()
        .print(Printed.toSysOut());
        // .to("demo-application-log-aggregated");
        return builder.build();

    }
    // KafkaStreams streams;

    // @Incoming("restservicecall")
    // public CompletionStage<Void> consumeMessages(Message<String> incomingRestServiceCall) {
    //     StringBuilder out = new StringBuilder();
    //     // tag::code[]
    //     IncomingKafkaRecordMetadata<Integer, String> metadata = incomingRestServiceCall.getMetadata(IncomingKafkaRecordMetadata.class).orElse(null);
    //     if (metadata != null) {
    //         // The topic
    //         String topic = metadata.getTopic();

    //         // The key
    //         Integer key = metadata.getKey();

    //         // The timestamp
    //         Instant timestamp = metadata.getTimestamp();

    //         // The underlying record
    //         KafkaConsumerRecord<Integer, String> record = metadata.getRecord();

    //         // ...
    //         out.append("Topic: ").append(topic).append("\r\n");
    //         out.append("Key: ").append(key).append("\r\n");
    //         out.append("Timestamp: ").append(timestamp).append("\r\n");
    //         out.append("Record: ").append(record).append("\r\n");
    //     }
    //     logger.info(out.toString());
    //     return incomingRestServiceCall.ack();
    //     // end::code[]
    // }

}