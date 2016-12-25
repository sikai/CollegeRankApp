package org.vess.edu.beans;

public class PageAbstract {
	   private String web_id;
	   private String class_lvl1;
	   private String class_lvl2;
	   private String page_org;
	   private int page_ref;

	   
	 
	   public PageAbstract() {
	        
	   }
	    
	   public String getWebId() {
	       return web_id;
	   }
	 
	   public void setWebId(String pid) {
	       this.web_id = pid;
	   }
	 
		   
	   public String getClassLvl1() {
	       return class_lvl1;
	   }
	 
	   public void setClassLvl1(String pt) {
	       this.class_lvl1 = pt;
	   }
	   
	   public String getClassLvl2() {
	       return class_lvl2;
	   }
	 
	   public void setClassLvl2(String pt) {
	       this.class_lvl2 = pt;
	   }
	  
	   public String getPageOrg(){
		   return page_org;
	   }
	   
	   public void setPageOrg(String wd){
		   this.page_org=wd;
	   }
	   
	   public int getPageRef(){
		   return page_ref;
	   }
	   
	   public void setPageRef(int wd){
		   this.page_ref=wd;
	   }
}
