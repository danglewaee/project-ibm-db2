package com.danglewaee.b2bops.order.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SalesOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsDraftOrderWithPersistedLineItems() throws Exception {
        String location = createOrder("""
                {
                  "customerCode": "CUST-ACME",
                  "requestedShipDate": "2026-04-15",
                  "priority": 2,
                  "notes": "First enterprise test order",
                  "lineItems": [
                    { "sku": "SKU-1001", "orderedQty": 4.000 },
                    { "sku": "SKU-1002", "orderedQty": 2.500 }
                  ]
                }
                """);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCode", is("CUST-ACME")))
                .andExpect(jsonPath("$.lineItems[1].sku", is("SKU-1002")))
                .andExpect(jsonPath("$.lineItems[1].reservedQty", is(0.000)))
                .andExpect(jsonPath("$.lineItems[1].status", is("OPEN")));
    }

    @Test
    void rejectsUnknownProductSku() throws Exception {
        String payload = """
                {
                  "customerCode": "CUST-ACME",
                  "requestedShipDate": "2026-04-15",
                  "priority": 2,
                  "lineItems": [
                    { "sku": "SKU-9999", "orderedQty": 1.000 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Active products not found for SKUs: [SKU-9999]")));
    }

    @Test
    void reservesStockAgainstAnExistingOrder() throws Exception {
        String location = createOrder("""
                {
                  "customerCode": "CUST-ACME",
                  "requestedShipDate": "2026-04-15",
                  "priority": 2,
                  "lineItems": [
                    { "sku": "SKU-1001", "orderedQty": 4.000 },
                    { "sku": "SKU-1002", "orderedQty": 2.500 }
                  ]
                }
                """);

        mockMvc.perform(post(location + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseCode": "WH-EAST",
                                  "lineReservations": [
                                    { "sku": "SKU-1001", "reserveQty": 4.000 },
                                    { "sku": "SKU-1002", "reserveQty": 1.500 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehouseCode", is("WH-EAST")))
                .andExpect(jsonPath("$.orderStatus", is("PARTIALLY_ALLOCATED")))
                .andExpect(jsonPath("$.reservations", hasSize(2)))
                .andExpect(jsonPath("$.reservations[0].reservationId").isNumber())
                .andExpect(jsonPath("$.reservations[0].availableQtyAfter", is(21.000)))
                .andExpect(jsonPath("$.reservations[1].itemStatus", is("PARTIALLY_ALLOCATED")));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PARTIALLY_ALLOCATED")))
                .andExpect(jsonPath("$.lineItems[0].reservedQty", is(4.000)))
                .andExpect(jsonPath("$.lineItems[0].status", is("ALLOCATED")))
                .andExpect(jsonPath("$.lineItems[1].reservedQty", is(1.500)))
                .andExpect(jsonPath("$.lineItems[1].status", is("PARTIALLY_ALLOCATED")));
    }

    @Test
    void rejectsReservationWhenAvailableStockIsTooLow() throws Exception {
        String location = createOrder("""
                {
                  "customerCode": "CUST-NOVA",
                  "requestedShipDate": "2026-04-16",
                  "priority": 3,
                  "lineItems": [
                    { "sku": "SKU-1003", "orderedQty": 20.000 }
                  ]
                }
                """);

        mockMvc.perform(post(location + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseCode": "WH-EAST",
                                  "lineReservations": [
                                    { "sku": "SKU-1003", "reserveQty": 7.000 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Insufficient available quantity for SKU SKU-1003 in warehouse WH-EAST")));
    }

    private String createOrder(String payload) throws Exception {
        return mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/orders/SO-\\d{8}")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }
}
