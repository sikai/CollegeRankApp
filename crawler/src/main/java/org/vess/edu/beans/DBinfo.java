package org.vess.edu.beans;

public class DBinfo {
	private int college_id;
	private String db_name;
	private String db_patent_name;
	private String paper_score_name;

	   public int getCollegeId() {
	       return college_id;
	   }
	 
	   public void setCollegeId(int cid) {
	       this.college_id = cid;
	   }
	 
	   public String getDBName() {
	       return db_name;
	   }
	 
	   public void setDBName(String city1) {
	       this.db_name = city1;
	   }
	   
	   public String getDBPatentName() {
	       return db_patent_name;
	   }
	 
	   public void setDBPatentName(String city2) {
	       this.db_patent_name = city2;
	   }
	   
	   public String getPSName() {
	       return paper_score_name;
	   }
	 
	   public void setPSName(String city2) {
	       this.paper_score_name = city2;
	   }
	   
}
