package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** A set of constraints on a route announcement. */
public class RouteConstraints {

  private static final String PROP_PREFIX_SPACE = "prefixSpace";
  private static final String PROP_COMPLEMENT_PREFIX_SPACE = "complementPrefixSpace";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MULTI_EXIT_DISCRIMINATOR = "multiExitDiscriminator";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_COMPLEMENT_COMMUNITIES = "complementCommunities";

  PrefixSpace _prefixSpace;
  boolean _complementPrefixSpace;
  IntegerSpace _localPref;
  IntegerSpace _med;
  CommunitySet _communities;
  boolean _complementCommunities;

  @JsonCreator
  private RouteConstraints(
      @Nullable @JsonProperty(PROP_PREFIX_SPACE) PrefixSpace prefixSpace,
      @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE) boolean complementPrefixSpace,
      @Nullable @JsonProperty(PROP_LOCAL_PREFERENCE) IntegerSpace localPref,
      @Nullable @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR) IntegerSpace med,
      @Nullable @JsonProperty(PROP_COMMUNITIES) CommunitySet communities,
      @JsonProperty(PROP_COMPLEMENT_COMMUNITIES) boolean complementCommunities) {
    _prefixSpace = firstNonNull(prefixSpace, new PrefixSpace());
    _complementPrefixSpace = complementPrefixSpace;
    _localPref = firstNonNull(localPref, IntegerSpace.EMPTY);
    _med = firstNonNull(med, IntegerSpace.EMPTY);
    _communities = firstNonNull(communities, CommunitySet.empty());
    _complementCommunities = complementCommunities;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private PrefixSpace _prefixSpace;
    private boolean _complementPrefixSpace = false;
    private IntegerSpace _localPref;
    private IntegerSpace _med;
    private CommunitySet _communities;
    private boolean _complementCommunities = false;

    public Builder() {}

    public Builder setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
      return this;
    }

    public Builder setComplementPrefixSpace(boolean complementPrefixSpace) {
      _complementPrefixSpace = complementPrefixSpace;
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

    public Builder setCommunities(CommunitySet communities) {
      _communities = communities;
      return this;
    }

    public Builder setComplementCommunities(boolean complementCommunities) {
      _complementCommunities = complementCommunities;
      return this;
    }

    public RouteConstraints build() {
      return new RouteConstraints(
          _prefixSpace,
          _complementPrefixSpace,
          _localPref,
          _med,
          _communities,
          _complementCommunities);
    }
  }

  @JsonProperty(PROP_PREFIX_SPACE)
  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @JsonProperty(PROP_COMPLEMENT_PREFIX_SPACE)
  public boolean getComplementPrefixSpace() {
    return _complementPrefixSpace;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public IntegerSpace getLocalPref() {
    return _localPref;
  }

  @JsonProperty(PROP_MULTI_EXIT_DISCRIMINATOR)
  public IntegerSpace getMed() {
    return _med;
  }

  @JsonProperty(PROP_COMMUNITIES)
  public CommunitySet getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_COMPLEMENT_COMMUNITIES)
  public boolean getComplementCommunities() {
    return _complementCommunities;
  }
}
