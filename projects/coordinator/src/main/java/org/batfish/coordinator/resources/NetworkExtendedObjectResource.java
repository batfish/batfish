package org.batfish.coordinator.resources;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.Main;

@ParametersAreNonnullByDefault
public final class NetworkExtendedObjectResource {

  private final String _network;

  private final URI _uri;

  public NetworkExtendedObjectResource(String network, URI uri) {
    _network = network;
    _uri = uri;
  }

  @DELETE
  public Response delete() {
    boolean deleted;
    try {
      deleted = Main.getWorkMgr().deleteNetworkExtendedObject(_network, _uri);
    } catch (IOException e) {
      throw new InternalServerErrorException(Throwables.getStackTraceAsString(e));
    }
    if (deleted) {
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response get() {
    InputStream inputStream;
    try {
      inputStream = Main.getWorkMgr().getNetworkExtendedObject(_network, _uri);
    } catch (IOException e) {
      throw new InternalServerErrorException(Throwables.getStackTraceAsString(e));
    }
    if (inputStream == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    String filename = Paths.get(_uri).getFileName().toString();
    return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
        .header(CoordConsts.SVC_FILENAME_HDR, filename)
        .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  public Response put(InputStream inputStream) {
    try {
      Main.getWorkMgr().putNetworkExtendedObject(inputStream, _network, _uri);
    } catch (IOException e) {
      throw new InternalServerErrorException(Throwables.getStackTraceAsString(e));
    }
    return Response.ok().build();
  }
}
