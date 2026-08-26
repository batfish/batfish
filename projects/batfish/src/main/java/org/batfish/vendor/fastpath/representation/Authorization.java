package org.batfish.vendor.fastpath.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/** A single {@code aaa authorization {commands|exec}} method list. */
public final class Authorization implements Serializable {

  private final @Nonnull AuthorizationType _type;
  private final @Nonnull String _name;
  private final @Nonnull List<AaaMethod> _methods;

  public Authorization(AuthorizationType type, String name, List<AaaMethod> methods) {
    _type = type;
    _name = name;
    _methods = ImmutableList.copyOf(methods);
  }

  public @Nonnull AuthorizationType getType() {
    return _type;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<AaaMethod> getMethods() {
    return _methods;
  }
}
