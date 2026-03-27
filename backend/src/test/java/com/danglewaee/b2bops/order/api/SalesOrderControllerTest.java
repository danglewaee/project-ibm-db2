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
        String payload = """
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
                """;

        String location = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/orders/SO-\\d{8}")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.customerCode", is("CUST-ACME")))
                .andExpect(jsonPath("$.lineItems", hasSize(2)))
                .andExpect(jsonPath("$.lineItems[0].lineNumber", is(1)))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCode", is("CUST-ACME")))
                .andExpect(jsonPath("$.lineItems[1].sku", is("SKU-1002")))
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
}
