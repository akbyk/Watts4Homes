package com.akbyk.watts4homes.core.homes;

import com.akbyk.watts4homes.core.homes.dto.HomeStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/homes")
@RequiredArgsConstructor
@Tag(name = "Home Status", description = "Live status polling - reads exclusively from Ignite")
public class HomeStatusController {

    private final HomeStatusService homeStatusService;

    @GetMapping("/{homeId}/status")
    @Operation(summary = "Get live status for one home (polled every 1-2s by the frontend)")
    public ResponseEntity<HomeStatusResponse> getStatus(@PathVariable Long homeId) {
        return homeStatusService.getStatus(homeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status")
    @Operation(summary = "Get live status for all homes (dashboard grid view)")
    public ResponseEntity<List<HomeStatusResponse>> getAllStatuses() {
        return ResponseEntity.ok(homeStatusService.getAllStatuses());
    }
}