package org.vess.edu.beans;

public class Paper {
	   private long paper_id;
	   private String paper_author_lvl1;
	   private String paper_author_lvl2;
	   private String paper_author_lvl3;
	   private String paper_title;
	   private String paper_date;
	   private String paper_class_lvl1;
	   private String paper_class_lvl2;
	   private String paper_source;
	   private String paper_record;
	   private String paper_funds;
	   private String paper_web_id;
	   private int paper_references;
	   
	   
	 
	   public Paper() {
	        
	   }
	    
	   public long getPaperId() {
	       return paper_id;
	   }
	 
	   public void setPaperId(long pid) {
	       this.paper_id = pid;
	   }
	 
	   public String getPaperAuthorLvl1() {
	       return paper_author_lvl1;
	   }
	 
	   public void setPaperAuthorLvl1(String a1) {
	       this.paper_author_lvl1 = a1;
	   }
	   
	   public String getPaperAuthorLvl2() {
	       return paper_author_lvl2;
	   }
	 
	   public void setPaperAuthorLvl2(String a2) {
	       this.paper_author_lvl2 = a2;
	   }
	   
	   public String getPaperAuthorLvl3() {
	       return paper_author_lvl3;
	   }
	 
	   public void setPaperAuthorLvl3(String a3) {
	       this.paper_author_lvl3 = a3;
	   }
	   
	   
	   public String getPaperTitle() {
	       return paper_title;
	   }
	 
	   public void setPaperTitle(String pt) {
	       this.paper_title = pt;
	   }
	   
	   public String getPaperDate() {
	       return paper_date;
	   }
	 
	   public void setPaperDate(String dt) {
	       this.paper_date = dt;
	   }
	   
	   public String getPaperClassLvl1() {
	       return paper_class_lvl1;
	   }
	 
	   public void setPaperClassLvl1(String c1) {
	       this.paper_class_lvl1 = c1;
	   }
	 
	   public String getPaperClassLvl2() {
	       return paper_class_lvl2;
	   }
	 
	   public void setPaperClassLvl2(String c2) {
	       this.paper_class_lvl2 = c2;
	   }
	   
	   public String getPaperSource() {
	       return paper_source;
	   }
	   
	   public void setPaperSource(String sc) {
	       this.paper_source = sc;
	   }
	   
	   public String getPaperRecord() {
	       return paper_record;
	   }
	   
	   public void setPaperRecord(String rc) {
	       this.paper_record = rc;
	   }

	   public String getPaperFunds(){
		   return paper_funds;
	   }
	   
	   public void setPaperFunds(String fd){
		   this.paper_funds=fd;
	   }
	   
	   public String getPaperWebId(){
		   return paper_web_id;
	   }
	   
	   public void setPaperWebId(String wd){
		   this.paper_web_id=wd;
	   }
	   
	   public int getPaperReferences(){
		   return paper_references;
	   }
	   
	   public void setPaperReferences(int rf){
		   this.paper_references=rf;
	   }
}
