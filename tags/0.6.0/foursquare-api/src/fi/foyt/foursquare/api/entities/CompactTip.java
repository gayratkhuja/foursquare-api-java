/*
 * FoursquareAPI - Foursquare API for Java
 * Copyright (C) 2008 - 2011 Antti Leppä / Foyt
 * http://www.foyt.fi
 * 
 * License: 
 * 
 * Licensed under GNU Lesser General Public License Version 3 or later (the "LGPL")
 * http://www.gnu.org/licenses/lgpl.html
 */

package fi.foyt.foursquare.api.entities;

import fi.foyt.foursquare.api.FoursquareEntity;

public class CompactTip implements FoursquareEntity {

  private static final long serialVersionUID = 5912726139848171570L;
  
  public String getId() {
    return id;
  }
  
  public String getText() {
    return text;
  }
  
  public Long getCreatedAt() {
    return createdAt;
  }
  
  public String getStatus() {
    return status;
  }
  
  public Photo getPhoto() {
    return photo;
  }
  
  public String getPhotoURL() {
    return photourl;
  }
  
  public CompactUser getUser() {
    return user;
  }
  
  public CompactVenue getVenue() {
    return venue;
  }
  
  public String getUrl() {
    return url;
  }
  
  private String id;
  private String text;
  private Long createdAt;
  private String status;
  private Photo photo;
  private String photourl;
  private String url;
  private CompactUser user;
  private CompactVenue venue;
}