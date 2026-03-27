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
                        "ship-order-partially-or-fully",
                        "count-stock-and-reconcile"
                ),
                List.of(
                        "expand persistence to shipments and stock movements",
                        "wire db/db2-schema.sql into a migration strategy",
                        "switch from local H2 to the db2 profile in IBM environments"
                )
        );
    }
}
