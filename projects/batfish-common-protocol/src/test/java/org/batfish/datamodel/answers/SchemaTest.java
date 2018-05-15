package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Ip;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchemaTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void fromValue() {
    // test a few base values; since the code path is the same, we needn't test them all
    assertThat(Schema.fromValue(true), equalTo(Schema.BOOLEAN));
    assertThat(Schema.fromValue(2), equalTo(Schema.INTEGER));
    assertThat(Schema.fromValue(new Ip("1.1.1.1")), equalTo(Schema.IP));
    assertThat(Schema.fromValue("string"), equalTo(Schema.STRING));

    // un-mappable value because no Schema exists for Schema
    assertThat(Schema.fromValue(Schema.STRING), equalTo(null));

    // check list-ification
    assertThat(Schema.fromValue(ImmutableList.of(2)), equalTo(Schema.list(Schema.INTEGER)));
    assertThat(Schema.fromValue(ImmutableSet.of(2)), equalTo(Schema.list(Schema.INTEGER)));
  }

  @Test
  public void fromValueMaps() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("No Schema exists");
    Schema.fromValue(ImmutableSortedMap.of("a", "2"));
  }
}
