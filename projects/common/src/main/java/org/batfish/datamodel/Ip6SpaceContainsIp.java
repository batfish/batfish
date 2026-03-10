package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Implementation of {@link AbstractIp6SpaceContainsIp}. */
public final class Ip6SpaceContainsIp extends AbstractIp6SpaceContainsIp {
  private final Map<String, Ip6Space> _namedIp6Spaces;
  // A cache of prior containsIp resulsts. Not size-limited, but IpSpaceContainsIp instances are
  // expected to be short-lived.
  private final Map<Ip6Space, Boolean> _cached;

  public Ip6SpaceContainsIp(Ip6 ip6, Map<String, Ip6Space> namedIp6Spaces) {
    super(ip6);
    _namedIp6Spaces = ImmutableMap.copyOf(namedIp6Spaces);
    _cached = new ConcurrentHashMap<>();
  }

  @Override
  public Boolean visit(Ip6Space ip6Space) {
    // We can't use _cached.computeIfAbsent since it does not support recursive updates.
    Boolean cachedResult = _cached.get(ip6Space);
    if (cachedResult != null) {
      return cachedResult;
    }
    Boolean result = super.visit(ip6Space);
    _cached.putIfAbsent(ip6Space, result);
    return result;
  }

  @Override
  public Boolean visitIp6SpaceReference(Ip6SpaceReference ip6SpaceReference) {
    Ip6Space ip6Space = _namedIp6Spaces.get(ip6SpaceReference.getName());
    if (ip6Space == null) {
      return false;
    }
    return visit(ip6Space);
  }
}
