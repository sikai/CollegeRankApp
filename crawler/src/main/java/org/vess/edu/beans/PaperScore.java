package org.vess.edu.beans;

public class PaperScore {
	   private String web_id;
	   private int ref_rate;
	   private int funds_rate;
	   private int sci_num;
	   private int ei_num;
	   private int istic_num;
	   private int cssci_num;
	   private int pku_num;
	 
	   public PaperScore() {
	        
	   }
	    
	   public String getWebId() {
	       return web_id;
	   }
	 
	   public void setWebId(String pid) {
	       this.web_id = pid;
	   }
	   
	   public int getRefRate(){
		   return ref_rate;
	   }
	   
	   public void setRefRate(int wd){
		   this.ref_rate=wd;
	   }
	   
	   public int getFundsRate(){
		   return funds_rate;
	   }
	   
	   public void setFundsRate(int wd){
		   this.funds_rate=wd;
	   }
	   
	   public int getSciNum(){
		   return sci_num;
	   }
	   
	   public void setSciNum(int wd){
		   this.sci_num=wd;
	   }
	   
	   public int getEiNum(){
		   return ei_num;
	   }
	   
	   public void setEiNum(int wd){
		   this.ei_num=wd;
	   }
	   
	   public int getIsticNum(){
		   return istic_num;
	   }
	   
	   public void setIsticNum(int wd){
		   this.istic_num=wd;
	   }
	   
	   public int getCssciNum(){
		   return cssci_num;
	   }
	   
	   public void setCssciNum(int wd){
		   this.cssci_num=wd;
	   }
	   
	   public int getPkuNum(){
		   return pku_num;
	   }
	   
	   public void setPkuNum(int wd){
		   this.pku_num=wd;
	   }
}
