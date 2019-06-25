package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class ServiceClass extends ComparableStructure<String> {
  private static final String PROP_FRIENDLY_NAME = "friendlyName";

  private String _friendlyName;

  @JsonCreator
  public ServiceClass(@JsonProperty(PROP_NAME) String number) {
    super(number);
  }

  @JsonProperty(PROP_FRIENDLY_NAME)
  public String getFriendlyName() {
    return _friendlyName;
  }

  @JsonProperty(PROP_FRIENDLY_NAME)
  public void setFriendlyName(String friendlyName) {
    _friendlyName = friendlyName;
  }
}
