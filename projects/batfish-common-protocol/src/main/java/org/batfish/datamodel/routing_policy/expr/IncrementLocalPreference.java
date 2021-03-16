package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.BgpRoute.MAX_LOCAL_PREFERENCE;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.HasReadableLocalPreference;
import org.batfish.datamodel.routing_policy.Environment;

public final class IncrementLocalPreference extends LongExpr {

  private long _addend;

  @JsonCreator
  private IncrementLocalPreference() {}

  public IncrementLocalPreference(long addend) {
    checkArgument(
        addend >= 0, "To add a negative number (%s), use DecrementLocalPreference", addend);
    checkArgument(
        addend <= MAX_LOCAL_PREFERENCE, "Value (%s) is out of range for local preference", addend);
    _addend = addend;
  }

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitIncrementLocalPreference(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof IncrementLocalPreference)) {
      return false;
    }
    IncrementLocalPreference other = (IncrementLocalPreference) obj;
    return _addend == other._addend;
  }

  @Override
  public long evaluate(Environment environment) {
    long oldLocalPreference;
    if (environment.getReadFromIntermediateBgpAttributes()) {
      oldLocalPreference = environment.getIntermediateBgpAttributes().getLocalPreference();
    } else if (!(environment.getOriginalRoute() instanceof HasReadableLocalPreference)) {
      oldLocalPreference = 0L;
    } else {
      HasReadableLocalPreference oldRoute =
          (HasReadableLocalPreference) environment.getOriginalRoute();
      oldLocalPreference = oldRoute.getLocalPreference();
    }
    return Math.min(oldLocalPreference + _addend, MAX_LOCAL_PREFERENCE);
  }

  public long getAddend() {
    return _addend;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_addend);
  }

  public void setAddend(int addend) {
    _addend = addend;
  }
}
