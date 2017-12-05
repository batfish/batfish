package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.DefinedStructure;

public class DepiClass extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private DepiClass(@JsonProperty(PROP_NAME) String name) {
    super(name, -1);
  }

  public DepiClass(String number, int definitionLine) {
    super(number, definitionLine);
  }
}
