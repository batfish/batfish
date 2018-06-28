package org.batfish.question.aaaauthenticationlogin;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.Arrays;
import java.util.Collections;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.batfish.datamodel.vendor_family.cisco.Line;
import org.batfish.question.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer;
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
                AaaAuthenticationAnswerer.COLUMN_NODE,
                new Node("noAuthentication"),
                AaaAuthenticationAnswerer.COLUMN_LINE_NAMES,
                Collections.singletonList("aux0"))));
  }
}
