package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;

/** Return a particular {@link Action} and stop policy evaluation */
public final class Return implements Statement {
  private static final String PROP_ACTION = "action";

  private final Action _action;

  public Return(Action action) {
    _action = action;
  }

  @JsonCreator
  private static Return jsonCreator(@JsonProperty(PROP_ACTION) @Nullable Action action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    return new Return(action);
  }

  @JsonProperty(PROP_ACTION)
  public Action getAction() {
    return _action;
  }

  @Override
  public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitReturn(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Return aReturn = (Return) o;
    return Objects.equals(getAction(), aReturn.getAction());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getAction());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("action", _action).toString();
  }
}
