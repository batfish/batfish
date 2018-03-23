package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ComplementIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;

public interface GenericIpSpaceVisitor<R> {
  R castToGenericIpSpaceVisitorReturnType(Object o);

  R visitAclIpSpace(AclIpSpace aclIpSpace);

  R visitComplementIpSpace(ComplementIpSpace complementIpSpace);

  R visitEmptyIpSpace(EmptyIpSpace emptyIpSpace);

  R visitIp(Ip ip);

  R visitIpWildcard(IpWildcard ipWildcard);

  R visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace);

  R visitPrefix(Prefix prefix);

  R visitUniverseIpSpace(UniverseIpSpace universeIpSpace);
}
