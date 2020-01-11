package org.batfish.representation.juniper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** A firewall filter on Juniper. */
public final class ConcreteFirewallFilter extends FirewallFilter {

  private final Family _family;

  /*
   * This is important filtering information for security policies (i.e. security policies only
   * apply to specific from-zones)
   */
  private String _fromZone;
  private boolean _usedForFBF;
  private final Map<String, FwTerm> _terms;

  public ConcreteFirewallFilter(String name, Family family) {
    super(name);
    _family = family;
    _fromZone = null;
    _terms = new LinkedHashMap<>();
  }

  @Override
  public Family getFamily() {
    return _family;
  }

  @Override
  public Optional<String> getFromZone() {
    return Optional.ofNullable(_fromZone);
  }

  /** Whether or not this filter is used for Filter-Based Forwarding (FBF) */
  @Override
  public boolean isUsedForFBF() {
    return _usedForFBF;
  }

  public Map<String, FwTerm> getTerms() {
    return _terms;
  }

  public void setUsedForFBF(boolean usedForFBF) {
    _usedForFBF = usedForFBF;
  }

  public void setFromZone(String fromZone) {
    _fromZone = fromZone;
  }
}
