package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Represents every azure resource having name, id, and type attribute. */
public class Resource implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull String _id;
  private final @Nonnull String _type;

  // Batfish doesn't handle '/' in Configuration hostnames (because of serializing)
  private final String _cleanId;

  public Resource(@Nonnull String name, @Nonnull String id, @Nonnull String type) {
    checkArgument(name != null, "resource name must be provided");
    checkArgument(id != null, "resource id must be provided");
    checkArgument(type != null, "resource type must be provided");
    _name = name;
    _id = id;
    _type = type;
    _cleanId = convertId(id);
  }

  private static String convertId(String id) {
    return id.replace('/', '_').toLowerCase();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull String getId() {
    return _id;
  }

  public @Nonnull String getType() {
    return _type;
  }

  public @Nonnull String getCleanId() {
    return _cleanId;
  }
}
