package com.danglewaee.b2bops.system.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemInfoController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.runtime-mode}")
    private String runtimeMode;

    @Value("${app.persistence-mode}")
    private String persistenceMode;

    @Value("${app.ddl-path}")
    private String ddlPath;

    @GetMapping("/info")
    public SystemInfoResponse getInfo() {
        return new SystemInfoResponse(
                applicationName,
                runtimeMode,
                persistenceMode,
                ddlPath,
                List.of(
                        "create-order-draft",
                        "lookup-order-by-order-number",
                        "reserve-stock-by-warehouse",
                        "cancel-order-and-release-reservations",
                        "ship-order-partially-or-fully",
                        "count-stock-and-reconcile",
                        "lookup-audit-trail-by-correlation-id"
                ),
                List.of(
                        "run the same flow set against a live Db2 instance",
                        "package the backend for IBM Cloud Code Engine",
                        "add smoke checks for migration + startup in db2 profile"
                )
        );
    }
}
