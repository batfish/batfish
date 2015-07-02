package org.batfish.question;

public class VerifyQuestion extends Question {

   private Assertion _assertion;

   private InterfaceSelector _interfaceSelector;

   private NodeSelector _nodeSelector;

   public VerifyQuestion() {
      super(QuestionType.VERIFY);
   }

   public Assertion getAssertion() {
      return _assertion;
   }

   public InterfaceSelector getInterfaceSelector() {
      return _interfaceSelector;
   }

   public NodeSelector getNodeSelector() {
      return _nodeSelector;
   }

   public void setAssertion(Assertion assertion) {
      _assertion = assertion;
   }

   public void setInterfaceSelector(InterfaceSelector interfaceSelector) {
      _interfaceSelector = interfaceSelector;
   }

   public void setNodeSelector(NodeSelector nodeSelector) {
      _nodeSelector = nodeSelector;
   }

}
