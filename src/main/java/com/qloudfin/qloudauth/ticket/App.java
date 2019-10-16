package com.qloudfin.qloudauth.ticket;

import com.qloudfin.qloudauth.ticket.rest.TicketRestController;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import lombok.extern.log4j.Log4j2;


/**
 * App
 */
@Log4j2
public class App {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ap = new AnnotationConfigApplicationContext("com.qloudfin.qloudauth.ticket");
        Object bean = ap.getBean("ticketRegistry");
        log.info(bean);

        TicketRegistry tg = (TicketRegistry)bean;
        log.info(tg.getTickets());

    }
}