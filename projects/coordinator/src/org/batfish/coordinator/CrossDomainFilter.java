package org.batfish.coordinator;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * Allow the system to serve xhr level 2 from all cross domain site
 */
@Provider
public class CrossDomainFilter implements ContainerResponseFilter {
   /**
    * Add the cross domain data to the output if needed
    *
    * @param creq
    *           The container request (input)
    * @param cres
    *           The container request (output)
    * @return The output request with cross domain if needed
    */
   @Override
   public void filter(ContainerRequestContext creq,
         ContainerResponseContext cres) {
      cres.getHeaders().add("Access-Control-Allow-Origin", "*");
      cres.getHeaders().add("Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization");
      cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
      cres.getHeaders().add("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
      // cres.getHeaders().add("Access-Control-Max-Age", "");

      // response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
      // response.getHttpHeaders().add("Access-Control-Allow-Headers",
      // "origin, content-type, accept, authorization");
      // response.getHttpHeaders().add("Access-Control-Allow-Credentials","true");
      // response.getHttpHeaders().add("Access-Control-Allow-Methods",
      // "GET, POST, PUT, DELETE, OPTIONS, HEAD");
   }
}