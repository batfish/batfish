package org.batfish.question.ospfsession;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ospf.OspfSessionStatus;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.parboiled.Grammar;

/** A {@link Question} that returns compatible OSPF sessions */
public class OspfSessionCompatibilityQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_REMOTE_NODES = "remoteNodes";
  private static final String PROP_STATUSES = "statuses";

  @Nullable private String _nodes;

  @Nullable private String _remoteNodes;

  @Nullable private String _statuses;

  @Nonnull private final Set<OspfSessionStatus> _expandedStatuses;

  @JsonCreator
  public OspfSessionCompatibilityQuestion(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_REMOTE_NODES) @Nullable String remoteNodes,
      @JsonProperty(PROP_STATUSES) @Nullable String statuses) {
    _nodes = nodes;
    _remoteNodes = remoteNodes;
    _statuses = statuses;
    // Generate set of all statuses matching the status specifier
    _expandedStatuses =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                statuses,
                Grammar.OSPF_SESSION_STATUS_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.copyOf(OspfSessionStatus.values())))
            .resolve();
  }

  /** Return the set of all statuses matching the status specifier */
  Set<OspfSessionStatus> getStatusSet() {
    return _expandedStatuses;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "ospfSessionCompatibility";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_REMOTE_NODES)
  @Nullable
  public String getremoteNodes() {
    return _remoteNodes;
  }

  @JsonProperty(PROP_STATUSES)
  @Nullable
  public String getStatuses() {
    return _statuses;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getRemoteNodesSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _remoteNodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
