package org.batfish.question.ipsecsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** Return status of all IPSec sessions in the network */
@ParametersAreNonnullByDefault
public class IpsecSessionStatusQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_REMOTE_NODES = "remoteNodes";
  private static final String PROP_STATUS = "status";

  private static final String QUESTION_NAME = "ipsecSessionStatus";

  private @Nullable String _nodes;

  private @Nullable String _remoteNodes;

  private @Nullable String _status;

  @JsonCreator
  private static IpsecSessionStatusQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_REMOTE_NODES) @Nullable String remoteNodes,
      @JsonProperty(PROP_STATUS) @Nullable String status) {
    return new IpsecSessionStatusQuestion(nodes, remoteNodes, status);
  }

  public IpsecSessionStatusQuestion(
      @Nullable String nodes, @Nullable String remoteNodes, @Nullable String status) {
    _nodes = nodes;
    _remoteNodes = remoteNodes;
    _status = status;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return QUESTION_NAME;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_REMOTE_NODES)
  public @Nullable String getRemoteNodes() {
    return _remoteNodes;
  }

  @JsonProperty(PROP_STATUS)
  public String getStatus() {
    return _status;
  }
}
