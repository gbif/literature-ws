package org.gbif.literature.api;

public enum LiteratureType {

  JOURNAL("journal"),
  BOOK("book"),
  GENERIC("generic"),
  BOOK_SECTION("book_section"),
  CONFERENCE_PROCEEDINGS("conference_proceedings"),
  WORKING_PAPER("working_paper"),
  REPORT("report"),
  WEB_PAGE("web_page"),
  THESIS("thesis"),
  MAGAZINE_ARTICLE("magazine_article"),
  STATUTE("statute"),
  PATENT("patent"),
  NEWSPAPER_ARTICLE("newspaper_article"),
  COMPUTER_PROGRAM("computer_program"),
  HEARING("hearing"),
  TELEVISION_BROADCAST("television_broadcast"),
  ENCYCLOPEDIA_ARTICLE("encyclopedia_article"),
  CASE("case"),
  FILM("film"),
  BILL("bill");

  private final String type;

  LiteratureType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
