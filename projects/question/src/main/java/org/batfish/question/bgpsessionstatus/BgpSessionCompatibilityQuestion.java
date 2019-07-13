package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.specifier.ConstantEnumSetSpecifier;
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
   * @param nodes String that adheres to node specifier grammar to specify the nodes names for one
   *     end of the sessions. Default is all nodes.
   * @param remoteNodes String that adheres to node specifier grammar to specify the nodes names for
   *     the other end of the sessions. Default is all nodes.
   * @param status String that adheres to enum set grammar over {@link ConfiguredSessionStatus}.
   * @param type String that adheres to enum set grammar over {@link SessionType}
   */
  public BgpSessionCompatibilityQuestion(
      @Nullable String nodes,
      @Nullable String remoteNodes,
      @Nullable String status,
      @Nullable String type) {
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

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof BgpSessionCompatibilityQuestion)) {
      return false;
    }
    BgpSessionCompatibilityQuestion that = (BgpSessionCompatibilityQuestion) o;
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
