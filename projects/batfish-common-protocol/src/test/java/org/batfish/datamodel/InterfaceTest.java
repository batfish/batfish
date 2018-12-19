package org.batfish.datamodel;

import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.Interface} */
@RunWith(JUnit4.class)
public class InterfaceTest {

  @Test
  public void testDependencyEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new Dependency("i1", DependencyType.BIND), new Dependency("i1", DependencyType.BIND))
        .addEqualityGroup(new Dependency("i1", DependencyType.AGGREGATE))
        .addEqualityGroup(new Dependency("i2", DependencyType.BIND))
        .testEquals();
  }

  @Test
  public void testBlacklisting() {
    Interface i = Interface.builder().setName("iface").build();
    assertThat("Interface is not blacklisted", !i.getBlacklisted());
    assertThat("Interface is not disabled", i.getActive());

    i.blacklist();

    assertThat("Interface is blacklisted", i.getBlacklisted());
    assertThat("Interface is disabled", !i.getActive());
  }

  @Test
  public void testRealInterafceName() {
    assertThat(isRealInterfaceName("Ethernet0"), equalTo(true));
    assertThat(isRealInterfaceName("ge-0/0/0"), equalTo(true));
    assertThat(isRealInterfaceName("asdfasdf"), equalTo(true));
    assertThat(isRealInterfaceName("null_interface"), equalTo(false));
    assertThat(isRealInterfaceName("unset_local_interface"), equalTo(false));
    assertThat(isRealInterfaceName("invalid_local_interface"), equalTo(false));
    assertThat(isRealInterfaceName("dynamic"), equalTo(false));
  }
}
