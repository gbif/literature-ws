package org.gbif.literature.api;

import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LiteratureSearchResult {

  private String abstr; // TODO: 16/10/2020 "abstract"
  private String accessed;
  private boolean authored;
  private List<String> authors;
  private boolean confirmed;
  private String contentType;
  private List<Country> countriesOfCoverage;
  private List<Country> countriesOfResearcher;
  private Country country;
  private Date created;
  private Date createdAt;
  private Integer day;
  private boolean fileAttached;
  private List<String> gbifDownloadKey;
  private List<String> gbifRegion;
  private UUID groupId;
  private Map<String, String> identifiers;
  private List<String> keywords;
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
  private List<Relevance> relevance;
  private boolean searchable;
  private String source;
  private boolean starred;
  private List<String> tags;
  private String title;
  private List<Topic> topics;
  private Date updatedAt;
  private String userContext;
  private List<String> websites;
  private Integer year;

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

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
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

  public List<Country> getCountriesOfCoverage() {
    return countriesOfCoverage;
  }

  public void setCountriesOfCoverage(List<Country> countriesOfCoverage) {
    this.countriesOfCoverage = countriesOfCoverage;
  }

  public List<Country> getCountriesOfResearcher() {
    return countriesOfResearcher;
  }

  public void setCountriesOfResearcher(List<Country> countriesOfResearcher) {
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

  public List<String> getGbifRegion() {
    return gbifRegion;
  }

  public void setGbifRegion(List<String> gbifRegion) {
    this.gbifRegion = gbifRegion;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public void setGroupId(UUID groupId) {
    this.groupId = groupId;
  }

  public Map<String, String> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Map<String, String> identifiers) {
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

  public List<Relevance> getRelevance() {
    return relevance;
  }

  public void setRelevance(List<Relevance> relevance) {
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

  public List<Topic> getTopics() {
    return topics;
  }

  public void setTopics(List<Topic> topics) {
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
