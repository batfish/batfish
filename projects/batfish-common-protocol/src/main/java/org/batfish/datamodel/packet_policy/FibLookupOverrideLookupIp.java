package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Perform a FIB lookup for one (or more) of the {@link #getIps() next hop IPs}. After the first IP
 * is successfully resolved, send the packet according to the resolution process with ARP IP being
 * the the chosen IP. If resolution of all IPs fails, execute the {@link #getDefaultAction() default
 * action}.
 *
 * <p>Unless overriden by (see {@link Builder#setRequireConnected(boolean)}, the IP needs to be
 * matched by a connected route ({@link #requireConnected()}.
 *
 * <p><strong>Note:</strong>
 *
 * <ul>
 *   <li>This does not modify the packet
 *   <li>This does not load-balance/EMCP across all IPs, the {@link #getIps() override IPs} are
 *       strictly ordered by preference
 * </ul>
 */
public final class FibLookupOverrideLookupIp implements Action {

  private static final String PROP_IPS = "ips";
  private static final String PROP_DEFAULT_ACTION = "defaultAction";
  private static final String PROP_REQUIRE_CONNECTED = "requireConnected";
  private static final String PROP_VRF_EXPR = "vrfExpr";

  @Nonnull private final List<Ip> _ips;
  @Nonnull private final VrfExpr _vrfExpr;
  @Nonnull private final Action _defaultAction;
  private final boolean _requireConnected;

  private FibLookupOverrideLookupIp(
      List<Ip> ips, VrfExpr vrfExpr, boolean requireConnected, Action defaultAction) {
    _requireConnected = requireConnected;
    checkArgument(!ips.isEmpty(), "%s must not be empty", PROP_IPS);
    _ips = ips;
    _vrfExpr = vrfExpr;
    _defaultAction = defaultAction;
  }

  /** Return a list of IPs to use as the next hop, ordered by precedence */
  @Nonnull
  @JsonProperty(PROP_IPS)
  public List<Ip> getIps() {
    return _ips;
  }

  /** The default action to be taken if all IPs cannot be resolved */
  @Nonnull
  @JsonProperty(PROP_DEFAULT_ACTION)
  public Action getDefaultAction() {
    return _defaultAction;
  }

  /** {@link VrfExpr} which encodes in which VRF to do the lookup. */
  @Nonnull
  @JsonProperty(PROP_VRF_EXPR)
  public VrfExpr getVrfExpr() {
    return _vrfExpr;
  }

  /** Whether the route that matches the next hop IP needs to be connected */
  @JsonProperty(PROP_REQUIRE_CONNECTED)
  public boolean requireConnected() {
    return _requireConnected;
  }

  @Override
  public <T> T accept(ActionVisitor<T> visitor) {
    // TODO: plug into visitors
    return null;
  }

  public static Builder builder() {
    return new Builder();
  }

  // Boiler plate below

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FibLookupOverrideLookupIp)) {
      return false;
    }
    FibLookupOverrideLookupIp that = (FibLookupOverrideLookupIp) o;
    return _ips.equals(that._ips)
        && _requireConnected == that._requireConnected
        && _vrfExpr.equals(that._vrfExpr)
        && _defaultAction.equals(that._defaultAction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ips, _requireConnected, _vrfExpr, _defaultAction);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FibLookupOverrideLookupIp.class)
        .add(PROP_IPS, _ips)
        .add(PROP_REQUIRE_CONNECTED, _requireConnected)
        .add(PROP_VRF_EXPR, _vrfExpr)
        .add(PROP_DEFAULT_ACTION, _defaultAction)
        .toString();
  }

  @JsonCreator
  private static FibLookupOverrideLookupIp jsonCreator(
      @Nullable @JsonProperty(PROP_IPS) List<Ip> ips,
      @Nullable @JsonProperty(PROP_VRF_EXPR) VrfExpr vrfExpr,
      @Nullable @JsonProperty(PROP_REQUIRE_CONNECTED) Boolean requireConnected,
      @Nullable @JsonProperty(PROP_DEFAULT_ACTION) Action defaultAction) {
    checkArgument(ips != null, "Missing %s", PROP_IPS);
    checkArgument(vrfExpr != null, "Missing %s", PROP_VRF_EXPR);
    checkArgument(requireConnected != null, "Missing %s", PROP_REQUIRE_CONNECTED);
    checkArgument(defaultAction != null, "Missing %s", PROP_DEFAULT_ACTION);
    return new FibLookupOverrideLookupIp(ips, vrfExpr, requireConnected, defaultAction);
  }

  public static final class Builder {
    @Nullable private List<Ip> _ips;
    @Nullable private VrfExpr _vrfExpr;
    @Nullable private Action _defaultAction;
    // This is the common case: next hop needs to be directly connected
    private boolean _requireConnected = true;

    private Builder() {}

    @Nonnull
    public Builder setIps(List<Ip> ips) {
      _ips = ips;
      return this;
    }

    @Nonnull
    public Builder setVrfExpr(VrfExpr vrfExpr) {
      _vrfExpr = vrfExpr;
      return this;
    }

    @Nonnull
    public Builder setDefaultAction(Action defaultAction) {
      _defaultAction = defaultAction;
      return this;
    }

    @Nonnull
    public Builder setRequireConnected(boolean requireConnected) {
      _requireConnected = requireConnected;
      return this;
    }

    @Nonnull
    public FibLookupOverrideLookupIp build() {
      checkArgument(_ips != null, "Missing %s", PROP_IPS);
      checkArgument(_vrfExpr != null, "Missing %s", PROP_VRF_EXPR);
      checkArgument(_defaultAction != null, "Missing %s", PROP_DEFAULT_ACTION);
      return new FibLookupOverrideLookupIp(_ips, _vrfExpr, _requireConnected, _defaultAction);
    }
  }
}
