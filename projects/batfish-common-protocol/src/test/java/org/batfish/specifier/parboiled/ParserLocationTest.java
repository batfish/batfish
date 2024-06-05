package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_CONNECTED_TO;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_TYPE;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_VRF;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_ZONE;
import static org.batfish.specifier.parboiled.Anchor.Type.LOCATION_ENTER;
import static org.batfish.specifier.parboiled.Anchor.Type.LOCATION_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.LOCATION_SET_OP;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_AND_DIMENSION;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_TYPE;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_INTERFACE_GROUP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.util.isp.IspModelingUtils;
import org.batfish.datamodel.InterfaceType;
import org.batfish.specifier.Grammar;
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
    return new ReportingParseRunner<>(Parser.instance().getInputRule(Grammar.LOCATION_SPECIFIER));
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

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.LOCATION_SPECIFIER,
                Parser.ANCHORS,
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
            new ParboiledAutoCompleteSuggestion("node1", query.length(), NODE_NAME),
            new ParboiledAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParboiledAutoCompleteSuggestion("(", query.length(), LOCATION_PARENS),
            new ParboiledAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion(
                "@connectedTo(", query.length(), INTERFACE_CONNECTED_TO),
            new ParboiledAutoCompleteSuggestion("@deviceType(", query.length(), NODE_TYPE),
            new ParboiledAutoCompleteSuggestion("@enter(", query.length(), LOCATION_ENTER),
            new ParboiledAutoCompleteSuggestion(
                "@interfaceGroup(", query.length(), REFERENCE_BOOK_AND_INTERFACE_GROUP),
            new ParboiledAutoCompleteSuggestion("@interfaceType(", query.length(), INTERFACE_TYPE),
            new ParboiledAutoCompleteSuggestion("@role(", query.length(), NODE_ROLE_AND_DIMENSION),
            new ParboiledAutoCompleteSuggestion("@vrf(", query.length(), INTERFACE_VRF),
            new ParboiledAutoCompleteSuggestion("@zone(", query.length(), INTERFACE_ZONE)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "node1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node11")).build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.LOCATION_SPECIFIER,
                Parser.ANCHORS,
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
            new ParboiledAutoCompleteSuggestion("node1", 0, NODE_NAME),
            new ParboiledAutoCompleteSuggestion("node11", 0, NODE_NAME),
            new ParboiledAutoCompleteSuggestion("\\", query.length(), LOCATION_SET_OP),
            new ParboiledAutoCompleteSuggestion(",", query.length(), LOCATION_SET_OP),
            new ParboiledAutoCompleteSuggestion("&", query.length(), LOCATION_SET_OP),
            new ParboiledAutoCompleteSuggestion("[", query.length(), NODE_AND_INTERFACE_TAIL)));
  }

  @Test
  public void testParseLocationBrackets() {
    // make sure that bracket binds more tightly than union
    assertThat(
        ParserUtils.getAst(getRunner().run("n1 , n2[e2]")),
        equalTo(
            new UnionLocationAstNode(
                InterfaceLocationAstNode.createFromNode("n1"),
                InterfaceLocationAstNode.createFromInterfaceWithNode(
                    new NameNodeAstNode("n2"), new NameInterfaceAstNode("e2")))));

    // this should parse well too
    assertThat(
        ParserUtils.getAst(getRunner().run("n1[e1] , n2[e2]")),
        equalTo(
            new UnionLocationAstNode(
                InterfaceLocationAstNode.createFromInterfaceWithNode(
                    new NameNodeAstNode("n1"), new NameInterfaceAstNode("e1")),
                InterfaceLocationAstNode.createFromInterfaceWithNode(
                    new NameNodeAstNode("n2"), new NameInterfaceAstNode("e2")))));

    // brackets after a complex node expression
    assertThat(
        ParserUtils.getAst(getRunner().run("(n1 , n2)[e1/0]")),
        equalTo(
            InterfaceLocationAstNode.createFromInterfaceWithNode(
                new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
                new NameInterfaceAstNode("e1/0"))));

    // brackets after a complex node expression and with complex interface expression
    assertThat(
        ParserUtils.getAst(getRunner().run("(n1 , n2)[e1 , e2]")),
        equalTo(
            InterfaceLocationAstNode.createFromInterfaceWithNode(
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
  }

  /** Test of the special internet query and then also interfaces on the internet node. */
  @Test
  public void testParseLocationInternet() {
    assertThat(
        ParserUtils.getAst(getRunner().run("internet")), equalTo(InternetLocationAstNode.INSTANCE));
    assertThat(
        ParserUtils.getAst(getRunner().run("internet[To-Isp123]")),
        equalTo(
            InterfaceLocationAstNode.createFromInterfaceWithNode(
                new InterfaceWithNodeInterfaceAstNode(
                    new NameNodeAstNode(IspModelingUtils.INTERNET_HOST_NAME),
                    new NameInterfaceAstNode("To-Isp123")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("@enter(internet[To-Isp123])")),
        equalTo(
            new EnterLocationAstNode(
                InterfaceLocationAstNode.createFromInterfaceWithNode(
                    new InterfaceWithNodeInterfaceAstNode(
                        new NameNodeAstNode(IspModelingUtils.INTERNET_HOST_NAME),
                        new NameInterfaceAstNode("To-Isp123"))))));
  }

  @Test
  public void testParseLocationNodeInterface() {
    String input = "node[@interfaceType(physical)]";
    InterfaceLocationAstNode expectedAst =
        InterfaceLocationAstNode.createFromInterfaceWithNode(
            new NameNodeAstNode("node"),
            new TypeInterfaceAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(ParserUtils.getAst(getRunner().run(input)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + input + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseLocationInterfaceDeprecatedOvereating() {
    // the whole expression should not be eaten by the deprecated regex parser
    String input = "tor-001a.sea3[/.*/]";
    LocationAstNode expectedAst =
        InterfaceLocationAstNode.createFromInterfaceWithNode(
            new NameNodeAstNode("tor-001a.sea3"), new NameRegexInterfaceAstNode(".*"));

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
  public void testParseLocationNodeDeprecated() {
    String input = "node.*";
    InterfaceLocationAstNode expectedAst =
        InterfaceLocationAstNode.createFromNode(new NameRegexNodeAstNode(input));

    assertThat(ParserUtils.getAst(getRunner().run(input)), equalTo(expectedAst));
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
                InterfaceLocationAstNode.createFromInterfaceWithNode(
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
