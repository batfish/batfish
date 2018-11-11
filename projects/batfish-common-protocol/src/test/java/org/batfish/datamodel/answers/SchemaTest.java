package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class SchemaTest {

  @Test
  public void constructor() {
    assertThat(new Schema("Integer"), equalTo(Schema.INTEGER));
    assertThat(new Schema("List<Integer>"), equalTo(Schema.list(Schema.INTEGER)));
    assertThat(new Schema("Set<Integer>"), equalTo(Schema.set(Schema.INTEGER)));
    assertThat(new Schema("Set<Integer>"), not(equalTo(Schema.list(Schema.INTEGER))));
  }

  @Test
  public void isIntBased() {
    assertThat(Schema.INTEGER.isIntBased(), equalTo(true));
    assertThat(Schema.list(Schema.INTEGER).isIntBased(), equalTo(true));
    assertThat(Schema.set(Schema.INTEGER).isIntBased(), equalTo(true));
    assertThat(Schema.list(Schema.STRING).isIntBased(), equalTo(false));
  }
}
