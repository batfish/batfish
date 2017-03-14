package org.batfish.question;

import java.util.Iterator;
import java.util.SortedMap;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONObject;

public class FileMapQuestionPlugin extends QuestionPlugin {

   public static class FileMapAnswerElement implements AnswerElement {

      SortedMap<String, String> _fileMap;

      public SortedMap<String, String> getFileMap() {
         return _fileMap;
      }

      public void setFileMap(SortedMap<String, String> fileMap) {
         _fileMap = fileMap;
      }

   }

   public static class FileMapAnswerer extends Answerer {

      public FileMapAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public FileMapAnswerElement answer() {
         ParseVendorConfigurationAnswerElement pvcae = _batfish
               .loadParseVendorConfigurationAnswerElement();
         FileMapAnswerElement ae = new FileMapAnswerElement();
         ae.setFileMap(pvcae.getFileMap());
         return ae;
      }
   }

   // <question_page_comment>
   /**
    * Outputs mapping of hostnames to filenames
    *
    * @type FileMap multifile
    *
    * @example bf_answer("filemap")
    *
    */
   public static class FileMapQuestion extends Question {

      public FileMapQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "filemap";
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         return getName();
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
            switch (paramKey) {
            default:
               throw new BatfishException("Unknown key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
      }

   }

   @Override
   protected FileMapAnswerer createAnswerer(Question question,
         IBatfish batfish) {
      return new FileMapAnswerer(question, batfish);
   }

   @Override
   protected FileMapQuestion createQuestion() {
      return new FileMapQuestion();
   }

}
