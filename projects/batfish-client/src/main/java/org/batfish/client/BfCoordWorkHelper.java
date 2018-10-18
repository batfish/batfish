package org.batfish.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.batfish.client.config.Settings;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Pair;
import org.batfish.common.Version;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AutocompleteSuggestion.CompletionType;
import org.batfish.datamodel.pojo.WorkStatus;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class BfCoordWorkHelper {

  private final String _coordWorkMgr;
  private final String _coordWorkMgrV2;
  private final BatfishLogger _logger;
  private final Settings _settings;
  private final Client _client;

  BfCoordWorkHelper(BatfishLogger logger, Settings settings) {
    _logger = logger;
    _settings = settings;
    _coordWorkMgr = _settings.getCoordinatorHost() + ":" + _settings.getCoordinatorWorkPort();
    _coordWorkMgrV2 = _settings.getCoordinatorHost() + ":" + _settings.getCoordinatorWorkV2Port();
    try {
      _client = getClientBuilder().build();
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      throw new BatfishException("Failed to create HTTP client", e);
    }
  }

  private static void addFileMultiPart(MultiPart multiPart, String key, String filename) {
    multiPart.bodyPart(
        new FormDataBodyPart(key, new File(filename), MediaType.APPLICATION_OCTET_STREAM_TYPE));
  }

  private static void addTextMultiPart(MultiPart multiPart, String key, String value) {
    multiPart.bodyPart(new FormDataBodyPart(key, value, MediaType.TEXT_PLAIN_TYPE));
  }

  String autoComplete(
      String containerName,
      String testrigName,
      CompletionType completionType,
      String query,
      int maxSuggestions) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_AUTO_COMPLETE);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_COMPLETION_TYPE, completionType.toString());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_QUERY, query);
      addTextMultiPart(
          multiPart, CoordConsts.SVC_KEY_MAX_SUGGESTIONS, Integer.toString(maxSuggestions));

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }
      if (!jObj.has(CoordConsts.SVC_KEY_SUGGESTIONS)) {
        _logger.errorf("suggestions key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getString(CoordConsts.SVC_KEY_SUGGESTIONS);
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  String checkApiKey() {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_CHECK_API_KEY);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }
      return Boolean.toString(jObj.getBoolean(CoordConsts.SVC_KEY_API_KEY));
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  boolean configureAnalysis(
      String containerName,
      boolean newAnalysis,
      String analysisName,
      @Nullable String addQuestionsFileName,
      @Nullable String delQuestionsStr) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_CONFIGURE_ANALYSIS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      if (newAnalysis) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_NEW_ANALYSIS, "new");
      }
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_ANALYSIS_NAME, analysisName);
      if (addQuestionsFileName != null) {
        addFileMultiPart(multiPart, CoordConsts.SVC_KEY_FILE, addQuestionsFileName);
      }
      if (delQuestionsStr != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_DEL_ANALYSIS_QUESTIONS, delQuestionsStr);
      }

      return postData(webTarget, multiPart) != null;
    } catch (Exception e) {
      if (e.getMessage().contains("FileNotFoundException")) {
        _logger.errorf("File not found: %s (addQuestionsFile file)\n", addQuestionsFileName);
      } else {
        _logger.errorf(
            "Exception when configuring analysis to %s using (%s, %s, %s, %s, %s): %s\n",
            _coordWorkMgr,
            containerName,
            newAnalysis,
            analysisName,
            addQuestionsFileName,
            delQuestionsStr,
            Throwables.getStackTraceAsString(e));
      }
      return false;
    }
  }

  @Nullable
  String configureTemplate(String inTemplate, JsonNode exceptions, JsonNode assertion) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_CONFIGURE_QUESTION_TEMPLATE);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_QUESTION, inTemplate);
      if (exceptions != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_EXCEPTIONS, exceptions.toString());
      }
      if (assertion != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_ASSERTION, assertion.toString());
      }

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_QUESTION)) {
        _logger.errorf("question key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getString(CoordConsts.SVC_KEY_QUESTION);
    } catch (Exception e) {
      _logger.errorf(
          "Exception in configureTemplate from %s using (%s, %s, %s)\n",
          _coordWorkMgr, inTemplate, exceptions, assertion);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  boolean delAnalysis(String containerName, String analysisName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_DEL_ANALYSIS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_ANALYSIS_NAME, analysisName);

      return postData(webTarget, multiPart) != null;
    } catch (Exception e) {
      _logger.errorf(
          "Exception when deleting analysis to %s using (%s, %s): %s\n",
          _coordWorkMgr, containerName, analysisName, Throwables.getStackTraceAsString(e));
      return false;
    }
  }

  boolean delContainer(String containerName) {
    try {
      WebTarget webTarget =
          getTargetV2(Lists.newArrayList(CoordConstsV2.RSC_CONTAINERS, containerName));

      Response response =
          webTarget
              .request(MediaType.APPLICATION_JSON)
              .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
              .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
              .delete();

      if (response.getStatus() != Status.NO_CONTENT.getStatusCode()) {
        _logger.errorf("delNetwork: Did not get OK response. Got: %s\n", response.getStatus());
        _logger.error(response.readEntity(String.class) + "\n");
        return false;
      }
      return true;
    } catch (Exception e) {
      _logger.errorf("Exception in delNetwork from %s for %s\n", _coordWorkMgrV2, containerName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  boolean delQuestion(String containerName, String testrigName, String questionName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_DEL_QUESTION);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_QUESTION_NAME, questionName);

      JSONObject jObj = postData(webTarget, multiPart);
      return jObj != null;
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  boolean delTestrig(String containerName, String testrigName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_DEL_TESTRIG);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);

      JSONObject jObj = postData(webTarget, multiPart);
      return jObj != null;
    } catch (Exception e) {
      _logger.errorf("Exception in delSnapshot for network %s:\n", containerName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  // private JSONObject getJsonResponse(WebTarget webTarget) throws Exception {
  // try {
  // Response response = webTarget.request(MediaType.APPLICATION_JSON)
  // .get();
  //
  // _logger.info(response.getStatus() + " " + response.getStatusInfo()
  // + " " + response + "\n");
  //
  // if (response.getStatus() != Response.Status.OK.getStatusCode()) {
  // _logger.errorf("Did not get an OK response from: %s\n", webTarget);
  // return null;
  // }
  //
  // String sobj = response.readEntity(String.class);
  // JSONArray array = new JSONArray(sobj);
  //
  // _logger.infof("response: %s [%s] [%s]\n", array.toString(),
  // array.get(0), array.get(1));
  //
  // if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
  // _logger.errorf("did not get success: %s %s\n", array.get(0),
  // array.get(1));
  // return null;
  // }
  //
  // return new JSONObject(array.get(1).toString());
  // }
  // catch (ProcessingException e) {
  // if (e.getMessage().contains("ConnectException")) {
  // _logger.errorf("unable to connect to coordinator at %s\n",
  // _coordWorkMgr);
  // return null;
  // }
  // if (e.getMessage().contains("SSLHandshakeException")) {
  // _logger
  // .errorf("SSL handshake exception while connecting to coordinator (Is the
  // coordinator using SSL and using keys that you trust?)\n");
  // return null;
  // }
  // if (e.getMessage().contains("SocketException: Unexpected end of file")) {
  // _logger
  // .errorf("SocketException while connecting to coordinator. (Are you using
  // SSL?)\n");
  // return null;
  // }
  // throw e;
  // }
  // }

  @Nullable
  String getAnalysisAnswers(
      String containerName, String baseTestrig, String deltaTestrig, String analysisName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_GET_ANALYSIS_ANSWERS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, baseTestrig);
      if (deltaTestrig != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_DELTA_TESTRIG_NAME, deltaTestrig);
      }
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_ANALYSIS_NAME, analysisName);

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_ANSWERS)) {
        _logger.errorf("answer key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getString(CoordConsts.SVC_KEY_ANSWERS);
    } catch (Exception e) {
      _logger.errorf(
          "Exception in getAnswer from %s using (%s, %s)\n",
          _coordWorkMgr, baseTestrig, analysisName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  String getAnswer(
      String containerName, String baseTestrig, String deltaTestrig, String questionName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_GET_ANSWER);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, baseTestrig);
      if (deltaTestrig != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_DELTA_TESTRIG_NAME, deltaTestrig);
      }
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_QUESTION_NAME, questionName);

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_ANSWER)) {
        _logger.errorf("answer key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getString(CoordConsts.SVC_KEY_ANSWER);

    } catch (Exception e) {
      _logger.errorf(
          "Exception in getAnswer from %s using (%s, %s)\n",
          _coordWorkMgr, baseTestrig, questionName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  private ClientBuilder getClientBuilder() {
    return CommonUtil.createHttpClientBuilder(
            _settings.getSslDisable(),
            _settings.getSslTrustAllCerts(),
            _settings.getSslKeystoreFile(),
            _settings.getSslKeystorePassword(),
            _settings.getSslTruststoreFile(),
            _settings.getSslTruststorePassword(),
            true)
        .register(MultiPartFeature.class);
  }

  /**
   * Returns a string contains the content of the configuration file {@code configName}, returns
   * null if configuration file {@code configName} does not exist or the api key that is using has
   * no access to the container {@code containerName}
   */
  @Nullable
  String getConFiguration(String containerName, String testrigName, String configName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_GET_CONFIGURATION);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_VERSION, Version.getVersion());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONFIGURATION_NAME, configName);

      Response response =
          webTarget
              .request(MediaType.APPLICATION_JSON)
              .post(Entity.entity(multiPart, multiPart.getMediaType()));

      _logger.debugf("%s %s %s\n", response.getStatus(), response.getStatusInfo(), response);

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.error("GetConfiguration: Did not get an OK response\n");
        _logger.error(response.readEntity(String.class) + "\n");
        return null;
      }

      return response.readEntity(String.class);
    } catch (Exception e) {
      _logger.errorf(
          "Exception in getConfiguration from %s for network %s, snapshot %s, configuration %s\n",
          _coordWorkMgr, containerName, testrigName, configName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  /**
   * Returns a {@link Container Container} that contains information of '{@code networkName}',
   * returns null if network '{@code networkName}' does not exist or the api key that is using has
   * no access to the network
   */
  @Nullable
  Container getNetwork(String networkName) {
    try {
      WebTarget webTarget =
          getTargetV2(Lists.newArrayList(CoordConstsV2.RSC_CONTAINERS, networkName));

      Response response =
          webTarget
              .request(MediaType.APPLICATION_JSON)
              .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
              .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
              .get();

      _logger.debug(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.errorf("getNetwork: Did not get OK response. Got: %s\n", response.getStatus());
        _logger.error(response.readEntity(String.class) + "\n");
        return null;
      }

      String containerStr = response.readEntity(String.class);
      return BatfishObjectMapper.mapper().readValue(containerStr, Container.class);
    } catch (Exception e) {
      _logger.errorf("Exception in getNetwork from %s for %s\n", _coordWorkMgrV2, networkName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  Map<String, String> getInfo() {
    try {
      WebTarget webTarget = getTarget("");

      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();

      _logger.debugf(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        System.err.print("GET did not get an OK response\n");
        return null;
      }

      String sobj = response.readEntity(String.class);
      JSONArray array = new JSONArray(sobj);
      _logger.debugf("response: %s [%s] [%s]\n", array, array.get(0), array.get(1));

      if (!array.get(0).equals(CoordConsts.SVC_KEY_SUCCESS)) {
        _logger.errorf("Error in PostData: %s %s\n", array.get(0), array.get(1));
        return null;
      }

      JSONObject jObject = array.getJSONObject(1);
      Iterator<?> keys = jObject.keys();

      Map<String, String> retMap = new HashMap<>();

      while (keys.hasNext()) {
        String key = (String) keys.next();
        String value = jObject.getString(key);
        retMap.put(key, value);
      }

      return retMap;
    } catch (Exception e) {
      _logger.errorf("Exception in getInfo from %s\n", _coordWorkMgr);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  String getObject(String containerName, String testrigName, String objectName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_GET_OBJECT);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_VERSION, Version.getVersion());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_OBJECT_NAME, objectName);

      Response response =
          webTarget
              .request(MediaType.APPLICATION_OCTET_STREAM)
              .post(Entity.entity(multiPart, multiPart.getMediaType()));

      _logger.debug(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.debugf(
            "GetObject: Did not get an OK response for %s -> %s->%s\n",
            containerName, testrigName, objectName);
        return null;
      }

      // see if we have a filename header
      // String outFileStr = objectName;
      //
      // MultivaluedMap<String, String> headers =
      // response.getStringHeaders();
      //
      // if (headers.containsKey(CoordConsts.SVC_FILENAME_HDR)) {
      // String value = headers.getFirst(CoordConsts.SVC_FILENAME_HDR);
      // if (value != null && !value.equals("")) {
      // outFileStr = value;
      // }
      // }

      File inFile = response.readEntity(File.class);

      File tmpOutFile = Files.createTempFile("batfish_client", null).toFile();
      tmpOutFile.deleteOnExit();

      FileUtils.copyFile(inFile, tmpOutFile);
      if (!inFile.delete()) {
        throw new BatfishException("Failed to delete temporary file: " + inFile.getAbsolutePath());
      }
      return tmpOutFile.getAbsolutePath();
    } catch (Exception e) {
      _logger.errorf(
          "Exception in getObject from %s using (%s, %s)\n",
          _coordWorkMgr, testrigName, objectName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  /**
   * Gets the questions configured at the coordinator
   *
   * @return JSON Object containing question keys and question content as values null if there is
   *     any failure
   */
  @Nullable
  JSONObject getQuestionTemplates() {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_GET_QUESTION_TEMPLATES);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_QUESTION_LIST)) {
        _logger.errorf("question list key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getJSONObject(CoordConsts.SVC_KEY_QUESTION_LIST);
    } catch (Exception e) {
      _logger.errorf("Exception in getQuestionTemplates from %s\n", _coordWorkMgr);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  private WebTarget getTarget(String resource) {
    String protocol = (_settings.getSslDisable()) ? "http" : "https";
    String urlString =
        String.format(
            "%s://%s%s/%s", protocol, _coordWorkMgr, CoordConsts.SVC_CFG_WORK_MGR, resource);
    return _client.target(urlString);
  }

  /**
   * Returns a {@link WebTarget webTarget} for BatFish service V2 by resolving each segment in
   * {@code resources} to base url of service V2.
   */
  private WebTarget getTargetV2(List<String> resources) {
    String protocol = (_settings.getSslDisable()) ? "http" : "https";
    String urlString =
        String.format("%s://%s%s", protocol, _coordWorkMgrV2, CoordConsts.SVC_CFG_WORK_MGR2);
    WebTarget target = _client.target(urlString);
    for (String resource : resources) {
      target = target.path(resource);
    }
    return target;
  }

  @Nullable
  Pair<WorkStatusCode, String> getWorkStatus(UUID workId) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_GET_WORKSTATUS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_WORKID, workId.toString());

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_WORKSTATUS)) {
        _logger.errorf("workstatus key not found in: %s\n", jObj);
        return null;
      }

      WorkStatusCode workStatus =
          WorkStatusCode.valueOf(jObj.getString(CoordConsts.SVC_KEY_WORKSTATUS));

      if (!jObj.has(CoordConsts.SVC_KEY_TASKSTATUS)) {
        _logger.errorf("taskstatus key not found in: %s\n", jObj);
      }
      String taskStr = jObj.getString(CoordConsts.SVC_KEY_TASKSTATUS);

      return new Pair<>(workStatus, taskStr);
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  String initContainer(@Nullable String containerName, @Nullable String containerPrefix) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_INIT_CONTAINER);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      if (containerName != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      } else {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_PREFIX, containerPrefix);
      }

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_CONTAINER_NAME)) {
        _logger.errorf("container name key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getString(CoordConsts.SVC_KEY_CONTAINER_NAME);
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  boolean isReachable(boolean chatty) {

    WebTarget webTarget = getTarget("");

    try {
      Response response = webTarget.request().get();

      _logger.info(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.errorf("GetObject: Did not get an OK response\n");
        return false;
      }

      return true;
    } catch (ProcessingException e) {
      if (e.getMessage().contains("ConnectException")) {
        if (chatty) {
          _logger.errorf("BF-Client: unable to connect to coordinator at %s\n", webTarget.getUri());
        }
        return false;
      }
      if (e.getMessage().contains("SSLHandshakeException")) {
        if (chatty) {
          _logger.errorf(
              "SSL handshake exception while connecting to coordinator (Is the coordinator using "
                  + "SSL and using keys that you trust?): %s\n",
              e.getMessage());
        }
        return false;
      }
      if (e.getMessage().contains("SocketException: Unexpected end of file")) {
        if (chatty) {
          _logger.errorf("SocketException while connecting to coordinator. (Are you using SSL?)\n");
        }
        return false;
      }
      throw e;
    }
  }

  boolean killWork(UUID workId) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_KILL_WORK);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_WORKID, workId.toString());

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return false;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_RESULT)) {
        _logger.errorf("result key not found in: %s\n", jObj);
        return false;
      }

      return jObj.getBoolean(CoordConsts.SVC_KEY_RESULT);
    } catch (Exception e) {
      _logger.errorf("exception: %s\n", Throwables.getStackTraceAsString(e));
      return false;
    }
  }

  @Nullable
  JSONObject listAnalyses(String containerName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_LIST_ANALYSES);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_ANALYSIS_LIST)) {
        _logger.errorf("analysis list key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getJSONObject(CoordConsts.SVC_KEY_ANALYSIS_LIST);
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  String[] listContainers() {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_LIST_CONTAINERS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());

      JSONObject jObj = postData(webTarget, multiPart);

      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_CONTAINER_LIST)) {
        _logger.errorf("container list key not found in: %s\n", jObj);
        return null;
      }

      JSONArray containerArray = jObj.getJSONArray(CoordConsts.SVC_KEY_CONTAINER_LIST);

      String[] containerList = new String[containerArray.length()];

      for (int index = 0; index < containerArray.length(); index++) {
        containerList[index] = containerArray.getString(index);
      }

      return containerList;
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  List<WorkStatus> listIncompleteWork(String containerName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_LIST_INCOMPLETE_WORK);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_WORK_LIST)) {
        _logger.errorf("work list key not found in: %s\n", jObj);
        return null;
      }

      String result = jObj.getString(CoordConsts.SVC_KEY_WORK_LIST);

      return BatfishObjectMapper.mapper()
          .readValue(result, new TypeReference<List<WorkStatus>>() {});
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  String[] listQuestions(String containerName, String testrigName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_LIST_QUESTIONS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_QUESTION_LIST)) {
        _logger.errorf("question list key not found in: %s\n", jObj);
        return null;
      }

      JSONObject questions = jObj.getJSONObject(CoordConsts.SVC_KEY_QUESTION_LIST);

      @SuppressWarnings("unchecked") // JSONObject has String keys, so the implicit cast is safe.
      String[] questionList = Iterators.toArray((Iterator<String>) questions.keys(), String.class);
      return questionList;
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  JSONArray listTestrigs(String containerName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_LIST_TESTRIGS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      if (containerName != null) {
        addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      }

      JSONObject jObj = postData(webTarget, multiPart);
      if (jObj == null) {
        return null;
      }

      if (!jObj.has(CoordConsts.SVC_KEY_TESTRIG_LIST)) {
        _logger.errorf("testrig key not found in: %s\n", jObj);
        return null;
      }

      return jObj.getJSONArray(CoordConsts.SVC_KEY_TESTRIG_LIST);
    } catch (Exception e) {
      _logger.errorf("Exception in listSnapshots for network %s:\n", containerName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  @Nullable
  private JSONObject postData(WebTarget webTarget, MultiPart multiPart) throws Exception {
    try {

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_VERSION, Version.getVersion());

      Response response =
          webTarget
              .request(MediaType.APPLICATION_JSON)
              .post(Entity.entity(multiPart, multiPart.getMediaType()));

      _logger.debugf(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        System.err.print("PostData: Did not get an OK response\n");
        return null;
      }

      String sobj = response.readEntity(String.class);
      JSONArray array = new JSONArray(sobj);
      _logger.debugf("response: %s [%s] [%s]\n", array, array.get(0), array.get(1));

      if (!array.get(0).equals(CoordConsts.SVC_KEY_SUCCESS)) {
        _logger.errorf("Error in PostData: %s %s\n", array.get(0), array.get(1));
        return null;
      }

      return new JSONObject(array.get(1).toString());
    } catch (ProcessingException e) {
      if (e.getMessage().contains("ConnectException")) {
        _logger.errorf("unable to connect to coordinator at %s\n", _coordWorkMgr);
        return null;
      }
      if (e.getMessage().contains("SSLHandshakeException")) {
        _logger.errorf(
            "SSL handshake exception while connecting to coordinator (Is the coordinator using "
                + "SSL and using keys that you trust?)\n");
        return null;
      }
      if (e.getMessage().contains("SocketException: Unexpected end of file")) {
        _logger.errorf("SocketException while connecting to coordinator. (Are you using SSL?)\n");
        return null;
      }
      throw e;
    }
  }

  boolean queueWork(WorkItem wItem) {

    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_QUEUE_WORK);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(
          multiPart, CoordConsts.SVC_KEY_WORKITEM, BatfishObjectMapper.writeString(wItem));
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());

      JSONObject jObj = postData(webTarget, multiPart);
      return jObj != null;
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  boolean syncTestrigsSyncNow(String pluginId, String containerName, boolean force) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_SYNC_TESTRIGS_SYNC_NOW);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_VERSION, Version.getVersion());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_PLUGIN_ID, pluginId);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_FORCE, String.valueOf(force));

      JSONObject jObj = postData(webTarget, multiPart);
      return jObj != null;
    } catch (Exception e) {
      _logger.errorf("Exception syncing snapshots in network %s:\n", containerName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  boolean syncTestrigsUpdateSettings(
      String pluginId, String containerName, Map<String, String> settings) {
    try {
      String settingsStr = BatfishObjectMapper.writePrettyString(settings);

      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_SYNC_TESTRIGS_UPDATE_SETTINGS);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_VERSION, Version.getVersion());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_PLUGIN_ID, pluginId);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_SETTINGS, settingsStr);

      JSONObject jObj = postData(webTarget, multiPart);
      return jObj != null;
    } catch (Exception e) {
      _logger.errorf("Exception syncing snapshots in network %s:\n", containerName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  boolean uploadCustomObject(
      String containerName, String testrigName, String objName, String objFileName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_PUT_OBJECT);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_OBJECT_NAME, objName);
      addFileMultiPart(multiPart, CoordConsts.SVC_KEY_FILE, objFileName);

      return postData(webTarget, multiPart) != null;
    } catch (Exception e) {
      if (e.getMessage().contains("FileNotFoundException")) {
        _logger.errorf("File not found: %s\n", objFileName);
      } else {
        _logger.errorf(
            "Exception when uploading custom object to %s using (%s, %s, %s): %s\n",
            _coordWorkMgr, testrigName, objName, objFileName, Throwables.getStackTraceAsString(e));
      }
      return false;
    }
  }

  boolean uploadQuestion(String containerName, String testrigName, String qName, String qFileName) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_UPLOAD_QUESTION);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_QUESTION_NAME, qName);
      addFileMultiPart(multiPart, CoordConsts.SVC_KEY_FILE, qFileName);

      return postData(webTarget, multiPart) != null;
    } catch (Exception e) {
      if (e.getMessage().contains("FileNotFoundException")) {
        _logger.errorf("File not found: %s (question file)\n", qFileName);
      } else {
        _logger.errorf(
            "Exception when uploading question to %s using (%s, %s, %s): %s\n",
            _coordWorkMgr, testrigName, qName, qFileName, Throwables.getStackTraceAsString(e));
      }
      return false;
    }
  }

  boolean uploadTestrig(
      String containerName, String testrigName, String zipfileName, boolean autoAnalyze) {
    try {
      WebTarget webTarget = getTarget(CoordConsts.SVC_RSC_UPLOAD_TESTRIG);

      MultiPart multiPart = new MultiPart();
      multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_API_KEY, _settings.getApiKey());
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_CONTAINER_NAME, containerName);
      addTextMultiPart(multiPart, CoordConsts.SVC_KEY_TESTRIG_NAME, testrigName);
      addFileMultiPart(multiPart, CoordConsts.SVC_KEY_ZIPFILE, zipfileName);
      addTextMultiPart(
          multiPart, CoordConsts.SVC_KEY_AUTO_ANALYZE_TESTRIG, String.valueOf(autoAnalyze));

      return postData(webTarget, multiPart) != null;
    } catch (Exception e) {
      if (e.getMessage().contains("FileNotFoundException")) {
        _logger.errorf("File not found: %s\n", zipfileName);
      } else {
        _logger.errorf(
            "Exception when uploading snapshot to %s using (%s, %s, %s): %s\n",
            _coordWorkMgr,
            containerName,
            testrigName,
            zipfileName,
            Throwables.getStackTraceAsString(e));
      }
      return false;
    }
  }

  public boolean debugV2(
      FileWriter outWriter, String method, String urlTail, Object entity, MediaType mediaType)
      throws IOException {
    WebTarget webTarget = getTargetV2(Arrays.asList(urlTail.split("/", -1)));
    Invocation.Builder builder =
        webTarget
            .request(MediaType.APPLICATION_JSON)
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
    try (Response response =
        entity != null
            ? builder.method(method, Entity.entity(entity, mediaType))
            : builder.method(method)) {
      if (response.hasEntity()) {
        String output = response.readEntity(String.class);
        if (outWriter != null) {
          outWriter.write(output);
        } else {
          _logger.outputf("%s\n", output);
        }
      }
      return response.getStatus() == Status.OK.getStatusCode();
    }
  }
}
