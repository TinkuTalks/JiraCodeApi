package com.ab.ApiCodeJiraRest;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;


public class App 
{
	@Test
    public void addComment() {
    	
    	RestAssured.baseURI = "http://localhost:9090";
    	SessionFilter session = new SessionFilter();
    	
    String response=	given().relaxedHTTPSValidation().log().all().header("Content-Type","application/json")
    	.body("{ \"username\": \"anshuul2050\", \"password\": \"myaimspace\" }\r\n"
    			+ "").filter(session).when().post("/rest/auth/1/session").then().log().all().extract()
    	.response().asString();
    
    System.out.println("Passing login below");
    	String expectedmsg ="Hello! this is a new comment";
    	//Add comment
    String addCommntResp = 	given().pathParam("id", "10101").log().all().header("Content-Type","application/json")
    	.body("{\r\n"
    			+ "    \"body\": \""+expectedmsg+"\",\r\n"
    			+ "    \"visibility\": {\r\n"
    			+ "        \"type\": \"role\",\r\n"
    			+ "        \"value\": \"Administrators\"\r\n"
    			+ "    }\r\n"
    			+ "}").filter(session).when()
    	.post("/rest/api/2/issue/{id}/comment").then().log().all().assertThat().statusCode(201).extract().response()
    	.asString();
    
        JsonPath jscmntid= new JsonPath(addCommntResp);
        String commentid = jscmntid.getString("id");
    	
    	//Add Attachment
    	given().header("X-Atlassian-Token", "no-check").log().all().filter(session).pathParam("id", "10101")
    	.header("Content-Type","multipart/form-data")
    	.multiPart("file",new File("jira.txt")).log().all()
    	.when().post("/rest/api/2/issue/{id}/attachments").then().log().all().assertThat().statusCode(200);
    	
    	//get Issue details
    	String details =given().filter(session).pathParam("id", "10101").queryParam("fields", "comment").when().get("/rest/api/2/issue/{id}")
    	.then().log().all().assertThat().statusCode(200).extract().response().asString();
    	
    	System.out.println(details);
    	
    	JsonPath getjscmntid= new JsonPath(details);
       int getcommentCount = getjscmntid.getInt("fields.comment.comments.size()");
        System.out.println(getcommentCount);
       for(int i = 0; i<getcommentCount;i++) {
    	   
    	   String getCmtId=getjscmntid.get("fields.comment.comments["+i+"].id").toString(); 
    	  System.out.println(getjscmntid.getInt("fields.comment.comments["+i+"].id")); 
    	  
    	  if(getCmtId.equals(commentid)) {
    		  
    		  System.out.println("Matched comment is :"+getCmtId);
    		  
    		  System.out.println(getjscmntid.get("fields.comment.comments["+i+"].body").toString());
    		  String actualmsg=getjscmntid.get("fields.comment.comments["+i+"].body").toString();
    		  
    		  Assert.assertEquals(expectedmsg, actualmsg);
    	  }
    	   
       }
        
    	
    	
    	
    }
}
