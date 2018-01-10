package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasDeclaredNames;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasSourceNats;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsActive;
import org.hamcrest.Matcher;

public final class InterfaceMatchers {

  /**
   * Provides a matcher that matches if the provided the interface's declared names comprise the set
   * of unique strings in {@code expectedDeclaredNames}.
   */
  public static HasDeclaredNames hasDeclaredNames(@Nonnull Iterable<String> expectedDeclaredNames) {
    return new HasDeclaredNames(
        containsInAnyOrder(ImmutableSet.copyOf(expectedDeclaredNames).toArray()));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * declared names.
   */
  public static HasDeclaredNames hasDeclaredNames(
      @Nonnull Matcher<? super Set<String>> subMatcher) {
    return new HasDeclaredNames(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided the interface's declared names comprise the set
   * of unique strings in {@code expectedDeclaredNames}.
   */
  public static HasDeclaredNames hasDeclaredNames(@Nonnull String... expectedDeclaredNames) {
    return new HasDeclaredNames(
        containsInAnyOrder(ImmutableSet.copyOf(expectedDeclaredNames).toArray()));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * source NATs.
   */
  public static HasSourceNats hasSourceNats(@Nonnull Matcher<? super List<SourceNat>> subMatcher) {
    return new HasSourceNats(subMatcher);
  }

  /** Provides a matcher that matches if the interface is active. */
  public static IsActive isActive() {
    return new IsActive(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * active flag.
   */
  public static IsActive isActive(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsActive(subMatcher);
  }

  private InterfaceMatchers() {}
}
