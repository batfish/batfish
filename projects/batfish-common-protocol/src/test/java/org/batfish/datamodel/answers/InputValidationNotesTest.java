package org.batfish.datamodel.answers;

import static org.batfish.datamodel.answers.InputValidationNotes.Validity.EMPTY;
import static org.batfish.datamodel.answers.InputValidationNotes.Validity.INVALID;
import static org.batfish.datamodel.answers.InputValidationNotes.Validity.VALID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InputValidationNotesTest {

  @Test
  public void testEquals() {
    InputValidationNotes notes = new InputValidationNotes(VALID, null, null);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(notes, notes, new InputValidationNotes(VALID, null, null))
        .addEqualityGroup(new InputValidationNotes(INVALID, null, null))
        .addEqualityGroup(new InputValidationNotes(EMPTY, null, null))
        .addEqualityGroup(new InputValidationNotes(VALID, "description", null))
        .addEqualityGroup(
            new InputValidationNotes(VALID, "description", ImmutableList.of("expansion")))
        .addEqualityGroup(
            new InputValidationNotes(VALID, "description2", ImmutableList.of("expansion")))
        .addEqualityGroup(
            new InputValidationNotes(VALID, "description", ImmutableList.of("expansion2")))
        .addEqualityGroup(
            new InputValidationNotes(INVALID, "description", ImmutableList.of("expansion")))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    InputValidationNotes validationNotes =
        new InputValidationNotes(
            VALID, "some description", ImmutableList.of("expansion1", "expansion2"));

    assertThat(
        BatfishObjectMapper.clone(validationNotes, InputValidationNotes.class),
        equalTo(validationNotes));
  }
}
