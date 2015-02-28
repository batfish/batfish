package org.batfish.main;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/form")
@Produces("text/html")
public class Service {

    @GET
    @Produces("text/html")
    public String getForm() {
       return "sful 2";
    }

    @GET
    @Path("getsettings")
    @Produces("text/plain")
    public String getSettings() {
       return "sful";
    }
}
