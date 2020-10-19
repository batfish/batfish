package org.batfish.question.findmatchingfilterlines;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.table.TableMetadata.toColumnMap;
import static org.batfish.question.FilterQuestionUtils.getSpecifiedFilters;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesQuestion.PROP_IGNORE_COMPOSITES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.MemoizedIpAccessListToBdd;
import org.batfish.common.bdd.PermitAndDenyBdds;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.ActionGetter.LineBehavior;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesQuestion.Action;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** Answerer for FindMatchingFilterLinesQuestion */
@ParametersAreNonnullByDefault
public final class FindMatchingFilterLinesAnswerer extends Answerer {
  public static final String COL_NODE = "Node";
  public static final String COL_FILTER = "Filter";
  public static final String COL_LINE = "Line";
  public static final String COL_LINE_INDEX = "Line_Index";
  public static final String COL_ACTION = "Action";

  public static final List<ColumnMetadata> COLUMN_METADATA =
      ImmutableList.of(
          new ColumnMetadata(COL_NODE, Schema.STRING, "Node", true, false),
          new ColumnMetadata(COL_FILTER, Schema.STRING, "Filter name", true, false),
          new ColumnMetadata(COL_LINE, Schema.STRING, "Line text", true, false),
          new ColumnMetadata(COL_LINE_INDEX, Schema.INTEGER, "Index of line", true, false),
          new ColumnMetadata(
              COL_ACTION,
              Schema.STRING,
              "Action performed by the line (e.g., PERMIT or DENY)",
              true,
              false));

  private static final Map<String, ColumnMetadata> METADATA_MAP = toColumnMap(COLUMN_METADATA);

  FindMatchingFilterLinesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    FindMatchingFilterLinesQuestion question = (FindMatchingFilterLinesQuestion) _question;

    SpecifierContext ctxt = _batfish.specifierContext(snapshot);
    Multimap<String, String> specifiedAcls =
        getSpecifiedFilters(
            ctxt,
            question.getNodeSpecifier(),
            question.getFilterSpecifier(),
            question.getIgnoreComposites());

    // Throw if no filters matched
    if (specifiedAcls.values().isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Did not find any filters that meet the specified criteria. (Tips: Set '%s' to false"
                  + " if you want to analyze combined filters; use 'resolveFilterSpecifier'"
                  + " question to see which filters your nodes and filters match.)",
              PROP_IGNORE_COMPOSITES));
    }

    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    getRows(question.getHeaderConstraints(), question.getAction(), specifiedAcls, ctxt)
        .forEach(answer::addRow);
    return answer;
  }

  private static Stream<Row> getRows(
      PacketHeaderConstraints phc,
      @Nullable Action action,
      Multimap<String, String> acls,
      SpecifierContext ctxt) {
    Map<String, Configuration> configs = ctxt.getConfigs();

    BDDPacket bddPacket = new BDDPacket();

    BDD headerSpaceBdd =
        PacketHeaderConstraintsUtil.toBDD(
            bddPacket,
            phc,
            resolveIpSpace(phc.getSrcIps(), ctxt),
            resolveIpSpace(phc.getDstIps(), ctxt));

    Map<String, BDDSourceManager> mgrMap = BDDSourceManager.forNetwork(bddPacket, configs);

    return acls.keySet().stream()
        .flatMap(
            nodeName ->
                getRowsForNode(
                    configs.get(nodeName),
                    bddPacket,
                    mgrMap.get(nodeName),
                    acls.get(nodeName),
                    headerSpaceBdd,
                    action));
  }

  private static Stream<Row> getRowsForNode(
      Configuration node,
      BDDPacket bddPacket,
      BDDSourceManager mgr,
      Collection<String> acls,
      BDD headerSpaceBdd,
      @Nullable Action action) {
    MemoizedIpAccessListToBdd bddConverter =
        new MemoizedIpAccessListToBdd(bddPacket, mgr, node.getIpAccessLists(), node.getIpSpaces());
    Row.TypedRowBuilder rowBuilder = Row.builder(METADATA_MAP).put(COL_NODE, node.getHostname());
    return acls.stream()
        .flatMap(
            aclName -> {
              List<AclLine> aclLines = node.getIpAccessLists().get(aclName).getLines();
              return getBehaviorToReport(aclLines, headerSpaceBdd, bddConverter, action)
                  .entrySet()
                  .stream()
                  .map(
                      e -> {
                        int lineIndex = e.getKey();
                        AclLine line = aclLines.get(lineIndex);
                        return rowBuilder
                            .put(COL_FILTER, aclName)
                            .put(COL_LINE, firstNonNull(line.getName(), line.toString()))
                            .put(COL_LINE_INDEX, lineIndex)
                            .put(COL_ACTION, e.getValue())
                            .build();
                      });
            });
  }

  /**
   * Returns a map of line index in {@code aclLines} to behavior to report for that line. Only
   * includes lines that should be reported.
   */
  @VisibleForTesting
  static SortedMap<Integer, LineBehavior> getBehaviorToReport(
      List<AclLine> aclLines,
      BDD headerSpaceBdd,
      IpAccessListToBdd bddConverter,
      @Nullable Action action) {
    LineBehaviorFinder behaviorFinder =
        new LineBehaviorFinder(bddConverter, action, headerSpaceBdd);
    ImmutableSortedMap.Builder<Integer, LineBehavior> actionsToReport =
        ImmutableSortedMap.naturalOrder();
    for (int i = 0; i < aclLines.size(); i++) {
      LineBehavior lineBehavior = behaviorFinder.visit(aclLines.get(i));
      if (lineBehavior != null) {
        actionsToReport.put(i, lineBehavior);
      }
    }
    return actionsToReport.build();
  }

  /**
   * Returns the line's behavior if the answer should include the visited line based on provided
   * parameters, otherwise null.
   */
  @VisibleForTesting
  static final class LineBehaviorFinder implements GenericAclLineVisitor<LineBehavior> {
    private final IpAccessListToBdd _ipAccessListToBdd;

    // Restrictions on lines to include, based on question parameters
    @Nullable private final Action _action;
    private final BDD _headerSpaceBdd;

    LineBehaviorFinder(
        IpAccessListToBdd ipAccessListToBdd, @Nullable Action action, BDD headerSpaceBdd) {
      _ipAccessListToBdd = ipAccessListToBdd;
      _action = action;
      _headerSpaceBdd = headerSpaceBdd;
    }

    private boolean actionMatches(LineAction lineAction) {
      return _action == null
          || _action == Action.PERMIT && lineAction == LineAction.PERMIT
          || _action == Action.DENY && lineAction == LineAction.DENY;
    }

    @Override
    public LineBehavior visitAclAclLine(AclAclLine aclAclLine) {
      PermitAndDenyBdds permitAndDenyBdds = _ipAccessListToBdd.toPermitAndDenyBdds(aclAclLine);
      boolean permitsAnything = _headerSpaceBdd.andSat(permitAndDenyBdds.getPermitBdd());
      boolean deniesAnything = _headerSpaceBdd.andSat(permitAndDenyBdds.getDenyBdd());

      if (permitsAnything && deniesAnything) {
        // The line can both permit and deny packets within the specified headerspace.
        // Don't care what action the question specifies; this line can perform either action.
        return LineBehavior.VARIABLE;
      }

      // The line either doesn't match the headerspace or only takes one action on matching packets.
      // In the latter case, report the line if question says to include that action.
      if (permitsAnything && actionMatches(LineAction.PERMIT)) {
        return LineBehavior.PERMIT;
      } else if (deniesAnything && actionMatches(LineAction.DENY)) {
        return LineBehavior.DENY;
      }
      return null;
    }

    @Override
    public LineBehavior visitExprAclLine(ExprAclLine exprAclLine) {
      if (!actionMatches(exprAclLine.getAction())) {
        return null;
      }
      // If there is any overlap between the header space BDD and this line, include it
      BDD lineBdd = _ipAccessListToBdd.toBdd(exprAclLine.getMatchCondition());
      return _headerSpaceBdd.andSat(lineBdd)
          ? LineBehavior.fromLineAction(exprAclLine.getAction())
          : null;
    }
  }

  /** Creates {@link TableMetadata} from the question. */
  private static TableMetadata createMetadata(Question question) {
    String textDesc =
        String.format(
            "Filter ${%s} on node ${%s} has matching line at index ${%s}: ${%s}",
            COL_FILTER, COL_NODE, COL_LINE_INDEX, COL_LINE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(COLUMN_METADATA, textDesc);
  }

  private static IpSpace resolveIpSpace(@Nullable String ips, SpecifierContext ctx) {
    IpSpaceSpecifier specifier =
        SpecifierFactories.getIpSpaceSpecifierOrDefault(
            ips, new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE));
    return firstNonNull(
        AclIpSpace.union(
            specifier.resolve(ImmutableSet.of(), ctx).getEntries().stream()
                .map(Entry::getIpSpace)
                .collect(ImmutableList.toImmutableList())),
        EmptyIpSpace.INSTANCE);
  }
}
