package org.batfish.datamodel.questions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Assertion.AssertionType;
import org.batfish.datamodel.table.Rows;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test Assertion functionality */
public class AssertionTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testEvaluateCount() throws IOException {
    Assertion twoCount = new Assertion(AssertionType.countequals, new IntNode(2));

    Rows oneRow = new Rows();
    oneRow.add(BatfishObjectMapper.mapper().createObjectNode());

    Rows twoRows = new Rows();
    twoRows.add(BatfishObjectMapper.mapper().createObjectNode());
    twoRows.add(BatfishObjectMapper.mapper().createObjectNode());

    assertThat(twoCount.evaluate(oneRow), equalTo(false));
    assertThat(twoCount.evaluate(twoRows), equalTo(true));
  }

  @Test
  public void testEvaluateEqualsFalse() throws IOException {
    Assertion assertion =
        new Assertion(
            AssertionType.equals,
            BatfishObjectMapper.mapper()
                .readValue("[{\"key1\": \"value1\"}, {\"key2\": \"value2\"}]", JsonNode.class));

    Rows sameRows = new Rows();
    sameRows.add(
        (ObjectNode)
            BatfishObjectMapper.mapper().createObjectNode().set("key1", new TextNode("value1")));
    sameRows.add(
        (ObjectNode)
            BatfishObjectMapper.mapper().createObjectNode().set("key2", new TextNode("value2")));

    Rows diffRows = new Rows();
    diffRows.add(
        (ObjectNode)
            BatfishObjectMapper.mapper().createObjectNode().set("key2", new TextNode("value2")));
    diffRows.add(
        (ObjectNode)
            BatfishObjectMapper.mapper().createObjectNode().set("key1", new TextNode("value1")));

    assertThat(assertion.evaluate(sameRows), equalTo(true));
    assertThat(assertion.evaluate(diffRows), equalTo(false));
  }
}
