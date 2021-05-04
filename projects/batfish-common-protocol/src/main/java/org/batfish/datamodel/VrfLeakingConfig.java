package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
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

    public static final class Builder {

      @Nonnull
      public BgpLeakConfig build() {
        return new BgpLeakConfig(_admin, _attachRouteTargets, _weight);
      }

      @Nonnull
      public Builder setAdmin(int admin) {
        _admin = admin;
        return this;
      }

      @Nonnull
      public Builder setAttachRouteTargets(Iterable<ExtendedCommunity> attachRouteTargets) {
        _attachRouteTargets = ImmutableSet.copyOf(attachRouteTargets);
        return this;
      }

      @Nonnull
      public Builder setAttachRouteTargets(ExtendedCommunity... attachRouteTargets) {
        return setAttachRouteTargets(Arrays.asList(attachRouteTargets));
      }

      @Nonnull
      public Builder setWeight(int weight) {
        _weight = weight;
        return this;
      }

      private int _admin;
      @Nonnull private Set<ExtendedCommunity> _attachRouteTargets;
      private int _weight;

      private Builder() {
        _attachRouteTargets = ImmutableSet.of();
      }
    }

    @Nonnull
    public static Builder builder() {
      return new Builder();
    }

    private BgpLeakConfig(int admin, Set<ExtendedCommunity> attachRouteTargets, int weight) {
      _admin = admin;
      _attachRouteTargets = attachRouteTargets;
      _weight = weight;
    }

    /** Administrative distance to apply to leaked routes. */
    @JsonProperty(PROP_ADMIN)
    public int getAdmin() {
      return _admin;
    }

    /** Additional route-targets to attach to a leaked route, on top of any set by policy. */
    @JsonIgnore
    @Nonnull
    public Set<ExtendedCommunity> getAttachRouteTargets() {
      return _attachRouteTargets;
    }

    @JsonProperty(PROP_ATTACH_ROUTE_TARGETS)
    @Nonnull
    private SortedSet<ExtendedCommunity> getAttachRouteTargetsSorted() {
      return ImmutableSortedSet.copyOf(Comparator.naturalOrder(), _attachRouteTargets);
    }

    /** Weight to apply to leaked routes. */
    @JsonProperty(PROP_WEIGHT)
    public int getWeight() {
      return _weight;
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
      return _admin == that._admin
          && _attachRouteTargets.equals(that._attachRouteTargets)
          && _weight == that._weight;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_admin, _attachRouteTargets, _weight);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(BgpLeakConfig.class)
          .add("admin", _admin)
          .add("attachRouteTargets", _attachRouteTargets)
          .add("weight", _weight)
          .toString();
    }

    private final int _admin;
    @Nonnull private final Set<ExtendedCommunity> _attachRouteTargets;
    private final int _weight;

    private static final String PROP_ADMIN = "admin";
    private static final String PROP_ATTACH_ROUTE_TARGETS = "attachRouteTargets";
    private static final String PROP_WEIGHT = "weight";

    @JsonCreator
    private static BgpLeakConfig jsonCreate(
        @JsonProperty(PROP_ADMIN) int admin,
        @Nullable @JsonProperty(PROP_ATTACH_ROUTE_TARGETS)
            Iterable<ExtendedCommunity> attachRouteTarget,
        @JsonProperty(PROP_WEIGHT) int weight) {
      return new BgpLeakConfig(
          admin, ImmutableSet.copyOf(firstNonNull(attachRouteTarget, ImmutableSet.of())), weight);
    }
  }
}
