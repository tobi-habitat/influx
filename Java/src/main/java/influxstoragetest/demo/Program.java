package influxstoragetest.demo;

import io.vavr.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

@Component
public class Program implements CommandLineRunner {

    private final Logger LOG = LoggerFactory.getLogger(Program.class);

    private final InfluxService influxService;

    public Program(InfluxService influxService) {
        this.influxService = influxService;
    }

    @Override
    public void run(String... args) {
        influxService.createDatabase();

        int rowCount = 10 * 1000 * 1000;
        String metric = "metric.java";
        LocalDateTime time = LocalDateTime.now();
        double value = 10.01;
        final int INFLUX_BATCH_SIZE = 5 * 1000;

        AtomicInteger counter = new AtomicInteger(0);

        Stream
                //prepare batch
                .range(0, rowCount)
                .zipWithIndex()
                .groupBy(tuple -> tuple._2 / INFLUX_BATCH_SIZE)

                //execute batch
                .forEach((index, batch) -> {

                    java.util.List<String> rows = batch
                            .map(iTuple -> createInfluxRow(iTuple._1, metric, value, time))
                            .toJavaList();

                    counter.addAndGet(rows.size());

                    ListenableFuture<ResponseEntity<String>> future = influxService.writeBulkAsync(rows);
                    future.addCallback(

                            new ListenableFutureCallback<ResponseEntity<String>>() {
                                @Override
                                public void onSuccess(ResponseEntity<String> response) {
                                    if(response.getStatusCode() != HttpStatus.NO_CONTENT) {
                                        LOG.error(response.getBody());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    LOG.error(t.getMessage(), t);
                                }
                            }
                    );
                });

        LOG.info("\n\nEnd with counter={}\n", counter.get());
    }

    private String createInfluxRow(int i, String metric, double value, LocalDateTime now) {
        long epochSecond = now.plusSeconds(i).atZone(ZoneId.systemDefault()).toEpochSecond();
        long epochNano = epochSecond * 1000 * 1000 * 1000;
        return format("%s value=%s %s", metric, value, epochNano);
    }
}
