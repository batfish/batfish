package org.batfish.main;

import static org.batfish.main.StreamDecoder.decodeStreamAndAppendNewline;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.SortedMap;
import java.util.stream.Stream;
import org.batfish.common.BatfishLogger;

/** Utility functions for internal CLI tools */
final class CliUtils {

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
  static SortedMap<Path, String> readAllFiles(Path directory, BatfishLogger logger)
      throws IOException {
    try (Stream<Path> paths = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> !path.getFileName().toString().startsWith("."))
          .map(
              path -> {
                logger.debugf("Reading: \"%s\"\n", path);
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

  private CliUtils() {}
}
