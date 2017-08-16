namespace java org.batfish.storage


typedef i32 int


//Question Type
enum  Type {
  BOOLEAN,
  COMPARATOR,
  DOUBLE,
  FLOAT,
  INTEGER,
  IP,
  IP_PROTOCOL,
  IP_WILDCARD,
  JAVA_REGEX,
  JSON_PATH,
  JSON_PATH_REGEX,
  LONG,
  PREFIX,
  PREFIX_RANGE,
  PROTOCOL,
  QUESTION,
  STRING,
  SUBRANGE
}

struct Container {
  1: string _name,
  2: list<Analysis> _analyses,
  3: list<Testrig> _testrigs
}

struct Testrig {
  1: string _name,
  2: map<string, Question> _questions
}

struct Analysis {
  1: string _name,
  2: map<string, Question> _questions
}

struct Question {
  1: set<string> _allowedValues;
  2: string _description,
  3: int _minElements,
  4: int _minLenght,
  5: bool _optional,
  6: Type _type,
  7: string _value //This needs to be Json in the actual class
}

exception BatfishException {
  1: string _message
}
