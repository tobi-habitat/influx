package influxstoragetest.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InfluxConfig {

    public final String endpoint;
    public final String database;

    public InfluxConfig(@Value("${influx.endpoint:http://localhost:8086}") String endpoint,
                        @Value("${influx.database:test}") String database) {

        this.endpoint = endpoint;
        this.database = database;
    }
}
