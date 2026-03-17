package com.example.retail.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaleRecord")
class SaleRecordTest {

    @Test
    @DisplayName("getters e setters refletem os valores definidos")
    void gettersAndSetters() {
        SaleRecord record = new SaleRecord();
        record.setStoreId("LOJA-01");
        record.setSaleDate(LocalDate.of(2025, 3, 17));
        record.setAmount(1500.50);

        assertEquals("LOJA-01", record.getStoreId());
        assertEquals(LocalDate.of(2025, 3, 17), record.getSaleDate());
        assertEquals(1500.50, record.getAmount());
        assertNull(record.getId());
    }

    @Test
    @DisplayName("aceita valores nulos em campos editáveis para atualização parcial")
    void acceptsNullForPartialUpdate() {
        SaleRecord record = new SaleRecord();
        record.setStoreId("LOJA-02");
        record.setSaleDate(null);
        record.setAmount(null);

        assertEquals("LOJA-02", record.getStoreId());
        assertNull(record.getSaleDate());
        assertNull(record.getAmount());
    }
}
