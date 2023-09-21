package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents configuration of a PaloAlto custom-url-category. */
public final class CustomUrlCategory implements Serializable {
  public static final String TYPE_URL_LIST = "URL List";

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nonnull Set<String> getList() {
    return _list;
  }

  public void addToList(String item) {
    _list =
        ImmutableSet.<String>builderWithExpectedSize(_list.size() + 1)
            .addAll(_list)
            .add(item)
            .build();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public CustomUrlCategory(String name) {
    _list = ImmutableSet.of();
    _name = name;
  }

  private @Nullable String _description;
  private @Nonnull Set<String> _list;
  private final @Nonnull String _name;
}
