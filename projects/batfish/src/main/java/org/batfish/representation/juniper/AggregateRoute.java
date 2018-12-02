package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Prefix;

public final class AggregateRoute implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private AsPath _asPath;

  private final Set<Long> _communities;

  private int _preference;

  private final Prefix _prefix;

  private Integer _tag;

  private String _policy;

  public AggregateRoute(Prefix prefix) {
    _communities = new HashSet<>();
    _prefix = prefix;
  }

  public AsPath getAsPath() {
    return _asPath;
  }

  public Set<Long> getCommunities() {
    return _communities;
  }

  public int getMetric() {
    return _preference;
  }

  public String getPolicy() {
    return _policy;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public Integer getTag() {
    return _tag;
  }

  public void setAsPath(AsPath asPath) {
    _asPath = asPath;
  }

  public void setPolicy(String policy) {
    _policy = policy;
  }

  public void setPreference(int preference) {
    _preference = preference;
  }

  public void setTag(int tag) {
    _tag = tag;
  }
}
