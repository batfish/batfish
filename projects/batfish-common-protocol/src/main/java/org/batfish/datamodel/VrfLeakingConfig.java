package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

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

  /**
   * Whether or not to leak routes between BGP RIBs or main RIBs.
   *
   * <p>For those familiar with vendor semantics: if set to {@code true}, leaking routes as BGP is
   * equivalent to IOS vrf leaking which is done between BGP RIBs of different VRFs (on a real
   * device, via VPNv4 address family). If set to {@code false}, the leaking process more closely
   * follows the Juniper model, where routes are simply copied from the main RIB of one routing
   * instance (read: VRF) into another, with appropriate src-VRF annotation.
   */
  @JsonProperty(PROP_LEAK_AS_BGP)
  public boolean leakAsBgp() {
    return _leakAsBgp;
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
        && _leakAsBgp == that._leakAsBgp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_importPolicy, _importFromVrf, _leakAsBgp);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(VrfLeakingConfig.class)
        .add("importPolicy", _importPolicy)
        .add("importFromVrf", _importFromVrf)
        .add("leakAsBgp", _leakAsBgp)
        .toString();
  }

  @Nullable private final String _importPolicy;
  @Nonnull private final String _importFromVrf;
  private final boolean _leakAsBgp;

  private VrfLeakingConfig(Builder builder) {
    checkArgument(builder._importFromVrf != null, "VRF leaking config is missing import VRF");
    _importPolicy = builder._importPolicy;
    _importFromVrf = builder._importFromVrf;
    _leakAsBgp = builder._leakAsBgp;
  }

  private static final String PROP_IMPORT_FROM_VRF = "importFromVrf";
  private static final String PROP_IMPORT_POLICY = "importPolicy";
  private static final String PROP_LEAK_AS_BGP = "leakAsBgp";

  @JsonCreator
  private static VrfLeakingConfig create(
      @Nullable @JsonProperty(PROP_IMPORT_FROM_VRF) String importFromVrf,
      @Nullable @JsonProperty(PROP_IMPORT_POLICY) String importPolicy,
      @Nullable @JsonProperty(PROP_LEAK_AS_BGP) Boolean leakAsBgp) {
    checkArgument(importFromVrf != null, String.format("Missing %s", PROP_IMPORT_FROM_VRF));
    return builder()
        .setImportFromVrf(importFromVrf)
        .setImportPolicy(importPolicy)
        .setLeakAsBgp(firstNonNull(leakAsBgp, Boolean.FALSE))
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

    public Builder setLeakAsBgp(boolean leakAsBgp) {
      _leakAsBgp = leakAsBgp;
      return this;
    }

    public VrfLeakingConfig build() {
      return new VrfLeakingConfig(this);
    }

    @Nullable private String _importPolicy;
    @Nullable private String _importFromVrf;
    private boolean _leakAsBgp;

    private Builder() {}
  }
}
