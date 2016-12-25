package org.vess.edu.beans;

public class Baidu {
	 
	
	    
	   private int baidu_college_id;
	   private String baidu_college_title;
	   private int grad;
	   private int doc;
	   private int post_doc;
	   private int good_subject;
	    
	 
	   public Baidu() {
	        
	   }
	    
	   public int getBaiduId() {
	       return baidu_college_id;
	   }
	 
	   public void setBaiduId(int cid) {
	       this.baidu_college_id = cid;
	   }
	   
	   public String getBaiduCollegeTitle() {
	       return baidu_college_title;
	   }
	 
	   public void setBaiduCollegeTitle(String ct) {
	       this.baidu_college_title = ct;
	   }
	   
	   public int getGrad() {
	       return grad;
	   }
	 
	   public void setGrad(int cg) {
	       this.grad = cg;
	   }
	   
	   public int getDoc() {
	       return doc;
	   }
	 
	   public void setDoc(int cg) {
	       this.doc = cg;
	   }
	   
	   public int getPostDoc() {
	       return post_doc;
	   }
	 
	   public void setPostDoc(int cg) {
	       this.post_doc = cg;
	   }
	   
	   
	   public int getGoodSubject() {
	       return good_subject;
	   }
	 
	   public void setGoodSubject(int cg) {
	       this.good_subject = cg;
	   }
	 
	}
