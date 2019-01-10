package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.Variable.Type.BGP_PEER_PROPERTY_SPEC;
import static org.batfish.datamodel.questions.Variable.Type.BGP_PROCESS_PROPERTY_SPEC;
import static org.batfish.datamodel.questions.Variable.Type.INTERFACE_PROPERTY_SPEC;
import static org.batfish.datamodel.questions.Variable.Type.NAMED_STRUCTURE_SPEC;
import static org.batfish.datamodel.questions.Variable.Type.NODE_PROPERTY_SPEC;
import static org.batfish.datamodel.questions.Variable.Type.NODE_SPEC;
import static org.batfish.datamodel.questions.Variable.Type.OSPF_PROPERTY_SPEC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AutocompleteSuggestion.CompletionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class VariableTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

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

  @Test
  public void testFromStringCompletionTypes() {
    assertThat(
        Variable.Type.fromString(CompletionType.BGP_PEER_PROPERTY.toString()),
        equalTo(BGP_PEER_PROPERTY_SPEC));
    assertThat(
        Variable.Type.fromString(CompletionType.BGP_PROCESS_PROPERTY.toString()),
        equalTo(BGP_PROCESS_PROPERTY_SPEC));
    assertThat(
        Variable.Type.fromString(CompletionType.INTERFACE_PROPERTY.toString()),
        equalTo(INTERFACE_PROPERTY_SPEC));
    assertThat(
        Variable.Type.fromString(CompletionType.NAMED_STRUCTURE.toString()),
        equalTo(NAMED_STRUCTURE_SPEC));
    assertThat(Variable.Type.fromString(CompletionType.NODE.toString()), equalTo(NODE_SPEC));
    assertThat(
        Variable.Type.fromString(CompletionType.NODE_PROPERTY.toString()),
        equalTo(NODE_PROPERTY_SPEC));
    assertThat(
        Variable.Type.fromString(CompletionType.OSPF_PROPERTY.toString()),
        equalTo(OSPF_PROPERTY_SPEC));
  }

  @Test
  public void testFromStringInvalid() {
    String name = "blah";

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        "No " + Variable.Type.class.getSimpleName() + " with name: '" + name + "'");

    Variable.Type.fromString(name);
  }

  @Test
  public void testFromStringValid() {
    for (Variable.Type type : Variable.Type.values()) {
      assertThat(type, equalTo(Variable.Type.fromString(type.getName())));
    }
  }
}
