package org.batfish.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;

public class FileStorageUtils {

  private final Path _containerLocation;

  public FileStorageUtils(Path containerLocation) {
    _containerLocation = containerLocation;
  }

  /**
   * Finds the full path using the base path  and all directories specified
   *
   * @param basePath Base directory
   * @param dirs Names of directories to be resolved
   * @return Resolved path
   */
  public Path resolvePath(Path basePath, String... dirs) {
    if (dirs.length == 0) {
      return basePath;
    }
    return basePath.resolve(String.join(File.separator, dirs));
  }

  /**
   * Gets the container path using the set container location
   *
   * @param containerName Container name
   * @return Resolved path of container
   */
  public Path getContainerPath(String containerName) {
    return resolvePath(_containerLocation, containerName);
  }

  /**
   * Gets analysis full path within the given container
   *
   * @param containerName Container Name
   * @param analysisName  Name of Analysis within the container
   * @return Resolved path of analysis
   */
  public Path getAnalysisPath(String containerName, String analysisName) {
    return resolvePath(
        _containerLocation, containerName, BfConsts.RELPATH_ANALYSES_DIR, analysisName);
  }

  /**
   * Gets all questions in an analysis in a given container
   *
   * @param containerName Container Name
   * @param analysisName  Name of analysis within the container
   * @return Paths of all questions in the analysis
   */
  public SortedSet<Path> getAnalysisQuestions(String containerName, String analysisName) {
    Path analysisPath = getAnalysisPath(containerName, analysisName);
    Path questionsDir = resolvePath(analysisPath, BfConsts.RELPATH_QUESTIONS_DIR);
    if (!Files.exists(questionsDir)) {
      throw new BatfishException(
          String.format("Analysis '%s' doesn't contain questions directory", analysisName));
    }
    return CommonUtil.getEntries(questionsDir);
  }

  /**
   * Gets the full path of a question in a analysis of the given container
   *
   * @param containerName Container Name
   * @param analysisName  Name of the analysis within the container
   * @param questionName  Name of the question within the analysis
   * @return Resolved path of the question
   */
  public Path getQuestionPath(String containerName, String analysisName, String questionName) {
    return resolvePath(
        getAnalysisPath(containerName, analysisName), BfConsts.RELPATH_QUESTIONS_DIR, questionName);
  }

  /**
   * Checks if analysis is not having any questions
   *
   * @param containerName Container Name
   * @param analysisName  Name of Analysis within the container
   * @return true if analysis is empty, false otherwise
   */
  public boolean isAnalysisEmpty(String containerName, String analysisName) {
    return resolvePath(getAnalysisPath(containerName, analysisName), BfConsts.RELPATH_QUESTIONS_DIR)
            .toFile()
            .list()
            .length
        == 0;
  }
}
