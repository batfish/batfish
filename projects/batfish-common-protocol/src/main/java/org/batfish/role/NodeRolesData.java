package org.batfish.role;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Class that captures the node roles */
public class NodeRolesData {

  private static final String PROP_DEFAULT_DIMENSION = "defaultDimension";
  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";

  @Nullable private String _defaultDimension;

  @Nonnull private SortedSet<NodeRoleDimension> _roleDimensions;

  @JsonCreator
  public NodeRolesData(
      @JsonProperty(PROP_DEFAULT_DIMENSION) String defaultDimension,
      @JsonProperty(PROP_ROLE_DIMENSIONS) SortedSet<NodeRoleDimension> roleDimensions) {
    _defaultDimension = defaultDimension;
    _roleDimensions = roleDimensions == null ? new TreeSet<>() : roleDimensions;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRolesData)) {
      return false;
    }
    return Objects.equals(_defaultDimension, ((NodeRolesData) o)._defaultDimension)
        && Objects.equals(_roleDimensions, ((NodeRolesData) o)._roleDimensions);
  }

  @JsonProperty(PROP_DEFAULT_DIMENSION)
  public String getDefaultDimension() {
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
  public Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) throws IOException {
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
  @Nullable
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
  public SortedSet<NodeRoleDimension> getNodeRoleDimensions() {
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
