package org.batfish.main;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toMap;
import static org.batfish.bddreachability.BDDMultipathInconsistency.computeMultipathInconsistencies;
import static org.batfish.bddreachability.BDDReachabilityUtils.constructFlows;
import static org.batfish.common.runtime.SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA;
import static org.batfish.common.util.CompletionMetadataUtils.getFilterNames;
import static org.batfish.common.util.CompletionMetadataUtils.getInterfaces;
import static org.batfish.common.util.CompletionMetadataUtils.getIps;
import static org.batfish.common.util.CompletionMetadataUtils.getLocationCompletionMetadata;
import static org.batfish.common.util.CompletionMetadataUtils.getMlagIds;
import static org.batfish.common.util.CompletionMetadataUtils.getNodes;
import static org.batfish.common.util.CompletionMetadataUtils.getPrefixes;
import static org.batfish.common.util.CompletionMetadataUtils.getRoutingPolicyNames;
import static org.batfish.common.util.CompletionMetadataUtils.getStructureNames;
import static org.batfish.common.util.CompletionMetadataUtils.getVrfs;
import static org.batfish.common.util.CompletionMetadataUtils.getZones;
import static org.batfish.common.util.isp.IspModelingUtils.INTERNET_HOST_NAME;
import static org.batfish.datamodel.InactiveReason.AUTOSTATE_FAILURE;
import static org.batfish.datamodel.InactiveReason.IGNORE_MGMT;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.interface_dependency.InterfaceDependencies.getInterfacesToDeactivate;
import static org.batfish.main.ReachabilityParametersResolver.resolveReachabilityParameters;
import static org.batfish.main.StreamDecoder.decodeStreamAndAppendNewline;
import static org.batfish.specifier.LocationInfoUtils.computeLocationInfo;
import static org.batfish.vendor.check_point_management.parsing.CheckpointManagementParser.parseCheckpointManagementData;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.bddreachability.BDDLoopDetectionAnalysis;
import org.batfish.bddreachability.BDDReachabilityAnalysis;
import org.batfish.bddreachability.BDDReachabilityAnalysisFactory;
import org.batfish.bddreachability.BidirectionalReachabilityAnalysis;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.ErrorDetails;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.BgpTablePlugin;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.plugin.ExternalBgpAdvertisementPlugin;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.PluginClientType;
import org.batfish.common.plugin.PluginConsumer;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1TopologiesFactory;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.TopologyContainer;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.isp.IspModelingUtils;
import org.batfish.common.util.isp.IspModelingUtils.ModeledNodes;
import org.batfish.config.Settings;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerMetadataUtil;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ConvertStatus;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.InitStepAnswerElement;
import org.batfish.datamodel.answers.ParseAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpTopologyUtils;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspConfigurationException;
import org.batfish.datamodel.ospf.OspfTopologyUtils;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.BgpTableFormat;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.NopFlattener;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.juniper.JuniperCombinedParser;
import org.batfish.grammar.juniper.JuniperFlattener;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedCombinedParser;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedFlattener;
import org.batfish.grammar.vyos.VyosCombinedParser;
import org.batfish.grammar.vyos.VyosFlattener;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.IdResolver;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.identifiers.StorageBasedIdResolver;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.job.ConvertConfigurationJob;
import org.batfish.job.ParseEnvironmentBgpTableJob;
import org.batfish.job.ParseResult;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.question.SrcNattedConstraint;
import org.batfish.question.bidirectionalreachability.BidirectionalReachabilityResult;
import org.batfish.question.differentialreachability.DifferentialReachabilityParameters;
import org.batfish.question.differentialreachability.DifferentialReachabilityResult;
import org.batfish.question.multipath.MultipathConsistencyParameters;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.representation.aws.AwsConfiguration;
import org.batfish.representation.azure.AzureConfiguration;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.role.InferRoles;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.NodeRolesData.Type;
import org.batfish.role.RoleMapping;
import org.batfish.specifier.AllInterfaceLinksLocationSpecifier;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceAssignmentSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierContextImpl;
import org.batfish.specifier.UnionLocationSpecifier;
import org.batfish.storage.FileBasedStorage;
import org.batfish.storage.StorageProvider;
import org.batfish.symbolic.IngressLocation;
import org.batfish.topology.TopologyProviderImpl;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.check_point_management.CheckpointManagementConfiguration;
import org.batfish.version.BatfishVersion;

/** This class encapsulates the main control logic for Batfish. */
public class Batfish extends PluginConsumer implements IBatfish {

  private static final Pattern MANAGEMENT_INTERFACES =
      Pattern.compile(
          "(\\Amgmt)|(\\Amanagement)|(\\Afxp0)|(\\Aem0)|(\\Ame0)|(\\Avme)|(\\Awlan-ap)|(\\Aeth\\d+-mgmt\\d+)",
          CASE_INSENSITIVE);

  private static final Pattern MANAGEMENT_VRFS =
      Pattern.compile("(\\Amgmt)|(\\Amanagement)", CASE_INSENSITIVE);

  static void checkTopology(Map<String, Configuration> configurations, Topology topology) {
    for (Edge edge : topology.getEdges()) {
      if (!configurations.containsKey(edge.getNode1())) {
        throw new BatfishException(
            String.format("Topology contains a non-existent node '%s'", edge.getNode1()));
      }
      if (!configurations.containsKey(edge.getNode2())) {
        throw new BatfishException(
            String.format("Topology contains a non-existent node '%s'", edge.getNode2()));
      }
      // nodes are valid, now checking corresponding interfaces
      Configuration config1 = configurations.get(edge.getNode1());
      Configuration config2 = configurations.get(edge.getNode2());
      if (!config1.getAllInterfaces().containsKey(edge.getInt1())) {
        throw new BatfishException(
            String.format(
                "Topology contains a non-existent interface '%s' on node '%s'",
                edge.getInt1(), edge.getNode1()));
      }
      if (!config2.getAllInterfaces().containsKey(edge.getInt2())) {
        throw new BatfishException(
            String.format(
                "Topology contains a non-existent interface '%s' on node '%s'",
                edge.getInt2(), edge.getNode2()));
      }
    }
  }

  public static @Nonnull Flattener flatten(
      String input,
      BatfishLogger logger,
      GrammarSettings settings,
      Warnings warnings,
      ConfigurationFormat format,
      String header) {
    switch (format) {
      case PALO_ALTO_NESTED:
        {
          PaloAltoNestedCombinedParser parser = new PaloAltoNestedCombinedParser(input, settings);
          ParserRuleContext tree = parse(parser, logger, settings);
          PaloAltoNestedFlattener flattener =
              new PaloAltoNestedFlattener(
                  VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER);
          ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
          try {
            walker.walk(flattener, tree);
          } catch (BatfishParseException e) {
            warnings.setErrorDetails(e.getErrorDetails());
            throw new BatfishException(
                String.format("Error flattening %s config", format.getVendorString()), e);
          }
          return flattener;
        }

      case JUNIPER:
        {
          JuniperCombinedParser parser = new JuniperCombinedParser(input, settings);
          ParserRuleContext tree = parse(parser, logger, settings);
          JuniperFlattener flattener =
              new JuniperFlattener(
                  VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER, input);
          ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
          try {
            walker.walk(flattener, tree);
          } catch (BatfishParseException e) {
            warnings.setErrorDetails(e.getErrorDetails());
            throw new BatfishException(
                String.format("Error flattening %s config", format.getVendorString()), e);
          }
          return flattener;
        }

      case VYOS:
        {
          VyosCombinedParser parser = new VyosCombinedParser(input, settings);
          ParserRuleContext tree = parse(parser, logger, settings);
          VyosFlattener flattener =
              new VyosFlattener(VendorConfigurationFormatDetector.BATFISH_FLATTENED_VYOS_HEADER);
          ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
          try {
            walker.walk(flattener, tree);
          } catch (BatfishParseException e) {
            warnings.setErrorDetails(e.getErrorDetails());
            throw new BatfishException(
                String.format("Error flattening %s config", format.getVendorString()), e);
          }
          return flattener;
        }

      // $CASES-OMITTED$
      default:
        return new NopFlattener(input);
    }
  }

  private void initLocalSettings(Settings settings) {
    if (settings == null || settings.getStorageBase() == null || settings.getContainer() == null) {
      // This should only happen in tests.
      return;
    }
    _snapshot = settings.getTestrig();
    if (_snapshot == null) {
      throw new CleanBatfishException("Must supply argument to -" + BfConsts.ARG_TESTRIG);
    }
    _referenceSnapshot = settings.getDeltaTestrig();
  }

  /**
   * Reads the snapshot input objects corresponding to the provided keys, and returns a map from
   * each object's key to its contents.
   */
  private @Nonnull SortedMap<String, String> readAllInputObjects(
      Stream<String> keys, NetworkSnapshot snapshot) {
    return keys.parallel()
        .map(
            key -> {
              _logger.debugf("Reading: \"%s\"\n", key);
              try (InputStream inputStream =
                  _storage.loadSnapshotInputObject(
                      snapshot.getNetwork(), snapshot.getSnapshot(), key)) {
                return new SimpleEntry<>(key, decodeStreamAndAppendNewline(inputStream));
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            })
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Ordering.natural(), SimpleEntry::getKey, SimpleEntry::getValue));
  }

  public static void logWarnings(BatfishLogger logger, Warnings warnings) {
    for (Warning warning : warnings.getRedFlagWarnings()) {
      logger.redflag(logWarningsHelper(warning));
    }
    for (Warning warning : warnings.getUnimplementedWarnings()) {
      logger.unimplemented(logWarningsHelper(warning));
    }
    for (Warning warning : warnings.getPedanticWarnings()) {
      logger.pedantic(logWarningsHelper(warning));
    }
  }

  private static String logWarningsHelper(Warning warning) {
    return "   " + warning.getTag() + ": " + warning.getText() + "\n";
  }

  /**
   * Returns the parse tree for the given parser, logging to the given logger and using the given
   * settings to control the parse tree printing, if applicable.
   */
  public static ParserRuleContext parse(
      BatfishCombinedParser<?, ?> parser, BatfishLogger logger, GrammarSettings settings) {
    ParserRuleContext tree;
    try {
      tree = parser.parse();
    } catch (BatfishException e) {
      throw new ParserBatfishException("Parser error", e);
    }
    List<String> errors = parser.getErrors();
    int numErrors = errors.size();
    if (numErrors > 0) {
      throw new ParserBatfishException("Parser error(s)", errors);
    } else if (!settings.getPrintParseTree()) {
      logger.info("OK\n");
    } else {
      logger.info("OK, PRINTING PARSE TREE:\n");
      logger.info(
          ParseTreePrettyPrinter.print(tree, parser, settings.getPrintParseTreeLineNums())
              + "\n\n");
    }
    return tree;
  }

  private final Map<String, AnswererCreator> _answererCreators;

  private SnapshotId _snapshot;

  private SortedMap<BgpTableFormat, BgpTablePlugin> _bgpTablePlugins;

  private final Cache<NetworkSnapshot, SortedMap<String, Configuration>> _cachedConfigurations;

  private final Cache<NetworkSnapshot, DataPlane> _cachedDataPlanes;

  private final Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>>
      _cachedEnvironmentBgpTables;

  private final Cache<NetworkSnapshot, Map<String, VendorConfiguration>>
      _cachedVendorConfigurations;

  private SnapshotId _referenceSnapshot;

  private Set<ExternalBgpAdvertisementPlugin> _externalBgpAdvertisementPlugins;

  private IdResolver _idResolver;

  private BatfishLogger _logger;

  private Settings _settings;

  private final StorageProvider _storage;

  // this variable is used communicate with parent thread on how the job
  // finished (null if job finished successfully)
  private String _terminatingExceptionMessage;

  private Map<String, DataPlanePlugin> _dataPlanePlugins;

  private final TopologyProvider _topologyProvider;

  public Batfish(
      Settings settings,
      Cache<NetworkSnapshot, SortedMap<String, Configuration>> cachedConfigurations,
      Cache<NetworkSnapshot, DataPlane> cachedDataPlanes,
      Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>> cachedEnvironmentBgpTables,
      Cache<NetworkSnapshot, Map<String, VendorConfiguration>> cachedVendorConfigurations,
      @Nullable StorageProvider alternateStorageProvider,
      @Nullable IdResolver alternateIdResolver) {
    _settings = settings;
    _bgpTablePlugins = new TreeMap<>();
    _cachedConfigurations = cachedConfigurations;
    _cachedDataPlanes = cachedDataPlanes;
    _cachedEnvironmentBgpTables = cachedEnvironmentBgpTables;
    _cachedVendorConfigurations = cachedVendorConfigurations;
    _externalBgpAdvertisementPlugins = new TreeSet<>();
    initLocalSettings(settings);
    _logger = _settings.getLogger();
    _terminatingExceptionMessage = null;
    _answererCreators = new HashMap<>();
    _dataPlanePlugins = new HashMap<>();
    _storage =
        alternateStorageProvider != null
            ? alternateStorageProvider
            : new FileBasedStorage(_settings.getStorageBase(), _logger, this::newBatch);
    _idResolver =
        alternateIdResolver != null ? alternateIdResolver : new StorageBasedIdResolver(_storage);
    _topologyProvider = new TopologyProviderImpl(this, _storage);
    loadPlugins();
  }

  public Answer answer() {
    Question question = null;

    // return right away if we cannot parse the question successfully
    String rawQuestionStr;
    try {
      rawQuestionStr = _storage.loadQuestion(_settings.getContainer(), _settings.getQuestionName());
    } catch (Exception e) {
      Answer answer = new Answer();
      BatfishException exception = new BatfishException("Could not read question", e);
      answer.setStatus(AnswerStatus.FAILURE);
      answer.addAnswerElement(exception.getBatfishStackTrace());
      return answer;
    }
    try {
      question = Question.parseQuestion(rawQuestionStr);
    } catch (Exception e) {
      Answer answer = new Answer();
      BatfishException exception = new BatfishException("Could not parse question", e);
      answer.setStatus(AnswerStatus.FAILURE);
      answer.addAnswerElement(exception.getBatfishStackTrace());
      return answer;
    }

    LOGGER.info("Answering question {}", question.getClass().getSimpleName());
    if (_settings.getDifferential()) {
      question.setDifferential(true);
    }
    boolean dp = question.getDataPlane();
    boolean diff = question.getDifferential();
    _settings.setDiffQuestion(diff);

    // Ensures configurations are parsed and ready
    loadConfigurations(getSnapshot());
    if (diff) {
      loadConfigurations(getReferenceSnapshot());
    }

    prepareToAnswerQuestions(diff, dp);

    AnswerElement answerElement = null;
    BatfishException exception = null;
    long startTime = System.nanoTime();
    try {
      if (question.getDifferential()) {
        answerElement =
            Answerer.create(question, this).answerDiff(getSnapshot(), getReferenceSnapshot());
      } else {
        answerElement = Answerer.create(question, this).answer(getSnapshot());
      }
    } catch (Exception e) {
      exception =
          new BatfishException(
              String.format("Failed to answer question %s", question.getClass().getSimpleName()),
              e);
    }
    Duration answerTime = Duration.ofNanos(System.nanoTime() - startTime);

    Answer answer = new Answer();
    answer.setQuestion(question);

    if (exception == null) {
      LOGGER.info(
          "Question {} answered successfully in {}",
          question.getClass().getSimpleName(),
          answerTime);
      // success
      answer.setStatus(AnswerStatus.SUCCESS);
      answer.addAnswerElement(answerElement);
    } else {
      LOGGER.warn(
          "Question {} execution failed in {}",
          question.getClass().getSimpleName(),
          answerTime,
          exception);
      // failure
      answer.setStatus(AnswerStatus.FAILURE);
      answer.addAnswerElement(exception.getBatfishStackTrace());
    }
    return answer;
  }

  private static void computeAggregatedInterfaceBandwidths(Map<String, Interface> interfaces) {
    // Set bandwidths for aggregate interfaces
    interfaces.values().stream()
        .filter(iface -> iface.getInterfaceType() == InterfaceType.AGGREGATED)
        .forEach(
            iface -> {
              /* If interface has dependencies, bandwidth should be sum of their bandwidths. */
              if (!iface.getDependencies().isEmpty()) {
                iface.setBandwidth(
                    iface.getDependencies().stream()
                        .map(dependency -> interfaces.get(dependency.getInterfaceName()))
                        .filter(Objects::nonNull)
                        .filter(Interface::getActive)
                        .map(Interface::getBandwidth)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .sum());
              } else {
                /* Bandwidth should be sum of bandwidth of channel-group members. */
                iface.setBandwidth(
                    iface.getChannelGroupMembers().stream()
                        .mapToDouble(ifaceName -> interfaces.get(ifaceName).getBandwidth())
                        .sum());
              }
            });
    // Now that aggregate interfaces have bandwidths, set bandwidths for aggregate child interfaces
    interfaces.values().stream()
        .filter(iface -> iface.getInterfaceType() == InterfaceType.AGGREGATE_CHILD)
        .forEach(
            iface -> {
              /*
              Bandwidth for aggregate child interfaces (e.g. units) should be inherited from parent.
              */
              double bandwidth =
                  iface.getDependencies().stream()
                      .filter(d -> d.getType() == DependencyType.BIND)
                      .findFirst()
                      .map(Dependency::getInterfaceName)
                      .map(interfaces::get)
                      .map(Interface::getBandwidth)
                      .orElse(0.0);
              iface.setBandwidth(bandwidth);
            });
  }

  private static void computeRedundantInterfaceBandwidths(Map<String, Interface> interfaces) {
    // Set bandwidths for redundant interfaces
    interfaces.values().stream()
        .filter(iface -> iface.getInterfaceType() == InterfaceType.REDUNDANT)
        .forEach(
            iface -> {
              /* If interface has dependencies, bandwidth should be bandwidth of any active dependency. */
              iface.setBandwidth(
                  iface.getDependencies().stream()
                      .map(dependency -> interfaces.get(dependency.getInterfaceName()))
                      .filter(Objects::nonNull)
                      .filter(Interface::getActive)
                      .map(Interface::getBandwidth)
                      .filter(Objects::nonNull)
                      .mapToDouble(Double::doubleValue)
                      .min()
                      .orElse(0.0));
            });
    // Now that redundant interfaces have bandwidths, set bandwidths for redundant child interfaces
    interfaces.values().stream()
        .filter(iface -> iface.getInterfaceType() == InterfaceType.REDUNDANT_CHILD)
        .forEach(
            iface -> {
              /*
              Bandwidth for redundant child interfaces (e.g. units) should be inherited from parent.
              */
              double bandwidth =
                  iface.getDependencies().stream()
                      .filter(d -> d.getType() == DependencyType.BIND)
                      .findFirst()
                      .map(Dependency::getInterfaceName)
                      .map(interfaces::get)
                      .map(Interface::getBandwidth)
                      .orElse(0.0);
              iface.setBandwidth(bandwidth);
            });
  }

  public static Warnings buildWarnings(Settings settings) {
    return Warnings.forLogger(settings.getLogger());
  }

  private static final NetworkSnapshot DUMMY_SNAPSHOT =
      new NetworkSnapshot(
          new NetworkId("__BATFISH_DUMMY_NETWORK"), new SnapshotId("__BATFISH_DUMMY_SNAPSHOT"));
  private static final DataPlane DUMMY_DATAPLANE =
      new DataPlane() {
        @Override
        public Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Table<String, String, Set<Bgpv4Route>> getBgpBackupRoutes() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Map<String, Fib>> getFibs() {
          throw new UnsupportedOperationException();
        }

        @Override
        public ForwardingAnalysis getForwardingAnalysis() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Table<String, String, FinalMainRib> getRibs() {
          throw new UnsupportedOperationException();
        }

        @Override
        public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
            getPrefixTracingInfoSummary() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Table<String, String, Set<Layer3Vni>> getLayer3Vnis() {
          throw new UnsupportedOperationException();
        }
      };

  @Override
  public DataPlaneAnswerElement computeDataPlane(NetworkSnapshot snapshot) {
    LOGGER.info("Starting data plane computation");
    // If already present, invalidate a dataplane for this snapshot.
    // (unlikely, only when devs force recomputation)
    _cachedDataPlanes.invalidate(snapshot);

    // Reserve space for the new dataplane in the in-memory cache by inserting and invalidating a
    // dummy value.
    _cachedDataPlanes.put(DUMMY_SNAPSHOT, DUMMY_DATAPLANE);
    _cachedDataPlanes.invalidate(DUMMY_SNAPSHOT);

    ComputeDataPlaneResult result = getDataPlanePlugin().computeDataPlane(snapshot);
    DataPlaneAnswerElement answerElement = result._answerElement;
    DataPlane dataplane = result._dataPlane;
    TopologyContainer topologyContainer = result._topologies;
    result = null; // let it be garbage collected.

    saveDataPlane(snapshot, dataplane, topologyContainer);
    LOGGER.info("Finished data plane computation successfully");
    return answerElement;
  }

  /* Write the dataplane to disk and cache, and write the answer element to disk.
   */
  private void saveDataPlane(
      NetworkSnapshot snapshot, DataPlane dataplane, TopologyContainer topologies) {
    _cachedDataPlanes.put(snapshot, dataplane);

    _logger.resetTimer();
    newBatch("Writing data plane to disk", 0);
    try {
      LOGGER.info("Storing DataPlane");
      _storage.storeDataPlane(dataplane, snapshot);
      LOGGER.info("Storing BGP Topology");
      _storage.storeBgpTopology(topologies.getBgpTopology(), snapshot);
      LOGGER.info("Storing EIGRP Topology");
      _storage.storeEigrpTopology(topologies.getEigrpTopology(), snapshot);
      LOGGER.info("Storing L3 Adjacencies");
      _storage.storeL3Adjacencies(topologies.getL3Adjacencies(), snapshot);
      LOGGER.info("Storing Layer3 Topology");
      _storage.storeLayer3Topology(topologies.getLayer3Topology(), snapshot);
      LOGGER.info("Storing OSPF Topology");
      _storage.storeOspfTopology(topologies.getOspfTopology(), snapshot);
      LOGGER.info("Storing VxLAN Topology");
      _storage.storeVxlanTopology(topologies.getVxlanTopology(), snapshot);
    } catch (IOException e) {
      throw new BatfishException("Failed to save data plane", e);
    }
    _logger.printElapsedTime();
  }

  private Map<String, Configuration> convertConfigurations(
      Map<String, VendorConfiguration> vendorConfigurations,
      ConversionContext conversionContext,
      SnapshotRuntimeData runtimeData,
      ConvertConfigurationAnswerElement answerElement) {
    _logger.info("\n*** CONVERTING VENDOR CONFIGURATIONS TO INDEPENDENT FORMAT ***\n");
    _logger.resetTimer();
    Map<String, Configuration> configurations = new TreeMap<>();
    List<ConvertConfigurationJob> jobs = new ArrayList<>();
    for (Entry<String, VendorConfiguration> config : vendorConfigurations.entrySet()) {
      VendorConfiguration vc = config.getValue();
      ConvertConfigurationJob job =
          new ConvertConfigurationJob(
              _settings, conversionContext, runtimeData, vc, config.getKey());
      jobs.add(job);
    }
    BatfishJobExecutor.runJobsInExecutor(
        _settings,
        _logger,
        jobs,
        configurations,
        answerElement,
        _settings.getHaltOnConvertError(),
        "Convert configurations to vendor-independent format");
    _logger.printElapsedTime();
    return configurations;
  }

  @Override
  public boolean debugFlagEnabled(String flag) {
    return _settings.debugFlagEnabled(flag);
  }

  @Override
  public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot snapshot) {
    return computeLocationInfo(
        getTopologyProvider().getInitialIpOwners(snapshot), loadConfigurations(snapshot));
  }

  private void disableUnusableVlanInterfaces(Map<String, Configuration> configurations) {
    for (Configuration c : configurations.values()) {
      String hostname = c.getHostname();

      Map<Integer, Interface> vlanInterfaces = new HashMap<>();
      Map<Integer, Integer> vlanMemberCounts = new HashMap<>();
      Set<Interface> nonVlanInterfaces = new HashSet<>();
      // Populate vlanInterface and nonVlanInterfaces, and initialize
      // vlanMemberCounts:
      for (Interface iface : c.getActiveInterfaces().values()) {
        Integer vlanNumber = iface.getVlan();
        if (iface.getInterfaceType() == InterfaceType.VLAN && vlanNumber != null) {
          vlanInterfaces.put(vlanNumber, iface);
          vlanMemberCounts.put(vlanNumber, 0);
        } else {
          nonVlanInterfaces.add(iface);
        }
      }
      // Update vlanMemberCounts:
      for (Interface iface : nonVlanInterfaces) {
        IntegerSpace.Builder vlans = IntegerSpace.builder();
        if (iface.getSwitchportMode() == SwitchportMode.TRUNK) { // vlan trunked interface
          IntegerSpace allowed = iface.getAllowedVlans();
          if (!allowed.isEmpty()) {
            // Explicit list of allowed VLANs
            vlans.including(allowed);
          } else {
            // No explicit list, so all VLANs are allowed.
            vlanInterfaces.keySet().forEach(vlans::including);
          }
          // Add the native VLAN as well.
          Integer nativeVlan = iface.getNativeVlan();
          if (nativeVlan != null) {
            vlans.including(nativeVlan);
          }
        } else if (iface.getSwitchportMode() == SwitchportMode.ACCESS) { // access mode ACCESS
          Integer accessVlan = iface.getAccessVlan();
          if (accessVlan != null) {
            vlans.including(accessVlan);
          }
          // Any other Switch Port mode is unsupported
        } else if (iface.getSwitchportMode() != SwitchportMode.NONE) {
          _logger.warnf(
              "WARNING: Unsupported switch port mode %s, assuming no VLANs allowed: \"%s:%s\"\n",
              iface.getSwitchportMode(), hostname, iface.getName());
        }

        vlans.build().stream()
            .forEach(
                vlanId -> vlanMemberCounts.compute(vlanId, (k, v) -> (v == null) ? 1 : (v + 1)));
      }
      // Disable all "normal" vlan interfaces with zero member counts:
      IntegerSpace normalVlanRange = c.getNormalVlanRange();
      for (Map.Entry<Integer, Integer> entry : vlanMemberCounts.entrySet()) {
        if (entry.getValue() == 0) {
          int vlanNumber = entry.getKey();
          if (normalVlanRange.contains(vlanNumber)) {
            Interface iface = vlanInterfaces.get(vlanNumber);
            if ((iface != null) && iface.getAutoState()) {
              _logger.warnf(
                  "Disabling unusable vlan interface because no switch port is assigned to it: %s",
                  NodeInterfacePair.of(iface));
              iface.deactivate(AUTOSTATE_FAILURE);
            }
          }
        }
      }
    }
  }

  /** Returns a map of hostname to VI {@link Configuration} */
  public Map<String, Configuration> getConfigurations(
      Map<String, VendorConfiguration> vendorConfigurations,
      ConversionContext conversionContext,
      SnapshotRuntimeData runtimeData,
      ConvertConfigurationAnswerElement answerElement) {
    Map<String, Configuration> configurations =
        convertConfigurations(vendorConfigurations, conversionContext, runtimeData, answerElement);

    identifyDeviceTypes(configurations.values());
    return configurations;
  }

  @Override
  public NetworkId getContainerName() {
    return _settings.getContainer();
  }

  @Override
  public DataPlanePlugin getDataPlanePlugin() {
    DataPlanePlugin plugin = _dataPlanePlugins.get(_settings.getDataPlaneEngineName());
    if (plugin == null) {
      throw new BatfishException(
          String.format(
              "Dataplane engine %s is unavailable or unsupported",
              _settings.getDataPlaneEngineName()));
    }
    return plugin;
  }

  private SortedMap<String, BgpAdvertisementsByVrf> getEnvironmentBgpTables(
      NetworkSnapshot snapshot, ParseEnvironmentBgpTablesAnswerElement answerElement) {
    _logger.info("\n*** READING Environment BGP Tables ***\n");
    SortedMap<String, String> inputData;
    try (Stream<String> keys = _storage.listInputEnvironmentBgpTableKeys(snapshot)) {
      inputData = readAllInputObjects(keys, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    SortedMap<String, BgpAdvertisementsByVrf> bgpTables =
        parseEnvironmentBgpTables(snapshot, inputData, answerElement);
    return bgpTables;
  }

  @Override
  public BatfishLogger getLogger() {
    return _logger;
  }

  /**
   * Gets the {@link NodeRolesData} for the testrig
   *
   * @return The {@link NodeRolesData} object.
   */
  @Override
  public NodeRolesData getNodeRolesData() {
    try {
      NetworkId networkId = _settings.getContainer();
      Optional<NodeRolesId> networkNodeRolesIdOpt = _idResolver.getNetworkNodeRolesId(networkId);
      if (!networkNodeRolesIdOpt.isPresent()) {
        return null;
      }
      return BatfishObjectMapper.mapper()
          .readValue(
              _storage.loadNodeRoles(networkId, networkNodeRolesIdOpt.get()), NodeRolesData.class);
    } catch (IOException e) {
      _logger.errorf("Could not read roles data: %s", e);
      return null;
    }
  }

  /**
   * Gets the {@link NodeRoleDimension} object given dimension name. If {@code dimension} is null,
   * returns the default dimension.
   *
   * @param dimension The dimension name
   * @return An {@link Optional} that has the requested NodeRoleDimension or empty otherwise.
   */
  @Override
  public Optional<NodeRoleDimension> getNodeRoleDimension(@Nullable String dimension) {
    NodeRolesData nodeRolesData = getNodeRolesData();
    return nodeRolesData.nodeRoleDimensionFor(dimension);
  }

  /** Gets the {@link ReferenceLibrary} for the network */
  @Override
  public @Nullable ReferenceLibrary getReferenceLibraryData() {
    try {
      return _storage
          .loadReferenceLibrary(_settings.getContainer())
          .orElse(new ReferenceLibrary(null));
    } catch (IOException e) {
      _logger.errorf(
          "Could not read reference library data for network %s: %s", _settings.getContainer(), e);
      return null;
    }
  }

  public Settings getSettings() {
    return _settings;
  }

  @Override
  public ImmutableConfiguration getSettingsConfiguration() {
    return _settings.getImmutableConfiguration();
  }

  @Override
  public NetworkSnapshot getSnapshot() {
    return new NetworkSnapshot(_settings.getContainer(), _snapshot);
  }

  @Override
  public NetworkSnapshot getReferenceSnapshot() {
    return new NetworkSnapshot(_settings.getContainer(), _referenceSnapshot);
  }

  @Override
  public String getTaskId() {
    return _settings.getTaskId();
  }

  public String getTerminatingExceptionMessage() {
    return _terminatingExceptionMessage;
  }

  @Override
  public @Nonnull TopologyProvider getTopologyProvider() {
    return _topologyProvider;
  }

  @Override
  public PluginClientType getType() {
    return PluginClientType.BATFISH;
  }

  @Override
  public InitInfoAnswerElement initInfo(
      NetworkSnapshot snapshot, boolean summary, boolean verboseError) {
    LOGGER.info("Getting snapshot initialization info");
    ParseVendorConfigurationAnswerElement parseAnswer =
        loadParseVendorConfigurationAnswerElement(snapshot);
    InitInfoAnswerElement answerElement = mergeParseAnswer(summary, verboseError, parseAnswer);
    ConvertConfigurationAnswerElement convertAnswer =
        loadConvertConfigurationAnswerElementOrReparse(snapshot);
    mergeConvertAnswer(summary, verboseError, convertAnswer, answerElement);
    _logger.info(answerElement.toString());
    return answerElement;
  }

  @Override
  public InitInfoAnswerElement initInfoBgpAdvertisements(
      NetworkSnapshot snapshot, boolean summary, boolean verboseError) {
    ParseEnvironmentBgpTablesAnswerElement parseAnswer =
        loadParseEnvironmentBgpTablesAnswerElement(snapshot);
    InitInfoAnswerElement answerElement = mergeParseAnswer(summary, verboseError, parseAnswer);
    _logger.info(answerElement.toString());
    return answerElement;
  }

  private void prepareToAnswerQuestions(NetworkSnapshot snapshot, boolean dp) {
    try {
      if (!_storage.hasParseEnvironmentBgpTablesAnswerElement(snapshot)) {
        computeEnvironmentBgpTables(snapshot);
      }
      if (dp && _cachedDataPlanes.getIfPresent(snapshot) == null) {
        if (!_storage.hasDataPlane(snapshot)) {
          computeDataPlane(snapshot);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void prepareToAnswerQuestions(boolean diff, boolean dp) {
    prepareToAnswerQuestions(getSnapshot(), dp);
    if (diff) {
      prepareToAnswerQuestions(getReferenceSnapshot(), dp);
    }
  }

  @Override
  public Optional<SortedMap<String, Configuration>> getProcessedConfigurations(
      NetworkSnapshot snapshot) {
    return loadConfigurations(snapshot, false);
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
    return loadConfigurations(snapshot, true).get();
  }

  private Optional<SortedMap<String, Configuration>> loadConfigurations(
      NetworkSnapshot snapshot, boolean parseIfNeeded) {
    _logger.debugf("Loading configurations for %s\n", snapshot);
    // Do we already have configurations in the cache?
    SortedMap<String, Configuration> configurations = _cachedConfigurations.getIfPresent(snapshot);
    if (configurations != null) {
      return Optional.of(configurations);
    }
    _logger.debugf("Loading configurations for %s, cache miss", snapshot);

    // Next, see if we have an up-to-date configurations on disk.
    configurations = _storage.loadConfigurations(snapshot.getNetwork(), snapshot.getSnapshot());
    if (configurations == null && !parseIfNeeded) {
      return Optional.empty();
    }

    if (configurations != null) {
      _logger.debugf("Loaded configurations for %s off disk", snapshot);
    } else {
      // Otherwise, we have to parse the configurations. Fall back to old, hacky code.
      configurations = actuallyParseConfigurations(snapshot);
    }

    // Apply things like blacklist and aggregations before installing in the cache.
    postProcessSnapshot(snapshot, configurations);
    _cachedConfigurations.put(snapshot, configurations);

    return Optional.of(configurations);
  }

  private @Nonnull SortedMap<String, Configuration> actuallyParseConfigurations(
      NetworkSnapshot snapshot) {
    _logger.infof("Repairing configurations for testrig %s", snapshot.getSnapshot());
    repairConfigurations(snapshot);
    SortedMap<String, Configuration> configurations =
        _storage.loadConfigurations(snapshot.getNetwork(), snapshot.getSnapshot());
    verify(
        configurations != null,
        "Configurations should not be null when loaded immediately after repair.");
    assert configurations != null;
    return configurations;
  }

  @Override
  public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse(
      NetworkSnapshot snapshot) {
    ConvertConfigurationAnswerElement ccae =
        _storage.loadConvertConfigurationAnswerElement(
            snapshot.getNetwork(), snapshot.getSnapshot());
    if (ccae != null) {
      return ccae;
    }

    repairConfigurations(snapshot);
    ccae =
        _storage.loadConvertConfigurationAnswerElement(
            snapshot.getNetwork(), snapshot.getSnapshot());
    if (ccae != null) {
      return ccae;
    } else {
      throw new BatfishException(
          "Version error repairing configurations for convert configuration answer element");
    }
  }

  @Override
  public DataPlane loadDataPlane(NetworkSnapshot snapshot) {
    try {
      return _cachedDataPlanes.get(
          snapshot,
          () -> {
            LOGGER.info("Data plane cache miss on snapshot {}", snapshot);
            long start = System.currentTimeMillis();
            newBatch("Loading data plane from disk", 0);
            DataPlane dp = _storage.loadDataPlane(snapshot);
            LOGGER.info(
                "Loading data plane for snapshot {} took {}ms",
                snapshot,
                System.currentTimeMillis() - start);
            return dp;
          });
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public SortedMap<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(
      NetworkSnapshot snapshot) {
    SortedMap<String, BgpAdvertisementsByVrf> environmentBgpTables =
        _cachedEnvironmentBgpTables.get(snapshot);
    if (environmentBgpTables == null) {
      loadParseEnvironmentBgpTablesAnswerElement(snapshot);
      try {
        environmentBgpTables =
            ImmutableSortedMap.copyOf(_storage.loadEnvironmentBgpTables(snapshot));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      _cachedEnvironmentBgpTables.put(snapshot, environmentBgpTables);
    }
    return environmentBgpTables;
  }

  public ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
      NetworkSnapshot snapshot) {
    return loadParseEnvironmentBgpTablesAnswerElement(snapshot, true);
  }

  private ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
      NetworkSnapshot snapshot, boolean firstAttempt) {
    try {
      if (!_storage.hasParseEnvironmentBgpTablesAnswerElement(snapshot)) {
        repairEnvironmentBgpTables(snapshot);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    try {
      return _storage.loadParseEnvironmentBgpTablesAnswerElement(snapshot);
    } catch (Exception e) {
      /* Do nothing, this is expected on serialization or other errors. */
      _logger.warn("Unable to load prior parse data");
    }

    if (firstAttempt) {
      repairEnvironmentBgpTables(snapshot);
      return loadParseEnvironmentBgpTablesAnswerElement(snapshot, false);
    } else {
      throw new BatfishException(
          "Version error repairing environment BGP tables for parse environment BGP tables "
              + "answer element");
    }
  }

  @Override
  public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
      NetworkSnapshot snapshot) {
    return loadParseVendorConfigurationAnswerElement(snapshot, true);
  }

  private ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
      NetworkSnapshot snapshot, boolean firstAttempt) {
    try {
      if (_storage.hasParseVendorConfigurationAnswerElement(snapshot)) {
        try {
          return _storage.loadParseVendorConfigurationAnswerElement(snapshot);
        } catch (Exception e) {
          /* Do nothing, this is expected on serialization or other errors. */
          _logger.warn("Unable to load prior parse data");
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    if (firstAttempt) {
      repairVendorConfigurations(snapshot);
      return loadParseVendorConfigurationAnswerElement(snapshot, false);
    } else {
      throw new BatfishException(
          "Version error repairing vendor configurations for parse configuration answer element");
    }
  }

  @Override
  public Map<String, VendorConfiguration> loadVendorConfigurations(NetworkSnapshot snapshot) {
    _logger.debugf("Loading vendor configurations for %s\n", snapshot);
    // Do we already have configurations in the cache?
    Map<String, VendorConfiguration> vendorConfigurations =
        _cachedVendorConfigurations.getIfPresent(snapshot);
    if (vendorConfigurations == null) {
      _logger.debugf("Loading vendor configurations for %s, cache miss", snapshot);
      loadParseVendorConfigurationAnswerElement(snapshot);
      try {
        vendorConfigurations = _storage.loadVendorConfigurations(snapshot);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      _cachedVendorConfigurations.put(snapshot, vendorConfigurations);
    }
    return vendorConfigurations;
  }

  private void mergeConvertAnswer(
      boolean summary,
      boolean verboseError,
      ConvertConfigurationAnswerElement convertAnswer,
      InitInfoAnswerElement answerElement) {
    mergeInitStepAnswer(answerElement, convertAnswer, summary, verboseError);
    convertAnswer.getConvertStatus().entrySet().stream()
        .filter(s -> s.getValue() == ConvertStatus.FAILED)
        .forEach(s -> answerElement.getParseStatus().put(s.getKey(), ParseStatus.FAILED));
  }

  private void mergeInitStepAnswer(
      InitInfoAnswerElement initInfoAnswerElement,
      InitStepAnswerElement initStepAnswerElement,
      boolean summary,
      boolean verboseError) {
    if (!summary) {
      if (verboseError) {
        SortedMap<String, List<BatfishStackTrace>> errors = initInfoAnswerElement.getErrors();
        initStepAnswerElement
            .getErrors()
            .forEach(
                (hostname, initStepErrors) ->
                    errors.computeIfAbsent(hostname, k -> new ArrayList<>()).add(initStepErrors));
      }
      SortedMap<String, Warnings> warnings = initInfoAnswerElement.getWarnings();
      initStepAnswerElement
          .getWarnings()
          .forEach(
              (hostname, initStepWarnings) -> {
                Warnings combined =
                    warnings.computeIfAbsent(hostname, h -> buildWarnings(_settings));
                combined.getParseWarnings().addAll(initStepWarnings.getParseWarnings());
                combined.getPedanticWarnings().addAll(initStepWarnings.getPedanticWarnings());
                combined.getRedFlagWarnings().addAll(initStepWarnings.getRedFlagWarnings());
                combined
                    .getUnimplementedWarnings()
                    .addAll(initStepWarnings.getUnimplementedWarnings());
              });
    }
  }

  private InitInfoAnswerElement mergeParseAnswer(
      boolean summary, boolean verboseError, ParseAnswerElement parseAnswer) {
    InitInfoAnswerElement answerElement = new InitInfoAnswerElement();
    mergeInitStepAnswer(answerElement, parseAnswer, summary, verboseError);
    answerElement.setParseStatus(parseAnswer.getParseStatus());
    answerElement.setParseTrees(parseAnswer.getParseTrees());
    return answerElement;
  }

  @Override
  public AtomicInteger newBatch(String description, int jobs) {
    return BatchManager.get().newBatch(_settings, description, jobs);
  }

  private void outputAnswer(Answer answer) {
    outputAnswer(answer, /* log */ false);
  }

  void outputAnswerWithLog(Answer answer) {
    outputAnswer(answer, /* log */ true);
  }

  private void outputAnswer(Answer answer, boolean writeLog) {
    try {
      // Write answer to work json log if caller requested.
      // Summarize that answer if all of the following are true:
      // - answering a question
      // - question successful
      // - client did not request full successful answers
      String answerString = BatfishObjectMapper.writeString(answer);
      boolean summarizeWorkJsonLogAnswer =
          writeLog
              && _settings.getQuestionName() != null
              && !_settings.getAlwaysIncludeAnswerInWorkJsonLog()
              && answer.getStatus() == AnswerStatus.SUCCESS;
      String workJsonLogAnswerString;
      if (summarizeWorkJsonLogAnswer) {
        Answer summaryAnswer = new Answer();
        summaryAnswer.setQuestion(answer.getQuestion());
        summaryAnswer.setStatus(answer.getStatus());
        summaryAnswer.setSummary(answer.getSummary());
        // do not include answer elements
        workJsonLogAnswerString = BatfishObjectMapper.writeString(summaryAnswer);
      } else {
        workJsonLogAnswerString = answerString;
      }
      _logger.debug(answerString);
      writeJsonAnswerWithLog(answerString, workJsonLogAnswerString, writeLog);
    } catch (Exception e) {
      BatfishException be = new BatfishException("Error in sending answer", e);
      try {
        Answer failureAnswer = Answer.failureAnswer(e.toString(), answer.getQuestion());
        failureAnswer.addAnswerElement(be.getBatfishStackTrace());
        String answerString = BatfishObjectMapper.writeString(failureAnswer);
        _logger.error(answerString);
        // write "answer" to work json log if caller requested
        writeJsonAnswerWithLog(answerString, answerString, writeLog);
      } catch (Exception e1) {
        _logger.errorf(
            "Could not serialize failure answer. %s", Throwables.getStackTraceAsString(e1));
      }
      throw be;
    }
  }

  void outputAnswerMetadata(Answer answer) throws IOException {
    QuestionId questionId = _settings.getQuestionName();
    if (questionId == null) {
      return;
    }
    SnapshotId referenceSnapshot = _settings.getDiffQuestion() ? _referenceSnapshot : null;
    NetworkId networkId = _settings.getContainer();
    NodeRolesId networkNodeRolesId =
        _idResolver
            .getNetworkNodeRolesId(networkId)
            .orElse(NodeRolesId.DEFAULT_NETWORK_NODE_ROLES_ID);
    AnswerId baseAnswerId =
        _idResolver.getAnswerId(
            networkId, _snapshot, questionId, networkNodeRolesId, referenceSnapshot);

    _storage.storeAnswerMetadata(
        networkId,
        _snapshot,
        AnswerMetadataUtil.computeAnswerMetadata(answer, _logger),
        baseAnswerId);
  }

  /** Parse AWS configurations for a single account (possibly with multiple regions) */
  @VisibleForTesting
  public static @Nonnull AwsConfiguration parseAwsConfigurations(
      Map<String, String> configurationData, ParseVendorConfigurationAnswerElement pvcae) {
    AwsConfiguration config = new AwsConfiguration();
    for (Entry<String, String> configFile : configurationData.entrySet()) {
      // Using path for convenience for now to handle separators and key hierarchcially gracefully
      Path path = Paths.get(configFile.getKey());

      // Find the place in the path where "aws_configs" starts
      int awsRootIndex = 0;
      for (Path value : path) {
        if (value.toString().equals(BfConsts.RELPATH_AWS_CONFIGS_DIR)) {
          break;
        }
        awsRootIndex++;
      }
      int pathLength = path.getNameCount();
      String regionName;
      String accountName;
      if (pathLength == 2) {
        // Currently happens for tests, but probably shouldn't be allowed
        regionName = AwsConfiguration.DEFAULT_REGION_NAME;
        accountName = AwsConfiguration.DEFAULT_ACCOUNT_NAME;
      } else if (pathLength == 3) {
        // If we are processing old-style packaging, just put everything in to one "default"
        // account.
        regionName = path.getName(pathLength - 2).toString(); // parent dir name
        accountName = AwsConfiguration.DEFAULT_ACCOUNT_NAME;
      } else if (pathLength > 3) {
        regionName = path.getName(pathLength - 2).toString(); // parent dir name
        accountName = path.getName(pathLength - 3).toString(); // account dir name
      } else {
        pvcae.addRedFlagWarning(
            BfConsts.RELPATH_AWS_CONFIGS_FILE,
            new Warning(String.format("Unexpected AWS configuration path:  %s", path), "AWS"));
        continue;
      }
      String fileName = path.subpath(awsRootIndex, pathLength).toString();
      pvcae.getFileMap().put(BfConsts.RELPATH_AWS_CONFIGS_FILE, fileName);

      try {
        JsonNode json = BatfishObjectMapper.mapper().readTree(configFile.getValue());
        config.addConfigElement(regionName, json, fileName, pvcae, accountName);
      } catch (IOException e) {
        pvcae.addRedFlagWarning(
            BfConsts.RELPATH_AWS_CONFIGS_FILE,
            new Warning(String.format("Unexpected content in AWS file %s", fileName), "AWS"));
      }
    }
    return config;
  }

  /** Parse Azure configurations for a single account (possibly with multiple regions) */
  @VisibleForTesting
  public static @Nonnull AzureConfiguration parseAzureConfigurations(
          Map<String, String> configurationData, ParseVendorConfigurationAnswerElement pvcae) {
    AzureConfiguration config = new AzureConfiguration();
    for (Entry<String, String> configFile : configurationData.entrySet()) {
      // Using path for convenience for now to handle separators and key hierarchcially gracefully
      Path path = Paths.get(configFile.getKey());

      // Find the place in the path where "azure_configs" starts
      int azureRootIndex = 0;
      for (Path value : path) {
        if (value.toString().equals(BfConsts.RELPATH_AZURE_CONFIGS_DIR)) {
          break;
        }
        azureRootIndex++;
      }
      int pathLength = path.getNameCount();

      String fileName = path.subpath(azureRootIndex, pathLength).toString();
      pvcae.getFileMap().put(BfConsts.RELPATH_AZURE_CONFIGS_DIR, fileName);

      try {
        JsonNode json = BatfishObjectMapper.mapper().readTree(configFile.getValue());
        config.addConfigElement(json);
      } catch (IOException e) {
        pvcae.addRedFlagWarning(
                BfConsts.RELPATH_AWS_CONFIGS_FILE,
                new Warning(String.format("Unexpected content in AWS file %s", fileName), "AWS"));
      }
    }
    return config;
  }

  private SortedMap<String, BgpAdvertisementsByVrf> parseEnvironmentBgpTables(
      NetworkSnapshot snapshot,
      SortedMap<String, String> inputData,
      ParseEnvironmentBgpTablesAnswerElement answerElement) {
    _logger.info("\n*** PARSING ENVIRONMENT BGP TABLES ***\n");
    _logger.resetTimer();
    SortedMap<String, BgpAdvertisementsByVrf> bgpTables = new TreeMap<>();
    List<ParseEnvironmentBgpTableJob> jobs = new ArrayList<>();
    SortedMap<String, Configuration> configurations = loadConfigurations(snapshot);
    for (Entry<String, String> bgpObject : inputData.entrySet()) {
      String currentKey = bgpObject.getKey();
      String objectText = bgpObject.getValue();
      String hostname = Paths.get(currentKey).getFileName().toString(); //
      String optionalSuffix = ".bgp";
      if (hostname.endsWith(optionalSuffix)) {
        hostname = hostname.substring(0, hostname.length() - optionalSuffix.length());
      }
      if (!configurations.containsKey(hostname)) {
        continue;
      }
      Warnings warnings = buildWarnings(_settings);
      ParseEnvironmentBgpTableJob job =
          new ParseEnvironmentBgpTableJob(
              _settings, snapshot, objectText, hostname, currentKey, warnings, _bgpTablePlugins);
      jobs.add(job);
    }
    BatfishJobExecutor.runJobsInExecutor(
        _settings,
        _logger,
        jobs,
        bgpTables,
        answerElement,
        _settings.getHaltOnParseError(),
        "Parse environment BGP tables");
    _logger.printElapsedTime();
    return bgpTables;
  }

  /**
   * Returns a {@link ParseVendorConfigurationJob}.
   *
   * <p>{@code expectedFormat} specifies the type of files expected in the {@code keyedFileText}
   * map, or is set to {@link ConfigurationFormat#UNKNOWN} to trigger format detection.
   */
  private ParseVendorConfigurationJob makeParseVendorConfigurationJob(
      NetworkSnapshot snapshot,
      Map<String, String> keyedFileText,
      ConfigurationFormat expectedFormat) {
    return new ParseVendorConfigurationJob(
        _settings,
        snapshot,
        keyedFileText,
        Warnings.Settings.fromLogger(_settings.getLogger()),
        expectedFormat,
        HashMultimap.create());
  }

  /**
   * Parses the given configuration files and returns a map keyed by hostname representing the
   * {@link VendorConfiguration vendor-specific configurations}.
   *
   * <p>{@code keyedConfigurationText} is a map from filename to its content, and each entry
   * represent a single file parsing job.
   *
   * <p>{@code expectedFormat} specifies the type of files expected in the {@code keyedFileText}
   * map, or is set to {@link ConfigurationFormat#UNKNOWN} to trigger format detection.
   */
  private SortedMap<String, VendorConfiguration> parseVendorConfigurations(
      NetworkSnapshot snapshot,
      Map<String, String> keyedConfigurationText,
      ParseVendorConfigurationAnswerElement answerElement,
      ConfigurationFormat expectedFormat) {
    List<Map<String, String>> jobList =
        keyedConfigurationText.entrySet().stream()
            .map(e -> ImmutableMap.of(e.getKey(), e.getValue()))
            .collect(ImmutableList.toImmutableList());
    return parseVendorConfigurations(snapshot, jobList, answerElement, expectedFormat);
  }

  /**
   * Parses the given configuration files and returns a map keyed by hostname representing the
   * {@link VendorConfiguration vendor-specific configurations}.
   *
   * <p>{@code keyedConfigurationTexts} is a list of (possibly multi-file) parsing jobs, where each
   * job is described by its corresponding filename to content map.
   *
   * <p>{@code expectedFormat} specifies the type of files expected in the {@code keyedFileTexts}
   * map, or is set to {@link ConfigurationFormat#UNKNOWN} to trigger format detection.
   */
  private SortedMap<String, VendorConfiguration> parseVendorConfigurations(
      NetworkSnapshot snapshot,
      List<Map<String, String>> keyedConfigurationTexts,
      ParseVendorConfigurationAnswerElement answerElement,
      ConfigurationFormat expectedFormat) {
    _logger.info("\n*** PARSING VENDOR CONFIGURATION FILES ***\n");
    _logger.resetTimer();
    SortedMap<String, VendorConfiguration> vendorConfigurations = new TreeMap<>();
    List<ParseVendorConfigurationJob> jobs =
        keyedConfigurationTexts.stream()
            .map(fileMap -> makeParseVendorConfigurationJob(snapshot, fileMap, expectedFormat))
            .collect(
                Collectors.toList()); // should not be immutable because the job executor shuffles
    BatfishJobExecutor.runJobsInExecutor(
        _settings,
        _logger,
        jobs,
        vendorConfigurations,
        answerElement,
        _settings.getHaltOnParseError(),
        "Parse configurations");
    _logger.printElapsedTime();
    return vendorConfigurations;
  }

  private void populateChannelGroupMembers(
      Map<String, Interface> interfaces, String ifaceName, Interface iface) {
    String portChannelName = iface.getChannelGroup();
    if (portChannelName == null) {
      return;
    }
    Interface portChannel = interfaces.get(portChannelName);
    if (portChannel == null) {
      return;
    }
    portChannel.setChannelGroupMembers(
        ImmutableSortedSet.<String>naturalOrder()
            .addAll(portChannel.getChannelGroupMembers())
            .add(ifaceName)
            .build());
  }

  private void postProcessAggregatedInterfaces(Map<String, Configuration> configurations) {
    configurations
        .values()
        .forEach(c -> postProcessAggregatedInterfacesHelper(c.getAllInterfaces()));
  }

  private void postProcessAggregatedInterfacesHelper(Map<String, Interface> interfaces) {
    /* Populate aggregated interfaces with members referring to them. */
    interfaces.forEach(
        (ifaceName, iface) -> populateChannelGroupMembers(interfaces, ifaceName, iface));

    /* Compute bandwidth for aggregated interfaces. */
    computeAggregatedInterfaceBandwidths(interfaces);
  }

  private void postProcessRedundantInterfaces(Map<String, Configuration> configurations) {
    configurations
        .values()
        .forEach(
            c ->
                c.getVrfs()
                    .values()
                    .forEach(
                        v ->
                            postProcessRedundantInterfacesHelper(c.getAllInterfaces(v.getName()))));
  }

  private void postProcessRedundantInterfacesHelper(Map<String, Interface> interfaces) {
    /* Compute bandwidth for redundnant interfaces. */
    computeRedundantInterfaceBandwidths(interfaces);
  }

  private void identifyDeviceTypes(Collection<Configuration> configurations) {
    for (Configuration c : configurations) {
      if (c.getDeviceType() != null) {
        continue;
      }
      // Set device type to host iff the configuration format is HOST
      if (c.getConfigurationFormat() == ConfigurationFormat.HOST) {
        c.setDeviceType(DeviceType.HOST);
      } else if (c.getVrfs().values().stream()
          .anyMatch(
              vrf ->
                  vrf.getBgpProcess() != null
                      || !vrf.getEigrpProcesses().isEmpty()
                      || vrf.getIsisProcess() != null
                      || !vrf.getOspfProcesses().isEmpty()
                      || vrf.getRipProcess() != null)) {
        c.setDeviceType(DeviceType.ROUTER);
      } else {
        // If device was not a host or router, call it a switch
        c.setDeviceType(DeviceType.SWITCH);
      }
    }
  }

  @VisibleForTesting
  static void postProcessInterfaceDependencies(
      Map<String, Configuration> configurations, Layer1Topologies layer1Topologies) {
    getInterfacesToDeactivate(configurations, layer1Topologies)
        .forEach(
            (iface, inactiveReason) ->
                configurations
                    .get(iface.getHostname())
                    .getAllInterfaces()
                    .get(iface.getInterface())
                    .deactivate(inactiveReason));
  }

  private void postProcessEigrpCosts(Map<String, Configuration> configurations) {
    configurations.values().stream()
        .flatMap(c -> c.getAllInterfaces().values().stream())
        .filter(
            iface ->
                iface.getEigrp() != null
                    && (iface.getInterfaceType() == InterfaceType.AGGREGATED
                        || iface.getInterfaceType() == InterfaceType.AGGREGATE_CHILD))
        .forEach(
            iface -> {
              EigrpMetricValues metricValues = iface.getEigrp().getMetric().getValues();
              if (metricValues.getBandwidth() == null) {
                // only set bandwidth if it's not explicitly configured for EIGRP
                Double bw = iface.getBandwidth();
                assert bw != null; // all bandwidths should be finalized at this point
                metricValues.setBandwidth(bw.longValue() / 1000); // convert to kbps
              }
            });
  }

  private void postProcessOspfCosts(Map<String, Configuration> configurations) {
    configurations
        .values()
        .forEach(
            c ->
                c.getVrfs()
                    .values()
                    .forEach(
                        vrf -> {
                          // Compute OSPF interface costs where they are missing
                          vrf.getOspfProcesses().values().forEach(p -> p.initInterfaceCosts(c));
                        }));
  }

  @Override
  public Set<BgpAdvertisement> loadExternalBgpAnnouncements(
      NetworkSnapshot snapshot, Map<String, Configuration> configurations) {
    Set<BgpAdvertisement> advertSet = new LinkedHashSet<>();
    for (ExternalBgpAdvertisementPlugin plugin : _externalBgpAdvertisementPlugins) {
      Set<BgpAdvertisement> currentAdvertisements = plugin.loadExternalBgpAdvertisements(snapshot);
      advertSet.addAll(currentAdvertisements);
    }
    return advertSet;
  }

  /**
   * Builds the {@link Trace}s for a {@link Set} of {@link Flow}s.
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow}s to {@link List} of {@link Trace}s
   */
  @Override
  public SortedMap<Flow, List<Trace>> buildFlows(
      NetworkSnapshot snapshot, Set<Flow> flows, boolean ignoreFilters) {
    return getTracerouteEngine(snapshot).computeTraces(flows, ignoreFilters);
  }

  @Override
  public TracerouteEngine getTracerouteEngine(NetworkSnapshot snapshot) {
    return new TracerouteEngineImpl(
        loadDataPlane(snapshot),
        _topologyProvider.getLayer3Topology(snapshot),
        loadConfigurations(snapshot));
  }

  /** Function that processes an interface blacklist across all configurations */
  private static void processInterfaceBlacklist(
      Set<NodeInterfacePair> interfaceBlacklist, NetworkConfigurations configurations) {
    interfaceBlacklist.stream()
        .map(iface -> configurations.getInterface(iface.getHostname(), iface.getInterface()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(Interface::hasLineStatus)
        .forEach(Interface::blacklist);
  }

  @VisibleForTesting
  static void processManagementInterfaces(Map<String, Configuration> configurations) {
    configurations
        .values()
        .forEach(
            configuration -> {
              for (Interface iface : configuration.getAllInterfaces().values()) {
                if (iface.getActive()
                    && (MANAGEMENT_INTERFACES.matcher(iface.getName()).find()
                        || MANAGEMENT_VRFS.matcher(iface.getVrfName()).find())) {
                  // Intentionally avoid touching line status, since we really just want to ensure
                  // no L2+.
                  iface.deactivate(IGNORE_MGMT);
                }
              }
            });
  }

  @Override
  public @Nullable String readExternalBgpAnnouncementsFile(NetworkSnapshot snapshot) {
    try {
      return _storage.loadExternalBgpAnnouncementsFile(snapshot).orElse(null);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Read Iptable Files for each host in the keyset of {@code hostConfigurations}, and store the
   * contents in {@code iptablesData}. Each task fails if the Iptables file specified by host does
   * not exist.
   *
   * @throws BatfishException if there is a failed task and either {@link
   *     Settings#getExitOnFirstError()} or {@link Settings#getHaltOnParseError()} is set.
   */
  void readIptablesFiles(
      NetworkSnapshot snapshot,
      SortedMap<String, VendorConfiguration> hostConfigurations,
      SortedMap<String, String> iptablesData,
      ParseVendorConfigurationAnswerElement answerElement) {
    List<BatfishException> failureCauses = new ArrayList<>();
    for (VendorConfiguration vc : hostConfigurations.values()) {
      HostConfiguration hostConfig = (HostConfiguration) vc;
      String iptablesFile = hostConfig.getIptablesFile();
      if (iptablesFile == null) {
        continue;
      }

      // ensure that the iptables file is not taking us outside of the
      // testrig
      try {
        if (!_storage.hasSnapshotInputObject(iptablesFile, snapshot)) {
          String failureMessage =
              String.format(
                  "Iptables file %s for host %s is not contained within the snapshot",
                  hostConfig.getIptablesFile(), hostConfig.getHostname());
          BatfishException bfc;
          if (answerElement.getErrors().containsKey(hostConfig.getHostname())) {
            bfc =
                new BatfishException(
                    failureMessage,
                    answerElement.getErrors().get(hostConfig.getHostname()).getException());
            answerElement.getErrors().put(hostConfig.getHostname(), bfc.getBatfishStackTrace());
            answerElement
                .getErrorDetails()
                .put(
                    hostConfig.getHostname(),
                    new ErrorDetails(Throwables.getStackTraceAsString(bfc)));
          } else {
            bfc = new BatfishException(failureMessage);
            if (_settings.getExitOnFirstError()) {
              throw bfc;
            } else {
              failureCauses.add(bfc);
              answerElement.getErrors().put(hostConfig.getHostname(), bfc.getBatfishStackTrace());
              answerElement.getParseStatus().put(hostConfig.getIptablesFile(), ParseStatus.FAILED);
              answerElement
                  .getErrorDetails()
                  .put(
                      hostConfig.getHostname(),
                      new ErrorDetails(Throwables.getStackTraceAsString(bfc)));
            }
          }
        } else {
          try (InputStream inputStream =
              _storage.loadSnapshotInputObject(
                  snapshot.getNetwork(), snapshot.getSnapshot(), iptablesFile)) {
            iptablesData.put(iptablesFile, decodeStreamAndAppendNewline(inputStream));
          }
        }
      } catch (IOException e) {
        throw new BatfishException("Could not get canonical path", e);
      }
    }

    if (_settings.getHaltOnParseError() && !failureCauses.isEmpty()) {
      BatfishException e =
          new BatfishException(
              "Fatal exception due to at least one Iptables file is"
                  + " not contained within the testrig");
      failureCauses.forEach(e::addSuppressed);
      throw e;
    }
  }

  @Override
  public void registerAnswerer(
      String questionName,
      String questionClassName,
      BiFunction<Question, IBatfish, Answerer> answererCreator) {
    AnswererCreator oldAnswererCreator =
        _answererCreators.putIfAbsent(
            questionName, new AnswererCreator(questionClassName, answererCreator));
    if (oldAnswererCreator != null) {
      // Error: questionName collision.
      String oldQuestionClassName = _answererCreators.get(questionClassName).getQuestionClassName();
      throw new IllegalArgumentException(
          String.format(
              "questionName %s already exists.\n"
                  + "  old questionClassName: %s\n"
                  + "  new questionClassName: %s",
              questionName, oldQuestionClassName, questionClassName));
    }
  }

  @Override
  public void registerBgpTablePlugin(BgpTableFormat format, BgpTablePlugin bgpTablePlugin) {
    _bgpTablePlugins.put(format, bgpTablePlugin);
  }

  @Override
  public void registerExternalBgpAdvertisementPlugin(
      ExternalBgpAdvertisementPlugin externalBgpAdvertisementPlugin) {
    _externalBgpAdvertisementPlugins.add(externalBgpAdvertisementPlugin);
  }

  private void repairConfigurations(NetworkSnapshot snapshot) {
    // Needed to ensure vendor configs are written
    loadParseVendorConfigurationAnswerElement(snapshot);
    serializeIndependentConfigs(snapshot);
  }

  /**
   * Post-process the configuration in the current snapshot. Post-processing includes:
   *
   * <ul>
   *   <li>Applying node and interface blacklists.
   *   <li>Process interface dependencies and deactivate interfaces that cannot be up
   * </ul>
   */
  private void updateBlacklistedAndInactiveConfigs(
      NetworkSnapshot snapshot, Map<String, Configuration> configurations) {
    NetworkConfigurations nc = NetworkConfigurations.of(configurations);
    NetworkId networkId = snapshot.getNetwork();
    SnapshotId snapshotId = snapshot.getSnapshot();

    // Start of blacklisting. Nothing should touch line status until after blacklisting is done.
    SortedSet<String> blacklistedNodes = _storage.loadNodeBlacklist(networkId, snapshotId);
    if (blacklistedNodes != null) {
      processNodeBlacklist(blacklistedNodes, nc);
    }
    // If interface blacklist was provided, it was converted to runtime data file by WorkMgr
    SnapshotRuntimeData runtimeData = _storage.loadRuntimeData(networkId, snapshotId);
    if (runtimeData != null) {
      processInterfaceBlacklist(runtimeData.getBlacklistedInterfaces(), nc);
    }
    // End of blacklisting.

    // Currently NOP
    // TODO: decide whether/when to correlate adminUp and lineUp
    disconnectAdminDownInterfaces(configurations.values());

    if (_settings.ignoreManagementInterfaces()) {
      processManagementInterfaces(configurations);
    }

    /* compute a Layer1Topologies directly instead of getting it from _topologyProvider, since doing so would try to
     * load configurations (which we're in the middle of loading now). We don't yet have the "real" configs, so the
     * adjacencies we build now may not match what we get later.
     */
    Layer1Topology synthesizedLayer1Topology;
    try {
      synthesizedLayer1Topology =
          _storage.loadSynthesizedLayer1Topology(snapshot).orElse(Layer1Topology.EMPTY);
    } catch (IOException e) {
      synthesizedLayer1Topology = Layer1Topology.EMPTY;
    }
    Layer1Topologies l1Topologies =
        Layer1TopologiesFactory.create(
            _topologyProvider.getRawLayer1PhysicalTopology(snapshot).orElse(Layer1Topology.EMPTY),
            synthesizedLayer1Topology,
            configurations);

    postProcessInterfaceDependencies(configurations, l1Topologies);

    // We do not process the edge blacklist here. Instead, we rely on these edges being explicitly
    // deleted from the Topology (aka list of edges) that is used along with configurations in
    // answering questions.

    // TODO: take this out once dependencies are *the* definitive way to disable interfaces
    disableUnusableVlanInterfaces(configurations);
  }

  @VisibleForTesting
  static void processNodeBlacklist(Set<String> blacklistedNodes, NetworkConfigurations nc) {
    blacklistedNodes.stream()
        // Get all valid/present node configs
        .map(nc::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        // All interfaces in each config
        .flatMap(c -> c.getAllInterfaces().values().stream())
        // Disable the interface
        .forEach(Interface::nodeDown);
  }

  private static void disconnectAdminDownInterfaces(Collection<Configuration> configurations) {
    for (Configuration c : configurations) {
      if (!c.getDisconnectAdminDownInterfaces()) {
        continue;
      }
      for (Interface i : c.getAllInterfaces().values()) {
        if (!i.getAdminUp() && i.hasLineStatus() && i.getLineUp()) {
          // TODO: decide whether/when to correlate adminUp and lineUp
          // i.disconnect();
          assert Boolean.TRUE;
        }
      }
    }
  }

  /**
   * Ensures that the current configurations for the current snapshot are correct by performing some
   * post-processing on the vendor-independent datamodel. Among other things, this includes:
   *
   * <ul>
   *   <li>Invalidating cached configs if the in-memory copy has been changed by question
   *       processing.
   *   <li>Re-loading configurations from disk, including re-parsing if the configs were parsed on a
   *       previous version of Batfish.
   *   <li>Ensuring that blacklists are honored.
   * </ul>
   */
  private void postProcessSnapshot(
      NetworkSnapshot snapshot, Map<String, Configuration> configurations) {
    updateBlacklistedAndInactiveConfigs(snapshot, configurations);
    postProcessAggregatedInterfaces(configurations);
    postProcessRedundantInterfaces(configurations);
    NetworkConfigurations nc = NetworkConfigurations.of(configurations);
    OspfTopologyUtils.initNeighborConfigs(nc);
    postProcessOspfCosts(configurations);
    postProcessEigrpCosts(configurations); // must be after postProcessAggregatedInterfaces
    EigrpTopologyUtils.initNeighborConfigs(nc);
  }

  private void computeAndStoreCompletionMetadata(
      NetworkSnapshot snapshot, Map<String, Configuration> configurations) {
    try {
      _storage.storeCompletionMetadata(
          computeCompletionMetadata(snapshot, configurations),
          _settings.getContainer(),
          snapshot.getSnapshot());
    } catch (IOException e) {
      _logger.errorf("Error storing CompletionMetadata: %s", e);
    }
  }

  private CompletionMetadata computeCompletionMetadata(
      NetworkSnapshot snapshot, Map<String, Configuration> configurations) {
    return new CompletionMetadata(
        getFilterNames(configurations),
        getInterfaces(configurations),
        getIps(configurations, getTopologyProvider().getInitialIpOwners(snapshot)),
        getLocationCompletionMetadata(getLocationInfo(snapshot), configurations),
        getMlagIds(configurations),
        getNodes(configurations),
        getPrefixes(configurations),
        getRoutingPolicyNames(configurations),
        getStructureNames(configurations),
        getVrfs(configurations),
        getZones(configurations));
  }

  @MustBeClosed
  @Override
  public @Nonnull InputStream getNetworkObject(NetworkId networkId, String key) throws IOException {
    return _storage.loadNetworkObject(networkId, key);
  }

  @MustBeClosed
  @Override
  public @Nonnull InputStream getSnapshotObject(
      NetworkId networkId, SnapshotId snapshotId, String key) throws IOException {
    return _storage.loadSnapshotObject(networkId, snapshotId, key);
  }

  @Override
  public void putSnapshotObject(
      NetworkId networkId, SnapshotId snapshotId, String key, InputStream stream)
      throws IOException {
    _storage.storeSnapshotObject(stream, networkId, snapshotId, key);
  }

  @MustBeClosed
  @Override
  public @Nonnull InputStream getSnapshotInputObject(NetworkSnapshot snapshot, String key)
      throws IOException {
    return _storage.loadSnapshotInputObject(snapshot.getNetwork(), snapshot.getSnapshot(), key);
  }

  private void repairEnvironmentBgpTables(NetworkSnapshot snapshot) {
    try {
      _storage.deleteParseEnvironmentBgpTablesAnswerElement(snapshot);
      _storage.deleteEnvironmentBgpTables(snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    computeEnvironmentBgpTables(snapshot);
  }

  private void repairVendorConfigurations(NetworkSnapshot snapshot) {
    try {
      _storage.deleteParseVendorConfigurationAnswerElement(snapshot);
      _storage.deleteVendorConfigurations(snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    serializeVendorConfigs(snapshot);
  }

  public Answer run(NetworkSnapshot snapshot) {
    newBatch("Begin job", 0);
    boolean action = false;
    Answer answer = new Answer();

    if (_settings.getSerializeVendor()) {
      answer.append(serializeVendorConfigs(snapshot));
      action = true;
    }

    if (_settings.getSerializeIndependent()) {
      answer.append(serializeIndependentConfigs(snapshot));
      // TODO: compute topology on initialization in cleaner way
      initializeTopology(snapshot);
      updateSnapshotNodeRoles(snapshot);
      action = true;
    }

    if (_settings.getInitInfo()) {
      InitInfoAnswerElement initInfoAnswerElement = initInfo(snapshot, true, false);
      // In this context we can remove parse trees because they will be returned in preceding answer
      // element. Note that parse trees are not removed when asking initInfo as its own question.
      initInfoAnswerElement.setParseTrees(Collections.emptySortedMap());
      answer.addAnswerElement(initInfoAnswerElement);
      action = true;
    }

    if (_settings.getAnswer()) {
      answer.append(answer());
      action = true;
    }

    if (_settings.getDataPlane()) {
      answer.addAnswerElement(computeDataPlane(snapshot));
      action = true;
    }

    if (!action) {
      throw new CleanBatfishException("No task performed! Run with -help flag to see usage\n");
    }
    LOGGER.info("Completed work.");
    return answer;
  }

  /** Initialize topologies, commit {raw, raw pojo, pruned} layer-3 topologies to storage. */
  @VisibleForTesting
  void initializeTopology(NetworkSnapshot networkSnapshot) {
    Map<String, Configuration> configurations = loadConfigurations(networkSnapshot);
    LOGGER.info("Initializing topology");
    Topology rawLayer3Topology = _topologyProvider.getRawLayer3Topology(networkSnapshot);
    checkTopology(configurations, rawLayer3Topology);
    org.batfish.datamodel.pojo.Topology pojoTopology =
        org.batfish.datamodel.pojo.Topology.create(
            _settings.getSnapshotName(), configurations, rawLayer3Topology);
    try {
      _storage.storePojoTopology(
          pojoTopology, networkSnapshot.getNetwork(), networkSnapshot.getSnapshot());
    } catch (IOException e) {
      throw new BatfishException("Could not serialize layer-3 POJO topology", e);
    }
    Topology layer3Topology = _topologyProvider.getInitialLayer3Topology(networkSnapshot);
    try {
      _storage.storeInitialTopology(
          layer3Topology, networkSnapshot.getNetwork(), networkSnapshot.getSnapshot());
    } catch (IOException e) {
      throw new BatfishException("Could not serialize layer-3 topology", e);
    }
  }

  /** Returns {@code true} iff AWS configuration data is found. */
  private boolean serializeAwsConfigs(
      NetworkSnapshot snapshot, ParseVendorConfigurationAnswerElement pvcae) {
    _logger.info("\n*** READING AWS CONFIGS ***\n");

    AwsConfiguration awsConfiguration;
    boolean found = false;
    try {
      Map<String, String> awsConfigurationData;
      // Try to parse all accounts as one vendor configuration
      try (Stream<String> keys = _storage.listInputAwsMultiAccountKeys(snapshot)) {
        awsConfigurationData = readAllInputObjects(keys, snapshot);
      }
      if (awsConfigurationData.isEmpty()) {
        // No multi-account data, so try to parse as single-account
        try (Stream<String> keys = _storage.listInputAwsSingleAccountKeys(snapshot)) {
          awsConfigurationData = readAllInputObjects(keys, snapshot);
        }
      }
      found = !awsConfigurationData.isEmpty();
      awsConfiguration = parseAwsConfigurations(awsConfigurationData, pvcae);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    if (!found) {
      // nothing to serialize.
      return found;
    }

    _logger.info("\n*** SERIALIZING AWS CONFIGURATION STRUCTURES ***\n");
    _logger.resetTimer();
    _logger.debugf("Serializing AWS");
    try {
      _storage.storeVendorConfigurations(
          ImmutableMap.of(BfConsts.RELPATH_AWS_CONFIGS_FILE, awsConfiguration), snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    _logger.debug("OK\n");
    _logger.printElapsedTime();
    return found;
  }

  /** Returns {@code true} iff Azure configuration data is found. */
  private boolean serializeAzureConfigs(
          NetworkSnapshot snapshot, ParseVendorConfigurationAnswerElement pvcae) {
    _logger.info("\n*** READING AZURE CONFIGS ***\n");


    AzureConfiguration azureConfiguration;
    boolean found = false;
    try {
      Map<String, String> azureConfigurationData;
      // Try to parse all accounts as one vendor configuration
      try (Stream<String> keys = _storage.listInputAzureSingleAccountKeys(snapshot)) {
        azureConfigurationData = readAllInputObjects(keys, snapshot);
      }
      found = !azureConfigurationData.isEmpty();
      azureConfiguration = parseAzureConfigurations(azureConfigurationData, pvcae);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    if (!found) {
      // nothing to serialize.
      return found;
    }

    _logger.info("\n*** SERIALIZING AZURE CONFIGURATION STRUCTURES ***\n");
    _logger.resetTimer();
    _logger.debugf("Serializing Azure");
    try {
      _storage.storeVendorConfigurations(
              ImmutableMap.of(BfConsts.RELPATH_AZURE_CONFIGS_DIR, azureConfiguration), snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    _logger.debug("OK\n");
    _logger.printElapsedTime();
    return found;
  }

  private void serializeConversionContext(
      NetworkSnapshot snapshot, ParseVendorConfigurationAnswerElement pvcae) {
    ConversionContext conversionContext = new ConversionContext();

    // Serialize Checkpoint management servers if present
    try {
      List<String> actualKeys;
      // Try to parse all accounts as one vendor configuration
      try (Stream<String> keys = _storage.listInputCheckpointManagementKeys(snapshot)) {
        actualKeys = keys.collect(Collectors.toList());
      }
      if (!actualKeys.isEmpty()) {
        LOGGER.info("\n*** READING CHECKPOINT MANAGEMENT CONFIGS ***\n");
        Map<String, String> cpServerData = readAllInputObjects(actualKeys.stream(), snapshot);
        CheckpointManagementConfiguration cpMgmtConfig =
            parseCheckpointManagementData(cpServerData, pvcae);
        conversionContext.setCheckpointManagementConfiguration(cpMgmtConfig);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    if (!conversionContext.isEmpty()) {
      LOGGER.info("\n*** SERIALIZING CONVERSION CONTEXT ***\n");
      try {
        _storage.storeConversionContext(conversionContext, snapshot);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  private Answer computeEnvironmentBgpTables(NetworkSnapshot snapshot) {
    Answer answer = new Answer();
    ParseEnvironmentBgpTablesAnswerElement answerElement =
        new ParseEnvironmentBgpTablesAnswerElement();
    answerElement.setVersion(BatfishVersion.getVersionStatic());
    answer.addAnswerElement(answerElement);
    SortedMap<String, BgpAdvertisementsByVrf> bgpTables =
        getEnvironmentBgpTables(snapshot, answerElement);
    try {
      _storage.storeEnvironmentBgpTables(bgpTables, snapshot);
      _storage.storeParseEnvironmentBgpTablesAnswerElement(answerElement, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return answer;
  }

  private SortedMap<String, VendorConfiguration> serializeHostConfigs(
      NetworkSnapshot snapshot, ParseVendorConfigurationAnswerElement answerElement) {
    _logger.info("\n*** READING HOST CONFIGS ***\n");
    Map<String, String> keyedHostText;
    try (Stream<String> keys = _storage.listInputHostConfigurationsKeys(snapshot)) {
      keyedHostText = readAllInputObjects(keys, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // read the host files
    SortedMap<String, VendorConfiguration> allHostConfigurations =
        parseVendorConfigurations(snapshot, keyedHostText, answerElement, ConfigurationFormat.HOST);
    if (allHostConfigurations == null) {
      throw new BatfishException("Exiting due to parser errors");
    }
    _logger.infof(
        "Testrig:%s in container:%s has total number of host configs:%d",
        snapshot.getSnapshot(), snapshot.getNetwork(), allHostConfigurations.size());

    // split into hostConfigurations and overlayConfigurations
    SortedMap<String, VendorConfiguration> overlayConfigurations =
        allHostConfigurations.entrySet().stream()
            .filter(e -> ((HostConfiguration) e.getValue()).getOverlay())
            .collect(toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v1, TreeMap::new));
    SortedMap<String, VendorConfiguration> nonOverlayHostConfigurations =
        allHostConfigurations.entrySet().stream()
            .filter(e -> !((HostConfiguration) e.getValue()).getOverlay())
            .collect(toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v1, TreeMap::new));

    // read and associate iptables files for specified hosts
    SortedMap<String, String> keyedIptablesText = new TreeMap<>();
    readIptablesFiles(snapshot, allHostConfigurations, keyedIptablesText, answerElement);

    SortedMap<String, VendorConfiguration> iptablesConfigurations =
        parseVendorConfigurations(
            snapshot, keyedIptablesText, answerElement, ConfigurationFormat.IPTABLES);
    for (VendorConfiguration vc : allHostConfigurations.values()) {
      HostConfiguration hostConfig = (HostConfiguration) vc;
      if (hostConfig.getIptablesFile() != null) {
        String iptablesKeyFromHost = hostConfig.getIptablesFile();
        if (iptablesConfigurations.containsKey(iptablesKeyFromHost)) {
          hostConfig.setIptablesVendorConfig(
              (IptablesVendorConfiguration) iptablesConfigurations.get(iptablesKeyFromHost));
        }
      }
    }

    // now, serialize
    _logger.info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
    _logger.resetTimer();

    try {
      _storage.storeVendorConfigurations(nonOverlayHostConfigurations, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    // serialize warnings
    try {
      _storage.storeParseVendorConfigurationAnswerElement(answerElement, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    _logger.printElapsedTime();
    return overlayConfigurations;
  }

  private Answer serializeIndependentConfigs(NetworkSnapshot snapshot) {
    Answer answer = new Answer();
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    answerElement.setVersion(BatfishVersion.getVersionStatic());
    if (_settings.getVerboseParse()) {
      answer.addAnswerElement(answerElement);
    }

    ConversionContext conversionContext;
    try {
      conversionContext = _storage.loadConversionContext(snapshot);
    } catch (FileNotFoundException e) {
      // Not written when it is empty.
      conversionContext = new ConversionContext();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    SnapshotRuntimeData runtimeData =
        firstNonNull(
            _storage.loadRuntimeData(snapshot.getNetwork(), snapshot.getSnapshot()),
            EMPTY_SNAPSHOT_RUNTIME_DATA);
    Map<String, VendorConfiguration> vendorConfigs;
    Map<String, Configuration> configurations;
    LOGGER.info(
        "Converting the Vendor-Specific configurations to Vendor-Independent configurations");
    try {
      vendorConfigs = _storage.loadVendorConfigurations(snapshot);
      configurations =
          getConfigurations(vendorConfigs, conversionContext, runtimeData, answerElement);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Set<Layer1Edge> layer1Edges =
        vendorConfigs.values().stream()
            .flatMap(vc -> vc.getLayer1Edges().stream())
            .collect(Collectors.toSet());

    Warnings internetWarnings =
        answerElement
            .getWarnings()
            .computeIfAbsent(INTERNET_HOST_NAME, i -> buildWarnings(_settings));

    ModeledNodes modeledNodes =
        getInternetAndIspNodes(snapshot, configurations, vendorConfigs, internetWarnings);

    mergeInternetAndIspNodes(modeledNodes, configurations, layer1Edges, internetWarnings);

    LOGGER.info("Serializing Vendor-Independent configurations");
    try {
      _storage.storeConfigurations(
          configurations,
          answerElement,
          // we don't write anything if no Layer1 edges were produced
          // empty topologies are currently dangerous for L1 computation
          layer1Edges.isEmpty() ? null : new Layer1Topology(layer1Edges),
          snapshot.getNetwork(),
          snapshot.getSnapshot());
    } catch (IOException e) {
      throw new BatfishException("Could not store vendor independent configs to disk: %s", e);
    }

    LOGGER.info("Post-processing the Vendor-Independent devices");
    postProcessSnapshot(snapshot, configurations);

    if (_settings.getPrecomputeAutocomplete()) {
      LOGGER.info("Computing completion metadata");
      computeAndStoreCompletionMetadata(snapshot, configurations);
    }

    LOGGER.info("Completed serializing snapshot data");
    return answer;
  }

  /**
   * Merges modeled nodes into {@code configurations} and {@code layer1Edges}. Nothing is done if
   * the input configurations have a node in common with modeled nodes.
   */
  @VisibleForTesting
  static void mergeInternetAndIspNodes(
      ModeledNodes modeledNodes,
      Map<String, Configuration> configurations,
      Set<Layer1Edge> layer1Edges,
      Warnings internetWarnings) {
    Map<String, Configuration> modeledConfigs = modeledNodes.getConfigurations();
    Set<String> commonNodes = Sets.intersection(configurations.keySet(), modeledConfigs.keySet());
    if (!commonNodes.isEmpty()) {
      internetWarnings.redFlagf(
          "Cannot add internet and ISP nodes because nodes with the following names already"
              + " exist in the snapshot: %s",
          commonNodes);
      return;
    }
    configurations.putAll(modeledConfigs);
    layer1Edges.addAll(modeledNodes.getLayer1Edges());
  }

  /**
   * Creates and returns ISP and Internet nodes.
   *
   * <p>If a node named 'internet' already exists in input {@code configurations} an empty {@link
   * ModeledNodes} object is returned.
   */
  private @Nonnull ModeledNodes getInternetAndIspNodes(
      NetworkSnapshot snapshot,
      Map<String, Configuration> configurations,
      Map<String, VendorConfiguration> vendorConfigs,
      Warnings internetWarnings) {
    if (configurations.containsKey(INTERNET_HOST_NAME)) {
      internetWarnings.redFlag(
          "Cannot model internet because a node with the name 'internet' already exists");
      return new ModeledNodes();
    }

    ImmutableList.Builder<IspConfiguration> ispConfigurations = new ImmutableList.Builder<>();

    try {
      IspConfiguration ispConfiguration =
          _storage.loadIspConfiguration(snapshot.getNetwork(), snapshot.getSnapshot());
      if (ispConfiguration != null) {
        LOGGER.info("Loading Batfish ISP Configuration");
        ispConfigurations.add(ispConfiguration);
      }
    } catch (IspConfigurationException e) {
      internetWarnings.redFlag(e.getMessage());
      _logger.warnf(
          "Error loading ISP configuration for snapshot %s", Throwables.getStackTraceAsString(e));
    }

    vendorConfigs.values().stream()
        .map(VendorConfiguration::getIspConfiguration)
        .filter(Objects::nonNull)
        .forEach(ispConfigurations::add);

    return IspModelingUtils.getInternetAndIspNodes(
        configurations, ispConfigurations.build(), _logger, internetWarnings);
  }

  private void updateSnapshotNodeRoles(NetworkSnapshot snapshot) {
    // Compute new auto role data and updates existing auto data with it
    NodeRolesId snapshotNodeRolesId =
        _idResolver.getSnapshotNodeRolesId(snapshot.getNetwork(), snapshot.getSnapshot());
    Set<String> nodeNames = loadConfigurations(snapshot).keySet();
    Topology rawLayer3Topology = _topologyProvider.getRawLayer3Topology(snapshot);
    Optional<RoleMapping> autoRoles = new InferRoles(nodeNames, rawLayer3Topology).inferRoles();
    NodeRolesData.Builder snapshotNodeRoles = NodeRolesData.builder();
    try {
      if (autoRoles.isPresent()) {
        snapshotNodeRoles.setDefaultDimension(NodeRoleDimension.AUTO_DIMENSION_PRIMARY);
        snapshotNodeRoles.setRoleMappings(ImmutableList.of(autoRoles.get()));
        snapshotNodeRoles.setType(Type.AUTO);
      }
      _storage.storeNodeRoles(
          snapshot.getNetwork(), snapshotNodeRoles.build(), snapshotNodeRolesId);
    } catch (IOException e) {
      _logger.warnf("Could not update node roles: %s", e);
    }
  }

  private ParseVendorConfigurationResult getOrParse(
      ParseVendorConfigurationJob job, GrammarSettings settings) {
    // Short-circuit all cache-related code.
    if (!_settings.getParseReuse()) {
      long startTime = System.currentTimeMillis();
      ParseResult result = job.parse();
      long elapsed = System.currentTimeMillis() - startTime;
      return job.fromResult(result, elapsed);
    }

    Hasher hasher =
        Hashing.murmur3_128()
            .newHasher()
            .putString("Cached Parse Result", UTF_8)
            .putBoolean(settings.getDisableUnrecognized())
            .putInt(settings.getMaxParserContextLines())
            .putInt(settings.getMaxParserContextTokens())
            .putInt(settings.getMaxParseTreePrintLength())
            .putBoolean(settings.getPrintParseTreeLineNums())
            .putBoolean(settings.getPrintParseTree())
            .putBoolean(settings.getThrowOnLexerError())
            .putBoolean(settings.getThrowOnParserError());
    job.getFileTexts().keySet().stream()
        .sorted()
        .forEach(
            filename -> {
              hasher.putString(filename, UTF_8);
              hasher.putString(job.getFileTexts().get(filename), UTF_8);
            });
    String id = hasher.hash().toString();
    long startTime = System.currentTimeMillis();
    boolean cached = false;
    ParseResult result;
    try (InputStream in = _storage.loadNetworkBlob(getContainerName(), id)) {
      result = SerializationUtils.deserialize(in);
      // sanity-check filenames. In the extremely unlikely event of a collision, we'll lose reuse
      // for this input.
      cached = result.getFileResults().keySet().equals(job.getFileTexts().keySet());
    } catch (FileNotFoundException e) {
      result = job.parse();
    } catch (Exception e) {
      _logger.warnf(
          "Error deserializing cached parse result for %s: %s",
          job.getFileTexts().keySet(), Throwables.getStackTraceAsString(e));
      result = job.parse();
    }
    if (!cached) {
      try {
        byte[] serialized = SerializationUtils.serialize(result);
        _storage.storeNetworkBlob(new ByteArrayInputStream(serialized), getContainerName(), id);
        result = SerializationUtils.deserialize(serialized);
      } catch (Exception e) {
        _logger.warnf(
            "Error caching parse result for %s: %s",
            job.getFileTexts().keySet(), Throwables.getStackTraceAsString(e));
      }
    }
    long elapsed = System.currentTimeMillis() - startTime;
    return job.fromResult(result, elapsed);
  }

  /**
   * Parses configuration files for networking devices from the uploaded user data and produces
   * {@link VendorConfiguration vendor-specific configurations} serialized to the given output path.
   *
   * <p>Returns {@code true} iff at least one valid (node-generating) network configuration was
   * found.
   *
   * <p>This function should be named better, but it's called by the {@link
   * #serializeVendorConfigs(NetworkSnapshot)}, so leaving as-is for now.
   */
  private boolean serializeNetworkConfigs(
      NetworkSnapshot snapshot,
      ParseVendorConfigurationAnswerElement answerElement,
      SortedMap<String, VendorConfiguration> overlayHostConfigurations) {
    if (!overlayHostConfigurations.isEmpty()) {
      // Not able to cache with overlays.
      return oldSerializeNetworkConfigs(snapshot, answerElement, overlayHostConfigurations);
    }
    _logger.info("\n*** READING DEVICE CONFIGURATION FILES ***\n");

    List<ParseVendorConfigurationResult> parseResults;
    List<ParseVendorConfigurationJob> jobs = new LinkedList<>();
    // add devices in the 'configs' folder
    try (Stream<String> keys = _storage.listInputNetworkConfigurationsKeys(snapshot)) {
      readAllInputObjects(keys, snapshot).entrySet().stream()
          .map(
              entry ->
                  makeParseVendorConfigurationJob(
                      snapshot,
                      ImmutableMap.of(entry.getKey(), entry.getValue()),
                      ConfigurationFormat.UNKNOWN))
          .forEach(jobs::add);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // add devices in the sonic_configs folder
    try (Stream<String> keys = _storage.listInputSonicConfigsKeys(snapshot)) {
      Map<String, String> sonicObjects = readAllInputObjects(keys, snapshot);
      makeSonicFileGroups(sonicObjects.keySet(), answerElement).stream()
          .map(
              files ->
                  makeParseVendorConfigurationJob(
                      snapshot,
                      files.stream()
                          .collect(
                              ImmutableMap.toImmutableMap(Function.identity(), sonicObjects::get)),
                      ConfigurationFormat.SONIC))
          .forEach(jobs::add);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // Java parallel streams are not self-balancing in large networks, so shuffle the jobs.
    Collections.shuffle(jobs);

    AtomicInteger batch = newBatch("Parse network configs", jobs.size());
    LOGGER.info("Parsing {} configuration files", jobs.size());
    parseResults =
        jobs.parallelStream()
            .map(
                j -> {
                  ParseVendorConfigurationResult result = getOrParse(j, _settings);
                  int done = batch.incrementAndGet();
                  if (done % 100 == 0) {
                    LOGGER.info("Successfully parsed {}/{} configuration files", done, jobs.size());
                  }
                  return result;
                })
            .collect(ImmutableList.toImmutableList());
    LOGGER.info("Done parsing {} configuration files", jobs.size());

    if (_settings.getHaltOnParseError()
        && parseResults.stream().anyMatch(r -> r.getFailureCause() != null)) {
      BatfishException e = new BatfishException("Exiting due to parser errors");
      parseResults.stream()
          .map(ParseVendorConfigurationResult::getFailureCause)
          .filter(Objects::nonNull)
          .forEach(e::addSuppressed);
      throw e;
    }

    _logger.infof(
        "Snapshot %s in network %s has total number of network configs:%d",
        snapshot.getSnapshot(), snapshot.getNetwork(), parseResults.size());

    /* Assemble answer. */
    SortedMap<String, VendorConfiguration> vendorConfigurations = new TreeMap<>();
    parseResults.forEach(pvcr -> pvcr.applyTo(vendorConfigurations, _logger, answerElement));
    LOGGER.info("Serializing Vendor-Specific configurations");
    try {
      _logger.info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      _logger.resetTimer();
      Map<String, VendorConfiguration> output = new TreeMap<>();
      vendorConfigurations.forEach(
          (name, vc) -> {
            if (name.contains(File.separator)) {
              // iptables will get a hostname like configs/iptables-save if they
              // are not set up correctly using host files
              _logger.errorf("Cannot serialize configuration with bad hostname %s\n", name);
              answerElement.addRedFlagWarning(
                  name,
                  new Warning(
                      "Cannot serialize network config. Bad hostname " + name.replace("\\", "/"),
                      "MISCELLANEOUS"));
            } else {
              output.put(name, vc);
            }
          });

      _storage.storeVendorConfigurations(output, snapshot);
      _logger.printElapsedTime();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    // checking vendorConfigurations is a quick, common-case check before creating streams
    return !vendorConfigurations.isEmpty() || networkConfigsExist(snapshot);
  }

  /** Returns {@code true} iff at least one network configuration was found. */
  private boolean oldSerializeNetworkConfigs(
      NetworkSnapshot snapshot,
      ParseVendorConfigurationAnswerElement answerElement,
      SortedMap<String, VendorConfiguration> overlayHostConfigurations) {
    _logger.info("\n*** READING DEVICE CONFIGURATION FILES ***\n");

    Map<String, VendorConfiguration> vendorConfigurations = new HashMap<>();
    // consider what is in the configs folder
    try (Stream<String> keys = _storage.listInputNetworkConfigurationsKeys(snapshot)) {
      vendorConfigurations.putAll(
          parseVendorConfigurations(
              snapshot,
              readAllInputObjects(keys, snapshot).entrySet().stream()
                  .map(e -> ImmutableMap.of(e.getKey(), e.getValue()))
                  .collect(ImmutableList.toImmutableList()),
              answerElement,
              ConfigurationFormat.UNKNOWN));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    // consider what is in the sonic_configs folder
    try (Stream<String> keys = _storage.listInputSonicConfigsKeys(snapshot)) {
      Map<String, String> sonicObjects = readAllInputObjects(keys, snapshot);
      vendorConfigurations.putAll(
          parseVendorConfigurations(
              snapshot,
              makeSonicFileGroups(sonicObjects.keySet(), answerElement).stream()
                  .map(
                      files ->
                          files.stream()
                              .collect(
                                  ImmutableMap.toImmutableMap(
                                      Function.identity(), sonicObjects::get)))
                  .collect(ImmutableList.toImmutableList()),
              answerElement,
              ConfigurationFormat.SONIC));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    _logger.infof(
        "Snapshot %s in network %s has total number of network configs:%d",
        snapshot.getSnapshot(), snapshot.getNetwork(), vendorConfigurations.size());

    try {
      _logger.info("\n*** SERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
      _logger.resetTimer();
      Map<String, VendorConfiguration> output = new TreeMap<>();
      vendorConfigurations.forEach(
          (name, vc) -> {
            if (name.contains(File.separator)) {
              // iptables will get a hostname like configs/iptables-save if they
              // are not set up correctly using host files
              _logger.errorf("Cannot serialize configuration with hostname %s\n", name);
              answerElement.addRedFlagWarning(
                  name,
                  new Warning(
                      "Cannot serialize network config. Bad hostname " + name.replace("\\", "/"),
                      "MISCELLANEOUS"));
            } else {
              // apply overlay if it exists
              VendorConfiguration overlayConfig = overlayHostConfigurations.get(name);
              if (overlayConfig != null) {
                vc.setOverlayConfiguration(overlayConfig);
                overlayHostConfigurations.remove(name);
              }

              output.put(name, vc);
            }
          });

      // warn about unused overlays
      overlayHostConfigurations.forEach(
          (name, overlay) ->
              answerElement.getParseStatus().put(overlay.getFilename(), ParseStatus.ORPHANED));

      _storage.storeVendorConfigurations(output, snapshot);
      _logger.printElapsedTime();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    // checking vendorConfigurations is a quick, common-case check before creating streams
    return !vendorConfigurations.isEmpty() || networkConfigsExist(snapshot);
  }

  /** Returns if any network configuration files exist under configs or sonic_configs folders. */
  private boolean networkConfigsExist(NetworkSnapshot snapshot) {
    try (Stream<String> keys = _storage.listInputNetworkConfigurationsKeys(snapshot)) {
      if (keys.findAny().isPresent()) {
        return true;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    try (Stream<String> keys = _storage.listInputSonicConfigsKeys(snapshot)) {
      if (keys.findAny().isPresent()) {
        return true;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return false;
  }

  /**
   * Given a set of sonic object keys, return a collection of sets that occur in the same folder.
   *
   * <p>Set elements are file names, and the cardinality of each set must be at least 2 because
   * frr.conf and configdb.json files are mandatory.
   */
  @VisibleForTesting
  static Collection<Set<String>> makeSonicFileGroups(
      Set<String> sonicKeys, ParseVendorConfigurationAnswerElement pvcae) {
    ImmutableMultimap.Builder<String, String> dirToFilesB = ImmutableMultimap.builder();

    // Expected packaging: sonic_configs -> dir1 -> (dir2 ->)* {file1, file2, ..}

    for (String filename : sonicKeys) {
      // Using Path as a convenient way to interpret hierarchical keys for now (as for AWS)
      Path path = Paths.get(filename);
      if (path.getNameCount() < 3) {
        // file right below sonic_configs
        pvcae.addRedFlagWarning(
            filename,
            new Warning(
                "Unexpected packaging: SONiC files must be in a subdirectory under sonic_configs.",
                "sonic"));
        pvcae.getParseStatus().put(filename, ParseStatus.UNEXPECTED_PACKAGING);
        continue;
      }
      String dir = path.getParent().toString();
      dirToFilesB.put(dir, filename);
    }
    ImmutableMultimap<String, String> dirToFiles = dirToFilesB.build();
    List<Set<String>> validFileGroups = new LinkedList<>();
    for (String dir : dirToFiles.keySet()) {
      Collection<String> files = dirToFiles.get(dir);
      if (files.size() == 1) {
        String filename = files.iterator().next();
        pvcae.addRedFlagWarning(
            filename,
            new Warning(
                "Unexpected packaging: There must be at least two files in each SONiC device"
                    + " folder.",
                "sonic"));
        pvcae.getParseStatus().put(filename, ParseStatus.UNEXPECTED_PACKAGING);
        continue;
      }
      validFileGroups.add(ImmutableSet.copyOf(files));
    }
    return validFileGroups;
  }

  /**
   * Parses configuration files from the uploaded user data and produces {@link VendorConfiguration
   * vendor-specific configurations} serialized to the given output path.
   *
   * <p>This function should be named better, but it's called by the {@code -sv} argument to Batfish
   * so leaving as-is for now.
   */
  private Answer serializeVendorConfigs(NetworkSnapshot snapshot) {
    Answer answer = new Answer();
    boolean configsFound = false;
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();
    answerElement.setVersion(BatfishVersion.getVersionStatic());
    if (_settings.getVerboseParse()) {
      answer.addAnswerElement(answerElement);
    }

    // look for host configs and overlay configs in the `hosts/` subfolder of the upload.
    SortedMap<String, VendorConfiguration> overlayHostConfigurations = new TreeMap<>();
    if (hasHostConfigs(snapshot)) {
      overlayHostConfigurations.putAll(serializeHostConfigs(snapshot, answerElement));
      configsFound = true;
    }

    // look for network configs in the `configs/` subfolder of the upload.
    if (serializeNetworkConfigs(snapshot, answerElement, overlayHostConfigurations)) {
      configsFound = true;
    }

    // look for AWS VPC configs in the `aws_configs/` subfolder of the upload.
    if (serializeAwsConfigs(snapshot, answerElement)) {
      configsFound = true;
    }

    // look for Azure configs in the 'azure_configs/' subfolder of the upload
    if (serializeAzureConfigs(snapshot, answerElement)) {
      configsFound = true;
    }

    if (!configsFound) {
      throw new BatfishException("No valid configurations found in snapshot");
    }

    // serialize any context needed for conversion (this does not include any configs)
    serializeConversionContext(snapshot, answerElement);

    // serialize warnings
    try {
      _storage.storeParseVendorConfigurationAnswerElement(answerElement, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return answer;
  }

  private boolean hasHostConfigs(NetworkSnapshot snapshot) {
    try (Stream<String> keys = _storage.listInputHostConfigurationsKeys(snapshot)) {
      return keys.findAny().isPresent();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void registerDataPlanePlugin(DataPlanePlugin plugin, String name) {
    _dataPlanePlugins.put(name, plugin);
  }

  public void setTerminatingExceptionMessage(String terminatingExceptionMessage) {
    _terminatingExceptionMessage = terminatingExceptionMessage;
  }

  @Override
  public SpecifierContext specifierContext(NetworkSnapshot networkSnapshot) {
    return new SpecifierContextImpl(this, networkSnapshot);
  }

  @Override
  public BidirectionalReachabilityResult bidirectionalReachability(
      NetworkSnapshot snapshot, BDDPacket bddPacket, ReachabilityParameters parameters) {
    ResolvedReachabilityParameters params;
    try {
      params = resolveReachabilityParameters(this, parameters, snapshot);
    } catch (InvalidReachabilityParametersException e) {
      throw new BatfishException("Error resolving reachability parameters", e);
    }

    DataPlane dataPlane = loadDataPlane(snapshot);
    return new BidirectionalReachabilityAnalysis(
            bddPacket,
            loadConfigurations(snapshot),
            dataPlane.getForwardingAnalysis(),
            new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
            params.getSourceIpAssignment(),
            params.getHeaderSpace(),
            params.getForbiddenTransitNodes(),
            params.getRequiredTransitNodes(),
            params.getFinalNodes(),
            params.getActions())
        .getResult();
  }

  @Override
  public AnswerElement standard(
      NetworkSnapshot snapshot, ReachabilityParameters reachabilityParameters) {
    return bddSingleReachability(snapshot, reachabilityParameters);
  }

  public AnswerElement bddSingleReachability(
      NetworkSnapshot snapshot, ReachabilityParameters parameters) {
    ResolvedReachabilityParameters params;
    try {
      params = resolveReachabilityParameters(this, parameters, snapshot);
    } catch (InvalidReachabilityParametersException e) {
      return e.getInvalidParametersAnswer();
    }

    checkArgument(
        params.getSrcNatted() == SrcNattedConstraint.UNCONSTRAINED,
        "Requiring or forbidding Source NAT is currently unsupported");

    BDDPacket pkt = new BDDPacket();
    boolean ignoreFilters = params.getIgnoreFilters();
    BDDReachabilityAnalysisFactory bddReachabilityAnalysisFactory =
        getBddReachabilityAnalysisFactory(snapshot, pkt, ignoreFilters);

    Map<IngressLocation, BDD> reachableBDDs =
        bddReachabilityAnalysisFactory.getAllBDDs(
            params.getSourceIpAssignment(),
            params.getHeaderSpace(),
            params.getForbiddenTransitNodes(),
            params.getRequiredTransitNodes(),
            params.getFinalNodes(),
            params.getActions());

    Set<Flow> flows = constructFlows(pkt, reachableBDDs);

    return new TraceWrapperAsAnswerElement(buildFlows(snapshot, flows, ignoreFilters));
  }

  @Override
  public Set<Flow> bddLoopDetection(NetworkSnapshot snapshot) {
    BDDPacket pkt = new BDDPacket();
    // TODO add ignoreFilters parameter
    boolean ignoreFilters = false;
    BDDReachabilityAnalysisFactory bddReachabilityAnalysisFactory =
        getBddReachabilityAnalysisFactory(snapshot, pkt, ignoreFilters);
    BDDLoopDetectionAnalysis analysis =
        bddReachabilityAnalysisFactory.bddLoopDetectionAnalysis(
            getAllSourcesInferFromLocationIpSpaceAssignment(snapshot));
    Map<IngressLocation, BDD> loopBDDs = analysis.detectLoops();

    return loopBDDs.entrySet().stream()
        .map(
            entry ->
                pkt.getFlow(entry.getValue())
                    .map(
                        fb -> {
                          IngressLocation loc = entry.getKey();
                          fb.setIngressNode(loc.getNode());
                          switch (loc.getType()) {
                            case INTERFACE_LINK -> fb.setIngressInterface(loc.getInterface());
                            case VRF -> fb.setIngressVrf(loc.getVrf());
                          }
                          return fb.build();
                        }))
        .flatMap(Optional::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<Flow> bddMultipathConsistency(
      NetworkSnapshot snapshot, MultipathConsistencyParameters parameters) {
    BDDPacket pkt = new BDDPacket();
    // TODO add ignoreFilters parameter
    boolean ignoreFilters = false;
    BDDReachabilityAnalysisFactory bddReachabilityAnalysisFactory =
        getBddReachabilityAnalysisFactory(snapshot, pkt, ignoreFilters);
    IpSpaceAssignment srcIpSpaceAssignment = parameters.getSrcIpSpaceAssignment();
    Set<String> finalNodes = parameters.getFinalNodes();
    Set<FlowDisposition> failureDispositions =
        ImmutableSet.of(
            FlowDisposition.DENIED_IN,
            FlowDisposition.DENIED_OUT,
            FlowDisposition.LOOP,
            FlowDisposition.INSUFFICIENT_INFO,
            FlowDisposition.NEIGHBOR_UNREACHABLE,
            FlowDisposition.NO_ROUTE,
            FlowDisposition.NULL_ROUTED);
    Set<FlowDisposition> successDispositions =
        ImmutableSet.of(
            FlowDisposition.ACCEPTED,
            FlowDisposition.DELIVERED_TO_SUBNET,
            FlowDisposition.EXITS_NETWORK);
    Set<String> forbiddenTransitNodes = parameters.getForbiddenTransitNodes();
    Set<String> requiredTransitNodes = parameters.getRequiredTransitNodes();
    Map<IngressLocation, BDD> successBdds =
        bddReachabilityAnalysisFactory.getAllBDDs(
            srcIpSpaceAssignment,
            parameters.getHeaderSpace(),
            forbiddenTransitNodes,
            requiredTransitNodes,
            finalNodes,
            successDispositions);
    Map<IngressLocation, BDD> failureBdds =
        bddReachabilityAnalysisFactory.getAllBDDs(
            srcIpSpaceAssignment,
            parameters.getHeaderSpace(),
            forbiddenTransitNodes,
            requiredTransitNodes,
            finalNodes,
            failureDispositions);

    return ImmutableSet.copyOf(computeMultipathInconsistencies(pkt, successBdds, failureBdds));
  }

  public @Nonnull IpSpaceAssignment getAllSourcesInferFromLocationIpSpaceAssignment(
      NetworkSnapshot snapshot) {
    SpecifierContextImpl specifierContext = new SpecifierContextImpl(this, snapshot);
    Set<Location> locations =
        new UnionLocationSpecifier(
                AllInterfacesLocationSpecifier.INSTANCE,
                AllInterfaceLinksLocationSpecifier.INSTANCE)
            .resolve(specifierContext);
    return InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE.resolve(
        locations, specifierContext);
  }

  private @Nonnull BDDReachabilityAnalysisFactory getBddReachabilityAnalysisFactory(
      NetworkSnapshot snapshot, BDDPacket pkt, boolean ignoreFilters) {
    DataPlane dataPlane = loadDataPlane(snapshot);
    return new BDDReachabilityAnalysisFactory(
        pkt,
        loadConfigurations(snapshot),
        dataPlane.getForwardingAnalysis(),
        new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
        ignoreFilters,
        false);
  }

  public BDDReachabilityAnalysis getBddReachabilityAnalysis(
      NetworkSnapshot snapshot,
      BDDPacket pkt,
      IpSpaceAssignment srcIpSpaceAssignment,
      AclLineMatchExpr initialHeaderSpace,
      Set<String> forbiddenTransitNodes,
      Set<String> requiredTransitNodes,
      Set<String> finalNodes,
      Set<FlowDisposition> actions,
      boolean ignoreFilters,
      boolean useInterfaceRoots) {
    BDDReachabilityAnalysisFactory factory =
        getBddReachabilityAnalysisFactory(snapshot, pkt, ignoreFilters);
    return factory.bddReachabilityAnalysis(
        srcIpSpaceAssignment,
        initialHeaderSpace,
        forbiddenTransitNodes,
        requiredTransitNodes,
        finalNodes,
        actions,
        useInterfaceRoots);
  }

  /**
   * Return a set of flows (at most 1 per source {@link Location}) for which reachability has been
   * reduced by the change from base to delta snapshot.
   */
  @Override
  public DifferentialReachabilityResult bddDifferentialReachability(
      NetworkSnapshot snapshot,
      NetworkSnapshot reference,
      DifferentialReachabilityParameters parameters) {
    checkArgument(
        !parameters.getFlowDispositions().isEmpty(), "Must specify at least one FlowDisposition");
    BDDPacket pkt = new BDDPacket();

    AclLineMatchExpr headerSpace =
        parameters.getInvertSearch()
            ? not(parameters.getHeaderSpace())
            : parameters.getHeaderSpace();

    /*
     * TODO should we have separate parameters for base and delta?
     * E.g. suppose we add a host subnet in the delta network. This would be a source of
     * differential reachability, but we currently won't find it because it won't be in the
     * IpSpaceAssignment.
     */
    Map<IngressLocation, BDD> baseAcceptBDDs =
        getBddReachabilityAnalysisFactory(snapshot, pkt, parameters.getIgnoreFilters())
            .getAllBDDs(
                parameters.getIpSpaceAssignment(),
                headerSpace,
                parameters.getForbiddenTransitNodes(),
                parameters.getRequiredTransitNodes(),
                parameters.getFinalNodes(),
                parameters.getFlowDispositions());

    Map<IngressLocation, BDD> deltaAcceptBDDs =
        getBddReachabilityAnalysisFactory(reference, pkt, parameters.getIgnoreFilters())
            .getAllBDDs(
                parameters.getIpSpaceAssignment(),
                headerSpace,
                parameters.getForbiddenTransitNodes(),
                parameters.getRequiredTransitNodes(),
                parameters.getFinalNodes(),
                parameters.getFlowDispositions());

    Set<IngressLocation> commonSources =
        Sets.intersection(baseAcceptBDDs.keySet(), deltaAcceptBDDs.keySet());

    Set<Flow> decreasedFlows =
        getDifferentialFlows(pkt, commonSources, baseAcceptBDDs, deltaAcceptBDDs);
    Set<Flow> increasedFlows =
        getDifferentialFlows(pkt, commonSources, deltaAcceptBDDs, baseAcceptBDDs);
    return new DifferentialReachabilityResult(increasedFlows, decreasedFlows);
  }

  private static Set<Flow> getDifferentialFlows(
      BDDPacket pkt,
      Set<IngressLocation> commonSources,
      Map<IngressLocation, BDD> includeBDDs,
      Map<IngressLocation, BDD> excludeBDDs) {
    return commonSources.stream()
        .flatMap(
            source -> {
              BDD difference = includeBDDs.get(source).diff(excludeBDDs.get(source));

              if (difference.isZero()) {
                return Stream.of();
              }

              Flow.Builder flow =
                  pkt.getFlow(difference)
                      .orElseThrow(() -> new BatfishException("Error getting flow from BDD"));

              // set flow parameters
              flow.setIngressNode(source.getNode());
              switch (source.getType()) {
                case VRF -> flow.setIngressVrf(source.getVrf());
                case INTERFACE_LINK -> flow.setIngressInterface(source.getInterface());
              }
              return Stream.of(flow.build());
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  private void writeJsonAnswer(String structuredAnswerString) throws IOException {
    SnapshotId referenceSnapshot = _settings.getDiffQuestion() ? _referenceSnapshot : null;
    NetworkId networkId = _settings.getContainer();
    QuestionId questionId = _settings.getQuestionName();
    NodeRolesId networkNodeRolesId =
        _idResolver
            .getNetworkNodeRolesId(networkId)
            .orElse(NodeRolesId.DEFAULT_NETWORK_NODE_ROLES_ID);
    AnswerId baseAnswerId =
        _idResolver.getAnswerId(
            networkId, _snapshot, questionId, networkNodeRolesId, referenceSnapshot);
    _storage.storeAnswer(networkId, _snapshot, structuredAnswerString, baseAnswerId);
  }

  private void writeJsonAnswerWithLog(
      String answerOutput, String workJsonLogAnswerString, boolean writeLog) throws IOException {
    if (writeLog && _settings.getTaskId() != null) {
      _storage.storeWorkJson(
          workJsonLogAnswerString,
          _settings.getContainer(),
          _settings.getTestrig(),
          _settings.getTaskId());
    }
    // Write answer if WorkItem was answering a question
    if (_settings.getQuestionName() != null) {
      writeJsonAnswer(answerOutput);
    }
  }

  @Override
  public @Nullable Answerer createAnswerer(@Nonnull Question question) {
    AnswererCreator creator = _answererCreators.get(question.getName());
    return creator != null ? creator.create(question, this) : null;
  }

  private static final Logger LOGGER = LogManager.getLogger(Batfish.class);
}
