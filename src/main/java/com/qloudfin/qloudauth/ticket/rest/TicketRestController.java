package com.qloudfin.qloudauth.ticket.rest;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qloudfin.qloudauth.ticket.util.TicketUtils;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * TicketRestController
 */
@RestController
@Log4j2
public class TicketRestController {

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Autowired
    CentralAuthenticationService centralAuthenticationService;

    @Autowired
    AuthenticationSystemSupport authenticationSystemSupport;

    @RequestMapping(value = "/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTickets() {
        Collection<? extends Ticket> tickets = ticketRegistry.getTickets();
        // Object[] results = tickets.stream().map(ticket ->
        // ticket.toString()).toArray();
        try {
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(tickets), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/ticketGrantingTicket", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> grantTicketGrantingTicket(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "pwd", required = true) String pwd) {

        Service selectedService = TicketUtils.generateService("http://www.baidu.com");

        Credential c = new UsernamePasswordCredential(username, pwd);
        val result = authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(selectedService, c);
        try {

            val factory = (TicketGrantingTicketFactory) this.ticketFactory.get(TicketGrantingTicket.class);
            val ticketGrantingTicket = factory.create(result.getAuthentication(), TicketGrantingTicket.class);
            ticketRegistry.addTicket(ticketGrantingTicket);

            log.info("TicketGrantingTicket Id: {}", ticketGrantingTicket.getId());

            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(ticketGrantingTicket), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/serviceTicket", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> grantServiceTicket(@RequestParam("ticket") String ticketId,
            @RequestParam("service") String serviceId) {
        TicketGrantingTicket tgt = ticketRegistry.getTicket(ticketId, TicketGrantingTicket.class);
        Service selectedService = TicketUtils.generateService("http://www.baidu.com");

        ServiceTicket serviceTicket = tgt.grantServiceTicket(TicketUtils.GENERATOR.getNewTicketId("ST"), selectedService, TicketUtils.EXP_POLICY, true, false);
        try {
            ticketRegistry.addTicket(serviceTicket);
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(serviceTicket), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/validate")
    public ResponseEntity<String> validateServiceTicket(@RequestParam("ticket") String serviceTicketId,
            @RequestParam("service") String serviceId) {
        log.debug("Service Ticket Validation, parameters: {}, {}", serviceTicketId, serviceId);
        try {
            Service selectedService = TicketUtils.generateService(serviceId);
            log.debug("Service Ticket Validation, SelectedService: {}", selectedService);
            Assertion assertion = centralAuthenticationService.validateServiceTicket(serviceTicketId, selectedService);
            log.debug("Service Ticket Validation Assertion: {}", assertion);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("error", HttpStatus.OK);
        }
        return new ResponseEntity<>("true", HttpStatus.OK);
    }
}