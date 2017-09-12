package org.batfish.storage;

import com.google.common.collect.Sets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.pojo.Analysis;

public class FileStorageImpl implements Storage {

  private final Path _containersLocation;
  private final FileStorageUtils _utils;

  private FileStorageImpl(Path containersLocation) throws BatfishException {
    try {
      if (containersLocation != null) {
        _containersLocation = containersLocation;
        _containersLocation.toFile().mkdirs();
        _utils = new FileStorageUtils(_containersLocation);
      } else {
        throw new BatfishException("container location is null");
      }
    } catch (InvalidPathException e) {
      throw new BatfishException("cannot resolve containers location '" + containersLocation + "'");
    }
  }

  /**
   * Gets a new instance of File Storage implementation using specified settings
   *
   * @param settings Settings for initializing storage. Contains containers location in property
   *     name {@link BfConsts#PROP_CONTAINER_LOCATION}
   * @return {@link FileStorageImpl}
   */
  public static Storage create(Map<String, Object> settings) {
    return new FileStorageImpl((Path) settings.get(BfConsts.PROP_CONTAINER_LOCATION));
  }

  /**
   * Get an Analysis object
   *
   * @param containerName Parent container
   * @param analysisName Name of analysis
   * @return Analysis from storage
   */
  @Override
  public Analysis getAnalysis(String containerName, String analysisName) {
    Path aDir = _utils.getAnalysisPath(containerName, analysisName);
    if (!Files.exists(aDir)) {
      throw new BatfishException(
          String.format(
              "Analysis '%s' doesn't exist for container '%s'", analysisName, containerName));
    }

    Map<String, String> questions = new HashMap<>();
    for (Path questionDir : _utils.getAnalysisQuestions(containerName, analysisName)) {
      Path questionFile = _utils.resolvePath(questionDir, BfConsts.RELPATH_QUESTION_FILE);
      if (!Files.exists(questionFile)) {
        throw new BatfishException(
            String.format(
                "Failed to read analysis '%s', question empty: '%s'",
                analysisName, questionDir.getFileName()));
      }
      questions.put(questionDir.getFileName().toString(), CommonUtil.readFile(questionFile));
    }
    return new Analysis(analysisName, questions);
  }

  /**
   * Save an Analysis object
   *
   * @param containerName Parent container
   * @param analysis Analysis object to be saved
   * @return Persisted copy of analysis object
   */
  @Override
  public Analysis saveAnalysis(String containerName, Analysis analysis) {
    Path aDir = _utils.getAnalysisPath(containerName, analysis.getName());
    if (Files.exists(aDir)) {
      throw new BatfishException(
          String.format(
              "Analysis '%s' already exists for container '%s'",
              analysis.getName(), containerName));
    }
    //trying to create analysis skeleton dir with questions dir
    if (!_utils.resolvePath(aDir, BfConsts.RELPATH_QUESTIONS_DIR).toFile().mkdirs()) {
      throw new BatfishException(String.format("Failed to create analysis directory '%s'", aDir));
    }

    for (String questionName : analysis.getQuestions().keySet()) {
      Path questionDir = _utils.getQuestionPath(containerName, analysis.getName(), questionName);
      if (!questionDir.toFile().mkdirs()) {
        throw new BatfishException(
            String.format("Failed to create question directory '%s'", questionDir));
      }
      Path questionFile = _utils.resolvePath(questionDir, BfConsts.RELPATH_QUESTION_FILE);
      CommonUtil.writeFile(questionFile, analysis.getQuestions().get(questionName));
    }

    return getAnalysis(containerName, analysis.getName());
  }

  /**
   * Update an Analysis object
   *
   * @param containerName Parent container
   * @param analysis Analysis object to be updated
   * @return Updated analysis object from storage
   */
  @Override
  public Analysis updateAnalysis(String containerName, Analysis analysis) {
    Analysis oldAnalysisObj = getAnalysis(containerName, analysis.getName());
    Set<String> questionsToDelete =
        Sets.difference(oldAnalysisObj.getQuestions().keySet(), analysis.getQuestions().keySet());
    Set<String> questionsToAdd =
        Sets.difference(analysis.getQuestions().keySet(), oldAnalysisObj.getQuestions().keySet());

    for (String question : questionsToDelete) {
      try {
        CommonUtil.deleteDirectory(
            _utils.getQuestionPath(containerName, analysis.getName(), question));
      } catch (BatfishException e) {
        throw new BatfishException(String.format("Could not delete question '%s'", question));
      }
    }
    for (String question : questionsToAdd) {
      Path questionDir = _utils.getQuestionPath(containerName, analysis.getName(), question);
      if (!questionDir.toFile().mkdirs()) {
        throw new BatfishException(
            String.format("Failed to create question directory '%s'", question));
      }

      CommonUtil.writeFile(
          _utils.resolvePath(questionDir, BfConsts.RELPATH_QUESTION_FILE),
          analysis.getQuestions().get(question));
    }
    return getAnalysis(containerName, analysis.getName());
  }

  /**
   * Delete an Analysis object
   *
   * @param containerName Parent container
   * @param analysisName Name of analysis to be deleted
   * @param force Force deletion of non empty analysis
   * @return true if analysis deleted, false if it does not exist
   */
  @Override
  public boolean deleteAnalysis(String containerName, String analysisName, boolean force) {
    Path aDir = _utils.getAnalysisPath(containerName, analysisName);
    if (!Files.exists(aDir)) {
      return false;
    }
    if (!_utils.isAnalysisEmpty(containerName, analysisName) && !force) {
      throw new BatfishException(
          String.format("'%s' is not empty, deletion must be forced", analysisName));
    }
    CommonUtil.deleteDirectory(aDir);
    return true;
  }

  /**
   * List all analysis names
   *
   * @param containerName Parent container
   * @return Names of all analysis in this container
   */
  @Override
  public List<String> listAnalyses(String containerName) {
    Path analysisDir =
        _utils.resolvePath(_utils.getContainerPath(containerName), BfConsts.RELPATH_ANALYSES_DIR);
    if (!Files.exists(analysisDir)) {
      return new ArrayList<>();
    }
    List<String> analysisNames = new ArrayList<>();
    CommonUtil.getSubdirectories(analysisDir)
        .forEach(analysisPath -> analysisNames.add(analysisPath.getFileName().toString()));
    return analysisNames;
  }
}
