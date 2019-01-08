package org.batfish.question.vxlanproperties;

import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_CONVERT_STATUS;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_NODE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ConvertStatus;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.junit.Test;

public class VxlanVniPropertiesAnswererTest {
  @Test
  public void testAnswer() {
    VxlanVniPropertiesAnswerer answerer =
        new VxlanVniPropertiesAnswerer(
            new VxlanVniPropertiesQuestion(NodesSpecifier.ALL),
            new VxlanVniPropertiesAnswererTest.TestBatfish());
    TableAnswerElement answer = answerer.answer();
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(Row.of(COL_NODE, "n1", COL_CONVERT_STATUS, ConvertStatus.PASSED))
                .add(Row.of(COL_NODE, "n2", COL_CONVERT_STATUS, ConvertStatus.WARNINGS))
                .add(Row.of(COL_NODE, "n3", COL_CONVERT_STATUS, ConvertStatus.FAILED))));
  }

  private static class TestBatfish extends IBatfishTestAdapter {
    @Override
    public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse() {
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      ccae.getConvertStatus().put("n1", ConvertStatus.PASSED);
      ccae.getConvertStatus().put("n2", ConvertStatus.WARNINGS);
      ccae.getConvertStatus().put("n3", ConvertStatus.FAILED);
      return ccae;
    }
  }
}
