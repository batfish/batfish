package org.batfish.question.reachfilter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.tracefilters.TraceFiltersAnswerer;
import org.batfish.question.tracefilters.TraceFiltersQuestion;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Answerer for ReachFilterQuestion */
public final class ReachFilterAnswerer extends Answerer {
  static final String COLUMN_SNAPSHOT = "snapshot";
  static final String COLUMN_RESULT_TYPE = "resultType";
  static final String COLUMN_NODE = "node";
  static final String COLUMN_FILTER_NAME = "filterName";
  static final String COLUMN_FLOW = "flow";
  static final String COLUMN_ACTION = "action";
  static final String COLUMN_LINE_NUMBER = "lineNumber";
  static final String COLUMN_LINE_CONTENT = "lineContent";
  static final String COLUMN_TRACE = "trace";

  static final String BASE = "base";
  static final String DELTA = "delta";
  static final String INCREASED = "increased";
  static final String DECREASED = "decreased";

  private TableAnswerElement _tableAnswerElement;

  public ReachFilterAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    ReachFilterQuestion question = (ReachFilterQuestion) _question;
    if (question.getDifferential()) {
      differentialAnswer(question);
    } else {
      nonDifferentialAnswer(question);
    }
    return _tableAnswerElement;
  }

  @Override
  public AnswerElement answerDiff() {
    differentialAnswer((ReachFilterQuestion) _question);
    return _tableAnswerElement;
  }

  private void differentialAnswer(ReachFilterQuestion question) {
    _tableAnswerElement = new TableAnswerElement(differentialTableMetaData());

    _batfish.pushBaseEnvironment();
    Map<String, Configuration> baseConfigs = _batfish.loadConfigurations();
    Multimap<String, String> baseAcls = getSpecifiedAcls(question);
    _batfish.popEnvironment();

    _batfish.pushDeltaEnvironment();
    Map<String, Configuration> deltaConfigs = _batfish.loadConfigurations();
    Multimap<String, String> deltaAcls = getSpecifiedAcls(question);
    _batfish.popEnvironment();

    Set<String> commonNodes = Sets.intersection(baseAcls.keySet(), deltaAcls.keySet());
    for (String node : commonNodes) {
      Configuration baseConfig = baseConfigs.get(node);
      Configuration deltaConfig = deltaConfigs.get(node);
      Set<String> commonAcls =
          Sets.intersection(
              ImmutableSet.copyOf(baseAcls.get(node)), ImmutableSet.copyOf(deltaAcls.get(node)));
      for (String aclName : commonAcls) {
        Optional<IpAccessList> baseAcl = makeQueryAcl(baseConfig.getIpAccessLists().get(aclName));
        Optional<IpAccessList> deltaAcl = makeQueryAcl(deltaConfig.getIpAccessLists().get(aclName));
        if (!baseAcl.isPresent() && !deltaAcl.isPresent()) {
          continue;
        }
        if (baseAcl.isPresent() ^ deltaAcl.isPresent()) {
          _logger.warn("Could only make query ACL for one of the snapshots.");
          continue;
        }

        DifferentialReachFilterResult result =
            _batfish.differentialReachFilter(
                baseConfig,
                baseAcl.get(),
                deltaConfig,
                deltaAcl.get(),
                question.toReachFilterParameters());

        result
            .getDecreasedFlow()
            .ifPresent(
                flow ->
                    processDifferentialFlow(DECREASED, node, baseAcl.get(), deltaAcl.get(), flow));

        result
            .getIncreasedFlow()
            .ifPresent(
                flow ->
                    processDifferentialFlow(INCREASED, node, baseAcl.get(), deltaAcl.get(), flow));
      }
    }
  }

  private void processDifferentialFlow(
      String resultType, String hostname, IpAccessList baseAcl, IpAccessList deltaAcl, Flow flow) {
    appendRows(
        toDifferentialTableRows(resultType, BASE, traceFilterRows(true, hostname, baseAcl, flow)));
    appendRows(
        toDifferentialTableRows(
            resultType, DELTA, traceFilterRows(false, hostname, deltaAcl, flow)));
  }

  private void nonDifferentialAnswer(ReachFilterQuestion question) {
    _tableAnswerElement = TraceFiltersAnswerer.create(new TraceFiltersQuestion(null, null));

    List<Pair<String, IpAccessList>> acls = getQueryAcls(question);
    if (acls.isEmpty()) {
      throw new BatfishException("No matching filters");
    }

    /*
     * For each query ACL, try to get a flow. If one exists, run traceFilter on that flow.
     * Concatenate the answers for all flows into one big table.
     */
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    for (Pair<String, IpAccessList> pair : acls) {
      String hostname = pair.getFirst();
      Configuration node = configurations.get(hostname);
      IpAccessList acl = pair.getSecond();
      Optional<Flow> result;
      try {
        result = _batfish.reachFilter(node, acl, question.toReachFilterParameters());
      } catch (Throwable t) {
        _batfish.getLogger().warn(t.getMessage());
        continue;
      }
      result.ifPresent(flow -> appendRows(traceFilterRows(hostname, acl, flow)));
    }
  }

  private static Rows toDifferentialTableRows(String resultType, String snapshot, Rows traceRows) {
    ImmutableMultiset.Builder<Row> rows = ImmutableMultiset.builder();
    RowBuilder rb =
        Row.builder().put(COLUMN_RESULT_TYPE, resultType).put(COLUMN_SNAPSHOT, snapshot);
    traceRows.iterator().forEachRemaining(row -> rows.add(rb.putAll(row).build()));
    return new Rows(rows.build());
  }

  private TableMetadata differentialTableMetaData() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COLUMN_RESULT_TYPE, Schema.STRING, "Result type", true, false),
            new ColumnMetadata(COLUMN_SNAPSHOT, Schema.STRING, "Snapshot", true, false),
            new ColumnMetadata(COLUMN_NODE, Schema.NODE, "Node", true, false),
            new ColumnMetadata(COLUMN_FILTER_NAME, Schema.STRING, "Filter name", true, false),
            new ColumnMetadata(COLUMN_FLOW, Schema.FLOW, "Evaluated flow", true, false),
            new ColumnMetadata(COLUMN_ACTION, Schema.STRING, "Outcome", false, true),
            new ColumnMetadata(COLUMN_LINE_NUMBER, Schema.INTEGER, "Line number", false, true),
            new ColumnMetadata(COLUMN_LINE_CONTENT, Schema.STRING, "Line content", false, true),
            new ColumnMetadata(COLUMN_TRACE, Schema.ACL_TRACE, "ACL trace", false, true));
    String textDesc =
        String.format(
            "Filter ${%s} on node ${%s} will ${%s} flow ${%s} at line ${%s} ${%s}",
            COLUMN_FILTER_NAME,
            COLUMN_NODE,
            COLUMN_ACTION,
            COLUMN_FLOW,
            COLUMN_LINE_NUMBER,
            COLUMN_LINE_CONTENT);
    return new TableMetadata(columnMetadata, textDesc);
  }

  private void appendRows(Rows rows) {
    rows.iterator().forEachRemaining(_tableAnswerElement::addRow);
  }

  private Multimap<String, String> getSpecifiedAcls(ReachFilterQuestion question) {
    SortedMap<String, Configuration> configs = _batfish.loadConfigurations();
    FilterSpecifier filterSpecifier = question.getFilterSpecifier();
    SpecifierContext specifierContext = _batfish.specifierContext();
    ImmutableMultimap.Builder<String, String> acls = ImmutableMultimap.builder();
    question
        .getNodesSpecifier()
        .resolve(_batfish.specifierContext())
        .stream()
        .map(configs::get)
        .forEach(
            config ->
                filterSpecifier
                    .resolve(config.getHostname(), specifierContext)
                    .forEach(acl -> acls.put(config.getHostname(), acl.getName())));
    return acls.build();
  }

  private Optional<IpAccessList> makeQueryAcl(IpAccessList originalAcl) {
    ReachFilterQuestion question = (ReachFilterQuestion) _question;
    switch (question.getType()) {
      case PERMIT:
        return Optional.of(originalAcl);
      case DENY:
        return Optional.of(toDenyAcl(originalAcl));
      case MATCH_LINE:
        // for each ACL, construct a new ACL that accepts if and only if the specified line matches
        Integer lineNumber = question.getLineNumber();
        Preconditions.checkState(
            lineNumber != null, "Cannot perform a match line query without a line number");
        return originalAcl.getLines().size() > lineNumber
            ? Optional.of(toMatchLineAcl(lineNumber, originalAcl))
            : Optional.empty();
      default:
        throw new BatfishException("Unexpected query Type: " + question.getType());
    }
  }

  @VisibleForTesting
  List<Pair<String, IpAccessList>> getQueryAcls(ReachFilterQuestion question) {
    Map<String, Configuration> configs = _batfish.loadConfigurations();
    return getSpecifiedAcls(question)
        .entries()
        .stream()
        .map(
            entry -> {
              String hostName = entry.getKey();
              String aclName = entry.getValue();
              Optional<IpAccessList> queryAcl =
                  makeQueryAcl(configs.get(hostName).getIpAccessLists().get(aclName));
              return queryAcl.map(acl -> new Pair<>(hostName, acl));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  static IpAccessList toMatchLineAcl(Integer lineNumber, IpAccessList acl) {
    List<IpAccessListLine> lines =
        Streams.concat(
                acl.getLines()
                    .subList(0, lineNumber)
                    .stream()
                    .map(l -> l.toBuilder().setAction(LineAction.DENY).build()),
                Stream.of(
                    acl.getLines()
                        .get(lineNumber)
                        .toBuilder()
                        .setAction(LineAction.PERMIT)
                        .build()))
            .collect(ImmutableList.toImmutableList());
    return IpAccessList.builder().setName(acl.getName()).setLines(lines).build();
  }

  @VisibleForTesting
  static IpAccessList toDenyAcl(IpAccessList acl) {
    List<IpAccessListLine> lines =
        Streams.concat(
                acl.getLines()
                    .stream()
                    .map(
                        l ->
                            l.toBuilder()
                                // flip action
                                .setAction(
                                    l.getAction() == LineAction.PERMIT
                                        ? LineAction.DENY
                                        : LineAction.PERMIT)
                                .build()),
                // accept if we reach the end of the ACL
                Stream.of(IpAccessListLine.ACCEPT_ALL))
            .collect(ImmutableList.toImmutableList());
    return IpAccessList.builder().setName(acl.getName()).setLines(lines).build();
  }

  private Rows traceFilterRows(boolean base, String hostname, IpAccessList acl, Flow flow) {
    if (base) {
      _batfish.pushBaseEnvironment();
    } else {
      _batfish.pushDeltaEnvironment();
    }
    Rows rows = traceFilterRows(hostname, acl, flow);
    _batfish.popEnvironment();
    return rows;
  }

  @VisibleForTesting
  @Nonnull
  Rows traceFilterRows(String hostname, IpAccessList acl, Flow flow) {
    TraceFiltersQuestion traceFiltersQuestion =
        new TraceFiltersQuestion(new NodesSpecifier(hostname), acl.getName());
    traceFiltersQuestion.setDscp(flow.getDscp());
    traceFiltersQuestion.setDst(flow.getDstIp().toString());
    traceFiltersQuestion.setDstPort(flow.getDstPort());
    traceFiltersQuestion.setEcn(flow.getEcn());
    traceFiltersQuestion.setFragmentOffset(flow.getFragmentOffset());
    traceFiltersQuestion.setIcmpCode(flow.getFragmentOffset());
    traceFiltersQuestion.setIcmpType(flow.getFragmentOffset());
    traceFiltersQuestion.setIngressInterface(flow.getIngressInterface());
    traceFiltersQuestion.setIpProtocol(flow.getIpProtocol());
    traceFiltersQuestion.setPacketLength(flow.getPacketLength());
    traceFiltersQuestion.setSrcIp(flow.getSrcIp());
    traceFiltersQuestion.setSrcPort(flow.getSrcPort());
    traceFiltersQuestion.setState(flow.getState());
    traceFiltersQuestion.setTcpFlagsAck(flow.getTcpFlagsAck() == 1);
    traceFiltersQuestion.setTcpFlagsCwr(flow.getTcpFlagsCwr() == 1);
    traceFiltersQuestion.setTcpFlagsEce(flow.getTcpFlagsEce() == 1);
    traceFiltersQuestion.setTcpFlagsFin(flow.getTcpFlagsFin() == 1);
    traceFiltersQuestion.setTcpFlagsPsh(flow.getTcpFlagsPsh() == 1);
    traceFiltersQuestion.setTcpFlagsRst(flow.getTcpFlagsRst() == 1);
    traceFiltersQuestion.setTcpFlagsSyn(flow.getTcpFlagsSyn() == 1);
    traceFiltersQuestion.setTcpFlagsUrg(flow.getTcpFlagsUrg() == 1);
    return new TraceFiltersAnswerer(traceFiltersQuestion, _batfish).answer().getRows();
  }
}
