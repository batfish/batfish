package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.OspfInterfacePropertySpecifier;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Tests of {@link Parser} producing {@link EnumSetAstNode}. It uses NamedStructureType as the enum
 * set for most examples, and has one test to check that things work for non-string values.
 */
public class ParserEnumSetTest {

  /** */
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Collection<?> ALL_NAMED_STRUCTURE_TYPES =
      Grammar.getEnumValues(Grammar.NAMED_STRUCTURE_SPECIFIER);

  private static AbstractParseRunner<AstNode> getRunner() {
    return getRunner(Grammar.NAMED_STRUCTURE_SPECIFIER);
  }

  private static AbstractParseRunner<AstNode> getRunner(Grammar grammar) {
    return new ReportingParseRunner<>(Parser.instance().getInputRule(grammar));
  }

  private static AbstractParseRunner<AstNode> getRunner(Collection<?> allValues) {
    Parser parser = Parser.instance();
    return new ReportingParseRunner<>(parser.input(parser.EnumSetSpec(allValues)));
  }

  private static ParboiledAutoComplete getPAC(String query, Grammar grammar) {
    return new ParboiledAutoComplete(
        grammar,
        Parser.instance().getInputRule(grammar),
        Parser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.EMPTY,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  /** A helper to test parsing of different types of enums */
  private static void testParseOtherProperties(String value1, String value2, Grammar grammar) {
    String query = String.format("%s,%s", value1, value2);

    Collection<?> allValues = Grammar.getEnumValues(grammar);

    EnumSetAstNode expectedAst =
        new UnionEnumSetAstNode(
            new ValueEnumSetAstNode<>(value1, allValues),
            new ValueEnumSetAstNode<>(value2, allValues));

    assertThat(ParserUtils.getAst(getRunner(grammar).run(query)), equalTo(expectedAst));
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
                Grammar.NAMED_STRUCTURE_SPECIFIER,
                Parser.instance().getInputRule(Grammar.NAMED_STRUCTURE_SPECIFIER),
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
                Grammar.NAMED_STRUCTURE_SPECIFIER,
                Parser.instance().getInputRule(Grammar.NAMED_STRUCTURE_SPECIFIER),
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
    EnumSetAstNode expectedAst = new ValueEnumSetAstNode<>(query, ALL_NAMED_STRUCTURE_TYPES);

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
    EnumSetAstNode expectedAst = new ValueEnumSetAstNode<>(query, ALL_NAMED_STRUCTURE_TYPES);

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
  public void testParseSuperStrings() {
    Collection<String> allValues = ImmutableList.of("long", "longer");

    assertThat(
        ParserUtils.getAst(getRunner(allValues).run("longer")),
        equalTo(new ValueEnumSetAstNode<>("longer", allValues)));
    assertThat(
        ParserUtils.getAst(getRunner(allValues).run("long")),
        equalTo(new ValueEnumSetAstNode<>("long", allValues)));
  }

  @Test
  public void testParseFilterUnion() {
    String t1 = NamedStructurePropertySpecifier.IP_ACCESS_LIST;
    String t2Regex = "ip";
    UnionEnumSetAstNode expectedNode =
        new UnionEnumSetAstNode(
            new ValueEnumSetAstNode<>(t1, ALL_NAMED_STRUCTURE_TYPES),
            new RegexEnumSetAstNode(t2Regex));

    assertThat(
        ParserUtils.getAst(getRunner().run(String.format("%s,/%s/", t1, t2Regex))),
        equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(String.format(" %s , /%s/ ", t1, t2Regex))),
        equalTo(expectedNode));
  }

  /** Test that application enums (which are not strings) work */
  @Test
  public void testApplication() {
    String query = "";
    Set<ParboiledAutoCompleteSuggestion> suggestions =
        getPAC("", Grammar.APPLICATION_SPECIFIER).run();

    assertThat(
        suggestions,
        equalTo(
            Stream.concat(
                    Arrays.stream(Protocol.values())
                        .map(
                            val ->
                                new ParboiledAutoCompleteSuggestion(
                                    val.toString(), query.length(), Type.ENUM_SET_VALUE)),
                    ImmutableSet.of(
                        new ParboiledAutoCompleteSuggestion("/", 0, Type.ENUM_SET_REGEX))
                        .stream())
                .collect(ImmutableSet.toImmutableSet())));
  }

  /**
   * Test that in enums where some options are substrings, we autocomplete to their super strings
   * properly, instead of limiting ourselves to the first match.
   */
  @Test
  public void testAutoCompleteSuperStrings() {
    assertThat(
        getPAC("ht", Grammar.APPLICATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, Type.ENUM_SET_VALUE),
            new ParboiledAutoCompleteSuggestion(
                Protocol.HTTPS.toString(), 0, Type.ENUM_SET_VALUE)));

    assertThat(
        getPAC("http", Grammar.APPLICATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(",", 4, Type.ENUM_SET_SET_OP),
            new ParboiledAutoCompleteSuggestion(
                Protocol.HTTPS.toString(), 0, Type.ENUM_SET_VALUE)));

    assertThat(
        getPAC("https", Grammar.APPLICATION_SPECIFIER).run(),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion(",", 5, Type.ENUM_SET_SET_OP)));
  }

  /** Test that we auto complete properly when the query is a non-prefix substring */
  @Test
  public void testAutoCompleteNonPrefixSubstrings() {
    assertThat(
        getPAC("tt", Grammar.APPLICATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, Type.ENUM_SET_VALUE),
            new ParboiledAutoCompleteSuggestion(
                Protocol.HTTPS.toString(), 0, Type.ENUM_SET_VALUE)));
  }

  /** Test that bgp peer properties are being parsed */
  @Test
  public void testParseBgpPeerProperties() {
    testParseOtherProperties(
        BgpPeerPropertySpecifier.LOCAL_IP,
        BgpPeerPropertySpecifier.LOCAL_AS,
        Grammar.BGP_PEER_PROPERTY_SPECIFIER);
  }

  /** Test that bgp process properties are being parsed */
  @Test
  public void testParseBgpProcessProperties() {
    testParseOtherProperties(
        BgpProcessPropertySpecifier.ROUTE_REFLECTOR,
        BgpProcessPropertySpecifier.TIE_BREAKER,
        Grammar.BGP_PROCESS_PROPERTY_SPECIFIER);
  }

  @Test
  public void testParseBgpSessionType() {
    testParseOtherProperties(
        SessionType.EBGP_SINGLEHOP.toString(),
        SessionType.EBGP_MULTIHOP.toString(),
        Grammar.BGP_SESSION_TYPE_SPECIFIER);
  }

  /** Test that interface properties are being parsed */
  @Test
  public void testParseInterfaceProperties() {
    testParseOtherProperties(
        InterfacePropertySpecifier.DESCRIPTION,
        InterfacePropertySpecifier.ACCESS_VLAN,
        Grammar.INTERFACE_PROPERTY_SPECIFIER);
  }

  @Test
  public void testParseIpsecSessionStatus() {
    testParseOtherProperties(
        IpsecSessionStatus.MISSING_END_POINT.toString(),
        IpsecSessionStatus.IKE_PHASE1_FAILED.toString(),
        Grammar.IPSEC_SESSION_STATUS_SPECIFIER);
  }

  @Test
  public void testParseNodeProperties() {
    testParseOtherProperties(
        NodePropertySpecifier.NTP_SERVERS,
        NodePropertySpecifier.DNS_SERVERS,
        Grammar.NODE_PROPERTY_SPECIFIER);
  }

  @Test
  public void testParseOspfInterfaceProperties() {
    testParseOtherProperties(
        OspfInterfacePropertySpecifier.OSPF_AREA_NAME,
        OspfInterfacePropertySpecifier.OSPF_COST,
        Grammar.OSPF_INTERFACE_PROPERTY_SPECIFIER);
  }

  @Test
  public void testParseOspfProcessProperties() {
    testParseOtherProperties(
        OspfProcessPropertySpecifier.AREA_BORDER_ROUTER,
        OspfProcessPropertySpecifier.AREAS,
        Grammar.OSPF_PROCESS_PROPERTY_SPECIFIER);
  }

  @Test
  public void testVxlanVniProperties() {
    testParseOtherProperties(
        VxlanVniPropertySpecifier.VTEP_FLOOD_LIST,
        VxlanVniPropertySpecifier.VXLAN_PORT,
        Grammar.VXLAN_VNI_PROPERTY_SPECIFIER);
  }
}
