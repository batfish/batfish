package org.batfish.common.util;

import org.batfish.common.BfConsts;
import org.batfish.common.WorkItem;

public class WorkItemBuilder {

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String envName,
      String deltaTestrig,
      String deltaEnvName,
      boolean isDelta) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
    wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
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

  public static WorkItem getWorkItemCompileDeltaEnvironment(
      String containerName, String testrigName, String envName, String diffEnvName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_COMPILE_DIFF_ENVIRONMENT, "");
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, diffEnvName);
    wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
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

  public static WorkItem getWorkItemParse(
      String containerName, String testrigName, boolean doDelta) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
    wItem.addRequestParam(BfConsts.COMMAND_INIT_INFO, "");
    wItem.addRequestParam(BfConsts.ARG_UNIMPLEMENTED_SUPPRESS, "");
    if (doDelta) {
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    }
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

  public static WorkItem getWorkItemRunCompare(
      String containerName,
      String testrigName,
      String envName,
      String deltaTestrig,
      String deltaEnvName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_COMPARE, "");
    wItem.addRequestParam(BfConsts.ARG_TESTRIG, testrigName);
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
    wItem.addRequestParam(BfConsts.ARG_DELTA_ENVIRONMENT_NAME, deltaEnvName);
    wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    wItem.addRequestParam(BfConsts.ARG_DIFFERENTIAL, "");
    return wItem;
  }

  public static WorkItem getWorkItemRunExplore(
      String containerName, String testrigName, String envName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_EXPLORE, "");
    wItem.addRequestParam(BfConsts.ARG_TESTRIG, testrigName);
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    return wItem;
  }

  public static WorkItem getWorkItemValidateEnvironment(
      String containerName, String testrigName, String envName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_VALIDATE_ENVIRONMENT, "");
    wItem.addRequestParam(BfConsts.ARG_TESTRIG, testrigName);
    wItem.addRequestParam(BfConsts.ARG_ENVIRONMENT_NAME, envName);
    return wItem;
  }
}
