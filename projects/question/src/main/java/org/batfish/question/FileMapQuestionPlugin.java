package org.batfish.question;

import com.google.auto.service.AutoService;
import java.util.SortedMap;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class FileMapQuestionPlugin extends QuestionPlugin {

  public static class FileMapAnswerElement extends AnswerElement {

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
      ParseVendorConfigurationAnswerElement pvcae =
          _batfish.loadParseVendorConfigurationAnswerElement();
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
   * @example bf_answer("filemap")
   */
  public static class FileMapQuestion extends Question {

    public FileMapQuestion() {}

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "filemap";
    }

    @Override
    public String prettyPrint() {
      return getName();
    }
  }

  @Override
  protected FileMapAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new FileMapAnswerer(question, batfish);
  }

  @Override
  protected FileMapQuestion createQuestion() {
    return new FileMapQuestion();
  }
}
