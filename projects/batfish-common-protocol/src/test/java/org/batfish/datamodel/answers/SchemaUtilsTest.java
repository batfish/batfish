package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.pojo.Node;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchemaUtilsTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static JsonNode toJson(Object object) {
    return BatfishObjectMapper.mapper().valueToTree(object);
  }

  @Test
  public void convertTypeCorrect() {
    assertThat(SchemaUtils.convertType(toJson(2), Schema.INTEGER), equalTo(2));
    assertThat(
        SchemaUtils.convertType(toJson(ImmutableList.of(1, 2)), Schema.list(Schema.INTEGER)),
        equalTo(ImmutableList.of(1, 2)));
    assertThat(
        SchemaUtils.convertType(
            toJson(ImmutableList.of(ImmutableList.of(1), ImmutableList.of(2))),
            Schema.list(Schema.list(Schema.INTEGER))),
        equalTo(ImmutableList.of(ImmutableList.of(1), ImmutableList.of(2))));
  }

  @Test
  public void convertTypeClassCastException() {
    _thrown.expect(ClassCastException.class);
    _thrown.expectMessage("Cannot recover");
    SchemaUtils.convertType(toJson(ImmutableList.of(1)), Schema.INTEGER);
  }

  @Test
  public void testIsValidObject_Node() {
    assertFalse(SchemaUtils.isValidObject("nodeName", Schema.NODE));
    assertTrue(SchemaUtils.isValidObject(new Node("node"), Schema.NODE));
  }
}
