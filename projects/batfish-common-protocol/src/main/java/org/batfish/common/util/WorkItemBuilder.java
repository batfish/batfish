package org.batfish.common.util;

import org.batfish.common.BfConsts;
import org.batfish.common.WorkItem;

public class WorkItemBuilder {

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String deltaTestrig,
      boolean isDifferential,
      boolean isDelta) {
    return getWorkItemAnswerQuestion(
        questionName, containerName, testrigName, deltaTestrig, null, isDifferential, isDelta);
  }

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String deltaTestrig,
      String analysisName,
      boolean isDifferential,
      boolean isDelta) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
    wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
    if (isDifferential) {
      wItem.addRequestParam(BfConsts.ARG_DIFFERENTIAL, "");
    }
    if (deltaTestrig != null) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
    }
    if (isDelta) {
      wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    }
    if (analysisName != null) {
      wItem.addRequestParam(BfConsts.ARG_ANALYSIS_NAME, analysisName);
    }
    return wItem;
  }

  public static WorkItem getWorkItemGenerateDataPlane(String containerName, String testrigName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
    return wItem;
  }

  public static WorkItem getWorkItemGenerateDeltaDataPlane(
      String containerName, String testrigName, String deltaTestrigName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
    wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrigName);
    wItem.addRequestParam(BfConsts.ARG_DIFF_ACTIVE, "");
    return wItem;
  }

  public static WorkItem getWorkItemParse(String containerName, String testrigName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
    wItem.addRequestParam(BfConsts.COMMAND_INIT_INFO, "");
    wItem.addRequestParam(BfConsts.ARG_IGNORE_MANAGEMENT_INTERFACES, "");
    return wItem;
  }

  public static WorkItem getWorkItemRunAnalysis(
      String analysisName,
      String containerName,
      String testrigName,
      String deltaTestrig,
      boolean delta,
      boolean differential) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANALYZE, "");
    wItem.addRequestParam(BfConsts.ARG_ANALYSIS_NAME, analysisName);
    wItem.addRequestParam(BfConsts.ARG_TESTRIG, testrigName);
    if (differential || delta) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
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
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC);
  }

  public static String getAnalysisName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_ANALYSIS_NAME);
  }

  public static String getQuestionName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_QUESTION_NAME);
  }
}
