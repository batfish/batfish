package org.batfish.coordinator.resources;

import java.security.AccessControlException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;

/**
 * This filter verify the access permissions for a user based on apiKey and version provided in
 * request header.
 */
@Provider public class AuthenticationFilter implements ContainerRequestFilter {
  @Override public void filter(ContainerRequestContext requestContext) {
    String apiKey = CoordConsts.DEFAULT_API_KEY;
    if (requestContext.getHeaders().containsKey(CoordConsts.SVC_KEY_API_KEY)) {
      apiKey = requestContext.getHeaderString(CoordConsts.SVC_KEY_API_KEY);
    }

    String clientVersion = requestContext.getHeaderString(CoordConsts.SVC_KEY_VERSION);

    // TODO: provides default version? Assume right version now
    if (clientVersion == null) {
      clientVersion = Version.getVersion();
    }

    try {
      // Don't need to check now since we provide value for null inputs
      checkStringParam(apiKey, CoordConsts.SVC_KEY_API_KEY);
      checkStringParam(clientVersion, CoordConsts.SVC_KEY_VERSION);

      checkApiKeyValidity(apiKey);
      checkClientVersion(clientVersion);

    } catch (Exception e) {
      requestContext.abortWith(Response.status(Status.BAD_REQUEST)
          .entity(e.getMessage())
          .type(MediaType.APPLICATION_JSON)
          .build());
    }

  }

  private void checkStringParam(String paramStr, String parameterName) {
    if (paramStr == null || paramStr.equals("")) {
      throw new IllegalArgumentException(parameterName + " is missing or empty");
    }
  }

  private void checkApiKeyValidity(String apiKey) throws Exception {
    if (!Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
      throw new AccessControlException("Invalid API key: " + apiKey);
    }
  }

  private void checkClientVersion(String clientVersion) throws Exception {
    Version.checkCompatibleVersion("Service", "Client", clientVersion);
  }

}