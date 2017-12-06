package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.DefinedStructure;

public class DepiTunnel extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private DepiTunnel(@JsonProperty(PROP_NAME) String name) {
    super(name, -1);
  }

  public DepiTunnel(String number, int definitionLine) {
    super(number, definitionLine);
  }
}
