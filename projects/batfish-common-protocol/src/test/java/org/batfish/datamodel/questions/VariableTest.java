package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public final class VariableTest {

  @Test
  public void testSerializationFields()
      throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
    Variable variable = new Variable();
    String fieldName = "f1";
    Field field = new Field();
    field.setOptional(true);
    variable.setFields(ImmutableMap.of(fieldName, field));

    // fields survive serialization cyle intact
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(BatfishObjectMapper.writeString(variable), Variable.class)
            .getFields()
            .get(fieldName)
            .getOptional(),
        equalTo(true));
  }
}
