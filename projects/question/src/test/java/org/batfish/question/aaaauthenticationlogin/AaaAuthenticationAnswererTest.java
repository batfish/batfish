package org.batfish.question.aaaauthenticationlogin;

import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer.COLUMN_LINE_NAMES;
import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer.COLUMN_NODE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.batfish.datamodel.vendor_family.cisco.Line;
import org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer;
import org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationQuestion;
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
        AaaAuthenticationAnswerer.getRow(
            "requiresAuthentication", Collections.singletonList(lineRequiresAuthentication)),
        nullValue());
    assertThat(
        AaaAuthenticationAnswerer.getRow(
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
    AaaAuthenticationQuestion question = new AaaAuthenticationQuestion();
    TableAnswerElement tableAnswerElement = AaaAuthenticationAnswerer.create(question);

    Set<String> columnNames =
        tableAnswerElement
            .getMetadata()
            .getColumnMetadata()
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(columnNames, equalTo(ImmutableSet.of(COLUMN_NODE, COLUMN_LINE_NAMES)));
  }
}
