package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/** Implementation of {@link AbstractIpSpaceContainsIp}. */
public final class IpSpaceContainsIp extends AbstractIpSpaceContainsIp {
  private final Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceContainsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    super(ip);
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    IpSpace ipSpace = _namedIpSpaces.get(ipSpaceReference.getName());
    if (ipSpace == null) {
      return false;
    }
    return ipSpace.accept(this);
  }
}
