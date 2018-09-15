package org.batfish.question.testfilters;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.question.traceroute.TracerouteQuestion;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FlexibleInferFromLocationIpSpaceSpecifierFactory;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationVisitor;
import org.batfish.specifier.SpecifierContext;

public class TestFiltersAnswerer extends Answerer {

  private static final String IP_SPECIFIER_FACTORY =
      FlexibleInferFromLocationIpSpaceSpecifierFactory.NAME;

  static int DEFAULT_DST_PORT = 80; // HTTP
  static IpProtocol DEFAULT_IP_PROTOCOL = IpProtocol.TCP;
  static int DEFAULT_SRC_PORT = 49152; // lowest ephemeral port

  public static final String COL_NODE = "Node";
  public static final String COL_FILTER_NAME = "Filter_Name";
  public static final String COL_FLOW = "Flow";
  public static final String COL_ACTION = "Action";
  public static final String COL_LINE_NUMBER = "Line_Number";
  public static final String COL_LINE_CONTENT = "Line_Content";
  public static final String COL_TRACE = "Trace";

  private final Map<String, Configuration> _configurations;
  private final IpSpaceRepresentative _ipSpaceRepresentative;
  private final IpSpaceAssignment _sourceIpAssignment;

  public TestFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
    _configurations = batfish.loadConfigurations();
    _ipSpaceRepresentative = IpSpaceRepresentative.load();
    _sourceIpAssignment = initSourceIpAssignment();
  }

  @VisibleForTesting
  IpSpaceAssignment initSourceIpAssignment() {
    /* construct specifiers */
    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    LocationSpecifier sourceLocationSpecifier = (question).getStartLocationSpecifier();

    IpSpaceSpecifier sourceIpSpaceSpecifier =
        IpSpaceSpecifierFactory.load(IP_SPECIFIER_FACTORY)
            .buildIpSpaceSpecifier(question.getHeaders().getSrcIps());

    /* resolve specifiers */
    SpecifierContext ctxt = _batfish.specifierContext();
    Set<Location> sourceLocations = sourceLocationSpecifier.resolve(ctxt);
    return sourceIpSpaceSpecifier.resolve(sourceLocations, ctxt);
  }

  /**
   * Creates a {@link TableAnswerElement} object containing the right metadata
   *
   * @param question The question object for which the answer is being created, to borrow its {@link
   *     DisplayHints}
   * @return The creates the answer element object.
   */
  public static TableAnswerElement create(TestFiltersQuestion question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false),
            new ColumnMetadata(COL_FILTER_NAME, Schema.STRING, "Filter name", true, false),
            new ColumnMetadata(COL_FLOW, Schema.FLOW, "Evaluated flow", true, false),
            new ColumnMetadata(COL_ACTION, Schema.STRING, "Outcome", false, true),
            new ColumnMetadata(COL_LINE_NUMBER, Schema.INTEGER, "Line number", false, true),
            new ColumnMetadata(COL_LINE_CONTENT, Schema.STRING, "Line content", false, true),
            new ColumnMetadata(COL_TRACE, Schema.ACL_TRACE, "ACL trace", false, true));
    String textDesc =
        String.format(
            "Filter ${%s} on node ${%s} will ${%s} flow ${%s} at line ${%s} ${%s}",
            COL_FILTER_NAME, COL_NODE, COL_ACTION, COL_FLOW, COL_LINE_NUMBER, COL_LINE_CONTENT);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    TableMetadata metadata = new TableMetadata(columnMetadata, textDesc);
    return new TableAnswerElement(metadata);
  }

  @Override
  public TableAnswerElement answer() {
    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes = question.getNodes().getMatchingNodes(_batfish);
    SpecifierContext specifierContext = _batfish.specifierContext();

    Multiset<Row> rows = rawAnswer(configurations, question, includeNodes, specifierContext);

    TableAnswerElement answer = create(question);
    answer.postProcessAnswer(question, rows);
    return answer;
  }

  private Set<Flow> getFlows(String node) {
    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    Set<Location> srcLocations =
        question.getStartLocationSpecifier().resolve(_batfish.specifierContext());

    ImmutableSet.Builder<Flow> setBuilder = ImmutableSet.builder();

    // Perform cross-product of all locations to flows
    for (Location srcLocation : srcLocations) {
      Flow.Builder flowBuilder = headerConstraintsToFlow(question.getHeaders(), srcLocation);
      setSourceLocation(flowBuilder, srcLocation);
      // should this filtering happen earlier for performance?
      if (!flowBuilder.getIngressNode().equals(node)) {
        continue;
      }
      flowBuilder.setTag("FlowTag"); // dummy tag; consistent tags enable flow diffs
      setBuilder.add(flowBuilder.build());
    }

    return setBuilder.build();
  }

  private void setSourceLocation(Builder flowBuilder, Location loc) {
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
    String headerSrcIp = constraints.getSrcIps();
    if (headerSrcIp != null) {
      // interpret given Src IP using sane mode
      IpSpaceSpecifier srcIpSpecifier =
          IpSpaceSpecifierFactory.load(IP_SPECIFIER_FACTORY).buildIpSpaceSpecifier(headerSrcIp);
      IpSpaceAssignment srcIps =
          srcIpSpecifier.resolve(ImmutableSet.of(), _batfish.specifierContext());
      checkArgument(
          srcIps.getEntries().size() == 1,
          "Specified source: %s, resolves to more than one IP",
          headerSrcIp);
      IpSpace space = srcIps.getEntries().iterator().next().getIpSpace();
      Optional<Ip> srcIp = _ipSpaceRepresentative.getRepresentative(space);
      checkArgument(srcIp.isPresent(), "At least one source IP is required");
      builder.setSrcIp(srcIp.get());
    } else {
      // Use from source location to determine header Src IP
      Optional<Entry> entry =
          _sourceIpAssignment
              .getEntries()
              .stream()
              .filter(e -> e.getLocations().contains(srcLocation))
              .findFirst();

      final String locationSpecifierInput =
          ((TracerouteQuestion) _question).getSourceLocationSpecifierInput();
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

    String headerDstIp = constraints.getDstIps();
    checkArgument(constraints.getDstIps() != null, "Cannot run testfilters without destination IP");
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

    // Deal with IP packet header values.
    // IP protocol (default to UDP)
    Set<IpProtocol> ipProtocols = constraints.resolveIpProtocols();
    checkArgument(
        ipProtocols == null || ipProtocols.size() == 1,
        "Cannot run testfilters with multiple IP protocols");
    if (ipProtocols != null) {
      builder.setIpProtocol(ipProtocols.iterator().next());
    } else {
      builder.setIpProtocol(DEFAULT_IP_PROTOCOL);
    }

    // Src Ports
    Set<SubRange> srcPorts = constraints.getSrcPorts();
    checkArgument(
        srcPorts == null || srcPorts.size() == 1,
        "Cannot run testfilters with multiple source ports");
    if (srcPorts != null) {
      SubRange srcPort = srcPorts.iterator().next();
      builder.setSrcPort(srcPort.getStart());
    } else {
      builder.setSrcPort(DEFAULT_SRC_PORT);
    }

    // Dst Ports
    Set<SubRange> dstPorts = constraints.resolveDstPorts();
    checkArgument(
        dstPorts == null || dstPorts.size() == 1,
        "Cannot perform traceroute with multiple destination ports");
    if (dstPorts != null) {
      SubRange dstPort = dstPorts.iterator().next();
      if (dstPorts.size() > 1 || !dstPort.isSingleValue()) {
        throw new IllegalArgumentException();
      }
      builder.setDstPort(dstPort.getStart());
    } else {
      builder.setDstPort(DEFAULT_DST_PORT);
    }

    // Icmp values
    Set<SubRange> icmpTypes = constraints.getIcmpTypes();
    if (icmpTypes != null) {
      SubRange icmpType = icmpTypes.iterator().next();
      if (icmpTypes.size() > 1 || !icmpType.isSingleValue()) {
        throw new IllegalArgumentException("Cannot run testfilters with multiple ICMP types");
      }
      builder.setIcmpType(icmpType.getStart());
    }
    Set<SubRange> icmpCodes = constraints.getIcmpCodes();
    if (icmpCodes != null) {
      SubRange icmpCode = icmpCodes.iterator().next();
      if (icmpCodes.size() > 1 || !icmpCode.isSingleValue()) {
        throw new IllegalArgumentException("Cannot run testfilters with multiple ICMP codes");
      }
      builder.setIcmpType(icmpCode.getStart());
    }

    // DSCP value
    Set<SubRange> dscps = constraints.getDscps();
    if (dscps != null) {
      SubRange dscp = dscps.iterator().next();
      if (dscps.size() > 1 || !dscp.isSingleValue()) {
        throw new IllegalArgumentException("Cannot run testfilters with multiple DSCP values");
      }
      builder.setDscp(dscp.getStart());
    } else {
      builder.setDscp(0);
    }

    // TODO: ECN value, fragments, etc
    return builder;
  }

  private static Row getRow(
      String nodeName,
      String filterName,
      Flow flow,
      LineAction action,
      Integer matchLine,
      String lineContent,
      AclTrace trace) {
    RowBuilder row = Row.builder();
    row.put(TestFiltersAnswerer.COL_NODE, new Node(nodeName))
        .put(TestFiltersAnswerer.COL_FILTER_NAME, filterName)
        .put(TestFiltersAnswerer.COL_FLOW, flow)
        .put(TestFiltersAnswerer.COL_ACTION, action)
        .put(TestFiltersAnswerer.COL_LINE_NUMBER, matchLine)
        .put(TestFiltersAnswerer.COL_LINE_CONTENT, lineContent)
        .put(TestFiltersAnswerer.COL_TRACE, trace);
    return row.build();
  }

  Multiset<Row> rawAnswer(
      Map<String, Configuration> configurations,
      TestFiltersQuestion question,
      Set<String> includeNodes,
      SpecifierContext specifierContext) {

    FilterSpecifier filterSpecifier = question.getFilterSpecifier();

    Multiset<Row> rows = HashMultiset.create();

    for (Configuration c : configurations.values()) {
      if (!includeNodes.contains(c.getHostname())) {
        continue;
      }
      // there should be another for loop for v6 filters when we add v6 support
      for (IpAccessList filter : filterSpecifier.resolve(c.getHostname(), specifierContext)) {

        Set<Flow> flows = getFlows(c.getHostname());

        for (Flow flow : flows) {
          AclTrace trace =
              AclTracer.trace(
                  filter,
                  flow,
                  flow.getIngressInterface(),
                  c.getIpAccessLists(),
                  c.getIpSpaces(),
                  c.getIpSpaceMetadata());
          FilterResult result =
              filter.filter(
                  flow, flow.getIngressInterface(), c.getIpAccessLists(), c.getIpSpaces());
          Integer matchLine = result.getMatchLine();
          String lineDesc = "no-match";
          if (matchLine != null) {
            lineDesc = filter.getLines().get(matchLine).getName();
            if (lineDesc == null) {
              lineDesc = "line:" + matchLine;
            }
          }
          rows.add(
              getRow(
                  c.getHostname(),
                  filter.getName(),
                  flow,
                  result.getAction(),
                  matchLine,
                  lineDesc,
                  trace));
        }
      }
    }
    return rows;
  }
}
