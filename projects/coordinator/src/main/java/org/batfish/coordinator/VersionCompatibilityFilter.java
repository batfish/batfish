package org.batfish.coordinator;

import com.google.common.base.Strings;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;

/**
 * This filter verifies that the client's version is compatible with the version of Batfish service.
 */
public class VersionCompatibilityFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String clientVersion = requestContext.getHeaderString(CoordConsts.SVC_KEY_VERSION);
    if (Strings.isNullOrEmpty(clientVersion)) {
      requestContext.abortWith(
          Response.status(Status.PRECONDITION_FAILED)
              .entity("Version is missing or empty")
              .type(MediaType.APPLICATION_JSON)
              .build());
    } else {
      try {
        checkClientVersion(clientVersion);
      } catch (Exception e) {
        requestContext.abortWith(
            Response.status(Status.PRECONDITION_FAILED)
                .entity(e.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build());
      }
    }
  }

  private void checkClientVersion(String clientVersion) throws Exception {
    Version.checkCompatibleVersion("Service", "Client", clientVersion);
  }
}
