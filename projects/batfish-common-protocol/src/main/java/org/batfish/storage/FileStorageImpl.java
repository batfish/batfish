package org.batfish.storage;

import com.google.common.collect.Sets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.pojo.Analysis;

public class FileStorageImpl implements Storage {

  private final Path _containersLocation;

  public FileStorageImpl(Path containersLocation) throws BatfishException {
    try {
      if (containersLocation != null) {
        _containersLocation = containersLocation;
        _containersLocation.toFile().mkdir();
      } else {
        throw new BatfishException("container location is null");
      }
    } catch (InvalidPathException e) {
      throw new BatfishException("cannot resolve containers location '" + containersLocation + "'");
    }
  }

  /**
   * Retrieve a container Path
   *
   * @param containerName Container name
   * @return Path of the container
   */
  // deprecated
  @Override
  public Path getContainerPath(String containerName) {
    Path containerDir = _containersLocation.resolve(containerName).toAbsolutePath();
    if (!Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' not found");
    }
    return containerDir;
  }

  @Override
  public Analysis getAnalysis(String containerName, String analysisName) {
    Path aDir =
        _containersLocation.resolve(
            Paths.get(containerName, BfConsts.RELPATH_ANALYSES_DIR, analysisName));
    if (!Files.exists(aDir)) {
      throw new BatfishException(
          String.format(
              "Analysis '%s' doesn't exist for container '%s'", analysisName, containerName));
    }
    Path questionsDir = aDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    if (!Files.exists(questionsDir)) {
      throw new BatfishException(
          String.format("Analysis '%s' doesn't contain questions directory", analysisName));
    }
    Map<String, String> questions = new HashMap<>();
    for (Path questionDir : CommonUtil.getEntries(questionsDir)) {
      Path questionFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      if (!Files.exists(questionFile)) {
        throw new BatfishException(
            String.format(
                "Failed to read analysis '%s', question empty: '%s'",
                analysisName, questionDir.getFileName()));
      }
      questions.put(
          questionDir.getFileName().toString(),
          CommonUtil.readFile(questionFile));
    }
    return new Analysis(analysisName, questions);
  }

  @Override
  public Analysis saveAnalysis(String containerName, Analysis analysis) {
    Path aDir =
        _containersLocation.resolve(
            Paths.get(containerName, BfConsts.RELPATH_ANALYSES_DIR, analysis.getName()));
    if (Files.exists(aDir)) {
      throw new BatfishException(
          String.format(
              "Analysis '%s' already exists for container '%s'",
              analysis.getName(), containerName));
    }
    //trying to create analysis skeleton dir with questions dir
    if (!aDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR).toFile().mkdirs()) {
      throw new BatfishException(String.format("Failed to create analysis directory '%s'", aDir));
    }

    for (String questionName : analysis.getQuestions().keySet()) {
      Path questionDir = aDir.resolve(Paths.get(BfConsts.RELPATH_QUESTIONS_DIR, questionName));
      if (!questionDir.toFile().mkdirs()) {
        throw new BatfishException(
            String.format("Failed to create question directory '%s'", questionDir));
      }
      Path questionFile = questionDir.resolve(BfConsts.RELPATH_QUESTION_FILE);
      CommonUtil.writeFile(questionFile, analysis.getQuestions().get(questionName));
    }

    return getAnalysis(containerName, analysis.getName());
  }

  @Override
  public Analysis updateAnalysis(String containerName, Analysis analysis) {
    Analysis oldAnalysisObj = getAnalysis(containerName, analysis.getName());
    Set<String> questionsToDelete =
        Sets.difference(oldAnalysisObj.getQuestions().keySet(), analysis.getQuestions().keySet());
    Set<String> questionsToAdd =
        Sets.difference(analysis.getQuestions().keySet(), oldAnalysisObj.getQuestions().keySet());
    Path questionsDir =
        _containersLocation.resolve(
            Paths.get(
                containerName,
                BfConsts.RELPATH_ANALYSES_DIR,
                analysis.getName(),
                BfConsts.RELPATH_QUESTIONS_DIR));
    for (String question : questionsToDelete) {
      try {
        CommonUtil.deleteDirectory(questionsDir.resolve(question));
      } catch (BatfishException e) {
        throw new BatfishException(String.format("Could not delete question '%s'", question));
      }
    }
    for (String question : questionsToAdd) {
      if (!questionsDir.resolve(question).toFile().mkdirs()) {
        throw new BatfishException(
            String.format("Failed to create question directory '%s'", question));
      }

      CommonUtil.writeFile(
          questionsDir.resolve(Paths.get(question, BfConsts.RELPATH_QUESTION_FILE)),
          analysis.getQuestions().get(question));
    }
    return getAnalysis(containerName, analysis.getName());
  }

  @Override
  public boolean deleteAnalysis(String containerName, String analysisName, boolean force) {
    Path aDir =
        _containersLocation.resolve(
            Paths.get(containerName, BfConsts.RELPATH_ANALYSES_DIR, analysisName));
    if (!Files.exists(aDir)) {
      return false;
    }
    if (aDir.resolve(BfConsts.RELPATH_QUESTIONS_DIR).toFile().list().length > 0 && !force) {
      throw new BatfishException(
          String.format("'%s' is not empty, deletion must be forced", analysisName));
    }
    CommonUtil.deleteDirectory(aDir);
    return true;
  }
}
