package org.batfish.question.searchfilters;

import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;
import static org.batfish.question.FilterQuestionUtils.differentialBDDSourceManager;
import static org.batfish.question.FilterQuestionUtils.resolveSources;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COLUMN_METADATA;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_FILTER_NAME;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_NODE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.MemoizedIpAccessListToBdd;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.FilterQuestionUtils;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.question.testfilters.TestFiltersAnswerer;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Answerer for SearchFiltersQuestion */
public final class SearchFiltersAnswerer extends Answerer {
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
    SearchFiltersQuery query = question.getQuery();

    TableAnswerElement baseTable = new TableAnswerElement(new TableMetadata(COLUMN_METADATA));
    TableAnswerElement refTable = new TableAnswerElement(new TableMetadata(COLUMN_METADATA));

    Map<String, Map<String, IpAccessList>> acls = getSpecifiedAcls(snapshot, question);
    Map<String, Map<String, IpAccessList>> refAcls = getSpecifiedAcls(reference, question);
    Map<String, DiffConfigContext> configContexts =
        getDiffConfigContexts(acls, refAcls, snapshot, reference, parameters);

    for (Entry<String, DiffConfigContext> e : configContexts.entrySet()) {
      String hostname = e.getKey();
      DiffConfigContext configContext = e.getValue();
      Map<String, IpAccessList> aclsForNode = acls.get(hostname);
      Map<String, IpAccessList> refAclsForNode = refAcls.get(hostname);

      Set<String> commonAcls = Sets.intersection(aclsForNode.keySet(), refAclsForNode.keySet());
      for (String aclName : commonAcls) {
        IpAccessList acl = aclsForNode.get(aclName);
        IpAccessList refAcl = refAclsForNode.get(aclName);

        // If either ACL can't be queried, can't compare them; fill in row in the other table if
        // necessary and continue
        boolean canQueryAcl = query.canQuery(acl);
        boolean canQueryRefAcl = query.canQuery(refAcl);
        if (!canQueryAcl || !canQueryRefAcl) {
          if (question.getIncludeOneTableKeys() && (canQueryAcl || canQueryRefAcl)) {
            // One of them is not null and question specifies to include rows in this case
            TableAnswerElement table = canQueryAcl ? baseTable : refTable;
            table.addRow(
                Row.builder(table.getMetadata().toColumnMap())
                    .put(COL_NODE, new Node(hostname))
                    .put(COL_FILTER_NAME, aclName)
                    .build());
          }
          continue;
        }

        // present in both snapshot
        DifferentialSearchFiltersResult result = getDiffResult(acl, refAcl, configContext, query);

        Stream.of(result.getDecreasedFlow(), result.getIncreasedFlow())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(
                flow -> {
                  baseTable.addRow(testFiltersRow(snapshot, hostname, aclName, flow));
                  refTable.addRow(testFiltersRow(reference, hostname, aclName, flow));
                });
      }
    }

    // take care of nodes that are present in only one snapshot
    if (question.getIncludeOneTableKeys()) {
      addOneSnapshotNodes(Sets.difference(acls.keySet(), refAcls.keySet()), baseTable);
      addOneSnapshotNodes(Sets.difference(refAcls.keySet(), acls.keySet()), refTable);
    }

    TableAnswerElement diffTable =
        TableDiff.diffTables(baseTable, refTable, question.getIncludeOneTableKeys());

    _tableAnswerElement = new TableAnswerElement(diffTable.getMetadata());
    _tableAnswerElement.postProcessAnswer(question, diffTable.getRows().getData());
  }

  private void nonDifferentialAnswer(NetworkSnapshot snapshot, SearchFiltersQuestion question) {
    Map<String, Map<String, IpAccessList>> specifiedAcls = getSpecifiedAcls(snapshot, question);
    if (specifiedAcls.values().stream().allMatch(Map::isEmpty)) {
      throw new BatfishException("No matching filters");
    }

    Multiset<Row> rows = HashMultiset.create();

    /*
     * For each ACL, try to get a flow matching the query. If one exists, run traceFilter on that
     * flow. Concatenate the answers for all flows into one big table.
     */
    SearchFiltersParameters parameters = question.toSearchFiltersParameters();
    SearchFiltersQuery query = question.getQuery();
    for (Entry<String, NonDiffConfigContext> e :
        getConfigContexts(specifiedAcls, snapshot, parameters).entrySet()) {
      String hostname = e.getKey();
      NonDiffConfigContext configContext = e.getValue();
      for (IpAccessList acl : specifiedAcls.get(hostname).values()) {
        // Ensure that query is applicable to acl
        if (!query.canQuery(acl)) {
          continue;
        }

        // Generate representative flow for ACL, if one exists
        Flow flow = configContext.getFlow(configContext.getReachBdd(acl, query));
        if (flow == null) {
          continue;
        }

        // Add result to table
        rows.add(testFiltersRow(snapshot, hostname, acl.getName(), flow));
      }

      _tableAnswerElement = new TableAnswerElement(new TableMetadata(COLUMN_METADATA));
      _tableAnswerElement.postProcessAnswer(question, rows);
    }
  }

  /**
   * Given all specified ACLs on all configs of the given snapshot, returns a {@link
   * NonDiffConfigContext} for each config.
   */
  private Map<String, NonDiffConfigContext> getConfigContexts(
      Map<String, Map<String, IpAccessList>> specifiedAcls,
      NetworkSnapshot snapshot,
      SearchFiltersParameters parameters) {
    Map<String, Configuration> configs = _batfish.loadConfigurations(snapshot);
    BDDPacket pkt = new BDDPacket();
    return specifiedAcls.entrySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e -> {
                  Configuration c = configs.get(e.getKey());
                  Set<String> aclNames = e.getValue().keySet();
                  return new NonDiffConfigContext(c, aclNames, snapshot, _batfish, parameters, pkt);
                }));
  }

  /**
   * Given all specified ACLs in two snapshots, returns a {@link DiffConfigContext} for each config
   * in common between the two snapshots.
   */
  private Map<String, DiffConfigContext> getDiffConfigContexts(
      Map<String, Map<String, IpAccessList>> baseAcls,
      Map<String, Map<String, IpAccessList>> refAcls,
      NetworkSnapshot snapshot,
      NetworkSnapshot reference,
      SearchFiltersParameters parameters) {
    Map<String, Configuration> baseConfigs = _batfish.loadConfigurations(snapshot);
    Map<String, Configuration> refConfigs = _batfish.loadConfigurations(reference);
    BDDPacket pkt = new BDDPacket();

    Set<String> commonNodes = Sets.intersection(baseAcls.keySet(), refAcls.keySet());
    ImmutableMap.Builder<String, DiffConfigContext> configContexts = ImmutableMap.builder();
    for (String hostname : commonNodes) {
      Configuration c = baseConfigs.get(hostname);
      Configuration refC = refConfigs.get(hostname);
      Set<String> commonAcls =
          Sets.intersection(baseAcls.get(hostname).keySet(), refAcls.get(hostname).keySet());
      configContexts.put(
          hostname,
          new DiffConfigContext(
              c, refC, commonAcls, snapshot, reference, _batfish, parameters, pkt));
    }
    return configContexts.build();
  }

  /**
   * Creates a map of hostname to ACL name to {@link IpAccessList} specifying all ACLs to query.
   * Keys include all hostnames matching the query, even if they contain no ACLs matching the query
   * (might matter in differential context where the other snapshot's version of the node does have
   * matching ACLs).
   */
  @VisibleForTesting
  Map<String, Map<String, IpAccessList>> getSpecifiedAcls(
      NetworkSnapshot snapshot, SearchFiltersQuestion question) {
    FilterSpecifier filterSpecifier = question.getFilterSpecifier();
    SpecifierContext specifierContext = _batfish.specifierContext(snapshot);
    return question.getNodesSpecifier().resolve(specifierContext).stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                hostname ->
                    filterSpecifier.resolve(hostname, specifierContext).stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                IpAccessList::getName, Function.identity()))));
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

  private static Set<String> getActiveSources(
      Configuration c, SpecifierContext specifierContext, SearchFiltersParameters parameters) {
    Set<String> inactiveIfaces =
        Sets.difference(c.getAllInterfaces().keySet(), c.activeInterfaceNames());
    return Sets.difference(
        resolveSources(specifierContext, parameters.getStartLocationSpecifier(), c.getHostname()),
        inactiveIfaces);
  }

  /** Performs a difference reachFilters analysis (both increased and decreased reachability). */
  @VisibleForTesting
  static DifferentialSearchFiltersResult getDiffResult(
      IpAccessList acl,
      IpAccessList refAcl,
      DiffConfigContext configContext,
      SearchFiltersQuery query) {
    BDD bdd = configContext.getReachBdd(acl, query, false);
    BDD refBdd = configContext.getReachBdd(refAcl, query, true);

    // Find example increased and decreased flows, if they exist
    BDD increasedBDD = bdd.diff(refBdd);
    BDD decreasedBDD = refBdd.diff(bdd);
    Flow increasedFlow = configContext.getFlow(increasedBDD);
    Flow decreasedFlow = configContext.getFlow(decreasedBDD);

    return new DifferentialSearchFiltersResult(increasedFlow, decreasedFlow);
  }

  /** Holds BDD state for one configuration */
  @VisibleForTesting
  static final class NonDiffConfigContext {
    private final String _hostname;
    private final IpAccessListToBdd _ipAccessListToBdd;

    private final BDDPacket _pkt;
    private final BDDSourceManager _mgr;
    private final BDD _prerequisiteBdd;

    NonDiffConfigContext(
        Configuration config,
        Set<String> specifiedAcls,
        NetworkSnapshot snapshot,
        IBatfish batfish,
        SearchFiltersParameters parameters,
        BDDPacket pkt) {
      _hostname = config.getHostname();
      _pkt = pkt;

      SpecifierContext specifierContext = batfish.specifierContext(snapshot);
      Set<String> activeSources = getActiveSources(config, specifierContext, parameters);
      Set<String> referencedSources = referencedSources(config.getIpAccessLists(), specifiedAcls);
      _mgr = BDDSourceManager.forSources(_pkt, activeSources, referencedSources);
      AclLineMatchExpr headerSpace = parameters.resolveHeaderspace(specifierContext);
      BDD headerSpaceBdd =
          new IpAccessListToBddImpl(
                  _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), config.getIpSpaces())
              .toBdd(headerSpace);

      _prerequisiteBdd = headerSpaceBdd.and(_mgr.isValidValue());

      _ipAccessListToBdd =
          new MemoizedIpAccessListToBdd(
              _pkt, _mgr, config.getIpAccessLists(), config.getIpSpaces());
    }

    /**
     * Returns the BDD representing all flows that match the query for the given ACL. Assumes the
     * question is applicable to the ACL (see {@link SearchFiltersQuery#canQuery(IpAccessList)}).
     */
    @Nonnull
    BDD getReachBdd(IpAccessList acl, SearchFiltersQuery query) {
      return query.getMatchingBdd(acl, _ipAccessListToBdd, _pkt).and(_prerequisiteBdd);
    }

    /** Returns a concrete flow satisfying the input {@link BDD}, if one exists. */
    @Nullable
    Flow getFlow(BDD reachBdd) {
      return FilterQuestionUtils.getFlow(_pkt, _mgr, _hostname, reachBdd).orElse(null);
    }
  }

  @VisibleForTesting
  /** Holds BDD state for two snapshots' versions of one configuration */
  static final class DiffConfigContext {
    private final String _hostname;
    private final IpAccessListToBdd _ipAccessListToBdd;
    private final IpAccessListToBdd _refIpAccessListToBdd;

    private final BDDPacket _pkt;
    private final BDDSourceManager _mgr;
    private final BDD _prerequisiteBdd;

    DiffConfigContext(
        Configuration config,
        Configuration refConfig,
        Set<String> specifiedAcls,
        NetworkSnapshot snapshot,
        NetworkSnapshot refSnapshot,
        IBatfish batfish,
        SearchFiltersParameters parameters,
        BDDPacket pkt) {
      // Both configs should share the same hostname
      _hostname = config.getHostname();
      _pkt = pkt;

      SpecifierContext specifierContext = batfish.specifierContext(snapshot);
      SpecifierContext refSpecifierContext = batfish.specifierContext(refSnapshot);
      _mgr =
          differentialBDDSourceManager(
              _pkt,
              specifierContext,
              refSpecifierContext,
              config,
              refConfig,
              specifiedAcls,
              parameters.getStartLocationSpecifier());

      // TODO: How to adjust _headerSpace in differential context?
      AclLineMatchExpr headerSpace = parameters.resolveHeaderspace(specifierContext);
      BDD headerSpaceBdd =
          new IpAccessListToBddImpl(
                  _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), config.getIpSpaces())
              .toBdd(headerSpace);

      _prerequisiteBdd = headerSpaceBdd.and(_mgr.isValidValue());

      _ipAccessListToBdd =
          new MemoizedIpAccessListToBdd(
              _pkt, _mgr, config.getIpAccessLists(), config.getIpSpaces());
      _refIpAccessListToBdd =
          new MemoizedIpAccessListToBdd(
              _pkt, _mgr, refConfig.getIpAccessLists(), refConfig.getIpSpaces());
    }

    /**
     * Returns the BDD representing all flows that match the query for the given ACL. Assumes the
     * question is applicable to the ACL (see {@link SearchFiltersQuery#canQuery(IpAccessList)}).
     *
     * @param reference Whether the provided ACL is from the reference snapshot
     */
    @Nonnull
    BDD getReachBdd(IpAccessList acl, SearchFiltersQuery query, boolean reference) {
      IpAccessListToBdd ipAccessListToBdd = reference ? _refIpAccessListToBdd : _ipAccessListToBdd;
      return query.getMatchingBdd(acl, ipAccessListToBdd, _pkt).and(_prerequisiteBdd);
    }

    /** Returns a concrete flow satisfying the input {@link BDD}, if one exists. */
    @Nullable
    Flow getFlow(BDD reachBdd) {
      return FilterQuestionUtils.getFlow(_pkt, _mgr, _hostname, reachBdd).orElse(null);
    }
  }
}
