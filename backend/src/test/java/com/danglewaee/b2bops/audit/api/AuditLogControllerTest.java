package com.danglewaee.b2bops.audit.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsOrderLifecycleAuditTrailByCorrelationId() throws Exception {
        String location = createOrder("""
                {
                  "customerCode": "CUST-ACME",
                  "requestedShipDate": "2026-04-20",
                  "priority": 1,
                  "lineItems": [
                    { "sku": "SKU-1001", "orderedQty": 4.000 },
                    { "sku": "SKU-1002", "orderedQty": 2.500 }
                  ]
                }
                """);
        String orderNumber = location.substring(location.lastIndexOf('/') + 1);

        String reserveResponse = reserveOrder(location, """
                {
                  "warehouseCode": "WH-EAST",
                  "lineReservations": [
                    { "sku": "SKU-1001", "reserveQty": 4.000 },
                    { "sku": "SKU-1002", "reserveQty": 1.500 }
                  ]
                }
                """);

        Long reservationOne = ((Number) JsonPath.read(
                reserveResponse,
                "$.reservations[0].reservationId"
        )).longValue();
        Long reservationTwo = ((Number) JsonPath.read(
                reserveResponse,
                "$.reservations[1].reservationId"
        )).longValue();

        mockMvc.perform(post(location + "/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseCode": "WH-EAST",
                                  "shipmentLines": [
                                    { "reservationId": %d, "shipQty": 4.000 },
                                    { "reservationId": %d, "shipQty": 1.500 }
                                  ]
                                }
                                """.formatted(reservationOne, reservationTwo)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("correlationId", orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(3)))
                .andExpect(jsonPath("$.entries[0].entityType", is("SALES_ORDER")))
                .andExpect(jsonPath("$.entries[0].action", is("INSERT")))
                .andExpect(jsonPath("$.entries[0].afterState.orderNumber", is(orderNumber)))
                .andExpect(jsonPath("$.entries[1].entityType", is("SALES_ORDER")))
                .andExpect(jsonPath("$.entries[1].action", is("RESERVE")))
                .andExpect(jsonPath("$.entries[1].afterState.orderStatus", is("PARTIALLY_ALLOCATED")))
                .andExpect(jsonPath("$.entries[2].entityType", is("SHIPMENT")))
                .andExpect(jsonPath("$.entries[2].action", is("SHIP")))
                .andExpect(jsonPath("$.entries[2].afterState.shipmentNumber", matchesPattern("SHP-\\d{8}")))
                .andExpect(jsonPath("$.entries[2].correlationId", is(orderNumber)));
    }

    @Test
    void returnsCancelledOrderAuditTrailByCorrelationId() throws Exception {
        String location = createOrder("""
                {
                  "customerCode": "CUST-ACME",
                  "requestedShipDate": "2026-04-21",
                  "priority": 2,
                  "lineItems": [
                    { "sku": "SKU-1001", "orderedQty": 4.000 }
                  ]
                }
                """);
        String orderNumber = location.substring(location.lastIndexOf('/') + 1);

        reserveOrder(location, """
                {
                  "warehouseCode": "WH-EAST",
                  "lineReservations": [
                    { "sku": "SKU-1001", "reserveQty": 4.000 }
                  ]
                }
                """);

        mockMvc.perform(post(location + "/cancel"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("correlationId", orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(3)))
                .andExpect(jsonPath("$.entries[0].action", is("INSERT")))
                .andExpect(jsonPath("$.entries[1].action", is("RESERVE")))
                .andExpect(jsonPath("$.entries[2].entityType", is("SALES_ORDER")))
                .andExpect(jsonPath("$.entries[2].action", is("CANCEL")))
                .andExpect(jsonPath("$.entries[2].afterState.orderStatus", is("CANCELLED")))
                .andExpect(jsonPath("$.entries[2].afterState.releasedReservations", hasSize(1)))
                .andExpect(jsonPath("$.entries[2].correlationId", is(orderNumber)));
    }

    @Test
    void returnsCountLifecycleAuditTrailByCorrelationId() throws Exception {
        String location = createCountSession("""
                {
                  "warehouseCode": "WH-EAST",
                  "countItems": [
                    { "sku": "SKU-1001", "countedOnHandQty": 23.000 }
                  ]
                }
                """);
        String countNumber = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(post(location + "/reconcile"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("correlationId", countNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.entries[0].entityType", is("STOCK_COUNT_SESSION")))
                .andExpect(jsonPath("$.entries[0].action", is("COUNT")))
                .andExpect(jsonPath("$.entries[0].afterState.countNumber", is(countNumber)))
                .andExpect(jsonPath("$.entries[1].entityType", is("STOCK_COUNT_SESSION")))
                .andExpect(jsonPath("$.entries[1].action", is("RECONCILE")))
                .andExpect(jsonPath("$.entries[1].afterState.countStatus", is("POSTED")))
                .andExpect(jsonPath("$.entries[1].correlationId", is(countNumber)));
    }

    private String createOrder(String payload) throws Exception {
        return mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/orders/SO-\\d{8}")))
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }

    private String reserveOrder(String location, String payload) throws Exception {
        return mockMvc.perform(post(location + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String createCountSession(String payload) throws Exception {
        return mockMvc.perform(post("/api/v1/inventory/count-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/inventory/count-sessions/CNT-\\d{8}")))
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }
}
