package org.batfish.storage;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.common.Container;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
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

  String createTestrigTopology(String containerName, String testrigName);

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

  Topology getTestrigTopology(String containerName, String testrigName) throws BatfishException;

  /** Get Collection/Stream of objects * */
  Set<Question> getAllAnalysisQuestions(String containerName, String analysisName)
      throws BatfishException;

  Stream<String> getAllContainersStream();

  Set<Container> getAllContainers();

  Set<Testrig> getAllTestrigs(String containerName) throws BatfishException;

  Set<Environment> getAllEnvironments(String containerName, String testrigName)
      throws BatfishException;

  /** Delete APIs */
  boolean deleteContainer(String containerName) throws BatfishException;

  boolean deleteTestrig(String containerName, String testrigName) throws BatfishException;

  boolean deleteQuestion(String contaninerName, String testrigName, String questionName)
      throws BatfishException;

  boolean deleteAnalysis(String containerName, String analysisName) throws BatfishException;

  boolean deleteEnvironment(String containerName, String testrigName, String env)
      throws BatfishException;
}
