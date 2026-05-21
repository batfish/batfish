package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Detects overlap between {@link NamedCommunity} structures used in same-term {@code then community
 * add/set} and {@code then community delete} actions in a Junos {@code policy-statement}.
 *
 * <p>Junos applies {@code then} actions in declared order at runtime. When a {@code community
 * add}/{@code community set} contributes a community that a later {@code community delete} also
 * matches, the result is that the just-added community is silently removed. This is rarely
 * intended.
 *
 * <p>Junos only contributes {@link LiteralCommunityMember}s when setting/adding (regex members are
 * skipped at runtime). Junos {@code delete} matches literal and regex members alike, using
 * substring-style {@link Pattern#find()} semantics on the colon-separated community string. This
 * class implements that asymmetry.
 */
@ParametersAreNonnullByDefault
final class CommunityActionOverlap {

  /**
   * Returns the literal communities that would be set/added by {@code adder} and also matched (and
   * therefore removed) by {@code deleter}, formatted as colon-separated strings.
   *
   * <p>Empty result means there is no overlap (no warning is warranted).
   */
  static @Nonnull List<String> overlappingCommunities(
      NamedCommunity adder, NamedCommunity deleter) {
    ImmutableList.Builder<String> overlap = ImmutableList.builder();
    for (CommunityMember addMember : adder.getMembers()) {
      if (!(addMember instanceof LiteralCommunityMember literalAdd)) {
        // Junos only contributes literal communities on add/set. Regex members are ignored.
        continue;
      }
      Community c = literalAdd.getCommunity();
      if (deleterMatches(deleter, c)) {
        overlap.add(c.toString());
      }
    }
    return overlap.build();
  }

  /**
   * Returns true if {@code deleter} would match {@code c} according to Junos {@code delete}
   * semantics: literal members match by equality; regex members match via {@link Pattern#find()} on
   * the community's match string.
   */
  private static boolean deleterMatches(NamedCommunity deleter, Community c) {
    String matchString = c.matchString();
    for (CommunityMember m : deleter.getMembers()) {
      if (m instanceof LiteralCommunityMember literal) {
        if (literal.getCommunity().equals(c)) {
          return true;
        }
      } else if (m instanceof RegexCommunityMember regex) {
        try {
          if (Pattern.compile(regex.getJavaRegex()).matcher(matchString).find()) {
            return true;
          }
        } catch (java.util.regex.PatternSyntaxException ignored) {
          // Malformed regex — caller already warns elsewhere; treat as non-match here.
        }
      }
    }
    return false;
  }

  private CommunityActionOverlap() {}
}
