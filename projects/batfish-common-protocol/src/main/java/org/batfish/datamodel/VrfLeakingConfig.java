package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/**
 * Configuration responsible for VRF leaking. To successfully leak routes from one VRF to another,
 * the destination VRF should contain one or more {@link VrfLeakingConfig} objects.
 *
 * <p>This object describes <em>how</em> to leak the routes, in particular:
 *
 * <ul>
 *   <li>from which source VRF to pull routes
 *   <li>what import policy to apply, if any
 *   <li>whether to leak routes as BGP or not
 * </ul>
 *
 * <p><b>Note on BGP leaking: please see {@link #leakAsBgp()} for full explanation of semantics</b>
 */
@ParametersAreNonnullByDefault
public final class VrfLeakingConfig implements Serializable {

  /**
   * Name of the import policy to apply to imported routes when leaking. If {@code null} no policy
   * is applied, all routes are allowed.
   */
  @Nullable
  @JsonProperty(PROP_IMPORT_POLICY)
  public String getImportPolicy() {
    return _importPolicy;
  }

  /**
   * Name of the source VRF from which to copy routes.
   *
   * <p>If {@link #leakAsBgp()} is set, the source VRF <b>must</b> have a BGP process/RIBs.
   */
  @Nonnull
  @JsonProperty(PROP_IMPORT_FROM_VRF)
  public String getImportFromVrf() {
    return _importFromVrf;
  }

  /** Knobs that control Cisco-IOS-style VRF leaking */
  @Nullable
  @JsonProperty(PROP_BGP_CONFIG)
  public BgpLeakConfig getBgpConfig() {
    return _bgpConfig;
  }

  /**
   * Whether or not to leak routes between BGP RIBs or main RIBs. True if {@link #getBgpConfig()} is
   * not null.
   *
   * <p>For those familiar with vendor semantics: if set to {@code true}, leaking routes as BGP is
   * equivalent to IOS vrf leaking which is done between BGP RIBs of different VRFs (on a real
   * device, via VPNv4 address family). If set to {@code false}, the leaking process more closely
   * follows the Juniper model, where routes are simply copied from the main RIB of one routing
   * instance (read: VRF) into another, with appropriate src-VRF annotation.
   */
  public boolean leakAsBgp() {
    return _bgpConfig != null;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VrfLeakingConfig)) {
      return false;
    }
    VrfLeakingConfig that = (VrfLeakingConfig) o;
    return _importFromVrf.equals(that._importFromVrf)
        && Objects.equals(_importPolicy, that._importPolicy)
        && Objects.equals(_bgpConfig, that._bgpConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_importPolicy, _importFromVrf, _bgpConfig);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(VrfLeakingConfig.class)
        .omitNullValues()
        .add("importPolicy", _importPolicy)
        .add("importFromVrf", _importFromVrf)
        .add("bgpConfig", _bgpConfig)
        .toString();
  }

  @Nullable private final String _importPolicy;
  @Nonnull private final String _importFromVrf;
  @Nullable private final BgpLeakConfig _bgpConfig;

  private VrfLeakingConfig(Builder builder) {
    checkArgument(builder._importFromVrf != null, "VRF leaking config is missing import VRF");
    _importPolicy = builder._importPolicy;
    _importFromVrf = builder._importFromVrf;
    _bgpConfig = builder._bgpConfig;
  }

  private static final String PROP_IMPORT_FROM_VRF = "importFromVrf";
  private static final String PROP_IMPORT_POLICY = "importPolicy";
  private static final String PROP_BGP_CONFIG = "bgpConfig";

  @JsonCreator
  private static VrfLeakingConfig create(
      @Nullable @JsonProperty(PROP_IMPORT_FROM_VRF) String importFromVrf,
      @Nullable @JsonProperty(PROP_IMPORT_POLICY) String importPolicy,
      @Nullable @JsonProperty(PROP_BGP_CONFIG) BgpLeakConfig bgpConfig) {
    checkArgument(importFromVrf != null, String.format("Missing %s", PROP_IMPORT_FROM_VRF));
    return builder()
        .setImportFromVrf(importFromVrf)
        .setImportPolicy(importPolicy)
        .setBgpLeakConfig(bgpConfig)
        .build();
  }

  @ParametersAreNonnullByDefault
  public static class Builder {

    public Builder setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return this;
    }

    public Builder setImportFromVrf(String importFromVrf) {
      _importFromVrf = importFromVrf;
      return this;
    }

    public Builder setBgpLeakConfig(@Nullable BgpLeakConfig config) {
      _bgpConfig = config;
      return this;
    }

    public VrfLeakingConfig build() {
      return new VrfLeakingConfig(this);
    }

    @Nullable private String _importPolicy;
    @Nullable private String _importFromVrf;
    @Nullable private BgpLeakConfig _bgpConfig;

    private Builder() {}
  }

  public static final class BgpLeakConfig implements Serializable {

    public BgpLeakConfig(ExtendedCommunity attachRouteTarget) {
      _attachRouteTarget = attachRouteTarget;
    }

    public ExtendedCommunity getAttachRouteTarget() {
      return _attachRouteTarget;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof BgpLeakConfig)) {
        return false;
      }
      BgpLeakConfig that = (BgpLeakConfig) o;
      return _attachRouteTarget.equals(that._attachRouteTarget);
    }

    @Override
    public int hashCode() {
      return _attachRouteTarget.hashCode();
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(BgpLeakConfig.class)
          .add("attachRouteTarget", _attachRouteTarget)
          .toString();
    }

    @Nonnull private final ExtendedCommunity _attachRouteTarget;
    private static final String PROP_ATTACH_ROUTE_TARGET = "attachRouteTarget";

    @JsonCreator
    private static BgpLeakConfig jsonCreate(
        @Nullable @JsonProperty(PROP_ATTACH_ROUTE_TARGET) ExtendedCommunity attachRouteTarget) {
      checkArgument(attachRouteTarget != null, "Missing %s", PROP_ATTACH_ROUTE_TARGET);
      return new BgpLeakConfig(attachRouteTarget);
    }
  }
}
