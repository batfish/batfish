package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

public interface GenericIpSpaceVisitor<R> {
  R castToGenericIpSpaceVisitorReturnType(Object o);

  R visitAclIpSpace(AclIpSpace aclIpSpace);

  R visitEmptyIpSpace(EmptyIpSpace emptyIpSpace);

  R visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace);

  R visitUniverseIpSpace(UniverseIpSpace universeIpSpace);
}
