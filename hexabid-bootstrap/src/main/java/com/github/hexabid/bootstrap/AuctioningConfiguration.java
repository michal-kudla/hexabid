package com.github.hexabid.bootstrap;

import com.github.hexabid.adapter.in.ws.AuctionWebSocketEventPublisher;
import com.github.hexabid.adapter.out.kafka.KafkaAuctionEventPublisher;
import com.github.hexabid.auth.core.identityaccess.port.in.FindCurrentUserProfileUseCase;
import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.auth.core.identityaccess.port.out.UserVerificationStatusPort;
import com.github.hexabid.auth.core.identityaccess.usecase.CompositeCurrentUserProvider;
import com.github.hexabid.auth.core.identityaccess.usecase.FindCurrentUserProfileService;
import com.github.hexabid.core.auctioning.event.AuctionDomainEvent;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsUseCase;
import com.github.hexabid.core.auctioning.port.in.CloseExpiredAuctionsUseCase;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionUseCase;
import com.github.hexabid.core.auctioning.port.in.FindAuctionDetailsUseCase;
import com.github.hexabid.core.auctioning.port.in.PlaceBidUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionEventPublisher;
import com.github.hexabid.core.auctioning.port.out.AuctionReadModel;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import com.github.hexabid.core.auctioning.port.out.KycClient;
import com.github.hexabid.core.auctioning.usecase.BrowseAuctionsService;
import com.github.hexabid.core.auctioning.usecase.CloseExpiredAuctionsService;
import com.github.hexabid.core.auctioning.usecase.CreateAuctionService;
import com.github.hexabid.core.auctioning.usecase.FindAuctionDetailsService;
import com.github.hexabid.core.auctioning.usecase.PlaceBidService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;
import java.util.List;

@Configuration
class AuctioningConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CreateAuctionUseCase createAuctionUseCase(AuctionRepository repository, KycClient kycClient, Clock clock) {
        return new CreateAuctionService(repository, kycClient, clock);
    }

    @Bean
    PlaceBidUseCase placeBidUseCase(
            AuctionRepository repository,
            KycClient kycClient,
            AuctionEventPublisher eventPublisher,
            Clock clock
    ) {
        return new PlaceBidService(repository, kycClient, eventPublisher, clock);
    }

    @Bean
    FindAuctionDetailsUseCase findAuctionDetailsUseCase(AuctionRepository repository) {
        return new FindAuctionDetailsService(repository);
    }

    @Bean
    BrowseAuctionsUseCase browseAuctionsUseCase(AuctionReadModel auctionReadModel) {
        return new BrowseAuctionsService(auctionReadModel);
    }

    @Bean
    CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase(
            AuctionRepository repository,
            AuctionEventPublisher eventPublisher
    ) {
        return new CloseExpiredAuctionsService(repository, eventPublisher);
    }

    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    KafkaAuctionEventPublisher kafkaAuctionEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${auctions.kafka.topics.outbid:auctions.outbid}") String outbidTopic,
            @Value("${auctions.kafka.topics.ended:auctions.ended}") String endedTopic
    ) {
        return new KafkaAuctionEventPublisher(kafkaTemplate, outbidTopic, endedTopic);
    }

    @Bean
    AuctionWebSocketEventPublisher auctionWebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        return new AuctionWebSocketEventPublisher(messagingTemplate);
    }

    @Bean
    AuctionEventPublisher auctionEventPublisher(
            ObjectProvider<KafkaAuctionEventPublisher> kafkaAuctionEventPublisherProvider,
            AuctionWebSocketEventPublisher auctionWebSocketEventPublisher
    ) {
        return event -> publishToChannels(
                event,
                kafkaAuctionEventPublisherProvider.getIfAvailable(),
                auctionWebSocketEventPublisher
        );
    }

    @Bean
    UserVerificationStatusPort userVerificationStatusPort(KycClient kycClient) {
        return kycClient::isVerified;
    }

    @Bean
    @Primary
    CurrentUserProvider compositeCurrentUserProvider(List<CurrentUserProvider> providers) {
        return new CompositeCurrentUserProvider(providers);
    }

    @Bean
    FindCurrentUserProfileUseCase findCurrentUserProfileUseCase(
            @Qualifier("compositeCurrentUserProvider") CurrentUserProvider currentUserProvider,
            UserVerificationStatusPort userVerificationStatusPort
    ) {
        return new FindCurrentUserProfileService(currentUserProvider, userVerificationStatusPort);
    }

    private static void publishToChannels(
            AuctionDomainEvent event,
            KafkaAuctionEventPublisher kafkaAuctionEventPublisher,
            AuctionWebSocketEventPublisher auctionWebSocketEventPublisher
    ) {
        if (kafkaAuctionEventPublisher != null) {
            kafkaAuctionEventPublisher.publish(event);
        }
        auctionWebSocketEventPublisher.publish(event);
    }
}
