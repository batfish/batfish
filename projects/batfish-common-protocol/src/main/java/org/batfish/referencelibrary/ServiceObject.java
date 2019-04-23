package org.batfish.referencelibrary;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;
import org.batfish.datamodel.SubRange;

public class ServiceObject implements Comparable<ServiceObject>, Serializable {

  private static final long serialVersionUID = 1L;

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
    Names.checkName(name, "service object", Type.REFERENCE_OBJECT);

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
