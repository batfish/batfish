package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PREFIX;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_SPACE_SET_OP;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_WILDCARD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.Ip;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Parser} producing {@link IpSpaceAstNode}. */
public class ParserIpSpaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Set<ParboiledAutoCompleteSuggestion> autoCompleteHelper(
      String query, ReferenceLibrary referenceLibrary) {
    return new ParboiledAutoComplete(
            Grammar.IP_SPACE_SPECIFIER,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            null,
            NodeRolesData.builder().build(),
            referenceLibrary)
        .run();
  }

  /** This is a complex completion test that exercises a bunch of the grammar */
  @Test
  public void testIpCompletion() {
    // should auto complete to 1.1.1.10, '-' (range), ':' (wildcard), and ',' (list)
    String query = "1.1.1.1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setIps(ImmutableSet.of(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.10")))
            .setPrefixes(ImmutableSet.of("1.1.1.1/22"))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Grammar.IP_SPACE_SPECIFIER,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                completionMetadata,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("1.1.1.1", 0, IP_ADDRESS),
            new ParboiledAutoCompleteSuggestion("1.1.1.10", 0, IP_ADDRESS),
            new ParboiledAutoCompleteSuggestion("-", query.length(), IP_RANGE),
            new ParboiledAutoCompleteSuggestion(":", query.length(), IP_WILDCARD),
            new ParboiledAutoCompleteSuggestion("1.1.1.1/22", 0, IP_PREFIX),
            new ParboiledAutoCompleteSuggestion("\\", query.length(), IP_SPACE_SET_OP),
            new ParboiledAutoCompleteSuggestion("&", query.length(), IP_SPACE_SET_OP),
            new ParboiledAutoCompleteSuggestion(",", query.length(), IP_SPACE_SET_OP)));
  }

  @Test
  public void testIpSpaceAddressGroup() {
    IpSpaceAstNode expectedAst = new AddressGroupIpSpaceAstNode("a", "b");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "@addressgroup(a, b)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, " @addressgroup ( a , b ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "@ADDRESSGROUP(a , b)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "@addressGroup(a , b)"),
        equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpAddress() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1"),
        equalTo(new IpAstNode("1.1.1.1")));
  }

  @Test
  public void testIpSpaceIpAddressFail() {
    _thrown.expectMessage("1111 is an invalid octet");
    _thrown.expect(IllegalArgumentException.class);
    SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1111");
  }

  @Test
  public void testIpSpaceIpRange() {
    IpSpaceAstNode expectedAst = new IpRangeAstNode("1.1.1.1", "2.2.2.2");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1-2.2.2.2"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, " 1.1.1.1 - 2.2.2.2 "),
        equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpWildcard() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1:2.2.2.2"),
        equalTo(new IpWildcardAstNode("1.1.1.1:2.2.2.2")));
  }

  @Test
  public void testIpSpaceLocationNodeDeprecated() {
    IpSpaceAstNode expectedNode =
        new LocationIpSpaceAstNode(
            InterfaceLocationAstNode.createFromNode(new NameRegexNodeAstNode("node.*")));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "node.*"), equalTo(expectedNode));
  }

  @Test
  public void testIpSpacePrefix() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1/1"),
        equalTo(new PrefixAstNode("1.1.1.1/1")));
  }

  @Test
  public void testIpSpacePrefixFail() {
    _thrown.expectMessage("Invalid prefix length");
    _thrown.expect(IllegalArgumentException.class);
    SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1/33");
  }

  @Test
  public void testIpSpaceSeDifference() {
    IpSpaceAstNode expectedNode =
        new DifferenceIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1\\2.2.2.2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, " 1.1.1.1 \\ 2.2.2.2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceSetIntersection() {
    IpSpaceAstNode expectedNode =
        new IntersectionIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1&2.2.2.2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, " 1.1.1.1 & 2.2.2.2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceSetUnion() {
    IpSpaceAstNode expectedNode =
        new UnionIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1,2.2.2.2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, " 1.1.1.1 , 2.2.2.2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceSetPrecedence() {
    IpSpaceAstNode expectedNode =
        new UnionIpSpaceAstNode(
            new IpAstNode("1.1.1.1"),
            new IntersectionIpSpaceAstNode(new IpAstNode("2.2.2.2"), new IpAstNode("3.3.3.3")));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1,2.2.2.2&3.3.3.3"),
        equalTo(expectedNode));
  }

  /** Test complex terms in set operations */
  @Test
  public void testIpSpaceSetComplexTerms() {
    IpSpaceAstNode expectedNode2 =
        new UnionIpSpaceAstNode(
            new UnionIpSpaceAstNode(
                new IpAstNode("1.1.1.1"), new IpRangeAstNode("2.2.2.2", "2.2.2.3")),
            new IpAstNode("3.3.3.3"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_SPACE_SPECIFIER, "1.1.1.1,2.2.2.2-2.2.2.3,3.3.3.3"),
        equalTo(expectedNode2));
  }

  /**
   * Test that address group rule is written in a way that allows for context sensitive
   * autocompletion
   */
  @Test
  public void testContextSensitiveAddressGroup() {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("b1")
                    .setAddressGroups(ImmutableList.of(new AddressGroup(null, "g1")))
                    .build(),
                ReferenceBook.builder("b2")
                    .setAddressGroups(ImmutableList.of(new AddressGroup(null, "g2")))
                    .build()));

    String query = "@addressGroup(b1,";

    // only g1 should be suggested
    assertThat(
        autoCompleteHelper(query, library),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g1", query.length(), ADDRESS_GROUP_NAME)));
  }
}
