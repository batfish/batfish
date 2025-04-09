package org.batfish.representation.juniper;

import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

public class CommunityMemberParseResult {

  private final CommunityMember _member;
  private final @Nullable String _warning;
  private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");
  private static final Pattern STANDARD_PATTERN = Pattern.compile("^\\d*:\\d*$");

  public CommunityMemberParseResult(CommunityMember member, @Nullable String warning) {
    _member = member;
    _warning = warning;
  }

  public CommunityMember getMember() {
    return _member;
  }

  public @Nullable String getWarning() {
    return _warning;
  }

  public static CommunityMemberParseResult parseCommunityMember(String text) {
    Optional<CommunityMemberParseResult> specialCase = handleSpecialStandardCommunity(text);
    if (specialCase.isPresent()) {
      return specialCase.get();
    }

    if (DIGITS_ONLY_PATTERN.matcher(text).matches()) {
      return new CommunityMemberParseResult(new RegexCommunityMember(text), null);
    }

    Community literalCommunity = tryParseLiteralCommunity(text);
    if (literalCommunity != null) {
      return new CommunityMemberParseResult(new LiteralCommunityMember(literalCommunity), null);
    }
    List<String> unintendedMatches = RegexCommunityMember.getUnintendedCommunityMatches(text);
    String warning = null;
    if (!unintendedMatches.isEmpty()) {
      warning =
          "RISK: Community regex "
              + text
              + " allows longer matches such as "
              + String.join(" and ", unintendedMatches);
    }
    return new CommunityMemberParseResult(new RegexCommunityMember(text), warning);
  }

  private static Optional<CommunityMemberParseResult> handleSpecialStandardCommunity(String text) {
    if (!STANDARD_PATTERN.matcher(text).matches()) {
      return Optional.empty();
    }

    if (text.equals(":")) {
      return Optional.of(
          new CommunityMemberParseResult(
              new LiteralCommunityMember(StandardCommunity.of(0, 0)),
              String.format("RISK: Community string '%s' is interpreted as '0:0'", text)));
    }

    if (text.endsWith(":")) {
      String asStr = text.substring(0, text.length() - 1);
      Integer asNum = Ints.tryParse(asStr);
      if (asNum != null) {
        return Optional.of(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(StandardCommunity.of(asNum, 0)),
                String.format(
                    "RISK: Community string '%s' is interpreted as '%s:0'", text, asStr)));
      }
    }

    if (text.startsWith(":")) {
      String valueStr = text.substring(1);
      Integer value = Ints.tryParse(valueStr);
      if (value != null) {
        return Optional.of(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(StandardCommunity.of(0, value)),
                String.format(
                    "RISK: Community string '%s' is interpreted as '0:%s'", text, value)));
      }
    }

    return Optional.empty();
  }

  /** Returns a {@link Community} if {@code text} can be parsed as one, or else {@code null}. */
  private static @Nullable Community tryParseLiteralCommunity(String text) {
    Optional<StandardCommunity> standard = StandardCommunity.tryParse(text);
    if (standard.isPresent()) {
      return standard.get();
    }
    // TODO: decouple extended community parsing for vendors
    Optional<ExtendedCommunity> extended = ExtendedCommunity.tryParse(text);
    if (extended.isPresent()) {
      return extended.get();
    }
    Optional<LargeCommunity> large = LargeCommunity.tryParse(text);
    if (large.isPresent()) {
      return large.get();
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CommunityMemberParseResult)) {
      return false;
    }
    CommunityMemberParseResult cmpr = (CommunityMemberParseResult) o;
    return Objects.equals(_member, cmpr._member) && Objects.equals(_warning, cmpr._warning);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_member, _warning);
  }
}
