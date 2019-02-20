package org.batfish.question.multipath;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.question.specifiers.PathConstraintsUtil.createPathConstraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
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
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.traceroute.TracerouteAnswerer;
import org.batfish.specifier.FlexibleInferFromLocationIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;

public class MultipathConsistencyAnswerer extends Answerer {
  public MultipathConsistencyAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    MultipathConsistencyParameters parameters = parameters();
    Set<Flow> flows = _batfish.bddMultipathConsistency(parameters);
    SortedMap<Flow, List<Trace>> flowTraces = _batfish.buildFlows(flows, false);
    TableAnswerElement tableAnswer = new TableAnswerElement(TracerouteAnswerer.metadata(false));
    TracerouteAnswerer.flowTracesToRows(flowTraces, parameters.getMaxTraces())
        .forEach(tableAnswer::addRow);
    return tableAnswer;
  }

  private MultipathConsistencyParameters parameters() {
    MultipathConsistencyQuestion question = (MultipathConsistencyQuestion) _question;

    PacketHeaderConstraints headerConstraints = question.getHeaderConstraints();
    PathConstraints pathConstraints = createPathConstraints(question.getPathConstraints());

    SpecifierContext ctxt = _batfish.specifierContext();
    Set<String> forbiddenTransitNodes = pathConstraints.getForbiddenLocations().resolve(ctxt);
    Set<String> requiredTransitNodes = pathConstraints.getTransitLocations().resolve(ctxt);
    Set<Location> startLocations = pathConstraints.getStartLocation().resolve(ctxt);
    Set<String> finalNodes = pathConstraints.getEndLocation().resolve(ctxt);

    IpSpaceSpecifierFactory flexibleIpSpaceSpecifierFactory =
        new FlexibleInferFromLocationIpSpaceSpecifierFactory();
    IpSpaceAssignment ipSpaceAssignment =
        flexibleIpSpaceSpecifierFactory
            .buildIpSpaceSpecifier(headerConstraints.getSrcIps())
            .resolve(startLocations, ctxt);
    IpSpace dstIps =
        firstNonNull(
            AclIpSpace.union(
                flexibleIpSpaceSpecifierFactory.buildIpSpaceSpecifier(headerConstraints.getDstIps())
                    .resolve(ImmutableSet.of(), ctxt).getEntries().stream()
                    .map(Entry::getIpSpace)
                    .collect(ImmutableList.toImmutableList())),
            UniverseIpSpace.INSTANCE);
    AclLineMatchExpr headerSpace =
        match(
            PacketHeaderConstraintsUtil.toHeaderSpaceBuilder(headerConstraints)
                .setDstIps(dstIps)
                .build());

    return new MultipathConsistencyParameters(
        headerSpace,
        ipSpaceAssignment,
        finalNodes,
        forbiddenTransitNodes,
        question.getMaxTraces(),
        requiredTransitNodes);
  }
}
