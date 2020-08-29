package org.batfish.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Streams.stream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.plugin.PluginConsumer.DEFAULT_HEADER_LENGTH_BYTES;
import static org.batfish.common.plugin.PluginConsumer.detectFormat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.PluginConsumer.Format;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.ZipUtility;
import org.batfish.datamodel.AnalysisMetadata;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.Id;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.vendor.VendorConfiguration;

/** A utility class that abstracts the underlying file system storage used by Batfish. */
@ParametersAreNonnullByDefault
public class FileBasedStorage implements StorageProvider {

  @VisibleForTesting static final Duration GC_SKEW_ALLOWANCE = Duration.ofMinutes(10L);
  private static final String ID_EXTENSION = ".id";
  private static final String SUFFIX_LOG_FILE = ".log";
  private static final String SUFFIX_ANSWER_JSON_FILE = ".json";
  private static final String RELPATH_COMPLETION_METADATA_FILE = "completion_metadata.json";
  private static final String RELPATH_BGP_TOPOLOGY = "bgp_topology.json";
  private static final String RELPATH_EIGRP_TOPOLOGY = "eigrp_topology.json";
  private static final String RELPATH_SYNTHESIZED_LAYER1_TOPOLOGY =
      "synthesized_layer1_topology.json";
  private static final String RELPATH_LAYER2_TOPOLOGY = "layer2_topology.json";
  private static final String RELPATH_LAYER3_TOPOLOGY = "layer3_topology.json";
  private static final String RELPATH_OSPF_TOPOLOGY = "ospf_topology.json";
  private static final String RELPATH_VXLAN_TOPOLOGY = "vxlan_topology.json";
  private static final String RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR = "indep";
  private static final String RELPATH_QUESTIONS_DIR = "questions";
  private static final String RELPATH_TESTRIG_POJO_TOPOLOGY_PATH = "testrig_pojo_topology";
  private static final String RELPATH_ORIGINAL_DIR = "original";
  private static final String RELPATH_METADATA_FILE = "metadata.json";
  private static final String RELPATH_FORK_REQUEST_FILE = "fork_request";
  private static final String RELPATH_ENV_TOPOLOGY_FILE = "env_topology";
  private static final String RELPATH_CONVERT_ANSWER_PATH = "convert_answer";
  private static final String RELPATH_CONTAINER_SETTINGS_ISSUES = "issues";
  private static final String RELPATH_CONTAINER_SETTINGS = "settings";
  private static final String RELPATH_ANSWERS_DIR = "answers";
  private static final String RELPATH_ANSWER_METADATA = "answer_metadata.json";
  private static final String RELPATH_ANSWER_JSON = "answer.json";
  private static final String RELPATH_ANALYSES_DIR = "analyses";
  private static final String RELPATH_BATFISH_CONFIGS_DIR = "batfish";
  private static final String RELPATH_ISP_CONFIG_FILE = "isp_config.json";
  private static final String RELPATH_SNAPSHOT_ZIP_FILE = "snapshot.zip";
  private static final String RELPATH_DATA_PLANE = "dp";
  private static final String RELPATH_SERIALIZED_ENVIRONMENT_BGP_TABLES = "bgp_processed";
  private static final String RELPATH_ENVIRONMENT_BGP_TABLES_ANSWER = "bgp_answer";
  private static final String RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS =
      "external_bgp_announcements.json";
  private static final String RELPATH_PARSE_ANSWER_PATH = "parse_answer";
  private static final String RELPATH_VENDOR_SPECIFIC_CONFIG_DIR = "vendor";
  private static final String RELPATH_AWS_ACCOUNTS_DIR = "accounts";
  private static final String RELPATH_SNAPSHOTS_DIR = "snapshots";
  private static final String RELPATH_OUTPUT = "output";

  private final BatfishLogger _logger;
  private final BiFunction<String, Integer, AtomicInteger> _newBatch;
  private final Path _baseDir;

  /**
   * Create a new {@link FileBasedStorage} instance that uses the given root path and job batch
   * provider function.
   */
  public FileBasedStorage(
      Path baseDir, BatfishLogger logger, BiFunction<String, Integer, AtomicInteger> newBatch) {
    _logger = logger;
    _newBatch = newBatch;
    try {
      _baseDir = baseDir.toFile().getCanonicalFile().toPath();
    } catch (IOException e) {
      throw new UncheckedIOException(
          String.format("Could not get canonical path of %s", baseDir), e);
    }
  }

  /**
   * Create a new {@link FileBasedStorage} instance that uses the given root path job and whose job
   * batch provider function is a NOP.
   */
  public FileBasedStorage(Path baseDir, BatfishLogger logger) {
    this(baseDir, logger, (a, b) -> new AtomicInteger());
  }

  /**
   * Returns the configuration files for the given testrig. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Override
  @Nullable
  public SortedMap<String, Configuration> loadConfigurations(
      NetworkId network, SnapshotId snapshot) {
    Path indepDir = getVendorIndependentConfigDir(network, snapshot);
    // If the directory that would contain these configs does not even exist, no cache exists.
    if (!Files.exists(indepDir)) {
      _logger.debugf("Unable to load configs for %s from disk: no cache directory", snapshot);
      return null;
    }

    // If the directory exists, then likely the configs exist and are useful. Still, we need to
    // confirm that they were serialized with a compatible version of Batfish first.
    if (!cachedConfigsAreCompatible(network, snapshot)) {
      _logger.debugf(
          "Unable to load configs for %s from disk: error or incompatible version", snapshot);
      return null;
    }

    _logger.info("\n*** DESERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
    Map<Path, String> namesByPath = new TreeMap<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(indepDir)) {
      for (Path serializedConfig : stream) {
        String name = serializedConfig.getFileName().toString();
        namesByPath.put(serializedConfig, name);
      }
    } catch (IOException e) {
      throw new BatfishException(
          "Error reading vendor-independent configs directory: '" + indepDir + "'", e);
    }
    try {
      return deserializeObjects(namesByPath, Configuration.class);
    } catch (BatfishException e) {
      return null;
    }
  }

  @Override
  public @Nullable ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      NetworkId network, SnapshotId snapshot) {
    Path ccaePath = getConvertAnswerPath(network, snapshot);
    if (!Files.exists(ccaePath)) {
      return null;
    }
    try {
      return deserializeObject(ccaePath, ConvertConfigurationAnswerElement.class);
    } catch (BatfishException e) {
      _logger.errorf(
          "Failed to deserialize ConvertConfigurationAnswerElement: %s",
          Throwables.getStackTraceAsString(e));
      return null;
    }
  }

  @Override
  public @Nullable SortedSet<NodeInterfacePair> loadInterfaceBlacklist(
      NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(
            BfConsts.RELPATH_INPUT,
            RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel)
            .map(p -> getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    try {
      return BatfishObjectMapper.mapper()
          .readValue(path.get().toFile(), new TypeReference<SortedSet<NodeInterfacePair>>() {});
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading interface blacklist for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    }
  }

  @VisibleForTesting
  static final String ISP_CONFIGURATION_KEY =
      String.format("%s/%s", RELPATH_BATFISH_CONFIGS_DIR, RELPATH_ISP_CONFIG_FILE);

  @Override
  public @Nullable IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot) {
    try (InputStream inputStream =
        loadSnapshotInputObject(network, snapshot, ISP_CONFIGURATION_KEY)) {
      return BatfishObjectMapper.mapper().readValue(inputStream, IspConfiguration.class);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading ISP configuration for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    }
  }

  @Override
  public @Nullable SortedSet<String> loadNodeBlacklist(NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(
            BfConsts.RELPATH_INPUT,
            RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_NODE_BLACKLIST_FILE);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_NODE_BLACKLIST_FILE);
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel)
            .map(p -> getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    try {
      return BatfishObjectMapper.mapper()
          .readValue(path.get().toFile(), new TypeReference<SortedSet<String>>() {});
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading node blacklist for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    }
  }

  @Override
  public @Nullable Layer1Topology loadLayer1Topology(NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(
            BfConsts.RELPATH_INPUT, RELPATH_BATFISH_CONFIGS_DIR, BfConsts.RELPATH_L1_TOPOLOGY_PATH);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_L1_TOPOLOGY_PATH);
    Path deprecated = Paths.get(BfConsts.RELPATH_INPUT, "testrig_layer1_topology");
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel, deprecated)
            .map(p -> getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    AtomicInteger counter = _newBatch.apply("Reading layer-1 topology", 1);
    try {
      return BatfishObjectMapper.mapper().readValue(path.get().toFile(), Layer1Topology.class);
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading layer-1 topology for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    } finally {
      counter.incrementAndGet();
    }
  }

  @Override
  @Nonnull
  public String loadWorkLog(NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    Path filePath = getWorkLogPath(network, snapshot, workId);
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(
          String.format("Could not find log file for work ID: %s", workId));
    }
    return readFileToString(filePath, UTF_8);
  }

  @Override
  @Nonnull
  public String loadWorkJson(NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    Path filePath = getWorkJsonPath(network, snapshot, workId);
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(
          String.format("Could not find work json for work ID: %s", workId));
    }
    return readFileToString(filePath, UTF_8);
  }

  @Override
  public @Nullable MajorIssueConfig loadMajorIssueConfig(
      NetworkId network, IssueSettingsId majorIssueType) {
    Path path = getMajorIssueConfigDir(network, majorIssueType);

    if (!Files.exists(path)) {
      return null;
    }

    try {
      return BatfishObjectMapper.mapper().readValue(path.toFile(), MajorIssueConfig.class);
    } catch (IOException e) {
      _logger.errorf(
          "ERROR: Could not cast read file for major issue settings with ID %s in network %s: %s",
          majorIssueType, network, Throwables.getStackTraceAsString(e));
      return null;
    }
  }

  @Override
  public void storeMajorIssueConfig(
      NetworkId network, IssueSettingsId majorIssueType, MajorIssueConfig majorIssueConfig)
      throws IOException {
    Path path = getMajorIssueConfigDir(network, majorIssueType);

    if (Files.notExists(path)) {
      Files.createDirectories(path.getParent());
    }

    writeStringToFile(
        path, BatfishObjectMapper.mapper().writeValueAsString(majorIssueConfig), UTF_8);
  }

  private @Nonnull Path getQuestionSettingsPath(
      NetworkId network, QuestionSettingsId questionSettingsId) {
    return getNetworkSettingsDir(network)
        .resolve(RELPATH_QUESTIONS_DIR)
        .resolve(String.format("%s.json", questionSettingsId.getId()));
  }

  @Override
  public @Nullable SnapshotRuntimeData loadRuntimeData(NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(
            BfConsts.RELPATH_INPUT,
            RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_RUNTIME_DATA_FILE);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_RUNTIME_DATA_FILE);
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel)
            .map(p -> getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    AtomicInteger counter = _newBatch.apply("Reading runtime data", 1);
    try {
      return BatfishObjectMapper.mapper().readValue(path.get().toFile(), SnapshotRuntimeData.class);
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading runtime data for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    } finally {
      counter.incrementAndGet();
    }
  }

  /**
   * Stores the configuration information into the given testrig. Will replace any previously-stored
   * configurations.
   */
  @Override
  public void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      @Nullable Layer1Topology synthesizedLayer1Topology,
      NetworkId network,
      SnapshotId snapshot)
      throws IOException {

    mkdirs(getSnapshotDir(network, snapshot));

    // Save the convert configuration answer element.
    Path ccaePath = getConvertAnswerPath(network, snapshot);
    mkdirs(ccaePath.getParent());
    serializeObject(convertAnswerElement, ccaePath);

    // Save the synthesized layer1 topology
    if (synthesizedLayer1Topology != null) {
      storeSynthesizedLayer1Topology(synthesizedLayer1Topology, network, snapshot);
    }

    Path outputDir = getVendorIndependentConfigDir(network, snapshot);

    String batchName =
        String.format(
            "Serializing %s vendor-independent configuration structures for snapshot %s",
            configurations.size(), snapshot);

    storeConfigurations(outputDir, batchName, configurations);
  }

  private @Nonnull Path getConvertAnswerPath(NetworkId network, SnapshotId snapshot) {
    return getSnapshotOutputDir(network, snapshot).resolve(RELPATH_CONVERT_ANSWER_PATH);
  }

  private @Nonnull Path getSynthesizedLayer1TopologyPath(NetworkId network, SnapshotId snapshot) {
    return getSnapshotOutputDir(network, snapshot).resolve(RELPATH_SYNTHESIZED_LAYER1_TOPOLOGY);
  }

  private void storeConfigurations(
      Path outputDir, String batchName, Map<String, Configuration> configurations)
      throws IOException {
    _logger.infof("\n*** %s***\n", batchName.toUpperCase());
    AtomicInteger progressCount = _newBatch.apply(batchName, configurations.size());

    // Delete any existing output, then recreate.
    deleteDirectory(outputDir);
    mkdirs(outputDir);

    configurations
        .entrySet()
        .parallelStream()
        .forEach(
            e -> {
              Path currentOutputPath = outputDir.resolve(e.getKey());
              serializeObject(e.getValue(), currentOutputPath);
              progressCount.incrementAndGet();
            });
  }

  @Override
  public void storeAnswer(String answerStr, AnswerId answerId) throws IOException {
    Path answerPath = getAnswerPath(answerId);
    mkdirs(answerPath.getParent());
    writeStringToFile(answerPath, answerStr, UTF_8);
  }

  @Override
  public void storeAnswerMetadata(AnswerMetadata answerMetadata, AnswerId answerId)
      throws IOException {
    Path answerMetadataPath = getAnswerMetadataPath(answerId);
    mkdirs(answerMetadataPath.getParent());
    writeJsonFile(answerMetadataPath, answerMetadata);
  }

  /**
   * Returns a single object of the given class deserialized from the given file. Uses the {@link
   * FileBasedStorage} default file encoding including serialization format and compression.
   */
  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer
  private <S extends Serializable> S deserializeObject(Path inputFile, Class<S> outputClass)
      throws BatfishException {
    Path sanitizedInputFile = validatePath(inputFile);
    try (Closer closer = Closer.create()) {
      FileInputStream fis = closer.register(new FileInputStream(sanitizedInputFile.toFile()));
      PushbackInputStream pbstream = new PushbackInputStream(fis, DEFAULT_HEADER_LENGTH_BYTES);
      Format f = detectFormat(pbstream);
      ObjectInputStream ois;
      if (f == Format.GZIP) {
        GZIPInputStream gis =
            closer.register(new GZIPInputStream(pbstream, 8192 /* enlarge buffer */));
        ois = new ObjectInputStream(gis);
      } else if (f == Format.LZ4) {
        LZ4FrameInputStream lis = closer.register(new LZ4FrameInputStream(pbstream));
        ois = new ObjectInputStream(lis);
      } else if (f == Format.JAVA_SERIALIZED) {
        ois = new ObjectInputStream(pbstream);
      } else {
        throw new BatfishException(
            String.format("Could not detect format of the file %s", sanitizedInputFile));
      }
      closer.register(ois);
      return outputClass.cast(ois.readObject());
    } catch (Exception e) {
      throw new BatfishException(
          String.format(
              "Failed to deserialize object of type %s from file %s",
              outputClass.getCanonicalName(), sanitizedInputFile),
          e);
    }
  }

  private <S extends Serializable> SortedMap<String, S> deserializeObjects(
      Map<Path, String> namesByPath, Class<S> outputClass) {
    String outputClassName = outputClass.getName();
    AtomicInteger completed =
        _newBatch.apply(
            String.format("Deserializing objects of type '%s' from files", outputClassName),
            namesByPath.size());
    return new TreeMap<>(
        namesByPath
            .entrySet()
            .parallelStream()
            .collect(
                Collectors.toMap(
                    Entry::getValue,
                    entry -> {
                      Path inputPath = entry.getKey();
                      String name = entry.getValue();
                      _logger.debugf(
                          "Reading %s '%s' from '%s'\n", outputClassName, name, inputPath);
                      S output = deserializeObject(inputPath, outputClass);
                      completed.incrementAndGet();
                      return output;
                    })));
  }

  /**
   * Writes a single object of the given class to the given file. Uses the {@link FileBasedStorage}
   * default file encoding including serialization format and compression.
   */
  private void serializeObject(Serializable object, Path outputFile) {
    Path sanitizedOutputFile = validatePath(outputFile);
    try {
      Path tmpFile = Files.createTempFile(null, null);
      try {
        try (OutputStream out = Files.newOutputStream(tmpFile);
            LZ4FrameOutputStream gos = new LZ4FrameOutputStream(out);
            ObjectOutputStream oos = new ObjectOutputStream(gos)) {
          oos.writeObject(object);
        } catch (Throwable e) {
          throw new BatfishException(
              "Failed to serialize object to output file: " + sanitizedOutputFile, e);
        }
        mkdirs(sanitizedOutputFile.getParent());
        Files.move(tmpFile, sanitizedOutputFile, StandardCopyOption.REPLACE_EXISTING);
      } finally {
        Files.deleteIfExists(tmpFile);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private <S extends Serializable> void serializeObjects(Map<Path, S> objectsByPath) {
    if (objectsByPath.isEmpty()) {
      return;
    }
    int size = objectsByPath.size();
    String className = objectsByPath.values().iterator().next().getClass().getName();
    AtomicInteger serializeCompleted =
        _newBatch.apply(String.format("Serializing '%s' instances to disk", className), size);
    objectsByPath
        .entrySet()
        .parallelStream()
        .forEach(
            entry -> {
              Path outputPath = entry.getKey();
              S object = entry.getValue();
              serializeObject(object, outputPath);
              serializeCompleted.incrementAndGet();
            });
  }

  private boolean cachedConfigsAreCompatible(NetworkId network, SnapshotId snapshot) {
    try {
      ConvertConfigurationAnswerElement ccae =
          loadConvertConfigurationAnswerElement(network, snapshot);
      return ccae != null;
    } catch (BatfishException e) {
      _logger.warnf(
          "Unexpected exception caught while deserializing configs for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return false;
    }
  }

  @Override
  public @Nonnull String loadQuestion(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis) throws IOException {
    return readFileToString(getQuestionPath(network, question, analysis), UTF_8);
  }

  @Override
  public boolean checkQuestionExists(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis) {
    return Files.exists(getQuestionPath(network, question, analysis));
  }

  private @Nonnull Path getQuestionPath(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis) {
    return getQuestionDir(network, question, analysis).resolve(BfConsts.RELPATH_QUESTION_FILE);
  }

  @Override
  public @Nonnull String loadAnswer(AnswerId answerId) throws FileNotFoundException, IOException {
    Path answerPath = getAnswerPath(answerId);
    if (!Files.exists(answerPath)) {
      throw new FileNotFoundException(String.format("Could not find answer with ID: %s", answerId));
    }
    return readFileToString(answerPath, UTF_8);
  }

  @Override
  public @Nonnull AnswerMetadata loadAnswerMetadata(AnswerId answerId)
      throws FileNotFoundException, IOException {
    Path answerMetadataPath = getAnswerMetadataPath(answerId);
    if (!Files.exists(answerMetadataPath)) {
      throw new FileNotFoundException(
          String.format("Could not find answer metadata for ID: %s", answerId));
    }
    return BatfishObjectMapper.mapper()
        .readValue(answerMetadataPath.toFile(), new TypeReference<AnswerMetadata>() {});
  }

  @VisibleForTesting
  @Nonnull
  Path getAnswerPath(AnswerId answerId) {
    return getAnswerDir(answerId).resolve(RELPATH_ANSWER_JSON);
  }

  private @Nonnull Path getAnswerMetadataPath(AnswerId answerId) {
    return getAnswerDir(answerId).resolve(RELPATH_ANSWER_METADATA);
  }

  @Override
  public void storeQuestion(
      String questionStr, NetworkId network, QuestionId question, @Nullable AnalysisId analysis)
      throws IOException {
    Path questionPath = getQuestionPath(network, question, analysis);
    mkdirs(questionPath.getParent());
    writeStringToFile(questionPath, questionStr, UTF_8);
  }

  @Override
  public @Nullable String loadQuestionSettings(
      NetworkId networkId, QuestionSettingsId questionSettingsId) throws IOException {
    Path questionSettingsPath = getQuestionSettingsPath(networkId, questionSettingsId);
    if (!Files.exists(questionSettingsPath)) {
      return null;
    }
    return readFileToString(questionSettingsPath, UTF_8);
  }

  @Override
  public boolean checkNetworkExists(NetworkId network) {
    return Files.exists(getNetworkDir(network));
  }

  @Override
  public void storeQuestionSettings(
      String settings, NetworkId network, QuestionSettingsId questionSettingsId)
      throws IOException {
    writeStringToFile(getQuestionSettingsPath(network, questionSettingsId), settings, UTF_8);
  }

  @Override
  public boolean hasAnswerMetadata(AnswerId answerId) {
    return Files.exists(getAnswerMetadataPath(answerId));
  }

  @Override
  public String loadQuestionClassId(
      NetworkId networkId, QuestionId questionId, AnalysisId analysisId) throws IOException {
    return Question.parseQuestion(loadQuestion(networkId, questionId, analysisId)).getName();
  }

  @Override
  public boolean hasAnalysisMetadata(NetworkId networkId, AnalysisId analysisId) {
    return Files.exists(getAnalysisMetadataPath(networkId, analysisId));
  }

  private @Nonnull Path getAnalysisMetadataPath(NetworkId networkId, AnalysisId analysisId) {
    return getNetworkAnalysisDir(networkId, analysisId).resolve(RELPATH_METADATA_FILE);
  }

  private @Nonnull Path getSnapshotMetadataPath(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_METADATA_FILE);
  }

  @Override
  public void storeAnalysisMetadata(
      AnalysisMetadata analysisMetadata, NetworkId networkId, AnalysisId analysisId)
      throws IOException {
    writeJsonFile(getAnalysisMetadataPath(networkId, analysisId), analysisMetadata);
  }

  @Override
  public String loadAnalysisMetadata(NetworkId networkId, AnalysisId analysisId)
      throws FileNotFoundException, IOException {
    return readFileToString(getAnalysisMetadataPath(networkId, analysisId), UTF_8);
  }

  @Override
  public void storeSnapshotMetadata(
      SnapshotMetadata snapshotMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    writeJsonFile(getSnapshotMetadataPath(networkId, snapshotId), snapshotMetadata);
  }

  @Override
  public String loadSnapshotMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws FileNotFoundException, IOException {
    return readFileToString(getSnapshotMetadataPath(networkId, snapshotId), UTF_8);
  }

  @Override
  public void storeNodeRoles(NodeRolesData nodeRolesData, NodeRolesId nodeRolesId)
      throws IOException {
    writeJsonFile(getNodeRolesPath(nodeRolesId), nodeRolesData);
  }

  private @Nonnull Path getNodeRolesPath(NodeRolesId nodeRolesId) {
    return getNodeRolesDir().resolve(String.format("%s%s", nodeRolesId.getId(), ".json"));
  }

  @Override
  public String loadNodeRoles(NodeRolesId nodeRolesId) throws FileNotFoundException, IOException {
    return readFileToString(getNodeRolesPath(nodeRolesId), UTF_8);
  }

  @Override
  public boolean hasNodeRoles(NodeRolesId nodeRolesId) {
    return Files.exists(getNodeRolesPath(nodeRolesId));
  }

  @Override
  public void initNetwork(NetworkId networkId) {
    getNetworkDir(networkId).toFile().mkdirs();
  }

  @Override
  public void deleteAnswerMetadata(AnswerId answerId) throws FileNotFoundException, IOException {
    Files.delete(getAnswerMetadataPath(answerId));
  }

  /** {@code key} must be relative normalized path. */
  @VisibleForTesting
  static @Nonnull Path objectKeyToRelativePath(String key) {
    Path relativePathCandidate = Paths.get(FilenameUtils.separatorsToSystem(key));
    // ensure path is relative
    checkArgument(
        relativePathCandidate.getRoot() == null,
        "Key '%s' does not represent a relative path",
        key);
    // ensure path is normalized
    checkArgument(
        relativePathCandidate.equals(relativePathCandidate.normalize()),
        "Key '%s' does not represent a normalized path  (without '.', '..',  etc.)",
        key);
    return relativePathCandidate;
  }

  private @Nonnull Path getNetworkObjectPath(NetworkId networkId, String key) {
    String encodedKey = toBase64(key);
    return getNetworkObjectsDir(networkId).resolve(encodedKey);
  }

  @Override
  public @Nonnull InputStream loadNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException {
    Path objectPath = getNetworkObjectPath(networkId, key);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not load: %s", objectPath));
    }
    return Files.newInputStream(objectPath);
  }

  @Override
  public void storeNetworkObject(InputStream inputStream, NetworkId networkId, String key)
      throws IOException {
    Path objectPath = getNetworkObjectPath(networkId, key);
    objectPath.getParent().toFile().mkdirs();
    try {
      FileUtils.copyInputStreamToFile(inputStream, objectPath.toFile());
    } finally {
      inputStream.close();
    }
  }

  private @Nonnull Path getNetworkBlobPath(NetworkId networkId, String key) {
    String encodedKey = toBase64(key);
    return getNetworkBlobsDir(networkId).resolve(encodedKey);
  }

  @Override
  public @Nonnull InputStream loadNetworkBlob(NetworkId networkId, String key)
      throws FileNotFoundException, IOException {
    Path objectPath = getNetworkBlobPath(networkId, key);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not load: %s", objectPath));
    }
    return Files.newInputStream(objectPath);
  }

  @Override
  public void storeNetworkBlob(InputStream inputStream, NetworkId networkId, String key)
      throws IOException {
    Path objectPath = getNetworkBlobPath(networkId, key);
    objectPath.getParent().toFile().mkdirs();
    try {
      FileUtils.copyInputStreamToFile(inputStream, objectPath.toFile());
    } finally {
      inputStream.close();
    }
  }

  @Override
  public void deleteNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException {
    Path objectPath = getNetworkObjectPath(networkId, key);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not delete: %s", objectPath));
    }
    Files.delete(objectPath);
  }

  private @Nonnull Path getSnapshotObjectPath(
      NetworkId networkId, SnapshotId snapshotId, String key) {
    String encodedKey = toBase64(key);
    return getSnapshotObjectsDir(networkId, snapshotId).resolve(encodedKey);
  }

  public static @Nonnull String toBase64(String key) {
    return Base64.getUrlEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
  }

  public static @Nonnull String fromBase64(String key) {
    return new String(Base64.getUrlDecoder().decode(key), StandardCharsets.UTF_8);
  }

  @Override
  public @Nonnull InputStream loadSnapshotObject(
      NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException {
    Path objectPath = getSnapshotObjectPath(networkId, snapshotId, key);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not load: %s", objectPath));
    }
    return Files.newInputStream(objectPath);
  }

  @Override
  public void storeSnapshotObject(
      InputStream inputStream, NetworkId networkId, SnapshotId snapshotId, String key)
      throws IOException {
    Path objectPath = getSnapshotObjectPath(networkId, snapshotId, key);
    objectPath.getParent().toFile().mkdirs();
    try {
      FileUtils.copyInputStreamToFile(inputStream, objectPath.toFile());
    } finally {
      inputStream.close();
    }
  }

  @Override
  public void deleteSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException {
    Path objectPath = getSnapshotObjectPath(networkId, snapshotId, key);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not delete: %s", objectPath));
    }
    Files.delete(objectPath);
  }

  @MustBeClosed
  @Override
  public @Nonnull InputStream loadSnapshotInputObject(
      NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException {
    Path objectPath = getSnapshotInputObjectPath(networkId, snapshotId, key);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not load: %s", objectPath));
    }
    return Files.isDirectory(objectPath)
        ? ZipUtility.zipFilesToInputStream(objectPath)
        : Files.newInputStream(objectPath);
  }

  @Override
  public boolean hasSnapshotInputObject(String key, NetworkSnapshot snapshot) throws IOException {
    return Files.exists(
        getSnapshotInputObjectPath(snapshot.getNetwork(), snapshot.getSnapshot(), key));
  }

  @Override
  public @Nonnull List<StoredObjectMetadata> getSnapshotInputObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    Path objectPath = getSnapshotInputObjectsDir(networkId, snapshotId);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not load: %s", objectPath));
    }

    try {
      return Files.walk(objectPath)
          .filter(Files::isRegularFile)
          .map(
              path ->
                  new StoredObjectMetadata(
                      objectPath.relativize(path).toString(), getObjectSize(path)))
          .collect(ImmutableList.toImmutableList());
    } catch (BatfishException e) {
      throw new IOException(e);
    }
  }

  @Override
  public @Nonnull List<StoredObjectMetadata> getSnapshotExtendedObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    Path objectPath = getSnapshotObjectsDir(networkId, snapshotId);
    if (!Files.exists(objectPath)) {
      throw new FileNotFoundException(String.format("Could not load: %s", objectPath));
    }

    try {
      return Files.walk(objectPath)
          .filter(Files::isRegularFile)
          .map(
              path ->
                  new StoredObjectMetadata(
                      fromBase64(path.getFileName().toString()), getObjectSize(path)))
          .collect(ImmutableList.toImmutableList());
    } catch (BatfishException e) {
      throw new IOException(e);
    }
  }

  private long getObjectSize(Path objectPath) {
    try {
      return Files.size(objectPath);
    } catch (IOException e) {
      throw new BatfishException(
          String.format("Could not get size of object at path: %s", objectPath), e);
    }
  }

  @VisibleForTesting
  Path getSnapshotInputObjectPath(NetworkId networkId, SnapshotId snapshotId, String key) {
    Path relativePath = objectKeyToRelativePath(key);
    return getSnapshotInputObjectsDir(networkId, snapshotId).resolve(relativePath);
  }

  @Override
  public @Nonnull String loadPojoTopology(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    return readFileToString(getPojoTopologyPath(networkId, snapshotId), UTF_8);
  }

  private @Nonnull Path getBgpTopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_BGP_TOPOLOGY);
  }

  private @Nonnull Path getEigrpTopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_EIGRP_TOPOLOGY);
  }

  private @Nonnull Path getLayer2TopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_LAYER2_TOPOLOGY);
  }

  private @Nonnull Path getLayer3TopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_LAYER3_TOPOLOGY);
  }

  private @Nonnull Path getOspfTopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_OSPF_TOPOLOGY);
  }

  private @Nonnull Path getVxlanTopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_VXLAN_TOPOLOGY);
  }

  private @Nonnull Path getPojoTopologyPath(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_TESTRIG_POJO_TOPOLOGY_PATH);
  }

  /**
   * Returns path of work log for a given baseDir, network, snapshot, and workId. This function is a
   * temporary helper until file-based logging in batfish worker is abstracted away.
   */
  public static @Nonnull Path getWorkLogPath(
      Path baseDir, NetworkId network, SnapshotId snapshot, String workId) {
    return new FileBasedStorage(baseDir, null).getWorkLogPath(network, snapshot, workId);
  }

  @Nonnull
  private Path getWorkLogPath(NetworkId network, SnapshotId snapshot, String workId) {
    return getSnapshotOutputDir(network, snapshot).resolve(toBase64(workId + SUFFIX_LOG_FILE));
  }

  @Nonnull
  private Path getWorkJsonPath(NetworkId network, SnapshotId snapshot, String workId) {
    return getSnapshotOutputDir(network, snapshot)
        .resolve(toBase64(workId + SUFFIX_ANSWER_JSON_FILE));
  }

  @Override
  public @Nonnull String loadInitialTopology(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path path = getEnvTopologyPath(networkId, snapshotId);
    return readFileToString(path, UTF_8);
  }

  private @Nonnull Path getEnvTopologyPath(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_ENV_TOPOLOGY_FILE);
  }

  @Override
  public void storeInitialTopology(Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path path = getEnvTopologyPath(networkId, snapshotId);
    path.getParent().toFile().mkdirs();
    writeJsonFile(path, topology);
  }

  @Override
  public void storePojoTopology(
      org.batfish.datamodel.pojo.Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path path = getPojoTopologyPath(networkId, snapshotId);
    path.getParent().toFile().mkdirs();
    writeStringToFile(path, BatfishObjectMapper.writeString(topology), UTF_8);
  }

  @Override
  public void storeWorkLog(String logOutput, NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    writeStringToFile(getWorkLogPath(network, snapshot, workId), logOutput, UTF_8);
  }

  @Override
  public void storeWorkJson(
      String jsonOutput, NetworkId network, SnapshotId snapshot, String workId) throws IOException {
    writeStringToFile(getWorkJsonPath(network, snapshot, workId), jsonOutput, UTF_8);
  }

  @Override
  public CompletionMetadata loadCompletionMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path completionMetadataPath = getSnapshotCompletionMetadataPath(networkId, snapshotId);
    if (!Files.exists(completionMetadataPath)) {
      return CompletionMetadata.EMPTY;
    }
    return BatfishObjectMapper.mapper()
        .readValue(completionMetadataPath.toFile(), CompletionMetadata.class);
  }

  @Override
  public void storeCompletionMetadata(
      CompletionMetadata completionMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path completionMetadataPath = getSnapshotCompletionMetadataPath(networkId, snapshotId);
    mkdirs(completionMetadataPath.getParent());
    writeJsonFile(completionMetadataPath, completionMetadata);
  }

  private @Nonnull Path getSnapshotCompletionMetadataPath(
      NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_COMPLETION_METADATA_FILE);
  }

  /**
   * Make specified directory along with any parent directories if they do not already exist.
   *
   * @param dir directory to create
   * @throws IOException if there is an error creating the directories
   */
  @VisibleForTesting
  void mkdirs(Path dir) throws IOException {
    Path sanitizedDir = validatePath(dir);
    if (!sanitizedDir.toFile().mkdirs() && !sanitizedDir.toFile().exists()) {
      throw new IOException(String.format("Unable to create directory '%s'", sanitizedDir));
    }
  }

  @VisibleForTesting
  @Nonnull
  Path validatePath(Path path) {
    try {
      Path sanitizedPath = path.toFile().getCanonicalFile().toPath();
      checkArgument(
          sanitizedPath.toString().startsWith(_baseDir.toString()),
          "Path %s outside of base dir %s",
          path,
          _baseDir);
      return sanitizedPath;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void deleteDirectory(Path path) throws IOException {
    Path sanitizedPath = validatePath(path);
    FileUtils.deleteDirectory(sanitizedPath.toFile());
  }

  private void deleteIfExists(Path path) throws IOException {
    Path sanitizedPath = validatePath(path);
    Files.deleteIfExists(sanitizedPath);
  }

  private boolean isRegularFile(Path path) {
    Path sanitizedPath = validatePath(path);
    return Files.isRegularFile(sanitizedPath);
  }

  private boolean isDirectory(Path path) {
    Path sanitizedPath = validatePath(path);
    return Files.isDirectory(sanitizedPath);
  }

  private boolean exists(Path path) {
    Path sanitizedPath = validatePath(path);
    return Files.exists(sanitizedPath);
  }

  @MustBeClosed
  @Nonnull
  private Stream<Path> list(Path path) throws IOException {
    Path sanitizedPath = validatePath(path);
    return Files.list(sanitizedPath);
  }

  @VisibleForTesting
  @Nonnull
  Instant getLastModifiedTime(Path path) throws IOException {
    Path sanitizedPath = validatePath(path);
    return Files.getLastModifiedTime(sanitizedPath).toInstant();
  }

  /**
   * Read the contents of a file into a string, using the provided input charset.
   *
   * @throws FileNotFoundException if the file does not exist or is otherwise inaccessible
   * @throws IOException if there is any other error
   */
  private @Nonnull String readFileToString(Path file, Charset charset) throws IOException {
    Path sanitizedFile = validatePath(file);
    try (InputStream in = new FileInputStream(sanitizedFile.toFile())) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteStreams.copy(in, out);
      return new String(out.toByteArray(), charset);
    }
  }

  private void writeStringToFile(Path file, CharSequence data, Charset charset) throws IOException {
    Path sanitizedFile = validatePath(file);
    Path tmpFile = Files.createTempFile(null, null);
    try {
      FileUtils.write(tmpFile.toFile(), data, charset);
      mkdirs(sanitizedFile.getParent());
      Files.move(tmpFile, sanitizedFile, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  private void writeJsonFile(Path file, @Nullable Object json) throws IOException {
    Path sanitizedFile = validatePath(file);
    Path tmpFile = Files.createTempFile(null, null);
    try {
      BatfishObjectMapper.writer().writeValue(tmpFile.toFile(), json);
      mkdirs(sanitizedFile.getParent());
      Files.move(tmpFile, sanitizedFile, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  private void writeStreamToFile(InputStream inputStream, Path outputFile) throws IOException {
    Path sanitizedOutputFile = validatePath(outputFile);
    Path tmpFile = Files.createTempFile(null, null);
    try (OutputStream fileOutputStream = Files.newOutputStream(tmpFile)) {
      int read = 0;
      final byte[] bytes = new byte[STREAMED_FILE_BUFFER_SIZE];
      while ((read = inputStream.read(bytes)) != -1) {
        fileOutputStream.write(bytes, 0, read);
      }
      mkdirs(sanitizedOutputFile.getParent());
      Files.move(tmpFile, sanitizedOutputFile, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  @Override
  public @Nonnull BgpTopology loadBgpTopology(NetworkSnapshot networkSnapshot) throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(getBgpTopologyPath(networkSnapshot).toFile(), BgpTopology.class);
  }

  @Override
  public @Nonnull EigrpTopology loadEigrpTopology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(getEigrpTopologyPath(networkSnapshot).toFile(), EigrpTopology.class);
  }

  @Nonnull
  @Override
  public Optional<Layer1Topology> loadSynthesizedLayer1Topology(NetworkSnapshot snapshot)
      throws IOException {
    Path sl1tPath = getSynthesizedLayer1TopologyPath(snapshot.getNetwork(), snapshot.getSnapshot());
    // this is here for backward compatibility when we load up an existing container
    if (!Files.exists(sl1tPath)) {
      return Optional.empty();
    }
    return Optional.ofNullable(
        BatfishObjectMapper.mapper().readValue(sl1tPath.toFile(), Layer1Topology.class));
  }

  @Override
  public @Nonnull Optional<Layer2Topology> loadLayer2Topology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return Optional.ofNullable(
        BatfishObjectMapper.mapper()
            .readValue(getLayer2TopologyPath(networkSnapshot).toFile(), Layer2Topology.class));
  }

  @Override
  public @Nonnull Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(getLayer3TopologyPath(networkSnapshot).toFile(), Topology.class);
  }

  @Override
  public @Nonnull OspfTopology loadOspfTopology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(getOspfTopologyPath(networkSnapshot).toFile(), OspfTopology.class);
  }

  @Override
  public @Nonnull VxlanTopology loadVxlanTopology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(getVxlanTopologyPath(networkSnapshot).toFile(), VxlanTopology.class);
  }

  @Override
  public void storeBgpTopology(BgpTopology bgpTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getBgpTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeJsonFile(path, bgpTopology);
  }

  @Override
  public void storeEigrpTopology(EigrpTopology eigrpTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getEigrpTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeJsonFile(path, eigrpTopology);
  }

  @Override
  public void storeLayer2Topology(
      Optional<Layer2Topology> layer2Topology, NetworkSnapshot networkSnapshot) throws IOException {
    Path path = getLayer2TopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeJsonFile(path, layer2Topology.orElse(null));
  }

  @Override
  public void storeLayer3Topology(Topology layer3Topology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getLayer3TopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeJsonFile(path, layer3Topology);
  }

  @Override
  public void storeOspfTopology(OspfTopology ospfTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getOspfTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeJsonFile(path, ospfTopology);
  }

  @Override
  public void storeVxlanTopology(VxlanTopology vxlanTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getVxlanTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeJsonFile(path, vxlanTopology);
  }

  @VisibleForTesting
  void storeSynthesizedLayer1Topology(
      Layer1Topology synthesizedLayer1Topology, NetworkId network, SnapshotId snapshot)
      throws IOException {
    Path sl1tPath = getSynthesizedLayer1TopologyPath(network, snapshot);
    mkdirs(sl1tPath.getParent());
    writeJsonFile(sl1tPath, synthesizedLayer1Topology);
  }

  @Override
  public @Nonnull Optional<String> readId(Class<? extends Id> idType, String name, Id... ancestors)
      throws IOException {
    try {
      return Optional.of(readFileToString(getIdFile(idType, name, ancestors), UTF_8));
    } catch (FileNotFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public void writeId(Id id, String name, Id... ancestors) throws IOException {
    Path file = getIdFile(id.getClass(), name, ancestors);
    mkdirs(file.getParent());
    writeStringToFile(file, id.getId(), UTF_8);
  }

  @Override
  public boolean deleteNameIdMapping(Class<? extends Id> type, String name, Id... ancestors)
      throws IOException {
    return Files.deleteIfExists(getIdFile(type, name, ancestors));
  }

  @Override
  public boolean hasId(Class<? extends Id> type, String name, Id... ancestors) {
    return Files.exists(getIdFile(type, name, ancestors));
  }

  @Override
  public @Nonnull Set<String> listResolvableNames(Class<? extends Id> type, Id... ancestors)
      throws IOException {
    Path idsDir = getIdsDir(type, ancestors);
    if (!Files.exists(idsDir)) {
      return ImmutableSet.of();
    }
    try (Stream<Path> files = list(idsDir)) {
      return files
          .filter(
              path -> {
                try {
                  return fromBase64(path.getFileName().toString()).endsWith(ID_EXTENSION);
                } catch (IllegalArgumentException e) {
                  return false;
                }
              })
          .map(Path::getFileName)
          .map(Path::toString)
          .map(FileBasedStorage::fromBase64)
          .map(
              nameWithExtension ->
                  nameWithExtension.substring(
                      0, nameWithExtension.length() - ID_EXTENSION.length()))
          .collect(ImmutableSet.toImmutableSet());
    } catch (IOException e) {
      throw new IOException("Could not list files in '" + idsDir + "'", e);
    }
  }

  @Nonnull
  @Override
  public Optional<ReferenceLibrary> loadReferenceLibrary(NetworkId network) throws IOException {
    Path path = getReferenceLibraryPath(network);
    if (!Files.exists(path)) {
      return Optional.empty();
    }
    return Optional.of(
        BatfishObjectMapper.mapper()
            .readValue(getReferenceLibraryPath(network).toFile(), ReferenceLibrary.class));
  }

  @Override
  public void storeReferenceLibrary(ReferenceLibrary referenceLibrary, NetworkId network)
      throws IOException {
    writeJsonFile(getReferenceLibraryPath(network), referenceLibrary);
  }

  @Override
  public void storeUploadSnapshotZip(InputStream inputStream, String key, NetworkId network)
      throws IOException {
    writeStreamToFile(inputStream, getUploadSnapshotZipPath(key, network));
  }

  private @Nonnull Path getUploadSnapshotZipPath(String key, NetworkId network) {
    return getOriginalDir(key, network).resolve(RELPATH_SNAPSHOT_ZIP_FILE);
  }

  @Override
  public void storeForkSnapshotRequest(String forkSnapshotRequest, String key, NetworkId network)
      throws IOException {
    writeStringToFile(getForkSnapshotRequestPath(key, network), forkSnapshotRequest, UTF_8);
  }

  private @Nonnull Path getForkSnapshotRequestPath(String key, NetworkId network) {
    return getOriginalDir(key, network).resolve(RELPATH_FORK_REQUEST_FILE);
  }

  private static final int STREAMED_FILE_BUFFER_SIZE = 1024;

  @MustBeClosed
  @Nonnull
  @Override
  public InputStream loadUploadSnapshotZip(String key, NetworkId network) throws IOException {
    return new FileInputStream(getUploadSnapshotZipPath(key, network).toFile());
  }

  @Override
  public void storeSnapshotInputObject(
      InputStream inputStream, String key, NetworkSnapshot snapshot) throws IOException {
    writeStreamToFile(
        inputStream,
        getSnapshotInputObjectPath(snapshot.getNetwork(), snapshot.getSnapshot(), key));
  }

  @MustBeClosed
  @Nonnull
  @Override
  public Stream<String> listSnapshotInputObjectKeys(NetworkSnapshot snapshot) throws IOException {
    Path inputObjectsPath =
        getSnapshotInputObjectsDir(snapshot.getNetwork(), snapshot.getSnapshot());
    if (!isDirectory(inputObjectsPath)) {
      if (!isDirectory(inputObjectsPath.getParent())) {
        throw new FileNotFoundException(String.format("Missing snapshot dir for %s", snapshot));
      }
      // snapshot contained no input objects
      return Stream.empty();
    }
    return Files.walk(inputObjectsPath)
        .filter(Files::isRegularFile)
        .map(inputObjectsPath::relativize)
        // ignore hidden files and folders
        .filter(
            path -> stream(path).noneMatch(pathElement -> pathElement.toString().startsWith(".")))
        .map(Object::toString);
  }

  @Nonnull
  @Override
  public DataPlane loadDataPlane(NetworkSnapshot snapshot) throws IOException {
    return deserializeObject(getDataPlanePath(snapshot), DataPlane.class);
  }

  @Override
  public void storeDataPlane(DataPlane dataPlane, NetworkSnapshot snapshot) throws IOException {
    serializeObject(dataPlane, getDataPlanePath(snapshot));
  }

  @Override
  public boolean hasDataPlane(NetworkSnapshot snapshot) throws IOException {
    return Files.exists(getDataPlanePath(snapshot));
  }

  @MustBeClosed
  @Nonnull
  @Override
  public Stream<String> listInputEnvironmentBgpTableKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> key.startsWith(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES));
  }

  @Nonnull
  @Override
  public ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
      NetworkSnapshot snapshot) throws IOException {
    return deserializeObject(
        getParseEnvironmentBgpTablesAnswerElementPath(snapshot),
        ParseEnvironmentBgpTablesAnswerElement.class);
  }

  @Override
  public void storeParseEnvironmentBgpTablesAnswerElement(
      ParseEnvironmentBgpTablesAnswerElement parseEnvironmentBgpTablesAnswerElement,
      NetworkSnapshot snapshot)
      throws IOException {
    serializeObject(
        parseEnvironmentBgpTablesAnswerElement,
        getParseEnvironmentBgpTablesAnswerElementPath(snapshot));
  }

  @Override
  public boolean hasParseEnvironmentBgpTablesAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    return Files.exists(getParseEnvironmentBgpTablesAnswerElementPath(snapshot));
  }

  @Override
  public void deleteParseEnvironmentBgpTablesAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    Files.deleteIfExists(getParseEnvironmentBgpTablesAnswerElementPath(snapshot));
  }

  private @Nonnull Path getParseEnvironmentBgpTablesAnswerElementPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_ENVIRONMENT_BGP_TABLES_ANSWER);
  }

  @Nonnull
  @Override
  public Map<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(NetworkSnapshot snapshot)
      throws IOException {
    _logger.info("\n*** DESERIALIZING ENVIRONMENT BGP TABLES ***\n");
    _logger.resetTimer();
    Map<Path, String> namesByPath = new HashMap<>();
    Path dir = getEnvironmentBgpTablesPath(snapshot);
    if (!Files.exists(dir)) {
      return ImmutableSortedMap.of();
    }
    try (DirectoryStream<Path> serializedBgpTables =
        Files.newDirectoryStream(getEnvironmentBgpTablesPath(snapshot))) {
      for (Path serializedBgpTable : serializedBgpTables) {
        String name = serializedBgpTable.getFileName().toString();
        namesByPath.put(serializedBgpTable, name);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Error reading serialized BGP tables", e);
    }
    SortedMap<String, BgpAdvertisementsByVrf> bgpTables =
        deserializeObjects(namesByPath, BgpAdvertisementsByVrf.class);
    _logger.printElapsedTime();
    return bgpTables;
  }

  @Override
  public void storeEnvironmentBgpTables(
      Map<String, BgpAdvertisementsByVrf> environmentBgpTables, NetworkSnapshot snapshot)
      throws IOException {
    _logger.info("\n*** SERIALIZING ENVIRONMENT BGP TABLES ***\n");
    _logger.resetTimer();
    SortedMap<Path, BgpAdvertisementsByVrf> output = new TreeMap<>();
    Path outputPath = getEnvironmentBgpTablesPath(snapshot);
    environmentBgpTables.forEach(
        (name, rt) -> {
          Path currentOutputPath = outputPath.resolve(name);
          output.put(currentOutputPath, rt);
        });
    serializeObjects(output);
    _logger.printElapsedTime();
  }

  @Override
  public void deleteEnvironmentBgpTables(NetworkSnapshot snapshot) throws IOException {
    deleteDirectory(getEnvironmentBgpTablesPath(snapshot));
  }

  @Nonnull
  @Override
  public Optional<String> loadExternalBgpAnnouncementsFile(NetworkSnapshot snapshot)
      throws IOException {
    Path path =
        getSnapshotInputObjectPath(
            snapshot.getNetwork(), snapshot.getSnapshot(), RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS);
    if (!Files.exists(path)) {
      return Optional.empty();
    }
    return Optional.of(readFileToString(path, UTF_8));
  }

  private @Nonnull Path getEnvironmentBgpTablesPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_SERIALIZED_ENVIRONMENT_BGP_TABLES);
  }

  private @Nonnull Path getDataPlanePath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_DATA_PLANE);
  }

  private @Nonnull Path getReferenceLibraryPath(NetworkId network) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH);
  }

  private static String toIdDirName(Class<? extends Id> type) {
    return toBase64(type.getCanonicalName());
  }

  private @Nonnull Path getIdsDir(Class<? extends Id> type, Id... ancestors) {
    Path file = getStorageBase().resolve("ids");
    for (Id id : ancestors) {
      file = file.resolve(toIdDirName(id.getClass())).resolve(id.getId());
    }
    return file.resolve(toIdDirName(type));
  }

  private @Nonnull Path getIdFile(Class<? extends Id> type, String name, Id... ancestors) {
    return getIdsDir(type, ancestors).resolve(toBase64(name + ID_EXTENSION));
  }

  private static final String RELPATH_BLOBS = "blobs";
  private static final String RELPATH_EXTENDED = "extended";
  private static final String RELPATH_NODE_ROLES_DIR = "node_roles";

  private @Nonnull Path getAdHocQuestionDir(NetworkId network, QuestionId question) {
    return getAdHocQuestionsDir(network).resolve(question.getId());
  }

  private @Nonnull Path getAdHocQuestionsDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(RELPATH_QUESTIONS_DIR);
  }

  private @Nonnull Path getAnalysisQuestionDir(
      NetworkId network, QuestionId question, AnalysisId analysis) {
    return getAnalysisQuestionsDir(network, analysis).resolve(question.getId());
  }

  private @Nonnull Path getAnalysisQuestionsDir(NetworkId network, AnalysisId analysis) {
    return getNetworkAnalysisDir(network, analysis).resolve(RELPATH_QUESTIONS_DIR);
  }

  private @Nonnull Path getAnswersDir() {
    return _baseDir.resolve(RELPATH_ANSWERS_DIR);
  }

  @VisibleForTesting
  @Nonnull
  Path getAnswerDir(AnswerId answerId) {
    return getAnswersDir().resolve(answerId.getId());
  }

  private @Nonnull Path getMajorIssueConfigDir(NetworkId network, IssueSettingsId majorIssueType) {
    return getNetworkSettingsDir(network)
        .resolve(RELPATH_CONTAINER_SETTINGS_ISSUES)
        .resolve(majorIssueType + ".json");
  }

  private @Nonnull Path getNetworkAnalysisDir(NetworkId network, AnalysisId analysis) {
    return getNetworkDir(network).resolve(RELPATH_ANALYSES_DIR).resolve(analysis.getId());
  }

  private @Nonnull Path getNetworksDir() {
    return _baseDir.resolve("networks");
  }

  @VisibleForTesting
  @Nonnull
  Path getNetworkDir(NetworkId network) {
    return getNetworksDir().resolve(network.getId());
  }

  /** Directory where original initialization or fork requests are stored */
  private @Nonnull Path getOriginalsDir(NetworkId network) {
    return getNetworkDir(network).resolve(RELPATH_ORIGINAL_DIR);
  }

  /** Directory where original initialization or fork request is stored */
  @Nonnull
  Path getOriginalDir(String key, NetworkId network) {
    return getOriginalsDir(network).resolve(toBase64(key));
  }

  private @Nonnull Path getNetworkSettingsDir(NetworkId network) {
    return getNetworkDir(network).resolve(RELPATH_CONTAINER_SETTINGS);
  }

  private Path getNodeRolesDir() {
    return _baseDir.resolve(RELPATH_NODE_ROLES_DIR);
  }

  private @Nonnull Path getQuestionDir(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis) {
    return analysis != null
        ? getAnalysisQuestionDir(network, question, analysis)
        : getAdHocQuestionDir(network, question);
  }

  private @Nonnull Path getSnapshotsDir(NetworkId network) {
    return getNetworkDir(network).resolve(RELPATH_SNAPSHOTS_DIR);
  }

  @VisibleForTesting
  @Nonnull
  Path getSnapshotDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotsDir(network).resolve(snapshot.getId());
  }

  @VisibleForTesting
  @Nonnull
  Path getStorageBase() {
    return _baseDir;
  }

  private @Nonnull Path getVendorIndependentConfigDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotOutputDir(network, snapshot).resolve(RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);
  }

  private Path getNetworkBlobsDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(RELPATH_BLOBS);
  }

  private Path getNetworkObjectsDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(RELPATH_EXTENDED);
  }

  private Path getSnapshotObjectsDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(RELPATH_EXTENDED);
  }

  @VisibleForTesting
  @Nonnull
  Path getSnapshotInputObjectsDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(BfConsts.RELPATH_INPUT);
  }

  private Path getSnapshotOutputDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(RELPATH_OUTPUT);
  }

  @Nonnull
  @Override
  public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
      NetworkSnapshot snapshot) throws IOException {
    return deserializeObject(
        getParseVendorConfigurationAnswerElementPath(snapshot),
        ParseVendorConfigurationAnswerElement.class);
  }

  @Override
  public void storeParseVendorConfigurationAnswerElement(
      ParseVendorConfigurationAnswerElement parseVendorConfigurationAnswerElement,
      NetworkSnapshot snapshot)
      throws IOException {
    serializeObject(
        parseVendorConfigurationAnswerElement,
        getParseVendorConfigurationAnswerElementPath(snapshot));
  }

  @Override
  public boolean hasParseVendorConfigurationAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    return Files.exists(getParseVendorConfigurationAnswerElementPath(snapshot));
  }

  @Override
  public void deleteParseVendorConfigurationAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    Files.deleteIfExists(getParseVendorConfigurationAnswerElementPath(snapshot));
  }

  @Nonnull
  @Override
  public Map<String, VendorConfiguration> loadVendorConfigurations(NetworkSnapshot snapshot)
      throws IOException {
    _logger.info("\n*** DESERIALIZING VENDOR CONFIGURATION STRUCTURES ***\n");
    _logger.resetTimer();
    Map<Path, String> namesByPath = new TreeMap<>();
    Path serializedVendorConfigPath = getVendorConfigurationsPath(snapshot);
    if (!Files.exists(serializedVendorConfigPath)) {
      return ImmutableSortedMap.of();
    }
    try (DirectoryStream<Path> serializedConfigs =
        Files.newDirectoryStream(serializedVendorConfigPath)) {
      for (Path serializedConfig : serializedConfigs) {
        String name = serializedConfig.getFileName().toString();
        namesByPath.put(serializedConfig, name);
      }
    } catch (IOException e) {
      throw new BatfishException("Error reading vendor configs directory", e);
    }
    Map<String, VendorConfiguration> vendorConfigurations =
        deserializeObjects(namesByPath, VendorConfiguration.class);
    _logger.printElapsedTime();
    return vendorConfigurations;
  }

  private @Nonnull Path getVendorConfigurationsPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_VENDOR_SPECIFIC_CONFIG_DIR);
  }

  @Override
  public void storeVendorConfigurations(
      Map<String, VendorConfiguration> vendorConfigurations, NetworkSnapshot snapshot)
      throws IOException {
    Map<Path, VendorConfiguration> output = new HashMap<>();
    Path outputPath = getVendorConfigurationsPath(snapshot);
    vendorConfigurations.forEach(
        (name, vc) -> {
          Path currentOutputPath = outputPath.resolve(name);
          output.put(currentOutputPath, vc);
        });
    serializeObjects(output);
  }

  @Override
  public void deleteVendorConfigurations(NetworkSnapshot snapshot) throws IOException {
    deleteDirectory(getVendorConfigurationsPath(snapshot));
  }

  @MustBeClosed
  @Nonnull
  @Override
  public Stream<String> listInputHostConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> key.startsWith(BfConsts.RELPATH_HOST_CONFIGS_DIR));
  }

  @MustBeClosed
  @Nonnull
  @Override
  public Stream<String> listInputNetworkConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> key.startsWith(BfConsts.RELPATH_CONFIGURATIONS_DIR));
  }

  @MustBeClosed
  @Nonnull
  @Override
  public Stream<String> listInputAwsMultiAccountKeys(NetworkSnapshot snapshot) throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> key.startsWith(RELPATH_AWS_ACCOUNTS_DIR));
  }

  @MustBeClosed
  @Nonnull
  @Override
  public Stream<String> listInputAwsSingleAccountKeys(NetworkSnapshot snapshot) throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> key.startsWith(BfConsts.RELPATH_AWS_CONFIGS_DIR));
  }

  private @Nonnull Path getParseVendorConfigurationAnswerElementPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_PARSE_ANSWER_PATH);
  }

  @Override
  public void runGarbageCollection(Instant expungeBeforeDate) throws IOException {
    // Go back GC_SKEW_ALLOWANCE_MINUTES minutes to account for skew. This should safely
    // underapproximate data to delete.
    Instant safeExpungeBeforeDate = expungeBeforeDate.minus(GC_SKEW_ALLOWANCE);
    _logger.debugf("FBS GC: Expunge before: %s\n", safeExpungeBeforeDate);

    // Iterate over network dirs directly so we can expunge data from both extant and deleted
    // networks.
    if (exists(getNetworksDir())) {
      try (Stream<Path> networkDirStream = list(getNetworksDir())) {
        networkDirStream
            .map(networkDir -> new NetworkId(networkDir.getFileName().toString()))
            .forEach(
                networkId -> {
                  try {
                    expungeOldNetworkData(safeExpungeBeforeDate, networkId);
                  } catch (IOException e) {
                    _logger.errorf(
                        "Failed to expunge old data for network with ID '%s': %s",
                        networkId, Throwables.getStackTraceAsString(e));
                  }
                });
      }
    }
    // expunge answers
    expungeOldEntries(safeExpungeBeforeDate, getAnswersDir(), true);
  }

  private void expungeOldNetworkData(Instant expungeBeforeDate, NetworkId networkId)
      throws IOException {
    // expunge snapshots
    expungeOldEntries(expungeBeforeDate, getSnapshotsDir(networkId), true);

    // expunge original uploads
    expungeOldEntries(expungeBeforeDate, getOriginalsDir(networkId), true);

    // TODO: expunge question IDs, questions, analysis IDs, analyses, node roles IDs, node roles
  }

  /**
   * Deletes filesystem entries in {@code dir} whose last modified time precedes {@code
   * expungeBeforeDate}. When deleting regular files, {@code directories} should be {@code false}.
   * When deleting directories, {@code directories} should be {@code true}. Has no effect if {@code
   * dir} does not exist.
   *
   * @throws IOException if there is an error
   */
  @VisibleForTesting
  void expungeOldEntries(Instant expungeBeforeDate, Path dir, boolean directories)
      throws IOException {
    if (!exists(dir)) {
      return;
    }
    List<Path> toDelete;
    try (Stream<Path> fsEntryStream = list(dir)) {
      toDelete =
          fsEntryStream
              .filter(
                  path -> {
                    if (directories ? !isDirectory(path) : !isRegularFile(path)) {
                      // If this is not the type of filesystem entry we want to delete, ignore this
                      // path.
                      return false;
                    }
                    try {
                      return getLastModifiedTime(path).compareTo(expungeBeforeDate) < 0;
                    } catch (IOException e) {
                      // If for some reason the last modified time of the entry cannot be fetched
                      // (e.g. it was just deleted), ignore this path.
                      return false;
                    }
                  })
              .collect(ImmutableList.toImmutableList());
    }
    for (Path path : toDelete) {
      if (directories) {
        deleteDirectory(path);
      } else {
        deleteIfExists(path);
      }
      _logger.debugf("FBS GC: deleted: %s\n", path);
    }
  }
}
