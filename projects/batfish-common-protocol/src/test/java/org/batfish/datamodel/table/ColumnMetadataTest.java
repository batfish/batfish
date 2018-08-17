package org.batfish.datamodel.table;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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

  @Test
  public void testInvalidColumnNameWithSpecialCharacters() {

    // Allow the tilde character in the column name
    assertThat(ColumnMetadata.isLegalColumnName("~colNamewith~Tilde"), equalTo(true));

    // Allow the under score in the column name
    assertThat(ColumnMetadata.isLegalColumnName("_colNameWith_UnderScore"), equalTo(true));

    // Do not allow the hyphen in the start of column name
    assertThat(ColumnMetadata.isLegalColumnName("-colNameWith-hyphen"), equalTo(false));
    assertThat(ColumnMetadata.isLegalColumnName("colNameWith-hyphen"), equalTo(true));

    // Do not allow the space in the column name
    assertThat(ColumnMetadata.isLegalColumnName("column name"), equalTo(false));

    // Do not allow . characters in the start of column name
    assertThat(ColumnMetadata.isLegalColumnName(".test.colName"), equalTo(false));
    assertThat(ColumnMetadata.isLegalColumnName("test.colName"), equalTo(true));

    // Do not allow @ characters in the start of column name
    assertThat(ColumnMetadata.isLegalColumnName("@test@colName"), equalTo(false));
    assertThat(ColumnMetadata.isLegalColumnName("test@colName"), equalTo(true));

    // Allow the under score, tilde, hyphen, numbers in the column name
    assertThat(ColumnMetadata.isLegalColumnName("_colName_With~Under-Score0-9"), equalTo(true));

    // Allow the under score, tilde, hyphen, numbers in the column name
    assertThat(
        ColumnMetadata.isLegalColumnName("colNameWith!@#$%^&*()+Characters"), equalTo(false));

    // Atleast one character should be given as column name
    assertThat(ColumnMetadata.isLegalColumnName(""), equalTo(false));
    assertThat(ColumnMetadata.isLegalColumnName("a"), equalTo(true));
  }
}
