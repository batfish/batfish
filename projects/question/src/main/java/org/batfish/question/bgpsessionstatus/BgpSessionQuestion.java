package org.batfish.question.bgpsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.SessionStatus;

/** Based on node configurations, determines the status of IBGP and EBGP sessions. */
public abstract class BgpSessionQuestion extends Question {

  public static final String PROP_NODES = "nodes";

  public static final String PROP_REMOTE_NODES = "remoteNodes";

  public static final String PROP_STATUS = "status";

  public static final String PROP_TYPE = "type";

  @Nonnull protected NodesSpecifier _nodes;

  @Nonnull protected NodesSpecifier _remoteNodes;

  @Nonnull protected Pattern _status;

  @Nonnull protected Pattern _type;

  /**
   * Create a new BGP session question.
   *
   * @param nodes Regular expression to match the nodes names for one end of the sessions. Default
   *     is '.*' (all nodes).
   * @param remoteNodes Regular expression to match the nodes names for the other end of the
   *     sessions. Default is '.*' (all nodes).
   * @param status Regular expression to match status type (see {@link ConfiguredSessionStatus})
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  public BgpSessionQuestion(
      @Nullable NodesSpecifier nodes,
      @Nullable NodesSpecifier remoteNodes,
      @Nullable String status,
      @Nullable String type) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
    _remoteNodes = firstNonNull(remoteNodes, NodesSpecifier.ALL);
    _status =
        Strings.isNullOrEmpty(status)
            ? Pattern.compile(".*")
            : Pattern.compile(status.toUpperCase());
    _type =
        Strings.isNullOrEmpty(type) ? Pattern.compile(".*") : Pattern.compile(type.toUpperCase());
  }

  boolean matchesStatus(@Nullable ConfiguredSessionStatus status) {
    return status != null && _status.matcher(status.toString()).matches();
  }

  boolean matchesStatus(@Nullable SessionStatus status) {
    return status != null && _status.matcher(status.toString()).matches();
  }

  boolean matchesType(SessionType type) {
    return _type.matcher(type.toString()).matches();
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_REMOTE_NODES)
  public NodesSpecifier getRemoteNodes() {
    return _remoteNodes;
  }

  @JsonProperty(PROP_STATUS)
  protected String getStatus() {
    return _status.toString();
  }

  @JsonProperty(PROP_TYPE)
  protected String getType() {
    return _type.toString();
  }
}
