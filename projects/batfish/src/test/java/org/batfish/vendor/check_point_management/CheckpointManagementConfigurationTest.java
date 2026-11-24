package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link CheckpointManagementConfiguration}. */
final class CheckpointManagementConfigurationTest {

  @Test
  void testJavaSerialization() {
    CheckpointManagementConfiguration obj =
        new CheckpointManagementConfiguration(ImmutableMap.of());
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    CheckpointManagementConfiguration obj =
        new CheckpointManagementConfiguration(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new CheckpointManagementConfiguration(ImmutableMap.of()))
        .addEqualityGroup(
            new CheckpointManagementConfiguration(
                ImmutableMap.of("a", new ManagementServer(ImmutableMap.of(), "a"))))
        .testEquals();
  }
}
