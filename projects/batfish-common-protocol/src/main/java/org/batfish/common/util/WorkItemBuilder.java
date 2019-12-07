package org.batfish.common.util;

import org.batfish.common.BfConsts;
import org.batfish.common.WorkItem;

public class WorkItemBuilder {

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String deltaTestrig,
      boolean isDifferential) {
    return getWorkItemAnswerQuestion(
        questionName, containerName, testrigName, deltaTestrig, null, isDifferential);
  }

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String deltaTestrig,
      String analysisName,
      boolean isDifferential) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
    wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
    if (isDifferential) {
      wItem.addRequestParam(BfConsts.ARG_DIFFERENTIAL, "");
    }
    if (deltaTestrig != null) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
    }
    if (analysisName != null) {
      wItem.addRequestParam(BfConsts.ARG_ANALYSIS_NAME, analysisName);
    }
    return wItem;
  }

  public static WorkItem getWorkItemGenerateDataPlane(String network, String snapshot) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
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
      String analysisName, String containerName, String testrigName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANALYZE, "");
    wItem.addRequestParam(BfConsts.ARG_ANALYSIS_NAME, analysisName);
    wItem.addRequestParam(BfConsts.ARG_TESTRIG, testrigName);
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

  public static String getReferenceSnapshotName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_DELTA_TESTRIG);
  }
}
