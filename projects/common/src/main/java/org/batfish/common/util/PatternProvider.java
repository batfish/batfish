package org.batfish.common.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Cache-based {@link Pattern} provider. */
@ParametersAreNonnullByDefault
public final class PatternProvider {

  public static @Nonnull Pattern fromString(String regex) {
    return CACHE.get(regex);
  }

  private PatternProvider() {}

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  private static final LoadingCache<String, Pattern> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(Pattern::compile);
}
