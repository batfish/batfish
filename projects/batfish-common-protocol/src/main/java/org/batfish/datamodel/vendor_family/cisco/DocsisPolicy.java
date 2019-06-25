package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DocsisPolicy implements Serializable {

  private static final String PROP_NAME = "name";

  private final String _name;

  private List<String> _rules;

  @JsonCreator
  public DocsisPolicy(@JsonProperty(PROP_NAME) String number) {
    _name = number;
    _rules = new ArrayList<>();
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  public List<String> getRules() {
    return _rules;
  }

  public void setRules(List<String> rules) {
    _rules = rules;
  }
}
