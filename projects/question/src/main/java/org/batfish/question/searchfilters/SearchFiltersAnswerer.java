package org.batfish.question.searchfilters;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;
import static org.batfish.question.FilterQuestionUtils.differentialBDDSourceManager;
import static org.batfish.question.FilterQuestionUtils.getFlow;
import static org.batfish.question.FilterQuestionUtils.resolveSources;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COLUMN_METADATA;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_FILTER_NAME;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_NODE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.question.testfilters.TestFiltersAnswerer;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Answerer for SearchFiltersQuestion */
public final class SearchFiltersAnswerer extends Answerer {

  @VisibleForTesting
  public static final Function<String, String> NEGATED_RENAMER =
      name -> String.format("~~ Negated ACL: %s ~~", name);

  @VisibleForTesting
  public static final BiFunction<Integer, String, String> MATCH_LINE_RENAMER =
      (line, name) -> String.format("~~ Match-Line %d ACL: %s ~~", line, name);

  private TableAnswerElement _tableAnswerElement;

  public SearchFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SearchFiltersQuestion question = (SearchFiltersQuestion) _question;
    nonDifferentialAnswer(snapshot, question);
    return _tableAnswerElement;
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    differentialAnswer((SearchFiltersQuestion) _question, snapshot, reference);
    return _tableAnswerElement;
  }

  private void differentialAnswer(
      SearchFiltersQuestion question, NetworkSnapshot snapshot, NetworkSnapshot reference) {
    SearchFiltersParameters parameters = question.toSearchFiltersParameters();

    TableAnswerElement baseTable = new TableAnswerElement(new TableMetadata(COLUMN_METADATA));
    TableAnswerElement deltaTable = new TableAnswerElement(new TableMetadata(COLUMN_METADATA));

    Multimap<String, String> baseAcls = getSpecifiedAcls(snapshot, question);
    Multimap<String, String> deltaAcls = getSpecifiedAcls(reference, question);
    Map<String, Configuration> baseConfigs = _batfish.loadConfigurations(snapshot);
    Map<String, Configuration> deltaConfigs = _batfish.loadConfigurations(reference);

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
        if (baseAcl.isPresent() && !deltaAcl.isPresent() && question.getIncludeOneTableKeys()) {
          baseTable.addRow(
              Row.builder(baseTable.getMetadata().toColumnMap())
                  .put(COL_NODE, node)
                  .put(COL_FILTER_NAME, aclName)
                  .build());
          continue;
        }
        if (!baseAcl.isPresent() && question.getIncludeOneTableKeys()) {
          deltaTable.addRow(
              Row.builder(deltaTable.getMetadata().toColumnMap())
                  .put(COL_NODE, node)
                  .put(COL_FILTER_NAME, aclName)
                  .build());
          continue;
        }

        // present in both snapshot
        DifferentialSearchFiltersResult result =
            differentialReachFilter(
                snapshot,
                _batfish,
                baseConfig,
                baseAcl.get(),
                deltaConfig,
                deltaAcl.get(),
                parameters);

        Stream.of(result.getDecreasedFlow(), result.getIncreasedFlow())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(
                flow -> {
                  baseTable.addRow(testFiltersRow(snapshot, node, aclName, flow));
                  deltaTable.addRow(testFiltersRow(reference, node, aclName, flow));
                });
      }
    }

    // take care of nodes that are present in only one snapshot
    if (question.getIncludeOneTableKeys()) {
      addOneSnapshotNodes(Sets.difference(baseAcls.keySet(), deltaAcls.keySet()), baseTable);
      addOneSnapshotNodes(Sets.difference(deltaAcls.keySet(), baseAcls.keySet()), deltaTable);
    }

    TableAnswerElement diffTable =
        TableDiff.diffTables(baseTable, deltaTable, question.getIncludeOneTableKeys());

    _tableAnswerElement = new TableAnswerElement(diffTable.getMetadata());
    _tableAnswerElement.postProcessAnswer(question, diffTable.getRows().getData());
  }

  private void nonDifferentialAnswer(NetworkSnapshot snapshot, SearchFiltersQuestion question) {
    List<Triple<String, String, IpAccessList>> acls =
        getQueryAcls(_batfish.getSnapshot(), question);
    if (acls.isEmpty()) {
      throw new BatfishException("No matching filters");
    }

    Multiset<Row> rows = HashMultiset.create();
    /*
     * For each query ACL, try to get a flow. If one exists, run traceFilter on that flow.
     * Concatenate the answers for all flows into one big table.
     */
    Map<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);
    for (Triple<String, String, IpAccessList> triple : acls) {
      String hostname = triple.getLeft();
      String aclname = triple.getMiddle();
      Configuration node = configurations.get(hostname);
      IpAccessList acl = triple.getRight();
      Optional<Flow> optionalResultFlow =
          reachFilter(snapshot, _batfish, node, acl, question.toSearchFiltersParameters());
      optionalResultFlow.ifPresent(
          flow -> rows.add(testFiltersRow(snapshot, hostname, aclname, flow)));
    }

    _tableAnswerElement = new TableAnswerElement(new TableMetadata(COLUMN_METADATA));
    _tableAnswerElement.postProcessAnswer(question, rows);
  }

  private Multimap<String, String> getSpecifiedAcls(
      NetworkSnapshot snapshot, SearchFiltersQuestion question) {
    SortedMap<String, Configuration> configs = _batfish.loadConfigurations(snapshot);
    FilterSpecifier filterSpecifier = question.getFilterSpecifier();
    SpecifierContext specifierContext = _batfish.specifierContext(snapshot);
    ImmutableMultimap.Builder<String, String> acls = ImmutableMultimap.builder();
    question.getNodesSpecifier().resolve(specifierContext).stream()
        .map(configs::get)
        .forEach(
            config ->
                filterSpecifier
                    .resolve(config.getHostname(), specifierContext)
                    .forEach(acl -> acls.put(config.getHostname(), acl.getName())));
    return acls.build();
  }

  private Optional<IpAccessList> makeQueryAcl(IpAccessList originalAcl) {
    SearchFiltersQuestion question = (SearchFiltersQuestion) _question;
    switch (question.getType()) {
      case PERMIT:
        return Optional.of(originalAcl);
      case DENY:
        return Optional.of(toDenyAcl(originalAcl));
      case MATCH_LINE:
        // for each ACL, construct a new ACL that accepts if and only if the specified line matches
        Integer lineNumber = question.getLineNumber();
        checkState(lineNumber != null, "Cannot perform a match line query without a line number");
        return originalAcl.getLines().size() > lineNumber
            ? Optional.of(toMatchLineAcl(lineNumber, originalAcl))
            : Optional.empty();
      default:
        throw new BatfishException("Unexpected query Type: " + question.getType());
    }
  }

  // Each triple in the result is a node name, ACL name, and the ACL itself.  The ACL itself
  // may rename the ACL, so we explicitly keep track of the original name for later use.
  @VisibleForTesting
  List<Triple<String, String, IpAccessList>> getQueryAcls(
      NetworkSnapshot snapshot, SearchFiltersQuestion question) {
    Map<String, Configuration> configs = _batfish.loadConfigurations(snapshot);
    return getSpecifiedAcls(snapshot, question).entries().stream()
        .map(
            entry -> {
              String hostName = entry.getKey();
              String aclName = entry.getValue();
              Optional<IpAccessList> queryAcl =
                  makeQueryAcl(configs.get(hostName).getIpAccessLists().get(aclName));
              return queryAcl.map(acl -> ImmutableTriple.of(hostName, aclName, acl));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  static IpAccessList toMatchLineAcl(Integer lineNumber, IpAccessList acl) {
    CopierWithAction permittingCopier = new CopierWithAction(LineAction.PERMIT);
    CopierWithAction denyingCopier = new CopierWithAction(LineAction.DENY);
    List<AclLine> lines =
        Streams.concat(
                // Deny everything that matches any previous line
                acl.getLines().subList(0, lineNumber).stream().map(denyingCopier::visit),
                // Permit everything that matches selected line
                Stream.of(permittingCopier.visit(acl.getLines().get(lineNumber))))
            .collect(ImmutableList.toImmutableList());
    return IpAccessList.builder()
        .setName(MATCH_LINE_RENAMER.apply(lineNumber, acl.getName()))
        .setLines(lines)
        .build();
  }

  @VisibleForTesting
  static IpAccessList toDenyAcl(IpAccessList acl) {
    List<AclLine> lines =
        Streams.concat(
                acl.getLines().stream().map(ComplementaryCopier::copy),
                // accept if we reach the end of the ACL
                Stream.of(ExprAclLine.ACCEPT_ALL))
            .collect(ImmutableList.toImmutableList());
    return IpAccessList.builder()
        .setName(NEGATED_RENAMER.apply(acl.getName()))
        .setLines(lines)
        .build();
  }

  /**
   * Creates a copy of the visited {@link AclLine} that matches the same flows but always takes the
   * provided action.
   */
  private static final class CopierWithAction implements GenericAclLineVisitor<AclLine> {
    private final LineAction _action;

    CopierWithAction(LineAction action) {
      _action = action;
    }

    @Override
    public AclLine visitExprAclLine(ExprAclLine exprAclLine) {
      return exprAclLine.toBuilder().setAction(_action).build();
    }
  }

  /**
   * Creates a copy of the visited {@link AclLine} that matches all the same flows as the original
   * line, but permits the flows the original denies and vice versa.
   */
  private static final class ComplementaryCopier implements GenericAclLineVisitor<AclLine> {
    private static final ComplementaryCopier INSTANCE = new ComplementaryCopier();

    private ComplementaryCopier() {}

    static AclLine copy(AclLine line) {
      return INSTANCE.visit(line);
    }

    @Override
    public AclLine visitExprAclLine(ExprAclLine exprAclLine) {
      LineAction newAction =
          exprAclLine.getAction() == LineAction.PERMIT ? LineAction.DENY : LineAction.PERMIT;
      return exprAclLine.toBuilder().setAction(newAction).build();
    }
  }

  private Row testFiltersRow(NetworkSnapshot snapshot, String hostname, String aclName, Flow flow) {
    Configuration c = _batfish.loadConfigurations(snapshot).get(hostname);
    Row row = TestFiltersAnswerer.getRow(c.getIpAccessLists().get(aclName), flow, c);
    return row;
  }

  /** Adds {@code nodes} (which are present in only one snapshot) to the {@code table} */
  private static void addOneSnapshotNodes(SetView<String> nodes, TableAnswerElement table) {
    for (String node : nodes) {
      table.addRow(Row.builder(table.getMetadata().toColumnMap()).put(COL_NODE, node).build());
    }
  }

  @VisibleForTesting
  static Optional<Flow> reachFilter(
      NetworkSnapshot snapshot,
      IBatfish batfish,
      Configuration node,
      IpAccessList acl,
      SearchFiltersParameters parameters) {
    BDDPacket bddPacket = new BDDPacket();

    SpecifierContext specifierContext = batfish.specifierContext(snapshot);

    Set<String> inactiveIfaces =
        Sets.difference(node.getAllInterfaces().keySet(), node.activeInterfaceNames());
    Set<String> activeSources =
        Sets.difference(
            resolveSources(
                specifierContext, parameters.getStartLocationSpecifier(), node.getHostname()),
            inactiveIfaces);
    Set<String> referencedSources = referencedSources(node.getIpAccessLists(), acl);

    BDDSourceManager mgr = BDDSourceManager.forSources(bddPacket, activeSources, referencedSources);

    HeaderSpace headerSpace = parameters.resolveHeaderspace(specifierContext);
    BDD headerSpaceBDD = new HeaderSpaceToBDD(bddPacket, node.getIpSpaces()).toBDD(headerSpace);
    BDD bdd =
        IpAccessListToBdd.toBDD(bddPacket, acl, node.getIpAccessLists(), node.getIpSpaces(), mgr)
            .and(headerSpaceBDD)
            .and(mgr.isValidValue());

    return getFlow(bddPacket, mgr, node.getHostname(), bdd);
  }

  /** Performs a difference reachFilters analysis (both increased and decreased reachability). */
  @VisibleForTesting
  static DifferentialSearchFiltersResult differentialReachFilter(
      NetworkSnapshot snapshot,
      IBatfish batfish,
      Configuration baseConfig,
      IpAccessList baseAcl,
      Configuration deltaConfig,
      IpAccessList deltaAcl,
      SearchFiltersParameters searchFiltersParameters) {
    BDDPacket bddPacket = new BDDPacket();

    HeaderSpace headerSpace =
        searchFiltersParameters.resolveHeaderspace(batfish.specifierContext(snapshot));
    BDD headerSpaceBDD =
        new HeaderSpaceToBDD(bddPacket, baseConfig.getIpSpaces()).toBDD(headerSpace);

    BDDSourceManager mgr =
        differentialBDDSourceManager(
            bddPacket,
            batfish,
            baseConfig,
            deltaConfig,
            baseAcl,
            deltaAcl,
            searchFiltersParameters.getStartLocationSpecifier());

    BDD baseAclBDD =
        IpAccessListToBdd.toBDD(
                bddPacket, baseAcl, baseConfig.getIpAccessLists(), baseConfig.getIpSpaces(), mgr)
            .and(headerSpaceBDD)
            .and(mgr.isValidValue());
    BDD deltaAclBDD =
        IpAccessListToBdd.toBDD(
                bddPacket, deltaAcl, deltaConfig.getIpAccessLists(), deltaConfig.getIpSpaces(), mgr)
            .and(headerSpaceBDD)
            .and(mgr.isValidValue());

    String hostname = baseConfig.getHostname();

    BDD increasedBDD = deltaAclBDD.diff(baseAclBDD);
    Optional<Flow> increasedFlow = getFlow(bddPacket, mgr, hostname, increasedBDD);

    BDD decreasedBDD = baseAclBDD.diff(deltaAclBDD);
    Optional<Flow> decreasedFlow = getFlow(bddPacket, mgr, hostname, decreasedBDD);

    return new DifferentialSearchFiltersResult(
        increasedFlow.orElse(null), decreasedFlow.orElse(null));
  }
}
