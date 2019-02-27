package org.batfish.question.differentialreachability;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.question.specifiers.PathConstraintsUtil.createPathConstraints;
import static org.batfish.question.traceroute.TracerouteAnswerer.diffFlowTracesToRows;
import static org.batfish.question.traceroute.TracerouteAnswerer.metadata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.PathConstraints;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An {@link Answerer} for {@link DifferentialReachabilityQuestion}. */
public class DifferentialReachabilityAnswerer extends Answerer {
  public static final String COL_FLOW = "flow";

  public DifferentialReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer() {
    return answerDiff();
  }

  private DifferentialReachabilityParameters parameters() {
    DifferentialReachabilityQuestion question = (DifferentialReachabilityQuestion) _question;
    PacketHeaderConstraints headerConstraints = question.getHeaderConstraints();
    SpecifierContext ctxt = _batfish.specifierContext();

    PathConstraints pathConstraints = createPathConstraints(question.getPathConstraints());
    Set<String> forbiddenTransitNodes = pathConstraints.getForbiddenLocations().resolve(ctxt);
    Set<String> requiredTransitNodes = pathConstraints.getTransitLocations().resolve(ctxt);
    Set<Location> startLocations = pathConstraints.getStartLocation().resolve(ctxt);
    Set<String> finalNodes = pathConstraints.getEndLocation().resolve(ctxt);

    IpSpaceAssignment ipSpaceAssignment =
        SpecifierFactories.getIpSpaceSpecifierOrDefault(
                headerConstraints.getSrcIps(), InferFromLocationIpSpaceSpecifier.INSTANCE)
            .resolve(startLocations, ctxt);
    IpSpace dstIps =
        firstNonNull(
            AclIpSpace.union(
                SpecifierFactories.getIpSpaceSpecifierOrDefault(
                        headerConstraints.getDstIps(), InferFromLocationIpSpaceSpecifier.INSTANCE)
                    .resolve(ImmutableSet.of(), ctxt).getEntries().stream()
                    .map(Entry::getIpSpace)
                    .collect(ImmutableList.toImmutableList())),
            UniverseIpSpace.INSTANCE);
    AclLineMatchExpr headerSpace =
        match(
            PacketHeaderConstraintsUtil.toHeaderSpaceBuilder(headerConstraints)
                .setDstIps(dstIps)
                .build());

    return new DifferentialReachabilityParameters(
        ReachabilityParameters.filterDispositions(question.getActions().getDispositions()),
        forbiddenTransitNodes,
        finalNodes,
        headerSpace,
        question.getIgnoreFilters(),
        question.getInvertSearch(),
        ipSpaceAssignment,
        question.getMaxTraces(),
        requiredTransitNodes);
  }

  @Override
  public TableAnswerElement answerDiff() {
    DifferentialReachabilityParameters parameters = parameters();
    DifferentialReachabilityResult result = _batfish.bddDifferentialReachability(parameters);

    Set<Flow> flows =
        Sets.union(result.getDecreasedReachabilityFlows(), result.getIncreasedReachabilityFlows());
    Multiset<Row> rows;
    TableAnswerElement table;
    _batfish.pushBaseSnapshot();
    Map<Flow, List<Trace>> baseFlowTraces =
        _batfish.buildFlows(flows, parameters.getIgnoreFilters());
    _batfish.popSnapshot();

    _batfish.pushDeltaSnapshot();
    Map<Flow, List<Trace>> deltaFlowTraces =
        _batfish.buildFlows(flows, parameters.getIgnoreFilters());
    _batfish.popSnapshot();

    rows = diffFlowTracesToRows(baseFlowTraces, deltaFlowTraces, parameters.getMaxTraces());
    table = new TableAnswerElement(metadata(true));
    table.postProcessAnswer(_question, rows);
    return table;
  }
}
