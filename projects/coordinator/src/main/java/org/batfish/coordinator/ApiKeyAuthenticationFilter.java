package org.batfish.coordinator;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.batfish.common.CoordConsts;

/** This filter verifies the apiKey provided in request header is a valid work apikey. */
@Provider
public class ApiKeyAuthenticationFilter implements ContainerRequestFilter {
  @Override
  public void filter(ContainerRequestContext requestContext) {
    String apiKey =
        firstNonNull(
            requestContext.getHeaderString(CoordConsts.SVC_KEY_API_KEY),
            CoordConsts.DEFAULT_API_KEY);
    if (apiKey.isEmpty()) {
      requestContext.abortWith(
          Response.status(Status.UNAUTHORIZED)
              .entity("ApiKey is empty")
              .type(MediaType.APPLICATION_JSON)
              .build());
    } else if (!Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
      requestContext.abortWith(
          Response.status(Status.FORBIDDEN)
              .entity(String.format("Authorizer: %s is NOT a valid key", apiKey))
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

}
