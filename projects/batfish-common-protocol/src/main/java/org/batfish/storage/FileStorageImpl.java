package org.batfish.storage;

import static org.batfish.storage.StorageUtils.resolvePath;

import com.google.common.collect.Sets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.pojo.Analysis;

public class FileStorageImpl implements Storage {

  private final Path _containersLocation;
  private final StorageUtils _utils;

  public FileStorageImpl(Path containersLocation) throws BatfishException {
    try {
      if (containersLocation != null) {
        _containersLocation = containersLocation;
        _containersLocation.toFile().mkdirs();
        _utils = new StorageUtils(_containersLocation);
      } else {
        throw new BatfishException("container location is null");
      }
    } catch (InvalidPathException e) {
      throw new BatfishException("cannot resolve containers location '" + containersLocation + "'");
    }
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
      Path questionFile = resolvePath(questionDir, BfConsts.RELPATH_QUESTION_FILE);
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
      Path questionFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
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
}
