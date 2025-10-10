package org.batfish.datamodel;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.FibActionVisitor;

/** A {@link FibAction} indicating that lookup should be delegated to another VRF. */
@ParametersAreNonnullByDefault
public class FibNextVrf implements FibAction {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes data, would be 8 MiB total ignoring overhead).
  private static final LoadingCache<String, FibNextVrf> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(FibNextVrf::new);

  private final @Nonnull String _nextVrf;

  private FibNextVrf(String nextVrf) {
    _nextVrf = nextVrf;
  }

  public static FibNextVrf of(String nextVrf) {
    return CACHE.get(nextVrf);
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibNextVrf(this);
  }

  public @Nonnull String getNextVrf() {
    return _nextVrf;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FibNextVrf)) {
      return false;
    }
    return _nextVrf.equals(((FibNextVrf) obj)._nextVrf);
  }

  @Override
  public int hashCode() {
    return _nextVrf.hashCode();
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(_nextVrf);
  }
}
