package org.vess.edu.beans;

public class Patent {
	   private long patent_id;
	   private String patent_title;
	   private String patent_date;
	   private String patent_web_id;
	   private String patent_class_lvl1;
	   private String patent_class_lvl2;
	   private String patent_org;
	   
	 
	   public Patent() {
	        
	   }
	    
	   public long getPatentId() {
	       return patent_id;
	   }
	 
	   public void setPatentId(long pid) {
	       this.patent_id = pid;
	   }
	 
		   
	   public String getPatentTitle() {
	       return patent_title;
	   }
	 
	   public void setPatentTitle(String pt) {
	       this.patent_title = pt;
	   }
	   
	   public String getPatentDate() {
	       return patent_date;
	   }
	 
	   public void setPatentDate(String dt) {
	       this.patent_date = dt;
	   }
	  
	   public String getPatentWebId(){
		   return patent_web_id;
	   }
	   
	   public void setPatentWebId(String wd){
		   this.patent_web_id=wd;
	   }
	   public String getPatentClassLvl1() {
	       return patent_class_lvl1;
	   }
	 
	   public void setPatentClassLvl1(String c1) {
	       this.patent_class_lvl1 = c1;
	   }
	 
	   public String getPatentClassLvl2() {
	       return patent_class_lvl2;
	   }
	 
	   public void setPatentClassLvl2(String c2) {
	       this.patent_class_lvl2 = c2;
	   }
	   
	   public String getPatentOrg() {
	       return patent_org;
	   }
	 
	   public void setPatentOrg(String c2) {
	       this.patent_org = c2;
	   }
	   
}
