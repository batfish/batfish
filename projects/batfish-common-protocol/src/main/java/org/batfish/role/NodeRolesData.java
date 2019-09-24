package org.batfish.role;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;

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
    @Nullable private String _defaultDimension;
    @Nullable private List<String> _roleDimensionOrder;
    @Nonnull private List<NodeRoleDimension> _roleDimensions;

    private Builder() {
      _roleDimensions = ImmutableList.of();
    }

    public @Nonnull NodeRolesData build() {
      return new NodeRolesData(_defaultDimension, _roleDimensions, _roleDimensionOrder);
    }

    public @Nonnull Builder addRoleDimensions(List<NodeRoleDimension> addedRoleDimensions) {
      _roleDimensions =
          ImmutableList.<NodeRoleDimension>builder()
              .addAll(_roleDimensions)
              .addAll(addedRoleDimensions)
              .build();
      return this;
    }

    public @Nonnull Builder setDefaultDimension(@Nullable String defaultDimension) {
      _defaultDimension = defaultDimension;
      return this;
    }

    public @Nonnull Builder setRoleDimensionOrder(@Nullable List<String> roleDimensionOrder) {
      _roleDimensionOrder = roleDimensionOrder == null ? null : copyOf(roleDimensionOrder);
      return this;
    }

    public @Nonnull Builder setRoleDimensions(List<NodeRoleDimension> roleDimensions) {
      _roleDimensions = copyOf(roleDimensions);
      return this;
    }
  }

  private static final String PROP_DEFAULT_DIMENSION = "defaultDimension";
  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";
  private static final String PROP_ROLE_DIMENSION_ORDER = "roleDimensionOrder";

  @Nullable private String _defaultDimension;

  @Nonnull private List<NodeRoleDimension> _roleDimensions;

  /* the list of role dimensions (or a subset of them), ordered for hierarchical
    visualization / exploration
  */
  @Nullable private List<String> _roleDimensionOrder;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private NodeRolesData(
      @Nullable String defaultDimension,
      List<NodeRoleDimension> roleDimensions,
      @Nullable List<String> roleDimensionOrder) {
    checkNotNull(roleDimensions);
    if (defaultDimension != null) {
      Names.checkName(defaultDimension, "role dimension", Type.REFERENCE_OBJECT);
    }
    _defaultDimension = defaultDimension;
    _roleDimensions = roleDimensions;
    _roleDimensionOrder = roleDimensionOrder;
  }

  @JsonCreator
  private static @Nonnull NodeRolesData create(
      @JsonProperty(PROP_DEFAULT_DIMENSION) @Nullable String defaultDimension,
      @JsonProperty(PROP_ROLE_DIMENSIONS) @Nullable List<NodeRoleDimension> roleDimensions,
      @JsonProperty(PROP_ROLE_DIMENSION_ORDER) @Nullable List<String> roleDimensionOrder) {
    return new NodeRolesData(
        defaultDimension,
        copyOf(firstNonNull(roleDimensions, ImmutableList.of())),
        roleDimensionOrder);
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
        && Objects.equals(_roleDimensions, ((NodeRolesData) o)._roleDimensions)
        && Objects.equals(_roleDimensionOrder, ((NodeRolesData) o)._roleDimensionOrder);
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

  @JsonProperty(PROP_ROLE_DIMENSION_ORDER)
  public Optional<List<String>> getRoleDimensionOrder() {
    return Optional.ofNullable(_roleDimensionOrder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_defaultDimension, _roleDimensions, _roleDimensionOrder);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DEFAULT_DIMENSION, _defaultDimension)
        .add(PROP_ROLE_DIMENSIONS, _roleDimensions)
        .add(PROP_ROLE_DIMENSION_ORDER, _roleDimensionOrder)
        .toString();
  }
}
