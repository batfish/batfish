package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Metadata associated with a particular {@link ConcreteInterfaceAddress} to modify creation of
 * {@link ConnectedRoute connected routes}
 */
public final class ConnectedRouteMetadata implements Serializable {
  private static final String PROP_TAG = "tag";

  @Nullable private final Long _tag;

  @Nullable
  @JsonProperty(PROP_TAG)
  public Long getTag() {
    return _tag;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConnectedRouteMetadata)) {
      return false;
    }
    ConnectedRouteMetadata that = (ConnectedRouteMetadata) o;
    return Objects.equals(_tag, that._tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ConnectedRouteMetadata.class).add(PROP_TAG, _tag).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_tag);
  }

  public static Builder builder() {
    return new Builder();
  }

  /////////////////////////
  // Private implementation
  /////////////////////////

  private ConnectedRouteMetadata(@Nullable Long tag) {
    _tag = tag;
  }

  public static final class Builder {
    @Nullable private Long _tag;

    private Builder() {}

    @Nonnull
    public Builder setTag(@Nullable Long tag) {
      _tag = tag;
      return this;
    }

    @Nonnull
    public Builder setTag(long tag) {
      _tag = tag;
      return this;
    }

    @Nonnull
    public ConnectedRouteMetadata build() {
      return new ConnectedRouteMetadata(_tag);
    }
  }

  @JsonCreator
  private static ConnectedRouteMetadata jsonCreator(@Nullable @JsonProperty(PROP_TAG) Long tag) {
    return new ConnectedRouteMetadata(tag);
  }
}
