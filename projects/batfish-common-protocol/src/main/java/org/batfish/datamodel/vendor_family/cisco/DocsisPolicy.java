package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public class DocsisPolicy extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private List<String> _rules;

  @JsonCreator
  public DocsisPolicy(@JsonProperty(PROP_NAME) String number) {
    super(number);
    _rules = new ArrayList<>();
  }

  public List<String> getRules() {
    return _rules;
  }

  public void setRules(List<String> rules) {
    _rules = rules;
  }
}
