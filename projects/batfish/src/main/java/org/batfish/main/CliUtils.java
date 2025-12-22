package org.batfish.main;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.main.StreamDecoder.decodeStreamAndAppendNewline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishLogger;

/** Utility functions for internal CLI tools */
@ParametersAreNonnullByDefault
public final class CliUtils {

  private static final Logger LOGGER = LogManager.getLogger(CliUtils.class);

  /**
   * Reads the files in the given directory (recursively) and returns a map from each file's {@link
   * Path} to its contents.
   *
   * <p>Temporary files (files start with {@code .} are omitted from the returned list.
   *
   * <p>This method follows all symbolic links.
   *
   * @throws IOException if there is an error
   */
  static @Nonnull SortedMap<Path, String> readAllFiles(Path directory, BatfishLogger logger)
      throws IOException {
    try (Stream<Path> paths = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> !path.getFileName().toString().startsWith("."))
          .parallel()
          .map(
              path -> {
                if (logger != null) {
                  // TODO: remove BatfishLogger
                  logger.debugf("Reading: \"%s\"\n", path);
                } else {
                  LOGGER.debug("Reading file: '{}'", path);
                }
                try (InputStream inputStream = Files.newInputStream(path)) {
                  return new SimpleEntry<>(path, decodeStreamAndAppendNewline(inputStream));
                } catch (IOException e) {
                  throw new UncheckedIOException(String.format("Failed to read file: %s", path), e);
                }
              })
          .collect(
              ImmutableSortedMap.toImmutableSortedMap(
                  Ordering.natural(), SimpleEntry::getKey, SimpleEntry::getValue));
    }
  }

  /**
   * Reads the files in the given directory (recursively) and returns a map from each file's {@link
   * Path} to its contents.
   *
   * <p>Temporary files (files start with {@code .} are omitted from the returned list.
   *
   * <p>This method follows all symbolic links.
   *
   * @throws IOException if there is an error
   */
  public static @Nonnull SortedMap<Path, String> readAllFiles(Path directory) throws IOException {
    return readAllFiles(directory, null);
  }

  /**
   * For each entry in {@code outputDataByPath}, writes the data given by the value to the path
   * given by the key. Creates parent directories as needed.
   *
   * <p>This method follows all symbolic links.
   *
   * @throws IOException if there is an error
   */
  public static void writeAllFiles(Map<Path, String> outputDataByPath) throws IOException {
    for (Entry<Path, String> outputDataEntry : outputDataByPath.entrySet()) {
      Path outputPath = outputDataEntry.getKey();
      LOGGER.debug("Writing: {}", outputPath);
      outputPath.getParent().toFile().mkdirs();
      MoreFiles.asCharSink(outputPath, UTF_8).write(outputDataEntry.getValue());
    }
  }

  public static @Nonnull <T> Map<Path, T> relativize(Path basePath, Map<Path, T> pathKeyedMap) {
    ImmutableMap.Builder<Path, T> builder =
        ImmutableMap.builderWithExpectedSize(pathKeyedMap.size());
    pathKeyedMap.forEach(
        (absolutePath, data) -> builder.put(basePath.relativize(absolutePath), data));
    return builder.build();
  }

  public static @Nonnull <T> Map<Path, T> resolve(Path basePath, Map<Path, T> pathKeyedMap) {
    ImmutableMap.Builder<Path, T> builder =
        ImmutableMap.builderWithExpectedSize(pathKeyedMap.size());
    pathKeyedMap.forEach((relativePath, data) -> builder.put(basePath.resolve(relativePath), data));
    return builder.build();
  }

  private CliUtils() {}
}
