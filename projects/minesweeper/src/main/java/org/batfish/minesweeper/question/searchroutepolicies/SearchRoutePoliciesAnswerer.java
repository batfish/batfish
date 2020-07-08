package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.NODE;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteConstraints;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.PolicyQuotient;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An answerer for {@link SearchRoutePoliciesQuestion}. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesAnswerer extends Answerer {
  public static final String COL_NODE = "Node";
  public static final String COL_POLICY_NAME = "Policy_Name";
  public static final String COL_INPUT_ROUTE = "Input_Route";
  public static final String COL_ACTION = "Action";
  public static final String COL_OUTPUT_ROUTE = "Output_Route";

  private final RouteConstraints _inputConstraints;
  private final RouteConstraints _outputConstraints;
  private final String _nodes;
  private final String _policies;
  private final Action _action;

  private Graph _g;
  private PolicyQuotient _pq;

  private BDD _inputConstraintsBDD;

  public SearchRoutePoliciesAnswerer(SearchRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _inputConstraints = question.getInputConstraints();
    _outputConstraints = question.getOutputConstraints();
    _nodes = question.getNodes();
    _policies = question.getPolicies();
    _action = question.getAction();

    _g = new Graph(batfish, batfish.getSnapshot());
    _pq = new PolicyQuotient();

    _inputConstraintsBDD =
        routeConstraintsToBDD(_inputConstraints, new BDDRoute(_g.getAllCommunities()));
  }

  /**
   * Convert a bdd representing a single assignment to the variables from a BDDRoute, produce the
   * corresponding route.
   *
   * @param bdd the bdd
   * @return the corresponding route
   */
  private Bgpv4Route bddToRoute(BDD bdd, BDDRoute r) {
    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP);
    Ip ip = Ip.create(r.getPrefix().satAssignmentToLong(bdd));
    long len = r.getPrefixLength().satAssignmentToLong(bdd);
    builder.setNetwork(Prefix.create(ip, (int) len));

    builder.setLocalPreference(r.getLocalPref().satAssignmentToLong(bdd));
    builder.setAdmin((int) (long) r.getAdminDist().satAssignmentToLong(bdd));
    // TODO: BDDRoute has a med and a metric -- what is the difference?
    builder.setMetric(r.getMed().satAssignmentToLong(bdd));

    ImmutableSet.Builder<Community> comms = new ImmutableSet.Builder<>();
    for (Entry<CommunityVar, BDD> commEntry : r.getCommunities().entrySet()) {
      CommunityVar commVar = commEntry.getKey();
      BDD commBDD = commEntry.getValue();
      if (!commBDD.and(bdd).isZero()) {
        // TODO for now we only handle regexes that represent literal community values
        Community lit = commVar.getLiteralValue();
        if (lit != null) {
          comms.add(commVar.getLiteralValue());
        } else {
          String regex = commVar.getRegex();
          if (regex.startsWith("^") && regex.endsWith("$")) {
            try {
              comms.add(Community.fromString(regex.substring(1, regex.length() - 1)));
            } catch (Exception e) {
              throw new BatfishException("Unhandled community regex: " + regex, e);
            }
          } else {
            throw new BatfishException("Unhandled community regex: " + regex);
          }
        }
      }
    }
    builder.setCommunities(comms.build());

    return builder.build();
  }

  private SortedSet<RoutingPolicyId> resolvePolicies(SpecifierContext context) {
    NodeSpecifier nodeSpec =
        SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);

    RoutingPolicySpecifier policySpec =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(_policies, ALL_ROUTING_POLICIES);

    return nodeSpec.resolve(context).stream()
        .flatMap(
            node ->
                policySpec.resolve(node, context).stream()
                    .map(policy -> new RoutingPolicyId(node, policy.getName())))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  private BDD prefixSpaceToBDD(PrefixSpace space, BDDRoute r, boolean complementPrefixes) {
    BDDFactory factory = r.getPrefix().getFactory();
    if (space.isEmpty()) {
      return factory.one();
    } else {
      BDD result = factory.zero();
      for (PrefixRange range : space.getPrefixRanges()) {
        BDD rangeBDD = isRelevantFor(r, range);
        result = result.or(rangeBDD);
      }
      if (complementPrefixes) {
        result = result.not();
      }
      return result;
    }
  }

  private BDD integerSpaceToBDD(IntegerSpace space, BDDInteger bddInt) {
    if (space.isEmpty()) {
      return bddInt.getFactory().one();
    } else {
      BDD result = bddInt.getFactory().zero();
      for (SubRange range : space.getSubRanges()) {
        result = result.or(bddInt.range(range.getStart(), range.getEnd()));
      }
      return result;
    }
  }

  private BDD routeConstraintsToBDD(RouteConstraints constraints, BDDRoute r) {
    BDD prefixRange =
        prefixSpaceToBDD(constraints.getPrefixSpace(), r, constraints.getComplementPrefixSpace());
    BDD localPref = integerSpaceToBDD(constraints.getLocalPref(), r.getLocalPref());
    BDD med = integerSpaceToBDD(constraints.getMed(), r.getMed());

    return prefixRange.and(localPref).and(med);
  }

  private Optional<Result> searchPolicy(RoutingPolicy policy) {
    TransferBDD tbdd;
    try {
      tbdd = new TransferBDD(_g, policy.getOwner(), policy.getStatements(), _pq);
    } catch (Exception e) {
      throw new BatfishException(
          "Unsupported features in route policy "
              + policy.getName()
              + " in node "
              + policy.getOwner().getHostname(),
          e);
    }
    TransferReturn result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outputRoute = result.getFirst();
    BDD intersection;
    if (_action == Action.PERMIT) {
      // incorporate the constraints on the output route as well
      BDD outConstraints = routeConstraintsToBDD(_outputConstraints, outputRoute);
      intersection = acceptedAnnouncements.and(_inputConstraintsBDD).and(outConstraints);
    } else {
      intersection = acceptedAnnouncements.not().and(_inputConstraintsBDD);
    }

    if (intersection.isZero()) {
      return Optional.empty();
    } else {
      BDD model = intersection.fullSatOne();
      Bgpv4Route inRoute = bddToRoute(model, new BDDRoute(_g.getAllCommunities()));
      Bgpv4Route outRoute = _action == Action.PERMIT ? bddToRoute(model, outputRoute) : null;
      return Optional.of(
          new Result(
              new RoutingPolicyId(policy.getOwner().getHostname(), policy.getName()),
              inRoute,
              _action,
              outRoute));
    }
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext context = _batfish.specifierContext(snapshot);
    SortedSet<RoutingPolicyId> policies = resolvePolicies(context);
    Multiset<Row> rows =
        getPolicies(context, policies)
            .map(this::searchPolicy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(SearchRoutePoliciesAnswerer::toRow)
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @Nonnull
  private Stream<RoutingPolicy> getPolicies(
      SpecifierContext context, SortedSet<RoutingPolicyId> policies) {
    Map<String, Configuration> configs = context.getConfigs();
    return policies.stream()
        .map(
            policyId ->
                configs.get(policyId.getNode()).getRoutingPolicies().get(policyId.getPolicy()));
  }

  @Nullable
  private static org.batfish.datamodel.questions.BgpRoute toQuestionsBgpRoute(
      @Nullable Bgpv4Route dataplaneBgpRoute) {
    if (dataplaneBgpRoute == null) {
      return null;
    }
    return org.batfish.datamodel.questions.BgpRoute.builder()
        .setWeight(dataplaneBgpRoute.getWeight())
        .setNextHopIp(dataplaneBgpRoute.getNextHopIp())
        .setProtocol(dataplaneBgpRoute.getProtocol())
        .setSrcProtocol(dataplaneBgpRoute.getSrcProtocol())
        .setOriginType(dataplaneBgpRoute.getOriginType())
        .setOriginatorIp(dataplaneBgpRoute.getOriginatorIp())
        .setMetric(dataplaneBgpRoute.getMetric())
        .setLocalPreference(dataplaneBgpRoute.getLocalPreference())
        .setWeight(dataplaneBgpRoute.getWeight())
        .setNetwork(dataplaneBgpRoute.getNetwork())
        .setCommunities(dataplaneBgpRoute.getCommunities())
        .setAsPath(dataplaneBgpRoute.getAsPath())
        .build();
  }

  public static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, NODE, "The node that has the policy", true, false),
            new ColumnMetadata(COL_POLICY_NAME, STRING, "The name of this policy", true, false),
            new ColumnMetadata(COL_INPUT_ROUTE, BGP_ROUTE, "The input route", true, false),
            new ColumnMetadata(
                COL_ACTION, STRING, "The action of the policy on the input route", false, true),
            new ColumnMetadata(COL_OUTPUT_ROUTE, BGP_ROUTE, "The input route", false, false));
    return new TableMetadata(
        columnMetadata, String.format("Results for policy ${%s}", COL_POLICY_NAME));
  }

  private static Row toRow(Result result) {
    org.batfish.datamodel.questions.BgpRoute inputRoute =
        toQuestionsBgpRoute(result.getInputRoute());
    org.batfish.datamodel.questions.BgpRoute outputRoute =
        toQuestionsBgpRoute(result.getOutputRoute());
    Action action = result.getAction();
    RoutingPolicyId policyId = result.getPolicyId();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(COL_ACTION, action)
        .put(COL_OUTPUT_ROUTE, outputRoute)
        .build();
  }
}
