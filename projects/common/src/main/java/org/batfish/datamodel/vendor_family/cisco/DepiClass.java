package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class DepiClass implements Serializable {

  private static final String PROP_NAME = "name";

  private final String _name;

  @JsonCreator
  public DepiClass(@JsonProperty(PROP_NAME) String name) {
    _name = name;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
