package org.batfish.question.initialization;

import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_FILENAME;
import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_NODES;
import static org.batfish.question.initialization.FileParseStatusAnswerer.COL_PARSE_STATUS;
import static org.batfish.question.initialization.FileParseStatusAnswerer.TABLE_METADATA;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of {@link FileParseStatusAnswerer}. */
public final class FileParseStatusAnswererTest {

  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testAnswerJuniperLogicalSystems() throws IOException {
    String filename = "master_and_ls.cfg";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + "juniper_logical_systems", filename)
                .build(),
            _folder);
    TableAnswerElement answer =
        new FileParseStatusAnswerer(new FileParseStatusQuestion(), batfish).answer();
    String expectedFilename = String.format("configs/%s", filename);

    assertThat(
        answer.getRowsList(),
        contains(
            Row.builder(TABLE_METADATA.toColumnMap())
                .put(COL_FILENAME, expectedFilename)
                .put(COL_PARSE_STATUS, ParseStatus.PASSED)
                .put(
                    COL_NODES,
                    ImmutableList.of(new Node("ls1_hostname"), new Node("master_hostname")))
                .build()));
  }
}
