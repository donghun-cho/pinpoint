package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.web.uid.service.ApplicationIndexV2CopyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin/copy")
@ConditionalOnProperty(name = "pinpoint.web.application.index.v2.enabled", havingValue = "true")
public class ApplicationIndexV2CopyController {

    private final ApplicationIndexV2CopyService applicationIndexV2CopyService;

    public ApplicationIndexV2CopyController(ApplicationIndexV2CopyService applicationIndexV2CopyService) {
        this.applicationIndexV2CopyService = Objects.requireNonNull(applicationIndexV2CopyService, "applicationUidCopyService");
    }


    @GetMapping(value = "/applicationIndex")
    public ResponseEntity<String> copyApplicationList(
            @RequestParam(value = "durationDays", required = false, defaultValue = "0") int durationDays,
            @RequestParam(value = "maxIterations", required = false, defaultValue = "200000") int maxIterations,
            @RequestParam(value = "batchSize", required = false, defaultValue = "1000") int batchSize
    ) {
        applicationIndexV2CopyService.copyApplication();
        applicationIndexV2CopyService.copyAgentId(durationDays, maxIterations, batchSize);
        return ResponseEntity.ok("OK");
    }
}
