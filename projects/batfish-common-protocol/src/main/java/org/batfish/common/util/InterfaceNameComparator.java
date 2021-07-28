package org.batfish.common.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link Comparator<String>} implementation for interface names, biased towards Cisco- and
 * Juniper-like names that are a mix of letters and numbers, where numbers designate different
 * physical modules in some way.
 */
@ParametersAreNonnullByDefault
public final class InterfaceNameComparator implements Comparator<String>, Serializable {
  private static final Pattern DIGITS = Pattern.compile("\\d+");
  private static final Comparator<Iterable<String>> COMPARATOR =
      Comparators.lexicographical(InterfaceNameComparator::compareStringVsNum);
  private static final InterfaceNameComparator INSTANCE = new InterfaceNameComparator();

  public static @Nonnull InterfaceNameComparator instance() {
    return INSTANCE;
  }

  static int compareStringVsNum(String left, String right) {
    Integer leftInt = Ints.tryParse(left);
    Integer rightInt = Ints.tryParse(right);
    if (leftInt == null && rightInt == null) {
      // Neither one is an integer; use string compare ignoring case.
      return left.compareToIgnoreCase(right);
    } else if (leftInt != null && rightInt != null) {
      return leftInt.compareTo(rightInt);
    } else if (leftInt != null) {
      // Left is a number, right is a string.
      return -1;
    } else {
      // Right is a number, left is a string.
      return 1;
    }
  }

  @VisibleForTesting
  static @Nonnull List<String> split(String input) {
    ImmutableList.Builder<String> strings = ImmutableList.builder();
    Matcher matcher = DIGITS.matcher(input);
    int next = 0;
    while (matcher.find()) {
      strings.add(input.substring(next, matcher.start()));
      strings.add(input.substring(matcher.start(), matcher.end()));
      next = matcher.end();
    }
    if (next < input.length()) {
      strings.add(input.substring(next));
    }
    return strings.build();
  }

  @Override
  public int compare(String o1, String o2) {
    // We want to impose a total order, but the split comparisons ignore case and/or padding.
    // Handle this by falling back to raw string-compare for equal comparisons.
    // It's not perfect, but this really shouldn't happen on real devices.
    int comp = COMPARATOR.compare(split(o1), split(o2));
    return comp != 0 ? comp : o1.compareTo(o2);
  }

  /** Singleton after deserialization. */
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }

  private InterfaceNameComparator() {} // prevent instantiation of utility class
}
