package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

public class MockSpecifierContext implements SpecifierContext {

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull
  public Map<String, Map<String, IpSpace>> get_interfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  public static final class Builder {
    private @Nonnull SortedSet<ReferenceBook> _referenceBooks = ImmutableSortedSet.of();

    private @Nonnull Map<String, Configuration> _configs = ImmutableMap.of();

    private @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps = ImmutableMap.of();

    private @Nonnull SortedSet<NodeRoleDimension> _nodeRoleDimensions = ImmutableSortedSet.of();

    private @Nonnull IpSpace _snapshotOwnedIps;

    private @Nonnull Map<String, Map<String, IpSpace>> _vrfOwnedIps = ImmutableMap.of();

    private Builder() {}

    public Builder setConfigs(Map<String, Configuration> configs) {
      _configs = ImmutableMap.copyOf(configs);
      return this;
    }

    public Builder setInterfaceOwnedIps(Map<String, Map<String, IpSpace>> interfaceOwnedIps) {
      _interfaceOwnedIps = interfaceOwnedIps;
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

    public Builder setSnapshotOwnedIps(IpSpace snapshotOwnedIps) {
      _snapshotOwnedIps = snapshotOwnedIps;
      return this;
    }

    public Builder setVrfOwnedIps(Map<String, Map<String, IpSpace>> vrfOwnedIps) {
      _vrfOwnedIps = vrfOwnedIps;
      return this;
    }

    public MockSpecifierContext build() {
      if (_interfaceOwnedIps.isEmpty() & !_configs.isEmpty()) {
        _interfaceOwnedIps = new IpOwners(_configs).getInterfaceOwnedIpSpaces();
      }
      return new MockSpecifierContext(this);
    }
  }

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps;

  private final @Nonnull SortedSet<NodeRoleDimension> _nodeRoleDimensions;

  private final @Nonnull SortedSet<ReferenceBook> _referenceBooks;

  private final @Nonnull IpSpace _snapshotOwnedIps;

  private MockSpecifierContext(Builder builder) {
    _referenceBooks = builder._referenceBooks;
    _configs = builder._configs;
    _interfaceOwnedIps = builder._interfaceOwnedIps;
    _nodeRoleDimensions = builder._nodeRoleDimensions;
    _snapshotOwnedIps = builder._snapshotOwnedIps;
  }

  @Override
  @Nonnull
  public Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Override
  @Nonnull
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  @Override
  public IpSpace getSnapshotDeviceOwnedIps() {
    return _snapshotOwnedIps;
  }

  @Override
  @Nonnull
  public Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    return _nodeRoleDimensions.stream().filter(dim -> dim.getName().equals(dimension)).findAny();
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _referenceBooks.stream().filter(book -> book.getName().equals(bookName)).findAny();
  }
}
