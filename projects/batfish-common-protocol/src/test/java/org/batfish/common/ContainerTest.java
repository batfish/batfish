package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.pojo.Analysis;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Container}. */
@RunWith(JUnit4.class)
public class ContainerTest {

  @Test
  public void testConstructorAndGetter() {
    List<String> testrigs = Lists.newArrayList("testrig");
    List<Analysis> analyses = Lists.newArrayList(new Analysis("analysis", null));
    Container container = new Container("container", testrigs, analyses);
    assertThat(container.getName(), equalTo("container"));
    assertThat(container.getTestrigs(), equalTo(testrigs));
    assertThat(container.getAnalyses(), equalTo(analyses));
  }

  @Test
  public void testToString() {
    Container c = new Container("foo", null, null);
    assertThat(c.toString(), equalTo("Container{name=foo, testrigs=[], analyses=[]}"));
  }

  @Test
  public void testEquals() {
    Container c = new Container("foo", Lists.newArrayList(), Lists.newArrayList());
    Container cCopy = new Container("foo", Lists.newArrayList(), Lists.newArrayList());
    Container cWithTestrig =
        new Container("foo", Lists.newArrayList("testrig"), Lists.newArrayList());
    Container cWithAnalyses =
        new Container(
            "foo", Lists.newArrayList(), Lists.newArrayList(new Analysis("analysis", null)));
    Container cOtherName = new Container("bar", Lists.newArrayList(), Lists.newArrayList());

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithTestrig)
        .addEqualityGroup(cWithAnalyses)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
