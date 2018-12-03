package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Prefix;

public class GeneratedRoute implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _active;

  private Set<Long> _communities;

  private Integer _metric;

  private final List<String> _policies;

  private Integer _preference;

  private final Prefix _prefix;

  public GeneratedRoute(Prefix prefix) {
    _prefix = prefix;
    _policies = new LinkedList<>();
    _communities = new HashSet<>();
    _active = false;
  }

  public boolean getActive() {
    return _active;
  }

  public Set<Long> getCommunities() {
    return _communities;
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

  public void setActive(boolean active) {
    _active = active;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  public void setPreference(int preference) {
    _preference = preference;
  }
}
