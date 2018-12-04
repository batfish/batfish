package org.batfish.datamodel;

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
}
