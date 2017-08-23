package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.pojo.Analysis;
import org.batfish.datamodel.pojo.Testrig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Container}. */
@RunWith(JUnit4.class)
public class ContainerTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorAndGetter() {
    List<Testrig> testrigs =
        Lists.newArrayList(
            new Testrig(
                "testrig",
                Lists.newArrayList(),
                Lists.newArrayList(),
                Lists.newArrayList(),
                Maps.newHashMap()));
    List<Analysis> analyses = Lists.newArrayList(new Analysis("analysis", Maps.newHashMap()));
    Container container = new Container("container", testrigs, analyses);
    assertThat(container.getName(), equalTo("container"));
    assertThat(container.getTestrigs(), equalTo(testrigs));
    assertThat(container.getAnalyses(), equalTo(analyses));
  }

  @Test
  public void testToString() {
    List<Testrig> testrigs =
        Lists.newArrayList(
            new Testrig(
                "testrig",
                Lists.newArrayList(),
                Lists.newArrayList(),
                Lists.newArrayList(),
                Maps.newHashMap()));
    List<Analysis> analyses = Lists.newArrayList(new Analysis("analysis", Maps.newHashMap()));
    Container c = new Container("container", testrigs, analyses);
    assertThat(
        c.toString(),
        equalTo(
            String.format(
                "Container{name=container, testrigs=%s, analyses=%s}", testrigs, analyses)));
  }

  @Test
  public void testEquals() {
    List<Testrig> testrigs =
        Lists.newArrayList(
            new Testrig(
                "testrig",
                Lists.newArrayList(),
                Lists.newArrayList(),
                Lists.newArrayList(),
                Maps.newHashMap()));
    List<Analysis> analyses = Lists.newArrayList(new Analysis("analysis", Maps.newHashMap()));
    Container c = new Container("foo", Lists.newArrayList(), Lists.newArrayList());
    Container cCopy = new Container("foo", Lists.newArrayList(), Lists.newArrayList());
    Container cWithTestrig = new Container("foo", testrigs, Lists.newArrayList());
    Container cWithAnalyses = new Container("foo", Lists.newArrayList(), analyses);
    Container cOtherName = new Container("bar", Lists.newArrayList(), Lists.newArrayList());

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithTestrig)
        .addEqualityGroup(cWithAnalyses)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
