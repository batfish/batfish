package org.batfish.datamodel.answers;

public class StringAnswer implements AnswerElement{

   private String _answer;
   
   public StringAnswer() {
      
   }
   
   public StringAnswer(String answer) {
      this();
      setAnswer(answer);
   }
   
   public String getAnswer() {
      return _answer;
   }

   public void setAnswer(String answer) {
      this._answer = answer;
   }
}
