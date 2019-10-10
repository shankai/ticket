package com.qloudfin.qloudauth.ticket.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.qloudfin.qloudauth.ticket.util.TicketUtils;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * TicketRestController
 */
@RestController
@Log4j2
@Setter
public class TicketRestController {

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    private ServicesManager servicesManager;

    @RequestMapping("/tgt/{princialId}")
    public ResponseEntity<Object> getTicket(@PathVariable("princialId") String id) {
        // mock attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("title", "manager");
        attributes.put("alias1", id);

        TicketGrantingTicket tgt = TicketUtils.generateTicketGrantingTicket(id, attributes);
        ticketRegistry.addTicket(tgt);
        return new ResponseEntity<>(tgt.toString(), HttpStatus.OK);
    }

    @RequestMapping("/tickets")
    public ResponseEntity<Object[]> getTickets() {
        Collection<? extends Ticket> tickets = ticketRegistry.getTickets();
        Object[] results = tickets.stream().map(ticket -> ticket.toString()).toArray();
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @RequestMapping("/st/{tgtId}")
    public ResponseEntity<String> grantServiceTicket(@PathVariable("tgtId") String ticketId,
            @RequestParam("service") String serviceId) {

        // retrieve tgt
        TicketGrantingTicket tgt = ticketRegistry.getTicket(ticketId, TicketGrantingTicket.class);
        if (tgt == null) {
            log.error("TicketGrantingTicket not found");
            return new ResponseEntity<>("TicketGrantingTicket not found.", HttpStatus.UNAUTHORIZED);
        }

        log.debug("Retrieve tgt: {} for apply service ticket, princial attributes: {}", tgt.getId(),
                tgt.getAuthentication().getAttributes());

        // generate application service
        Service selectedService = TicketUtils.generateService(serviceId);
        log.debug("Match Application Service: {} for apply service ticket", selectedService.getId());

        val registeredService = this.servicesManager.findServiceBy(selectedService);
        RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(registeredService, selectedService, tgt,
                true);

        val audit = AuditableContext.builder().service(selectedService).authentication(tgt.getAuthentication())
                .registeredService(registeredService).retrievePrincipalAttributesFromReleasePolicy(Boolean.FALSE)
                .build();
        val accessResult = new RegisteredServiceAccessStrategyAuditableEnforcer().execute(audit);

        // accessResult.throwExceptionIfNeeded();
        if (accessResult.isExecutionFailure()) {
            log.error("accessResult is failure");
            return new ResponseEntity<>("accessResult is failure", HttpStatus.UNAUTHORIZED);
        }

        // produce service ticket
        ServiceTicket st = TicketUtils.generateServiceTicket((TicketGrantingTicket) tgt, selectedService);

        // register and update ticket
        ticketRegistry.updateTicket(tgt);
        ticketRegistry.addTicket(st);

        return new ResponseEntity<>(st.getId(), HttpStatus.OK);
    }

}