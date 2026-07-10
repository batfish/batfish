package org.batfish.specifier.parse.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

/**
 * Validates that the ANTLR specifier grammar accepts the same inputs the parboiled grammar did.
 * This checks the lexer/parser design (in particular the scannerless-to-lexer boundary decisions)
 * against representative inputs drawn from the parboiled Parser*Test suites, before the AST visitor
 * is wired up. It asserts the input parses with no syntax errors and consumes all input.
 */
public class SpecifierGrammarParseTest {

  private static class ErrorCollector extends BaseErrorListener {
    final List<String> _errors = new ArrayList<>();

    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int charPositionInLine,
        String msg,
        RecognitionException e) {
      _errors.add(String.format("%d:%d %s", line, charPositionInLine, msg));
    }
  }

  private static void assertParses(String input, Function<SpecifierParser, ParseTree> entry) {
    assertParses(input, entry, true);
  }

  private static void assertParses(
      String input, Function<SpecifierParser, ParseTree> entry, boolean slashInNames) {
    assertParses(input, entry, slashInNames, false);
  }

  private static void assertParses(
      String input,
      Function<SpecifierParser, ParseTree> entry,
      boolean slashInNames,
      boolean appKeywords) {
    SpecifierLexer lexer = new SpecifierLexer(CharStreams.fromString(input));
    lexer.slashInNames = slashInNames;
    lexer.appKeywords = appKeywords;
    ErrorCollector lexErrors = new ErrorCollector();
    lexer.removeErrorListeners();
    lexer.addErrorListener(lexErrors);
    SpecifierParser parser = new SpecifierParser(new CommonTokenStream(lexer));
    ErrorCollector parseErrors = new ErrorCollector();
    parser.removeErrorListeners();
    parser.addErrorListener(parseErrors);

    ParseTree tree = entry.apply(parser);

    assertThat("lex errors for [" + input + "]", lexErrors._errors, empty());
    assertThat("parse errors for [" + input + "]", parseErrors._errors, empty());
    // The *Input rules end in EOF, so a clean parse means all input was consumed.
    assertThat(
        "consumed all input for [" + input + "]",
        tree.getText().isEmpty() || parser.getCurrentToken().getType() == SpecifierParser.EOF,
        equalTo(true));
  }

  private static void assertNode(String input) {
    assertParses(input, SpecifierParser::nodeSpecInput);
  }

  private static void assertInterface(String input) {
    assertParses(input, SpecifierParser::interfaceSpecInput);
  }

  private static void assertFilter(String input) {
    assertParses(input, SpecifierParser::filterSpecInput);
  }

  private static void assertIpSpace(String input) {
    assertParses(input, SpecifierParser::ipSpaceSpecInput);
  }

  private static void assertLocation(String input) {
    assertParses(input, SpecifierParser::locationSpecInput);
  }

  private static void assertRoutingPolicy(String input) {
    assertParses(input, SpecifierParser::routingPolicySpecInput);
  }

  private static void assertIpProtocol(String input) {
    assertParses(input, SpecifierParser::ipProtocolSpecInput, false);
  }

  private static void assertApp(String input) {
    assertParses(input, SpecifierParser::appSpecInput, false, /* appKeywords= */ true);
  }

  private static void assertEnumSet(String input) {
    assertParses(input, SpecifierParser::enumSetSpecInput);
  }

  @Test
  public void testNode() {
    assertNode("node.com-011");
    assertNode("@role(a, b)");
    assertNode(" @role ( a , b ) ");
    assertNode("@deviceType(router)");
    assertNode("/^node-0.1\\/0.*.?$/");
    assertNode("node.*");
    assertNode(".*node.*");
    assertNode("(node-lhr)");
    assertNode("node0\\node1");
    assertNode("node0&node1");
    assertNode("node0,node1");
    assertNode("node0\\node1&eth1");
    assertNode("\"node with spaces\"");
  }

  @Test
  public void testInterface() {
    assertInterface("Ethernet0");
    assertInterface("ge-0/0/0");
    assertInterface("@connectedTo(1.1.1.1)");
    assertInterface("@interfaceGroup(book, group)");
    assertInterface("@interfaceType(physical)");
    assertInterface("@vrf(vrf1)");
    assertInterface("@zone(zone1)");
    assertInterface("node1[Ethernet0]");
    assertInterface("/eth.*/");
    assertInterface("eth.*");
  }

  @Test
  public void testFilter() {
    assertFilter("acl1");
    assertFilter("@in(Ethernet0)");
    assertFilter("@out(Ethernet0)");
    assertFilter("node1[acl1]");
    assertFilter("acl1,acl2");
    assertFilter("/acl.*/");
  }

  @Test
  public void testIpSpace() {
    assertIpSpace("1.1.1.0/24");
    assertIpSpace("1.1.1.1");
    assertIpSpace("1.1.1.1:255.255.255.0");
    assertIpSpace("1.1.1.1 - 1.1.1.2");
    assertIpSpace("@addressgroup(book, group)");
    assertIpSpace("1.1.1.0/24, 2.2.2.0/24");
  }

  @Test
  public void testLocation() {
    assertLocation("node1");
    assertLocation("node1[Ethernet0]");
    assertLocation("@enter(node1[Ethernet0])");
    assertLocation("internet");
    assertLocation("@vrf(vrf1)");
  }

  @Test
  public void testRoutingPolicy() {
    assertRoutingPolicy("rp1");
    assertRoutingPolicy("/rp.*/");
    assertRoutingPolicy("rp.*");
    assertRoutingPolicy("rp1,rp2");
  }

  @Test
  public void testIpProtocol() {
    assertIpProtocol("tcp");
    assertIpProtocol("51");
    assertIpProtocol("!tcp");
    assertIpProtocol("tcp,udp");
  }

  @Test
  public void testApp() {
    assertApp("http");
    assertApp("icmp");
    assertApp("icmp/8");
    assertApp("icmp/8/0");
    assertApp("tcp/80");
    assertApp("tcp/80,443");
    assertApp("udp/53");
    assertApp("tcp/80-90");
  }

  @Test
  public void testEnumSet() {
    assertEnumSet("value1");
    assertEnumSet("!value1");
    assertEnumSet("/val.*/");
    assertEnumSet("value1,value2");
  }
}
