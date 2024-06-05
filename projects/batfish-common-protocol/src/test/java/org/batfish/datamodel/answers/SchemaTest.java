package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class SchemaTest {

  @Test
  public void constructorTest() {
    new EqualsTester()
        .addEqualityGroup(new Schema("Set<List<Integer>>"), Schema.set(Schema.list(Schema.INTEGER)))
        .addEqualityGroup(new Schema("List<Set<Integer>>"), Schema.list(Schema.set(Schema.INTEGER)))
        .addEqualityGroup(new Schema("Set<Set<Integer>>"), Schema.set(Schema.set(Schema.INTEGER)))
        .addEqualityGroup(new Schema("List<Integer>"), Schema.list(Schema.INTEGER))
        .addEqualityGroup(new Schema("Set<Integer>"), Schema.set(Schema.INTEGER))
        .addEqualityGroup(new Schema("Integer"), Schema.INTEGER)
        .addEqualityGroup(new Schema("String"), Schema.STRING)
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
