package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.EnumSetSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.parboiled.Grammar;

/** Based on node configurations, determines the compatibility of IBGP and EBGP sessions. */
public class BgpSessionCompatibilityQuestion extends BgpSessionQuestion {

  // caches the set of statuses we got (optimization)
  @Nonnull private final Set<ConfiguredSessionStatus> _expandedStatuses;

  @JsonCreator
  private static BgpSessionCompatibilityQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) String remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status,
      @Nullable @JsonProperty(PROP_TYPE) String type) {
    return new BgpSessionCompatibilityQuestion(nodes, remoteNodes, status, type);
  }

  /** Create a new BGP session compatibility question with default parameters. */
  public BgpSessionCompatibilityQuestion() {
    this(null, null, null, null);
  }

  /**
   * Create a new BGP session compatibility question.
   *
   * @param nodes Regular expression to match the nodes names for one end of the sessions. Default
   *     is '.*' (all nodes).
   * @param remoteNodes Regular expression to match the nodes names for the other end of the
   *     sessions. Default is '.*' (all nodes).
   * @param status {@link EnumSetSpecifier} over {@link ConfiguredSessionStatus}.
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  public BgpSessionCompatibilityQuestion(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) String remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status,
      @Nullable @JsonProperty(PROP_TYPE) String type) {
    super(nodes, remoteNodes, status, type);
    _expandedStatuses =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                status,
                Grammar.BGP_SESSION_COMPAT_STATUS_SPECIFIER,
                new ConstantEnumSetSpecifier<>(
                    ImmutableSet.copyOf(ConfiguredSessionStatus.values())))
            .resolve();
  }

  boolean matchesStatus(ConfiguredSessionStatus status) {
    return _expandedStatuses.contains(status);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "bgpSessionCompatibility";
  }
}
