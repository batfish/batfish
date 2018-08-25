package org.batfish.question.bgpsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;

/** Based on node configurations, determines the status of IBGP and EBGP sessions. */
public class BgpSessionStatusQuestion extends Question {

  private static final String PROP_FOREIGN_BGP_GROUPS = "foreignBgpGroups";

  private static final String PROP_INCLUDE_ESTABLISHED_COUNT = "includeEstablishedCount";

  private static final String PROP_NODES = "nodes";

  private static final String PROP_REMOTE_NODES = "remoteNodes";

  private static final String PROP_STATUS = "status";

  private static final String PROP_TYPE = "type";

  @Nonnull private SortedSet<String> _foreignBgpGroups;

  private boolean _includeEstablishedCount;

  @Nonnull private NodesSpecifier _nodes;

  @Nonnull private NodesSpecifier _remoteNodes;

  @Nonnull private Pattern _status;

  @Nonnull private Pattern _type;

  /** Create a new BGP Session question with default parameters. */
  public BgpSessionStatusQuestion() {
    this(null, false, null, null, null, null);
  }

  /**
   * Create a new BGP Session question.
   *
   * @param foreignBgpGroups only look at peers that belong to a given named BGP group.
   * @param includeEstablishedCount run post-dataplane analysis to see how many sessions get
   *     actually established.
   * @param nodes Regular expression to match the nodes names for one end of the sessions. Default
   *     is '.*' (all nodes).
   * @param remoteNodes Regular expression to match the nodes names for the other end of the
   *     sessions. Default is '.*' (all nodes).
   * @param status Regular expression to match status type (see {@link
   *     BgpSessionInfo.SessionStatus})
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  @JsonCreator
  public BgpSessionStatusQuestion(
      @Nullable @JsonProperty(PROP_FOREIGN_BGP_GROUPS) SortedSet<String> foreignBgpGroups,
      @Nullable @JsonProperty(PROP_INCLUDE_ESTABLISHED_COUNT) Boolean includeEstablishedCount,
      @Nullable @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) NodesSpecifier remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status,
      @Nullable @JsonProperty(PROP_TYPE) String type) {
    _foreignBgpGroups = firstNonNull(foreignBgpGroups, ImmutableSortedSet.of());
    _includeEstablishedCount = firstNonNull(includeEstablishedCount, Boolean.FALSE);
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
    _remoteNodes = firstNonNull(remoteNodes, NodesSpecifier.ALL);
    _status =
        Strings.isNullOrEmpty(status)
            ? Pattern.compile(".*")
            : Pattern.compile(status.toUpperCase());
    _type =
        Strings.isNullOrEmpty(type) ? Pattern.compile(".*") : Pattern.compile(type.toUpperCase());
  }

  @Override
  public boolean getDataPlane() {
    return _includeEstablishedCount;
  }

  @JsonProperty(PROP_FOREIGN_BGP_GROUPS)
  private SortedSet<String> getForeignBgpGroups() {
    return _foreignBgpGroups;
  }

  @JsonProperty(PROP_INCLUDE_ESTABLISHED_COUNT)
  public boolean getIncludeEstablishedCount() {
    return _includeEstablishedCount;
  }

  @Override
  public String getName() {
    return "bgpSessionStatusNew";
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
  private String getStatus() {
    return _status.toString();
  }

  @JsonProperty(PROP_TYPE)
  private String getType() {
    return _type.toString();
  }

  boolean matchesForeignGroup(String group) {
    return _foreignBgpGroups.contains(group);
  }

  boolean matchesStatus(@Nullable SessionStatus status) {
    return status != null && _status.matcher(status.toString()).matches();
  }

  boolean matchesType(SessionType type) {
    return _type.matcher(type.toString()).matches();
  }
}
