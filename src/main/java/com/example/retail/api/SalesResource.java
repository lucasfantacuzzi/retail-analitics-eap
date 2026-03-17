package com.example.retail.api;

import com.example.retail.domain.SaleRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Path("/sales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesResource {

    @PersistenceContext
    private EntityManager em;

    public static class SaleRecordRequest {
        public String storeId;
        public String saleDate; // yyyy-MM-dd
        public Double amount;
    }

    @GET
    public List<SaleRecord> listAll() {
        return em.createQuery("SELECT s FROM SaleRecord s ORDER BY s.saleDate DESC", SaleRecord.class)
                .getResultList();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        SaleRecord record = em.find(SaleRecord.class, id);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(record).build();
    }

    @POST
    @Transactional
    public Response create(SaleRecordRequest request) {
        if (request == null || request.storeId == null || request.saleDate == null || request.amount == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"storeId, saleDate and amount are required\"}")
                    .build();
        }

        SaleRecord record = new SaleRecord();
        record.setStoreId(request.storeId);
        record.setSaleDate(LocalDate.parse(request.saleDate));
        record.setAmount(request.amount);

        em.persist(record);
        em.flush();

        return Response.created(URI.create("/api/sales/" + record.getId()))
                .entity(record)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, SaleRecordRequest request) {
        SaleRecord record = em.find(SaleRecord.class, id);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (request.storeId != null) {
            record.setStoreId(request.storeId);
        }
        if (request.saleDate != null) {
            record.setSaleDate(LocalDate.parse(request.saleDate));
        }
        if (request.amount != null) {
            record.setAmount(request.amount);
        }

        em.merge(record);
        return Response.ok(record).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        SaleRecord record = em.find(SaleRecord.class, id);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.remove(record);
        return Response.noContent().build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\":\"UP\"}").build();
    }
}

