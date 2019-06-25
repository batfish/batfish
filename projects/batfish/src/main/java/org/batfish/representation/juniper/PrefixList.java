package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.datamodel.Prefix;

public class PrefixList implements Serializable {

  private boolean _ipv6;

  private String _name;

  private Set<Prefix> _prefixes;

  public PrefixList(String name) {
    _name = name;
    _prefixes = new TreeSet<>();
  }

  public boolean getIpv6() {
    return _ipv6;
  }

  public String getName() {
    return _name;
  }

  public Set<Prefix> getPrefixes() {
    return _prefixes;
  }

  public void setIpv6(boolean ipv6) {
    _ipv6 = ipv6;
  }
}
