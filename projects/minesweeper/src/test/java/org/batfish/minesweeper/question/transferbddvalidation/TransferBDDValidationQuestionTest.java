package org.batfish.minesweeper.question.transferbddvalidation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests for {@link TransferBDDValidationQuestion}. */
public class TransferBDDValidationQuestionTest {

  @Test
  public void testDefaultConstructor() {
    TransferBDDValidationQuestion question = new TransferBDDValidationQuestion();
    assertThat(question.getNodes(), equalTo(null));
    assertThat(question.getPolicies(), equalTo(null));
  }

  @Test
  public void testConstructorWithParameters() {
    String nodes = "node.*";
    String policies = "policy.*";
    TransferBDDValidationQuestion question = new TransferBDDValidationQuestion(nodes, policies);
    assertThat(question.getNodes(), equalTo(nodes));
    assertThat(question.getPolicies(), equalTo(policies));
  }

  @Test
  public void testGetName() {
    TransferBDDValidationQuestion question = new TransferBDDValidationQuestion();
    assertThat(question.getName(), equalTo("transferBDDValidation"));
  }

  @Test
  public void testGetDataPlane() {
    TransferBDDValidationQuestion question = new TransferBDDValidationQuestion();
    assertThat(question.getDataPlane(), equalTo(false));
  }
}
