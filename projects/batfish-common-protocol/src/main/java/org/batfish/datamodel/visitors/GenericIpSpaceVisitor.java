package org.batfish.datamodel.visitors;

import org.batfish.datamodel.IpAddressAcl;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

public interface GenericIpSpaceVisitor<R> {
  R castToGenericIpSpaceVisitorReturnType(Object o);

  R visitIpAddressAcl(IpAddressAcl ipAddressAcl);

  R visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace);

  R visitUniverseIpSpace(UniverseIpSpace universeIpSpace);
}
