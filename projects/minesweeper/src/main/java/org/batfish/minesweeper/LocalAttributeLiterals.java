package org.batfish.minesweeper;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The literal values of the local (device-only) BGP route attributes {@code tag2}, {@code color}
 * and {@code color2} that route policies match or set (see {@link
 * org.batfish.datamodel.BgpRoute#getTag2()}, {@link org.batfish.datamodel.BgpRoute#getColor()} and
 * {@link org.batfish.datamodel.BgpRoute#getColor2()}). Symbolic analysis tracks exactly these
 * values for each attribute, plus "unset" (0) and "some other value".
 *
 * <p>Each list is sorted, has no duplicates, and excludes 0, which denotes an unset attribute.
 */
@ParametersAreNonnullByDefault
@AutoValue
public abstract class LocalAttributeLiterals {

  /**
   * Creates a {@link LocalAttributeLiterals} from the given values, which may be unsorted and
   * contain duplicates or 0.
   */
  public static @Nonnull LocalAttributeLiterals create(
      Collection<Long> tag2s, Collection<Long> colors, Collection<Long> color2s) {
    return new AutoValue_LocalAttributeLiterals(
        normalize(tag2s), normalize(colors), normalize(color2s));
  }

  /** No literal values for any of the attributes. */
  public static @Nonnull LocalAttributeLiterals empty() {
    return EMPTY;
  }

  /** The {@code tag2} literals. */
  public abstract @Nonnull List<Long> tag2s();

  /** The {@code color} literals. */
  public abstract @Nonnull List<Long> colors();

  /** The {@code color2} literals. */
  public abstract @Nonnull List<Long> color2s();

  private static List<Long> normalize(Collection<Long> values) {
    return values.stream()
        .filter(v -> v != 0L)
        .distinct()
        .sorted()
        .collect(ImmutableList.toImmutableList());
  }

  private static final LocalAttributeLiterals EMPTY =
      create(ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
}
