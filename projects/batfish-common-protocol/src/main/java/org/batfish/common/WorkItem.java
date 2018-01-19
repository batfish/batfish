package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.util.GlobalTracer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class WorkItem {

  private static final String PROP_CONTAINER_NAME = "containerName";
  private static final String PROP_ID = "id";
  private static final String PROP_REQUEST_PARAMS = "requestParams";
  private static final String PROP_TESTRIG_NAME = "testrigName";

  private final String _containerName;
  private final UUID _id;
  private Map<String, String> _requestParams;
  private final String _testrigName;
  private Map<String, String> _spanData; /* Map used by the TextMap carrier for SpanContext */

  public WorkItem(String containerName, String testrigName) {
    this(UUID.randomUUID(), containerName, testrigName, new HashMap<>());
  }

  @JsonCreator
  public WorkItem(
      @JsonProperty(PROP_ID) UUID id,
      @JsonProperty(PROP_CONTAINER_NAME) String containerName,
      @JsonProperty(PROP_TESTRIG_NAME) String testrigName,
      @JsonProperty(PROP_REQUEST_PARAMS) Map<String, String> reqParams) {
    _id = id;
    _containerName = containerName;
    _testrigName = testrigName;
    _requestParams = reqParams;
    _spanData = new HashMap<>();
  }

  public void addRequestParam(String key, String value) {
    _requestParams.put(key, value);
  }

  @JsonProperty(PROP_CONTAINER_NAME)
  public String getContainerName() {
    return _containerName;
  }

  @JsonProperty(PROP_ID)
  public UUID getId() {
    return _id;
  }

  @JsonProperty(PROP_REQUEST_PARAMS)
  public Map<String, String> getRequestParams() {
    return _requestParams;
  }

  /**
   * Retrieves a {@link SpanContext} which was serialized earlier in the {@link WorkItem}
   *
   * @return {@link SpanContext} or null if no {@link SpanContext} was serialized in the {@link
   *     WorkItem}
   */
  @Nullable
  @JsonIgnore
  public SpanContext getSourceSpan() {
    return getSourceSpan(GlobalTracer.get());
  }

  // visible for testing
  SpanContext getSourceSpan(Tracer tracer) {
    return tracer.extract(Builtin.TEXT_MAP, new TextMapExtractAdapter(_spanData));
  }

  @JsonProperty(PROP_TESTRIG_NAME)
  public String getTestrigName() {
    return _testrigName;
  }

  /**
   * The supplied workItem is a match if it has the same container, testrig, and request parameters
   *
   * @param workItem The workItem that should be matched
   * @return {@link boolean} that indicates whether the supplied workItem is a match
   */
  public boolean matches(WorkItem workItem) {
    return (workItem != null
        && workItem._containerName.equals(_containerName)
        && workItem._testrigName.equals(_testrigName)
        && workItem._requestParams.equals(_requestParams));
  }

  /**
   * Takes an {@link ActiveSpan} and attaches it to the {@link WorkItem} which can be fetched later
   * using {@link WorkItem#getSourceSpan()}
   */
  public void setSourceSpan(@Nullable ActiveSpan activeSpan) {
    setSourceSpan(activeSpan, GlobalTracer.get());
  }

  // visible for testing
  void setSourceSpan(@Nullable ActiveSpan activeSpan, Tracer tracer) {
    if (activeSpan == null) {
      return;
    }
    tracer.inject(activeSpan.context(), Builtin.TEXT_MAP, new TextMapInjectAdapter(_spanData));
  }

  public String toString() {
    return String.format("[%s %s %s %s]", _id, _containerName, _testrigName, _requestParams);
  }
}
