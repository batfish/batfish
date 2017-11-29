package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.DefinedStructure;

public class L2tpClass extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private L2tpClass(@JsonProperty(PROP_NAME) String name) {
    super(name, -1);
  }

  public L2tpClass(String number, int definitionLine) {
    super(number, definitionLine);
  }
}
