package org.batfish.common;

// IMPORTANT:
// If you change the values of any of these constants,
// make sure that the javascript, python clients is updated too

import org.batfish.common.BfConsts.TaskStatus;

public class CoordConsts {

  public enum WorkStatusCode {
    ASSIGNED,
    ASSIGNMENTERROR,
    BLOCKED,
    CHECKINGSTATUS,
    REQUEUEFAILURE,
    TERMINATEDABNORMALLY,
    TERMINATEDBYUSER,
    TERMINATEDNORMALLY,
    TRYINGTOASSIGN,
    UNASSIGNED;

    public static WorkStatusCode fromTerminatedTaskStatus(TaskStatus status) {
      switch (status) {
        case TerminatedAbnormally:
          return TERMINATEDABNORMALLY;
        case TerminatedByUser:
          return TERMINATEDBYUSER;
        case TerminatedNormally:
          return TERMINATEDNORMALLY;
        case RequeueFailure:
          return REQUEUEFAILURE;
        default:
          throw new IllegalArgumentException(
              "Cannot convert from " + status + " to WorkStatusCode");
      }
    }

    public boolean isTerminated() {
      return (this == ASSIGNMENTERROR // because we don't attempt assignment of this work
          || this == WorkStatusCode.TERMINATEDABNORMALLY
          || this == WorkStatusCode.TERMINATEDBYUSER
          || this == WorkStatusCode.TERMINATEDNORMALLY
          || this == WorkStatusCode.REQUEUEFAILURE);
    }
  }

  public static final String DEFAULT_API_KEY = "00000000000000000000000000000000";

  public static final Integer SVC_CFG_POOL_PORT = 9998;
  public static final boolean SVC_CFG_POOL_SSL_DISABLE = true;
  public static final String SVC_CFG_WORK_MGR2 = "/v2";
  public static final Integer SVC_CFG_WORK_V2_PORT = 9996;
  public static final boolean SVC_CFG_WORK_SSL_DISABLE = true;

  public static final String SVC_FILENAME_HDR = "FileName";

  /** Various constants used as keys on multi-part form data */
  public static final String SVC_KEY_ADD_WORKER = "addworker";

  /**
   * Note that despite the name and the value this has nothing to do with the former Batfish
   * analysis concept.
   */
  public static final String SVC_KEY_ANALYSIS_ANSWERS_OPTIONS = "analysisanswersoptions";

  public static final String SVC_KEY_ANSWER = "answer";
  public static final String SVC_KEY_API_KEY = "apikey";
  public static final String SVC_KEY_ASSERTION = "assertion";
  public static final String SVC_KEY_COMPLETION_TYPE = "completiontype";
  public static final String SVC_KEY_CONTAINER_LIST = "containerlist";
  public static final String SVC_KEY_CONTAINER_NAME = "container";
  public static final String SVC_KEY_DEL_WORKER = "delworker";
  public static final String SVC_KEY_EXCEPTIONS = "exceptions";
  public static final String SVC_KEY_FAILURE = "failure";
  public static final String SVC_KEY_FILE = "file";
  public static final String SVC_KEY_MAX_SUGGESTIONS = "maxsuggestions";
  public static final String SVC_KEY_NETWORK_LIST = "networklist";
  public static final String SVC_KEY_NETWORK_NAME = "networkname";
  public static final String SVC_KEY_NETWORK_PREFIX = "networkprefix";
  public static final String SVC_KEY_QUERY = "query";
  public static final String SVC_KEY_QUERY_METADATA = "querymetadata";
  public static final String SVC_KEY_QUESTION = "question";
  public static final String SVC_KEY_QUESTION_LIST = "questionlist";
  public static final String SVC_KEY_QUESTION_NAME = "questionname";
  public static final String SVC_KEY_REFERENCE_SNAPSHOT_NAME = "referencesnapshotname";
  public static final String SVC_KEY_SNAPSHOT_NAME = "snapshotname";
  public static final String SVC_KEY_SUCCESS = "success";
  public static final String SVC_KEY_SUGGESTED = "suggested";
  public static final String SVC_KEY_SUGGESTIONS = "suggestions";
  public static final String SVC_KEY_TASKSTATUS = "taskstatus";
  public static final String SVC_KEY_VERBOSE = "verbose";
  public static final String SVC_KEY_VERSION = "version";
  public static final String SVC_KEY_WORK_LIST = "worklist";
  public static final String SVC_KEY_WORK_TYPE = "worktype";
  public static final String SVC_KEY_WORKID = "workid";
  public static final String SVC_KEY_WORKITEM = "workitem";
  public static final String SVC_KEY_WORKSTATUS = "workstatus";
  public static final String SVC_KEY_ZIPFILE = "zipfile";

  /** Constants for endpoints of various service calls */
  public static final String SVC_RSC_AUTO_COMPLETE = "autocomplete";

  public static final String SVC_RSC_CHECK_API_KEY = "checkapikey";
  public static final String SVC_RSC_DEL_NETWORK = "delnetwork";
  public static final String SVC_RSC_DEL_QUESTION = "delquestion";
  public static final String SVC_RSC_DEL_SNAPSHOT = "delsnapshot";
  public static final String SVC_RSC_CONFIGURE_QUESTION_TEMPLATE = "configurequestiontemplate";
  public static final String SVC_RSC_GET_ANSWER = "getanswer";
  public static final String SVC_RSC_GET_ANSWER_METRICS = "getanswermetrics";
  public static final String SVC_RSC_GET_ANSWER_ROWS = "getanswerrows";
  public static final String SVC_RSC_GET_ANSWER_ROWS2 = "getanswerrows2";
  public static final String SVC_RSC_GET_NETWORK = "getnetwork";
  public static final String SVC_RSC_GET_QUESTION_TEMPLATES = "getquestiontemplates";
  public static final String SVC_RSC_GET_WORKSTATUS = "getworkstatus";
  public static final String SVC_RSC_GETSTATUS = "getstatus";
  public static final String SVC_RSC_INIT_NETWORK = "initnetwork";
  public static final String SVC_RSC_KILL_WORK = "killwork";
  public static final String SVC_RSC_LIST_INCOMPLETE_WORK = "listincompletework";
  public static final String SVC_RSC_LIST_NETWORKS = "listnetworks";
  public static final String SVC_RSC_LIST_QUESTIONS = "listquestions";
  public static final String SVC_RSC_POOL_GET_QUESTION_TEMPLATES = "getquestiontemplates";
  public static final String SVC_RSC_POOL_GETSTATUS = "getstatus";
  public static final String SVC_RSC_POOL_UPDATE = "updatepool";
  public static final String SVC_RSC_QUEUE_WORK = "queuework";
  public static final String SVC_RSC_UPLOAD_QUESTION = "uploadquestion";
  public static final String SVC_RSC_UPLOAD_SNAPSHOT = "uploadsnapshot";
}
