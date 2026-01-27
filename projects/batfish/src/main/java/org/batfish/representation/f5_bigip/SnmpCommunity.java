package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an SNMP community configuration. */
@ParametersAreNonnullByDefault
public final class SnmpCommunity implements Serializable {

  public static final class Builder {
    private @Nullable String _name;
    private @Nullable String _communityName;
    private @Nullable String _source;

    private Builder() {}

    public @Nonnull Builder setName(@Nonnull String name) {
      _name = checkNotNull(name);
      return this;
    }

    public @Nonnull Builder setCommunityName(@Nonnull String communityName) {
      _communityName = checkNotNull(communityName);
      return this;
    }

    public @Nonnull Builder setSource(@Nullable String source) {
      _source = source;
      return this;
    }

    public @Nonnull SnmpCommunity build() {
      return new SnmpCommunity(
          checkNotNull(_name, "Name cannot be null"),
          checkNotNull(_communityName, "Community name cannot be null"),
          _source);
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull String _name;
  private final @Nonnull String _communityName;
  private final @Nullable String _source;

  private SnmpCommunity(
      @Nonnull String name, @Nonnull String communityName, @Nullable String source) {
    _name = name;
    _communityName = communityName;
    _source = source;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull String getCommunityName() {
    return _communityName;
  }

  public @Nullable String getSource() {
    return _source;
  }

  public @Nonnull Builder toBuilder() {
    return builder().setName(_name).setCommunityName(_communityName).setSource(_source);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SnmpCommunity)) {
      return false;
    }
    SnmpCommunity that = (SnmpCommunity) o;
    return _name.equals(that._name)
        && _communityName.equals(that._communityName)
        && java.util.Objects.equals(_source, that._source);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(_name, _communityName, _source);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean withDaml) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("name: \"%s\"", _name));
    if (_communityName != null) {
      sb.append(",\ncommunity-name: ").append(_communityName);
    }
    if (_source != null) {
      sb.append(",\nsource: ").append(_source);
    }
    sb.append("\n}");
    return sb.toString();
  }
}
