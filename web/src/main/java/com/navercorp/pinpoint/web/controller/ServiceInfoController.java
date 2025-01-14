package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.config.UserConfigProperties;
import com.navercorp.pinpoint.web.service.ServiceInfoService;
import com.navercorp.pinpoint.web.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class ServiceInfoController {

    private final ServiceInfoService serviceInfoService;

    private final UserConfigProperties userConfigProperties;
    private final UserService userService;

    public ServiceInfoController(ServiceInfoService serviceInfoService, UserConfigProperties userConfigProperties, UserService userService) {
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceInfoService");
        this.userConfigProperties = Objects.requireNonNull(userConfigProperties, "userConfigProperties");
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    @GetMapping(value = "/serviceNames")
    public List<String> getAllServiceName() {
        List<String> serviceNames = serviceInfoService.selectAllServiceNames();
        return serviceNames.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @GetMapping(value = "/serviceName")
    public ResponseEntity<String> getServiceName(@RequestParam("serviceId") @NotBlank String serviceIdString) {
        UUID serviceId = UUID.fromString(serviceIdString);
        String serviceName = serviceInfoService.selectServiceName(serviceId);
        if (serviceName == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceName);
    }

    @GetMapping(value = "/serviceId")
    public ResponseEntity<UUID> getServiceId(@RequestParam("serviceName") @NotBlank String serviceName) {
        UUID uuid = serviceInfoService.selectServiceId(serviceName);
        if (uuid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(uuid);
    }

    @GetMapping(value = "/serviceInfo")
    public ResponseEntity<ServiceInfo> getServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceInfo serviceInfo = serviceInfoService.selectServiceInfo(serviceName);
        if (serviceInfo == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceInfo);
    }

    @PostMapping(value = "/serviceInfo")
    public Response insertServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName,
                                      @RequestBody(required = false) Map<String, String> tags) {
        Map<String, String> newTags = addUserId(tags);
        serviceInfoService.insertServiceInfo(serviceName, newTags);
        return SimpleResponse.ok();
    }

    @PutMapping(value = "/serviceInfo")
    public Response updateServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName,
                                      @RequestBody Map<String, String> tags) {
        Map<String, String> newTags = addUserId(tags);
        serviceInfoService.updateServiceInfo(serviceName, newTags);
        return SimpleResponse.ok();
    }

    @DeleteMapping(value = "/serviceInfo")
    public Response deleteServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        serviceInfoService.deleteServiceInfo(serviceName);
        return SimpleResponse.ok();
    }

    private Map<String, String> addUserId(Map<String, String> tags) {
        if (userConfigProperties.isOpenSource()) {
            return tags;
        } else {
            String userId = userService.getUserIdFromSecurity();
            if (StringUtils.isEmpty(userId)) {
                throw new IllegalStateException("no user id found");
            }

            if (tags == null) {
                return Collections.singletonMap("Pinpoint-userId", userId);
            } else {
                Map<String, String> treeMap = new TreeMap<>(tags);
                treeMap.putIfAbsent("Pinpoint-userId", userId);
                return treeMap;
            }
        }
    }
}
