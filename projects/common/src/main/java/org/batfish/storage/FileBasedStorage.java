package org.batfish.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Streams.stream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.BfConsts.RELPATH_INPUT;
import static org.batfish.common.BfConsts.RELPATH_ISP_CONFIG_FILE;
import static org.batfish.common.plugin.PluginConsumer.DEFAULT_HEADER_LENGTH_BYTES;
import static org.batfish.common.plugin.PluginConsumer.detectFormat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.io.Closer;
import com.google.common.io.MoreFiles;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
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
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.PluginConsumer.Format;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.ZipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspConfigurationException;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.Id;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;

/** A utility class that abstracts the underlying file system storage used by Batfish. */
@ParametersAreNonnullByDefault
@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
public class FileBasedStorage implements StorageProvider {
  private static final Logger LOGGER = LogManager.getLogger(FileBasedStorage.class);

  @VisibleForTesting static final Duration GC_SKEW_ALLOWANCE = Duration.ofMinutes(10L);
  private static final String ID_EXTENSION = ".id";
  private static final String SUFFIX_LOG_FILE = ".log";
  private static final String SUFFIX_ANSWER_JSON_FILE = ".json";
  private static final String RELPATH_COMPLETION_METADATA_FILE = "completion_metadata.json";
  private static final String RELPATH_BGP_TOPOLOGY = "bgp_topology.json";
  private static final String RELPATH_EIGRP_TOPOLOGY = "eigrp_topology.json";
  private static final String RELPATH_SYNTHESIZED_LAYER1_TOPOLOGY =
      "synthesized_layer1_topology.json";
  private static final String RELPATH_LAYER3_TOPOLOGY = "layer3_topology.json";
  private static final String RELPATH_L3_ADJACENCIES = "l3_adjacencies";
  private static final String RELPATH_OSPF_TOPOLOGY = "ospf_topology.json";
  private static final String RELPATH_VXLAN_TOPOLOGY = "vxlan_topology.json";
  private static final String RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR = "indep";
  private static final String RELPATH_QUESTIONS_DIR = "questions";
  private static final String RELPATH_TESTRIG_POJO_TOPOLOGY_PATH = "testrig_pojo_topology";
  private static final String RELPATH_ORIGINAL_DIR = "original";
  private static final String RELPATH_METADATA_FILE = "metadata.json";
  private static final String RELPATH_FORK_REQUEST_FILE = "fork_request";
  private static final String RELPATH_ENV_TOPOLOGY_FILE = "env_topology";
  private static final String RELPATH_CONVERSION_CONTEXT = "conversion_context";
  private static final String RELPATH_CONVERT_ANSWER_PATH = "convert_answer";
  private static final String RELPATH_ANSWERS_DIR = "answers";
  private static final String RELPATH_ANSWER_METADATA = "answer_metadata.json";
  private static final String RELPATH_ANSWER_JSON = "answer.json";
  private static final String RELPATH_BATFISH_CONFIGS_DIR = "batfish";
  private static final String RELPATH_SNAPSHOT_ZIP_FILE = "snapshot.zip";
  private static final String RELPATH_DATA_PLANE = "dp";
  private static final String RELPATH_DATA_PLANE_FORWARDING_ANALYSIS = "forwarding_analysis";
  private static final String RELPATH_SERIALIZED_ENVIRONMENT_BGP_TABLES = "bgp_processed";
  private static final String RELPATH_ENVIRONMENT_BGP_TABLES_ANSWER = "bgp_answer";
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

  /** Zip {@code srcFolder} in memory and return input stream from which zip may be read. */
  private static @MustBeClosed @Nonnull InputStream zipFilesToInputStream(Path srcFolder) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ZipUtility.zipToStream(srcFolder, baos);
    } catch (Exception e) {
      throw new BatfishException("Could not zip folder: '" + srcFolder + "'", e);
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /**
   * Returns the configuration files for the given testrig. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Override
  public @Nullable SortedMap<String, Configuration> loadConfigurations(
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
  public @Nonnull ConversionContext loadConversionContext(NetworkSnapshot snapshot)
      throws IOException {
    Path ccPath = getConversionContextPath(snapshot.getNetwork(), snapshot.getSnapshot());
    if (!Files.exists(ccPath)) {
      throw new FileNotFoundException();
    }
    try {
      return deserializeObject(ccPath, ConversionContext.class);
    } catch (BatfishException e) {
      throw new IOException(
          String.format(
              "Failed to deserialize ConversionContext: %s", Throwables.getStackTraceAsString(e)));
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
      LOGGER.error("Failed to deserialize ConvertConfigurationAnswerElement", e);
      return null;
    }
  }

  @Override
  public @Nullable SortedSet<NodeInterfacePair> loadInterfaceBlacklist(
      NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(
            RELPATH_INPUT, RELPATH_BATFISH_CONFIGS_DIR, BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
    Path topLevel = Paths.get(RELPATH_INPUT, BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE);
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
      LOGGER.warn(
          "Unexpected exception caught while loading interface blacklist for snapshot {}",
          snapshot,
          e);
      return null;
    }
  }

  @VisibleForTesting
  static final String ISP_CONFIGURATION_KEY =
      String.format("%s/%s", RELPATH_BATFISH_CONFIGS_DIR, RELPATH_ISP_CONFIG_FILE);

  @VisibleForTesting
  static boolean keyInDir(String key, String dirName) {
    return key.startsWith(dirName + "/");
  }

  @Override
  public @Nullable IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot)
      throws IspConfigurationException {
    try (InputStream inputStream =
        loadSnapshotInputObject(network, snapshot, ISP_CONFIGURATION_KEY)) {
      return BatfishObjectMapper.mapper().readValue(inputStream, IspConfiguration.class);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      throw new IspConfigurationException(
          String.format(
              "Could not parse the content of %s. (Is it valid JSON? Does it have the right"
                  + " information?): %s",
              ISP_CONFIGURATION_KEY, e.getMessage()),
          e);
    }
  }

  @Override
  public @Nullable SortedSet<String> loadNodeBlacklist(NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(RELPATH_INPUT, RELPATH_BATFISH_CONFIGS_DIR, BfConsts.RELPATH_NODE_BLACKLIST_FILE);
    Path topLevel = Paths.get(RELPATH_INPUT, BfConsts.RELPATH_NODE_BLACKLIST_FILE);
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
      LOGGER.warn(
          "Unexpected exception caught while loading node blacklist for snapshot {}", snapshot, e);
      return null;
    }
  }

  @Override
  public @Nullable Layer1Topology loadLayer1Topology(NetworkId network, SnapshotId snapshot) {
    // Prefer runtime data inside of batfish/ subfolder over top level
    Path insideBatfish =
        Paths.get(RELPATH_INPUT, RELPATH_BATFISH_CONFIGS_DIR, BfConsts.RELPATH_L1_TOPOLOGY_PATH);
    Path topLevel = Paths.get(RELPATH_INPUT, BfConsts.RELPATH_L1_TOPOLOGY_PATH);
    Path deprecated = Paths.get(RELPATH_INPUT, "testrig_layer1_topology");
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
      LOGGER.warn(
          "Unexpected exception caught while loading layer-1 topology for snapshot {}",
          snapshot,
          e);
      return null;
    } finally {
      counter.incrementAndGet();
    }
  }

  @Override
  public @Nonnull String loadWorkLog(NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    Path filePath = getWorkLogPath(network, snapshot, workId);
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(
          String.format("Could not find log file for work ID: %s", workId));
    }
    return readFileToString(filePath, UTF_8);
  }

  @Override
  public @Nonnull String loadWorkJson(NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    Path filePath = getWorkJsonPath(network, snapshot, workId);
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(
          String.format("Could not find work json for work ID: %s", workId));
    }
    return readFileToString(filePath, UTF_8);
  }

  @Override
  public @Nullable SnapshotRuntimeData loadRuntimeData(NetworkId network, SnapshotId snapshot) {
    Path path =
        getSnapshotDir(network, snapshot)
            .resolve(
                Paths.get(
                    RELPATH_INPUT,
                    RELPATH_BATFISH_CONFIGS_DIR,
                    BfConsts.RELPATH_RUNTIME_DATA_FILE));
    if (!Files.exists(path)) {
      return null;
    }

    AtomicInteger counter = _newBatch.apply("Reading runtime data", 1);
    try {
      return BatfishObjectMapper.mapper().readValue(path.toFile(), SnapshotRuntimeData.class);
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading runtime data for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      LOGGER.warn(
          "Unexpected exception caught while loading runtime data for snapshot {}", snapshot, e);
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

  @Override
  public void storeConversionContext(ConversionContext conversionContext, NetworkSnapshot snapshot)
      throws IOException {
    Path ccPath = getConversionContextPath(snapshot.getNetwork(), snapshot.getSnapshot());
    mkdirs(ccPath.getParent());
    serializeObject(conversionContext, ccPath);
  }

  @VisibleForTesting
  @Nonnull
  Path getConversionContextPath(NetworkId network, SnapshotId snapshot) {
    return getSnapshotOutputDir(network, snapshot).resolve(RELPATH_CONVERSION_CONTEXT);
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

    configurations.entrySet().parallelStream()
        .forEach(
            e -> {
              Path currentOutputPath = outputDir.resolve(e.getKey());
              serializeObject(e.getValue(), currentOutputPath);
              progressCount.incrementAndGet();
            });
  }

  @Override
  public void storeAnswer(
      NetworkId network, SnapshotId snapshot, String answerStr, AnswerId answerId)
      throws IOException {
    Path answerPath = getAnswerPath(network, snapshot, answerId);
    mkdirs(answerPath.getParent());
    writeStringToFile(answerPath, answerStr, UTF_8);
  }

  @Override
  public void storeAnswerMetadata(
      NetworkId networkId, SnapshotId snapshotId, AnswerMetadata answerMetadata, AnswerId answerId)
      throws IOException {
    Path answerMetadataPath = getAnswerMetadataPath(networkId, snapshotId, answerId);
    mkdirs(answerMetadataPath.getParent());
    writeJsonFile(answerMetadataPath, answerMetadata);
  }

  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer
  private <S extends Serializable> S deserializeObjectUnchecked(Path inputFile)
      throws BatfishException {
    Path sanitizedInputFile = validatePath(inputFile);
    try (Closer closer = Closer.create()) {
      FileInputStream fis = closer.register(new FileInputStream(sanitizedInputFile.toFile()));
      PushbackInputStream pbstream =
          closer.register(new PushbackInputStream(fis, DEFAULT_HEADER_LENGTH_BYTES));
      Format f = detectFormat(pbstream);
      InputStream ois;
      if (f == Format.GZIP) {
        ois = closer.register(new GZIPInputStream(pbstream, 8192 /* enlarge buffer */));
      } else if (f == Format.LZ4) {
        ois = closer.register(new LZ4FrameInputStream(pbstream));
      } else if (f == Format.JAVA_SERIALIZED) {
        ois = pbstream;
      } else {
        throw new BatfishException(
            String.format("Could not detect format of the file %s", sanitizedInputFile));
      }
      return SerializationUtils.deserialize(ois);
    } catch (Exception e) {
      throw new BatfishException(
          String.format("Failed to deserialize object from file %s", sanitizedInputFile), e);
    }
  }

  /**
   * Returns a single object of the given class deserialized from the given file. Uses the {@link
   * FileBasedStorage} default file encoding including serialization format and compression.
   */
  private <S extends Serializable> S deserializeObject(Path inputFile, Class<S> outputClass)
      throws BatfishException {
    try {
      return outputClass.cast(deserializeObjectUnchecked(inputFile));
    } catch (ClassCastException e) {
      Path sanitizedInputFile = validatePath(inputFile);
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
        namesByPath.entrySet().parallelStream()
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

  private static @Nonnull Path tempOutputFilePath(@Nonnull Path outputFile) {
    return outputFile.resolveSibling("." + outputFile.getFileName() + ".tmp");
  }

  /**
   * Writes a single object of the given class to the given file. Uses the {@link FileBasedStorage}
   * default file encoding including serialization format and compression.
   */
  @VisibleForTesting
  void serializeObject(Serializable object, Path outputFile) {
    Path sanitizedOutputFile = validatePath(outputFile);
    try {
      Path tmpFile = tempOutputFilePath(outputFile);
      try {
        mkdirs(sanitizedOutputFile.getParent());
        try (OutputStream out = Files.newOutputStream(tmpFile);
            LZ4FrameOutputStream gos = new LZ4FrameOutputStream(out);
            ObjectOutputStream oos = new ObjectOutputStream(gos)) {
          oos.writeObject(object);
        } catch (Throwable e) {
          throw new BatfishException(
              "Failed to serialize object to output file: " + sanitizedOutputFile, e);
        }
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
    objectsByPath.entrySet().parallelStream()
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
      LOGGER.warn(
          "Unexpected exception caught while deserializing configs for snapshot {}", snapshot, e);
      return false;
    }
  }

  @Override
  public @Nonnull String loadQuestion(NetworkId network, QuestionId question) throws IOException {
    return readFileToString(getQuestionPath(network, question), UTF_8);
  }

  @Override
  public boolean checkQuestionExists(NetworkId network, QuestionId question) {
    return Files.exists(getQuestionPath(network, question));
  }

  private @Nonnull Path getQuestionPath(NetworkId network, QuestionId question) {
    return getAdHocQuestionDir(network, question).resolve(BfConsts.RELPATH_QUESTION_FILE);
  }

  @Override
  public @Nonnull String loadAnswer(NetworkId networkId, SnapshotId snapshotId, AnswerId answerId)
      throws IOException {
    Path answerPath = getAnswerPath(networkId, snapshotId, answerId);
    if (Files.exists(answerPath)) {
      return readFileToString(answerPath, UTF_8);
    }
    // look for the answer in the legacy location
    Path oldAnswerPath = getOldAnswerPath(answerId);
    if (Files.exists(oldAnswerPath)) {
      return readFileToString(oldAnswerPath, UTF_8);
    }
    throw new FileNotFoundException(String.format("Could not find answer with ID: %s", answerId));
  }

  @Override
  public @Nonnull AnswerMetadata loadAnswerMetadata(
      NetworkId networkId, SnapshotId snapshotId, AnswerId answerId) throws IOException {
    Path answerMetadataPath = getAnswerMetadataPath(networkId, snapshotId, answerId);
    if (Files.exists(answerMetadataPath)) {
      return BatfishObjectMapper.mapper()
          .readValue(answerMetadataPath.toFile(), new TypeReference<AnswerMetadata>() {});
    }
    // look for the answer metadata in the legacy location
    Path oldAnswerMetadataPath = getOldAnswerMetadataPath(answerId);
    if (Files.exists(oldAnswerMetadataPath)) {
      return BatfishObjectMapper.mapper()
          .readValue(oldAnswerMetadataPath.toFile(), new TypeReference<AnswerMetadata>() {});
    }
    throw new FileNotFoundException(
        String.format("Could not find answer metadata for ID: %s", answerId));
  }

  @VisibleForTesting
  @Nonnull
  Path getAnswerPath(NetworkId networkId, SnapshotId snapshotId, AnswerId answerId) {
    return getAnswerDir(networkId, snapshotId, answerId).resolve(RELPATH_ANSWER_JSON);
  }

  @VisibleForTesting
  @Nonnull
  Path getOldAnswerPath(AnswerId answerId) {
    return getOldAnswerDir(answerId).resolve(RELPATH_ANSWER_JSON);
  }

  private @Nonnull Path getAnswerMetadataPath(
      NetworkId networkId, SnapshotId snapshotId, AnswerId answerId) {
    return getAnswerDir(networkId, snapshotId, answerId).resolve(RELPATH_ANSWER_METADATA);
  }

  @Nonnull
  Path getOldAnswerMetadataPath(AnswerId answerId) {
    return getOldAnswerDir(answerId).resolve(RELPATH_ANSWER_METADATA);
  }

  @Override
  public void storeQuestion(String questionStr, NetworkId network, QuestionId question)
      throws IOException {
    Path questionPath = getQuestionPath(network, question);
    mkdirs(questionPath.getParent());
    writeStringToFile(questionPath, questionStr, UTF_8);
  }

  @Override
  public boolean checkNetworkExists(NetworkId network) {
    return Files.exists(getNetworkDir(network));
  }

  @Override
  public boolean hasAnswerMetadata(NetworkId networkId, SnapshotId snapshotId, AnswerId answerId) {
    return Files.exists(getAnswerMetadataPath(networkId, snapshotId, answerId))
        || Files.exists(getOldAnswerMetadataPath(answerId));
  }

  @Override
  public String loadQuestionClassId(NetworkId networkId, QuestionId questionId) throws IOException {
    return Question.parseQuestion(loadQuestion(networkId, questionId)).getName();
  }

  private @Nonnull Path getSnapshotMetadataPath(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_METADATA_FILE);
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
  public void storeNodeRoles(
      NetworkId networkId, NodeRolesData nodeRolesData, NodeRolesId nodeRolesId)
      throws IOException {
    writeJsonFile(getNodeRolesPath(networkId, nodeRolesId), nodeRolesData);
  }

  private @Nonnull Path getNodeRolesPath(NetworkId networkId, NodeRolesId nodeRolesId) {
    return getNodeRolesDir(networkId).resolve(String.format("%s%s", nodeRolesId.getId(), ".json"));
  }

  @VisibleForTesting
  @Nonnull
  Path getOldNodeRolesPath(NodeRolesId nodeRolesId) {
    return getOldNodeRolesDir().resolve(String.format("%s%s", nodeRolesId.getId(), ".json"));
  }

  @Override
  public String loadNodeRoles(NetworkId networkId, NodeRolesId nodeRolesId) throws IOException {
    Path nodeRolesPath = getNodeRolesPath(networkId, nodeRolesId);
    if (Files.exists(nodeRolesPath)) {
      return readFileToString(getNodeRolesPath(networkId, nodeRolesId), UTF_8);
    }
    // try at the legacy location
    return readFileToString(getOldNodeRolesPath(nodeRolesId), UTF_8);
  }

  @Override
  public boolean hasNodeRoles(NetworkId networkId, NodeRolesId nodeRolesId) {
    return Files.exists(getNodeRolesPath(networkId, nodeRolesId))
        || Files.exists(getOldNodeRolesPath(nodeRolesId));
  }

  @Override
  public void initNetwork(NetworkId networkId) {
    getNetworkDir(networkId).toFile().mkdirs();
  }

  @Override
  public void deleteAnswerMetadata(NetworkId networkId, SnapshotId snapshotId, AnswerId answerId)
      throws FileNotFoundException, IOException {
    Files.delete(getAnswerMetadataPath(networkId, snapshotId, answerId));
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
  @SuppressWarnings("PMD.UseTryWithResources") // syntax is awkward to close stream you don't open
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

  @VisibleForTesting
  @Nonnull
  Path getNetworkBlobPath(NetworkId networkId, String key) {
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
  @SuppressWarnings("PMD.UseTryWithResources") // syntax is awkward to close stream you don't open
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
  @SuppressWarnings("PMD.UseTryWithResources") // syntax is awkward to close stream you don't open
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
        ? zipFilesToInputStream(objectPath)
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

  private @Nonnull Path getLayer3TopologyPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_LAYER3_TOPOLOGY);
  }

  private @Nonnull Path getL3AdjacenciesPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_L3_ADJACENCIES);
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

  private @Nonnull Path getWorkLogPath(NetworkId network, SnapshotId snapshot, String workId) {
    return getSnapshotOutputDir(network, snapshot).resolve(toBase64(workId + SUFFIX_LOG_FILE));
  }

  private @Nonnull Path getWorkJsonPath(NetworkId network, SnapshotId snapshot, String workId) {
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
    writeJsonFile(path, topology);
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
    return deserializeObject(completionMetadataPath, CompletionMetadata.class);
  }

  @Override
  public void storeCompletionMetadata(
      CompletionMetadata completionMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    Path completionMetadataPath = getSnapshotCompletionMetadataPath(networkId, snapshotId);
    mkdirs(completionMetadataPath.getParent());
    serializeObject(completionMetadata, completionMetadataPath);
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
  private @Nonnull Stream<Path> list(Path path) throws IOException {
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
    try {
      return Files.readString(sanitizedFile, charset);
    } catch (NoSuchFileException e) {
      // Convert to FileNotFoundException to maintain API contract
      throw new FileNotFoundException(sanitizedFile.toString());
    }
  }

  @VisibleForTesting
  void writeStringToFile(Path file, CharSequence data, Charset charset) throws IOException {
    Path sanitizedFile = validatePath(file);
    Path tmpFile = tempOutputFilePath(file);
    try {
      mkdirs(sanitizedFile.getParent());
      MoreFiles.asCharSink(tmpFile, charset).write(data);
      Files.move(tmpFile, sanitizedFile, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  private void writeJsonFile(Path file, @Nullable Object json) throws IOException {
    Path sanitizedFile = validatePath(file);
    Path tmpFile = tempOutputFilePath(file);
    try {
      mkdirs(sanitizedFile.getParent());
      BatfishObjectMapper.writer().writeValue(tmpFile.toFile(), json);
      Files.move(tmpFile, sanitizedFile, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  private void writeStreamToFile(InputStream inputStream, Path outputFile) throws IOException {
    Path sanitizedOutputFile = validatePath(outputFile);
    Path tmpFile = tempOutputFilePath(outputFile);
    mkdirs(sanitizedOutputFile.getParent());
    try (OutputStream fileOutputStream = Files.newOutputStream(tmpFile)) {
      int read = 0;
      byte[] bytes = new byte[STREAMED_FILE_BUFFER_SIZE];
      while ((read = inputStream.read(bytes)) != -1) {
        fileOutputStream.write(bytes, 0, read);
      }
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

  @Override
  public @Nonnull Optional<Layer1Topology> loadSynthesizedLayer1Topology(NetworkSnapshot snapshot)
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
  public @Nonnull Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(getLayer3TopologyPath(networkSnapshot).toFile(), Topology.class);
  }

  @Override
  public @Nonnull L3Adjacencies loadL3Adjacencies(NetworkSnapshot networkSnapshot)
      throws IOException {
    return deserializeObject(getL3AdjacenciesPath(networkSnapshot), L3Adjacencies.class);
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
  public void storeL3Adjacencies(L3Adjacencies l3Adjacencies, NetworkSnapshot networkSnapshot)
      throws IOException {
    Path path = getL3AdjacenciesPath(networkSnapshot);
    mkdirs(path.getParent());
    serializeObject(l3Adjacencies, path);
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

  /** Returns ids (as strings) that can ever be returned via a name-to-id mapping */
  private @Nonnull Set<String> listResolvedIds(Class<? extends Id> type, Id... ancestors)
      throws IOException {
    return listResolvableNames(type, ancestors).stream()
        .map(
            name -> {
              try {
                return readId(type, name, ancestors);
              } catch (IOException e) {
                _logger.errorf("Could not read id for '%s' (type '%s')", name, type);
                return Optional.<String>empty();
              }
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public @Nonnull Optional<ReferenceLibrary> loadReferenceLibrary(NetworkId network)
      throws IOException {
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
  @Override
  public @Nonnull InputStream loadUploadSnapshotZip(String key, NetworkId network)
      throws IOException {
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
  @Override
  public @Nonnull Stream<String> listSnapshotInputObjectKeys(NetworkSnapshot snapshot)
      throws IOException {
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

  @Override
  public @Nonnull DataPlane loadDataPlane(NetworkSnapshot snapshot) throws IOException {
    Map<Path, String> namesByPath = new TreeMap<>();
    Path dataplanePath = getDataPlanePath(snapshot);
    try (DirectoryStream<Path> hostDataPlanes = Files.newDirectoryStream(dataplanePath)) {
      for (Path hostDataPlane : hostDataPlanes) {
        String name = hostDataPlane.getFileName().toString();
        if (name.equals(RELPATH_DATA_PLANE_FORWARDING_ANALYSIS)) {
          continue;
        }
        namesByPath.put(hostDataPlane, fromBase64(name));
      }
    } catch (IOException e) {
      throw new BatfishException("Error reading data plane directory", e);
    }
    Map<String, PerHostDataPlane> perNodeDataPlanes =
        deserializeObjects(namesByPath, PerHostDataPlane.class);
    ForwardingAnalysis forwardingAnalysis =
        deserializeObjectUnchecked(getDataPlaneForwardingAnalysisPath(snapshot));
    return new SimpleFieldsDataPlane(perNodeDataPlanes, forwardingAnalysis);
  }

  @Override
  public void storeDataPlane(DataPlane dataPlane, NetworkSnapshot snapshot) throws IOException {
    dataPlane.getFibs().keySet().parallelStream()
        .forEach(
            hostname -> {
              PerHostDataPlane dp =
                  new PerHostDataPlane(
                      dataPlane.getBgpRoutes().row(hostname),
                      dataPlane.getBgpBackupRoutes().row(hostname),
                      dataPlane.getEvpnRoutes().row(hostname),
                      dataPlane.getEvpnBackupRoutes().row(hostname),
                      dataPlane.getFibs().get(hostname),
                      dataPlane.getLayer2Vnis().row(hostname),
                      dataPlane.getLayer3Vnis().row(hostname),
                      dataPlane.getPrefixTracingInfoSummary().get(hostname),
                      dataPlane.getRibs().row(hostname));
              serializeObject(dp, getDataPlaneHostPath(snapshot, hostname));
            });
    serializeObject(
        dataPlane.getForwardingAnalysis(), getDataPlaneForwardingAnalysisPath(snapshot));
  }

  @Override
  public boolean hasDataPlane(NetworkSnapshot snapshot) throws IOException {
    return Files.exists(getDataPlanePath(snapshot));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputEnvironmentBgpTableKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> key.startsWith(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES));
  }

  @Override
  public @Nonnull ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
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

  @Override
  public @Nonnull Map<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(
      NetworkSnapshot snapshot) throws IOException {
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

  @Override
  public @Nonnull Optional<String> loadExternalBgpAnnouncementsFile(NetworkSnapshot snapshot)
      throws IOException {
    Path path =
        getSnapshotInputObjectPath(
            snapshot.getNetwork(),
            snapshot.getSnapshot(),
            BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS);
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

  private @Nonnull Path getDataPlaneHostPath(NetworkSnapshot snapshot, String hostname) {
    return getDataPlanePath(snapshot).resolve(toBase64(hostname));
  }

  private @Nonnull Path getDataPlaneForwardingAnalysisPath(NetworkSnapshot snapshot) {
    return getDataPlanePath(snapshot).resolve(RELPATH_DATA_PLANE_FORWARDING_ANALYSIS);
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

  @VisibleForTesting
  @Nonnull
  Path getAnswersDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotOutputDir(networkId, snapshotId).resolve(RELPATH_ANSWERS_DIR);
  }

  private @Nonnull Path getOldAnswersDir() {
    return _baseDir.resolve(RELPATH_ANSWERS_DIR);
  }

  @VisibleForTesting
  @Nonnull
  Path getAnswerDir(NetworkId networkId, SnapshotId snapshotId, AnswerId answerId) {
    return getAnswersDir(networkId, snapshotId).resolve(answerId.getId());
  }

  private @Nonnull Path getOldAnswerDir(AnswerId answerId) {
    return getOldAnswersDir().resolve(answerId.getId());
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

  private Path getNodeRolesDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(RELPATH_NODE_ROLES_DIR);
  }

  private Path getOldNodeRolesDir() {
    return _baseDir.resolve(RELPATH_NODE_ROLES_DIR);
  }

  @VisibleForTesting
  @Nonnull
  Path getSnapshotsDir(NetworkId network) {
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

  @VisibleForTesting
  Path getNetworkBlobsDir(NetworkId networkId) {
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
    return getSnapshotDir(networkId, snapshotId).resolve(RELPATH_INPUT);
  }

  @VisibleForTesting
  Path getSnapshotOutputDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(RELPATH_OUTPUT);
  }

  @Override
  public @Nonnull ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
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

  @Override
  public @Nonnull Map<String, VendorConfiguration> loadVendorConfigurations(
      NetworkSnapshot snapshot) throws IOException {
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
  @Override
  public @Nonnull Stream<String> listInputHostConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, BfConsts.RELPATH_HOST_CONFIGS_DIR));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputNetworkConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, BfConsts.RELPATH_CONFIGURATIONS_DIR));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputAwsMultiAccountKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, RELPATH_AWS_ACCOUNTS_DIR));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputAwsSingleAccountKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, BfConsts.RELPATH_AWS_CONFIGS_DIR));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputAzureSingleAccountKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, BfConsts.RELPATH_AZURE_CONFIGS_DIR));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputCheckpointManagementKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, BfConsts.RELPATH_CHECKPOINT_MANAGEMENT_DIR));
  }

  @MustBeClosed
  @Override
  public @Nonnull Stream<String> listInputSonicConfigsKeys(NetworkSnapshot snapshot)
      throws IOException {
    return listSnapshotInputObjectKeys(snapshot)
        .filter(key -> keyInDir(key, BfConsts.RELPATH_SONIC_CONFIGS_DIR));
  }

  private @Nonnull Path getParseVendorConfigurationAnswerElementPath(NetworkSnapshot snapshot) {
    return getSnapshotOutputDir(snapshot.getNetwork(), snapshot.getSnapshot())
        .resolve(RELPATH_PARSE_ANSWER_PATH);
  }

  /**
   * Collects garbage inside the given network.
   *
   * <p>May delete things like non-existent snapshots, but does not delete the network itself.
   */
  private void garbageCollectNetwork(NetworkId networkId, Instant expungeBeforeDate)
      throws IOException {
    for (Path dir : getSnapshotDirsToExpunge(networkId, expungeBeforeDate)) {
      try {
        deleteDirectory(dir);
      } catch (IOException e) {
        _logger.errorf(
            "Failed to expunge snapshot directory '%s': %s",
            dir, Throwables.getStackTraceAsString(e));
        LOGGER.error("Failed to expunge snapshot directory {}", dir, e);
      }
    }
    Optional<Instant> maybeOldestExtantSnapshotFileModifiedDate =
        getOldestSnapshotCreationTime(networkId);
    if (maybeOldestExtantSnapshotFileModifiedDate.isPresent()) {
      Instant snapshotMetadataBasedExpungeBeforeDate =
          maybeOldestExtantSnapshotFileModifiedDate.get().minus(GC_SKEW_ALLOWANCE);
      Instant blobExpungeBeforeDate =
          Comparators.min(snapshotMetadataBasedExpungeBeforeDate, expungeBeforeDate);
      expungeOldBlobs(networkId, blobExpungeBeforeDate);
    } // else no point expunging blobs if this network has no snapshots
  }

  /** Expunge blobs in a network older than the provided date. */
  private void expungeOldBlobs(NetworkId networkId, Instant blobExpungeBeforeDate) {
    Path blobPath = getNetworkBlobsDir(networkId);
    if (!Files.exists(blobPath)) {
      return;
    }
    try (Stream<Path> blobs = Files.walk(blobPath)) {
      blobs.forEach(
          path -> {
            try {
              if (Files.isRegularFile(path)
                  && getLastModifiedTime(path).isBefore(blobExpungeBeforeDate)) {
                Files.delete(path);
              }
            } catch (IOException e) {
              LOGGER.error(
                  "Failed to expunge blob '{}' of networkId '{}'",
                  path.getFileName(),
                  networkId,
                  e);
            }
          });
    } catch (IOException e) {
      LOGGER.error("Failed to expunge blobs from networkId '{}'", networkId, e);
    }
  }

  @VisibleForTesting
  @Nonnull
  Optional<Instant> getOldestSnapshotCreationTime(NetworkId networkId) {
    try {
      return listResolvedIds(SnapshotId.class, networkId).stream()
          .map(SnapshotId::new)
          .map(
              snapshotId -> {
                try {
                  return Optional.of(
                      BatfishObjectMapper.mapper()
                          .readValue(
                              loadSnapshotMetadata(networkId, snapshotId), SnapshotMetadata.class));
                } catch (IOException e) {
                  LOGGER.error(
                      "Error loading snapshot metadata for networkId '{}' snapshotId '{}'",
                      networkId,
                      snapshotId,
                      e);
                  // Just skip this snapshot
                  return Optional.<SnapshotMetadata>empty();
                }
              })
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(SnapshotMetadata::getCreationTimestamp)
          .min(Comparator.naturalOrder());
    } catch (IOException e) {
      // Just skip this network
      return Optional.empty();
    }
  }

  /**
   * Get the oldest last madified time of any directory entry that is a descendant of provided path.
   * Return {@link Optional#empty()} if path does not exist or there are errors fetching last
   * modified time of all descendants.
   */
  private @Nonnull Optional<Instant> getOldestEntryLastModifiedDate(Path path) {
    try (Stream<Path> paths = Files.walk(path)) {
      return paths
          .map(
              p -> {
                try {
                  return Optional.of(getLastModifiedTime(p));
                } catch (IOException e) {
                  LOGGER.error("Failed to get last modified time of '{}'", p, e);

                  return Optional.<Instant>empty();
                }
              })
          .filter(Optional::isPresent)
          .map(Optional::get)
          .min(Comparator.naturalOrder());
    } catch (IOException e) {
      LOGGER.error("Failed to get oldest recursive entry of path rooted at '{}'", path, e);

      return Optional.empty();
    }
  }

  @Override
  public void runGarbageCollection() throws IOException {
    if (!exists(getNetworksDir())) {
      // There are no networks, nothing to do.
      return;
    }

    // Go back GC_SKEW_ALLOWANCE, so we under-approximate data to delete. This helps minimize race
    // conditions with in-progress or queued work.
    Instant expungeBeforeDate = Instant.now().minus(GC_SKEW_ALLOWANCE);

    Set<NetworkId> extantNetworkIds =
        listResolvedIds(NetworkId.class).stream()
            .map(NetworkId::new)
            .collect(ImmutableSet.toImmutableSet());
    Set<NetworkId> storageNetworkIds;
    try (Stream<Path> networkDirStream = list(getNetworksDir())) {
      // storageNetworkIds listed from disk will contain both existing AND deleted networks.
      storageNetworkIds =
          networkDirStream
              .map(networkDir -> new NetworkId(networkDir.getFileName().toString()))
              .collect(Collectors.toSet());
    }

    for (NetworkId networkId : Sets.intersection(storageNetworkIds, extantNetworkIds)) {
      // GC inside networks that exist.
      garbageCollectNetwork(networkId, expungeBeforeDate);
    }
    for (NetworkId networkId : Sets.difference(storageNetworkIds, extantNetworkIds)) {
      if (!canExpungeNetwork(networkId, expungeBeforeDate)) {
        // Too new, may be being created.
        continue;
      }
      // Delete networks that do not exist.
      Path dir = getNetworkDir(networkId);
      try {
        deleteDirectory(dir);
      } catch (IOException e) {
        _logger.errorf(
            "Failed to expunge network directory '%s': %s",
            dir, Throwables.getStackTraceAsString(e));
        LOGGER.error("Failed to expunge network directory {}", dir, e);
      }
    }
  }

  private List<Path> getSnapshotDirsToExpunge(NetworkId networkId, Instant expungeBeforeDate)
      throws IOException {
    // the directory may not exist if snapshots were never initialized in the network
    if (!Files.exists(getSnapshotsDir(networkId))) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<Path> snapshotDirsToDelete = ImmutableList.builder();
    Set<String> extantSnapshotIds = listResolvedIds(SnapshotId.class, networkId);
    try (Stream<Path> snapshotsDirStream = list(getSnapshotsDir(networkId))) {
      snapshotsDirStream
          .map(snapshotDir -> new SnapshotId(snapshotDir.getFileName().toString()))
          .filter(snapshotId -> !extantSnapshotIds.contains(snapshotId.toString()))
          .forEach(
              snapshotId -> {
                if (canExpungeSnapshot(networkId, snapshotId, expungeBeforeDate)) {
                  snapshotDirsToDelete.add(getSnapshotDir(networkId, snapshotId));
                }
              });
    }
    return snapshotDirsToDelete.build();
  }

  /**
   * Returns if it is safe to delete this network's folder, based on the last modified times of
   * itself, its subdirs, and its snapshots.
   */
  @VisibleForTesting
  boolean canExpungeNetwork(NetworkId networkId, Instant expungeBeforeDate) throws IOException {
    Path networkDir = getNetworkDir(networkId);
    try (Stream<Path> subdirs = list(networkDir)) {
      if (!canExpunge(expungeBeforeDate, Streams.concat(Stream.of(networkDir), subdirs))) {
        return false;
      }
    }
    // the directory may not exist if snapshots were never initialized in the network
    if (!Files.exists(getSnapshotsDir(networkId))) {
      return true;
    }
    try (Stream<Path> snapshotsDirStream = list(getSnapshotsDir(networkId))) {
      return snapshotsDirStream
          .map(snapshotDir -> new SnapshotId(snapshotDir.getFileName().toString()))
          .allMatch(snapshotId -> canExpungeSnapshot(networkId, snapshotId, expungeBeforeDate));
    }
  }

  /**
   * Returns if it is safe to delete this snapshot's folder, based on the last modified time of its
   * input, output, and answers.
   */
  @VisibleForTesting
  boolean canExpungeSnapshot(
      NetworkId networkId, SnapshotId snapshotId, Instant expungeBeforeDate) {
    return canExpunge(
        expungeBeforeDate,
        Stream.of(
            getSnapshotDir(networkId, snapshotId),
            getSnapshotInputObjectsDir(networkId, snapshotId),
            getSnapshotOutputDir(networkId, snapshotId),
            getAnswersDir(networkId, snapshotId)));
  }

  /**
   * Returns if all paths in {@code pathStream} have a last modified time less than the {@code
   * expungeBeforeDate}..
   */
  private boolean canExpunge(Instant expungeBeforeDate, Stream<Path> pathStream) {
    return pathStream
        .map(
            path -> {
              try {
                return getLastModifiedTime(path);
              } catch (IOException e) {
                // If for some reason the last modified time of the entry cannot be fetched
                // (e.g. it was just deleted), ignore this path.
                return Instant.MIN;
              }
            })
        .allMatch(lmTime -> lmTime.compareTo(expungeBeforeDate) < 0);
  }
}
