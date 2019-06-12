package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link EnumSetAstNode}. */
public class ParserNamedStructureTest {

  private static final Collection<String> ALL_NAMED_STRUCTURE_TYPES =
      Parser.getEnumValues(Grammar.NAMED_STRUCTURE_SPECIFIER);

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        Parser.instance().getInputRule(Grammar.NAMED_STRUCTURE_SPECIFIER));
  }

  /** This testParses if we have proper completion annotations on the rules */
  @Test
  public void testAnchorAnnotations() {
    ParsingResult<?> result = getRunner().run("");

    // not barfing means all potential paths have completion annotation at least for empty input
    ParserUtils.getPotentialMatches(
        (InvalidInputError) result.parseErrors.get(0), Parser.ANCHORS, false);
  }

  @Test
  public void testCompletionEmpty() {
    String query = "";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.NAMED_STRUCTURE_SPECIFIER,
                Parser.ANCHORS,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        equalTo(
            Stream.concat(
                    NamedStructurePropertySpecifier.JAVA_MAP.keySet().stream()
                        .map(
                            val ->
                                new ParboiledAutoCompleteSuggestion(
                                    val, query.length(), Type.ENUM_SET_VALUE)),
                    ImmutableSet.of(
                        new ParboiledAutoCompleteSuggestion("/", 0, Type.ENUM_SET_REGEX))
                        .stream())
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "IKE";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.NAMED_STRUCTURE_SPECIFIER,
                Parser.ANCHORS,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(
                NamedStructurePropertySpecifier.IKE_PHASE1_KEYS, 0, Type.ENUM_SET_VALUE),
            new ParboiledAutoCompleteSuggestion(
                NamedStructurePropertySpecifier.IKE_PHASE1_POLICIES, 0, Type.ENUM_SET_VALUE),
            new ParboiledAutoCompleteSuggestion(
                NamedStructurePropertySpecifier.IKE_PHASE1_PROPOSALS, 0, Type.ENUM_SET_VALUE)));
  }

  @Test
  public void testParseNamedStructureType() {
    String query = NamedStructurePropertySpecifier.IP_ACCESS_LIST;
    ValueEnumSetAstNode expectedAst = new ValueEnumSetAstNode(query, ALL_NAMED_STRUCTURE_TYPES);

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNamedStructureBad() {
    String query = "faux";
    _thrown.expect(IllegalArgumentException.class);
    ParserUtils.getAst(getRunner().run(query));
  }

  @Test
  public void testParseNamedStructureTypeCaseInsensitive() {
    String query = NamedStructurePropertySpecifier.IP_ACCESS_LIST.toLowerCase();
    ValueEnumSetAstNode expectedAst = new ValueEnumSetAstNode(query, ALL_NAMED_STRUCTURE_TYPES);

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNamedStructureTypeRegex() {
    String query = "/IP/";
    RegexEnumSetAstNode expectedAst = new RegexEnumSetAstNode("IP");

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNamedStructureTypeRegexDeprecated() {
    String query = "/ip.*/";
    RegexEnumSetAstNode expectedAst = new RegexEnumSetAstNode("ip.*");

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterUnion() {
    String t1 = NamedStructurePropertySpecifier.IP_ACCESS_LIST;
    String t2Regex = "ip";
    UnionEnumSetAstNode expectedNode =
        new UnionEnumSetAstNode(
            new ValueEnumSetAstNode(t1, ALL_NAMED_STRUCTURE_TYPES),
            new RegexEnumSetAstNode(t2Regex));

    assertThat(
        ParserUtils.getAst(getRunner().run(String.format("%s,/%s/", t1, t2Regex))),
        equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(String.format(" %s , /%s/ ", t1, t2Regex))),
        equalTo(expectedNode));
  }
}
