package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link CheckpointManagementConfiguration}. */
public final class CheckpointManagementConfigurationTest {

  @Test
  public void testJavaSerialization() {
    CheckpointManagementConfiguration obj =
        new CheckpointManagementConfiguration(ImmutableMap.of());
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
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
