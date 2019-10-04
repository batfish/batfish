package org.batfish.minesweeper.abstraction;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpacePrefixCollector implements GenericIpSpaceVisitor<Void> {
  ImmutableSet.Builder<Prefix> _prefixes;
  ImmutableSet.Builder<Prefix> _notPrefixes;

  public IpSpacePrefixCollector() {
    _prefixes = ImmutableSet.builder();
    _notPrefixes = ImmutableSet.builder();
  }

  public Set<Prefix> getPrefixes() {
    return _prefixes.build();
  }

  public Set<Prefix> getNotPrefixes() {
    return _notPrefixes.build();
  }

  public void collectPrefixes(IpSpace ipSpace) {
    if (ipSpace != null) {
      ipSpace.accept(this);
    }
  }

  @Override
  public Void castToGenericIpSpaceVisitorReturnType(Object o) {
    return null;
  }

  @Override
  public Void visitAclIpSpace(AclIpSpace aclIpSpace) {
    throw new BatfishException("AclIpSpace is unsupported.");
  }

  @Override
  public Void visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return null;
  }

  @Override
  public Void visitIpIpSpace(IpIpSpace ipIpSpace) {
    _prefixes.add(ipIpSpace.getIp().toPrefix());
    return null;
  }

  @Override
  public Void visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    throw new BatfishException("IpSpaceReference is unsupported.");
  }

  private void assertIpWildcardIsPrefix(IpWildcard ipWildcard) {
    if (!ipWildcard.isPrefix()) {
      throw new BatfishException("non-prefix IpWildcards are unsupported");
    }
  }

  @Override
  public Void visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    IpWildcard ipWildcard = ipWildcardIpSpace.getIpWildcard();
    assertIpWildcardIsPrefix(ipWildcard);
    _prefixes.add(ipWildcard.toPrefix());
    return null;
  }

  @Override
  public Void visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    ipWildcardSetIpSpace.getWhitelist().forEach(this::assertIpWildcardIsPrefix);
    ipWildcardSetIpSpace.getBlacklist().forEach(this::assertIpWildcardIsPrefix);
    ipWildcardSetIpSpace.getWhitelist().stream().map(IpWildcard::toPrefix).forEach(_prefixes::add);
    ipWildcardSetIpSpace.getBlacklist().stream()
        .map(IpWildcard::toPrefix)
        .forEach(_notPrefixes::add);
    return null;
  }

  @Override
  public Void visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    _prefixes.add(prefixIpSpace.getPrefix());
    return null;
  }

  @Override
  public Void visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    _prefixes.add(IpWildcard.ANY.toPrefix());
    return null;
  }
}
