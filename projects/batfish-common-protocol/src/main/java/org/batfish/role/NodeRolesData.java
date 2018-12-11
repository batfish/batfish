package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Class that captures the node roles */
@ParametersAreNonnullByDefault
public class NodeRolesData {

  public static final class Builder {
    private String _defaultDimension;
    private SortedSet<NodeRoleDimension> _roleDimensions;

    private Builder() {
      _roleDimensions = ImmutableSortedSet.of();
    }

    public @Nonnull NodeRolesData build() {
      return new NodeRolesData(_defaultDimension, _roleDimensions);
    }

    public @Nonnull Builder setDefaultDimension(String defaultDimension) {
      _defaultDimension = defaultDimension;
      return this;
    }

    public @Nonnull Builder setRoleDimensions(SortedSet<NodeRoleDimension> roleDimensions) {
      _roleDimensions = ImmutableSortedSet.copyOf(roleDimensions);
      return this;
    }
  }

  private static final String PROP_DEFAULT_DIMENSION = "defaultDimension";
  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";

  private String _defaultDimension;

  private SortedSet<NodeRoleDimension> _roleDimensions;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private NodeRolesData(
      @Nullable String defaultDimension, SortedSet<NodeRoleDimension> roleDimensions) {
    checkNotNull(roleDimensions);
    _defaultDimension = defaultDimension;
    _roleDimensions = roleDimensions;
  }

  @JsonCreator
  private static @Nonnull NodeRolesData create(
      @JsonProperty(PROP_DEFAULT_DIMENSION) @Nullable String defaultDimension,
      @JsonProperty(PROP_ROLE_DIMENSIONS) @Nullable Set<NodeRoleDimension> roleDimensions) {
    return new NodeRolesData(
        defaultDimension,
        ImmutableSortedSet.copyOf(firstNonNull(roleDimensions, ImmutableSortedSet.of())));
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeRolesData)) {
      return false;
    }
    return Objects.equals(_defaultDimension, ((NodeRolesData) o)._defaultDimension)
        && Objects.equals(_roleDimensions, ((NodeRolesData) o)._roleDimensions);
  }

  @JsonProperty(PROP_DEFAULT_DIMENSION)
  public @Nullable String getDefaultDimension() {
    return _defaultDimension;
  }

  /**
   * Get the {@link NodeRoleDimension} object for the specified dimension. If dimension is null,
   * returns {@link #getNodeRoleDimension()}.
   *
   * @param dimension The name of the dimension to fetch
   * @return The {@link NodeRoleDimension} object if one exists or throws {@link
   *     java.util.NoSuchElementException} if {@code dimension} is non-null and not found.
   * @throws IOException If the contents of the file could not be cast to {@link NodeRolesData}
   */
  public @Nonnull Optional<NodeRoleDimension> getNodeRoleDimension(@Nullable String dimension)
      throws IOException {
    if (dimension == null) {
      return getNodeRoleDimension();
    }
    return _roleDimensions
        .stream()
        .filter(d -> d.getName().equalsIgnoreCase(dimension))
        .findFirst();
  }

  /**
   * Get some "reasonable" {@link NodeRoleDimension} object for analysis. Preference order: the
   * default dimension if set and exists, the auto-inferred primary dimension if it exists, the
   * dimension that is lexicographically first, and null if no dimensions exist.
   *
   * @throws IOException If the contents of the file could not be cast to {@link NodeRolesData}
   */
  @Nonnull
  private Optional<NodeRoleDimension> getNodeRoleDimension() throws IOException {
    // check default
    if (getDefaultDimension() != null) {
      Optional<NodeRoleDimension> opt = getNodeRoleDimension(getDefaultDimension());
      if (opt.isPresent()) {
        return opt;
      }
    }
    // check auto primary
    Optional<NodeRoleDimension> optAuto =
        getNodeRoleDimension(NodeRoleDimension.AUTO_DIMENSION_PRIMARY);
    if (optAuto.isPresent()) {
      return optAuto;
    }
    // check first
    return getNodeRoleDimensions().stream().min(Comparator.comparing(NodeRoleDimension::getName));
  }

  @JsonProperty(PROP_ROLE_DIMENSIONS)
  public @Nonnull SortedSet<NodeRoleDimension> getNodeRoleDimensions() {
    return _roleDimensions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_defaultDimension, _roleDimensions);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DEFAULT_DIMENSION, _defaultDimension)
        .add(PROP_ROLE_DIMENSIONS, _roleDimensions)
        .toString();
  }
}
