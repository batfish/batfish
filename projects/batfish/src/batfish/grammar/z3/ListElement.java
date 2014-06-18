package batfish.grammar.z3;

import java.util.ArrayList;
import java.util.List;

public abstract class ListElement implements Element {

   protected List<Element> _bodyElements;
   protected Element _headElement;

   public ListElement() {
      _bodyElements = new ArrayList<Element>();
   }

   public void addBodyElement(Element element) {
      _bodyElements.add(element);
   }

   public void setHeadElement(Element headElement) {
      _headElement = headElement;
   }

}
