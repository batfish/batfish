package org.batfish.coordinator.resources;

import java.io.InputStream;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
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

  private BatfishLogger _logger = Main.getLogger();

  private UriInfo _uriInfo;
  private String _containerName;
  private String _name;

  public TestrigResource(UriInfo uriInfo, String containerName, String name) {
    _uriInfo = uriInfo;
    _containerName = containerName;
    _name = name;
  }

  /** Returns information about the given {@link Testrig}. */
  @GET
  public Response getTestrig() {
    _logger.info("WMS: getTestrig: '" + _name + "'\n");
    validates();
    List<String> configs = null;
    Testrig testrig = Testrig.of(_name, configs);
    return Response.ok(testrig).build();
  }

  /** Delete the specified Testrig with name: {@link #_name}. */
  @DELETE
  public Response delTestrig() {
    _logger.info("WMS: delTestrig: '" + _name + "'\n");
    validates();
    Main.getWorkMgr().delTestrig(_containerName, _name);
    return Response.noContent().build();
  }

  /** Upload a testrig InputStream {@code fileStream} to the server. */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadTestrig(
      @FormDataParam(CoordConsts.SVC_KEY_ZIPFILE) InputStream fileStream) {
    _logger.info("WMS: uploadTestrig: '" + _name + "'\n");
    Main.getWorkMgr().uploadTestrig(_containerName, _name, fileStream);
    return Response.created(_uriInfo.getRequestUri()).build();
  }

  /** Validate that the Testrig {@link #_name} exists in Container {@link #_containerName}. */
  private void validates() {
    if (!Main.getWorkMgr().checkTestrigExist(_containerName, _name)) {
      throw new NotFoundException("Testrig '" + _name + "' does not exist");
    }
  }

}
