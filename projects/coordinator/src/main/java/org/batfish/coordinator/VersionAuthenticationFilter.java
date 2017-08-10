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
 * This filter verify the access permissions for a user based on version provided in request header.
 */
public class VersionAuthenticationFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String clientVersion = requestContext.getHeaderString(CoordConsts.SVC_KEY_VERSION);
    try {
      if (Strings.isNullOrEmpty(clientVersion)) {
        throw new IllegalArgumentException("version is missing or empty");
      }

      checkClientVersion(clientVersion);

    } catch (Exception e) {
      requestContext.abortWith(
          Response.status(Status.BAD_REQUEST)
              .entity(e.getMessage())
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  private void checkClientVersion(String clientVersion) throws Exception {
    Version.checkCompatibleVersion("Service", "Client", clientVersion);
  }
}
