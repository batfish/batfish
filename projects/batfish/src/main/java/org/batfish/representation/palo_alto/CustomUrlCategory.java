package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents configuration of a PaloAlto custom-url-category. */
public final class CustomUrlCategory implements Serializable {
  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nonnull List<String> getList() {
    return _list;
  }

  public void addToList(String item) {
    _list =
        ImmutableList.<String>builderWithExpectedSize(_list.size() + 1)
            .addAll(_list)
            .add(item)
            .build();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public CustomUrlCategory(String name) {
    _list = ImmutableList.of();
    _name = name;
  }

  private @Nullable String _description;
  private @Nonnull List<String> _list;
  private @Nonnull final String _name;
}
