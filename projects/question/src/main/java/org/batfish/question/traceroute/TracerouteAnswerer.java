package org.batfish.question.traceroute;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.TracePruner;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.specifier.FlexibleInferFromLocationIpSpaceSpecifierFactory;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;
import org.batfish.specifier.LocationVisitor;
import org.batfish.specifier.SpecifierContext;

/** Produces the answer for {@link TracerouteQuestion} */
public final class TracerouteAnswerer extends Answerer {

  private static final String SRC_LOCATION_SPECIFIER_FACTORY =
      FlexibleLocationSpecifierFactory.NAME;
  private static final String IP_SPECIFIER_FACTORY =
      FlexibleInferFromLocationIpSpaceSpecifierFactory.NAME;

  public static final String COL_FLOW = "Flow";
  public static final String COL_TRACES = "Traces";
  public static final String COL_TRACE_COUNT = "TraceCount";
  public static final String COL_BASE_TRACES = TableDiff.baseColumnName(COL_TRACES);
  public static final String COL_DELTA_TRACES = TableDiff.deltaColumnName(COL_TRACES);
  public static final String COL_BASE_TRACE_COUNT = TableDiff.baseColumnName(COL_TRACE_COUNT);
  public static final String COL_DELTA_TRACE_COUNT = TableDiff.deltaColumnName(COL_TRACE_COUNT);
  private static final int TRACEROUTE_PORT = 33434;

  private final Map<String, Configuration> _configurations;
  private final IpSpaceRepresentative _ipSpaceRepresentative;
  private final IpSpaceAssignment _sourceIpAssignment;

  TracerouteAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
    _configurations = batfish.loadConfigurations();
    _ipSpaceRepresentative = new IpSpaceRepresentative();
    _sourceIpAssignment = initSourceIpAssignment();
  }

  @VisibleForTesting
  IpSpaceAssignment initSourceIpAssignment() {
    /* construct specifiers */
    TracerouteQuestion tracerouteQuestion = (TracerouteQuestion) _question;
    LocationSpecifier sourceLocationSpecifier =
        LocationSpecifierFactory.load(SRC_LOCATION_SPECIFIER_FACTORY)
            .buildLocationSpecifier(tracerouteQuestion.getSourceLocationStr());

    IpSpaceSpecifier sourceIpSpaceSpecifier =
        IpSpaceSpecifierFactory.load(IP_SPECIFIER_FACTORY)
            .buildIpSpaceSpecifier(tracerouteQuestion.getHeaderConstraints().getSrcIps());

    /* resolve specifiers */
    SpecifierContext ctxt = _batfish.specifierContext();
    Set<Location> sourceLocations = sourceLocationSpecifier.resolve(ctxt);
    return sourceIpSpaceSpecifier.resolve(sourceLocations, ctxt);
  }

  @Override
  public AnswerElement answer() {
    TracerouteQuestion question = (TracerouteQuestion) _question;
    String tag = _batfish.getFlowTag();
    Set<Flow> flows = getFlows(tag);
    Multiset<Row> rows;
    TableAnswerElement table;
    if (_batfish.debugFlagEnabled("oldtraceroute")) {
      _batfish.processFlows(flows, question.getIgnoreFilters());
      FlowHistory flowHistory = _batfish.getHistory();
      rows = flowHistoryToRows(flowHistory, false);
      table = new TableAnswerElement(createMetadata(false));
    } else {
      SortedMap<Flow, List<Trace>> flowTraces =
          _batfish.buildFlows(flows, question.getIgnoreFilters());
      rows = flowTracesToRows(flowTraces, question.getMaxTraces());
      table = new TableAnswerElement(metadata(false));
    }
    table.postProcessAnswer(_question, rows);
    return table;
  }

  @Override
  public AnswerElement answerDiff() {
    TracerouteQuestion question = ((TracerouteQuestion) _question);
    boolean ignoreFilters = question.getIgnoreFilters();
    Set<Flow> flows = getFlows(_batfish.getDifferentialFlowTag());
    Multiset<Row> rows;
    TableAnswerElement table;
    if (_batfish.debugFlagEnabled("oldtraceroute")) {
      _batfish.pushBaseSnapshot();
      _batfish.processFlows(flows, ignoreFilters);
      _batfish.popSnapshot();

      _batfish.pushDeltaSnapshot();
      _batfish.processFlows(flows, ignoreFilters);
      _batfish.popSnapshot();

      FlowHistory flowHistory = _batfish.getHistory();
      rows = flowHistoryToRows(flowHistory, true);
      table = new TableAnswerElement(createMetadata(true));
    } else {
      _batfish.pushBaseSnapshot();
      Map<Flow, List<Trace>> baseFlowTraces = _batfish.buildFlows(flows, ignoreFilters);
      _batfish.popSnapshot();

      _batfish.pushDeltaSnapshot();
      Map<Flow, List<Trace>> deltaFlowTraces = _batfish.buildFlows(flows, ignoreFilters);
      _batfish.popSnapshot();

      rows = diffFlowTracesToRows(baseFlowTraces, deltaFlowTraces, question.getMaxTraces());
      table = new TableAnswerElement(metadata(true));
    }
    table.postProcessAnswer(_question, rows);
    return table;
  }

  public static TableMetadata createMetadata(boolean differential) {
    List<ColumnMetadata> columnMetadata;
    if (differential) {
      columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, false),
              new ColumnMetadata(
                  TableDiff.baseColumnName(COL_TRACES),
                  Schema.set(Schema.FLOW_TRACE),
                  "The flow traces in the BASE environment",
                  false,
                  true),
              new ColumnMetadata(
                  TableDiff.deltaColumnName(COL_TRACES),
                  Schema.set(Schema.FLOW_TRACE),
                  "The flow traces in the DELTA environment",
                  false,
                  true));
    } else {
      columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, false),
              new ColumnMetadata(
                  COL_TRACES, Schema.set(Schema.FLOW_TRACE), "The flow traces", false, true));
    }
    return new TableMetadata(columnMetadata, String.format("Paths for flow ${%s}", COL_FLOW));
  }

  /** Create metadata for the new traceroute v2 answer */
  public static TableMetadata metadata(boolean differential) {
    List<ColumnMetadata> columnMetadata;
    if (differential) {
      columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, false),
              new ColumnMetadata(
                  TableDiff.baseColumnName(COL_TRACES),
                  Schema.set(Schema.TRACE),
                  "The traces in the BASE snapshot",
                  false,
                  true),
              new ColumnMetadata(
                  TableDiff.baseColumnName(COL_TRACE_COUNT),
                  Schema.INTEGER,
                  "The total number traces in the BASE snapshot",
                  false,
                  true),
              new ColumnMetadata(
                  TableDiff.deltaColumnName(COL_TRACES),
                  Schema.set(Schema.TRACE),
                  "The traces in the DELTA snapshot",
                  false,
                  true),
              new ColumnMetadata(
                  TableDiff.deltaColumnName(COL_TRACE_COUNT),
                  Schema.INTEGER,
                  "The total number traces in the DELTA snapshot",
                  false,
                  true));
    } else {
      columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, false),
              new ColumnMetadata(
                  COL_TRACES, Schema.set(Schema.TRACE), "The traces for this flow", false, true),
              new ColumnMetadata(
                  COL_TRACE_COUNT,
                  Schema.INTEGER,
                  "The total number traces for this flow",
                  false,
                  true));
    }
    return new TableMetadata(columnMetadata, String.format("Paths for flow ${%s}", COL_FLOW));
  }

  /**
   * Converts {@code FlowHistoryInfo} into {@link Row}. Expects that the history object contains
   * traces for only one environment
   */
  static Row flowHistoryToRow(FlowHistoryInfo historyInfo) {
    // there should be only environment in this object
    checkArgument(
        historyInfo.getPaths().size() == 1,
        String.format(
            "Expect only one environment in flow history info. Found %d",
            historyInfo.getPaths().size()));
    Set<FlowTrace> paths =
        historyInfo.getPaths().values().stream().findAny().orElseGet(ImmutableSet::of);
    return Row.of(COL_FLOW, historyInfo.getFlow(), COL_TRACES, paths);
  }

  /**
   * Converts {@code FlowHistoryInfo} into {@link Row}. Expects that the history object contains
   * traces for base and delta environments
   */
  private static Row diffFlowHistoryToRow(FlowHistoryInfo historyInfo) {
    // there should only be two environments in this object
    checkArgument(
        historyInfo.getPaths().size() == 2,
        String.format(
            "Expect exactly two environments in flow history info. Found %d",
            historyInfo.getPaths().size()));
    return Row.of(
        COL_FLOW,
        historyInfo.getFlow(),
        TableDiff.baseColumnName(COL_TRACES),
        historyInfo.getPaths().get(Flow.BASE_FLOW_TAG),
        TableDiff.deltaColumnName(COL_TRACES),
        historyInfo.getPaths().get(Flow.DELTA_FLOW_TAG));
  }

  /** Converts a flowHistory object into a set of Rows. */
  public static Multiset<Row> flowHistoryToRows(FlowHistory flowHistory, boolean differential) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    if (differential) {
      for (FlowHistoryInfo historyInfo : flowHistory.getTraces().values()) {
        rows.add(diffFlowHistoryToRow(historyInfo));
      }
    } else {
      for (FlowHistoryInfo historyInfo : flowHistory.getTraces().values()) {
        rows.add(flowHistoryToRow(historyInfo));
      }
    }
    return rows;
  }

  public static Multiset<Row> flowTracesToRows(
      SortedMap<Flow, List<Trace>> flowTraces, int maxTraces) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    for (Map.Entry<Flow, List<Trace>> flowTrace : flowTraces.entrySet()) {
      List<Trace> traces = flowTrace.getValue();
      List<Trace> prunedTraces = TracePruner.prune(traces, maxTraces);
      rows.add(
          Row.of(
              COL_FLOW,
              flowTrace.getKey(),
              COL_TRACES,
              prunedTraces,
              COL_TRACE_COUNT,
              traces.size()));
    }
    return rows;
  }

  public static Multiset<Row> diffFlowTracesToRows(
      Map<Flow, List<Trace>> baseFlowTraces,
      Map<Flow, List<Trace>> deltaFlowTraces,
      int maxTraces) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    checkArgument(
        baseFlowTraces.keySet().equals(deltaFlowTraces.keySet()),
        "Base and delta flow traces should have same flows");
    for (Flow flow : baseFlowTraces.keySet()) {
      rows.add(
          Row.of(
              COL_FLOW,
              flow,
              TableDiff.baseColumnName(COL_TRACES),
              TracePruner.prune(baseFlowTraces.get(flow), maxTraces),
              TableDiff.baseColumnName(COL_TRACE_COUNT),
              baseFlowTraces.get(flow).size(),
              TableDiff.deltaColumnName(COL_TRACES),
              TracePruner.prune(deltaFlowTraces.get(flow), maxTraces),
              TableDiff.deltaColumnName(COL_TRACE_COUNT),
              deltaFlowTraces.get(flow).size()));
    }
    return rows;
  }

  /** Generate a set of flows to do traceroute */
  @VisibleForTesting
  Set<Flow> getFlows(String tag) {
    TracerouteQuestion question = (TracerouteQuestion) _question;

    Set<Location> srcLocations =
        LocationSpecifierFactory.load(SRC_LOCATION_SPECIFIER_FACTORY)
            .buildLocationSpecifier(question.getSourceLocationStr())
            .resolve(_batfish.specifierContext());

    ImmutableSet.Builder<Flow> setBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<String> allProblems = ImmutableSet.builder();

    // Perform cross-product of all locations to flows
    for (Location srcLocation : srcLocations) {
      try {
        Flow.Builder flowBuilder =
            headerConstraintsToFlow(question.getHeaderConstraints(), srcLocation);
        setSourceLocation(flowBuilder, srcLocation);
        flowBuilder.setTag(tag);
        setBuilder.add(flowBuilder.build());
      } catch (IllegalArgumentException e) {
        // Try to ignore silently if possible
        allProblems.add(e.getMessage());
      }
    }

    Set<Flow> flows = setBuilder.build();
    checkArgument(
        !flows.isEmpty(),
        "Could not construct a flow for traceroute. Found issues: %s",
        String.join(",", allProblems.build()));
    return flows;
  }

  /**
   * Generate a flow builder given some set of packet header constraints.
   *
   * @param constraints {@link PacketHeaderConstraints}
   * @throws IllegalArgumentException if the {@code constraints} cannot be resolved to a single
   *     value.
   */
  private Flow.Builder headerConstraintsToFlow(
      PacketHeaderConstraints constraints, Location srcLocation) throws IllegalArgumentException {
    Flow.Builder builder = PacketHeaderConstraintsUtil.toFlow(constraints);

    // Extract and source IP from header constraints,
    setSrcIp(constraints, srcLocation, builder);
    setDstIp(constraints, builder);

    // Set defaults for protocol, and ports and packet lengths:
    if (builder.getIpProtocol() == null || builder.getIpProtocol() == IpProtocol.IP) {
      builder.setIpProtocol(IpProtocol.UDP);
    }
    if (builder.getDstPort() == 0) {
      builder.setDstPort(TRACEROUTE_PORT);
    }
    if (builder.getSrcPort() == 0) {
      builder.setSrcPort(NamedPort.EPHEMERAL_LOWEST.number());
    }
    return builder;
  }

  private void setSrcIp(
      PacketHeaderConstraints constraints, Location srcLocation, Builder builder) {
    String headerSrcIp = constraints.getSrcIps();
    if (headerSrcIp != null) {
      // interpret given Src IP "flexibly"
      IpSpaceSpecifier srcIpSpecifier =
          IpSpaceSpecifierFactory.load(IP_SPECIFIER_FACTORY).buildIpSpaceSpecifier(headerSrcIp);
      // Resolve to set of locations/IPs
      IpSpaceAssignment srcIps =
          srcIpSpecifier.resolve(ImmutableSet.of(), _batfish.specifierContext());
      // Filter out empty IP assignments
      ImmutableList<Entry> nonEmptyIpSpaces =
          srcIps
              .getEntries()
              .stream()
              .filter(e -> !e.getIpSpace().equals(EmptyIpSpace.INSTANCE))
              .collect(ImmutableList.toImmutableList());
      checkArgument(
          nonEmptyIpSpaces.size() > 0, "At least one source IP is required, could not resolve any");
      checkArgument(
          nonEmptyIpSpaces.size() == 1,
          "Specified source IP %s resolves to more than one location/IP: %s",
          headerSrcIp,
          nonEmptyIpSpaces);
      IpSpace space = srcIps.getEntries().iterator().next().getIpSpace();
      Optional<Ip> srcIp = _ipSpaceRepresentative.getRepresentative(space);
      // Extra check to ensure that we actually got an IP
      checkArgument(srcIp.isPresent(), "At least one source IP is required, could not resolve any");
      builder.setSrcIp(srcIp.get());
    } else {
      // Use from source location to determine header Src IP
      Optional<Entry> entry =
          _sourceIpAssignment
              .getEntries()
              .stream()
              .filter(e -> e.getLocations().contains(srcLocation))
              .findFirst();

      final String locationSpecifierInput = ((TracerouteQuestion) _question).getSourceLocationStr();
      checkArgument(
          entry.isPresent(),
          "Cannot resolve a source IP address from location %s",
          locationSpecifierInput);
      Optional<Ip> srcIp = _ipSpaceRepresentative.getRepresentative(entry.get().getIpSpace());
      checkArgument(
          srcIp.isPresent(),
          "At least one source IP is required, location %s produced none",
          srcLocation);
      builder.setSrcIp(srcIp.get());
    }
  }

  private void setDstIp(PacketHeaderConstraints constraints, Builder builder) {
    String headerDstIp = constraints.getDstIps();
    checkArgument(
        constraints.getDstIps() != null, "Cannot perform traceroute without a destination");
    IpSpaceSpecifier dstIpSpecifier =
        IpSpaceSpecifierFactory.load(IP_SPECIFIER_FACTORY).buildIpSpaceSpecifier(headerDstIp);
    IpSpaceAssignment dstIps =
        dstIpSpecifier.resolve(ImmutableSet.of(), _batfish.specifierContext());
    checkArgument(
        dstIps.getEntries().size() == 1,
        "Specified destination: %s, resolves to more than one IP",
        headerDstIp);
    IpSpace space = dstIps.getEntries().iterator().next().getIpSpace();
    Optional<Ip> dstIp = _ipSpaceRepresentative.getRepresentative(space);
    checkArgument(dstIp.isPresent(), "At least one destination IP is required");
    builder.setDstIp(dstIp.get());
  }

  private void setSourceLocation(Flow.Builder flowBuilder, Location loc) {
    loc.accept(
        new LocationVisitor<Void>() {
          @Override
          public Void visitInterfaceLinkLocation(
              @Nonnull InterfaceLinkLocation interfaceLinkLocation) {
            flowBuilder
                .setIngressInterface(interfaceLinkLocation.getInterfaceName())
                .setIngressNode(interfaceLinkLocation.getNodeName())
                .setIngressVrf(null);
            return null;
          }

          @Override
          public Void visitInterfaceLocation(@Nonnull InterfaceLocation interfaceLocation) {
            flowBuilder
                .setIngressInterface(null)
                .setIngressNode(interfaceLocation.getNodeName())
                .setIngressVrf(
                    interfaceVrf(
                        interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName()));
            return null;
          }
        });
  }

  private String interfaceVrf(String node, String iface) {
    return _configurations.get(node).getAllInterfaces().get(iface).getVrf().getName();
  }
}
