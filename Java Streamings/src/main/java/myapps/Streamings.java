package myapps;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Arrays;
import java.util.Date;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.apache.kafka.streams.kstream.Suppressed.*;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.*;

/**
 * In this example, we implement a simple WordCount program using the high-level Streams DSL
 * that reads from a source topic "streams-plaintext-input", where the values of messages represent lines of text,
 * split each text line into words and then compute the word occurence histogram, write the continuous updated histogram
 * into a topic "streams-wordcount-output" where each record is an updated count of a single word.
 */
public class Streamings {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        final StreamsBuilder builder = new StreamsBuilder();

        Duration bigWindow = Duration.ofMinutes(2);
        Duration windowSize = Duration.ofSeconds(15);

        final KStream<String, String> valores = builder.stream("mqtt.echo" , Consumed.with(Serdes.String(), Serdes.String()));
        final KStream<String, String> p1 = valores.flatMapValues(value -> Arrays.asList(value.split(",")))
        .filter((key, value) -> value.contains("uuid")).selectKey((ignoredKey, word) -> word.substring(0, 6))
        .mapValues(value -> value.substring(7));
        
        p1.groupByKey()
            .windowedBy(TimeWindows.of(bigWindow).grace(windowSize))
            .aggregate(() -> new Date().toString() + "|" , (key, uuids, total) -> total += uuids + ";",
                  Materialized.with(Serdes.String(), Serdes.String()))
                  .suppress(untilWindowCloses(unbounded()))
        .toStream()
        .to("IOTRACE");
        
        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
       
    }
}

