package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclIp6Space;
import org.batfish.datamodel.EmptyIp6Space;
import org.batfish.datamodel.Ip6Ip6Space;
import org.batfish.datamodel.Ip6Space;
import org.batfish.datamodel.Ip6SpaceReference;
import org.batfish.datamodel.Ip6WildcardIp6Space;
import org.batfish.datamodel.Ip6WildcardSetIp6Space;
import org.batfish.datamodel.PrefixIp6Space;
import org.batfish.datamodel.UniverseIp6Space;

public interface GenericIp6SpaceVisitor<R> {
  default R visit(Ip6Space ip6Space) {
    return ip6Space.accept(this);
  }

  R visitAclIp6Space(AclIp6Space aclIp6Space);

  R visitEmptyIp6Space(EmptyIp6Space emptyIp6Space);

  R visitIp6Ip6Space(Ip6Ip6Space ip6Ip6Space);

  R visitIp6SpaceReference(Ip6SpaceReference ip6SpaceReference);

  R visitPrefixIp6Space(PrefixIp6Space prefixIp6Space);

  R visitUniverseIp6Space(UniverseIp6Space universeIp6Space);

  R visitIp6WildcardIp6Space(Ip6WildcardIp6Space ip6WildcardIp6Space);

  R visitIp6WildcardSetIp6Space(Ip6WildcardSetIp6Space ip6WildcardSetIp6Space);
}
