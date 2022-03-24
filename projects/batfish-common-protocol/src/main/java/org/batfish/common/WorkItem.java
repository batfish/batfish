package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkItem {
  private static final String PROP_NETWORK = "containerName";
  private static final String PROP_ID = "id";
  private static final String PROP_REQUEST_PARAMS = "requestParams";
  private static final String PROP_SNAPSHOT = "testrigName";

  // used for testing to force an UUID
  private static UUID FIXED_UUID = null;

  private final String _network;
  private final UUID _id;
  private Map<String, String> _requestParams;
  private final String _snapshot;

  public WorkItem(String containerName, String testrigName) {
    this(
        (FIXED_UUID == null) ? UUID.randomUUID() : FIXED_UUID,
        containerName,
        testrigName,
        new HashMap<>());
  }

  @JsonCreator
  public WorkItem(
      @JsonProperty(PROP_ID) UUID id,
      @JsonProperty(PROP_NETWORK) String network,
      @JsonProperty(PROP_SNAPSHOT) String snapshot,
      @JsonProperty(PROP_REQUEST_PARAMS) Map<String, String> reqParams) {
    _id = id;
    _network = network;
    _snapshot = snapshot;
    _requestParams = firstNonNull(reqParams, new HashMap<>());
  }

  public void addRequestParam(String key, String value) {
    _requestParams.put(key, value);
  }

  @JsonProperty(PROP_NETWORK)
  public String getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_ID)
  public UUID getId() {
    return _id;
  }

  @JsonProperty(PROP_REQUEST_PARAMS)
  public Map<String, String> getRequestParams() {
    return _requestParams;
  }

  @JsonProperty(PROP_SNAPSHOT)
  public String getSnapshot() {
    return _snapshot;
  }

  /**
   * The supplied workItem is a match if it has the same network, snapshot, and request parameters
   *
   * @param workItem The workItem that should be matched
   * @return {@link boolean} that indicates whether the supplied workItem is a match
   */
  public boolean matches(WorkItem workItem) {
    return (workItem != null
        && workItem._network.equals(_network)
        && workItem._snapshot.equals(_snapshot)
        && workItem._requestParams.equals(_requestParams));
  }

  public static void setFixedUuid(UUID value) {
    FIXED_UUID = value;
  }

  @Override
  public String toString() {
    return String.format("[%s %s %s %s]", _id, _network, _snapshot, _requestParams);
  }
}
