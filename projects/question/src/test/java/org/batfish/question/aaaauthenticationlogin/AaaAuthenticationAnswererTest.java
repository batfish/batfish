package org.batfish.question.aaaauthenticationlogin;

import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginAnswerer.COLUMN_LINE_NAMES;
import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginAnswerer.COLUMN_NODE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.junit.Test;

public class AaaAuthenticationAnswererTest {

  @Test
  public void testGetRow() {
    Line lineRequiresAuthentication = new Line("con0");
    lineRequiresAuthentication.setAaaAuthenticationLoginList(
        new AaaAuthenticationLoginList(
            Collections.singletonList(AuthenticationMethod.GROUP_TACACS)));

    Line lineNoAuthentication = new Line("aux0");
    lineNoAuthentication.setAaaAuthenticationLoginList(
        new AaaAuthenticationLoginList(Collections.singletonList(AuthenticationMethod.NONE)));

    assertThat(
        AaaAuthenticationLoginAnswerer.getRow(
            "requiresAuthentication", Collections.singletonList(lineRequiresAuthentication)),
        nullValue());
    assertThat(
        AaaAuthenticationLoginAnswerer.getRow(
            "noAuthentication", Arrays.asList(lineRequiresAuthentication, lineNoAuthentication)),
        equalTo(
            Row.of(
                COLUMN_NODE,
                new Node("noAuthentication"),
                COLUMN_LINE_NAMES,
                Collections.singletonList("aux0"))));
  }

  @Test
  public void testTableAnswerElementColumns() {
    AaaAuthenticationLoginQuestion question = new AaaAuthenticationLoginQuestion();
    TableAnswerElement tableAnswerElement = AaaAuthenticationLoginAnswerer.create(question);

    Set<String> columnNames =
        tableAnswerElement.getMetadata().getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(columnNames, equalTo(ImmutableSet.of(COLUMN_NODE, COLUMN_LINE_NAMES)));
  }
}
