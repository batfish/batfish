package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Specialize an {@link IpSpace} to input destination IP whitelist and blacklist. The goal is to
 * simplify the {@link IpSpace} as much as possible under the assumption that the whitelist and
 * blacklist are always true (i.e. all packets match the whitelist, no packets match the blacklist).
 * For example, if the {@link IpSpace} is disjoint from the whitelist, it is effectively empty (i.e.
 * it contains no IPs in the whitelist).
 */
public class IpSpaceSpecializer implements GenericIpSpaceVisitor<IpSpace> {

  private final Set<IpWildcard> _whitelist;

  private final Set<IpWildcard> _blacklist;

  private final boolean _canSpecialize;

  public IpSpaceSpecializer(Set<IpWildcard> whitelist, Set<IpWildcard> blacklist) {
    _canSpecialize =
        !((whitelist.isEmpty() || whitelist.contains(IpWildcard.ANY)) && blacklist.isEmpty());

    _whitelist = !whitelist.isEmpty() ? whitelist : ImmutableSet.of(IpWildcard.ANY);
    _blacklist = blacklist;
  }

  @Override
  public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (IpSpace) o;
  }

  public IpSpace specialize(IpSpace ipSpace) {
    if (!_canSpecialize) {
      return IpSpaceSimplifier.simplify(ipSpace);
    } else {
      return IpSpaceSimplifier.simplify(ipSpace.accept(this));
    }
  }

  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    /* Just specialize the IpSpace of each acl line. */
    List<AclIpSpaceLine> specializedLines =
        aclIpSpace
            .getLines()
            .stream()
            .map(
                aclIpSpaceLine ->
                    AclIpSpaceLine.builder()
                        .setAction(aclIpSpaceLine.getAction())
                        .setIpSpace(aclIpSpaceLine.getIpSpace().accept(this))
                        .build())
            .collect(ImmutableList.toImmutableList());

    return AclIpSpace.builder().setLines(specializedLines).build();
  }

  @Override
  public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public IpSpace visitIp(Ip ip) {
    if (_blacklist.stream().noneMatch(ipWildcard -> ipWildcard.containsIp(ip))
        && _whitelist.stream().anyMatch(ipWildcard -> ipWildcard.containsIp(ip))) {
      return ip;
    } else {
      return EmptyIpSpace.INSTANCE;
    }
  }

  @Override
  public IpSpace visitIpWildcard(IpWildcard ipWildcard) {
    return visitIpWildcard(ipWildcard, _whitelist);
  }

  public IpSpace visitIpWildcard(IpWildcard ipWildcard, Set<IpWildcard> whitelist) {
    if (_blacklist.stream().anyMatch(ipWildcard::subsetOf)) {
      // blacklisted
      return EmptyIpSpace.INSTANCE;
    }

    if (whitelist.stream().allMatch(ipWildcard::supersetOf)
        && _blacklist.stream().noneMatch(ipWildcard::intersects)) {
      return UniverseIpSpace.INSTANCE;
    } else if (whitelist.stream().anyMatch(ipWildcard::intersects)) {
      // some match
      return ipWildcard;
    } else {
      return EmptyIpSpace.INSTANCE;
    }
  }

  @Override
  public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    /*
     * Remove any blacklisted Ips that are already blacklisted by _blacklist
     * or that don't match any _whitelist
     */
    Stream<IpWildcard> blacklistStream = ipWildcardSetIpSpace.getBlacklist().stream();
    if (!_blacklist.isEmpty()) {
      blacklistStream =
          blacklistStream.filter(notDstIp -> _blacklist.stream().noneMatch(notDstIp::subsetOf));
    }
    if (!_whitelist.isEmpty()) {
      blacklistStream =
          blacklistStream.filter(notDstIp -> _whitelist.stream().anyMatch(notDstIp::intersects));
    }
    Set<IpWildcard> blacklist = blacklistStream.collect(Collectors.toSet());

    // Temporarily narrow the headerspace whitelist to what is not covered by the IpSpace blacklist.
    Set<IpWildcard> newWhitelist =
        _whitelist
            .stream()
            .filter(ipWildcard -> blacklist.stream().noneMatch(ipWildcard::subsetOf))
            .collect(Collectors.toSet());

    if (newWhitelist.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }

    /*
     * Remove any whitelisted Ips that are either blacklisted or don't overlap with the specialized
     * whitelist.
     */
    Set<IpSpace> ipSpaceWhitelist =
        ipWildcardSetIpSpace
            .getWhitelist()
            .stream()
            .map(ipWildcard -> visitIpWildcard(ipWildcard, newWhitelist))
            .filter(ipSpace -> !(ipSpace instanceof EmptyIpSpace))
            .collect(Collectors.toSet());

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
      /*
       * visitIpWildcard can return either EmptyIpSpace, UniverseIpSpace, or IpWildcard.
       * We've already removed EmptyIpSpace and checked for UniverseIpSpace, so everything left
       * is an IpWildcard.
       */
      Set<IpWildcard> ipWildcardWhitelist =
          ipSpaceWhitelist.stream().map(IpWildcard.class::cast).collect(Collectors.toSet());
      return IpWildcardSetIpSpace.builder()
          .including(ipWildcardWhitelist)
          .excluding(blacklist)
          .build();
    }
  }

  @Override
  public IpSpace visitPrefix(Prefix prefix) {
    IpSpace specialized = visitIpWildcard(new IpWildcard(prefix));
    /*
     * visitIpWildcard can return EmptyIpSpace, UniverseIpSpace, or IpWildcard
     * If it returns IpWildcard, it will be unchanged, so return prefix instead.
     */
    if (specialized instanceof IpWildcard) {
      return prefix;
    } else {
      return specialized;
    }
  }

  @Override
  public IpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return universeIpSpace;
  }
}
