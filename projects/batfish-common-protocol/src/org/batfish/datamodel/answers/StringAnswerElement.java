package org.batfish.datamodel.answers;

public class StringAnswerElement implements AnswerElement {

   private String _answer;

   public StringAnswerElement() {

   }

   public StringAnswerElement(String answer) {
      this();
      setAnswer(answer);
   }

   public String getAnswer() {
      return _answer;
   }

   @Override
   public String prettyPrint() {
      return _answer;
   }

   public void setAnswer(String answer) {
      this._answer = answer;
   }

}
