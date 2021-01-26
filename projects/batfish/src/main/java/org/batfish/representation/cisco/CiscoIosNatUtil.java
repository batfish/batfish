package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.Builder;

/** Utility methods related to {@link CiscoIosNat}. */
@ParametersAreNonnullByDefault
final class CiscoIosNatUtil {
  /*
   * Initialize the type precedence for the supported NAT types.
   * Currently, the only rule is that Static NATs take precedence over dynamic NATs
   */
  private static final ImmutableMap<String, Integer> typePrecedence =
      ImmutableMap.of(
          CiscoIosDynamicNat.class.getSimpleName(), 1,
          CiscoIosStaticNat.class.getSimpleName(), 0);

  private CiscoIosNatUtil() {}

  /** The relative order of precedence for this NAT's type */
  static int getTypePrecedence(CiscoIosNat nat) {
    String name = nat.getClass().getSimpleName();
    if (!typePrecedence.containsKey(name)) {
      throw new BatfishException("Unsupported NAT type");
    }
    return typePrecedence.get(name);
  }

  /**
   * Converts the given {@link RouteMap} to an {@link AclLineMatchExpr} for use as a guard in a
   * {@link Transformation}. Currently only offers limited support (pending behavior tests); returns
   * empty if the route-map has any of the following:
   *
   * <ul>
   *   <li>deny clauses
   *   <li>set lines
   *   <li>match lines other than matching v4 ACLs
   *   <li>references to undefined ACLs
   *   <li>empty clauses or no clauses
   * </ul>
   *
   * Also returns empty if the route-map is supported but not matchable for the given interface.
   *
   * @param routeMap The {@link RouteMap} to convert
   * @param validAclNames Named ACLs in the config
   * @param ifaceName The outside interface for which this match expr will be used
   */
  static Optional<AclLineMatchExpr> toMatchExpr(
      RouteMap routeMap, Set<String> validAclNames, String ifaceName, Warnings w) {
    List<AclLineMatchExpr> clauseExprs = new ArrayList<>();
    if (routeMap.getClauses().isEmpty()) {
      w.redFlag(String.format("Ignoring NAT rule with empty route-map %s", routeMap.getName()));
      return Optional.empty();
    }
    for (RouteMapClause clause : routeMap.getClauses().values()) {
      Optional<AclLineMatchExpr> clauseExpr =
          clauseToMatchExpr(clause, routeMap.getName(), validAclNames, ifaceName, w);
      if (!clauseExpr.isPresent()) {
        // Clause couldn't be converted. Warning already filed.
        return Optional.empty();
      } else if (!clauseExpr.get().equals(AclLineMatchExprs.FALSE)) {
        // Clause is supported but not matchable; don't bother adding it to route-map expr
        clauseExprs.add(clauseExpr.get());
      }
    }
    // If none of the clauses were matchable, return empty optional
    return clauseExprs.isEmpty() ? Optional.empty() : Optional.of(or(clauseExprs));
  }

  /**
   * Converts the given {@link RouteMapClause} to an {@link AclLineMatchExpr}. Returns an empty
   * Optional if the clause can't be converted.
   */
  @VisibleForTesting
  static Optional<AclLineMatchExpr> clauseToMatchExpr(
      RouteMapClause clause,
      String rmName,
      Set<String> validAclNames,
      String ifaceName,
      Warnings w) {
    if (clause.getAction() != LineAction.PERMIT) {
      // TODO Support NAT rules referencing route-maps with deny clauses
      w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: deny clauses not supported in this"
                  + " context",
              rmName, clause.getSeqNum()));
      return Optional.empty();
    } else if (!clause.getSetList().isEmpty()) {
      // TODO Check if set lines take effect in context of NAT rule-matching
      w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: set lines not supported in this context",
              rmName, clause.getSeqNum()));
      return Optional.empty();
    } else if (clause.getMatchList().isEmpty()) {
      // TODO Check behavior of empty clauses (deny all or permit all?)
      w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: clauses without match lines not supported in"
                  + " this context",
              rmName, clause.getSeqNum()));
      return Optional.empty();
    }
    List<AclLineMatchExpr> clauseConjuncts = new ArrayList<>();
    boolean matchable = true;
    RouteMapMatchLineToExprVisitor toExprVisitor =
        new RouteMapMatchLineToExprVisitor(rmName, clause.getSeqNum(), validAclNames, ifaceName, w);
    for (RouteMapMatchLine matchLine : clause.getMatchList()) {
      Optional<AclLineMatchExpr> lineExpr = matchLine.accept(toExprVisitor);
      if (!lineExpr.isPresent()) {
        // Line couldn't be converted. Warning already filed.
        return Optional.empty();
      } else if (lineExpr.get().equals(AclLineMatchExprs.FALSE)) {
        // Since this line can't be matched, the clause can't be matched.
        // Don't return yet in case there's still an inconvertible line.
        matchable = false;
      } else {
        clauseConjuncts.add(lineExpr.get());
      }
    }
    if (!matchable) {
      return Optional.of(AclLineMatchExprs.FALSE);
    }
    // matchLines weren't empty, and we would have returned already if we'd hit any match line that
    // didn't contribute to clauseConjuncts, so clauseConjuncts must not be empty
    assert !clauseConjuncts.isEmpty();
    return Optional.of(and(clauseConjuncts));
  }

  @VisibleForTesting
  static class RouteMapMatchLineToExprVisitor
      implements RouteMapMatchLine.RouteMapMatchLineVisitor<Optional<AclLineMatchExpr>> {
    @Nonnull private final String _ifaceName;
    @Nonnull private final String _rmName;
    private final int _seqNum;
    @Nonnull private final Set<String> _validAclNames;
    @Nonnull private final Warnings _w;

    RouteMapMatchLineToExprVisitor(
        String rmName, int seqNum, Set<String> validAclNames, String ifaceName, Warnings w) {
      _ifaceName = ifaceName;
      _rmName = rmName;
      _seqNum = seqNum;
      _validAclNames = validAclNames;
      _w = w;
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchAsPathAccessListLine(
        RouteMapMatchAsPathAccessListLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match as-path-access-list not"
                  + " supported in this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchCommunityListLine(
        RouteMapMatchCommunityListLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match community not supported in"
                  + " this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchExtcommunityLine(
        RouteMapMatchExtcommunityLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match extcommunity not supported"
                  + " in this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchInterfaceLine(
        RouteMapMatchInterfaceLine line) {
      // Clause only matches traffic fwded to certain outside ifaces; see last paragraph here:
      // https://www.cisco.com/c/en/us/support/docs/ip/network-address-translation-nat/13739-nat-routemap.html
      // TODO Should a match interface line be ignored if it includes undefined interfaces?
      //  For now, we just check if the line mentions the interface this expr is being created for.
      if (line.getInterfaceNames().contains(_ifaceName)) {
        return Optional.of(toExpr(_ifaceName));
      }
      return Optional.of(AclLineMatchExprs.FALSE);
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchIpAccessListLine(
        RouteMapMatchIpAccessListLine line) {
      Set<String> missingNames = Sets.difference(line.getListNames(), _validAclNames);
      if (!missingNames.isEmpty()) {
        // TODO Check behavior of match ACL line when some or all ACLs are undefined
        _w.redFlag(
            String.format(
                "Ignoring NAT rule with route-map %s %d: route-map references undefined"
                    + " access-lists %s",
                _rmName, _seqNum, missingNames));
        return Optional.empty();
      }
      // Never need to reverse these ACLs because route-maps can't be used for destination inside.
      List<AclLineMatchExpr> permittedByAcls =
          line.getListNames().stream()
              .map(AclLineMatchExprs::permittedByAcl)
              .collect(ImmutableList.toImmutableList());
      return Optional.of(or(permittedByAcls));
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchIpPrefixListLine(
        RouteMapMatchIpPrefixListLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match ip address prefix-list not"
                  + " supported in this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchIpv6AccessListLine(
        RouteMapMatchIpv6AccessListLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match ipv6 address not supported"
                  + " in this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchIpv6PrefixListLine(
        RouteMapMatchIpv6PrefixListLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match ipv6 address prefix-list"
                  + " not supported in this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchSourceProtocolLine(
        RouteMapMatchSourceProtocolLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match source-protocol not"
                  + " supported in this context",
              _rmName, _seqNum));
      return Optional.empty();
    }

    @Override
    public Optional<AclLineMatchExpr> visitRouteMapMatchTagLine(RouteMapMatchTagLine line) {
      // TODO What happens here?
      _w.redFlag(
          String.format(
              "Ignoring NAT rule with route-map %s %d: match tag not supported in this"
                  + " context",
              _rmName, _seqNum));
      return Optional.empty();
    }
  }

  /**
   * Returns a {@link TrueExpr} with a trace element indicating that the expr was matched because
   * the traffic is destined for the given outside interface. Does not need to actually match on any
   * property of the traffic, because this expr will only be used on the given outside interface.
   */
  @VisibleForTesting
  static AclLineMatchExpr toExpr(String ifaceName) {
    return new TrueExpr(TraceElement.of(String.format("Matched outside interface %s", ifaceName)));
  }

  @Nonnull
  static Transformation toOutgoingTransformationChain(Map<CiscoIosNat, Builder> convertedNats) {
    return toTransformationChain(convertedNats, true);
  }

  @Nonnull
  static Transformation toIncomingTransformationChain(
      Map<CiscoIosNat, Transformation.Builder> convertedNats) {
    return toTransformationChain(convertedNats, false);
  }

  @Nonnull
  private static Transformation toTransformationChain(
      Map<CiscoIosNat, Transformation.Builder> convertedNats, boolean outgoing) {

    Map<IpField, List<Builder>> transformationsByField =
        convertedNats.keySet().stream()
            .sorted()
            .collect(
                Collectors.groupingBy(
                    nat -> nat.getAction().whatChanges(outgoing),
                    Collectors.mapping(convertedNats::get, Collectors.toList())));

    if (!Sets.difference(
                transformationsByField.keySet(),
                ImmutableSet.of(IpField.SOURCE, IpField.DESTINATION))
            .isEmpty()
        || transformationsByField.isEmpty()) {
      throw new BatfishException("Invalid transformations");
    }

    /*
    * transformationsByField contains non-empty lists of non-null transformations, sorted by the
    * order in which they are to be evaluated.

    * There are currently only two possible field transformations (source and destination).
    * Doesn't matter if SOURCE or DESTINATION is transformed first, pick a non-empty list
    * This is true so far for IOS NATs. ASA NATs can condition on both fields and modify both
    * fields, but that would be in a separate list.
    */
    List<Transformation.Builder> source = transformationsByField.get(IpField.SOURCE);
    List<Transformation.Builder> destination = transformationsByField.get(IpField.DESTINATION);

    // If there is only one field modified, chain all transformations with orElse
    Transformation onlyOrDestinationTransform = chain(firstNonNull(destination, source), null);
    if (transformationsByField.keySet().size() == 1) {
      return onlyOrDestinationTransform;
    }

    // If there is more than one field, chain each list with orElse and subsequent lists with
    // andThen
    return chain(source, onlyOrDestinationTransform);
  }

  private static Transformation chain(
      List<Transformation.Builder> nonEmptySortedList, @Nullable Transformation andThen) {
    // Start at the end of the chain and go backwards. The end of the chain should have
    // t.andThen == t.orElse
    Transformation previous = andThen;
    for (Transformation.Builder t : Lists.reverse(nonEmptySortedList)) {
      previous = t.setAndThen(andThen).setOrElse(previous).build();
    }
    return previous;
  }
}
