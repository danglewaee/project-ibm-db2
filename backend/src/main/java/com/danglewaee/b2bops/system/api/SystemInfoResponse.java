package com.danglewaee.b2bops.system.api;

import java.util.List;

public record SystemInfoResponse(
        String application,
        String runtimeMode,
        String persistenceMode,
        String ddlPath,
        List<String> supportedFlows,
        List<String> nextSteps
) {
}
