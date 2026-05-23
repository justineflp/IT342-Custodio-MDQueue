package edu.cit.custodio.mdqueue.feature.external;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MedicalDataService {

    private final WebClient webClient;

    public MedicalDataService(WebClient.Builder webClientBuilder) {
        // Using OpenFDA as a public health API example
        this.webClient = webClientBuilder.baseUrl("https://api.fda.gov/drug/label.json").build();
    }

    public Mono<String> getDrugInfo(String drugName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("search", "openfda.brand_name:\"" + drugName + "\"")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("{\"error\": \"Could not fetch data for " + drugName + "\"}");
    }
}
