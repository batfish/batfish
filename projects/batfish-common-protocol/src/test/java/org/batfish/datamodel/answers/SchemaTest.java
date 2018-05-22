package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class SchemaTest {

  @Test
  public void isIntOrIntList() {
    assertThat(Schema.INTEGER.isIntOrIntList(), equalTo(true));
    assertThat(Schema.list(Schema.INTEGER).isIntOrIntList(), equalTo(true));
    assertThat(Schema.list(Schema.STRING).isIntOrIntList(), equalTo(false));
  }

  @Test
  public void isList() {
    assertThat(Schema.list(Schema.STRING).isList(), equalTo(true));
    assertThat(Schema.STRING.isList(), equalTo(false));
  }
}
