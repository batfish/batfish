package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class SchemaTest {

  @Test
  public void constructorBase() {
    new EqualsTester()
        .addEqualityGroup(new Schema("Integer"), Schema.INTEGER)
        .addEqualityGroup(Schema.STRING)
        .testEquals();
  }

  @Test
  public void constructorCollection() {
    new EqualsTester()
        .addEqualityGroup(new Schema("List<Integer>"), Schema.list(Schema.INTEGER))
        .addEqualityGroup(Schema.set(Schema.INTEGER))
        .addEqualityGroup(Schema.INTEGER)
        .testEquals();
  }

  @Test
  public void constructorNestedCollection() {
    new EqualsTester()
        .addEqualityGroup(new Schema("Set<List<Integer>>"), Schema.set(Schema.list(Schema.INTEGER)))
        .addEqualityGroup(Schema.set(Schema.set(Schema.INTEGER)))
        .addEqualityGroup(Schema.list(Schema.list(Schema.INTEGER)))
        .addEqualityGroup(Schema.set(Schema.INTEGER))
        .addEqualityGroup(Schema.list(Schema.INTEGER))
        .addEqualityGroup(Schema.INTEGER)
        .testEquals();
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
