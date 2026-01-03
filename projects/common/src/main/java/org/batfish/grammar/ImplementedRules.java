package org.batfish.grammar;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * A utility class that returns a list of parser rules implemented in a given extractor, where
 * "implemented" is defined as "implements the {@code enterRule} or {@code exitRule} function".
 */
public final class ImplementedRules {
  /**
   * Returns a set of the names of functions that look like parser rules (all lowercase) implemented
   * in the given class.
   */
  public static Set<String> getImplementedRules(Class<?> clazz) {
    checkArgument(
        ParseTreeListener.class.isAssignableFrom(clazz), "%s is not a ParseTreeListener", clazz);
    return Arrays.stream(clazz.getDeclaredMethods())
        .map(Method::getName)
        .map(
            s -> {
              if (s.startsWith("enter")) {
                return s.substring("enter".length()).toLowerCase();
              } else if (s.startsWith("exit")) {
                return s.substring("exit".length()).toLowerCase();
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(ImmutableSet.toImmutableSet());
  }

  private ImplementedRules() {} // prevent instantiation
}
