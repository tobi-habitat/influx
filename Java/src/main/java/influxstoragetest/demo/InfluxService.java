package influxstoragetest.demo;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class InfluxService {

    private final RestTemplate restTemplate;
    private final InfluxConfig config;

    public InfluxService(InfluxConfig config, RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.config = config;
    }

    public ResponseEntity<String> createDatabase() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("q", "CREATE DATABASE " + config.database);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        String url = config.endpoint + "/query";

        return new RestTemplate().postForEntity( url, request , String.class );
    }

    public ListenableFuture<ResponseEntity<String>> write(String row) {
        String url = String.format("%s/write?db=%s", config.endpoint, config.database);
        return new AsyncRestTemplate().postForEntity(url, new HttpEntity<>(row), String.class);
    }

    public ResponseEntity<String> writeBulk(List<String> rows) {

        String content = String.join("\n", rows);
        String url = String.format("%s/write?db=%s", config.endpoint, config.database);

        return restTemplate.postForEntity(url, content, String.class);
    }

    public ListenableFuture<ResponseEntity<String>> writeBulkAsync(List<String> rows) {

        String content = String.join("\n", rows);
        String url = String.format("%s/write?db=%s", config.endpoint, config.database);

        return new AsyncRestTemplate().postForEntity(url, new HttpEntity<>(content), String.class);
    }
}
