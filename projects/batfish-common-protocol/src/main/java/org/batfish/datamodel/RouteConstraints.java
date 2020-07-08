package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;

/** A set of constraints on a route announcement. */
public class RouteConstraints {

  private static final String PROP_PREFIX_SPACE = "prefixSpace";
  private static final String PROP_COMPLEMENT_PREFIX_SPACE = "complementPrefixSpace";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MULTI_EXIT_DISCRIMINATOR = "multiExitDiscriminator";

  PrefixSpace _prefixSpace;
  boolean _complementPrefixSpace;
  IntegerSpace _localPref;
  IntegerSpace _med;

  @JsonCreator
  private RouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX_SPACE) PrefixSpace prefixSpace,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) IntegerSpace localPref,
      @Nullable @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR) IntegerSpace med,
      @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE) boolean complementPrefixSpace) {
    _prefixSpace = firstNonNull(prefixSpace, new PrefixSpace());
    _localPref = firstNonNull(localPref, IntegerSpace.EMPTY);
    _med = firstNonNull(med, IntegerSpace.EMPTY);
    _complementPrefixSpace = complementPrefixSpace;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private PrefixSpace _prefixSpace;
    private IntegerSpace _localPref;
    private IntegerSpace _med;
    private boolean _complementPrefixSpace = false;

    public Builder() {}

    public Builder setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
      return this;
    }

    public Builder setLocalPref(IntegerSpace localPref) {
      _localPref = localPref;
      return this;
    }

    public Builder setMed(IntegerSpace med) {
      _med = med;
      return this;
    }

    public Builder setComplementPrefixSpace(boolean complementPrefixSpace) {
      _complementPrefixSpace = complementPrefixSpace;
      return this;
    }

    public RouteConstraints build() {
      return new RouteConstraints(_prefixSpace, _localPref, _med, _complementPrefixSpace);
    }
  }

  @JsonProperty(PROP_PREFIX_SPACE)
  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public IntegerSpace getLocalPref() {
    return _localPref;
  }

  @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR)
  public IntegerSpace getMed() {
    return _med;
  }

  @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE)
  public boolean getComplementPrefixSpace() {
    return _complementPrefixSpace;
  }
}
