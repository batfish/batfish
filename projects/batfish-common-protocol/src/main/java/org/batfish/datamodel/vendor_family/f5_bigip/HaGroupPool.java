package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a {@link Pool} within a {@link HaGroup}. */
public final class HaGroupPool implements Serializable {

  public static final class Builder {

    public @Nonnull HaGroupPool build() {
      checkArgument(_name != null, "Missing %s", PROP_NAME);
      return new HaGroupPool(_name, _weight);
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setWeight(@Nullable Integer weight) {
      _weight = weight;
      return this;
    }

    private @Nullable String _name;
    private @Nullable Integer _weight;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_WEIGHT)
  public @Nullable Integer getWeight() {
    return _weight;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HaGroupPool)) {
      return false;
    }
    HaGroupPool rhs = (HaGroupPool) obj;
    return _name.equals(rhs._name) && Objects.equals(_weight, rhs._weight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _weight);
  }

  private static final String PROP_NAME = "name";
  private static final String PROP_WEIGHT = "weight";

  @JsonCreator
  private static @Nonnull HaGroupPool create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_WEIGHT) @Nullable Integer weight) {
    Builder builder = builder();
    ofNullable(name).ifPresent(builder::setName);
    return builder.setWeight(weight).build();
  }

  private final @Nonnull String _name;
  private final @Nullable Integer _weight;

  private HaGroupPool(String name, @Nullable Integer weight) {
    _name = name;
    _weight = weight;
  }
}
