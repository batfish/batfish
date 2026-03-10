package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration tracking for VRF leaking that should happen from all source VRFs. */
@ParametersAreNonnullByDefault
public class VrfLeakConfig implements Serializable {

  /**
   * VRF leak configs describing how routes should leak between BGP RIBs. Guaranteed empty if {@link
   * #getLeakAsBgp()} is not set
   */
  @JsonProperty(PROP_BGP_VRF_LEAK_CONFIGS)
  public @Nonnull List<BgpVrfLeakConfig> getBgpVrfLeakConfigs() {
    return _bgpVrfLeakConfigs;
  }

  public void addBgpVrfLeakConfig(BgpVrfLeakConfig c) {
    checkArgument(_leakAsBgp, "BGP VRF leak configs cannot be configured unless leakAsBgp is set");
    _bgpVrfLeakConfigs =
        ImmutableList.<BgpVrfLeakConfig>builderWithExpectedSize(_bgpVrfLeakConfigs.size() + 1)
            .addAll(_bgpVrfLeakConfigs)
            .add(c)
            .build();
  }

  /** VRF leak configs describing how routes should leak from BGPv4 RIBs to EVPN RIBs. */
  @JsonProperty(PROP_BGPV4_TO_EVPN_VRF_LEAK_CONFIGS)
  public @Nonnull List<Bgpv4ToEvpnVrfLeakConfig> getBgpv4ToEvpnVrfLeakConfigs() {
    return _bgpv4ToEvpnVrfLeakConfigs;
  }

  public void addBgpv4ToEvpnVrfLeakConfig(Bgpv4ToEvpnVrfLeakConfig c) {
    _bgpv4ToEvpnVrfLeakConfigs =
        ImmutableList.<Bgpv4ToEvpnVrfLeakConfig>builderWithExpectedSize(
                _bgpv4ToEvpnVrfLeakConfigs.size() + 1)
            .addAll(_bgpv4ToEvpnVrfLeakConfigs)
            .add(c)
            .build();
  }

  /** VRF leak configs describing how routes should leak from EVPN RIBs to BGPv4 RIBs. */
  @JsonProperty(PROP_EVPN_TO_BGPV4_VRF_LEAK_CONFIGS)
  public @Nonnull List<EvpnToBgpv4VrfLeakConfig> getEvpnToBgpv4VrfLeakConfigs() {
    return _evpnToBgpv4VrfLeakConfigs;
  }

  public void addEvpnToBgpv4VrfLeakConfig(EvpnToBgpv4VrfLeakConfig c) {
    _evpnToBgpv4VrfLeakConfigs =
        ImmutableList.<EvpnToBgpv4VrfLeakConfig>builderWithExpectedSize(
                _evpnToBgpv4VrfLeakConfigs.size() + 1)
            .addAll(_evpnToBgpv4VrfLeakConfigs)
            .add(c)
            .build();
  }

  /**
   * Whether the node containing this leak configuration leaks routes between BGP RIBs or main RIBs.
   *
   * <p>If leaking as BGP, {@link #getMainRibVrfLeakConfigs()} is guaranteed to be empty, and
   * otherwise {@link #getBgpVrfLeakConfigs} is guaranteed to be empty.
   *
   * <p>For those familiar with vendor semantics: if set to {@code true}, leaking routes as BGP is
   * equivalent to IOS vrf leaking which is done between BGP RIBs of different VRFs (on a real
   * device, via VPNv4 address family). If set to {@code false}, the leaking process more closely
   * follows the Juniper model, where routes are simply copied from the main RIB of one routing
   * instance (read: VRF) into another, with appropriate src-VRF annotation.
   */
  @JsonProperty(PROP_LEAK_AS_BGP)
  public boolean getLeakAsBgp() {
    return _leakAsBgp;
  }

  /**
   * VRF leak configs describing how routes should leak between main RIBs. Guaranteed empty if
   * {@link #getLeakAsBgp()} is set
   */
  @JsonProperty(PROP_MAIN_RIB_VRF_LEAK_CONFIGS)
  public @Nonnull List<MainRibVrfLeakConfig> getMainRibVrfLeakConfigs() {
    return _mainRibVrfLeakConfigs;
  }

  public void addMainRibVrfLeakConfig(@Nonnull MainRibVrfLeakConfig c) {
    checkArgument(
        !_leakAsBgp, "Main RIB VRF leak configs cannot be configured when leakAsBgp is set");
    _mainRibVrfLeakConfigs =
        ImmutableList.<MainRibVrfLeakConfig>builderWithExpectedSize(
                _mainRibVrfLeakConfigs.size() + 1)
            .addAll(_mainRibVrfLeakConfigs)
            .add(c)
            .build();
  }

  public static @Nonnull Builder builder(boolean leakAsBgp) {
    return new Builder(leakAsBgp);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VrfLeakConfig)) {
      return false;
    }
    VrfLeakConfig that = (VrfLeakConfig) o;
    return _leakAsBgp == that._leakAsBgp
        && _bgpVrfLeakConfigs.equals(that._bgpVrfLeakConfigs)
        && _bgpv4ToEvpnVrfLeakConfigs.equals(that._bgpv4ToEvpnVrfLeakConfigs)
        && _evpnToBgpv4VrfLeakConfigs.equals(that._evpnToBgpv4VrfLeakConfigs)
        && _mainRibVrfLeakConfigs.equals(that._mainRibVrfLeakConfigs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _leakAsBgp,
        _bgpVrfLeakConfigs,
        _bgpv4ToEvpnVrfLeakConfigs,
        _evpnToBgpv4VrfLeakConfigs,
        _mainRibVrfLeakConfigs);
  }

  public VrfLeakConfig(boolean leakAsBgp) {
    this(leakAsBgp, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
  }

  private VrfLeakConfig(
      boolean leakAsBgp,
      List<BgpVrfLeakConfig> bgpVrfLeakConfigs,
      List<Bgpv4ToEvpnVrfLeakConfig> bgpv4ToEvpnVrfLeakConfigs,
      List<EvpnToBgpv4VrfLeakConfig> evpnToBgpv4VrfLeakConfigs,
      List<MainRibVrfLeakConfig> mainRibVrfLeakConfigs) {
    if (leakAsBgp) {
      checkArgument(
          mainRibVrfLeakConfigs.isEmpty(),
          "Main RIB VRF leak configs cannot be configured when leakAsBgp is set");
    } else {
      checkArgument(
          bgpVrfLeakConfigs.isEmpty(),
          "BGP VRF leak configs cannot be configured unless leakAsBgp is set");
    }
    _leakAsBgp = leakAsBgp;
    _bgpVrfLeakConfigs = bgpVrfLeakConfigs;
    _bgpv4ToEvpnVrfLeakConfigs = bgpv4ToEvpnVrfLeakConfigs;
    _evpnToBgpv4VrfLeakConfigs = evpnToBgpv4VrfLeakConfigs;
    _mainRibVrfLeakConfigs = mainRibVrfLeakConfigs;
  }

  @JsonCreator
  private static VrfLeakConfig create(
      @JsonProperty(PROP_BGP_VRF_LEAK_CONFIGS) @Nullable List<BgpVrfLeakConfig> bgpVrfLeakConfigs,
      @JsonProperty(PROP_BGPV4_TO_EVPN_VRF_LEAK_CONFIGS) @Nullable
          List<Bgpv4ToEvpnVrfLeakConfig> bgpv4ToEvpnVrfLeakConfigs,
      @JsonProperty(PROP_EVPN_TO_BGPV4_VRF_LEAK_CONFIGS) @Nullable
          List<EvpnToBgpv4VrfLeakConfig> evpnToBgpv4VrfLeakConfigs,
      @JsonProperty(PROP_LEAK_AS_BGP) @Nullable Boolean leakAsBgp,
      @JsonProperty(PROP_MAIN_RIB_VRF_LEAK_CONFIGS) @Nullable
          List<MainRibVrfLeakConfig> mainRibVrfLeakConfigs) {
    return new VrfLeakConfig(
        firstNonNull(leakAsBgp, false),
        bgpVrfLeakConfigs == null ? ImmutableList.of() : ImmutableList.copyOf(bgpVrfLeakConfigs),
        bgpv4ToEvpnVrfLeakConfigs == null
            ? ImmutableList.of()
            : ImmutableList.copyOf(bgpv4ToEvpnVrfLeakConfigs),
        evpnToBgpv4VrfLeakConfigs == null
            ? ImmutableList.of()
            : ImmutableList.copyOf(evpnToBgpv4VrfLeakConfigs),
        mainRibVrfLeakConfigs == null
            ? ImmutableList.of()
            : ImmutableList.copyOf(mainRibVrfLeakConfigs));
  }

  private static final String PROP_BGP_VRF_LEAK_CONFIGS = "bgpVrfLeakConfigs";
  private static final String PROP_BGPV4_TO_EVPN_VRF_LEAK_CONFIGS = "bgpv4ToEvpnVrfLeakConfigs";
  private static final String PROP_EVPN_TO_BGPV4_VRF_LEAK_CONFIGS = "evpnToBgpv4VrfLeakConfigs";
  private static final String PROP_LEAK_AS_BGP = "leakAsBgp";
  private static final String PROP_MAIN_RIB_VRF_LEAK_CONFIGS = "mainRibVrfLeakConfigs";

  private final boolean _leakAsBgp;
  private @Nonnull List<BgpVrfLeakConfig> _bgpVrfLeakConfigs;
  private @Nonnull List<Bgpv4ToEvpnVrfLeakConfig> _bgpv4ToEvpnVrfLeakConfigs;
  private @Nonnull List<EvpnToBgpv4VrfLeakConfig> _evpnToBgpv4VrfLeakConfigs;
  private @Nonnull List<MainRibVrfLeakConfig> _mainRibVrfLeakConfigs;

  public static final class Builder {

    public @Nonnull Builder addBgpVrfLeakConfig(BgpVrfLeakConfig c) {
      _bgpVrfLeakConfigs.add(c);
      return this;
    }

    public @Nonnull Builder addBgpv4ToEvpnVrfLeakConfig(Bgpv4ToEvpnVrfLeakConfig c) {
      _bgpv4ToEvpnVrfLeakConfigs.add(c);
      return this;
    }

    public @Nonnull Builder addEvpnToBgpv4VrfLeakConfig(EvpnToBgpv4VrfLeakConfig c) {
      _evpnToBgpv4VrfLeakConfigs.add(c);
      return this;
    }

    public @Nonnull Builder addMainRibVrfLeakConfig(MainRibVrfLeakConfig c) {
      _mainRibVrfLeakConfigs.add(c);
      return this;
    }

    public @Nonnull VrfLeakConfig build() {
      return new VrfLeakConfig(
          _leakAsBgp,
          _bgpVrfLeakConfigs.build(),
          _bgpv4ToEvpnVrfLeakConfigs.build(),
          _evpnToBgpv4VrfLeakConfigs.build(),
          _mainRibVrfLeakConfigs.build());
    }

    private Builder(boolean leakAsBgp) {
      _leakAsBgp = leakAsBgp;
    }

    private final boolean _leakAsBgp;
    private @Nonnull ImmutableList.Builder<BgpVrfLeakConfig> _bgpVrfLeakConfigs =
        ImmutableList.builder();
    private @Nonnull ImmutableList.Builder<Bgpv4ToEvpnVrfLeakConfig> _bgpv4ToEvpnVrfLeakConfigs =
        ImmutableList.builder();
    private @Nonnull ImmutableList.Builder<EvpnToBgpv4VrfLeakConfig> _evpnToBgpv4VrfLeakConfigs =
        ImmutableList.builder();
    private @Nonnull ImmutableList.Builder<MainRibVrfLeakConfig> _mainRibVrfLeakConfigs =
        ImmutableList.builder();
  }
}
