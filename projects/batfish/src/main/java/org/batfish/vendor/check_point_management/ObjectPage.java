package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Representation of a single page of objects, as returned by a single call to the CheckPoint REST
 * API.
 */
public final class ObjectPage implements Serializable {

  public @Nonnull List<TypedManagementObject> getObjects() {
    return _objects;
  }

  @JsonCreator
  private static @Nonnull ObjectPage create(
      @JsonProperty(PROP_OBJECTS) @Nullable List<TypedManagementObject> objects) {
    checkArgument(objects != null, "Missing %s", PROP_OBJECTS);
    return new ObjectPage(objects);
  }

  @VisibleForTesting
  public ObjectPage(List<TypedManagementObject> objects) {
    _objects = ImmutableList.copyOf(objects);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ObjectPage)) {
      return false;
    }
    ObjectPage that = (ObjectPage) o;
    return _objects.equals(that._objects);
  }

  @Override
  public String toString() {
    return _objects.toString();
  }

  @Override
  public int hashCode() {
    return _objects.hashCode();
  }

  private static final String PROP_OBJECTS = "objects";

  private final @Nonnull List<TypedManagementObject> _objects;
}
