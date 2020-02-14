package org.batfish.common.ip;

public interface GenericIpSpaceVisitor<R> {
  R castToGenericIpSpaceVisitorReturnType(Object o);

  default R visit(IpSpace ipSpace) {
    return ipSpace.accept(this);
  }

  R visitAclIpSpace(AclIpSpace aclIpSpace);

  R visitEmptyIpSpace(EmptyIpSpace emptyIpSpace);

  R visitIpIpSpace(IpIpSpace ipIpSpace);

  R visitIpSpaceReference(IpSpaceReference ipSpaceReference);

  R visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace);

  R visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace);

  R visitPrefixIpSpace(PrefixIpSpace prefixIpSpace);

  R visitUniverseIpSpace(UniverseIpSpace universeIpSpace);
}
