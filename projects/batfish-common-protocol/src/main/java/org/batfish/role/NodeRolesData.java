package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

/** Class that captures the node roles */
@ParametersAreNonnullByDefault
public class NodeRolesData {

  public static final class Builder {
    private String _defaultDimension;
    private List<NodeRoleDimension> _roleDimensions;

    private Builder() {
      _roleDimensions = ImmutableList.of();
    }

    public @Nonnull NodeRolesData build() {
      return new NodeRolesData(_defaultDimension, _roleDimensions);
    }

    public @Nonnull Builder setDefaultDimension(String defaultDimension) {
      _defaultDimension = defaultDimension;
      return this;
    }

    public @Nonnull Builder setRoleDimensions(List<NodeRoleDimension> roleDimensions) {
      _roleDimensions = ImmutableList.copyOf(roleDimensions);
      return this;
    }
  }

  private static final String PROP_DEFAULT_DIMENSION = "defaultDimension";
  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";

  @Nullable private String _defaultDimension;

  @Nonnull List<NodeRoleDimension> _roleDimensions;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private NodeRolesData(@Nullable String defaultDimension, List<NodeRoleDimension> roleDimensions) {
    checkNotNull(roleDimensions);
    if (defaultDimension != null) {
      Names.checkName(defaultDimension, "role dimension", Type.REFERENCE_OBJECT);
    }
    _defaultDimension = defaultDimension;
    _roleDimensions = roleDimensions;
  }

  @JsonCreator
  private static @Nonnull NodeRolesData create(
      @JsonProperty(PROP_DEFAULT_DIMENSION) @Nullable String defaultDimension,
      @JsonProperty(PROP_ROLE_DIMENSIONS) @Nullable List<NodeRoleDimension> roleDimensions) {
    return new NodeRolesData(
        defaultDimension, ImmutableList.copyOf(firstNonNull(roleDimensions, ImmutableList.of())));
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
   * @return An {@link Optional} with {@link NodeRoleDimension} object if one exists or empty.
   */
  public @Nonnull Optional<NodeRoleDimension> getNodeRoleDimension(@Nullable String dimension) {
    if (dimension == null) {
      return getNodeRoleDimension();
    }
    return _roleDimensions.stream()
        .filter(d -> d.getName().equalsIgnoreCase(dimension))
        .findFirst();
  }

  /**
   * Get some "reasonable" {@link NodeRoleDimension} object for analysis. Preference order: the
   * default dimension if set and exists, the auto-inferred primary dimension if it exists, the
   * dimension that is lexicographically first, and null if no dimensions exist.
   */
  @Nonnull
  private Optional<NodeRoleDimension> getNodeRoleDimension() {
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
  public @Nonnull List<NodeRoleDimension> getNodeRoleDimensions() {
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
