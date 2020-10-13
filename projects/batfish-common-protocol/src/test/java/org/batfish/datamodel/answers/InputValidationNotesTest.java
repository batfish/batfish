package org.batfish.datamodel.answers;

import static org.batfish.datamodel.answers.InputValidationNotes.Validity.INVALID;
import static org.batfish.datamodel.answers.InputValidationNotes.Validity.NO_MATCH;
import static org.batfish.datamodel.answers.InputValidationNotes.Validity.VALID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InputValidationNotesTest {

  @Test
  public void testEquals() {
    InputValidationNotes notes = new InputValidationNotes(VALID, null, -1, null);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(notes, notes, new InputValidationNotes(VALID, null, -1, null))
        .addEqualityGroup(new InputValidationNotes(INVALID, null, -1, null))
        .addEqualityGroup(new InputValidationNotes(NO_MATCH, null, -1, null))
        .addEqualityGroup(new InputValidationNotes(VALID, "description", -1, null))
        .addEqualityGroup(
            new InputValidationNotes(VALID, "description", -1, ImmutableList.of("expansion")))
        .addEqualityGroup(
            new InputValidationNotes(VALID, "description2", -1, ImmutableList.of("expansion")))
        .addEqualityGroup(
            new InputValidationNotes(VALID, "description", -1, ImmutableList.of("expansion2")))
        .addEqualityGroup(
            new InputValidationNotes(INVALID, "description", -1, ImmutableList.of("expansion")))
        .addEqualityGroup(new InputValidationNotes(VALID, null, 0, null))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    InputValidationNotes validationNotes =
        new InputValidationNotes(
            VALID, "some description", 3, ImmutableList.of("expansion1", "expansion2"));

    assertThat(
        BatfishObjectMapper.clone(validationNotes, InputValidationNotes.class),
        equalTo(validationNotes));
  }
}
