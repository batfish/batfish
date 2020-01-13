package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * An {@link GenericIpSpaceVisitor IpSpace visitor} that tests if the {@link IpSpace} contains an
 * {@link Ip}.
 */
public class IpSpaceContainsIp implements GenericIpSpaceVisitor<Boolean> {
  private final Map<String, IpSpace> _namedIpSpaces;
  private final Ip _ip;

  public IpSpaceContainsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    _ip = ip;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
  }

  @Override
  public Boolean castToGenericIpSpaceVisitorReturnType(Object o) {
    return (Boolean) o;
  }

  @Override
  public Boolean visitAclIpSpace(AclIpSpace aclIpSpace) {
    return aclIpSpace.getLines().stream()
        .filter(line -> line.getIpSpace().accept(this))
        .map(AclIpSpaceLine::getAction)
        .findFirst()
        .map(action -> action == LineAction.PERMIT)
        .orElse(false);
  }

  @Override
  public Boolean visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return false;
  }

  @Override
  public Boolean visitIpIpSpace(IpIpSpace ipIpSpace) {
    return _ip.equals(ipIpSpace.getIp());
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    IpSpace ipSpace = _namedIpSpaces.get(ipSpaceReference.getName());
    if (ipSpace == null) {
      return false;
    }
    return ipSpace.accept(this);
  }

  @Override
  public Boolean visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return ipWildcardIpSpace.getIpWildcard().containsIp(_ip);
  }

  @Override
  public Boolean visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return ipWildcardSetIpSpace.getBlacklist().stream().noneMatch(w -> w.containsIp(_ip))
        && ipWildcardSetIpSpace.getWhitelist().stream().anyMatch(w -> w.containsIp(_ip));
  }

  @Override
  public Boolean visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return prefixIpSpace.getPrefix().containsIp(_ip);
  }

  @Override
  public Boolean visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return true;
  }
}
