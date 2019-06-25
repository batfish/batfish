package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public final class DocsisPolicyRule implements Serializable {
  private static final String PROP_NAME = "name";

  private final String _name;

  @JsonCreator
  public DocsisPolicyRule(@JsonProperty(PROP_NAME) String number) {
    _name = number;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
