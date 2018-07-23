package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.batfish.role.addressbook.ServiceEndpoint;

public class ServiceEndpointBean {

  public String address;
  public String name;
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
