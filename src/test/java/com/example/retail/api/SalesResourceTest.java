package com.example.retail.api;

import com.example.retail.domain.SaleRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesResource")
class SalesResourceTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<SaleRecord> typedQuery;

    private SalesResource resource;

    @BeforeEach
    void setUp() throws Exception {
        resource = new SalesResource();
        setEntityManager(resource, em);
    }

    private static void setEntityManager(SalesResource target, EntityManager em) throws Exception {
        var field = SalesResource.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(target, em);
    }

    @Test
    @DisplayName("health retorna UP")
    void healthReturnsUp() {
        Response response = resource.health();
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("UP"));
    }

    @Test
    @DisplayName("create retorna BAD_REQUEST quando request é nulo")
    void createRejectsNullRequest() {
        Response response = resource.create(null);
        assertEquals(400, response.getStatus());
        verify(em, never()).persist(any());
    }

    @Test
    @DisplayName("create retorna BAD_REQUEST quando storeId é nulo")
    void createRejectsNullStoreId() {
        SalesResource.SaleRecordRequest request = new SalesResource.SaleRecordRequest();
        request.storeId = null;
        request.saleDate = "2025-03-17";
        request.amount = 100.0;

        Response response = resource.create(request);
        assertEquals(400, response.getStatus());
        verify(em, never()).persist(any());
    }

    @Test
    @DisplayName("create persiste e retorna 201 com o registro")
    void createPersistsAndReturnsCreated() {
        SalesResource.SaleRecordRequest request = new SalesResource.SaleRecordRequest();
        request.storeId = "LOJA-01";
        request.saleDate = "2025-03-17";
        request.amount = 250.0;

        Response response = resource.create(request);
        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof SaleRecord);
        SaleRecord created = (SaleRecord) response.getEntity();
        assertEquals("LOJA-01", created.getStoreId());
        assertEquals(LocalDate.parse("2025-03-17"), created.getSaleDate());
        assertEquals(250.0, created.getAmount());
        verify(em).persist(any(SaleRecord.class));
        verify(em).flush();
    }

    @Test
    @DisplayName("findById retorna NOT_FOUND quando registro não existe")
    void findByIdReturnsNotFoundWhenMissing() {
        when(em.find(SaleRecord.class, 999L)).thenReturn(null);
        Response response = resource.findById(999L);
        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("findById retorna 200 e o registro quando existe")
    void findByIdReturnsRecordWhenExists() {
        SaleRecord record = new SaleRecord();
        record.setStoreId("LOJA-01");
        record.setSaleDate(LocalDate.of(2025, 3, 17));
        record.setAmount(100.0);
        when(em.find(SaleRecord.class, 1L)).thenReturn(record);

        Response response = resource.findById(1L);
        assertEquals(200, response.getStatus());
        assertSame(record, response.getEntity());
    }

    @Test
    @DisplayName("listAll retorna lista da query")
    void listAllReturnsQueryResult() {
        List<SaleRecord> list = Collections.emptyList();
        when(em.createQuery(anyString(), eq(SaleRecord.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(list);

        List<SaleRecord> result = resource.listAll();
        assertSame(list, result);
        verify(em).createQuery(anyString(), eq(SaleRecord.class));
    }
}
