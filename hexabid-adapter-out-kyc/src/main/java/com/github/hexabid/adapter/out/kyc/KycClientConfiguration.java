package com.github.hexabid.adapter.out.kyc;

import com.github.hexabid.kyc.client.api.KycVerificationApi;
import com.github.hexabid.kyc.client.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KycClientConfiguration {

    @Bean
    KycVerificationApi kycVerificationApi(@Value("${auctions.kyc.base-url:https://kyc.example.test}") String baseUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        return new KycVerificationApi(apiClient);
    }
}
