package org.batfish.storage;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.Container;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.pojo.Analysis;
import org.batfish.datamodel.pojo.Testrig;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.Environment;

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
  //Needs to be removed eventually
  @Override
  public Path getContainerPath(String containerName) {
    Path containerDir = _containersLocation.resolve(containerName).toAbsolutePath();
    if (!Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' not found");
    }
    return containerDir;
  }

  @Override
  public Testrig getTestrig(String containerName, String testrigName) {
    return null;
  }

  @Override
  public Analysis getAnalysis(String analysisName, String containerName) {
    return null;
  }

  @Override
  public Question getAnalysisQuestion(
      String questionName, String contaninerName, String analysisName) {
    return null;
  }

  @Override
  public Question getTestrigQuestion(
      String questionName, String contaninerName, String testrigName) {
    return null;
  }

  @Override
  public Answer getAnswer(
      String containerName,
      String baseTestrigName,
      String baseEnvName,
      String deltaTestrig,
      String deltaEnv,
      String questionName) {
    return null;
  }

  @Override
  public Environment getTestrigEnvironment(String containerName, String testrigName, String env)
      throws BatfishException {
    return null;
  }

  @Override
  public Topology getTopology(String containerName, String testrigName) throws BatfishException {
    return null;
  }

  @Override
  public Topology getTestrigTopology(String containerName, String testrigName)
      throws BatfishException {
    return null;
  }

  @Override
  public Map<String, Configuration> getIndepConfigurations(
      String containerName, String testrigName, @Nullable String envName) {
    return null;
  }

  @Override
  public Map<String, GenericConfigObject> getVendorConfigurations(
      String containerName, String testrigName, @Nullable String envName) {
    return null;
  }

  @Override
  public ParseVendorConfigurationAnswerElement getParseVendorConfigurationAnswerElement(
      String containerName, String testrigName) {
    return null;
  }

  @Override
  public ConvertConfigurationAnswerElement getConvertConfigurationAnswerElement(
      String containerName, String testrigName) {
    return null;
  }

  @Override
  public Set<Configuration> getConfiguration(
      String containerName, String testrigName, String configType) {
    return null;
  }

  @Override
  public boolean deleteQuestion(String questionName, String contaninerName, String analysisName) {
    return false;
  }

  @Override
  public boolean deleteAnalysis(String containerName, String analysisName) {
    return false;
  }

  @Override
  public boolean deleteContainer(String containerName) {
    return false;
  }

  @Override
  public boolean deleteTestrig(String containerName, String testrigName) throws BatfishException {
    return false;
  }

  @Override
  public boolean deleteEnvironment(String containerName, String testrigName, String env) {
    return false;
  }

  /**
   * Create a container
   *
   * @param containerName Create a container by this name
   * @return Name of the container
   */
  @Override
  public String createContainer(String containerName) {
    Path containerDir = _containersLocation.resolve(containerName);
    if (Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' already exists!");
    }
    if (!containerDir.toFile().mkdirs()) {
      throw new BatfishException("failed to create directory '" + containerDir.toString() + "'");
    }
    return containerName;
  }

  @Override
  public String createTestrig(String containerName, Testrig testRig) throws BatfishException {
    return null;
  }

  @Override
  public String createAnalysis(String containerName, String analysisName, boolean newAnalysis)
      throws BatfishException {
    return null;
  }

  @Override
  public String createQuestion(String contaninerName, String testrigName, Question question)
      throws BatfishException {
    return null;
  }

  @Override
  public String createAnalysisQuestion(String containerName, String analysisName, Question question)
      throws BatfishException {
    return null;
  }

  @Override
  public String createTestrigEnvironment(
      String containerName,
      String testrigName,
      Environment baseEnv,
      Environment deltaEnv,
      String newEnvName)
      throws BatfishException {
    return null;
  }

  @Override
  public String createTopology(String containerName, String testrigName) {
    return null;
  }

  @Override
  public String createTestrigTopology(String containerName, String testrigName) {
    return null;
  }

  @Override
  public String createParseVendorConfigurationAnswerElement(
      String containerName,
      String testrigName,
      ParseVendorConfigurationAnswerElement parseVendorConfigurationAnswerElementObj) {
    return null;
  }

  @Override
  public String createConvertConfigurationAnswerElement(
      String containerName,
      String testrigName,
      ConvertConfigurationAnswerElement convertConfigurationAnswerElementObj) {
    return null;
  }

  @Override
  public Container getContainer(String containerName) throws BatfishException {
    return null;
  }

  @Override
  public Set<Question> getAllAnalysisQuestions(String containerName, String analysisName) {
    return null;
  }

  @Override
  public Stream<String> getAllContainersStream() {
    return null;
  }

  @Override
  public Set<Container> getAllContainers() {
    return null;
  }

  @Override
  public Set<Testrig> getAllTestrigs(String containerName) throws BatfishException {
    return null;
  }

  @Override
  public Set<Environment> getAllEnvironments(String containerName, String testrigName)
      throws BatfishException {
    return null;
  }
}
