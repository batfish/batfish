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
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
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
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
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
  private static final int TRACEROUTE_PORT = 33434;
  @VisibleForTesting static final int PACKET_LENGTH = 512;

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
    String tag = _batfish.getFlowTag();
    Set<Flow> flows = getFlows(tag);
    _batfish.processFlows(flows, ((TracerouteQuestion) _question).getIgnoreAcls());
    FlowHistory flowHistory = _batfish.getHistory();
    Multiset<Row> rows = flowHistoryToRows(flowHistory, false);
    TableAnswerElement table = new TableAnswerElement(createMetadata(false));
    table.postProcessAnswer(_question, rows);
    return table;
  }

  @Override
  public AnswerElement answerDiff() {
    Set<Flow> flows = getFlows(_batfish.getDifferentialFlowTag());

    _batfish.pushBaseEnvironment();
    _batfish.processFlows(flows, ((TracerouteQuestion) _question).getIgnoreAcls());
    _batfish.popEnvironment();

    _batfish.pushDeltaEnvironment();
    _batfish.processFlows(flows, ((TracerouteQuestion) _question).getIgnoreAcls());
    _batfish.popEnvironment();

    FlowHistory flowHistory = _batfish.getHistory();
    Multiset<Row> rows = flowHistoryToRows(flowHistory, true);
    TableAnswerElement table = new TableAnswerElement(createMetadata(true));
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
  static Row diffFlowHistoryToRow(FlowHistoryInfo historyInfo) {
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
  @VisibleForTesting
  Flow.Builder headerConstraintsToFlow(PacketHeaderConstraints constraints, Location srcLocation)
      throws IllegalArgumentException {
    Flow.Builder builder = Flow.builder();

    // Extract and source IP from header constraints,
    setSrcIp(constraints, srcLocation, builder);

    setDstIp(constraints, builder);

    // Deal with IP packet header values.
    setIpProtocol(constraints, builder);

    // Src Ports (default to lowest ephemeral, for UDP traceroute)
    setSrcPort(constraints, builder);

    // Dst Ports (default to 33434, for UDP traceroute)
    setDstPort(constraints, builder);

    // Icmp values
    setIcmpValue(constraints, builder);

    // DSCP value
    setDscpValue(constraints, builder);

    // TODO: ECN value, fragments, etc
    setPacketLength(constraints, builder);

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

  @VisibleForTesting
  static void setDscpValue(PacketHeaderConstraints constraints, Builder builder) {
    Set<SubRange> dscps = constraints.getDscps();
    if (dscps != null) {
      SubRange dscp = dscps.iterator().next();
      if (dscps.size() > 1 || !dscp.isSingleValue()) {
        throw new IllegalArgumentException("Cannot perform traceroute with multiple DSCP values");
      }
      builder.setDscp(dscp.getStart());
    } else {
      builder.setDscp(0);
    }
  }

  @VisibleForTesting
  static void setIcmpValue(PacketHeaderConstraints constraints, Builder builder) {
    Set<SubRange> icmpTypes = constraints.getIcmpTypes();
    if (icmpTypes != null) {
      SubRange icmpType = icmpTypes.iterator().next();
      if (icmpTypes.size() > 1 || !icmpType.isSingleValue()) {
        throw new IllegalArgumentException("Cannot perform traceroute with multiple ICMP types");
      }
      builder.setIcmpType(icmpType.getStart());
    }
    Set<SubRange> icmpCodes = constraints.getIcmpCodes();
    if (icmpCodes != null) {
      SubRange icmpCode = icmpCodes.iterator().next();
      if (icmpCodes.size() > 1 || !icmpCode.isSingleValue()) {
        throw new IllegalArgumentException("Cannot perform traceroute with multiple ICMP codes");
      }
      builder.setIcmpType(icmpCode.getStart());
    }
  }

  @VisibleForTesting
  static void setDstPort(PacketHeaderConstraints constraints, Builder builder) {
    Set<SubRange> dstPorts = constraints.resolveDstPorts();
    checkArgument(
        dstPorts == null || dstPorts.size() == 1,
        "Cannot perform traceroute with multiple destination ports");
    if (dstPorts != null) {
      SubRange dstPort = dstPorts.iterator().next();
      checkArgument(
          dstPort.isSingleValue(), "Cannot perform traceroute with multiple destination ports");
      builder.setDstPort(dstPort.getStart());
    } else {
      builder.setDstPort(TRACEROUTE_PORT);
    }
  }

  @VisibleForTesting
  static void setSrcPort(PacketHeaderConstraints constraints, Builder builder) {
    Set<SubRange> srcPorts = constraints.getSrcPorts();
    checkArgument(
        srcPorts == null || srcPorts.size() == 1,
        "Cannot perform traceroute with multiple source ports");
    if (srcPorts != null) {
      SubRange srcPort = srcPorts.iterator().next();
      checkArgument(
          srcPort.isSingleValue(), "Cannot perform traceroute with multiple source ports");
      builder.setSrcPort(srcPort.getStart());
    } else {
      builder.setSrcPort(NamedPort.EPHEMERAL_LOWEST.number());
    }
  }

  private static void setIpProtocol(PacketHeaderConstraints constraints, Builder builder) {
    // IP protocol (default to UDP)
    Set<IpProtocol> ipProtocols = constraints.resolveIpProtocols();
    checkArgument(
        ipProtocols == null || ipProtocols.size() == 1,
        "Cannot perform traceroute with multiple IP protocols");
    if (ipProtocols != null) {
      builder.setIpProtocol(ipProtocols.iterator().next());
    } else {
      builder.setIpProtocol(IpProtocol.UDP);
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

  @VisibleForTesting
  static void setPacketLength(PacketHeaderConstraints constraints, Builder builder) {
    Set<SubRange> packetLengths = constraints.getPacketLengths();
    checkArgument(
        packetLengths == null || packetLengths.size() == 1,
        "Cannot perform traceroute with multiple packet lengths");
    if (packetLengths != null) {
      SubRange packetLength = packetLengths.iterator().next();
      checkArgument(
          packetLength.isSingleValue(), "Cannot perform traceroute with multiple packet lengths");
      builder.setPacketLength(packetLength.getStart());
    } else {
      builder.setPacketLength(PACKET_LENGTH);
    }
  }

  private String interfaceVrf(String node, String iface) {
    return _configurations.get(node).getAllInterfaces().get(iface).getVrf().getName();
  }
}
