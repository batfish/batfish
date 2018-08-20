package org.batfish.referencelibrary;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

public class ServiceObject implements Comparable<ServiceObject> {

  private static final String PROP_IP_PROTOCOL = "ipProtocol";
  private static final String PROP_NAME = "name";
  private static final String PROP_PORTS = "ports";

  @Nonnull private IpProtocol _ipProtocol;
  @Nonnull private String _name;
  @Nonnull private SubRange _ports;

  public ServiceObject(
      @JsonProperty(PROP_IP_PROTOCOL) IpProtocol ipProtocol,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_PORTS) SubRange ports) {
    checkArgument(ipProtocol != null, "Service object ipProtocol not be null");
    checkArgument(name != null, "Service object name cannot be null");
    checkArgument(ports != null, "Service object ports cannot be null");
    ReferenceLibrary.checkValidName(name, "service object");

    _ipProtocol = ipProtocol;
    _name = name;
    _ports = ports;
  }

  @Override
  public int compareTo(ServiceObject o) {
    return _name.compareTo(o._name);
  }

  @JsonProperty(PROP_IP_PROTOCOL)
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_PORTS)
  public SubRange getPorts() {
    return _ports;
  }
}
