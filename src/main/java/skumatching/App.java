package skumatching;

import com.hellofresh.culinary.menu_planning.menu_planning_service.menu;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serdes.Menu;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        final App app = new App();
        Properties props = app.loadProperties();

        final SpecificAvroSerde<menu> menuAvroSerde = Menu.BuildMenuAvroSerde(props);

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, menu> menuAvroStream = builder
                .stream("rawevents.menu.v1", Consumed.with(Serdes.String(), menuAvroSerde));
        menuAvroStream.print(Printed.toSysOut());

        final Topology topology = builder.build();
        System.out.println(topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            public void run() {
                log.info("closing kafka streaming application");
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            log.info("started kafka streaming application");
            latch.await();
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("client.properties"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler.class);

        return props;
    }

}
