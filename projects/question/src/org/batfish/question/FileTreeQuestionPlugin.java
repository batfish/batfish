package org.batfish.question;

import java.util.Iterator;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.Directory;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileTreeQuestionPlugin extends QuestionPlugin {

   public static class FileTreeAnswerElement implements AnswerElement {

      Directory _testRigRoot;

      public Directory getTestRigRoot() {
         return _testRigRoot;
      }

      public void setTestRigRoot(Directory testRigRoot) {
         _testRigRoot = testRigRoot;
      }

   }

   public static class FileTreeAnswerer extends Answerer {

      public FileTreeAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public FileTreeAnswerElement answer() {
         // FileTreeQuestion question = (FileTreeQuestion) _question;
         Directory root = _batfish.getTestrigFileTree();
         FileTreeAnswerElement ae = new FileTreeAnswerElement();
         ae.setTestRigRoot(root);
         return ae;
      }
   }

   // <question_page_comment>
   /**
    * Outputs results of test-rig initialization.
    *
    * @type InitInfo onefile
    *
    * @example bf_answer("initinfo", summary=True") Get summary information
    *          about test-rig initialization
    */
   public static class FileTreeQuestion extends Question {

      private static final String SUMMARY_VAR = "summary";

      private boolean _summary;

      public FileTreeQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "filetree";
      }

      @JsonProperty(SUMMARY_VAR)
      public boolean getSummary() {
         return _summary;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         return getName() + " " + SUMMARY_VAR + "=" + _summary;
      }

      @Override
      public void setJsonParameters(JSONObject parameters) {
         super.setJsonParameters(parameters);
         Iterator<?> paramKeys = parameters.keys();
         while (paramKeys.hasNext()) {
            String paramKey = (String) paramKeys.next();
            if (isBaseParamKey(paramKey)) {
               continue;
            }
            try {
               switch (paramKey) {
               case SUMMARY_VAR:
                  setSummary(parameters.getBoolean(paramKey));
                  break;
               default:
                  throw new BatfishException("Unknown key in "
                        + getClass().getSimpleName() + ": " + paramKey);
               }
            }
            catch (JSONException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      @JsonProperty(SUMMARY_VAR)
      public void setSummary(boolean summary) {
         _summary = summary;
      }

   }

   @Override
   protected FileTreeAnswerer createAnswerer(Question question,
         IBatfish batfish) {
      return new FileTreeAnswerer(question, batfish);
   }

   @Override
   protected FileTreeQuestion createQuestion() {
      return new FileTreeQuestion();
   }

}
