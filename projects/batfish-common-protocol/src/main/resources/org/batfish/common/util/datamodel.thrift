# The representation of datamodel for now
struct Container{
    1: string _name,
    2: set<string> _testrigs
}

struct Analysis{
    1: string _name,
    2: map<string,string> _questions
}