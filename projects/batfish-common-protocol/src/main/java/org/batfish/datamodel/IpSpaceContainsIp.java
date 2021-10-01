package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Implementation of {@link AbstractIpSpaceContainsIp}. */
public final class IpSpaceContainsIp extends AbstractIpSpaceContainsIp {
  private final Map<String, IpSpace> _namedIpSpaces;
  // A cache of prior containsIp resulsts. Not size-limited, but IpSpaceContainsIp instances are
  // expected to be short-lived.
  private final Map<IpSpace, Boolean> _cached;

  public IpSpaceContainsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    super(ip);
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _cached = new ConcurrentHashMap<>();
  }

  @Override
  public Boolean visit(IpSpace ipSpace) {
    // We can't use _cached.computeIfAbsent since it does not support recursive updates.
    Boolean cachedResult = _cached.get(ipSpace);
    if (cachedResult != null) {
      return cachedResult;
    }
    Boolean result = super.visit(ipSpace);
    _cached.putIfAbsent(ipSpace, result);
    return result;
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    IpSpace ipSpace = _namedIpSpaces.get(ipSpaceReference.getName());
    if (ipSpace == null) {
      return false;
    }
    return visit(ipSpace);
  }
}
