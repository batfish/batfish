package org.batfish.datamodel;

/** A set of constraints on a route announcement. */
public class RouteConstraints {

  // TODO: Allow constraints on fields other than the prefix.

  PrefixRange _prefixRange;

  private RouteConstraints(PrefixRange prefixRange) {
    _prefixRange = prefixRange;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    PrefixRange _prefixRange = PrefixRange.ALL;

    public Builder() {}

    public Builder setPrefixRange(PrefixRange prefixRange) {
      _prefixRange = prefixRange;
      return this;
    }

    public RouteConstraints build() {
      return new RouteConstraints(_prefixRange);
    }
  }

  public PrefixRange getPrefixRange() {
    return _prefixRange;
  }
}
