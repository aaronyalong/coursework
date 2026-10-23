package crux;

final class Authors {
  // TODO: Add author information.
  static final Author[] all = {new Author("Aaron Yalong", "16865957", "ayalong"), new Author("Sharbel Ghostine", "76251985", "ghostins")};
}


final class Author {
  final String name;
  final String studentId;
  final String uciNetId;

  Author(String name, String studentId, String uciNetId) {
    this.name = name;
    this.studentId = studentId;
    this.uciNetId = uciNetId;
  }
}
