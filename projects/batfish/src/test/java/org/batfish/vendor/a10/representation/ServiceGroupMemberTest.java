package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.vendor.a10.representation.ServiceGroupMember.NameAndPort;
import org.junit.Test;

/** Test of {@link ServiceGroupMember} */
public class ServiceGroupMemberTest {
  @Test
  public void testNameAndPortEquality() {
    NameAndPort obj = new NameAndPort("name", 1);
    new EqualsTester()
        .addEqualityGroup(obj, new NameAndPort("name", 1))
        .addEqualityGroup(new NameAndPort("name0", 1))
        .addEqualityGroup(new NameAndPort("name0", 10))
        .testEquals();
  }

  @Test
  public void testNameAndPortSerialization() {
    NameAndPort obj = new NameAndPort("name", 1);
    NameAndPort clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }
}
