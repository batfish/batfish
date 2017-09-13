package org.batfish.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;

/** This filter adds default authentication information (apikey and version) to HTTP headers for
 * every HTTP request. */
@Provider
public class BfClientRequestFilter implements ClientRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) {
    requestContext.getHeaders().add(CoordConsts.SVC_KEY_API_KEY, CoordConsts.DEFAULT_API_KEY);
    requestContext.getHeaders().add(CoordConsts.SVC_KEY_VERSION, Version.getVersion());
  }
}
