package org.vess.edu.beans;

public class College {
	 
	
	    
	   private int college_id;
	   private String college_city_lvl1;
	   private String college_city_lvl2;
	   private String college_title;
	   private String college_type;
	   private String college_grade;
	    
	 
	   public College() {
	        
	   }
	    
	   public int getCollegeId() {
	       return college_id;
	   }
	 
	   public void setCollegeId(int cid) {
	       this.college_id = cid;
	   }
	 
	   public String getCollegeCityLvl1() {
	       return college_city_lvl1;
	   }
	 
	   public void setCollegeCityLvl1(String city1) {
	       this.college_city_lvl1 = city1;
	   }
	   
	   public String getCollegeCityLvl2() {
	       return college_city_lvl2;
	   }
	 
	   public void setCollegeCityLvl2(String city2) {
	       this.college_city_lvl2 = city2;
	   }
	 
	   public String getCollegeType() {
	       return college_type;
	   }
	 
	   public void setCollegeType(String ctt) {
	       this.college_type = ctt;
	   }
	   
	   public String getCollegeTitle() {
	       return college_title;
	   }
	 
	   public void setCollegeTitle(String ct) {
	       this.college_title = ct;
	   }
	   
	   public String getCollegeGrade() {
	       return college_grade;
	   }
	 
	   public void setCollegeGrade(String cg) {
	       this.college_grade = cg;
	   }
	 
	}
