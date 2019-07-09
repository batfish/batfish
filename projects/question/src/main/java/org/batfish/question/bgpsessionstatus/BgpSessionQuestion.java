package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.SessionStatus;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.parboiled.Grammar;

/** Based on node configurations, determines the status of IBGP and EBGP sessions. */
public abstract class BgpSessionQuestion extends Question {

  private static final String MATCH_ALL = ".*";

  public static final String PROP_NODES = "nodes";

  public static final String PROP_REMOTE_NODES = "remoteNodes";

  public static final String PROP_STATUS = "status";

  public static final String PROP_TYPE = "type";

  @Nullable protected final String _nodes;

  @Nullable protected final String _remoteNodes;

  @Nonnull protected final Pattern _status;

  @Nullable protected final String _type;

  // caches what _type expands to (optimization)
  @Nonnull private final Set<SessionType> _expandedTypes;

  /**
   * Create a new BGP session question.
   *
   * @param nodes {@link NodesSpecifier} to specify matching local nodes. Default is all nodes.
   * @param remoteNodes {@link NodesSpecifier} to specify matching remote nodes. Default is all
   *     nodes.
   * @param status Regular expression to match status type (see {@link ConfiguredSessionStatus} and
   *     {@link SessionStatus})
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  public BgpSessionQuestion(
      @Nullable String nodes,
      @Nullable String remoteNodes,
      @Nullable String status,
      @Nullable String type) {
    _nodes = nodes;
    _remoteNodes = remoteNodes;
    _status =
        Strings.isNullOrEmpty(status)
            ? Pattern.compile(MATCH_ALL)
            : Pattern.compile(status.toUpperCase());
    _type = type;
    _expandedTypes =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                type,
                Grammar.BGP_SESSION_TYPE_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.copyOf(SessionType.values())))
            .resolve();
  }

  boolean matchesStatus(@Nullable String status) {
    return _status.pattern().equals(MATCH_ALL)
        || (status != null && _status.matcher(status).matches());
  }

  boolean matchesType(SessionType sessionType) {
    return _expandedTypes.contains(sessionType);
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  public NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @Nullable
  @JsonProperty(PROP_REMOTE_NODES)
  public String getRemoteNodes() {
    return _remoteNodes;
  }

  @JsonIgnore
  public NodeSpecifier getRemoteNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _remoteNodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_STATUS)
  protected String getStatus() {
    return _status.toString();
  }

  @JsonProperty(PROP_TYPE)
  protected String getType() {
    return _type;
  }
}
