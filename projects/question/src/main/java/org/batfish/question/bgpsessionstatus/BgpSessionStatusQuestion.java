package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.BgpSessionStatus;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.EnumSetSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.parboiled.Grammar;

/** Based on node configurations, determines the status of IBGP and EBGP sessions. */
public class BgpSessionStatusQuestion extends BgpSessionQuestion {

  // caches the set of statuses we got (optimization)
  @Nonnull private final Set<BgpSessionStatus> _expandedStatuses;

  @JsonCreator
  private static BgpSessionStatusQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) String remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status,
      @Nullable @JsonProperty(PROP_TYPE) String type) {
    return new BgpSessionStatusQuestion(nodes, remoteNodes, status, type);
  }

  /** Create a new BGP session status question with default parameters. */
  public BgpSessionStatusQuestion() {
    this(null, null, null, null);
  }

  /**
   * Creates a new BGP session status question.
   *
   * @param nodes Regular expression to match the nodes names for one end of the sessions. Default
   *     is '.*' (all nodes).
   * @param remoteNodes Regular expression to match the nodes names for the other end of the
   *     sessions. Default is '.*' (all nodes).
   * @param status {@link EnumSetSpecifier} over {@link BgpSessionStatus}
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  public BgpSessionStatusQuestion(
      @Nullable String nodes,
      @Nullable String remoteNodes,
      @Nullable String status,
      @Nullable String type) {
    super(nodes, remoteNodes, status, type);
    _expandedStatuses =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                status,
                Grammar.BGP_SESSION_STATUS_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.copyOf(BgpSessionStatus.values())))
            .resolve();
  }

  boolean matchesStatus(BgpSessionStatus status) {
    return _expandedStatuses.contains(status);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "bgpSessionStatus";
  }
}
