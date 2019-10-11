package com.qloudfin.qloudauth.ticket.util;

import java.time.ZonedDateTime;
import java.util.Map;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.val;

/**
 * TicketUtils
 */
public class TicketUtils {

    public static final ExpirationPolicy EXP_POLICY = new HardTimeoutExpirationPolicy(1000); // 1000s
    public static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    private static final String TICKET_GRANTING_TICKET_PREFIX = "TGT";
    private static final String SERVICE_TICKET_PREFIX = "ST";

    public static SimpleWebApplicationServiceImpl generateService(String serviceId) {
        SimpleWebApplicationServiceImpl service = new SimpleWebApplicationServiceImpl();
        service.setId(serviceId);
        service.setOriginalUrl(serviceId);
        service.setArtifactId(null);
        return service;
    }

    public static ServiceTicket generateServiceTicket(TicketGrantingTicket tgt, Service service) {
        ServiceTicket st = new ServiceTicketImpl(GENERATOR.getNewTicketId(SERVICE_TICKET_PREFIX), tgt, service, true,
                EXP_POLICY);
        return st;
    }

    public static TicketGrantingTicket generateTicketGrantingTicket(final String id,
            final Map<String, Object> attributes) {

        Principal principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(id, attributes);

        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        Authentication authentication = new DefaultAuthenticationBuilder(principal).addCredential(meta)
                .setAuthenticationDate(ZonedDateTime.now())
                .addSuccess("testHandler", new DefaultAuthenticationHandlerExecutionResult()).setAttributes(attributes)
                .build();

        TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl(
                GENERATOR.getNewTicketId(TICKET_GRANTING_TICKET_PREFIX), authentication, EXP_POLICY);
        return tgt;
    }
}