package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Date;
import org.batfish.common.util.CommonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Container}. */
@RunWith(JUnit4.class)
public class ContainerTest {
  @Test
  public void testToString() {
    String now = CommonUtil.formatDate(new Date());
    Container c = new Container("foo", now, 0, 0);
    assertThat(
        c.toString(),
        equalTo(
            String.format(
                "Container{name=foo, createdAt=%s, testrigsCount=0, analysesCount=0}", now)));
  }

  @Test
  public void testEquals() {
    String now = CommonUtil.formatDate(new Date());
    Container c = new Container("foo", now, 0, 0);
    Container cCopy = new Container("foo", now, 0, 0);
    Container cWithDiffDate = new Container("foo", CommonUtil.formatDate(new Date(0)), 0, 0);
    Container cWithTestrigCount = new Container("foo", now, 1, 0);
    Container cWithAnalysesCount = new Container("foo", now, 0, 1);
    Container cOtherName = new Container("bar", now, 0, 0);

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithDiffDate)
        .addEqualityGroup(cWithTestrigCount)
        .addEqualityGroup(cWithAnalysesCount)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
