package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.BgpRoute.MAX_LOCAL_PREFERENCE;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.HasReadableLocalPreference;
import org.batfish.datamodel.routing_policy.Environment;

public final class DecrementLocalPreference extends LongExpr {

  private long _subtrahend;

  @JsonCreator
  private DecrementLocalPreference() {}

  public DecrementLocalPreference(long subtrahend) {
    checkArgument(
        subtrahend >= 0,
        "To subtract a negative number (%s), use IncrementLocalPreference",
        subtrahend);
    checkArgument(
        subtrahend <= MAX_LOCAL_PREFERENCE,
        "Value (%s) is out of range for local preference",
        subtrahend);
    _subtrahend = subtrahend;
  }

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitDecrementLocalPreference(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof DecrementLocalPreference)) {
      return false;
    }
    return _subtrahend == ((DecrementLocalPreference) obj)._subtrahend;
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
    return Math.max(oldLocalPreference - _subtrahend, 0L);
  }

  public long getSubtrahend() {
    return _subtrahend;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_subtrahend);
  }

  public void setSubtrahend(long subtrahend) {
    _subtrahend = subtrahend;
  }
}
