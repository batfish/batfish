package org.batfish.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.batfish.common.util.BatfishObjectMapper;

/**
 * This class is registered with Jersey Application (server) and client code to customize how
 * Jackson serializes and deserializes data.
 */
@Provider
public class ServiceObjectMapper implements ContextResolver<ObjectMapper> {

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return BatfishObjectMapper.verboseMapper();
  }
}
