package com.qloudfin.qloudauth.ticket.rest;

import com.qloudfin.qloudauth.ticket.util.TicketUtils;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

/**
 * ValidationRestController
 */
@RestController
@Log4j2
public class ValidationRestController {

    @Autowired
    CentralAuthenticationService centralAuthenticationService;

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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
        return new ResponseEntity<>("true", HttpStatus.OK);
    }
}