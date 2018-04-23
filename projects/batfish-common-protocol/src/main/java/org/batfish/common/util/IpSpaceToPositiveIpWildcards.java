package org.batfish.common.util;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
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
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpaceToPositiveIpWildcards implements GenericIpSpaceVisitor<Void> {
  ImmutableSortedSet.Builder<IpWildcard> _ipWildcards;
  Map<String, IpSpace> _namedIpSpaces;

  private IpSpaceToPositiveIpWildcards(Map<String, IpSpace> namedIpSpaces) {
    _ipWildcards = new ImmutableSortedSet.Builder<IpWildcard>(Comparator.naturalOrder());
    _namedIpSpaces = namedIpSpaces;
  }

  public static SortedSet<IpWildcard> toPositiveIpWildcards(
      IpSpace ipSpace, Map<String, IpSpace> namedIpSpaces) {
    IpSpaceToPositiveIpWildcards visitor = new IpSpaceToPositiveIpWildcards(namedIpSpaces);
    ipSpace.accept(visitor);
    return visitor._ipWildcards.build();
  }

  @Override
  public Void castToGenericIpSpaceVisitorReturnType(Object o) {
    return null;
  }

  @Override
  public Void visitAclIpSpace(AclIpSpace aclIpSpace) {
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      if (line.getAction() == LineAction.REJECT && line.getIpSpace() != EmptyIpSpace.INSTANCE) {
        throw new BatfishException("IpSpace cannot be converted to positive IpWildcards");
      }
      line.getIpSpace().accept(this);
    }
    return null;
  }

  @Override
  public Void visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return null;
  }

  @Override
  public Void visitIpIpSpace(IpIpSpace ipIpSpace) {
    _ipWildcards.add(new IpWildcard(ipIpSpace.getIp()));
    return null;
  }

  @Override
  public Void visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    // TODO: detect infinite loops
    _namedIpSpaces.get(ipSpaceReference.getName()).accept(this);
    return null;
  }

  @Override
  public Void visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    _ipWildcards.add(ipWildcardIpSpace.getIpWildcard());
    return null;
  }

  @Override
  public Void visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    if (!ipWildcardSetIpSpace.getBlacklist().isEmpty()) {
      throw new BatfishException("IpSpace cannot be converted to positive IpWildcards");
    }
    _ipWildcards.addAll(ipWildcardSetIpSpace.getWhitelist());
    return null;
  }

  @Override
  public Void visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    _ipWildcards.add(new IpWildcard(prefixIpSpace.getPrefix()));
    return null;
  }

  @Override
  public Void visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    _ipWildcards.add(IpWildcard.ANY);
    return null;
  }
}
