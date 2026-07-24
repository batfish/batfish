package org.batfish.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.client.LenientJsonParser.LenientJsonException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link LenientJsonParser}. */
@RunWith(JUnit4.class)
public class LenientJsonParserTest {

  private static JsonNode parse(String text) throws LenientJsonException {
    return LenientJsonParser.parse(text);
  }

  @Test
  public void testUnquotedKey() throws Exception {
    JsonNode node = parse("{ nodes: \"as2.*\" }");
    assertThat(node.get("nodes").textValue(), equalTo("as2.*"));
  }

  @Test
  public void testEqualsSeparator() throws Exception {
    // The CLI historically passed params as {key=value}, relying on '=' as a separator.
    JsonNode node = parse("{ verbose=True }");
    assertTrue(node.get("verbose").isBoolean());
    assertThat(node.get("verbose").booleanValue(), equalTo(true));
  }

  @Test
  public void testFatArrowSeparator() throws Exception {
    JsonNode node = parse("{ k => 3 }");
    assertThat(node.get("k").intValue(), equalTo(3));
  }

  @Test
  public void testSemicolonPairSeparator() throws Exception {
    JsonNode node = parse("{ a: 1; b: 2 }");
    assertThat(node.get("a").intValue(), equalTo(1));
    assertThat(node.get("b").intValue(), equalTo(2));
  }

  @Test
  public void testCaseInsensitiveLiterals() throws Exception {
    JsonNode node = parse("{ t: TRUE, f: False, n: NULL }");
    assertThat(node.get("t").booleanValue(), equalTo(true));
    assertThat(node.get("f").booleanValue(), equalTo(false));
    assertTrue(node.get("n").isNull());
  }

  @Test
  public void testNestedObjectAndArray() throws Exception {
    JsonNode node = parse("{ headers={\"dstIps\": \"1.1.1.1\"}, nodes=\"host.*\" }");
    assertThat(node.get("headers").get("dstIps").textValue(), equalTo("1.1.1.1"));
    assertThat(node.get("nodes").textValue(), equalTo("host.*"));
  }

  @Test
  public void testArrayValue() throws Exception {
    JsonNode node = parse("{ xs: [1, 2, 3] }");
    assertThat(node.get("xs").size(), equalTo(3));
    assertThat(node.get("xs").get(2).intValue(), equalTo(3));
  }

  @Test
  public void testSingleQuotedString() throws Exception {
    JsonNode node = parse("{ 'a': 'b' }");
    assertThat(node.get("a").textValue(), equalTo("b"));
  }

  @Test
  public void testNumberTypes() throws Exception {
    JsonNode node = parse("{ i: 5, l: 5000000000, d: 1.5 }");
    assertThat(node.get("i").intValue(), equalTo(5));
    assertThat(node.get("l").longValue(), equalTo(5000000000L));
    assertThat(node.get("d").doubleValue(), equalTo(1.5));
  }

  @Test
  public void testBareUnquotedStringValue() throws Exception {
    JsonNode node = parse("{ a: passive }");
    assertThat(node.get("a").textValue(), equalTo("passive"));
  }

  @Test
  public void testTrailingComma() throws Exception {
    JsonNode node = parse("{ a: 1, }");
    assertThat(node.get("a").intValue(), equalTo(1));
    assertThat(node.size(), equalTo(1));
  }

  @Test
  public void testEmptyObject() throws Exception {
    JsonNode node = parse("{ }");
    assertThat(node.size(), equalTo(0));
    assertTrue(node.isObject());
  }

  @Test
  public void testInvalidMissingSeparator() {
    LenientJsonException e = assertThrows(LenientJsonException.class, () -> parse("{ a 1 }"));
    assertThat(e, instanceOf(LenientJsonException.class));
  }

  @Test
  public void testInvalidMissingValue() {
    assertThrows(LenientJsonException.class, () -> parse("{ a: }"));
  }
}
