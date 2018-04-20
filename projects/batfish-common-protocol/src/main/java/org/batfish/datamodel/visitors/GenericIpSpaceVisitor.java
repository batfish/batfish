package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

public interface GenericIpSpaceVisitor<R> {
  R castToGenericIpSpaceVisitorReturnType(Object o);

  R visitAclIpSpace(AclIpSpace aclIpSpace);

  R visitEmptyIpSpace(EmptyIpSpace emptyIpSpace);

  R visitIpIpSpace(IpIpSpace ipIpSpace);

  R visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace);

  R visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace);

  R visitPrefixIpSpace(PrefixIpSpace prefixIpSpace);

  R visitUniverseIpSpace(UniverseIpSpace universeIpSpace);
}
