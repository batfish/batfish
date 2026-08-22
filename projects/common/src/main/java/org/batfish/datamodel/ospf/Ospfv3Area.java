package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An OSPFv3 area configuration. */
@ParametersAreNonnullByDefault
public final class Ospfv3Area implements Serializable {

  public static final class Builder {
    private @Nonnull ImmutableSortedSet.Builder<String> _interfaces;
    private @Nullable Long _number;

    private Builder() {
      _interfaces = ImmutableSortedSet.naturalOrder();
    }

    public Ospfv3Area build() {
      checkArgument(_number != null, "Must set area number before building");
      return new Ospfv3Area(_number, _interfaces.build());
    }

    public Builder setNumber(long number) {
      _number = number;
      return this;
    }

    public Builder addInterface(String interfaceName) {
      _interfaces.add(interfaceName);
      return this;
    }

    public Builder addInterfaces(Collection<String> interfaceNames) {
      _interfaces.addAll(interfaceNames);
      return this;
    }
  }

  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_NAME = "name";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static Ospfv3Area create(
      @JsonProperty(PROP_NAME) long number,
      @JsonProperty(PROP_INTERFACES) @Nullable SortedSet<String> interfaces) {
    return new Ospfv3Area(
        number, firstNonNull(interfaces, ImmutableSortedSet.of()));
  }

  private Ospfv3Area(long areaNumber, Collection<String> interfaces) {
    _areaNumber = areaNumber;
    _interfaces = ImmutableSortedSet.copyOf(interfaces);
  }

  @JsonProperty(PROP_NAME)
  public long getAreaNumber() {
    return _areaNumber;
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nonnull SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  private final long _areaNumber;
  private final @Nonnull SortedSet<String> _interfaces;
}
