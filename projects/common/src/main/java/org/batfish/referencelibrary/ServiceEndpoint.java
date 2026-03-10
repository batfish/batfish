package org.batfish.referencelibrary;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

public class ServiceEndpoint implements Comparable<ServiceEndpoint>, Serializable {

  private static final String PROP_ADDRESS = "address";
  private static final String PROP_NAME = "name";
  private static final String PROP_SERVICE = "service";

  private @Nonnull String _address;
  private @Nonnull String _name;
  private @Nonnull String _service;

  @JsonCreator
  public ServiceEndpoint(
      @JsonProperty(PROP_ADDRESS) String address,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICE) String service) {
    checkArgument(address != null, "Service endpoint address cannot be null");
    checkArgument(name != null, "Service endpoint name cannot be null");
    checkArgument(service != null, "Service endpoint service cannot be null");
    Names.checkName(name, "service endpoint", Type.REFERENCE_OBJECT);

    _address = address;
    _name = name;
    _service = service;
  }

  void checkUndefinedReferences(List<String> addressGroupNames, List<String> serviceNames) {
    checkArgument(
        addressGroupNames.contains(_address),
        "Undefined address group '%s' in service endpoint '%s'",
        _address,
        _name);
    checkArgument(
        serviceNames.contains(_service),
        "Undefined service name '%s' in service endpoint '%s'",
        _service,
        _name);
  }

  @Override
  public int compareTo(ServiceEndpoint o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServiceEndpoint)) {
      return false;
    }
    return Objects.equals(_name, ((ServiceEndpoint) o)._name)
        && Objects.equals(_address, ((ServiceEndpoint) o)._address)
        && Objects.equals(_service, ((ServiceEndpoint) o)._service);
  }

  @JsonProperty(PROP_ADDRESS)
  public String getAddress() {
    return _address;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_SERVICE)
  public String getService() {
    return _service;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _address, _service);
  }
}
