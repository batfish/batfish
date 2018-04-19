package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

public class IpSpaceMayIntersectWildcard implements GenericIpSpaceVisitor<Boolean> {
  IpWildcard _ipWildcard;

  IpSpaceMayNotContainWildcard _mayNotContain;

  public IpSpaceMayIntersectWildcard(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
    _mayNotContain = new IpSpaceMayNotContainWildcard(ipWildcard, this);
  }

  public IpSpaceMayIntersectWildcard(
      IpWildcard ipWildcard, IpSpaceMayNotContainWildcard mayNotContain) {
    _ipWildcard = ipWildcard;
    _mayNotContain = mayNotContain;
  }

  @Override
  public Boolean castToGenericIpSpaceVisitorReturnType(Object o) {
    return (Boolean) o;
  }

  private boolean ipSpaceContainsWildcard(IpSpace ipSpace) {
    return !ipSpace.accept(_mayNotContain);
  }

  private boolean ipSpaceMayIntersectWildcard(IpSpace ipSpace) {
    return ipSpace.accept(this);
  }

  @Override
  public Boolean visitAclIpSpace(AclIpSpace aclIpSpace) {
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      if (line.getAction() == LineAction.ACCEPT && ipSpaceMayIntersectWildcard(line.getIpSpace())) {
        return true;
      }

      if (line.getAction() == LineAction.REJECT && ipSpaceContainsWildcard(line.getIpSpace())) {
        return false;
      }
    }
    return false;
  }

  @Override
  public Boolean visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return false;
  }

  @Override
  public Boolean visitIpIpSpace(IpIpSpace ipIpSpace) {
    return _ipWildcard.containsIp(ipIpSpace.getIp());
  }

  @Override
  public Boolean visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return _ipWildcard.intersects(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public Boolean visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return ipWildcardSetIpSpace.getBlacklist().stream().noneMatch(_ipWildcard::subsetOf)
        && ipWildcardSetIpSpace.getWhitelist().stream().anyMatch(_ipWildcard::intersects);
  }

  @Override
  public Boolean visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return new IpWildcard(prefixIpSpace.getPrefix()).intersects(_ipWildcard);
  }

  @Override
  public Boolean visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return true;
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
