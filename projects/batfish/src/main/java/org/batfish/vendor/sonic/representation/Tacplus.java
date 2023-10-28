package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents the settings of TACPLUS object */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tacplus implements Serializable {
  private static final String PROP_SRC_INTF = "src_intf";

  private final @Nullable String _srcIntf;

  public @Nonnull Optional<String> getSrcIntf() {
    return Optional.ofNullable(_srcIntf);
  }

  @JsonCreator
  private static @Nonnull Tacplus create(@JsonProperty(PROP_SRC_INTF) @Nullable String srcIntf) {
    return Tacplus.builder().setSrcIntf(srcIntf).build();
  }

  private Tacplus(@Nullable String srcIntf) {
    _srcIntf = srcIntf;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Tacplus)) {
      return false;
    }
    Tacplus that = (Tacplus) o;
    return Objects.equals(_srcIntf, that._srcIntf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_srcIntf);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues().add("srcIntf", _srcIntf).toString();
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String _srcIntf;

    public @Nonnull Builder setSrcIntf(@Nullable String srcIntf) {
      this._srcIntf = srcIntf;
      return this;
    }

    public @Nonnull Tacplus build() {
      return new Tacplus(_srcIntf);
    }
  }
}
