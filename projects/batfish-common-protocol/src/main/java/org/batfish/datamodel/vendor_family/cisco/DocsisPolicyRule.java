package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.DefinedStructure;

public class DocsisPolicyRule extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private DocsisPolicyRule(@JsonProperty(PROP_NAME) String number) {
    super(number, -1);
  }

  public DocsisPolicyRule(String number, int definitionLine) {
    super(number, definitionLine);
  }
}
