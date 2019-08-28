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
  private static final String PROP_ADMIN = "admin";
  private static final String PROP_TAG = "tag";

  @Nullable private final Integer _admin;

  @Nullable private final Long _tag;

  @Nullable
  @JsonProperty(PROP_ADMIN)
  public Integer getAdmin() {
    return _admin;
  }

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
    return Objects.equals(_admin, that._admin) && Objects.equals(_tag, that._tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ConnectedRouteMetadata.class)
        .omitNullValues()
        .add(PROP_ADMIN, _admin)
        .add(PROP_TAG, _tag)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_admin, _tag);
  }

  public static Builder builder() {
    return new Builder();
  }

  /////////////////////////
  // Private implementation
  /////////////////////////

  private ConnectedRouteMetadata(@Nullable Integer admin, @Nullable Long tag) {
    _admin = admin;
    _tag = tag;
  }

  public static final class Builder {
    @Nullable private Integer _admin;
    @Nullable private Long _tag;

    private Builder() {}

    @Nonnull
    public Builder setAdmin(@Nullable Integer admin) {
      _admin = admin;
      return this;
    }

    @Nonnull
    public Builder setAdmin(int admin) {
      _admin = admin;
      return this;
    }

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
      return new ConnectedRouteMetadata(_admin, _tag);
    }
  }

  @JsonCreator
  private static ConnectedRouteMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_ADMIN) Integer admin,
      @Nullable @JsonProperty(PROP_TAG) Long tag) {
    return new ConnectedRouteMetadata(admin, tag);
  }
}
