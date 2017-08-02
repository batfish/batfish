package org.batfish.coordinator.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.CoordConsts;
import org.batfish.common.Testrig;
import org.batfish.coordinator.Main;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * The TestrigResource is a RESTful service for servicing client API calls at testrig level.
 *
 * <p>The TestrigResource provides information about specified testrigs, and provides
 * the ability to create/delete specified testrigs for authenticated clients.
 *
 * <p>The TestrigResource also provides the access entry for client API calls at testrig level.
 */
@Produces(MediaType.APPLICATION_JSON)
public class TestrigResource {

  private UriInfo _uriInfo;
  private String _containerName;
  private String _name;

  public TestrigResource(UriInfo uriInfo, String containerName, String name) {
    _uriInfo = uriInfo;
    _containerName = containerName;
    _name = name;
  }

  @GET
  public Response getTestrig() {
    validates();
    String configsUri = Main.getWorkMgr().getConfigsUri(_containerName, _uriInfo, _name).toString();
    String hostsUri = Main.getWorkMgr().getHostsUri(_containerName, _uriInfo, _name).toString();
    Testrig testrig = Testrig.makeTestrig(_name, configsUri, hostsUri);
    return Response.ok(testrig).build();
  }

  @DELETE
  public Response delTestrig() {
    validates();
    Main.getWorkMgr().delTestrig(_containerName, _name);
    return Response.ok("Testrig '" + _name + "' deleted").build();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response initTestrig(
      @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream) {
    Main.getWorkMgr().uploadTestrig(_containerName, _name, fileStream);
    return Response.ok("Testrig '" + _name + "' uploaded").build();
  }

  private void validates() {
    if (!Main.getWorkMgr().checkTestrigExist(_containerName, _name)) {
      throw new NotFoundException("Testrig '" + _name + "' does not exist");
    }
  }

}
