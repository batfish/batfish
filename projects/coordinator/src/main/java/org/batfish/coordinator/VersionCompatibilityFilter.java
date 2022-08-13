package org.batfish.coordinator;

import static org.batfish.common.CoordConstsV2.HTTP_HEADER_BATFISH_VERSION;

import com.google.common.base.Strings;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.batfish.common.CoordConsts;

/** This filter verifies that the client supplied a version. */
@PreMatching
@Provider
public class VersionCompatibilityFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (requestContext.getUriInfo().getPath().startsWith(WHITELISTED_PATH_PREFIX)) {
      // Seems dumb to require a declared version to fetch supported API versions
      // TODO: Should we just delete this class? We don't use the header value.
      return;
    }
    String clientVersion = requestContext.getHeaderString(HTTP_HEADER_BATFISH_VERSION);
    if (Strings.isNullOrEmpty(clientVersion)) {
      requestContext.abortWith(
          Response.status(Status.BAD_REQUEST)
              .entity(
                  String.format(
                      "HTTP header %s should contain a client version",
                      HTTP_HEADER_BATFISH_VERSION))
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  private static final String WHITELISTED_PATH_PREFIX =
      CoordConsts.SVC_CFG_API_VERSION.substring(1);
}
