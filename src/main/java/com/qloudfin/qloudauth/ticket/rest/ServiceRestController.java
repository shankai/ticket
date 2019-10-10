package com.qloudfin.qloudauth.ticket.rest;

import java.util.Collection;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ServiceRestController
 */
@RestController
public class ServiceRestController {

    @Autowired
    private ServicesManager servicesManager;

    @RequestMapping("/services")
    public ResponseEntity<Object> getRegexRegisteredServices() {
        Collection<RegisteredService> services = servicesManager.getAllServices();
        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @RequestMapping("/service/{id}")
    public ResponseEntity<RegisteredService> getServiceById(@PathVariable("id") long id) {
        RegisteredService service = servicesManager.findServiceBy(id);
        return new ResponseEntity<>(service, HttpStatus.OK);
    }

}