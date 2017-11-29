package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;

public class DocsisPolicy extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private List<String> _rules;

  @JsonCreator
  private DocsisPolicy(@JsonProperty(PROP_NAME) String number) {
    super(number, -1);
    _rules = new ArrayList<>();
  }

  public DocsisPolicy(String number, int definitionLine) {
    super(number, definitionLine);
    _rules = new ArrayList<>();
  }

  public List<String> getRules() {
    return _rules;
  }

  public void setRules(List<String> rules) {
    _rules = rules;
  }
}
