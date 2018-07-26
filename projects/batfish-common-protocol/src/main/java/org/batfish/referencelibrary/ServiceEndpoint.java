package org.batfish.referencelibrary;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nonnull;

public class ServiceEndpoint implements Comparable<ServiceEndpoint> {

  private static final String PROP_ADDRESS = "address";
  private static final String PROP_NAME = "name";
  private static final String PROP_SERVICE = "service";

  @Nonnull private String _address;
  @Nonnull private String _name;
  @Nonnull private String _service;

  public ServiceEndpoint(
      @JsonProperty(PROP_ADDRESS) String address,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICE) String service) {
    checkArgument(address != null, "Service endpoint address cannot be null");
    checkArgument(name != null, "Service endpoint name cannot be null");
    checkArgument(service != null, "Service endpoint service cannot be null");
    ReferenceLibrary.checkValidName(name, "service endpoint");

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
}
