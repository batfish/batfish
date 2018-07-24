package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.referencelibrary.ServiceObject;

public class ServiceObjectBean {

  /** The IP protocol this service object (e.g., TCP, UDP, or a protocol number */
  public IpProtocol ipProtocol;

  /** The name of this service object */
  public String name;

  /** The range of ports for this service object (e.g., 80-82) */
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
