package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.CommunityActionOverlap.overlappingCommunities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests for {@link CommunityActionOverlap}. */
public class CommunityActionOverlapTest {

  private static NamedCommunity literalCommunity(String name, StandardCommunity... communities) {
    NamedCommunity nc = new NamedCommunity(name);
    for (StandardCommunity c : communities) {
      nc.getMembers().add(new LiteralCommunityMember(c));
    }
    return nc;
  }

  private static NamedCommunity regexCommunity(String name, String... regexes) {
    NamedCommunity nc = new NamedCommunity(name);
    for (String r : regexes) {
      nc.getMembers().add(new RegexCommunityMember(r));
    }
    return nc;
  }

  @Test
  public void testLiteralVsLiteralExactMatch() {
    NamedCommunity adder = literalCommunity("ADD", StandardCommunity.of(65000, 1));
    NamedCommunity deleter = literalCommunity("DEL", StandardCommunity.of(65000, 1));
    assertThat(overlappingCommunities(adder, deleter), contains("65000:1"));
  }

  @Test
  public void testLiteralVsLiteralNoMatch() {
    NamedCommunity adder = literalCommunity("ADD", StandardCommunity.of(65000, 1));
    NamedCommunity deleter = literalCommunity("DEL", StandardCommunity.of(65000, 2));
    assertThat(overlappingCommunities(adder, deleter), empty());
  }

  @Test
  public void testLiteralAddVsRegexDeleteCatchAll() {
    NamedCommunity adder = literalCommunity("ADD", StandardCommunity.of(65000, 1));
    NamedCommunity deleter = regexCommunity("DEL", ".*:.*");
    assertThat(overlappingCommunities(adder, deleter), contains("65000:1"));
  }

  @Test
  public void testLiteralAddVsRegexDeleteSpecific() {
    NamedCommunity adder = literalCommunity("ADD", StandardCommunity.of(65000, 1));
    NamedCommunity deleter = regexCommunity("DEL", "^65000:");
    assertThat(overlappingCommunities(adder, deleter), contains("65000:1"));
  }

  @Test
  public void testLiteralAddVsRegexDeleteNoMatch() {
    NamedCommunity adder = literalCommunity("ADD", StandardCommunity.of(65000, 1));
    NamedCommunity deleter = regexCommunity("DEL", "^65001:");
    assertThat(overlappingCommunities(adder, deleter), empty());
  }

  @Test
  public void testMultipleLiteralMembersPartialOverlap() {
    NamedCommunity adder =
        literalCommunity(
            "ADD",
            StandardCommunity.of(65000, 1),
            StandardCommunity.of(65000, 2),
            StandardCommunity.of(65001, 1));
    NamedCommunity deleter = regexCommunity("DEL", "^65000:");
    assertThat(overlappingCommunities(adder, deleter), containsInAnyOrder("65000:1", "65000:2"));
  }

  @Test
  public void testRegexAdderIsIgnored() {
    // Junos does not contribute regex members on community add/set, so no overlap is reported
    // even if the regexes obviously overlap.
    NamedCommunity adder = regexCommunity("ADD", "^65000:1$");
    NamedCommunity deleter = regexCommunity("DEL", ".*:.*");
    assertThat(overlappingCommunities(adder, deleter), empty());
  }

  @Test
  public void testMixedAdder() {
    // Literal members of a mixed adder are checked against the deleter; regex members are
    // ignored.
    NamedCommunity adder = new NamedCommunity("ADD");
    adder.getMembers().add(new LiteralCommunityMember(StandardCommunity.of(65000, 1)));
    adder.getMembers().add(new RegexCommunityMember("^65001:"));
    NamedCommunity deleter = regexCommunity("DEL", ".*:.*");
    assertThat(overlappingCommunities(adder, deleter), contains("65000:1"));
  }

  @Test
  public void testEmptyDeleterMembers() {
    NamedCommunity adder = literalCommunity("ADD", StandardCommunity.of(65000, 1));
    NamedCommunity deleter = new NamedCommunity("DEL");
    assertThat(overlappingCommunities(adder, deleter), empty());
  }
}
