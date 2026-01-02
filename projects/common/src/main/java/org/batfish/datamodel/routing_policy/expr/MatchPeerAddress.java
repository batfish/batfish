package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Evaluates to true when evaluated in an environment with {@link BgpSessionProperties} present and
 * the peer address is any one of the ones in this expression.
 */
@ParametersAreNonnullByDefault
public final class MatchPeerAddress extends BooleanExpr {
  private static final String PROP_PEERS = "peers";
  private final Set<Ip> _peers;

  public MatchPeerAddress(Ip... ips) {
    this(Arrays.asList(ips));
  }

  public MatchPeerAddress(Collection<Ip> ips) {
    checkArgument(!ips.isEmpty(), "Must match at least 1 type");
    _peers = ImmutableSet.copyOf(ips);
  }

  @JsonCreator
  private static MatchPeerAddress create(@JsonProperty(PROP_PEERS) @Nullable Collection<Ip> peers) {
    checkArgument(peers != null, "Missing %s", PROP_PEERS);
    return new MatchPeerAddress(peers);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchPeerAddress(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    @Nullable BgpSessionProperties props = environment.getBgpSessionProperties();
    if (props == null) {
      return new Result(false);
    }
    return new Result(_peers.contains(props.getRemoteIp()));
  }

  @JsonProperty(PROP_PEERS)
  public @Nonnull Set<Ip> getPeers() {
    return _peers;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MatchPeerAddress that)) {
      return false;
    }
    return _peers.equals(that._peers);
  }

  @Override
  public int hashCode() {
    return _peers.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MatchPeerAddress.class).add(PROP_PEERS, _peers).toString();
  }
}
