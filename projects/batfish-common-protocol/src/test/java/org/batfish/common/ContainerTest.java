package org.batfish.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.pojo.Analysis;
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
    List<String> testrigs = Lists.newArrayList("testrig");
    Map<String, Analysis> analyses = Maps.newHashMap();
    Container container = new Container("container", testrigs, analyses);
    assertThat(container.getName(), equalTo("container"));
    assertThat(container.getTestrigs(), equalTo(testrigs));
    assertThat(container.getAnalyses(), equalTo(analyses));
  }

  @Test
  public void testAddAnalysis() {
    Container container = new Container("container", Lists.newArrayList(), Maps.newHashMap());
    Analysis a = new Analysis("analysis", Maps.newHashMap());
    container.addAnalysis("analysis", a);
    assertThat(container.getAnalyses().get("analysis"), equalTo(a));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Analysis analysis already exists for container container"));
    container.addAnalysis("analysis", a);
  }

  @Test
  public void testDeleteAnalysis() {
    Container container =
        new Container(
            "container",
            Lists.newArrayList(),
            Maps.newHashMap(Collections.singletonMap("analysis", new Analysis("analysis", null))));
    container.deleteAnalysis("analysis");
    assertThat(container.getAnalyses().size(), is(0));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Analysis analysis does not exist for container container"));
    container.deleteAnalysis("analysis");
  }

  @Test
  public void testToString() {
    Container c = new Container("foo", null, null);
    assertThat(c.toString(), equalTo("Container{name=foo, testrigs=[], analyses={}}"));
    c.addAnalysis("analysis", new Analysis("analysis", Maps.newHashMap()));
    assertThat(
        c.toString(),
        equalTo(
            "Container{name=foo, testrigs=[],"
                + " analyses={analysis=Analysis{name=analysis, questions={}}}}"));
  }

  @Test
  public void testEquals() {
    Container c = new Container("foo", Lists.newArrayList(), Maps.newHashMap());
    Container cCopy = new Container("foo", Lists.newArrayList(), Maps.newHashMap());
    Container cWithTestrig = new Container("foo", Lists.newArrayList("testrig"), Maps.newHashMap());
    Container cWithAnalyses =
        new Container(
            "foo",
            Lists.newArrayList(),
            Collections.singletonMap("analysis", new Analysis("analysis", null)));
    Container cOtherName = new Container("bar", Lists.newArrayList(), Maps.newHashMap());

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithTestrig)
        .addEqualityGroup(cWithAnalyses)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
