package org.batfish.question.initialization;

import static org.batfish.question.initialization.ConversionWarningAnswerer.COL_COMMENT;
import static org.batfish.question.initialization.ConversionWarningAnswerer.COL_NODE;
import static org.batfish.question.initialization.ConversionWarningAnswerer.COL_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.junit.Test;

/** Tests of {@link ConversionWarningAnswerer}. */
public class ConversionWarningAnswererTest {

  @Test
  public void testAnswer() {
    TestBatfish batfish = new TestBatfish();
    ConversionWarningAnswerer answerer =
        new ConversionWarningAnswerer(new ConversionWarningQuestion(), batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODE,
                        new Node("node1"),
                        COL_TYPE,
                        ImmutableList.of(Warnings.TAG_UNIMPLEMENTED),
                        COL_COMMENT,
                        "unimplemented1"))
                .add(
                    Row.of(
                        COL_NODE,
                        new Node("node2"),
                        COL_TYPE,
                        ImmutableList.of(Warnings.TAG_RED_FLAG),
                        COL_COMMENT,
                        "redflag2"))));
  }

  private static class TestBatfish extends IBatfishTestAdapter {
    @Override
    public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse(
        NetworkSnapshot snapshot) {
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      Warnings warnings1 = new Warnings(true, true, true);
      warnings1.unimplemented("unimplemented1");
      Warnings warnings2 = new Warnings(true, true, true);
      warnings2.redFlag("redflag2");
      ccae.setWarnings(ImmutableSortedMap.of("node1", warnings1, "node2", warnings2));
      return ccae;
    }
  }
}
