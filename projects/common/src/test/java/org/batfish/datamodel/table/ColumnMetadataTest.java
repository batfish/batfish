package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.batfish.datamodel.answers.Schema;
import org.junit.jupiter.api.Test;

/** Tests of {@link ColumnMetadata}. */
class ColumnMetadataTest {

  @Test
  void testJacksonMissingName() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () ->
                ColumnMetadata.jsonCreator(
                    null, Schema.STRING, "desc", Boolean.TRUE, Boolean.FALSE));
    assertThat(exception.getMessage(), containsString("'name' cannot be null"));
  }

  @Test
  void testJacksonMissingDescription() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () ->
                ColumnMetadata.jsonCreator(
                    "name", Schema.STRING, null, Boolean.TRUE, Boolean.FALSE));
    assertThat(exception.getMessage(), containsString("'description' cannot be null"));
  }

  @Test
  void testJacksonMissingSchema() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () -> ColumnMetadata.jsonCreator("name", null, "desc", Boolean.TRUE, Boolean.FALSE));
    assertThat(exception.getMessage(), containsString("'schema' cannot be null"));
  }

  @Test
  void testJacksonDeserializeWithDefaults() {
    ColumnMetadata c = ColumnMetadata.jsonCreator("name", Schema.STRING, "desc", null, null);
    assertThat(c.getName(), equalTo("name"));
    assertThat(c.getDescription(), equalTo("desc"));
    assertThat(c.getSchema(), equalTo(Schema.STRING));
    assertThat(c.getIsKey(), equalTo(true));
    assertThat(c.getIsValue(), equalTo(true));
  }
}
