package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link Warning}. */
public class WarningTest {
  @Test
  public void testJavaSerialization() {
    Warning w = new Warning("A", "B");
    assertThat(SerializationUtils.clone(w), equalTo(w));

    Warning w2 = new Warning("A", null);
    assertThat(SerializationUtils.clone(w2), equalTo(w2));
  }

  @Test
  public void testJsonSerialization() {
    Warning w = new Warning("A", "B");
    assertThat(BatfishObjectMapper.clone(w, Warning.class), equalTo(w));

    Warning w2 = new Warning("A", null);
    assertThat(BatfishObjectMapper.clone(w2, Warning.class), equalTo(w2));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Warning("a", "b"), new Warning("a", "b"))
        .addEqualityGroup(new Warning("a", null))
        .addEqualityGroup(new Warning("a", "c"))
        .testEquals();
  }

  @Test
  public void testCompareTo() {
    List<Warning> warnings =
        ImmutableList.of(new Warning("a", null), new Warning("a", "a"), new Warning("a", "b"));

    assertThat(
        Lists.reverse(warnings).stream().sorted().collect(Collectors.toList()), equalTo(warnings));
  }
}
