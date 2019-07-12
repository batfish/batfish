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

  @Nullable private String _nodes;

  @Nullable private String _remoteNodes;

  @Nullable private String _status;

  @JsonCreator
  private static IpsecSessionStatusQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) String remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status) {
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

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nullable
  @JsonProperty(PROP_REMOTE_NODES)
  public String getRemoteNodes() {
    return _remoteNodes;
  }

  @JsonProperty(PROP_STATUS)
  public String getStatus() {
    return _status;
  }
}
