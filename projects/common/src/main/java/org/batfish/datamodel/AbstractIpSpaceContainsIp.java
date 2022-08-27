package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * An {@link GenericIpSpaceVisitor IpSpace visitor} that tests if the {@link IpSpace} contains an
 * {@link Ip}. Implementation of {@link IpSpaceReference} is left abstract.
 */
public abstract class AbstractIpSpaceContainsIp implements GenericIpSpaceVisitor<Boolean> {
  private final Ip _ip;

  public AbstractIpSpaceContainsIp(Ip ip) {
    _ip = ip;
  }

  @Override
  public final Boolean visitAclIpSpace(AclIpSpace aclIpSpace) {
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      if (visit(line.getIpSpace())) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public final Boolean visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return false;
  }

  @Override
  public final Boolean visitIpIpSpace(IpIpSpace ipIpSpace) {
    return _ip.equals(ipIpSpace.getIp());
  }

  @Override
  public final Boolean visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return ipWildcardIpSpace.getIpWildcard().containsIp(_ip);
  }

  @Override
  public final Boolean visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    for (IpWildcard w : ipWildcardSetIpSpace.getBlacklist()) {
      if (w.containsIp(_ip)) {
        return false;
      }
    }
    for (IpWildcard w : ipWildcardSetIpSpace.getWhitelist()) {
      if (w.containsIp(_ip)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public final Boolean visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return prefixIpSpace.getPrefix().containsIp(_ip);
  }

  @Override
  public final Boolean visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return true;
  }
}
