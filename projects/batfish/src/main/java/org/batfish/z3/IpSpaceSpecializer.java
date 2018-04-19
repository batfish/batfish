package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
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
import org.batfish.datamodel.visitors.IpSpaceContainedInWildcard;
import org.batfish.datamodel.visitors.IpSpaceMayIntersectWildcard;

/**
 * Specialize an {@link IpSpace} to input destination IP whitelist and blacklist. The goal is to
 * simplify the {@link IpSpace} as much as possible under the assumption that the whitelist and
 * blacklist are always true (i.e. all packets match the whitelist, no packets match the blacklist).
 * For example, if the {@link IpSpace} is disjoint from the whitelist, it is effectively empty (i.e.
 * it containsIp no IPs in the whitelist).
 */
public class IpSpaceSpecializer implements GenericIpSpaceVisitor<IpSpace> {

  private final IpSpace _ipSpace;

  public IpSpaceSpecializer(IpSpace ipSpace) {
    _ipSpace = IpSpaceSimplifier.simplify(ipSpace);
  }

  @Override
  public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (IpSpace) o;
  }

  public IpSpace specialize(IpSpace ipSpace) {
    if (_ipSpace == null || _ipSpace == UniverseIpSpace.INSTANCE) {
      return IpSpaceSimplifier.simplify(ipSpace);
    } else if (_ipSpace == EmptyIpSpace.INSTANCE) {
      return EmptyIpSpace.INSTANCE;
    } else {
      return IpSpaceSimplifier.simplify(ipSpace.accept(this));
    }
  }

  public IpSpace specialize(Ip ip) {
    if (_ipSpace.containsIp(ip, ImmutableMap.of())) {
      return ip.toIpSpace();
    } else {
      return EmptyIpSpace.INSTANCE;
    }
  }

  public IpSpace specialize(IpWildcard ipWildcard) {
    if (!_ipSpace.accept(new IpSpaceMayIntersectWildcard(ipWildcard))) {
      return EmptyIpSpace.INSTANCE;
    } else if (_ipSpace.accept(new IpSpaceContainedInWildcard(ipWildcard))) {
      return UniverseIpSpace.INSTANCE;
    } else {
      return ipWildcard.toIpSpace();
    }
  }

  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    /* Just specialize the IpSpace of each acl line. */
    List<AclIpSpaceLine> specializedLines =
        aclIpSpace
            .getLines()
            .stream()
            .map(line -> line.toBuilder().setIpSpace(line.getIpSpace().accept(this)).build())
            .filter(line -> line.getIpSpace() != EmptyIpSpace.INSTANCE)
            .collect(ImmutableList.toImmutableList());

    if (specializedLines.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }

    if (specializedLines
        .stream()
        .allMatch(aclIpSpaceLine -> aclIpSpaceLine.getAction() == LineAction.REJECT)) {
      return EmptyIpSpace.INSTANCE;
    }

    return AclIpSpace.builder().setLines(specializedLines).build();
  }

  @Override
  public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public IpSpace visitIpIpSpace(IpIpSpace ipIpSpace) {
    return specialize(ipIpSpace.getIp());
  }

  @Override
  public IpSpace visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return specialize(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    Set<IpSpace> blacklistIpSpace =
        ipWildcardSetIpSpace
            .getBlacklist()
            .stream()
            .map(this::specialize)
            .filter(ipSpace -> ipSpace != EmptyIpSpace.INSTANCE)
            .collect(ImmutableSet.toImmutableSet());

    if (blacklistIpSpace.contains(UniverseIpSpace.INSTANCE)) {
      return EmptyIpSpace.INSTANCE;
    }

    /*
     * A specialized IpWildcard is one of EmptyIpSpace, UniverseIpSpace, or IpWildcard.
     * We've already removed EmptyIpSpace and checked for UniverseIpSpace, so everything left
     * is an IpWildcard.
     */
    Set<IpWildcard> blacklist =
        blacklistIpSpace
            .stream()
            .map(IpWildcardIpSpace.class::cast)
            .map(IpWildcardIpSpace::getIpWildcard)
            .collect(ImmutableSet.toImmutableSet());

    IpSpaceSpecializer refinedSpecializer;

    if (blacklist.isEmpty()) {
      refinedSpecializer = this;
    } else {
      /* Specialize the whitelist using a refined specializer obtained by specializing _ipSpace
       * to the blacklist. This helps situations like this:
       *    _ipSpace <= blacklist <= whitelist
       * If _ipSpace is covered by the blacklist, then refinedIpSpace will be empty, and we will
       * later infer that the input ipWildcardSetIpSpace should be specialized to empty as well.
       * Without this, specialization would not be able to infer this, so we'd do less optimization.
       */
      IpSpace refinedIpSpace =
          _ipSpace.accept(
              new IpSpaceSpecializer(
                  IpWildcardSetIpSpace.builder()
                      .including(IpWildcard.ANY)
                      .excluding(blacklist)
                      .build()));

      /* blacklist covers the entire _ipSpace, so no need to consider the whitelist.
       * TODO is this possible if !blacklistIpSpace.containsIp(UniverseIpSpace.INSTANCE)?
       */
      if (refinedIpSpace == EmptyIpSpace.INSTANCE) {
        return EmptyIpSpace.INSTANCE;
      }

      refinedSpecializer = new IpSpaceSpecializer(refinedIpSpace);
    }

    Set<IpSpace> ipSpaceWhitelist =
        ipWildcardSetIpSpace
            .getWhitelist()
            .stream()
            .map(refinedSpecializer::specialize)
            .filter(ipSpace -> ipSpace != EmptyIpSpace.INSTANCE)
            .collect(ImmutableSet.toImmutableSet());

    if (ipSpaceWhitelist.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    } else if (ipSpaceWhitelist.contains(UniverseIpSpace.INSTANCE)) {
      if (blacklist.isEmpty()) {
        return UniverseIpSpace.INSTANCE;
      } else {
        return IpWildcardSetIpSpace.builder()
            .including(IpWildcard.ANY)
            .excluding(blacklist)
            .build();
      }
    } else {
      Set<IpWildcard> ipWildcardWhitelist =
          ipSpaceWhitelist
              .stream()
              .map(IpWildcardIpSpace.class::cast)
              .map(IpWildcardIpSpace::getIpWildcard)
              .collect(Collectors.toSet());
      return IpWildcardSetIpSpace.builder()
          .including(ipWildcardWhitelist)
          .excluding(blacklist)
          .build();
    }
  }

  @Override
  public IpSpace visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    IpSpace specialized = specialize(new IpWildcard(prefixIpSpace.getPrefix()));
    /*
     * visitIpWildcard can return EmptyIpSpace, UniverseIpSpace, or IpWildcard
     * If it returns IpWildcard, it will be unchanged, so return prefixIpSpace instead.
     */
    if (specialized instanceof IpWildcardIpSpace) {
      return prefixIpSpace;
    } else {
      return specialized;
    }
  }

  @Override
  public IpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return universeIpSpace;
  }

  @Override
  public IpSpace visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
