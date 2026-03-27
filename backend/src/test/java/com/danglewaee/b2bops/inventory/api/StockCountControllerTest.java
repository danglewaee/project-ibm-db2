package com.danglewaee.b2bops.inventory.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.danglewaee.b2bops.inventory.persistence.InventoryBalanceRepository;
import com.danglewaee.b2bops.inventory.persistence.StockMovementRepository;
import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
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
class StockCountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryBalanceRepository inventoryBalanceRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Test
    void createsStockCountSessionWithVarianceSnapshot() throws Exception {
        String location = createCountSession("""
                {
                  "warehouseCode": "WH-EAST",
                  "notes": "Cycle count A",
                  "countItems": [
                    { "sku": "SKU-1001", "countedOnHandQty": 23.000, "note": "2 units damaged" },
                    { "sku": "SKU-1002", "countedOnHandQty": 10.000 }
                  ]
                }
                """);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countNumber", matchesPattern("CNT-\\d{8}")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].sku", is("SKU-1001")))
                .andExpect(jsonPath("$.items[0].systemOnHandQty", is(25.000)))
                .andExpect(jsonPath("$.items[0].countedOnHandQty", is(23.000)))
                .andExpect(jsonPath("$.items[0].varianceQty", is(-2.000)))
                .andExpect(jsonPath("$.items[0].status", is("COUNTED")))
                .andExpect(jsonPath("$.items[1].sku", is("SKU-1002")))
                .andExpect(jsonPath("$.items[1].varianceQty", is(0.0)));
    }

    @Test
    void reconcilesCountSessionAndWritesAdjustmentMovements() throws Exception {
        String location = createCountSession("""
                {
                  "warehouseCode": "WH-EAST",
                  "notes": "Cycle count B",
                  "countItems": [
                    { "sku": "SKU-1001", "countedOnHandQty": 23.000 },
                    { "sku": "SKU-1002", "countedOnHandQty": 12.000 }
                  ]
                }
                """);

        long movementCountBefore = stockMovementRepository.count();

        mockMvc.perform(post(location + "/reconcile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countStatus", is("POSTED")))
                .andExpect(jsonPath("$.reconciledLines", hasSize(2)))
                .andExpect(jsonPath("$.reconciledLines[0].sku", is("SKU-1001")))
                .andExpect(jsonPath("$.reconciledLines[0].movementType", is("COUNT_ADJUST_DECREASE")))
                .andExpect(jsonPath("$.reconciledLines[0].onHandQtyAfter", is(23.000)))
                .andExpect(jsonPath("$.reconciledLines[1].sku", is("SKU-1002")))
                .andExpect(jsonPath("$.reconciledLines[1].movementType", is("COUNT_ADJUST_INCREASE")))
                .andExpect(jsonPath("$.reconciledLines[1].onHandQtyAfter", is(12.000)));

        Assertions.assertThat(stockMovementRepository.count())
                .isEqualTo(movementCountBefore + 2);
        Assertions.assertThat(inventoryBalanceRepository
                        .findByWarehouse_WarehouseCodeAndProduct_Sku("WH-EAST", "SKU-1001"))
                .get()
                .extracting(balance -> balance.getOnHandQty())
                .isEqualTo(new BigDecimal("23.000"));
        Assertions.assertThat(inventoryBalanceRepository
                        .findByWarehouse_WarehouseCodeAndProduct_Sku("WH-EAST", "SKU-1002"))
                .get()
                .extracting(balance -> balance.getOnHandQty())
                .isEqualTo(new BigDecimal("12.000"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("POSTED")))
                .andExpect(jsonPath("$.items[0].status", is("RECONCILED")))
                .andExpect(jsonPath("$.items[1].status", is("RECONCILED")));
    }

    @Test
    void rejectsReconciliationWhenSystemQuantityHasChanged() throws Exception {
        String location = createCountSession("""
                {
                  "warehouseCode": "WH-EAST",
                  "countItems": [
                    { "sku": "SKU-1001", "countedOnHandQty": 23.000 }
                  ]
                }
                """);

        var balance = inventoryBalanceRepository
                .findByWarehouse_WarehouseCodeAndProduct_Sku("WH-EAST", "SKU-1001")
                .orElseThrow();
        balance.adjustOnHand(new BigDecimal("-1.000"));
        inventoryBalanceRepository.flush();

        mockMvc.perform(post(location + "/reconcile"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.message",
                        is("System on-hand quantity changed for SKU SKU-1001 in warehouse WH-EAST since count was captured")
                ));
    }

    private String createCountSession(String payload) throws Exception {
        return mockMvc.perform(post("/api/v1/inventory/count-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/inventory/count-sessions/CNT-\\d{8}")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }
}
