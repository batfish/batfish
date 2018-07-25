package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.batfish.referencelibrary.ServiceEndpoint;

public class ServiceEndpointBean {

  /** The IP address for this service endpoint */
  public String address;

  /** The name of this service endpoint */
  public String name;

  /** The name of a service object or object group for this endpoint */
  public String service;

  @JsonCreator
  private ServiceEndpointBean() {}

  public ServiceEndpointBean(ServiceEndpoint endpoint) {
    address = endpoint.getAddress();
    name = endpoint.getName();
    service = endpoint.getService();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServiceEndpointBean)) {
      return false;
    }
    return Objects.equals(address, ((ServiceEndpointBean) o).address)
        && Objects.equals(name, ((ServiceEndpointBean) o).name)
        && Objects.equals(service, ((ServiceEndpointBean) o).service);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, name, service);
  }

  public ServiceEndpoint toServiceEndpoint() {
    return new ServiceEndpoint(address, name, service);
  }
}
