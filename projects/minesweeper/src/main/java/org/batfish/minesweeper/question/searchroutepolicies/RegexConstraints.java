package org.batfish.minesweeper.question.searchroutepolicies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A class that represents a list of regular expression constraints for a {@link
 * BgpRouteConstraints} object. These are used for constraints on communities as well as on AS
 * paths. Each constraint is optionally negated -- see {@link RegexConstraint}. The semantics of a
 * list of such constraints is the OR of all positive constraints, minus the OR of all negative
 * constraints. If there are no positive constraints, then implicitly there is a positive constraint
 * of logical "true", meaning that all possible values are allowed. This semantics is analogous to
 * that of other constraint types, such as {@link org.batfish.datamodel.IntegerSpace}, as well as
 * Batfish specifiers like {@link org.batfish.specifier.EnumSetSpecifier}.
 */
@ParametersAreNonnullByDefault
public class RegexConstraints {

  @Nonnull private final Set<RegexConstraint> _regexConstraints;

  public RegexConstraints() {
    this(ImmutableList.of());
  }

  @JsonCreator
  public RegexConstraints(@Nullable List<RegexConstraint> regexConstraints) {
    _regexConstraints =
        regexConstraints == null ? ImmutableSet.of() : ImmutableSet.copyOf(regexConstraints);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      return false;
    }
    RegexConstraints other = (RegexConstraints) o;
    return _regexConstraints.equals(other._regexConstraints);
  }

  @JsonValue
  public Set<RegexConstraint> getRegexConstraints() {
    return _regexConstraints;
  }

  public List<RegexConstraint> getPositiveRegexConstraints() {
    return _regexConstraints.stream()
        .filter(c -> !c.getNegated())
        .collect(ImmutableList.toImmutableList());
  }

  public List<RegexConstraint> getNegativeRegexConstraints() {
    return _regexConstraints.stream()
        .filter(RegexConstraint::getNegated)
        .collect(ImmutableList.toImmutableList());
  }

  public List<String> getAllRegexes() {
    return _regexConstraints.stream()
        .map(RegexConstraint::getRegex)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public int hashCode() {
    return _regexConstraints.hashCode();
  }

  public boolean isEmpty() {
    return _regexConstraints.isEmpty();
  }
}
