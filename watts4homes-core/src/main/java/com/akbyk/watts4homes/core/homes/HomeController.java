package com.akbyk.watts4homes.core.homes;

import com.akbyk.watts4homes.core.homes.dto.HomeRegistrationRequest;
import com.akbyk.watts4homes.core.homes.dto.HomeRegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/homes")
@RequiredArgsConstructor
@Tag(name = "Homes", description = "Home registration and live monitoring")
public class HomeController {

    private final HomeService homeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new home",
            description = "Persists the home with its appliances, then publishes the asset "
                    + "configuration to the Kafka registration topic after the transaction commits"
    )
    public HomeRegistrationResponse registerHome(@Valid @RequestBody HomeRegistrationRequest request) {
        return homeService.registerHome(request);
    }
}