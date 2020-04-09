package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class SnmpHost extends ComparableStructure<String> {

  @JsonCreator
  public SnmpHost(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }
}
