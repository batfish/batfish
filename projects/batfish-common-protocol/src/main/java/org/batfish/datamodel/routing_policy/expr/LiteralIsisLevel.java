package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class LiteralIsisLevel extends IsisLevelExpr {
  private static final String PROP_LEVEL = "level";

  private @Nonnull IsisLevel _level;

  @JsonCreator
  private static LiteralIsisLevel jsonCreator(@JsonProperty(PROP_LEVEL) @Nullable IsisLevel level) {
    checkArgument(level != null, "%s must be provided", PROP_LEVEL);
    return new LiteralIsisLevel(level);
  }

  public LiteralIsisLevel(IsisLevel level) {
    _level = level;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralIsisLevel)) {
      return false;
    }
    LiteralIsisLevel other = (LiteralIsisLevel) obj;
    return _level == other._level;
  }

  @Override
  public IsisLevel evaluate(Environment env) {
    return _level;
  }

  @JsonProperty(PROP_LEVEL)
  public @Nonnull IsisLevel getLevel() {
    return _level;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _level.ordinal();
    return result;
  }

  public void setLevel(IsisLevel level) {
    _level = level;
  }
}
