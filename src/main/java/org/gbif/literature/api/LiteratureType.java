/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
