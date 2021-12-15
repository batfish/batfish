package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the settings of a VLAN: https://github.com/Azure/SONiC/wiki/Configuration#vlan_member
 */
public class VlanMember implements Serializable {
  private static final String PROP_TAGGING_MODE = "tagging_mode";

  private @Nullable final String _taggingMode;

  public @Nullable String getTaggingMode() {
    return _taggingMode;
  }

  @JsonCreator
  private @Nonnull static VlanMember create(
      @Nullable @JsonProperty(PROP_TAGGING_MODE) String taggingMode) {
    return VlanMember.builder().setTaggingMode(taggingMode).build();
  }

  private VlanMember(@Nullable String taggingMode) {
    _taggingMode = taggingMode;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VlanMember)) {
      return false;
    }
    VlanMember that = (VlanMember) o;
    return Objects.equals(_taggingMode, that._taggingMode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_taggingMode);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("tagging_mode", _taggingMode)
        .toString();
  }

  public @Nonnull static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String _taggingMode;

    public @Nonnull Builder setTaggingMode(@Nullable String taggingMode) {
      this._taggingMode = taggingMode;
      return this;
    }

    public @Nonnull VlanMember build() {
      return new VlanMember(_taggingMode);
    }
  }
}
