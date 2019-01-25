package org.batfish.main;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;

class BatfishParsing {
  /**
   * Returns a sorted list of {@link Path paths} contains all files under the directory indicated by
   * {@code configsPath}. Directories under {@code configsPath} are recursively expanded but not
   * included in the returned list.
   *
   * <p>Temporary files(files start with {@code .} are omitted from the returned list.
   *
   * <p>This method follows all symbolic links.
   */
  static List<Path> listAllFiles(Path configsPath) {
    List<Path> configFilePaths;
    try (Stream<Path> allFiles = Files.walk(configsPath, FileVisitOption.FOLLOW_LINKS)) {
      configFilePaths =
          allFiles
              .filter(
                  path ->
                      !path.getFileName().toString().startsWith(".") && Files.isRegularFile(path))
              .sorted()
              .collect(Collectors.toList());
    } catch (IOException e) {
      throw new BatfishException("Failed to walk path: " + configsPath, e);
    }
    return configFilePaths;
  }

  /**
   * Reads the files in the given directory (recursively) and returns a map from each file's {@link
   * Path} to its contents.
   */
  static SortedMap<Path, String> readFiles(Path directory, BatfishLogger logger) {
    try (Stream<Path> paths = CommonUtil.list(directory)) {
      return paths
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
    }
  }

  private BatfishParsing() {} // prevent instantiation of utility class.
}
