package org.batfish.datamodel.vendor_family.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

/** Settings for bridged ports */
public final class Bridge implements Serializable {

  public static final class Builder {

    private @Nullable SortedSet<String> _ports;
    private @Nullable Integer _pvid;
    private @Nullable IntegerSpace _vids;

    private Builder() {}

    public @Nonnull Bridge build() {
      checkArgument(_ports != null, "Missing %s", PROP_PORTS);
      checkArgument(_pvid != null, "Missing %s", PROP_PVID);
      checkArgument(_vids != null, "Missing %s", PROP_VIDS);
      return new Bridge(_ports, _pvid, _vids);
    }

    public @Nonnull Builder setPorts(Iterable<String> ports) {
      _ports = ImmutableSortedSet.copyOf(ports);
      return this;
    }

    public @Nonnull Builder setPvid(int pvid) {
      _pvid = pvid;
      return this;
    }

    public @Nonnull Builder setVids(IntegerSpace vids) {
      _vids = vids;
      return this;
    }
  }

  private static final String PROP_PORTS = "ports";
  private static final String PROP_PVID = "pvid";
  private static final String PROP_VIDS = "vids";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull Bridge create(
      @JsonProperty(PROP_PORTS) @Nullable Set<String> ports,
      @JsonProperty(PROP_PVID) @Nullable Integer pvid,
      @JsonProperty(PROP_VIDS) @Nullable IntegerSpace vids) {
    checkArgument(pvid != null, "Missing %s", PROP_PVID);
    return new Bridge(
        ImmutableSortedSet.copyOf(firstNonNull(ports, ImmutableSortedSet.of())),
        pvid,
        firstNonNull(vids, IntegerSpace.EMPTY));
  }

  private final @Nonnull SortedSet<String> _ports;
  private final int _pvid;
  private final @Nonnull IntegerSpace _vids;

  private Bridge(SortedSet<String> ports, int pvid, IntegerSpace vids) {
    _ports = ports;
    _pvid = pvid;
    _vids = vids;
  }

  /** Bridged ports */
  @JsonProperty(PROP_PORTS)
  public @Nonnull Set<String> getPorts() {
    return _ports;
  }

  /** Default native VLAN for bridged ports */
  @JsonProperty(PROP_PVID)
  public int getPvid() {
    return _pvid;
  }

  /** Default allowed VLANs for bridged ports */
  @JsonProperty(PROP_VIDS)
  public @Nonnull IntegerSpace getVids() {
    return _vids;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Bridge)) {
      return false;
    }
    Bridge rhs = (Bridge) obj;
    return _ports.equals(rhs._ports) && _pvid == rhs._pvid && _vids.equals(rhs._vids);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ports, _pvid, _vids);
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(getClass())
        .add(PROP_PORTS, _ports)
        .add(PROP_PVID, _pvid)
        .add(PROP_VIDS, _vids)
        .toString();
  }
}
