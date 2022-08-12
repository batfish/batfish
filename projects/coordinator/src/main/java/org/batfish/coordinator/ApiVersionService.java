package org.batfish.coordinator;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.batfish.common.CoordConsts;

/** A RESTful service for providing the current API version(s). */
@Path(CoordConsts.SVC_CFG_API_VERSION)
public final class ApiVersionService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public @Nonnull ApiVersions getVersions() {
    return ApiVersions.instance();
  }
}
