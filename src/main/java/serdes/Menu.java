package serdes;

import com.hellofresh.culinary.menu_planning.menu_planning_service.menu;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Menu {

    public static SpecificAvroSerde<menu> BuildMenuAvroSerde(Properties props) {
        SpecificAvroSerde<menu> menuAvroSerde = new SpecificAvroSerde<>();

        Map<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put("basic.auth.credentials.source", props.getProperty("basic.auth.credentials.source"));
        serdeConfig.put("schema.registry.basic.auth.user.info", props.getProperty("schema.registry.basic.auth.user.info"));
        serdeConfig.put("schema.registry.url", props.getProperty("schema.registry.url"));
        menuAvroSerde.configure(serdeConfig, false);

        return menuAvroSerde;
    }
}
