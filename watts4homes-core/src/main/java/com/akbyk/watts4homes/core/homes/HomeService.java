package com.akbyk.watts4homes.core.homes;

import com.akbyk.watts4homes.core.homes.dto.ApplianceRequest;
import com.akbyk.watts4homes.core.homes.dto.HomeRegistrationRequest;
import com.akbyk.watts4homes.core.homes.dto.HomeRegistrationResponse;
import com.akbyk.watts4homes.core.event.HomeRegisteredEvent;
import com.akbyk.watts4homes.core.event.HomeRegisteredInternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeRepository homeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public HomeRegistrationResponse registerHome(HomeRegistrationRequest request) {
        Home home = new Home();
        home.setName(request.name());
        home.setAddress(request.address());
        home.setContactEmail(request.contactEmail());
        home.setBudgetQuota(BigDecimal.valueOf(request.budgetQuota()));
        home.setCurrentRate(BigDecimal.valueOf(request.currentRate()));
        home.setPenaltyRate(BigDecimal.valueOf(request.penaltyRate()));

        for (ApplianceRequest applianceRequest : request.appliances()) {
            Appliance appliance = new Appliance();
            appliance.setName(applianceRequest.name());
            appliance.setType(applianceRequest.type());
            appliance.setSafeLimitWatts(applianceRequest.safeLimitWatts());
            home.addAppliance(appliance);
        }

        Home saved = homeRepository.save(home);

        // Fired now, delivered by Spring AFTER this @Transactional method commits.
        applicationEventPublisher.publishEvent(new HomeRegisteredInternalEvent(toKafkaEvent(saved)));

        return toResponse(saved);
    }

    private HomeRegisteredEvent toKafkaEvent(Home home) {
        List<HomeRegisteredEvent.ApplianceEvent> appliances = home.getAppliances().stream()
                .map(a -> new HomeRegisteredEvent.ApplianceEvent(a.getId(), a.getName(), a.getType(), a.getSafeLimitWatts()))
                .toList();
        return new HomeRegisteredEvent(
                home.getId(), home.getName(), home.getContactEmail(),
                home.getBudgetQuota().doubleValue(), home.getCurrentRate().doubleValue(), home.getPenaltyRate().doubleValue(),
                appliances
        );
    }

    private HomeRegistrationResponse toResponse(Home home) {
        List<HomeRegistrationResponse.ApplianceResponse> appliances = home.getAppliances().stream()
                .map(a -> new HomeRegistrationResponse.ApplianceResponse(a.getId(), a.getName(), a.getType(), a.getSafeLimitWatts()))
                .toList();
        return new HomeRegistrationResponse(home.getId(), home.getName(), home.getContactEmail(), appliances);
    }
}