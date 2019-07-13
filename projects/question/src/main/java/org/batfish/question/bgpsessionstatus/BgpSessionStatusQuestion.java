package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.BgpSessionStatus;
import org.batfish.specifier.ConstantEnumSetSpecifier;
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
   * Create a new BGP session status question.
   *
   * @param nodes String that adheres to node specifier grammar to specify the nodes names for one
   *     end of the sessions. Default is all nodes.
   * @param remoteNodes String that adheres to node specifier grammar to specify the nodes names for
   *     the other end of the sessions. Default is all nodes.
   * @param status String that adheres to enum set grammar over {@link BgpSessionStatus}.
   * @param type String that adheres to enum set grammar over {@link SessionType}
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

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof BgpSessionStatusQuestion)) {
      return false;
    }
    BgpSessionStatusQuestion that = (BgpSessionStatusQuestion) o;
    return Objects.equals(_nodes, that._nodes)
        && Objects.equals(_remoteNodes, that._remoteNodes)
        && Objects.equals(_status, that._status)
        && Objects.equals(_type, that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _remoteNodes, _status, _type);
  }
}
