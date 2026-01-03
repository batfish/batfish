package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.answers.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link ColumnMetadata}. */
@RunWith(JUnit4.class)
public class ColumnMetadataTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testJacksonMissingName() {
    _thrown.expectMessage("'name' cannot be null");
    ColumnMetadata.jsonCreator(null, Schema.STRING, "desc", Boolean.TRUE, Boolean.FALSE);
  }

  @Test
  public void testJacksonMissingDescription() {
    _thrown.expectMessage("'description' cannot be null");
    ColumnMetadata.jsonCreator("name", Schema.STRING, null, Boolean.TRUE, Boolean.FALSE);
  }

  @Test
  public void testJacksonMissingSchema() {
    _thrown.expectMessage("'schema' cannot be null");
    ColumnMetadata.jsonCreator("name", null, "desc", Boolean.TRUE, Boolean.FALSE);
  }

  @Test
  public void testJacksonDeserializeWithDefaults() {
    ColumnMetadata c = ColumnMetadata.jsonCreator("name", Schema.STRING, "desc", null, null);
    assertThat(c.getName(), equalTo("name"));
    assertThat(c.getDescription(), equalTo("desc"));
    assertThat(c.getSchema(), equalTo(Schema.STRING));
    assertThat(c.getIsKey(), equalTo(true));
    assertThat(c.getIsValue(), equalTo(true));
  }
}
