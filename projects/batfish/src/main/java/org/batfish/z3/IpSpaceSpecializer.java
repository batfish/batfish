package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Specialize an IpSpace to input destination IP whitelist and blacklist. The goal is to simplify
 * the IpSpace as much as possible under the assumption that the whitelist and blacklist are true.
 * For example, if the IpSpace is disjoint from the whitelist, it is effectively empty (i.e. it
 * contains no IPs in the whitelist).
 */
public class IpSpaceSpecializer implements GenericIpSpaceVisitor<IpSpace> {

  private final Set<IpWildcard> _dstIps;

  private final Set<IpWildcard> _notDstIps;

  public IpSpaceSpecializer(Set<IpWildcard> dstIps, Set<IpWildcard> notDstIps) {
    _dstIps = dstIps;
    _notDstIps = notDstIps;
  }

  @Override
  public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (IpSpace) o;
  }

  public IpSpace specialize(IpSpace ipSpace) {
    if (_dstIps.isEmpty() && _notDstIps.isEmpty()) {
      return ipSpace;
    } else {
      return ipSpace.accept(this);
    }
  }

  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    List<AclIpSpaceLine> specializedLines =
        aclIpSpace
            .getLines()
            .stream()
            // specialize the IpSpace of each acl line
            .map(
                aclIpSpaceLine ->
                    AclIpSpaceLine.builder()
                        .setAction(aclIpSpaceLine.getAction())
                        .setIpSpace(aclIpSpaceLine.getIpSpace().accept(this))
                        .build())
            // remove the lines with empty IpSpace
            .filter(aclIpSpaceLine -> !aclIpSpaceLine.getIpSpace().equals(EmptyIpSpace.INSTANCE))
            .collect(ImmutableList.toImmutableList());

    // remove any lines after a UniverseIpSpace
    int lastLine =
        ListUtils.indexOf(
            specializedLines, line -> line.getIpSpace().equals(UniverseIpSpace.INSTANCE));
    if (lastLine == 0) {
      // first line is UniverseIpSpace
      return specializedLines.get(0).getAction() == LineAction.ACCEPT
          ? UniverseIpSpace.INSTANCE
          : EmptyIpSpace.INSTANCE;
    } else if (lastLine != -1) {
      specializedLines = specializedLines.subList(0, lastLine + 1);
    }

    if (specializedLines.size() == 0) {
      return EmptyIpSpace.INSTANCE;
    } else if (specializedLines
        .stream()
        .allMatch(aclIpSpaceLine -> aclIpSpaceLine.getAction() == LineAction.REJECT)) {
      return EmptyIpSpace.INSTANCE;
    } else {
      return AclIpSpace.builder().setLines(specializedLines).build();
    }
  }

  @Override
  public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public IpSpace visitIp(Ip ip) {
    if (_notDstIps.stream().noneMatch(ipWildcard -> ipWildcard.containsIp(ip))
        && _dstIps.stream().anyMatch(ipWildcard -> ipWildcard.containsIp(ip))) {
      return ip;
    } else {
      return EmptyIpSpace.INSTANCE;
    }
  }

  @Override
  public IpSpace visitIpWildcard(IpWildcard ipWildcard) {
    if (_notDstIps.stream().anyMatch(ipWildcard::subsetOf)) {
      // blacklisted
      return EmptyIpSpace.INSTANCE;
    }

    if (_dstIps.stream().allMatch(ipWildcard::supersetOf)) {
      return UniverseIpSpace.INSTANCE;
    } else if (_dstIps.stream().anyMatch(ipWildcard::intersects)) {
      // some match
      return ipWildcard;
    } else {
      return EmptyIpSpace.INSTANCE;
    }
  }

  @Override
  public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    /*
     * Remove any blacklisted Ips that are already blacklisted by _notDstIps
     * or that don't match any _dstIps
     */
    Stream<IpWildcard> blacklistStream = ipWildcardSetIpSpace.getBlacklist().stream();
    if (!_notDstIps.isEmpty()) {
      blacklistStream =
          blacklistStream.filter(notDstIp -> _notDstIps.stream().noneMatch(notDstIp::subsetOf));
    }
    if (!_dstIps.isEmpty()) {
      blacklistStream =
          blacklistStream.filter(notDstIp -> _dstIps.stream().anyMatch(notDstIp::intersects));
    }
    Set<IpWildcard> blacklist = blacklistStream.collect(Collectors.toSet());

    /*
     * Remove any whitelisted Ips that are either blacklisted or don't overlap with the headerspace
     * whitelist.
     */
    Set<IpSpace> ipSpaceWhitelist =
        ipWildcardSetIpSpace
            .getWhitelist()
            .stream()
            .map(this::visitIpWildcard)
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
