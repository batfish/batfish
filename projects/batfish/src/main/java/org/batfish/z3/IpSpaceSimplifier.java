package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.batfish.datamodel.visitors.IpSpaceMayIntersectWildcard;

/**
 * Simplify an {@link IpSpace}. For example, there are many ways to express an empty {@link IpSpace}
 * or the entire universe of IPs. We try to detect these and convert them to {@link EmptyIpSpace} or
 * {@link UniverseIpSpace} respectively.
 */
public class IpSpaceSimplifier implements GenericIpSpaceVisitor<IpSpace> {
  private Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceSimplifier(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
  }

  public IpSpace simplify(IpSpace ipSpace) {
    if (ipSpace == null) {
      return null;
    }

    return ipSpace.accept(this);
  }

  @Override
  public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (IpSpace) o;
  }

  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    /*
     * To simplify an AclIpSpace: 1) Simplify the IpSpace of each line. 2) Remove EmptyIpSpace
     * lines. 3) Remove all lines after the first UniverseIpSpace line - More generally, we could
     * remove all lines whose spaces are covered by a previous line, but this is not implemented.
     * It's also probably too expensive to implement a complete IpSpace subset operation, so we'll
     * stick to the easy and most important case.
     */
    List<AclIpSpaceLine> simplifiedLines = new ArrayList<>();
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      IpSpace simplifiedLineIpSpace = line.getIpSpace().accept(this);
      if (simplifiedLineIpSpace == EmptyIpSpace.INSTANCE) {
        continue;
      }
      AclIpSpaceLine simplifiedLine = line.toBuilder().setIpSpace(simplifiedLineIpSpace).build();
      simplifiedLines.add(simplifiedLine);
      if (simplifiedLineIpSpace == UniverseIpSpace.INSTANCE) {
        break;
      }
    }

    /*
     * If there is only one line, and it accepts, then simplify to the space of that line.
     */
    if (simplifiedLines.size() == 1 && simplifiedLines.get(0).getAction() == LineAction.PERMIT) {
      return simplifiedLines.get(0).getIpSpace();
    }

    /*
     * If all lines reject (or there are no lines), simplify to EmptyIpSpace.
     */
    if (simplifiedLines.stream().allMatch(line -> line.getAction() == LineAction.DENY)) {
      return EmptyIpSpace.INSTANCE;
    }

    /*
     * If all lines are accepts, and the last accepts UniverseIpSpace, then this can be simplified
     * to UniverseIpSpace.
     */
    if (simplifiedLines
            .get(simplifiedLines.size() - 1)
            .getIpSpace()
            .equals(UniverseIpSpace.INSTANCE)
        && simplifiedLines.stream().allMatch(line -> line.getAction() == LineAction.PERMIT)) {
      return UniverseIpSpace.INSTANCE;
    }

    return AclIpSpace.builder().setLines(simplifiedLines).build();
  }

  @Override
  public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public IpSpace visitIpIpSpace(IpIpSpace ipIpSpace) {
    return ipIpSpace;
  }

  @Override
  public IpSpace visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    if (ipWildcardIpSpace.getIpWildcard().equals(IpWildcard.ANY)) {
      return UniverseIpSpace.INSTANCE;
    }
    return ipWildcardIpSpace;
  }

  @Override
  public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    // Remove whitelisted wildcards that are covered by blacklisted wildcards
    Set<IpWildcard> whitelist =
        ipWildcardSetIpSpace
            .getWhitelist()
            .stream()
            .filter(
                whitelistedIpWildcard ->
                    ipWildcardSetIpSpace
                        .getBlacklist()
                        .stream()
                        .noneMatch(whitelistedIpWildcard::subsetOf))
            .collect(Collectors.toSet());

    if (whitelist.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }

    // Remove blacklisted wildcards that don't overlap with whitelisted wildcards
    Set<IpWildcard> blacklist =
        ipWildcardSetIpSpace
            .getBlacklist()
            .stream()
            .filter(
                blacklistedIpWildcard -> {
                  IpSpaceMayIntersectWildcard mayIntersect =
                      new IpSpaceMayIntersectWildcard(blacklistedIpWildcard, _namedIpSpaces);
                  return whitelist
                      .stream()
                      .map(IpWildcard::toIpSpace)
                      .anyMatch(mayIntersect::visit);
                })
            .collect(Collectors.toSet());

    if (blacklist.isEmpty()) {
      if (whitelist.contains(IpWildcard.ANY)) {
        return UniverseIpSpace.INSTANCE;
      }
      if (whitelist.size() == 1) {
        return whitelist.iterator().next().toIpSpace();
      }
    }

    return IpWildcardSetIpSpace.builder().including(whitelist).excluding(blacklist).build();
  }

  @Override
  public IpSpace visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    if (prefixIpSpace.getPrefix().equals(Prefix.ZERO)) {
      return UniverseIpSpace.INSTANCE;
    }
    return prefixIpSpace;
  }

  @Override
  public IpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return universeIpSpace;
  }

  @Override
  public IpSpace visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    // todo cache simplified named IpSpaces?
    return _namedIpSpaces.get(ipSpaceReference.getName()).accept(this);
  }
}
