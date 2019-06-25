package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class PrefixList implements Serializable {

  private String _description;

  private final String _name;

  private final Map<Integer, PrefixListRule> _rules;

  public PrefixList(String name) {
    _name = name;
    _rules = new TreeMap<>();
  }

  public String getDescription() {
    return _description;
  }

  public String getName() {
    return _name;
  }

  public Map<Integer, PrefixListRule> getRules() {
    return _rules;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
