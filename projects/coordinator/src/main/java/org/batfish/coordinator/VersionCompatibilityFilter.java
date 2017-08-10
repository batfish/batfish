package org.batfish.coordinator;

import com.google.common.base.Strings;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;

/**
 * This filter verifies that the client's version is compatible with the version of Batfish service.
 */
@Provider
public class VersionCompatibilityFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String clientVersion = requestContext.getHeaderString(CoordConsts.SVC_KEY_VERSION);
    if (Strings.isNullOrEmpty(clientVersion)) {
      requestContext.abortWith(
          Response.status(Status.BAD_REQUEST)
              .entity(
                  String.format(
                      "HTTP header %s should contain a client version",
                      requestContext.getHeaders()))
              .type(MediaType.APPLICATION_JSON)
              .build());
    } else if (!Version.isCompatibleVersion("Service", "Client", clientVersion)) {
      requestContext.abortWith(
          Response.status(Status.BAD_REQUEST)
              .entity(String.format("Illegal version '%s' for Client", clientVersion))
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }
}
