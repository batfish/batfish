package org.batfish.storage;

import static org.batfish.common.plugin.PluginConsumer.DEFAULT_HEADER_LENGTH_BYTES;
import static org.batfish.common.plugin.PluginConsumer.detectFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import com.google.common.io.Closer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Version;
import org.batfish.common.plugin.PluginConsumer.Format;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

/** A utility class that abstracts the underlying file system storage used by {@link Batfish}. */
@ParametersAreNonnullByDefault
public final class FileBasedStorage implements StorageProvider {
  private final Path _baseDir;
  private final BatfishLogger _logger;
  private final BiFunction<String, Integer, AtomicInteger> _newBatch;

  /**
   * Create a new {@link FileBasedStorage} instance that uses the given root path as a container.
   */
  public FileBasedStorage(
      Path baseDir, BatfishLogger logger, BiFunction<String, Integer, AtomicInteger> newBatch) {
    _baseDir = baseDir;
    _logger = logger;
    _newBatch = newBatch;
  }

  /**
   * Returns the compressed configuration files for the given testrig. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Override
  @Nullable
  public SortedMap<String, Configuration> loadCompressedConfigurations(
      String network, String snapshot) {
    Path testrigDir = getSnapshotDir(network, snapshot);
    Path indepDir = testrigDir.resolve(BfConsts.RELPATH_COMPRESSED_CONFIG_DIR);
    return loadConfigurations(network, snapshot, indepDir);
  }

  /**
   * Returns the configuration files for the given testrig. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Override
  @Nullable
  public SortedMap<String, Configuration> loadConfigurations(String network, String snapshot) {
    Path testrigDir = getSnapshotDir(network, snapshot);
    Path indepDir = testrigDir.resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);
    return loadConfigurations(network, snapshot, indepDir);
  }

  private @Nullable SortedMap<String, Configuration> loadConfigurations(
      String network, String snapshot, Path indepDir) {
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
      String network, String snapshot) {
    Path ccaePath = getSnapshotDir(network, snapshot).resolve(BfConsts.RELPATH_CONVERT_ANSWER_PATH);
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
  public @Nullable Topology loadLegacyTopology(String network, String snapshot) {
    Path path =
        getSnapshotDir(network, snapshot)
            .resolve(
                Paths.get(
                    BfConsts.RELPATH_TEST_RIG_DIR, BfConsts.RELPATH_TESTRIG_LEGACY_TOPOLOGY_PATH));
    if (!Files.exists(path)) {
      return null;
    }
    AtomicInteger counter = _newBatch.apply("Reading legacy topology", 1);
    String topologyFileText = CommonUtil.readFile(path);
    try {
      return BatfishObjectMapper.mapper().readValue(topologyFileText, Topology.class);
    } catch (IOException e) {
      _logger.warnf(
          "Unexpected exception caught while loading legacy testrig topology for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return null;
    } finally {
      counter.incrementAndGet();
    }
  }

  @Override
  public @Nullable Layer1Topology loadLayer1Topology(String network, String snapshot) {
    Path path =
        getSnapshotDir(network, snapshot)
            .resolve(
                Paths.get(
                    BfConsts.RELPATH_TEST_RIG_DIR, BfConsts.RELPATH_TESTRIG_L1_TOPOLOGY_PATH));
    if (!Files.exists(path)) {
      return null;
    }
    AtomicInteger counter = _newBatch.apply("Reading layer-1 topology", 1);
    String topologyFileText = CommonUtil.readFile(path);
    try {
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

  /**
   * Stores the configurations into the compressed config path for the given testrig. Will replace
   * any previously-stored compressed configurations.
   */
  @Override
  public void storeCompressedConfigurations(
      Map<String, Configuration> configurations, String network, String snapshot) {
    Path testrigDir = getSnapshotDir(network, snapshot);

    if (!testrigDir.toFile().exists() && !testrigDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Unable to create testrig directory '%s'", testrigDir));
    }

    Path outputDir = testrigDir.resolve(BfConsts.RELPATH_COMPRESSED_CONFIG_DIR);

    String batchName =
        String.format(
            "Serializing %s compressed configuration structures for snapshot %s",
            configurations.size(), snapshot);
    storeConfigurations(outputDir, batchName, configurations);
  }

  /**
   * Stores the configuration information into the given testrig. Will replace any previously-stored
   * configurations.
   */
  @Override
  public void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      String network,
      String snapshot) {
    Path testrigDir = getSnapshotDir(network, snapshot);
    if (!testrigDir.toFile().exists() && !testrigDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Unable to create testrig directory '%s'", testrigDir));
    }

    // Save the convert configuration answer element.
    Path ccaePath = testrigDir.resolve(BfConsts.RELPATH_CONVERT_ANSWER_PATH);
    CommonUtil.deleteIfExists(ccaePath);
    serializeObject(convertAnswerElement, ccaePath);

    Path outputDir = testrigDir.resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);

    String batchName =
        String.format(
            "Serializing %s vendor-independent configuration structures for snapshot %s",
            configurations.size(), snapshot);

    storeConfigurations(outputDir, batchName, configurations);
  }

  private void storeConfigurations(
      Path outputDir, String batchName, Map<String, Configuration> configurations) {
    _logger.infof("\n*** %s***\n", batchName.toUpperCase());
    AtomicInteger progressCount = _newBatch.apply(batchName, configurations.size());

    // Delete any existing output, then recreate.
    CommonUtil.deleteDirectory(outputDir);
    if (!outputDir.toFile().exists() && !outputDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Unable to create output directory '%s'", outputDir));
    }

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

  private @Nonnull Path getAnswerDir(
      String network,
      String baseSnapshot,
      @Nullable String deltaSnapshot,
      @Nullable String analysis,
      String question) {
    return deltaSnapshot != null
        ? getDeltaAnswerDir(network, baseSnapshot, deltaSnapshot, analysis, question)
        : getStandardAnswerDir(network, baseSnapshot, analysis, question);
  }

  private @Nonnull Path getStandardAnswerDir(
      String network, String snapshot, @Nullable String analysis, String question) {
    Path snapshotDir = getSnapshotDir(network, snapshot);
    Path dirWithQuestions =
        analysis != null
            ? snapshotDir.resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve(analysis)
            : snapshotDir;
    return dirWithQuestions
        .resolve(BfConsts.RELPATH_QUESTIONS_DIR)
        .resolve(question)
        .resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR)
        .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME);
  }

  private @Nonnull Path getDeltaAnswerDir(
      String network,
      String baseSnapshot,
      String deltaSnapshot,
      @Nullable String analysis,
      String question) {
    Path snapshotDir = getSnapshotDir(network, baseSnapshot);
    Path dirWithQuestions =
        analysis != null
            ? snapshotDir.resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve(analysis)
            : snapshotDir;
    return dirWithQuestions
        .resolve(BfConsts.RELPATH_QUESTIONS_DIR)
        .resolve(question)
        .resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR)
        .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
        .resolve(BfConsts.RELPATH_DELTA)
        .resolve(deltaSnapshot)
        .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME);
  }

  @Override
  public void storeAnswer(
      String answerStr,
      String network,
      String snapshot,
      String question,
      @Nullable String referenceSnapshot,
      @Nullable String analysis) {
    Path answerDir = getAnswerDir(network, snapshot, referenceSnapshot, analysis, question);
    // If settings has neither a question nor an analysis configured, don't write a file
    if (answerDir == null) {
      return;
    }
    Path answerPath = answerDir.resolve(BfConsts.RELPATH_ANSWER_JSON);
    answerDir.toFile().mkdirs();
    CommonUtil.writeFile(answerPath, answerStr);
  }

  @Override
  public void storeAnswerMetadata(
      AnswerMetadata answerMetrics,
      String network,
      String snapshot,
      String question,
      @Nullable String referenceSnapshot,
      @Nullable String analysis) {
    String metricsStr;
    try {
      metricsStr = BatfishObjectMapper.writePrettyString(answerMetrics);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Could not write answer metrics", e);
    }
    Path answerDir = getAnswerDir(network, snapshot, referenceSnapshot, analysis, question);
    if (answerDir == null) {
      // If settings has neither a question nor an analysis configured, don't write a file
      return;
    }
    answerDir.toFile().mkdirs();
    if (!answerDir.toFile().exists() && !answerDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Unable to create a answer metadata directory '%s'", answerDir));
    }
    Path answerMetricsPath = answerDir.resolve(BfConsts.RELPATH_ANSWER_METADATA);
    CommonUtil.writeFile(answerMetricsPath, metricsStr);
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
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
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
    } catch (IOException e) {
      throw new BatfishException("Failed to serialize object to output file: " + outputFile, e);
    }
  }

  private boolean cachedConfigsAreCompatible(String network, String snapshot) {
    try {
      ConvertConfigurationAnswerElement ccae =
          loadConvertConfigurationAnswerElement(network, snapshot);
      return ccae != null
          && Version.isCompatibleVersion(
              FileBasedStorage.class.getCanonicalName(),
              "Old processed configurations",
              ccae.getVersion());
    } catch (BatfishException e) {
      _logger.warnf(
          "Unexpected exception caught while deserializing configs for snapshot %s: %s",
          snapshot, Throwables.getStackTraceAsString(e));
      return false;
    }
  }

  private @Nonnull Path getNetworkAnalysisDir(String network, String analysis) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve(analysis);
  }

  private @Nonnull Path getAnalysisQuestionDir(String network, String analysis, String question) {
    return getNetworkAnalysisDir(network, analysis)
        .resolve(BfConsts.RELPATH_QUESTIONS_DIR)
        .resolve(question);
  }

  private @Nonnull Path getAdHocQuestionDir(String network, String question) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve(question);
  }

  private @Nonnull Path getNetworkDir(String network) {
    return _baseDir.resolve(network);
  }

  private @Nonnull Path getSnapshotDir(String network, String snapshot) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve(snapshot);
  }

  private @Nonnull Path getQuestionDir(String network, @Nullable String analysis, String question) {
    return analysis != null
        ? getAnalysisQuestionDir(network, analysis, question)
        : getAdHocQuestionDir(network, question);
  }

  @Override
  public @Nonnull String loadQuestion(String network, @Nullable String analysis, String question) {
    Path questionPath =
        getQuestionDir(network, analysis, question).resolve(BfConsts.RELPATH_QUESTION_FILE);
    return CommonUtil.readFile(questionPath);
  }
}
