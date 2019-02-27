package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.stream.Collectors;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link LocationAstNode}. */
public class ParserLocationTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.INSTANCE.input(Parser.INSTANCE.LocationExpression()));
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

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1")).build();

    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Parser.INSTANCE,
            Parser.INSTANCE.input(Parser.INSTANCE.LocationExpression()),
            Parser.ANCHORS,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            null,
            null);

    ImmutableSet<AutocompleteSuggestion> expectedSet =
        ImmutableSet.<AutocompleteSuggestion>builder()
            .add(
                new AutocompleteSuggestion(
                    "node1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()))
            .addAll(
                ImmutableList.of(
                        "(",
                        "/",
                        "\"",
                        "@connectedTo",
                        "connectedTo",
                        "@deviceType",
                        "@enter",
                        "enter",
                        "@interfaceGroup",
                        "ref.interfaceGroup",
                        "@interfaceType",
                        "type",
                        "@role",
                        "ref.nodeRole",
                        "@vrf",
                        "vrf",
                        "@zone",
                        "zone")
                    .stream()
                    .map(
                        s ->
                            new AutocompleteSuggestion(
                                s, true, null, RANK_STRING_LITERAL, query.length()))
                    .collect(Collectors.toSet()))
            .build();

    assertThat(ImmutableSet.copyOf(pac.run()), equalTo(expectedSet));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "node1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node11")).build();

    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Parser.INSTANCE,
            Parser.INSTANCE.input(Parser.INSTANCE.LocationExpression()),
            Parser.ANCHORS,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            null,
            null);

    assertThat(
        ImmutableSet.copyOf(pac.run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion(
                    "1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion("\\", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("&", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("[", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  @Test
  public void testParseLocationBrackets() {
    // make sure that bracket binds more tightly than union
    assertThat(
        ParserUtils.getAst(getRunner().run("n1 , n2[e2]")),
        equalTo(
            new UnionLocationAstNode(
                InterfaceLocationAstNode.createFromNode("n1"),
                InterfaceLocationAstNode.createFromNodeInterface(
                    new NameNodeAstNode("n2"), new NameInterfaceAstNode("e2")))));

    // this should parse well too
    assertThat(
        ParserUtils.getAst(getRunner().run("n1[e1] , n2[e2]")),
        equalTo(
            new UnionLocationAstNode(
                InterfaceLocationAstNode.createFromNodeInterface(
                    new NameNodeAstNode("n1"), new NameInterfaceAstNode("e1")),
                InterfaceLocationAstNode.createFromNodeInterface(
                    new NameNodeAstNode("n2"), new NameInterfaceAstNode("e2")))));

    // brackets after a complex node expression
    assertThat(
        ParserUtils.getAst(getRunner().run("(n1 , n2)[e1/0]")),
        equalTo(
            InterfaceLocationAstNode.createFromNodeInterface(
                new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
                new NameInterfaceAstNode("e1/0"))));

    // brackets after a complex node expression and with complex interface expression
    assertThat(
        ParserUtils.getAst(getRunner().run("(n1 , n2)[e1 , e2]")),
        equalTo(
            InterfaceLocationAstNode.createFromNodeInterface(
                new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
                new UnionInterfaceAstNode(
                    new NameInterfaceAstNode("e1"), new NameInterfaceAstNode("e2")))));
  }

  @Test
  public void testParseLocationDifference() {
    DifferenceLocationAstNode expectedNode =
        new DifferenceLocationAstNode(
            InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth1")),
            InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth2")));

    assertThat(
        ParserUtils.getAst(getRunner().run("@vrf(eth1)\\@vrf(eth2)")), equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @vrf(eth1) \\ @vrf(eth2) ")), equalTo(expectedNode));
  }

  @Test
  public void testParseLocationEnter() {
    EnterLocationAstNode expectedAst =
        new EnterLocationAstNode(
            InterfaceLocationAstNode.createFromNode(new NameNodeAstNode("node1")));

    assertThat(ParserUtils.getAst(getRunner().run("@enter(node1)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @enter ( node1 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@EnTER(node1)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("enter(node1)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" EnTer ( node1 ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseLocationNodeInterface() {
    String input = "node[@interfaceType(physical)]";
    InterfaceLocationAstNode expectedAst =
        InterfaceLocationAstNode.createFromNodeInterface(
            new NameNodeAstNode("node"),
            new TypeInterfaceAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(ParserUtils.getAst(getRunner().run(input)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + input + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseLocationInterfaceDeprecated() {
    String input = "[eth0/1]";
    InterfaceLocationAstNode expectedAst =
        InterfaceLocationAstNode.createFromInterface(new NameInterfaceAstNode("eth0/1"));

    assertThat(ParserUtils.getAst(getRunner().run(input)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + input + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseLocationInterfaceSpecifier() {
    String input = "@interfaceType(physical)";
    InterfaceLocationAstNode expectedAst =
        InterfaceLocationAstNode.createFromInterface(
            new TypeInterfaceAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(ParserUtils.getAst(getRunner().run(input)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + input + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseLocationIntersection() {
    IntersectionLocationAstNode expectedNode =
        new IntersectionLocationAstNode(
            InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth1")),
            InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth2")));

    assertThat(ParserUtils.getAst(getRunner().run("@vrf(eth1)&@vrf(eth2)")), equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @vrf(eth1) & @vrf(eth2) ")), equalTo(expectedNode));
  }

  @Test
  public void testParseLocationNode() {
    String input = "node";
    InterfaceLocationAstNode expectedAst = InterfaceLocationAstNode.createFromNode(input);

    assertThat(ParserUtils.getAst(getRunner().run(input)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + input + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseLocationParens() {
    assertThat(
        ParserUtils.getAst(getRunner().run("(node)")),
        equalTo(InterfaceLocationAstNode.createFromNode("node")));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( node ) ")),
        equalTo(InterfaceLocationAstNode.createFromNode("node")));
    assertThat(
        ParserUtils.getAst(getRunner().run("(node1&node2)")),
        equalTo(
            InterfaceLocationAstNode.createFromNode(
                new IntersectionNodeAstNode(
                    new NameNodeAstNode("node1"), new NameNodeAstNode("node2")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("(node1&node2[e1])")),
        equalTo(
            new IntersectionLocationAstNode(
                InterfaceLocationAstNode.createFromNode("node1"),
                InterfaceLocationAstNode.createFromNodeInterface(
                    new NameNodeAstNode("node2"), new NameInterfaceAstNode("e1")))));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseLocationSetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("@vrf(eth1)\\@vrf(eth2)&@vrf(eth3)")),
        equalTo(
            new DifferenceLocationAstNode(
                InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth1")),
                new IntersectionLocationAstNode(
                    InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth2")),
                    InterfaceLocationAstNode.createFromInterface(
                        new VrfInterfaceAstNode("eth3"))))));
    assertThat(
        ParserUtils.getAst(getRunner().run("@vrf(eth1)&@vrf(eth2),@vrf(eth3)")),
        equalTo(
            new UnionLocationAstNode(
                new IntersectionLocationAstNode(
                    InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth1")),
                    InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth2"))),
                InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth3")))));
  }

  @Test
  public void testParseLocationUnion() {
    UnionLocationAstNode expectedNode =
        new UnionLocationAstNode(
            InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth1")),
            InterfaceLocationAstNode.createFromInterface(new VrfInterfaceAstNode("eth2")));

    assertThat(ParserUtils.getAst(getRunner().run("@vrf(eth1),@vrf(eth2)")), equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @vrf(eth1) , @vrf(eth2) ")), equalTo(expectedNode));
  }
}
