package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.ReceivedFromVisitor;

/** Information indicating that a {@link BgpRoute} was locally originated. */
@ParametersAreNonnullByDefault
public final class ReceivedFromSelf implements ReceivedFrom {

  @Override
  public <T> T accept(ReceivedFromVisitor<T> visitor) {
    return visitor.visitReceivedFromSelf();
  }

  @JsonCreator
  public static @Nonnull ReceivedFromSelf instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof ReceivedFromSelf;
  }

  @Override
  public int hashCode() {
    return 0x631AFA34; // randomly generated
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

  private ReceivedFromSelf() {}

  private static final ReceivedFromSelf INSTANCE = new ReceivedFromSelf();

  /** Deserialize to singleton instance. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}
