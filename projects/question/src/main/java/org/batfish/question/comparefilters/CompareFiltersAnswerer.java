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
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;

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
    NodeSpecifier nodeSpecifier =
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier(_question.getNodes());
    FilterSpecifier filterSpecifier =
        new FlexibleFilterSpecifierFactory().buildFilterSpecifier(_question.getFilters());

    _batfish.pushBaseSnapshot();
    SortedMap<String, Configuration> currentConfigs = _batfish.loadConfigurations();
    Multimap<String, String> currentFilters =
        getSpecifiedFilters(
            currentConfigs, nodeSpecifier, filterSpecifier, _batfish.specifierContext());
    _batfish.popSnapshot();

    _batfish.pushDeltaSnapshot();
    SortedMap<String, Configuration> referenceConfigs = _batfish.loadConfigurations();
    Multimap<String, String> referenceFilters =
        getSpecifiedFilters(
            currentConfigs, nodeSpecifier, filterSpecifier, _batfish.specifierContext());
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
                        currentConfigs,
                        referenceConfigs))
            .map(filterDifference -> toRow(filterDifference, currentConfigs, referenceConfigs))
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement table = new TableAnswerElement(metadata());
    table.postProcessAnswer(_question, rows);
    return table;
  }

  private static Row toRow(
      FilterDifference difference,
      Map<String, Configuration> currentConfigurations,
      Map<String, Configuration> referenceConfigurations) {
    String hostname = difference.getHostname();
    String filtername = difference.getFilterName();

    Integer currentIndex = difference.getCurrentIndex();
    String currentIndexStr = currentIndex == null ? END_OF_ACL : currentIndex.toString();
    IpAccessListLine currentLine =
        currentIndex == null
            ? null
            : currentConfigurations
                .get(hostname)
                .getIpAccessLists()
                .get(filtername)
                .getLines()
                .get(currentIndex);
    LineAction currentAction = currentIndex == null ? LineAction.DENY : currentLine.getAction();
    String currentLineName = currentIndex == null ? "" : currentLine.getName();

    Integer referenceIndex = difference.getReferenceIndex();
    String referenceIndexStr = referenceIndex == null ? END_OF_ACL : referenceIndex.toString();
    String referenceLineName =
        referenceIndex == null
            ? ""
            : referenceConfigurations
                .get(hostname)
                .getIpAccessLists()
                .get(filtername)
                .getLines()
                .get(referenceIndex)
                .getName();
    return Row.builder()
        .put(COL_NODE, new Node(hostname))
        .put(COL_FILTER_NAME, filtername)
        .put(COL_CURRENT_LINE, currentIndexStr)
        .put(COL_CURRENT_NAME, currentLineName)
        .put(COL_CURRENT_ACTION, currentAction)
        .put(COL_REFERENCE_LINE, referenceIndexStr)
        .put(COL_REFERENCE_NAME, referenceLineName)
        .build();
  }

  private static Stream<FilterDifference> compareFilter(
      String hostname,
      String filtername,
      BDDPacket bddPacket,
      SortedMap<String, Configuration> currentConfigs,
      SortedMap<String, Configuration> referenceConfigs) {
    Configuration currentConfig = currentConfigs.get(hostname);
    Map<String, IpAccessList> currentAcls = currentConfig.getIpAccessLists();
    Map<String, IpSpace> currentIpSpaces = currentConfig.getIpSpaces();
    IpAccessList currentAcl = currentAcls.get(filtername);
    List<LineAction> currentActions =
        currentAcl.getLines().stream()
            .map(IpAccessListLine::getAction)
            .collect(ImmutableList.toImmutableList());
    BDDSourceManager currentSrcMgr =
        BDDSourceManager.forIpAccessList(bddPacket, currentConfig, currentAcl);
    IpAccessListToBDD currentToBdd =
        new IpAccessListToBDD(bddPacket, currentSrcMgr, currentAcls, currentIpSpaces);
    List<BDD> currentBdds = currentToBdd.reachAndMatchLines(currentAcl);

    Configuration referenceConfig = referenceConfigs.get(hostname);
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
        new IpAccessListToBDD(bddPacket, referenceSrcMgr, referenceAcls, referenceIpSpaces)
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
