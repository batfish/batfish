package org.batfish.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;

public class StorageUtils {

  private final Path _containerLocation;

  public StorageUtils(Path containerLocation) {
    _containerLocation = containerLocation;
  }

  public static Path resolvePath(Path basePath, String... dir) {
    if (dir.length == 0) {
      return basePath;
    }
    return basePath.resolve("/".join("/", dir));
  }

  public Path getContainerPath(String containerName) {
    return resolvePath(_containerLocation, containerName);
  }

  public Path getAnalysisPath(String containerName, String analysisName) {
    return resolvePath(
        _containerLocation, containerName, BfConsts.RELPATH_ANALYSES_DIR, analysisName);
  }

  public SortedSet<Path> getAnalysisQuestions(String containerName, String analysisName) {
    Path analysisPath = getAnalysisPath(containerName, analysisName);
    Path questionsDir = resolvePath(analysisPath, BfConsts.RELPATH_QUESTIONS_DIR);
    if (!Files.exists(questionsDir)) {
      throw new BatfishException(
          String.format("Analysis '%s' doesn't contain questions directory", analysisName));
    }
    return CommonUtil.getEntries(questionsDir);
  }

  public Path getQuestionPath(String containerName, String analysisName, String questionName) {
    return resolvePath(
        getAnalysisPath(containerName, analysisName), BfConsts.RELPATH_QUESTIONS_DIR, questionName);
  }

  public boolean isAnalysisEmpty(String containerName, String analysisName) {
    return resolvePath(getAnalysisPath(containerName, analysisName), BfConsts.RELPATH_QUESTIONS_DIR)
            .toFile()
            .list()
            .length
        == 0;
  }
}
