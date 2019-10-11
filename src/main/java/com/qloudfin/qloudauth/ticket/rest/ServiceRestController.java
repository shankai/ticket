package com.qloudfin.qloudauth.ticket.rest;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

/**
 * ServiceRestController
 */
@RestController
@Log4j2
public class ServiceRestController {

    @Autowired
    private ServicesManager servicesManager;

    @RequestMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getRegexRegisteredServices() {
        Collection<RegisteredService> services = servicesManager.getAllServices();
        try {
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(services), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/service/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getServiceById(@PathVariable("id") long id) {
        RegisteredService service = servicesManager.findServiceBy(id);
        try {
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(service), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}