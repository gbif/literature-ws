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

import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.GbifRegion;
import org.gbif.api.vocabulary.Language;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class LiteratureSearchResult {

  private String abstr;
  private String accessed;
  private boolean authored;
  private List<Map<String, Object>> authors = new ArrayList<>();
  private boolean confirmed;
  private String contentType;
  private Set<Country> countriesOfCoverage = new HashSet<>();
  private Set<Country> countriesOfResearcher = new HashSet<>();
  private Country country;
  private Date created;
  private Date createdAt;
  private Integer day;
  private boolean fileAttached;
  private List<String> gbifDownloadKey = new ArrayList<>();
  private Set<GbifRegion> gbifRegion = new HashSet<>();
  private UUID groupId;
  private Boolean hidden;
  private UUID id;
  private Map<String, Object> identifiers = new HashMap<>();
  private List<String> keywords = new ArrayList<>();
  private Language language;
  private LiteratureType literatureType;
  private Integer month;
  private String notes;
  private boolean openAccess;
  private boolean peerReview;
  private boolean privatePublication;
  private UUID profileId;
  private String publisher;
  private boolean read;
  private Set<Relevance> relevance = new HashSet<>();
  private boolean searchable;
  private String source;
  private boolean starred;
  private List<String> tags = new ArrayList<>();
  private String title;
  private Set<Topic> topics = new HashSet<>();
  private Date updatedAt;
  private String userContext;
  private List<String> websites = new ArrayList<>();
  private Integer year;

  @JsonProperty("abstract")
  public String getAbstr() {
    return abstr;
  }

  public void setAbstr(String abstr) {
    this.abstr = abstr;
  }

  public String getAccessed() {
    return accessed;
  }

  public void setAccessed(String accessed) {
    this.accessed = accessed;
  }

  public boolean isAuthored() {
    return authored;
  }

  public void setAuthored(boolean authored) {
    this.authored = authored;
  }

  public List<Map<String, Object>> getAuthors() {
    return authors;
  }

  public void setAuthors(List<Map<String, Object>> authors) {
    this.authors = authors;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public void setConfirmed(boolean confirmed) {
    this.confirmed = confirmed;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Set<Country> getCountriesOfCoverage() {
    return countriesOfCoverage;
  }

  public void setCountriesOfCoverage(Set<Country> countriesOfCoverage) {
    this.countriesOfCoverage = countriesOfCoverage;
  }

  public Set<Country> getCountriesOfResearcher() {
    return countriesOfResearcher;
  }

  public void setCountriesOfResearcher(Set<Country> countriesOfResearcher) {
    this.countriesOfResearcher = countriesOfResearcher;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Integer getDay() {
    return day;
  }

  public void setDay(Integer day) {
    this.day = day;
  }

  public boolean isFileAttached() {
    return fileAttached;
  }

  public void setFileAttached(boolean fileAttached) {
    this.fileAttached = fileAttached;
  }

  public List<String> getGbifDownloadKey() {
    return gbifDownloadKey;
  }

  public void setGbifDownloadKey(List<String> gbifDownloadKey) {
    this.gbifDownloadKey = gbifDownloadKey;
  }

  public Set<GbifRegion> getGbifRegion() {
    return gbifRegion;
  }

  public void setGbifRegion(Set<GbifRegion> gbifRegion) {
    this.gbifRegion = gbifRegion;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public void setGroupId(UUID groupId) {
    this.groupId = groupId;
  }

  public Boolean getHidden() {
    return hidden;
  }

  public void setHidden(Boolean hidden) {
    this.hidden = hidden;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Map<String, Object> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Map<String, Object> identifiers) {
    this.identifiers = identifiers;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public LiteratureType getLiteratureType() {
    return literatureType;
  }

  public void setLiteratureType(LiteratureType literatureType) {
    this.literatureType = literatureType;
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isOpenAccess() {
    return openAccess;
  }

  public void setOpenAccess(boolean openAccess) {
    this.openAccess = openAccess;
  }

  public boolean isPeerReview() {
    return peerReview;
  }

  public void setPeerReview(boolean peerReview) {
    this.peerReview = peerReview;
  }

  public boolean isPrivatePublication() {
    return privatePublication;
  }

  public void setPrivatePublication(boolean privatePublication) {
    this.privatePublication = privatePublication;
  }

  public UUID getProfileId() {
    return profileId;
  }

  public void setProfileId(UUID profileId) {
    this.profileId = profileId;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public Set<Relevance> getRelevance() {
    return relevance;
  }

  public void setRelevance(Set<Relevance> relevance) {
    this.relevance = relevance;
  }

  public boolean isSearchable() {
    return searchable;
  }

  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public boolean isStarred() {
    return starred;
  }

  public void setStarred(boolean starred) {
    this.starred = starred;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Set<Topic> getTopics() {
    return topics;
  }

  public void setTopics(Set<Topic> topics) {
    this.topics = topics;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUserContext() {
    return userContext;
  }

  public void setUserContext(String userContext) {
    this.userContext = userContext;
  }

  public List<String> getWebsites() {
    return websites;
  }

  public void setWebsites(List<String> websites) {
    this.websites = websites;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }
}
