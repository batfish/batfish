package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** Class for parsing a community value into a {@link CommunityMember} */
public class CommunityMemberParseResult {

  private final CommunityMember _member;
  private final @Nullable String _warning;
  private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");
  private static final Pattern STANDARD_PATTERN = Pattern.compile("^\\d*:\\d*$");
  private static final Pattern EXTENDED_PATTERN = Pattern.compile("^([^:]+):[^:]*:$");

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

  /** Converts a community value into a {@link CommunityMember} and stores any warnings */
  public static CommunityMemberParseResult parseCommunityMember(String text) {
    if (DIGITS_ONLY_PATTERN.matcher(text).matches()) {
      return new CommunityMemberParseResult(new RegexCommunityMember(text), null);
    }

    Optional<CommunityMemberParseResult> specialCase = tryParseJuniperIncompleteLiteral(text);
    if (specialCase.isPresent()) {
      return specialCase.get();
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

  /** Create community string from incomplete cases and try to parse */
  private static Optional<CommunityMemberParseResult> tryParseJuniperIncompleteLiteral(
      String text) {
    if (STANDARD_PATTERN.matcher(text).matches()) {
      // For a standard community, admin and value can be empty
      int colonIndex = text.indexOf(':');
      if (colonIndex == 0 || colonIndex == text.length() - 1) {
        String highStr = colonIndex > 0 ? text.substring(0, colonIndex) : "0";
        String lowStr = colonIndex < text.length() - 1 ? text.substring(colonIndex + 1) : "0";
        String normalizedText = highStr + ":" + lowStr;
        Optional<StandardCommunity> standardCommunity =
            StandardCommunity.tryParse(highStr + ":" + lowStr);
        if (standardCommunity.isPresent()) {
          return createLiteralCommunityWithWarning(standardCommunity.get(), text, normalizedText);
        }
      }
    } else if (text.toLowerCase().startsWith("large")) {
      // For a large community, global admin, local data 1, and local data 2 can be empty
      String[] parts = text.split(":", -1);
      boolean hasEmptyParts = false;
      for (int i = 1; i < parts.length; i++) {
        if (parts[i].isEmpty()) {
          hasEmptyParts = true;
          break;
        }
      }
      if (hasEmptyParts) {
        String[] normalizedParts = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
          normalizedParts[i] = parts[i].isEmpty() ? "0" : parts[i];
        }
        String normalizedText = String.join(":", normalizedParts);
        Optional<LargeCommunity> largeCommunity = LargeCommunity.tryParse(normalizedText);
        if (largeCommunity.isPresent()) {
          return createLiteralCommunityWithWarning(largeCommunity.get(), text, normalizedText);
        }
      }
    } else if (EXTENDED_PATTERN.matcher(text).matches()) {
      // For an extended community, only local admin is allowed to be empty
      String normalizedText = text + "0";
      Optional<ExtendedCommunity> extendedCommunity = ExtendedCommunity.tryParse(normalizedText);
      if (extendedCommunity.isPresent()) {
        return createLiteralCommunityWithWarning(extendedCommunity.get(), text, normalizedText);
      }
    }
    return Optional.empty();
  }

  private static Optional<CommunityMemberParseResult> createLiteralCommunityWithWarning(
      Community community, String originalText, String normalizedText) {
    return Optional.of(
        new CommunityMemberParseResult(
            new LiteralCommunityMember(community),
            String.format(
                "RISK: Community string '%s' is interpreted as '%s'",
                originalText, normalizedText)));
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
    return _member.equals(cmpr._member) && Objects.equals(_warning, cmpr._warning);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_member, _warning);
  }
}
