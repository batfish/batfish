package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.DefinedStructureInfo;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link ConvertConfigurationAnswerElement} */
public class ConvertConfigurationAnswerElementTest {

  ConvertConfigurationAnswerElement _element;

  @Test
  public void checkEmptyErrors() {
    assertThat(_element.getErrors().size(), is(0));
  }

  @Test
  public void checkNonEmptyErrors() {
    BatfishException exception = new BatfishException("sample exception");
    _element.getErrors().put("error", new BatfishStackTrace(exception));
    assertThat(_element.getErrors().size(), is(1));
  }

  @Before
  public void setUp() {
    _element = new ConvertConfigurationAnswerElement();
  }

  /**
   * Defined and referenced structures take a hand-written serialized form; they must round-trip.
   */
  @Test
  public void testJavaSerializationOfStructures() throws Exception {
    ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
    SortedMap<String, SortedMap<String, DefinedStructureInfo>> definedInF1 = new TreeMap<>();
    definedInF1.put(
        "interface",
        new TreeMap<>(
            ImmutableMap.of(
                "eth0",
                    new DefinedStructureInfo(
                        TreeRangeSet.create(ImmutableList.of(Range.closed(1, 3))), 2),
                "eth1",
                    new DefinedStructureInfo(
                        TreeRangeSet.create(ImmutableList.of(Range.singleton(7))), 0))));
    ccae.getDefinedStructures().put("f1", definedInF1);
    ccae.getDefinedStructures().put("f2", new TreeMap<>());
    SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> referencedInF1 =
        new TreeMap<>();
    referencedInF1.put(
        "interface",
        new TreeMap<>(
            ImmutableMap.of(
                "eth0",
                new TreeMap<>(
                    ImmutableMap.of(
                        "static route", new TreeSet<>(ImmutableList.of(10, 11, 12)))))));
    ccae.getReferencedStructures().put("f1", referencedInF1);
    ccae.getWarnings().put("f1", new Warnings());

    ConvertConfigurationAnswerElement clone = SerializationUtils.clone(ccae);

    // DefinedStructureInfo has no equals; compare its JSON form.
    assertThat(
        BatfishObjectMapper.writeString(clone.getDefinedStructures()),
        equalTo(BatfishObjectMapper.writeString(ccae.getDefinedStructures())));
    assertThat(clone.getReferencedStructures(), equalTo(ccae.getReferencedStructures()));
    assertThat(clone.getWarnings().keySet(), equalTo(ccae.getWarnings().keySet()));
    // Empty maps round-trip too.
    ConvertConfigurationAnswerElement emptyClone =
        SerializationUtils.clone(new ConvertConfigurationAnswerElement());
    assertThat(emptyClone.getDefinedStructures(), anEmptyMap());
    assertThat(emptyClone.getReferencedStructures(), anEmptyMap());
  }

  @Test
  public void testConvertStatus() {
    assertThat(_element.getConvertStatusProp(), anEmptyMap());
    _element.getConvertStatus().put("node", ConvertStatus.PASSED);
    assertThat(_element.getConvertStatusProp(), hasEntry("node", ConvertStatus.PASSED));
  }

  @Test
  public void testConvertStatusFromFailed() {
    Set<String> set = new TreeSet<>();
    _element.setFailed(set);
    _element.setConvertStatus(null);

    assertThat(_element.getConvertStatusProp(), anEmptyMap());

    // Confirm object containing failed-set, not convert-status-map still returns a correct map
    set.add("node");
    assertThat(_element.getConvertStatusProp(), hasEntry("node", ConvertStatus.FAILED));
  }

  @Test
  public void testGetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    _element.getErrors().put("error", stackTrace);
    assertThat(_element.getErrors().get("error"), is(stackTrace));
  }

  @Test
  public void testSetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    SortedMap<String, BatfishStackTrace> errors = new TreeMap<>();
    errors.put("error", stackTrace);
    _element.setErrors(errors);
    assertThat(_element.getErrors().get("error"), is(stackTrace));
  }
}
