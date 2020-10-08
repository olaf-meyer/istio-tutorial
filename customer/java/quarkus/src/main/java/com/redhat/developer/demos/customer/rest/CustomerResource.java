package com.redhat.developer.demos.customer.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.JsonbBuilder;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.developer.demos.customer.rest.RestServiceCall.Services;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;

@Path("/")
public class CustomerResource {

    private static final String VERSION="v1";

    private static final String RESPONSE_STRING_FORMAT = "customer => %s\n";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @RestClient
    PreferenceService preferenceService;

    
    @Inject
    @Channel("restServiceCall")
    Emitter<String> restServiceCall;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCustomer() {
        try {
            String response = preferenceService.getPreference().trim();
            writeLogMessage(response);
            return Response.ok(String.format(RESPONSE_STRING_FORMAT, response)).build();
        } catch (WebApplicationException ex) {
            Response response = ex.getResponse();
            logger.warn("Non HTTP 20x trying to get the response from preference service: " + response.getStatus());
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(String.format(RESPONSE_STRING_FORMAT,
                            String.format("Error: %d - %s", response.getStatus(), response.readEntity(String.class)))
                    )
                    .build();

        } catch (ProcessingException ex) {
            logger.warn("Exception trying to get the response from preference service.", ex);
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(String.format(RESPONSE_STRING_FORMAT, ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage()))
                    .build();
        }
    }

    private void writeLogMessage(String comment) {
        RestServiceCall serviceCall= new RestServiceCall(Services.CUSTOMER,Services.PREFERENCE,VERSION,comment);
        String out = JsonbBuilder.create().toJson(serviceCall);
        logger.info("RestServiceCall"+ out);
        KafkaRecord<Integer, String> msg = KafkaRecord.of(1, out);
        restServiceCall.send(msg);
    }
}