package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class L2tpClass extends ComparableStructure<String> {

  @JsonCreator
  public L2tpClass(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }
}
