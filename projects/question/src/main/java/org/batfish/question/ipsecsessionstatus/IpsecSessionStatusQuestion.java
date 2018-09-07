package org.batfish.question.ipsecsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ipsecsessionstatus.IpsecSessionInfo.IpsecSessionStatus;

/** Return status of all IPSec sessions in the network */
public class IpsecSessionStatusQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  private static final String PROP_REMOTE_NODES = "remoteNodes";

  private static final String PROP_STATUS = "status";

  private static final String QUESTION_NAME = "ipsecSessionStatus";

  @Nonnull private NodesSpecifier _nodes;

  @Nonnull private NodesSpecifier _remoteNodes;

  @Nonnull private Pattern _status;

  @JsonCreator
  public IpsecSessionStatusQuestion(
      @Nullable @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) NodesSpecifier remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
    _remoteNodes = firstNonNull(remoteNodes, NodesSpecifier.ALL);
    _status =
        Strings.isNullOrEmpty(status)
            ? Pattern.compile(".*")
            : Pattern.compile(status.toUpperCase());
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
  public NodesSpecifier getInitiatorRegex() {
    return _nodes;
  }

  @JsonProperty(PROP_REMOTE_NODES)
  public NodesSpecifier getResponderRegex() {
    return _remoteNodes;
  }

  @JsonProperty(PROP_STATUS)
  public String getStatus() {
    return _status.toString();
  }

  boolean matchesStatus(@Nullable IpsecSessionStatus status) {
    return status != null && _status.matcher(status.toString()).matches();
  }
}
