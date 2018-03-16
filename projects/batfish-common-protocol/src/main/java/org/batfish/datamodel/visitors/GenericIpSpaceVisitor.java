package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

public interface GenericIpSpaceVisitor<R> {
  R castToGenericIpSpaceVisitorReturnType(Object o);

  R visitAclIpSpace(AclIpSpace aclIpSpace);

  R visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace);

  R visitUniverseIpSpace(UniverseIpSpace universeIpSpace);
}
