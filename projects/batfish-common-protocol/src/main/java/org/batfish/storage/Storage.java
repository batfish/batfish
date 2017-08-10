package org.batfish.storage;

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

/** Common storage APIs */
public interface Storage {

  //deprecated
  Path getContainerPath(String containerName);

  /*
   * Create APIs,
   * Throw exception when parents do not exist
   * */

  String createContainer(String containerName)
      throws BatfishException; //when container already exists

  String createTestrig(String containerName, Testrig testRig) throws BatfishException;

  String createAnalysis(String containerName, String analysisName, boolean newAnalysis)
      throws BatfishException; //returns analysis name conditional on value of newAnalysis flag

  String createQuestion(String contaninerName, String testrigName, Question question)
      throws BatfishException; // Creates a question in testrig

  String createAnalysisQuestion(String containerName, String analysisName, Question question)
      throws BatfishException; //Create a question as a part of the analysis

  String createTestrigEnvironment(
      String containerName,
      String testrigName,
      Environment baseEnv,
      Environment deltaEnv,
      String newEnvName)
      throws BatfishException;
  // Exception when env already exists or when parent does not exist

  //Creates a topology under Testrig
  String createTopology(String containerName, String testrigName);

  //Creates a topology nested under 'testrig'
  String createTestrigTopology(String containerName, String testrigName);

  // may return a Unique ID for the object as does not have a name
  String createParseVendorConfigurationAnswerElement(
      String containerName,
      String testrigName,
      ParseVendorConfigurationAnswerElement parseVendorConfigurationAnswerElementObj);

  // may return a Unique ID for the object as does not have a name
  String createConvertConfigurationAnswerElement(
      String containerName,
      String testrigName,
      ConvertConfigurationAnswerElement convertConfigurationAnswerElementObj);

  /*
   * Get APIs
   * Throw exception if not found or parent not found
   * */

  Container getContainer(String containerName)
      throws BatfishException; //exception when container does not exist

  Testrig getTestrig(String containerName, String testrigName) throws BatfishException;

  Analysis getAnalysis(String containerName, String analysisName) throws BatfishException;

  Question getAnalysisQuestion(String questionName, String contaninerName, String analysisName)
      throws BatfishException;

  Question getTestrigQuestion(String contaninerName, String testrigName, String questionName)
      throws BatfishException;

  Answer getAnswer(
      String containerName,
      String baseTestrigName,
      String baseEnvName,
      String deltaTestrig,
      String deltaEnv,
      String questionName)
      throws BatfishException;
  // it would be better to split it in two functions for base and delta testrig

  Environment getTestrigEnvironment(String containerName, String testrigName, String env)
      throws BatfishException;

  //Gets the topology
  Topology getTopology(String containerName, String testrigName) throws BatfishException;

  //Gets the topology nested under testrig(topology.net)
  Topology getTestrigTopology(String containerName, String testrigName) throws BatfishException;

  /**
   * Gets all vendor independent configurations for a testrig
   *
   * @param containerName Container name
   * @param testrigName Testrig Name
   * @return Name and Configuration Object
   */
  //from deserializeConfigurations - can be from env or a testrig,
  Map<String, Configuration> getIndepConfigurations(
      String containerName, String testrigName, @Nullable String envName);

  /**
   * Gets all vendor dependent configurations for a testrig
   *
   * @param containerName Container name
   * @param testrigName Testrig Name
   * @return Name and Configuration Object
   */
  // from deserializeVendorConfigurations - can be from env ir a testrig
  Map<String, GenericConfigObject> getVendorConfigurations(
      String containerName, String testrigName, @Nullable String envName);

  // Found in loadParseVendorConfigurationAnswerElement() in Batfish.java
  ParseVendorConfigurationAnswerElement getParseVendorConfigurationAnswerElement(
      String containerName, String testrigName);

  // Found in loadConvertConfigurationAnswerElement() in Batfish.java
  ConvertConfigurationAnswerElement getConvertConfigurationAnswerElement(
      String containerName, String testrigName);

  // Reads configurations from inside /testrig configType can be host or config, ref readConfigurationFiles
  Set<Configuration> getConfiguration(
        String containerName, String testrigName, String configType);



  /** Get Collection/Stream of objects * */
  Set<Question> getAllAnalysisQuestions(String containerName, String analysisName)
      throws BatfishException;

  Stream<String> getAllContainersStream();

  Set<Container> getAllContainers();

  Set<Testrig> getAllTestrigs(String containerName) throws BatfishException;

  Set<Environment> getAllEnvironments(String containerName, String testrigName)
      throws BatfishException;

  /** Delete APIs Throw exception when parents do not exist * */
  boolean deleteContainer(String containerName) throws BatfishException;

  boolean deleteTestrig(String containerName, String testrigName) throws BatfishException;

  boolean deleteQuestion(String contaninerName, String testrigName, String questionName)
      throws BatfishException;

  boolean deleteAnalysis(String containerName, String analysisName) throws BatfishException;

  boolean deleteEnvironment(String containerName, String testrigName, String env)
      throws BatfishException;
}
