package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.visitors.CommunitySetExprVisitor;
import org.batfish.datamodel.visitors.VoidCommunitySetExprVisitor;

/**
 * A {@link CommunitySetExpr} that matches communities via a regular expression applied to the
 * string representation of the community attribute of a route. community's 32-bit value. The string
 * consists of a sequence of communities in colon-split notation separated by spaces. The sequence
 * is sorted by raw integer value of a standard community.
 */
public final class CommunityAttributeRegexCommunitySet extends CommunitySetExpr {

  private final class PatternSupplier implements Supplier<Pattern>, Serializable {

    @Override
    public Pattern get() {
      return Pattern.compile(_regex);
    }
  }

  private static final String PROP_REGEX = "regex";

  @JsonCreator
  private static @Nonnull CommunityAttributeRegexCommunitySet create(
      @JsonProperty(PROP_REGEX) String regex) {
    return new CommunityAttributeRegexCommunitySet(requireNonNull(regex));
  }

  private final Supplier<Pattern> _pattern;

  private final String _regex;

  public CommunityAttributeRegexCommunitySet(@Nonnull String regex) {
    _regex = regex;
    _pattern = Suppliers.memoize(new PatternSupplier());
  }

  @Override
  public <T> T accept(CommunitySetExprVisitor<T> visitor) {
    return visitor.visitCommunityAttributeRegexCommunitySet(this);
  }

  @Override
  public void accept(VoidCommunitySetExprVisitor visitor) {
    visitor.visitCommunityAttributeRegexCommunitySet(this);
  }

  @Nonnull
  @Override
  public SortedSet<Community> asLiteralCommunities(@Nonnull Environment environment) {
    throw new UnsupportedOperationException(
        "Cannot be represented as a list of literal communities");
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityAttributeRegexCommunitySet)) {
      return false;
    }
    return _regex.equals(((CommunityAttributeRegexCommunitySet) obj)._regex);
  }

  @JsonProperty(PROP_REGEX)
  public @Nonnull String getRegex() {
    return _regex;
  }

  @Override
  public int hashCode() {
    return _regex.hashCode();
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Community> communitySetCandidate) {
    String rendered =
        communitySetCandidate.stream()
            .sorted()
            .map(Object::toString)
            .collect(Collectors.joining(" "));
    return _pattern.get().matcher(rendered).find();
  }

  @Override
  public boolean matchCommunity(Environment environment, Community community) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean reducible() {
    return true;
  }
}
