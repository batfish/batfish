package org.batfish.datamodel.routing_policy.as_path;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link AsPathExpr} representing the input {@link org.batfish.datamodel.AsPath} of the {@link
 * AsPathContext}.
 */
public final class InputAsPath extends AsPathExpr {

  @JsonCreator
  public static @Nonnull InputAsPath instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(AsPathExprVisitor<T, U> visitor, U arg) {
    return visitor.visitInputAsPath(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof InputAsPath;
  }

  @Override
  public int hashCode() {
    return 0x9B78220B; // randomly generated
  }

  private static final InputAsPath INSTANCE = new InputAsPath();

  private InputAsPath() {}
}
