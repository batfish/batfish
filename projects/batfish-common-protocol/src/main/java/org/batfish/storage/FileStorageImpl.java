package org.batfish.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Analysis;
import org.batfish.datamodel.pojo.Environment;

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
   * Get an Environment object
   *
   * @param containerName Parent container
   * @param testrigName Name of testrig
   * @param environmentName Name of Environment
   * @return Environment from storage
   */
  @Override
  public Environment getEnvironment(
      String containerName, String testrigName, String environmentName) {
    Path envDir = _utils.getEnvironmentPath(containerName, testrigName, environmentName);
    if (!Files.exists(envDir)) {
      throw new BatfishException(
          String.format(
              "Environment '%s' doesn't exist for container '%s'->testrig '%s'",
              environmentName, containerName, testrigName));
    }
    SortedSet<Path> subFileList = CommonUtil.getEntries(envDir);
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    Environment.Builder envBuilder = Environment.builder();
    envBuilder.setName(environmentName);
    try {
      for (Path subdirFile : subFileList) {
        switch (subdirFile.getFileName().toString()) {
          case BfConsts.RELPATH_EDGE_BLACKLIST_FILE:
            List<Edge> edgeBlackList =
                mapper.readValue(
                    CommonUtil.readFile(subdirFile), new TypeReference<List<Edge>>() {});
            envBuilder.setEdgeBlacklist(edgeBlackList);
            break;
          case BfConsts.RELPATH_NODE_BLACKLIST_FILE:
            List<String> nodeBlacklist =
                mapper.readValue(
                    CommonUtil.readFile(subdirFile), new TypeReference<List<String>>() {});
            envBuilder.setNodeBlacklist(nodeBlacklist);
            break;
          case BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE:
            List<NodeInterfacePair> interfaceBlacklist =
                mapper.readValue(
                    CommonUtil.readFile(subdirFile),
                    new TypeReference<List<NodeInterfacePair>>() {});
            envBuilder.setInterfaceBlacklist(interfaceBlacklist);
            break;
          case BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES:
            Map<String, String> bgpTables = new HashMap<>();
            if (Files.isDirectory(subdirFile)) {
              CommonUtil.getEntries(subdirFile)
                  .forEach(
                      path -> {
                        bgpTables.put(path.getFileName().toString(), CommonUtil.readFile(path));
                      });
            }
            envBuilder.setBgpTables(bgpTables);
            break;
          case BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES:
            Map<String, String> routingTables = new HashMap<>();
            if (Files.isDirectory(subdirFile)) {
              CommonUtil.getEntries(subdirFile)
                  .forEach(
                      path -> {
                        routingTables.put(path.getFileName().toString(), CommonUtil.readFile(path));
                      });
            }
            envBuilder.setRoutingTables(routingTables);
            break;
          case BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS:
            envBuilder.setExternalBgpAnnouncements(CommonUtil.readFile(subdirFile));
            break;
          default:
            continue;
        }
      }
    } catch (IOException e) {
      throw new BatfishException("Environment is not properly formatted");
    }
    return envBuilder.build();
  }

  /**
   * Save an Environment object
   *
   * @param containerName Parent container
   * @param testrigName Name of testrig
   * @param environment Environment Object
   * @return Saved copy of environment from storage
   */
  @Override
  public Environment saveEnvironment(
      String containerName, String testrigName, Environment environment) {
    Path envDir = _utils.getEnvironmentPath(containerName, testrigName, environment.getName());
    if (Files.exists(envDir)) {
      throw new BatfishException(
          String.format(
              "Environment '%s' already exists for container '%s' testrig '%s'",
              environment.getName(), containerName, testrigName));
    }

    if (!envDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Failed to create environment directory '%s'", environment.getName()));
    }
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    try {
      if (!environment.getEdgeBlacklist().isEmpty()) {
        CommonUtil.writeFile(
            _utils.resolvePath(envDir, BfConsts.RELPATH_EDGE_BLACKLIST_FILE),
            mapper.writeValueAsString(environment.getEdgeBlacklist()));
      }
      if (!environment.getInterfaceBlacklist().isEmpty()) {
        CommonUtil.writeFile(
            _utils.resolvePath(envDir, BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE),
            mapper.writeValueAsString(environment.getInterfaceBlacklist()));
      }
      if (!environment.getNodeBlacklist().isEmpty()) {
        CommonUtil.writeFile(
            _utils.resolvePath(envDir, BfConsts.RELPATH_NODE_BLACKLIST_FILE),
            mapper.writeValueAsString(environment.getNodeBlacklist()));
      }
      if (!environment.getBgpTables().isEmpty()) {
        Path bgpDir = _utils.resolvePath(envDir, BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES);
        bgpDir.toFile().mkdirs();
        environment
            .getBgpTables()
            .entrySet()
            .forEach(
                entry ->
                    CommonUtil.writeFile(
                        _utils.resolvePath(bgpDir, entry.getKey()), entry.getValue()));
      }
      if (!environment.getRoutingTables().isEmpty()) {
        Path rtDir = _utils.resolvePath(envDir, BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES);
        rtDir.toFile().mkdirs();
        environment
            .getRoutingTables()
            .entrySet()
            .forEach(
                entry ->
                    CommonUtil.writeFile(
                        _utils.resolvePath(rtDir, entry.getKey()), entry.getValue()));
      }
      if (!Strings.isNullOrEmpty(environment.getExternalBgpAnnouncements())) {
        CommonUtil.writeFile(
            _utils.resolvePath(envDir, BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS),
            environment.getExternalBgpAnnouncements());
      }
    } catch (JsonProcessingException e) {
      throw new BatfishException(
          String.format("Error while serializing environment '%s'", environment.getName()));
    }
    return getEnvironment(containerName, testrigName, environment.getName());
  }

  @Override
  public Environment updateEnvironment(
      String containerName, String testrigName, Environment environment) {
    return null;
  }

  @Override
  public boolean deleteEnvironment(String containerName, String testrigName, boolean force) {
    return false;
  }
}
