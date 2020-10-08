package com.redhat.developer.demos.recommendation.rest;

import java.io.ByteArrayInputStream;
import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import javax.json.bind.JsonbBuilder;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.redhat.developer.demos.recommendation.rest.RestServiceCall.Services;

import org.jboss.logging.Logger;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;

@Path("/")
public class RecommendationResource {
    private static final String VERSION="v1";

    private static final String RESPONSE_STRING_FORMAT = "recommendation "+VERSION+" from '%s': %d\n";

    private static final String RESPONSE_STRING_NOW_FORMAT = "recommendation "+VERSION+" %s from '%s': %d\n";

    private final Logger logger = Logger.getLogger(getClass());


    @Inject
    @Channel("restServiceCall")
    Emitter<String> restServiceCall;

    /**
     * Counter to help us see the lifecycle
     */
    private int count = 0;

    /**
     * Flag for throwing a 503 when enabled
     */
    private boolean misbehave = false;

    private String HOSTNAME = System.getenv().getOrDefault("HOSTNAME", "unknown");

    @GET
    public Response getRecommendations() {

        count++;
        logger.info(String.format("recommendation request from %s: %d", HOSTNAME, count));

        writeLogMessage(String.valueOf(count));
        // timeout();

        logger.debug("recommendation service ready to return");
        if (misbehave) {
            return doMisbehavior();
        }
        return Response.ok(String.format(RESPONSE_STRING_FORMAT, HOSTNAME, count)).build();
        // return Response.ok(String.format(RESPONSE_STRING_NOW_FORMAT, getNow(), HOSTNAME, count)).build();
    }

    private void timeout() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.info("Thread interrupted");
        }
    }

    private Response doMisbehavior() {
        logger.debug(String.format("Misbehaving %d", count));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(String.format("recommendation misbehavior from '%s'\n", HOSTNAME)).build();
    }

    @GET
    @Path("/misbehave")
    public Response flagMisbehave() {
        this.misbehave = true;
        logger.debug("'misbehave' has been set to 'true'");
        return Response.ok("Following requests to / will return a 503\n").build();
    }

    @GET
    @Path("/behave")
    public Response flagBehave() {
        this.misbehave = false;
        logger.debug("'misbehave' has been set to 'false'");
        return Response.ok("Following requests to / will return 200\n").build();
    }

    private String getNow() {
        final Client client = ClientBuilder.newClient();
        final Response res = client.target("http://worldclockapi.com/api/json/cet/now").request().get();
        final String jsonObject = res.readEntity(String.class);
        return Json.createReader(new ByteArrayInputStream(jsonObject.getBytes())).readObject().getString("currentDateTime");
    }

    private void writeLogMessage(String comment) {
        RestServiceCall serviceCall= new RestServiceCall(Services.RECOMMENDATION,null,VERSION,comment);
        String out = JsonbBuilder.create().toJson(serviceCall);
        logger.info("RestServiceCall"+ out);
        KafkaRecord<Integer, String> msg = KafkaRecord.of(1, out);
        restServiceCall.send(msg);
    }

}