package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.role.addressbook.ServiceObject;

public class ServiceObjectBean {

  public IpProtocol ipProtocol;
  public String name;
  public SubRange ports;

  @JsonCreator
  private ServiceObjectBean() {}

  public ServiceObjectBean(ServiceObject serviceObject) {
    ipProtocol = serviceObject.getIpProtocol();
    name = serviceObject.getName();
    ports = serviceObject.getPorts();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServiceObjectBean)) {
      return false;
    }
    return Objects.equals(ipProtocol, ((ServiceObjectBean) o).ipProtocol)
        && Objects.equals(name, ((ServiceObjectBean) o).name)
        && Objects.equals(ports, ((ServiceObjectBean) o).ports);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipProtocol, name, ports);
  }

  public ServiceObject toServiceObject() {
    return new ServiceObject(ipProtocol, name, ports);
  }
}
