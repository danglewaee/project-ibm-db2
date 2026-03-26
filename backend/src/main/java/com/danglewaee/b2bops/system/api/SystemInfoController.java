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

    @Value("${app.ddl-path}")
    private String ddlPath;

    @GetMapping("/info")
    public SystemInfoResponse getInfo() {
        return new SystemInfoResponse(
                applicationName,
                runtimeMode,
                "planned-db2-jpa",
                ddlPath,
                List.of(
                        "create-order-draft",
                        "reserve-stock-by-warehouse",
                        "ship-order-partially-or-fully",
                        "count-stock-and-reconcile"
                ),
                List.of(
                        "replace stub sales order service with Db2 persistence",
                        "add reservation and shipment APIs",
                        "wire db/db2-schema.sql into migration strategy"
                )
        );
    }
}
