package org.batfish.question.comparefilters;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.question.FilterQuestionUtils.getSpecifiedFilters;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.SpecifierContext;

/** An answerer for {@link CompareFiltersQuestion}. */
public class CompareFiltersAnswerer extends Answerer {
  private static final String COL_NODE = "Node";
  private static final String COL_FILTER_NAME = "Filter_Name";
  private static final String COL_CURRENT_ACTION = "Line_Action";
  private static final String COL_CURRENT_LINE = "Line_Index";
  private static final String COL_CURRENT_NAME = "Line_Content";
  private static final String COL_REFERENCE_LINE = "Reference_Line_Index";
  private static final String COL_REFERENCE_NAME = "Reference_Line_Content";

  private static final String END_OF_ACL = "End of ACL";

  private final CompareFiltersQuestion _question;

  public CompareFiltersAnswerer(CompareFiltersQuestion question, IBatfish batfish) {
    super(question, batfish);
    _question = question;
  }

  @Override
  public AnswerElement answer() {
    throw new BatfishException(
        String.format("%s can only be run in differential mode.", _question.getName()));
  }

  @Override
  public AnswerElement answerDiff() {
    _batfish.pushBaseSnapshot();
    SpecifierContext currentContext = _batfish.specifierContext();
    Multimap<String, String> currentFilters =
        getSpecifiedFilters(
            currentContext,
            _question.getNodeSpecifier(),
            _question.getFilterSpecifier(),
            _question.getIgnoreComposites());
    _batfish.popSnapshot();

    _batfish.pushDeltaSnapshot();
    SpecifierContext referenceContext = _batfish.specifierContext();
    Multimap<String, String> referenceFilters =
        getSpecifiedFilters(
            referenceContext,
            _question.getNodeSpecifier(),
            _question.getFilterSpecifier(),
            _question.getIgnoreComposites());
    _batfish.popSnapshot();

    Multimap<String, String> commonFilters =
        Multimaps.filterEntries(
            currentFilters,
            entry -> referenceFilters.containsEntry(entry.getKey(), entry.getValue()));

    BDDPacket bddPacket = new BDDPacket();
    Multiset<Row> rows =
        commonFilters.entries().stream()
            .flatMap(
                entry ->
                    compareFilter(
                        entry.getKey(),
                        entry.getValue(),
                        bddPacket,
                        currentContext,
                        referenceContext))
            .map(filterDifference -> toRow(filterDifference, currentContext, referenceContext))
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement table = new TableAnswerElement(metadata());
    table.postProcessAnswer(_question, rows);
    return table;
  }

  private static Row toRow(
      FilterDifference difference,
      SpecifierContext currentContext,
      SpecifierContext referenceContext) {

    Row.TypedRowBuilder ret = Row.builder(metadata().toColumnMap());

    String hostname = difference.getHostname();
    String filtername = difference.getFilterName();
    ret.put(COL_NODE, new Node(hostname)).put(COL_FILTER_NAME, filtername);

    if (difference.getCurrentIndex() == null) {
      ret.put(COL_CURRENT_LINE, END_OF_ACL)
          .put(COL_CURRENT_ACTION, LineAction.DENY)
          .put(COL_CURRENT_NAME, "");
    } else {
      int index = difference.getCurrentIndex();
      IpAccessListLine line =
          currentContext
              .getConfigs()
              .get(hostname)
              .getIpAccessLists()
              .get(filtername)
              .getLines()
              .get(index);
      ret.put(COL_CURRENT_LINE, index)
          .put(COL_CURRENT_ACTION, line.getAction())
          .put(COL_CURRENT_NAME, line.getName());
    }

    if (difference.getReferenceIndex() == null) {
      ret.put(COL_REFERENCE_LINE, END_OF_ACL).put(COL_REFERENCE_NAME, "");
    } else {
      int index = difference.getReferenceIndex();
      IpAccessListLine line =
          referenceContext
              .getConfigs()
              .get(hostname)
              .getIpAccessLists()
              .get(filtername)
              .getLines()
              .get(index);
      ret.put(COL_REFERENCE_LINE, index).put(COL_REFERENCE_NAME, line.getName());
    }

    return ret.build();
  }

  private static Stream<FilterDifference> compareFilter(
      String hostname,
      String filtername,
      BDDPacket bddPacket,
      SpecifierContext currentContext,
      SpecifierContext referenceContext) {
    Configuration currentConfig = currentContext.getConfigs().get(hostname);
    Map<String, IpAccessList> currentAcls = currentConfig.getIpAccessLists();
    Map<String, IpSpace> currentIpSpaces = currentConfig.getIpSpaces();
    IpAccessList currentAcl = currentAcls.get(filtername);
    List<LineAction> currentActions =
        currentAcl.getLines().stream()
            .map(IpAccessListLine::getAction)
            .collect(ImmutableList.toImmutableList());
    BDDSourceManager currentSrcMgr =
        BDDSourceManager.forIpAccessList(bddPacket, currentConfig, currentAcl);
    IpAccessListToBdd currentToBdd =
        new IpAccessListToBddImpl(bddPacket, currentSrcMgr, currentAcls, currentIpSpaces);
    List<BDD> currentBdds = currentToBdd.reachAndMatchLines(currentAcl);

    Configuration referenceConfig = referenceContext.getConfigs().get(hostname);
    Map<String, IpAccessList> referenceAcls = referenceConfig.getIpAccessLists();
    Map<String, IpSpace> referenceIpSpaces = referenceConfig.getIpSpaces();
    IpAccessList referenceAcl = referenceAcls.get(filtername);
    List<LineAction> referenceActions =
        referenceAcl.getLines().stream()
            .map(IpAccessListLine::getAction)
            .collect(ImmutableList.toImmutableList());
    BDDSourceManager referenceSrcMgr =
        BDDSourceManager.forIpAccessList(bddPacket, referenceConfig, referenceAcl);
    List<BDD> referenceBdds =
        new IpAccessListToBddImpl(bddPacket, referenceSrcMgr, referenceAcls, referenceIpSpaces)
            .reachAndMatchLines(referenceAcl);
    return compareFilters(
        hostname, filtername, currentActions, currentBdds, referenceActions, referenceBdds);
  }

  @VisibleForTesting
  static Stream<FilterDifference> compareFilters(
      String hostname,
      String filtername,
      List<LineAction> currentActions,
      List<BDD> currentLineBdds,
      List<LineAction> referenceActions,
      List<BDD> referenceLineBdds) {
    checkArgument(!currentLineBdds.isEmpty());
    checkArgument(!referenceLineBdds.isEmpty());
    checkArgument(currentActions.size() == currentLineBdds.size() - 1);
    checkArgument(referenceActions.size() == referenceLineBdds.size() - 1);
    assert currentLineBdds.stream().reduce(BDD::or).get().isOne();
    assert referenceLineBdds.stream().reduce(BDD::or).get().isOne();

    return IntStream.range(0, currentLineBdds.size())
        .mapToObj(
            i -> {
              LineAction currentAction =
                  i < currentActions.size() ? currentActions.get(i) : LineAction.DENY;
              BDD currentLineBdd = currentLineBdds.get(i);
              return IntStream.range(0, referenceLineBdds.size())
                  .filter(
                      j -> {
                        LineAction referenceAction =
                            j < referenceActions.size() ? referenceActions.get(j) : LineAction.DENY;
                        return referenceAction != currentAction;
                      })
                  .filter(j -> !currentLineBdd.and(referenceLineBdds.get(j)).isZero())
                  .mapToObj(
                      j ->
                          new FilterDifference(
                              hostname,
                              filtername,
                              i < currentActions.size() ? i : null,
                              j < referenceActions.size() ? j : null));
            })
        .flatMap(Function.identity());
  }

  /** Create metadata for the new traceroute v2 answer */
  private static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.NODE, "Hostname.", true, false),
            new ColumnMetadata(COL_FILTER_NAME, Schema.STRING, "The filter name.", false, true),
            new ColumnMetadata(
                COL_CURRENT_LINE,
                Schema.STRING,
                "The index of the line in the current filter.",
                true,
                false),
            new ColumnMetadata(
                COL_CURRENT_NAME, Schema.STRING, "The current filter line content.", false, true),
            new ColumnMetadata(
                COL_CURRENT_ACTION, Schema.STRING, "The current filter line action.", false, true),
            new ColumnMetadata(
                COL_REFERENCE_LINE,
                Schema.STRING,
                "The index of the line in the reference filter.",
                false,
                true),
            new ColumnMetadata(
                COL_REFERENCE_NAME,
                Schema.STRING,
                "The reference filter line content.",
                true,
                false));
    return new TableMetadata(columnMetadata, "Lines that change treat flows differently.");
  }
}
