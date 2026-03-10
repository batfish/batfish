package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;

/**
 * Collects an assignment of {@link IpSpace}s to {@link Location}s. Each {@link Location} must be
 * unique.
 */
public final class IpSpaceAssignment {
  public static class Entry {
    private final @Nonnull IpSpace _ipSpace;

    private final @Nonnull Set<Location> _locations;

    Entry(@Nonnull IpSpace ipSpace, @Nonnull Set<Location> locations) {
      _ipSpace = ipSpace;
      _locations = ImmutableSet.copyOf(locations);
    }

    public IpSpace getIpSpace() {
      return _ipSpace;
    }

    public Set<Location> getLocations() {
      return _locations;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Entry)) {
        return false;
      }
      return Objects.equals(_ipSpace, ((Entry) o)._ipSpace)
          && Objects.equals(_locations, ((Entry) o)._locations);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_ipSpace, _locations);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(getClass())
          .add("locations", _locations)
          .add("ipSpace", _ipSpace)
          .toString();
    }
  }

  public static class Builder {
    private ImmutableList.Builder<Entry> _entries = ImmutableList.builder();
    private Set<Location> _allLocations = new HashSet<>();

    public Builder assign(Location location, IpSpace ipSpace) {
      checkArgument(_allLocations.add(location), "duplicate location: %s", location);
      _entries.add(new Entry(ipSpace, ImmutableSet.of(location)));
      return this;
    }

    public Builder assign(Set<Location> locations, IpSpace ipSpace) {
      int oldSize = _allLocations.size();
      _allLocations.addAll(locations);
      checkArgument(_allLocations.size() == oldSize + locations.size(), "duplicate location(s)");

      _entries.add(new Entry(ipSpace, locations));
      return this;
    }

    public IpSpaceAssignment build() {
      return new IpSpaceAssignment(_entries.build());
    }
  }

  private final List<Entry> _entries;

  private IpSpaceAssignment(List<Entry> entries) {
    _entries = ImmutableList.copyOf(entries);
  }

  public static IpSpaceAssignment empty() {
    return new IpSpaceAssignment(ImmutableList.of());
  }

  public static Builder builder() {
    return new Builder();
  }

  public Collection<Entry> getEntries() {
    return _entries;
  }

  public static IpSpaceAssignment of(List<Entry> entries) {
    return new IpSpaceAssignment(entries);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpSpaceAssignment)) {
      return false;
    }
    return Objects.equals(_entries, ((IpSpaceAssignment) o)._entries);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_entries);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("entries", _entries).toString();
  }
}
