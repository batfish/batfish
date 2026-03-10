package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement.Fragment;
import org.batfish.datamodel.TraceElement.LinkFragment;
import org.batfish.datamodel.TraceElement.TextFragment;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

/** Test for {@link TraceElement}. */
public final class TraceElementTest {
  @Test
  public void testTextFragment_equals() {
    new EqualsTester()
        .addEqualityGroup(new TextFragment("a"), new TextFragment("a"))
        .addEqualityGroup(new TextFragment("x"))
        .testEquals();
  }

  @Test
  public void testTextFragment_JsonSerialization() {
    TextFragment fragment = new TextFragment("asdf");
    TextFragment clone = (TextFragment) BatfishObjectMapper.clone(fragment, Fragment.class);
    assertEquals(fragment, clone);
  }

  @Test
  public void testLinkFragment_equals() {
    String text1 = "a";
    String text2 = "b";
    VendorStructureId vendorStructureId1 = new VendorStructureId("f1", "t1", "n1");
    VendorStructureId vendorStructureId2 = new VendorStructureId("f2", "t2", "n2");
    new EqualsTester()
        .addEqualityGroup(
            new LinkFragment(text1, vendorStructureId1),
            new LinkFragment(text1, vendorStructureId1))
        .addEqualityGroup(new LinkFragment(text2, vendorStructureId1))
        .addEqualityGroup(new LinkFragment(text1, vendorStructureId2))
        .testEquals();
  }

  @Test
  public void testLinkFragment_JsonSerialization() {
    LinkFragment fragment = new LinkFragment("asdf", new VendorStructureId("f", "t", "n"));
    LinkFragment clone = (LinkFragment) BatfishObjectMapper.clone(fragment, Fragment.class);
    assertEquals(fragment, clone);
  }

  @Test
  public void testTraceElement_equals() {
    List<Fragment> fragments1 = ImmutableList.of(new TextFragment("a"));
    List<Fragment> fragments2 = ImmutableList.of(new TextFragment("b"));
    new EqualsTester()
        .addEqualityGroup(new TraceElement(fragments1), new TraceElement(fragments1))
        .addEqualityGroup(new TraceElement(fragments2))
        .testEquals();
  }

  @Test
  public void testTraceElement_JsonSerialization() {
    TraceElement traceElement = new TraceElement(ImmutableList.of(new TextFragment("a")));
    TraceElement clone = BatfishObjectMapper.clone(traceElement, TraceElement.class);
    assertEquals(traceElement, clone);
  }

  @Test
  public void testGetText() {
    VendorStructureId vendorStructureId = new VendorStructureId("f", "t", "n");
    String actual =
        TraceElement.builder().add("text ").add("link", vendorStructureId).build().getText();
    assertEquals("text link", actual);
  }
}
