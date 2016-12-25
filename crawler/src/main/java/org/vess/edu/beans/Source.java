package org.vess.edu.beans;

public class Source {
	   private long source_id;
	   private String source_title;
	   private double source_complex_rate;
	   private double source_general_rate;

	   
	 
	   public Source() {
	        
	   }
	    
	   public long getSourceId() {
	       return source_id;
	   }
	 
	   public void setSourceId(long pid) {
	       this.source_id = pid;
	   }
	 
		   
	   public String getSourceTitle() {
	       return source_title;
	   }
	 
	   public void setSourceTitle(String pt) {
	       this.source_title = pt;
	   }
	   
	   public double getSourceComplexRate() {
	       return source_complex_rate;
	   }
	 
	   public void setSourceComplexRate(double dt) {
	       this.source_complex_rate = dt;
	   }
	  
	   public double getSourceGeneralRate(){
		   return source_general_rate;
	   }
	   
	   public void setSourceGeneralRate(double wd){
		   this.source_general_rate=wd;
	   }
	   
}
