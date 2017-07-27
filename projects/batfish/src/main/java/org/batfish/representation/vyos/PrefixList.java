package org.batfish.representation.vyos;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class PrefixList extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _description;

  private final Map<Integer, PrefixListRule> _rules;

  public PrefixList(String name) {
    super(name);
    _rules = new TreeMap<>();
  }

  public String getDescription() {
    return _description;
  }

  public Map<Integer, PrefixListRule> getRules() {
    return _rules;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
