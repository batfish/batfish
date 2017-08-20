package org.batfish.datamodel.pojo;

import java.util.Map;

/**
 * Analysis Object
 */
public class Analysis {

   /*Set<Question> questions
   *
   *
   * */
   String _name;
   Map<String, String> questions;



  public String get_name() {
    return _name;
  }

  public Analysis(String _name, Map<String, String> questions) {
    this._name = _name;
    this.questions = questions;
  }

  public void set_name(String _name) {
    this._name = _name;
  }

  public Map<String, String> getQuestions() {
    return questions;
  }

  public void setQuestions(Map<String, String> questions) {
    this.questions = questions;
  }
}
