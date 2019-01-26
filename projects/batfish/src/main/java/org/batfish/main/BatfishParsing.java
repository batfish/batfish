package org.batfish.main;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.SortedMap;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;

/** A utility class for the parsing-related code in {@link Batfish}. */
class BatfishParsing {
  /**
   * Reads the files in the given directory (recursively) and returns a map from each file's {@link
   * Path} to its contents.
   *
   * <p>Temporary files (files start with {@code .} are omitted from the returned list.
   *
   * <p>This method follows all symbolic links.
   */
  static SortedMap<Path, String> readAllFiles(Path directory, BatfishLogger logger) {
    try (Stream<Path> paths = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> !path.getFileName().toString().startsWith("."))
          .map(
              path -> {
                logger.debugf("Reading: \"%s\"\n", path);
                String fileText = CommonUtil.readFile(path.toAbsolutePath());
                if (!fileText.isEmpty()) {
                  // Adding a trailing newline helps EOF in some parsers.
                  fileText += '\n';
                }
                return new SimpleEntry<>(path, fileText);
              })
          .collect(
              ImmutableSortedMap.toImmutableSortedMap(
                  Ordering.natural(), SimpleEntry::getKey, SimpleEntry::getValue));
    } catch (IOException e) {
      throw new BatfishException("Failed to walk path: " + directory, e);
    }
  }

  private BatfishParsing() {} // prevent instantiation of utility class.
}
