package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class SchemaTest {

  @Test
  public void constructorBase() {
    assertThat(new Schema("Integer"), equalTo(Schema.INTEGER));
    assertThat(new Schema("String"), not(equalTo(Schema.INTEGER)));
  }

  @Test
  public void constructorCollection() {
    assertThat(new Schema("List<Integer>"), equalTo(Schema.list(Schema.INTEGER)));
    assertThat(new Schema("Set<Integer>"), equalTo(Schema.set(Schema.INTEGER)));
    assertThat(new Schema("Set<Integer>"), not(equalTo(Schema.list(Schema.INTEGER))));
  }

  @Test
  public void constructorNestedCollection() {
    assertThat(new Schema("Set<List<Integer>>"), equalTo(Schema.set(Schema.list(Schema.INTEGER))));
    assertThat(new Schema("Set<Set<Integer>>"), equalTo(Schema.set(Schema.set(Schema.INTEGER))));
  }

  @Test
  public void isIntBased() {
    assertThat(Schema.INTEGER.isIntBased(), equalTo(true));
    assertThat(Schema.list(Schema.INTEGER).isIntBased(), equalTo(true));
    assertThat(Schema.set(Schema.INTEGER).isIntBased(), equalTo(true));
    assertThat(Schema.set(Schema.list(Schema.INTEGER)).isIntBased(), equalTo(true));
    assertThat(Schema.list(Schema.STRING).isIntBased(), equalTo(false));
  }

  @Test
  public void isCollection() {
    assertThat(Schema.INTEGER.isCollection(), equalTo(false));
    assertThat(Schema.list(Schema.INTEGER).isCollection(), equalTo(true));
    assertThat(Schema.set(Schema.INTEGER).isCollection(), equalTo(true));
    assertThat(Schema.set(Schema.list(Schema.INTEGER)).isCollection(), equalTo(true));
  }
}
