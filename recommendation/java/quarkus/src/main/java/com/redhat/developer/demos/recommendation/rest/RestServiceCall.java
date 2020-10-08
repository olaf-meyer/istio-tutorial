package com.redhat.developer.demos.recommendation.rest;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RestServiceCall {
  
  public enum Services {
    CUSTOMER, PREFERENCE, RECOMMENDATION
  }

  private Services origin;
  private Services destination;
  private String originVersion;
  private String comment;

  public RestServiceCall(final Services origin, final Services destination, final String originVersion, final String comment) {
    this.origin=origin;
    this.destination=destination;
    this.originVersion=originVersion;
    this.comment=comment;
  }

  public Services getOrigin(){
    return this.origin;
  }

  public Services getDestination(){
    return this.destination;
  }

  public String getOriginVersion(){
    return this.originVersion;
  }

  public String getComment(){
    return this.comment;
  }

  @Override
  public String toString() {
      return "RestServiceCall [origin=" + origin + ", destination=" + destination + ", originVersion=" + originVersion + ", comment=" + comment +"]";
  }

  public void setOrigin( Services origin){
    this.origin=origin;
  }

  public void setDestination(Services destination){
    this.destination=destination;
  }

  public void setOriginVersion(String originVersion){
    this.originVersion=originVersion;
  }

  public void setComment(String comment){
    this.comment=comment;
  }
  
}
