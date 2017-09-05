package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Testrig}. */
@RunWith(JUnit4.class)
public class TestrigTest {

  @Test public void testConstructorAndGetter() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    List<Environment> environments = Lists.newArrayList(environment);
    Map<String, String> configurations = Collections.singletonMap("config", "configContent");
    Testrig testrig = new Testrig("testrig", environments, configurations);
    assertThat(testrig.getName(), equalTo("testrig"));
    assertThat(testrig.getEnvironments(), equalTo(environments));
    assertThat(testrig.getConfigurations(), equalTo(configurations));
  }

  @Test public void testToString() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    List<Environment> environments = Lists.newArrayList(environment);
    Map<String, String> configurations = Collections.singletonMap("config", "configContent");
    Testrig testrig = new Testrig("testrig", environments, configurations);
    String expected = String.format("Testrig{name=testrig, environments=%s, configurations=%s}",
        environments,
        configurations);
    assertThat(testrig.toString(), equalTo(expected));
  }

  @Test public void testEquals() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    List<Environment> environments = Lists.newArrayList(environment);
    Map<String, String> configurations = Collections.singletonMap("config", "configContent");
    Testrig t = new Testrig("foo", Lists.newArrayList(), Maps.newHashMap());
    Testrig tCopy = new Testrig("foo", Lists.newArrayList(), Maps.newHashMap());
    Testrig tWithEnvironments = new Testrig("foo", environments, Maps.newHashMap());
    Testrig tWithConfigurations = new Testrig("foo", Lists.newArrayList(), configurations);
    Testrig tOtherName = new Testrig("bar", Lists.newArrayList(), Maps.newHashMap());

    new EqualsTester().addEqualityGroup(t, tCopy)
        .addEqualityGroup(tWithEnvironments)
        .addEqualityGroup(tWithConfigurations)
        .addEqualityGroup(tOtherName)
        .testEquals();
  }
}
