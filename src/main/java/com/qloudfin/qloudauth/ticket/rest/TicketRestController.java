package com.qloudfin.qloudauth.ticket.rest;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qloudfin.qloudauth.ticket.util.TicketUtils;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    CentralAuthenticationService centralAuthenticationService;

    @Autowired
    AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @GetMapping(value = "/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTickets() {
        Collection<? extends Ticket> tickets = ticketRegistry.getTickets();
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
            @RequestParam(value = "pwd", required = true) String pwd, final HttpServletRequest request,
            final HttpServletResponse response) {

        // Service selectedService =
        // TicketUtils.generateService("http://www.baidu.com");
        Credential c = new UsernamePasswordCredential(username, pwd);
        val authenticationResult = authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(null,
                c);
        try {
            val ticketGrantingTicket = this.centralAuthenticationService
                    .createTicketGrantingTicket(authenticationResult);
            log.info("TicketGrantingTicket Id: {}", ticketGrantingTicket.getId());

            ticketGrantingTicketCookieGenerator.addCookie(request, response, ticketGrantingTicket.getId());

            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(ticketGrantingTicket), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/serviceTicket", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> grantServiceTicket(@RequestParam("ticket") String ticketId,
            @RequestParam("service") String serviceId) {
        TicketGrantingTicket tgt = ticketRegistry.getTicket(ticketId, TicketGrantingTicket.class);
        Service selectedService = TicketUtils.generateService(serviceId);

        AuthenticationResult authenticationResult = new DefaultAuthenticationResult(tgt.getAuthentication(),
                selectedService);
        // grant service ticket & add/update ticket registry
        ServiceTicket serviceTicket = this.centralAuthenticationService.grantServiceTicket(ticketId, selectedService,
                authenticationResult);
        try {
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(serviceTicket), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/validate")
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

    @GetMapping(value = "/proxyGrantingTicket", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> proxyGrantingTicket(@RequestParam("ticket") String serviceTicketId) {

        ServiceTicket serviceTicket = this.ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);
        log.debug("Service Ticket: {}", serviceTicket);
        AuthenticationResult authenticationResult = new DefaultAuthenticationResult(
                serviceTicket.getTicketGrantingTicket().getAuthentication(), serviceTicket.getService());

        log.debug("AuthenticationResult: {}", authenticationResult);
        // create pgt & add/update ticket registry
        ProxyGrantingTicket proxyGrantingTicket = this.centralAuthenticationService
                .createProxyGrantingTicket(serviceTicketId, authenticationResult);

        try {
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(proxyGrantingTicket), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/proxyTicket", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> grantProxyTicket(@RequestParam("pgtId") String pgtId,
            @RequestParam("targetService") String targetServiceId) {

        Service targetService = TicketUtils.generateService(targetServiceId);
        // grant proxy ticket & add/update ticket registry
        ProxyTicket proxyTicket = this.centralAuthenticationService.grantProxyTicket(pgtId, targetService);
        try {
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(proxyTicket), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}