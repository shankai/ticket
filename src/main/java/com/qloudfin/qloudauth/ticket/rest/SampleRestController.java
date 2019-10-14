package com.qloudfin.qloudauth.ticket.rest;

import java.util.HashMap;
import java.util.Map;

import com.qloudfin.qloudauth.ticket.util.TicketUtils;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * SampleRestController
 */
@RestController
@Log4j2
public class SampleRestController {

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    private ServicesManager servicesManager;

    @GetMapping("/sample/tgt/{princialId}")
    public ResponseEntity<Object> getTicket(@PathVariable("princialId") String id) {
        // mock attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("title", "manager");

        TicketGrantingTicket tgt = TicketUtils.generateTicketGrantingTicket(id, attributes);
        ticketRegistry.addTicket(tgt);
        return new ResponseEntity<>(tgt.toString(), HttpStatus.OK);
    }

    @GetMapping("/sample/st")
    public ResponseEntity<String> st(@RequestParam("ticket") String ticketId,
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