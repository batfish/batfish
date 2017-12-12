package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;

/** Represents a space of IPv4 addresses. */
public class IpSpace implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Set<IpWildcard> _blacklist;

  private final Set<IpWildcard> _whitelist;

  public IpSpace(Set<IpWildcard> blacklist, Set<IpWildcard> whitelist) {
    _blacklist = ImmutableSet.copyOf(blacklist);
    _whitelist = ImmutableSet.copyOf(whitelist);
  }

  public boolean contains(Ip ip) {
    return _blacklist.stream().noneMatch(w -> w.contains(ip))
        && _whitelist.stream().anyMatch(w -> w.contains(ip));
  }
}
