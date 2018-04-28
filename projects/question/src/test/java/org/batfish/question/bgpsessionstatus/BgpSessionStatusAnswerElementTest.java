package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;
import org.junit.Test;

public class BgpSessionStatusAnswerElementTest {

  @Test
  public void toAndFromRow() throws JsonProcessingException {
    BgpSessionInfo session =
        new BgpSessionInfo(
            "nodeName",
            "vrfName",
            new Prefix(new Ip("1.1.1.1"), 32),
            new Ip("2.2.2.2"),
            null,
            "remoteNode",
            SessionStatus.UNKNOWN_REMOTE,
            -1,
            SessionType.EBGP_SINGLEHOP);

    ObjectNode row = BgpSessionStatusAnswerElement.toRowStatic(session);
    BgpSessionInfo session2 = BgpSessionStatusAnswerElement.fromRowStatic(row);

    // session2 and session should be identical
    assertThat(session, equalTo(session2));
  }
}
