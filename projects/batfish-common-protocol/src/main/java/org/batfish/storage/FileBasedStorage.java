package org.batfish.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.plugin.PluginConsumer.DEFAULT_HEADER_LENGTH_BYTES;
import static org.batfish.common.plugin.PluginConsumer.detectFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closer;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
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
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.bgp.BgpTopology;
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
import org.batfish.role.NodeRolesData;

/** A utility class that abstracts the underlying file system storage used by Batfish. */
@ParametersAreNonnullByDefault
public final class FileBasedStorage implements StorageProvider {

  private static final String ID_EXTENSION = ".id";
  private static final String RELPATH_COMPLETION_METADATA_FILE = "completion_metadata.json";
  private static final String RELPATH_BGP_TOPOLOGY = "bgp_topology.json";
  private static final String RELPATH_EIGRP_TOPOLOGY = "eigrp_topology.json";
  private static final String RELPATH_SYNTHESIZED_LAYER1_TOPOLOGY =
      "synthesized_layer1_topology.json";
  private static final String RELPATH_LAYER2_TOPOLOGY = "layer2_topology.json";
  private static final String RELPATH_LAYER3_TOPOLOGY = "layer3_topology.json";
  private static final String RELPATH_OSPF_TOPOLOGY = "ospf_topology.json";
  private static final String RELPATH_VXLAN_TOPOLOGY = "vxlan_topology.json";

  private final BatfishLogger _logger;
  private final BiFunction<String, Integer, AtomicInteger> _newBatch;
  private FileBasedStorageDirectoryProvider _d;

  @VisibleForTesting
  @Nonnull
  FileBasedStorageDirectoryProvider getDirectoryProvider() {
    return _d;
  }

  /**
   * Create a new {@link FileBasedStorage} instance that uses the given root path and job batch
   * provider function.
   */
  public FileBasedStorage(
      Path baseDir, BatfishLogger logger, BiFunction<String, Integer, AtomicInteger> newBatch) {
    _logger = logger;
    _newBatch = newBatch;
    _d = new FileBasedStorageDirectoryProvider(baseDir);
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
    Path testrigDir = _d.getSnapshotDir(network, snapshot);
    Path indepDir =
        testrigDir.resolve(
            Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR));
    return loadConfigurations(network, snapshot, indepDir);
  }

  private @Nullable SortedMap<String, Configuration> loadConfigurations(
      NetworkId network, SnapshotId snapshot, Path indepDir) {
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
    Path ccaePath =
        _d.getSnapshotDir(network, snapshot)
            .resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_CONVERT_ANSWER_PATH));
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
            BfConsts.RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel)
            .map(p -> _d.getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    try {
      String fileText = readFileToString(path.get(), UTF_8);
      return BatfishObjectMapper.mapper()
          .readValue(fileText, new TypeReference<SortedSet<NodeInterfacePair>>() {});
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading interface blacklist for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    }
  }

  @Override
  public @Nullable IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot) {
    Path path =
        _d.getSnapshotDir(network, snapshot)
            .resolve(
                Paths.get(
                    BfConsts.RELPATH_INPUT,
                    BfConsts.RELPATH_BATFISH_CONFIGS_DIR,
                    BfConsts.RELPATH_ISP_CONFIG_FILE));
    if (!Files.exists(path)) {
      return null;
    }
    try {
      String fileText = readFileToString(path, UTF_8);
      return BatfishObjectMapper.mapper()
          .readValue(fileText, new TypeReference<IspConfiguration>() {});
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
            BfConsts.RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_NODE_BLACKLIST_FILE);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_NODE_BLACKLIST_FILE);
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel)
            .map(p -> _d.getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    try {
      String fileText = readFileToString(path.get(), UTF_8);
      return BatfishObjectMapper.mapper()
          .readValue(fileText, new TypeReference<SortedSet<String>>() {});
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
            BfConsts.RELPATH_INPUT,
            BfConsts.RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_L1_TOPOLOGY_PATH);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_L1_TOPOLOGY_PATH);
    Path deprecated = Paths.get(BfConsts.RELPATH_INPUT, "testrig_layer1_topology");
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel, deprecated)
            .map(p -> _d.getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    AtomicInteger counter = _newBatch.apply("Reading layer-1 topology", 1);
    try {
      String topologyFileText = readFileToString(path.get(), UTF_8);
      return BatfishObjectMapper.mapper().readValue(topologyFileText, Layer1Topology.class);
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
    Path path = _d.getMajorIssueConfigDir(network, majorIssueType);

    if (!Files.exists(path)) {
      return null;
    }

    try {
      String majorIssueFileText = readFileToString(path, UTF_8);
      return BatfishObjectMapper.mapper().readValue(majorIssueFileText, MajorIssueConfig.class);
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
    Path path = _d.getMajorIssueConfigDir(network, majorIssueType);

    if (Files.notExists(path)) {
      Files.createDirectories(path.getParent());
    }

    writeStringToFile(
        path, BatfishObjectMapper.mapper().writeValueAsString(majorIssueConfig), UTF_8);
  }

  private @Nonnull Path getQuestionSettingsPath(
      NetworkId network, QuestionSettingsId questionSettingsId) {
    return _d.getNetworkSettingsDir(network)
        .resolve(BfConsts.RELPATH_QUESTIONS_DIR)
        .resolve(String.format("%s.json", questionSettingsId.getId()));
  }

  @Override
  public @Nullable SnapshotRuntimeData loadRuntimeData(NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(
            BfConsts.RELPATH_INPUT,
            BfConsts.RELPATH_BATFISH_CONFIGS_DIR,
            BfConsts.RELPATH_RUNTIME_DATA_FILE);
    Path topLevel = Paths.get(BfConsts.RELPATH_INPUT, BfConsts.RELPATH_RUNTIME_DATA_FILE);
    Optional<Path> path =
        Stream.of(insideBatfish, topLevel)
            .map(p -> _d.getSnapshotDir(network, snapshot).resolve(p))
            .filter(Files::exists)
            .findFirst();
    if (!path.isPresent()) {
      // Neither file was present in input.
      return null;
    }

    AtomicInteger counter = _newBatch.apply("Reading runtime data", 1);
    try {
      String runtimeDataFileText = readFileToString(path.get(), UTF_8);
      return BatfishObjectMapper.mapper().readValue(runtimeDataFileText, SnapshotRuntimeData.class);
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

    mkdirs(_d.getSnapshotDir(network, snapshot));

    // Save the convert configuration answer element.
    Path ccaePath = getConvertAnswerPath(network, snapshot);
    mkdirs(ccaePath.getParent());
    serializeObject(convertAnswerElement, ccaePath);

    // Save the synthesized layer1 topology
    if (synthesizedLayer1Topology != null) {
      storeSynthesizedLayer1Topology(synthesizedLayer1Topology, network, snapshot);
    }

    Path outputDir = _d.getVendorIndependentConfigDir(network, snapshot);

    String batchName =
        String.format(
            "Serializing %s vendor-independent configuration structures for snapshot %s",
            configurations.size(), snapshot);

    storeConfigurations(outputDir, batchName, configurations);
  }

  private @Nonnull Path getConvertAnswerPath(NetworkId network, SnapshotId snapshot) {
    return _d.getSnapshotDir(network, snapshot)
        .resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_CONVERT_ANSWER_PATH));
  }

  private @Nonnull Path getSynthesizedLayer1TopologyPath(NetworkId network, SnapshotId snapshot) {
    return _d.getSnapshotDir(network, snapshot)
        .resolve(Paths.get(BfConsts.RELPATH_OUTPUT, RELPATH_SYNTHESIZED_LAYER1_TOPOLOGY));
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
    String metricsStr;
    try {
      metricsStr = BatfishObjectMapper.writeString(answerMetadata);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Could not write answer metrics", e);
    }
    Path answerMetadataPath = getAnswerMetadataPath(answerId);
    mkdirs(answerMetadataPath.getParent());
    writeStringToFile(answerMetadataPath, metricsStr, UTF_8);
  }

  /**
   * Returns a single object of the given class deserialized from the given file. Uses the {@link
   * FileBasedStorage} default file encoding including serialization format and compression.
   */
  private static <S extends Serializable> S deserializeObject(Path inputFile, Class<S> outputClass)
      throws BatfishException {
    try (Closer closer = Closer.create()) {
      FileInputStream fis = closer.register(new FileInputStream(inputFile.toFile()));
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
            String.format("Could not detect format of the file %s", inputFile));
      }
      closer.register(ois);
      return outputClass.cast(ois.readObject());
    } catch (Exception e) {
      throw new BatfishException(
          String.format(
              "Failed to deserialize object of type %s from file %s",
              outputClass.getCanonicalName(), inputFile),
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
  private static void serializeObject(Serializable object, Path outputFile) {
    try {
      try (OutputStream out = Files.newOutputStream(outputFile);
          LZ4FrameOutputStream gos = new LZ4FrameOutputStream(out);
          ObjectOutputStream oos = new ObjectOutputStream(gos)) {
        oos.writeObject(object);
      }
    } catch (Throwable e) {
      throw new BatfishException("Failed to serialize object to output file: " + outputFile, e);
    }
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
    return _d.getQuestionDir(network, question, analysis).resolve(BfConsts.RELPATH_QUESTION_FILE);
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
    String answerMetadataStr = readFileToString(answerMetadataPath, UTF_8);
    return BatfishObjectMapper.mapper()
        .readValue(answerMetadataStr, new TypeReference<AnswerMetadata>() {});
  }

  private @Nonnull Path getAnswerPath(AnswerId answerId) {
    return _d.getAnswerDir(answerId).resolve(BfConsts.RELPATH_ANSWER_JSON);
  }

  private @Nonnull Path getAnswerMetadataPath(AnswerId answerId) {
    return _d.getAnswerDir(answerId).resolve(BfConsts.RELPATH_ANSWER_METADATA);
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
    return Files.exists(_d.getNetworkDir(network));
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
    return _d.getNetworkAnalysisDir(networkId, analysisId).resolve(BfConsts.RELPATH_METADATA_FILE);
  }

  private @Nonnull Path getSnapshotMetadataPath(NetworkId networkId, SnapshotId snapshotId) {
    return _d.getSnapshotDir(networkId, snapshotId)
        .resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_METADATA_FILE));
  }

  @Override
  public void storeAnalysisMetadata(
      AnalysisMetadata analysisMetadata, NetworkId networkId, AnalysisId analysisId)
      throws IOException {
    writeStringToFile(
        getAnalysisMetadataPath(networkId, analysisId),
        BatfishObjectMapper.writeString(analysisMetadata),
        UTF_8);
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
    writeStringToFile(
        getSnapshotMetadataPath(networkId, snapshotId),
        BatfishObjectMapper.writeString(snapshotMetadata),
        UTF_8);
  }

  @Override
  public String loadSnapshotMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws FileNotFoundException, IOException {
    return readFileToString(getSnapshotMetadataPath(networkId, snapshotId), UTF_8);
  }

  @Override
  public void storeNodeRoles(NodeRolesData nodeRolesData, NodeRolesId nodeRolesId)
      throws IOException {
    writeFile(getNodeRolesPath(nodeRolesId), BatfishObjectMapper.writeString(nodeRolesData), UTF_8);
  }

  private @Nonnull Path getNodeRolesPath(NodeRolesId nodeRolesId) {
    return _d.getNodeRolesDir().resolve(String.format("%s%s", nodeRolesId.getId(), ".json"));
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
    _d.getNetworkDir(networkId).toFile().mkdirs();
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
    return _d.getNetworkObjectsDir(networkId).resolve(encodedKey);
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
    return _d.getNetworkBlobsDir(networkId).resolve(encodedKey);
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
    return _d.getSnapshotObjectsDir(networkId, snapshotId).resolve(encodedKey);
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
  public @Nonnull List<StoredObjectMetadata> getSnapshotInputObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    Path objectPath = _d.getSnapshotInputObjectsDir(networkId, snapshotId);
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
    Path objectPath = _d.getSnapshotObjectsDir(networkId, snapshotId);
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
    return _d.getSnapshotInputObjectsDir(networkId, snapshotId).resolve(relativePath);
  }

  @Override
  public @Nonnull String loadPojoTopology(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    return readFileToString(getPojoTopologyPath(networkId, snapshotId), UTF_8);
  }

  private @Nonnull Path getBgpTopologyPath(NetworkSnapshot snapshot) {
    return _d.getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_BGP_TOPOLOGY);
  }

  private @Nonnull Path getEigrpTopologyPath(NetworkSnapshot snapshot) {
    return _d.getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_EIGRP_TOPOLOGY);
  }

  private @Nonnull Path getLayer2TopologyPath(NetworkSnapshot snapshot) {
    return _d.getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_LAYER2_TOPOLOGY);
  }

  private @Nonnull Path getLayer3TopologyPath(NetworkSnapshot snapshot) {
    return _d.getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_LAYER3_TOPOLOGY);
  }

  private @Nonnull Path getOspfTopologyPath(NetworkSnapshot snapshot) {
    return _d.getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_OSPF_TOPOLOGY);
  }

  private @Nonnull Path getVxlanTopologyPath(NetworkSnapshot snapshot) {
    return _d.getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_VXLAN_TOPOLOGY);
  }

  private @Nonnull Path getPojoTopologyPath(NetworkId networkId, SnapshotId snapshotId) {
    return _d.getSnapshotDir(networkId, snapshotId)
        .resolve(BfConsts.RELPATH_OUTPUT)
        .resolve(BfConsts.RELPATH_TESTRIG_POJO_TOPOLOGY_PATH);
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
    return _d.getSnapshotOutputDir(network, snapshot)
        .resolve(toBase64(workId + BfConsts.SUFFIX_LOG_FILE));
  }

  @Nonnull
  private Path getWorkJsonPath(NetworkId network, SnapshotId snapshot, String workId) {
    return _d.getSnapshotOutputDir(network, snapshot)
        .resolve(toBase64(workId + BfConsts.SUFFIX_ANSWER_JSON_FILE));
  }

  @Override
  public @Nonnull String loadInitialTopology(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path path = getEnvTopologyPath(networkId, snapshotId);
    return readFileToString(path, UTF_8);
  }

  private @Nonnull Path getEnvTopologyPath(NetworkId networkId, SnapshotId snapshotId) {
    return _d.getSnapshotDir(networkId, snapshotId)
        .resolve(BfConsts.RELPATH_OUTPUT)
        .resolve(BfConsts.RELPATH_ENV_TOPOLOGY_FILE);
  }

  @Override
  public void storeInitialTopology(Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path path = getEnvTopologyPath(networkId, snapshotId);
    path.getParent().toFile().mkdirs();
    writeStringToFile(path, BatfishObjectMapper.writeString(topology), UTF_8);
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
    String completionMetadataStr = readFileToString(completionMetadataPath, UTF_8);
    return BatfishObjectMapper.mapper()
        .readValue(completionMetadataStr, new TypeReference<CompletionMetadata>() {});
  }

  @Override
  public void storeCompletionMetadata(
      CompletionMetadata completionMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path completionMetadataPath = getSnapshotCompletionMetadataPath(networkId, snapshotId);
    mkdirs(completionMetadataPath.getParent());
    writeFile(completionMetadataPath, BatfishObjectMapper.writeString(completionMetadata), UTF_8);
  }

  private @Nonnull Path getSnapshotCompletionMetadataPath(
      NetworkId networkId, SnapshotId snapshotId) {
    return _d.getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_COMPLETION_METADATA_FILE);
  }

  /**
   * Make specified directory along with any parent directories if they do not already exist.
   *
   * @param dir directory to create
   * @throws IOException if there is an error creating the directories
   */
  @VisibleForTesting
  static void mkdirs(Path dir) throws IOException {
    if (!dir.toFile().mkdirs() && !dir.toFile().exists()) {
      throw new IOException(String.format("Unable to create directory '%s'", dir));
    }
  }

  private static void deleteDirectory(Path path) throws IOException {
    FileUtils.deleteDirectory(path.toFile());
  }

  private static void deleteIfExists(Path path) throws IOException {
    Files.deleteIfExists(path);
  }

  private static @Nonnull String readFileToString(Path file, Charset charset) throws IOException {
    return FileUtils.readFileToString(file.toFile(), charset);
  }

  private static void writeFile(Path file, CharSequence data, Charset charset) throws IOException {
    Path tmpFile = Files.createTempFile(null, null);
    try {
      FileUtils.write(tmpFile.toFile(), data, charset);
      mkdirs(file.getParent());
      Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      deleteIfExists(tmpFile);
    }
  }

  private static void writeStringToFile(Path file, String data, Charset charset)
      throws IOException {
    Path tmpFile = Files.createTempFile(null, null);
    try {
      FileUtils.writeStringToFile(tmpFile.toFile(), data, charset);
      mkdirs(file.getParent());
      Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      deleteIfExists(tmpFile);
    }
  }

  @Override
  public @Nonnull BgpTopology loadBgpTopology(NetworkSnapshot networkSnapshot) throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(readFileToString(getBgpTopologyPath(networkSnapshot), UTF_8), BgpTopology.class);
  }

  @Override
  public @Nonnull EigrpTopology loadEigrpTopology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(
            readFileToString(getEigrpTopologyPath(networkSnapshot), UTF_8), EigrpTopology.class);
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
        BatfishObjectMapper.mapper()
            .readValue(readFileToString(sl1tPath, UTF_8), Layer1Topology.class));
  }

  @Override
  public @Nonnull Optional<Layer2Topology> loadLayer2Topology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return Optional.ofNullable(
        BatfishObjectMapper.mapper()
            .readValue(
                readFileToString(getLayer2TopologyPath(networkSnapshot), UTF_8),
                Layer2Topology.class));
  }

  @Override
  public @Nonnull Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(readFileToString(getLayer3TopologyPath(networkSnapshot), UTF_8), Topology.class);
  }

  @Override
  public @Nonnull OspfTopology loadOspfTopology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(
            readFileToString(getOspfTopologyPath(networkSnapshot), UTF_8), OspfTopology.class);
  }

  @Override
  public @Nonnull VxlanTopology loadVxlanTopology(NetworkSnapshot networkSnapshot)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(
            readFileToString(getVxlanTopologyPath(networkSnapshot), UTF_8), VxlanTopology.class);
  }

  @Override
  public void storeBgpTopology(BgpTopology bgpTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getBgpTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeFile(path, BatfishObjectMapper.writeString(bgpTopology), UTF_8);
  }

  @Override
  public void storeEigrpTopology(EigrpTopology eigrpTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getEigrpTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeFile(path, BatfishObjectMapper.writeString(eigrpTopology), UTF_8);
  }

  @Override
  public void storeLayer2Topology(
      Optional<Layer2Topology> layer2Topology, NetworkSnapshot networkSnapshot) throws IOException {
    Path path = getLayer2TopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeFile(path, BatfishObjectMapper.writeString(layer2Topology.orElse(null)), UTF_8);
  }

  @Override
  public void storeLayer3Topology(Topology layer3Topology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getLayer3TopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeFile(path, BatfishObjectMapper.writeString(layer3Topology), UTF_8);
  }

  @Override
  public void storeOspfTopology(OspfTopology ospfTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getOspfTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeFile(path, BatfishObjectMapper.writeString(ospfTopology), UTF_8);
  }

  @Override
  public void storeVxlanTopology(VxlanTopology vxlanTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getVxlanTopologyPath(networkSnapshot);
    mkdirs(path.getParent());
    writeFile(path, BatfishObjectMapper.writeString(vxlanTopology), UTF_8);
  }

  @VisibleForTesting
  void storeSynthesizedLayer1Topology(
      Layer1Topology synthesizedLayer1Topology, NetworkId network, SnapshotId snapshot)
      throws IOException {
    Path sl1tPath = getSynthesizedLayer1TopologyPath(network, snapshot);
    mkdirs(sl1tPath.getParent());
    writeFile(sl1tPath, BatfishObjectMapper.writeString(synthesizedLayer1Topology), UTF_8);
  }

  @Override
  public @Nonnull String readId(Class<? extends Id> idType, String name, Id... ancestors)
      throws IOException {
    return readFileToString(getIdFile(idType, name, ancestors), UTF_8);
  }

  @Override
  public void writeId(Id id, String name, Id... ancestors) throws IOException {
    Path file = getIdFile(id.getClass(), name, ancestors);
    mkdirs(file.getParent());
    writeStringToFile(file, id.getId(), UTF_8);
  }

  @Override
  public void deleteNameIdMapping(Class<? extends Id> type, String name, Id... ancestors)
      throws IOException {
    Files.delete(getIdFile(type, name, ancestors));
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
    try (Stream<Path> files = Files.list(idsDir)) {
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

  private static String toIdDirName(Class<? extends Id> type) {
    return toBase64(type.getCanonicalName());
  }

  private @Nonnull Path getIdsDir(Class<? extends Id> type, Id... ancestors) {
    Path file = _d.getStorageBase().resolve("ids");
    for (Id id : ancestors) {
      file = file.resolve(toIdDirName(id.getClass())).resolve(id.getId());
    }
    return file.resolve(toIdDirName(type));
  }

  private @Nonnull Path getIdFile(Class<? extends Id> type, String name, Id... ancestors) {
    return getIdsDir(type, ancestors).resolve(toBase64(name + ID_EXTENSION));
  }
}
