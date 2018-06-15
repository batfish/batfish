package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.Prefix;

public class GeneratedRoute implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Integer _metric;

  private final List<String> _policies;

  private Integer _preference;

  private final Prefix _prefix;

  public GeneratedRoute(Prefix prefix) {
    _prefix = prefix;
    _policies = new LinkedList<>();
  }

  public Integer getMetric() {
    return _metric;
  }

  public List<String> getPolicies() {
    return _policies;
  }

  public Integer getPreference() {
    return _preference;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  public void setPreference(int preference) {
    _preference = preference;
  }
}
