package org.batfish.question.testfilters;

import static org.batfish.datamodel.SetFlowStartLocation.setStartLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

public class TestFiltersAnswerer extends Answerer {
  public static final String COL_NODE = "Node";
  public static final String COL_FILTER_NAME = "Filter_Name";
  public static final String COL_FLOW = "Flow";
  public static final String COL_ACTION = "Action";
  public static final String COL_LINE_CONTENT = "Line_Content";
  public static final String COL_TRACE = "Trace";

  public static final List<ColumnMetadata> COLUMN_METADATA =
      ImmutableList.of(
          new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false),
          new ColumnMetadata(COL_FILTER_NAME, Schema.STRING, "Filter name", true, false),
          new ColumnMetadata(COL_FLOW, Schema.FLOW, "Evaluated flow", true, false),
          new ColumnMetadata(COL_ACTION, Schema.STRING, "Outcome", false, true),
          new ColumnMetadata(COL_LINE_CONTENT, Schema.STRING, "Line content", false, true),
          new ColumnMetadata(COL_TRACE, Schema.list(Schema.TRACE_TREE), "ACL trace", false, true));

  public TestFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableAnswerElement} object containing the right metadata
   *
   * @param question The question object for which the answer is being created, to borrow its {@link
   *     DisplayHints}
   */
  public static TableAnswerElement create(TestFiltersQuestion question) {
    String textDesc =
        String.format(
            "Filter ${%s} on node ${%s} will ${%s} flow ${%s} at line ${%s}",
            COL_FILTER_NAME, COL_NODE, COL_ACTION, COL_FLOW, COL_LINE_CONTENT);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    TableMetadata metadata = new TableMetadata(COLUMN_METADATA, textDesc);
    return new TableAnswerElement(metadata);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    Multiset<Row> rows = getRows(snapshot);

    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    TableAnswerElement answer = create(question);
    answer.postProcessAnswer(question, rows);
    return answer;
  }

  private SortedSet<Flow> getFlows(
      Set<Location> queryLocations,
      SpecifierContext context,
      Configuration c,
      ImmutableSet.Builder<String> allProblems) {
    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    String node = c.getHostname();
    Set<Location> srcLocations =
        queryLocations.stream()
            .filter(loc -> loc.getNodeName().equals(node))
            .collect(Collectors.toSet());
    if (srcLocations.isEmpty() && question.getStartLocation() != null) {
      // The user requested a specific location, not on this node. No work to do.
      return Collections.emptySortedSet();
    }

    PacketHeaderConstraints constraints = question.getHeaders();

    // if src ip is specified, srcIpAssignments would have only one entry (srcLocations,
    // resolvedIpSpace)
    // if src ip is not specified and location is specified, srcIpAssignments would have a set of
    // entries of (srcLocation, IpSpacePerLocation)
    IpSpaceAssignment srcIpAssignments =
        SpecifierFactories.getIpSpaceSpecifierOrDefault(
                constraints.getSrcIps(), InferFromLocationIpSpaceSpecifier.INSTANCE)
            .resolve(srcLocations, context);

    IpSpace dstIps =
        SpecifierFactories.getIpSpaceSpecifierOrDefault(
                constraints.getDstIps(), new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .resolve(ImmutableSet.of(), context)
            .getEntries()
            .stream()
            .findFirst()
            .map(Entry::getIpSpace)
            .orElse(UniverseIpSpace.INSTANCE);

    BDDPacket pkt = new BDDPacket();
    BDD hsBDD =
        PacketHeaderConstraintsUtil.toBDD(
            pkt,
            constraints,
            srcIpAssignments.getEntries().stream()
                .findFirst()
                .map(Entry::getIpSpace)
                .orElse(UniverseIpSpace.INSTANCE),
            dstIps);

    // this will happen if the node has no interfaces, and someone is just testing their ACLs
    if (srcLocations.isEmpty() && question.getStartLocation() == null) {
      try {
        Builder flowBuilder = pkt.getFlow(hsBDD, FlowPreference.TESTFILTER).get();

        flowBuilder.setIngressNode(node);
        flowBuilder.setIngressInterface(null);
        flowBuilder.setIngressVrf(
            Configuration.DEFAULT_VRF_NAME); // dummy because Flow needs non-null interface or vrf
        return ImmutableSortedSet.of(flowBuilder.build());
      } catch (NoSuchElementException e) {
        allProblems.add("cannot get a flow from the specifier");
      } catch (IllegalArgumentException e) {
        allProblems.add(e.getMessage());
      }
      // Only reachable in exceptional case.
      return ImmutableSortedSet.of();
    }

    ImmutableSortedSet.Builder<Flow> flows = ImmutableSortedSet.naturalOrder();
    // Perform cross-product of all locations to flows
    for (Entry entry : srcIpAssignments.getEntries()) {
      Set<Location> locations = entry.getLocations();
      IpSpace srcIps = entry.getIpSpace();
      Flow.Builder flowBuilder;
      try {
        flowBuilder =
            pkt.getFlow(
                    PacketHeaderConstraintsUtil.toBDD(pkt, constraints, srcIps, dstIps),
                    FlowPreference.TESTFILTER)
                .get();
      } catch (NoSuchElementException e) {
        allProblems.add("cannot get a flow from the specifier");
        continue;
      }

      for (Location location : locations) {
        try {
          setStartLocation(ImmutableMap.of(node, c), flowBuilder, location);
          flows.add(flowBuilder.build());
        } catch (IllegalArgumentException e) {
          // record this error but try to keep going
          allProblems.add(e.getMessage());
        }
      }
    }

    return flows.build();
  }

  /**
   * Returns a {@link Row} with results from injecting {@code flow} into {@code filter} at node
   * represented by {@code c}.
   */
  public static Row getRow(IpAccessList filter, Flow flow, Configuration c) {
    @Nullable
    List<TraceTree> trace =
        AclTracer.trace(
            filter,
            flow,
            flow.getIngressInterface(),
            c.getIpAccessLists(),
            c.getIpSpaces(),
            c.getIpSpaceMetadata());
    FilterResult result =
        filter.filter(flow, flow.getIngressInterface(), c.getIpAccessLists(), c.getIpSpaces());
    Integer matchLine = result.getMatchLine();
    String lineDesc = "no-match";
    if (matchLine != null) {
      lineDesc = filter.getLines().get(matchLine).getName();
      if (lineDesc == null) {
        lineDesc = "line:" + matchLine;
      }
    }
    return Row.builder()
        .put(COL_NODE, new Node(c.getHostname()))
        .put(COL_FILTER_NAME, filter.getName())
        .put(COL_FLOW, flow)
        .put(COL_ACTION, result.getAction())
        .put(COL_LINE_CONTENT, lineDesc)
        .put(COL_TRACE, trace)
        .build();
  }

  @VisibleForTesting
  Multiset<Row> getRows(NetworkSnapshot snapshot) {
    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    SpecifierContext context = _batfish.specifierContext(snapshot);
    Map<String, Configuration> configurations = context.getConfigs();
    SortedSet<String> includeNodes =
        ImmutableSortedSet.copyOf(question.getNodeSpecifier().resolve(context));
    FilterSpecifier filterSpecifier = question.getFilterSpecifier();

    Multiset<Row> rows = HashMultiset.create();

    // collect all errors while building flows; return this set when no valid flow is found
    ImmutableSet.Builder<String> allProblems = ImmutableSet.builder();

    boolean foundMatchingFilter = false;
    boolean foundMatchingFlow = false;

    Set<Location> queryLocations = question.getStartLocationSpecifier().resolve(context);

    for (String node : includeNodes) {
      SortedSet<IpAccessList> filtersByName =
          ImmutableSortedSet.copyOf(
              Comparator.comparing(IpAccessList::getName), filterSpecifier.resolve(node, context));
      if (filtersByName.isEmpty()) {
        continue;
      }
      foundMatchingFilter = true;

      Configuration c = configurations.get(node);
      SortedSet<Flow> flows = getFlows(queryLocations, context, c, allProblems);
      if (flows.isEmpty()) {
        continue;
      }
      foundMatchingFlow = true;

      // there should be another for loop for v6 filters when we add v6 support
      for (IpAccessList filter : filtersByName) {
        for (Flow flow : flows) {
          rows.add(getRow(filter, flow, c));
        }
      }
    }
    if (foundMatchingFilter && !foundMatchingFlow) {
      throw new BatfishException(
          String.format(
              "No valid flow found for specified parameters. Potential problems: %s",
              String.join(",", allProblems.build())));
    }
    return rows;
  }
}
