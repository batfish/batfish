package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.EnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Based on node configurations, determines the status of IBGP and EBGP sessions. */
public abstract class BgpSessionQuestion extends Question {

  public static final String PROP_NODES = "nodes";

  public static final String PROP_REMOTE_NODES = "remoteNodes";

  public static final String PROP_STATUS = "status";

  public static final String PROP_TYPE = "type";

  protected final @Nullable String _nodes;

  protected final @Nullable String _remoteNodes;

  protected final @Nullable String _status;

  protected final @Nullable String _type;

  // caches what _type expands to (optimization)
  private final @Nonnull Set<SessionType> _expandedTypes;

  /**
   * Create a new BGP session question.
   *
   * @param nodes {@link NodesSpecifier} to specify matching local nodes. Default is all nodes.
   * @param remoteNodes {@link NodesSpecifier} to specify matching remote nodes. Default is all
   *     nodes.
   * @param status {@link EnumSetSpecifier} over one of the supported status enums
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  public BgpSessionQuestion(
      @Nullable String nodes,
      @Nullable String remoteNodes,
      @Nullable String status,
      @Nullable String type) {
    _nodes = nodes;
    _remoteNodes = remoteNodes;
    _status = status;
    _type = type;
    _expandedTypes =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                type,
                Grammar.BGP_SESSION_TYPE_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.copyOf(SessionType.values())))
            .resolve();
  }

  boolean matchesType(SessionType sessionType) {
    return _expandedTypes.contains(sessionType);
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  public NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_REMOTE_NODES)
  public @Nullable String getRemoteNodes() {
    return _remoteNodes;
  }

  @JsonIgnore
  public NodeSpecifier getRemoteNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _remoteNodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_STATUS)
  protected @Nullable String getStatus() {
    return _status;
  }

  @JsonProperty(PROP_TYPE)
  protected @Nullable String getType() {
    return _type;
  }
}
