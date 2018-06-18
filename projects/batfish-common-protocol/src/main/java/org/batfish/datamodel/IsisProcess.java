package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableSet;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonSchemaDescription("An IS-IS routing process")
public class IsisProcess implements Serializable {

  public static class Builder {

    private Set<GeneratedRoute> _generatedRoutes;

    private IsisLevelSettings _level1;

    private IsisLevelSettings _level2;

    private IsoAddress _netAddress;

    private Integer _overloadTimeout;

    private Double _referenceBandwidth;

    private Vrf _vrf;

    private Builder() {
      _generatedRoutes = ImmutableSet.of();
    }

    public IsisProcess build() {
      IsisProcess proc =
          new IsisProcess(
              _generatedRoutes,
              _level1,
              _level2,
              requireNonNull(_netAddress),
              _overloadTimeout,
              _referenceBandwidth);
      if (_vrf != null) {
        _vrf.setIsisProcess(proc);
      }
      return proc;
    }

    public @Nonnull Builder setGeneratedRoutes(@Nonnull Set<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
      return this;
    }

    public @Nonnull Builder setLevel1(@Nullable IsisLevelSettings level1) {
      _level1 = level1;
      return this;
    }

    public @Nonnull Builder setLevel2(@Nullable IsisLevelSettings level2) {
      _level2 = level2;
      return this;
    }

    public @Nonnull Builder setNetAddress(@Nonnull IsoAddress netAddress) {
      _netAddress = netAddress;
      return this;
    }

    public @Nonnull Builder setOverloadTimeout(@Nullable Integer overloadTimeout) {
      _overloadTimeout = overloadTimeout;
      return this;
    }

    public @Nonnull Builder setReferenceBandwidth(@Nullable Double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
      return this;
    }

    public @Nonnull Builder setVrf(@Nonnull Vrf vrf) {
      _vrf = vrf;
      return this;
    }
  }

  public static final int DEFAULT_ISIS_INTERFACE_COST = 10;

  private static final String PROP_GENERATED_ROUTES = "generatedRoutes";

  private static final String PROP_LEVEL1 = "level1";

  private static final String PROP_LEVEL2 = "level2";

  private static final String PROP_NET_ADDRESS = "netAddress";

  private static final String PROP_OVERLOAD_TIMEOUT = "overloadTimeout";

  private static final String PROP_REFERENCE_BANDWIDTH = "referenceBandwidth";

  /** */
  private static final long serialVersionUID = 1L;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private Set<GeneratedRoute> _generatedRoutes;

  private IsisLevelSettings _level1;

  private IsisLevelSettings _level2;

  private IsoAddress _netAddress;

  private Integer _overloadTimeout;

  private Double _referenceBandwidth;

  private IsisProcess(
      @JsonProperty(PROP_GENERATED_ROUTES) @Nonnull Set<GeneratedRoute> generatedRoutes,
      @JsonProperty(PROP_LEVEL1) @Nullable IsisLevelSettings level1,
      @JsonProperty(PROP_LEVEL2) @Nullable IsisLevelSettings level2,
      @JsonProperty(PROP_NET_ADDRESS) @Nonnull IsoAddress netAddress,
      @JsonProperty(PROP_OVERLOAD_TIMEOUT) @Nullable Integer overloadTimeout,
      @JsonProperty(PROP_REFERENCE_BANDWIDTH) @Nullable Double referenceBandwidth) {
    _generatedRoutes = generatedRoutes;
    _level1 = level1;
    _level2 = level2;
    _netAddress = netAddress;
    _overloadTimeout = overloadTimeout;
    _referenceBandwidth = referenceBandwidth;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IsisProcess)) {
      return false;
    }
    IsisProcess rhs = (IsisProcess) obj;
    return Objects.equals(_generatedRoutes, rhs._generatedRoutes)
        && Objects.equals(_level1, rhs._level1)
        && Objects.equals(_level2, rhs._level2)
        && Objects.equals(_netAddress, rhs._netAddress)
        && Objects.equals(_overloadTimeout, rhs._overloadTimeout)
        && Objects.equals(_referenceBandwidth, rhs._referenceBandwidth);
  }

  @JsonPropertyDescription(
      "Generated IPV4 routes for the purpose of export into IS-IS. These routes are not imported "
          + "into the main RIB.")
  @JsonProperty(PROP_GENERATED_ROUTES)
  public Set<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @JsonProperty(PROP_LEVEL1)
  public @Nullable IsisLevelSettings getLevel1() {
    return _level1;
  }

  @JsonProperty(PROP_LEVEL2)
  public @Nullable IsisLevelSettings getLevel2() {
    return _level2;
  }

  @JsonPropertyDescription("The net address is an ISO address representing the IS-IS router ID.")
  @JsonProperty(PROP_NET_ADDRESS)
  public @Nullable IsoAddress getNetAddress() {
    return _netAddress;
  }

  @JsonProperty(PROP_OVERLOAD_TIMEOUT)
  public @Nullable Integer getOverloadTimeout() {
    return _overloadTimeout;
  }

  @JsonProperty(PROP_REFERENCE_BANDWIDTH)
  public @Nullable Double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _generatedRoutes, _level1, _level2, _netAddress, _overloadTimeout, _referenceBandwidth);
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  public void setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  @JsonProperty(PROP_LEVEL1)
  public void setLevel1(@Nullable IsisLevelSettings level1) {
    _level1 = level1;
  }

  @JsonProperty(PROP_LEVEL2)
  public void setLevel2(@Nullable IsisLevelSettings level2) {
    _level2 = level2;
  }

  @JsonProperty(PROP_NET_ADDRESS)
  public void setNetAddress(@Nullable IsoAddress netAddress) {
    _netAddress = netAddress;
  }

  @JsonProperty(PROP_OVERLOAD_TIMEOUT)
  public void setOverloadTimeout(@Nullable Integer overloadTimeout) {
    _overloadTimeout = overloadTimeout;
  }

  @JsonProperty(PROP_REFERENCE_BANDWIDTH)
  public void setReferenceBandwidth(@Nullable Double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }
}
