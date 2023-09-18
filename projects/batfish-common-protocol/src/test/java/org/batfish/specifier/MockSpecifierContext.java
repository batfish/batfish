package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

public class MockSpecifierContext implements SpecifierContext {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nonnull SortedSet<ReferenceBook> _referenceBooks = ImmutableSortedSet.of();

    private @Nonnull Map<String, Configuration> _configs = ImmutableMap.of();

    private @Nonnull SortedSet<NodeRoleDimension> _nodeRoleDimensions = ImmutableSortedSet.of();

    private @Nonnull Map<Location, LocationInfo> _locationInfo = ImmutableMap.of();

    private Builder() {}

    public Builder setConfigs(Map<String, Configuration> configs) {
      _configs = ImmutableMap.copyOf(configs);
      return this;
    }

    public Builder setNodeRoleDimensions(Set<NodeRoleDimension> nodeRoleDimensions) {
      _nodeRoleDimensions = ImmutableSortedSet.copyOf(nodeRoleDimensions);
      return this;
    }

    public Builder setReferenceBooks(SortedSet<ReferenceBook> referenceBooks) {
      _referenceBooks = ImmutableSortedSet.copyOf(referenceBooks);
      return this;
    }

    public Builder setLocationInfo(Map<Location, LocationInfo> locationInfo) {
      _locationInfo = ImmutableMap.copyOf(locationInfo);
      return this;
    }

    public MockSpecifierContext build() {
      return new MockSpecifierContext(this);
    }
  }

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull SortedSet<NodeRoleDimension> _nodeRoleDimensions;

  private final @Nonnull SortedSet<ReferenceBook> _referenceBooks;

  private final @Nonnull Map<Location, LocationInfo> _locationInfo;

  private MockSpecifierContext(Builder builder) {
    _referenceBooks = builder._referenceBooks;
    _configs = builder._configs;
    _nodeRoleDimensions = builder._nodeRoleDimensions;
    _locationInfo = builder._locationInfo;
  }

  @Override
  public @Nonnull Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Override
  public @Nonnull Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    return _nodeRoleDimensions.stream().filter(dim -> dim.getName().equals(dimension)).findAny();
  }

  @Override
  public LocationInfo getLocationInfo(Location location) {
    return _locationInfo.getOrDefault(location, LocationInfo.NOTHING);
  }

  @Override
  public Map<Location, LocationInfo> getLocationInfo() {
    return _locationInfo;
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _referenceBooks.stream().filter(book -> book.getName().equals(bookName)).findAny();
  }
}
