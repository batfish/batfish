package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link ManagementServer}. */
public final class ManagementServerTest {

  @Test
  public void testJavaSerialization() {
    ManagementServer obj = new ManagementServer(ImmutableMap.of(), "a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    ManagementServer obj = new ManagementServer(ImmutableMap.of(), "a");
    new EqualsTester()
        .addEqualityGroup(obj, new ManagementServer(ImmutableMap.of(), "a"))
        .addEqualityGroup(
            new ManagementServer(
                ImmutableMap.of(
                    "d",
                    new ManagementDomain(
                        new Domain("a", Uid.of("1")),
                        ImmutableMap.of(),
                        ImmutableMap.of(),
                        ImmutableList.of())),
                "a"))
        .addEqualityGroup(new ManagementServer(ImmutableMap.of(), "b"))
        .testEquals();
  }
}
