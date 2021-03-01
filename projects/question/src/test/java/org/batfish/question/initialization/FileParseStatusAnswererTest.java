package org.batfish.question.initialization;

import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_FILENAME;
import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_FILE_FORMAT;
import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_NODES;
import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_PARSE_STATUS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.junit.Test;

/** Tests of {@link FileParseStatusAnswerer}. */
public class FileParseStatusAnswererTest {

  @Test
  public void testGetRowWithoutHost() {
    Row expected =
        Row.of(
            COL_FILENAME,
            "nohost",
            COL_PARSE_STATUS,
            "FAILED",
            COL_FILE_FORMAT,
            "CISCO_IOS",
            COL_NODES,
            ImmutableList.of());

    Row row =
        FileParseStatusAnswerer.getRow(
            "nohost", ParseStatus.FAILED, ConfigurationFormat.CISCO_IOS, null);
    assertThat(row, equalTo(expected));
  }

  @Test
  public void testGetRowWithHost() {
    Row expected =
        Row.of(
            COL_FILENAME,
            "host",
            COL_PARSE_STATUS,
            "EMPTY",
            COL_FILE_FORMAT,
            "EMPTY",
            COL_NODES,
            ImmutableList.of(new Node("h1")));

    Row row =
        FileParseStatusAnswerer.getRow(
            "host", ParseStatus.EMPTY, ConfigurationFormat.EMPTY, ImmutableList.of("h1"));
    assertThat(row, equalTo(expected));
  }

  @Test
  public void testAnswererFlow() {
    TestBatfish batfish = new TestBatfish();
    FileParseStatusAnswerer answerer =
        new FileParseStatusAnswerer(new FileParseStatusQuestion(), batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_FILENAME,
                        "f",
                        COL_PARSE_STATUS,
                        ParseStatus.PASSED,
                        COL_FILE_FORMAT,
                        "UNKNOWN",
                        COL_NODES,
                        ImmutableList.of(new Node("h"), new Node("h1"), new Node("h2"))))));
  }

  private static class TestBatfish extends IBatfishTestAdapter {
    @Override
    public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
        NetworkSnapshot snapshot) {
      ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
      pvcae.setFileMap(ImmutableMultimap.of("h", "f"));
      pvcae.setParseStatus(ImmutableSortedMap.of("f", ParseStatus.PASSED));
      return pvcae;
    }

    @Override
    public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse(
        NetworkSnapshot snapshot) {
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      ccae.getFileMap().put("f", "h1");
      ccae.getFileMap().put("f", "h2");
      return ccae;
    }
  }
}
