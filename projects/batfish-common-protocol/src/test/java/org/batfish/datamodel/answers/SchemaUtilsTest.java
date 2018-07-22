package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
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
  }

  @Test
  public void convertTypeClassCastException() {
    _thrown.expect(ClassCastException.class);
    _thrown.expectMessage("Cannot recover");
    SchemaUtils.convertType(toJson(ImmutableList.of(1)), Schema.INTEGER);
  }
}
