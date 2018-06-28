package org.batfish.common.util;

import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.Pair;
import org.batfish.common.WorkItem;

public class WorkItemBuilder {

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String envName,
      String deltaTestrig,
      String deltaEnvName,
      boolean isDifferential,
      boolean isDelta) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
    wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    if (isDifferential) {
      wItem.addRequestParam(BfConsts.ARG_DIFFERENTIAL, "");
    }
    if (deltaEnvName != null) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, deltaEnvName);
    }
    if (deltaTestrig != null) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
    }
    if (isDelta) {
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    }
    return wItem;
  }

  public static WorkItem getWorkItemGenerateDataPlane(
      String containerName, String testrigName, String envName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    return wItem;
  }

  public static WorkItem getWorkItemGenerateDeltaDataPlane(
      String containerName,
      String testrigName,
      String envName,
      String deltaTestrigName,
      String deltaEnvName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrigName);
    wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, deltaEnvName);
    wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    return wItem;
  }

  public static WorkItem getWorkItemParse(String containerName, String testrigName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
    wItem.addRequestParam(BfConsts.COMMAND_INIT_INFO, "");
    wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
    return wItem;
  }

  public static WorkItem getWorkItemProcessEnvironment(
      String containerName, String testrigName, String envName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_COMPILE_DIFF_ENVIRONMENT, "");
    wItem.addRequestParam(BfConsts.COMMAND_VALIDATE_ENVIRONMENT, "");
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    return wItem;
  }

  public static WorkItem getWorkItemRunAnalysis(
      String analysisName,
      String containerName,
      String testrigName,
      String envName,
      String deltaTestrig,
      String deltaEnvName,
      boolean delta,
      boolean differential) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANALYZE, "");
    wItem.addRequestParam(BfConsts.ARG_ANALYSIS_NAME, analysisName);
    wItem.addRequestParam(BfConsts.ARG_TESTRIG, testrigName);
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    if (differential || delta) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
      wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, deltaEnvName);
    }
    if (delta) {
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    }
    if (differential) {
      wItem.addRequestParam(BfConsts.ARG_DIFFERENTIAL, "");
    }
    return wItem;
  }

  public static boolean isAnalyzingWorkItem(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_ANALYZE);
  }

  public static boolean isAnsweringWorkItem(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_ANSWER);
  }

  public static boolean isDataplaningWorkItem(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_DUMP_DP);
  }

  public static boolean isDifferential(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.ARG_DIFFERENTIAL);
  }

  public static boolean isParsingWorkItem(WorkItem workItem) {
    return (workItem.getRequestParams().containsKey(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC)
        || workItem.getRequestParams().containsKey(BfConsts.COMMAND_COMPILE_DIFF_ENVIRONMENT));
  }

  public static Pair<Pair<String, String>, Pair<String, String>> getBaseAndDeltaSettings(
      WorkItem workItem) {
    Map<String, String> reqParams = workItem.getRequestParams();

    String testrig = workItem.getTestrigName();
    String envName =
        reqParams.getOrDefault(
            BfConsts.ARG_ENVIRONMENT_NAME, BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME);
    String deltaTestrig = reqParams.get(BfConsts.ARG_DELTA_TESTRIG);
    String deltaEnvName = reqParams.get(BfConsts.ARG_DELTA_ENVIRONMENT_NAME);
    if (deltaEnvName != null && deltaTestrig == null) {
      deltaTestrig = testrig;
    }
    if (deltaTestrig != null && deltaEnvName == null) {
      throw new BatfishException("deltaEnv not specified for deltaTestrig " + deltaTestrig);
    }
    if (reqParams.containsKey(BfConsts.ARG_DIFF_ACTIVE)
        && !reqParams.get(BfConsts.ARG_DIFF_ACTIVE).equalsIgnoreCase("false")) {
      if (deltaTestrig == null) {
        throw new BatfishException("delta settings not specified when diff_active is on");
      }
      testrig = deltaTestrig;
      envName = deltaEnvName;
    }

    return new Pair<>(new Pair<>(testrig, envName), new Pair<>(deltaTestrig, deltaEnvName));
  }

  public static String getBaseEnvironment(
      Pair<Pair<String, String>, Pair<String, String>> settings) {
    return settings.getFirst().getSecond();
  }

  public static String getBaseTestrig(Pair<Pair<String, String>, Pair<String, String>> settings) {
    return settings.getFirst().getFirst();
  }

  public static String getDeltaEnvironment(
      Pair<Pair<String, String>, Pair<String, String>> settings) {
    return settings.getSecond().getSecond();
  }

  public static String getDeltaTestrig(Pair<Pair<String, String>, Pair<String, String>> settings) {
    return settings.getSecond().getFirst();
  }

  public static String getAnalysisName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_ANALYSIS_NAME);
  }

  public static String getQuestionName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_QUESTION_NAME);
  }
}
