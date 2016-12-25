package org.vess.edu.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
 
import org.vess.edu.beans.*;
import org.vess.edu.conn.*;

/*
private String college_id;
private String college_city_lvl1;
private String college_city_lvl2;
private String college_title;
private String college_type;
private String college_grade;*/

public class DBUtils {
	 
	public static void insertProduct(Connection conn, College collge) throws SQLException {
	      String sql = "Insert into College( college_city_lvl1,college_city_lvl2,college_title,college_type,college_grade) values (?,?,?,?,?)";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	 
	      
	      pstm.setString(1, collge.getCollegeCityLvl1());
	      pstm.setString(2, collge.getCollegeCityLvl2());
	      pstm.setString(3, collge.getCollegeTitle());
	      pstm.setString(4, collge.getCollegeType());
	      pstm.setString(5, collge.getCollegeGrade());
	 
	      pstm.executeUpdate();
	  }
	
	// Get amount of all colleges
	public static long queryCollegeAmount(Connection conn)throws SQLException {
		String sql = "Select count(*) AS num from College";
		PreparedStatement pstm = conn.prepareStatement(sql);
		ResultSet rs = pstm.executeQuery();
		int num = -1;
		if(rs.next()){
			 num = rs.getInt("num");
		}
	
		return (long)num;
	}
	
	// Find College info by college ID
	public static List<College> queryCollegeById(Connection conn, int clg_id) throws SQLException {
	      String sql = "Select a.college_id, a.college_city_lvl1, a.college_city_lvl2, a.college_title, a.college_type, a.college_grade "
	      		     + "from College a where a.college_id = ?";
	      
	      PreparedStatement pstm = conn.prepareStatement(sql);
	      pstm.setInt(1, clg_id);
	      ResultSet rs = pstm.executeQuery();
	      List<College> list = new ArrayList<College>();
	      while (rs.next()) {
	          String clg_city_lvl1 = rs.getString("college_city_lvl1");
	          String clg_city_lvl2 = rs.getString("college_city_lvl2");
	          String clg_title = rs.getString("college_title");
	          String clg_type = rs.getString("college_type");
	          String clg_grade = rs.getString("college_grade");
	          College clg = new College();
	          clg.setCollegeId(clg_id);
	          clg.setCollegeCityLvl1(clg_city_lvl1);
	          clg.setCollegeCityLvl2(clg_city_lvl2);
	          clg.setCollegeTitle(clg_title);
	          clg.setCollegeType(clg_type);
	          clg.setCollegeGrade(clg_grade);	          
	          list.add(clg);
	      }
	      return list;
	}
	
	// Find College info within a  college ID range list
		public static List<College> queryCollegeWithinIdRange(Connection conn, int clg_id_idx, int end_idx) throws SQLException {
		      String sql = "Select a.college_id, a.college_city_lvl1, a.college_city_lvl2, a.college_title, a.college_type, a.college_grade "
		      		     + "from College a where a.college_id <= ? AND a.college_id >= ?";
		      
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setInt(1, end_idx);
		      pstm.setInt(2, clg_id_idx);
		      ResultSet rs = pstm.executeQuery();
		      List<College> list = new ArrayList<College>();
		      while (rs.next()) {
		    	  int clg_id = rs.getInt("college_id");
		          String clg_city_lvl1 = rs.getString("college_city_lvl1");
		          String clg_city_lvl2 = rs.getString("college_city_lvl2");
		          String clg_title = rs.getString("college_title");
		          String clg_type = rs.getString("college_type");
		          String clg_grade = rs.getString("college_grade");
		          College clg = new College();
		          clg.setCollegeId(clg_id);
		          clg.setCollegeCityLvl1(clg_city_lvl1);
		          clg.setCollegeCityLvl2(clg_city_lvl2);
		          clg.setCollegeTitle(clg_title);
		          clg.setCollegeType(clg_type);
		          clg.setCollegeGrade(clg_grade);	          
		          list.add(clg);
		      }
		      return list;
		}
		
		/*
		 * private long paper_id;
		   private String paper_author_lvl1;
		   private String paper_author_lvl2;
		   private String paper_author_lvl3;
		   private String paper_title;
		   private String paper_date;
		   private String paper_class_lvl1;
		   private String paper_class_lvl2;
		   private String paper_source;
		   private int paper_funds;
		   private int paper_references;
		 */
		
		//Insert items in Paper Table
		public static void insertPaper(Connection conn, Paper paper, String db_name) throws SQLException {
		      String sql = "Insert into "+ db_name +" ( paper_author_lvl1, paper_author_lvl2, paper_author_lvl3, paper_title, paper_date,"
		      		     + "paper_class_lvl1, paper_class_lvl2, paper_source, paper_record, paper_funds, paper_web_id, paper_references) "
		      			 + "values (?,?,?,?,?,?,?,?,?,?,?,?)";
		 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		 	      
		      pstm.setString(1, paper.getPaperAuthorLvl1());
		      pstm.setString(2, paper.getPaperAuthorLvl2());
		      pstm.setString(3, paper.getPaperAuthorLvl3());
		      pstm.setString(4, paper.getPaperTitle());
		      pstm.setString(5, paper.getPaperDate());  //20080102 format
		      pstm.setString(6, paper.getPaperClassLvl1());
		      pstm.setString(7, paper.getPaperClassLvl2());
		      pstm.setString(8, paper.getPaperSource());
		      pstm.setString(9, paper.getPaperRecord());
		      pstm.setString(10, paper.getPaperFunds());
		      pstm.setString(11, paper.getPaperWebId());
		      pstm.setInt(12, paper.getPaperReferences());
		      
		      pstm.executeUpdate();
		 }
		
		
		
		//Insert patents into Patent table
		public static void insertPatent(Connection conn, Patent patent) throws SQLException {
		      String sql = "Insert into Patent( patent_title, patent_date, patent_web_id, patent_class_lvl1, patent_class_lvl2, "
		    		  	 + "patent_org)"
		      			 + "values (?,?,?,?,?,?)";
		 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setString(1, patent.getPatentTitle());
		      pstm.setString(2, patent.getPatentDate());
		      pstm.setString(3, patent.getPatentWebId());
		      pstm.setString(4, patent.getPatentClassLvl1());
		      pstm.setString(5, patent.getPatentClassLvl2());
		      pstm.setString(6, patent.getPatentOrg());
		      
		      pstm.executeUpdate();
		}
		
		//Insert source into Source table
		public static void insertSource(Connection conn, Source source) throws SQLException {
		      String sql = "Insert into Source( source_title, source_complex_rate, source_general_rate)"
		      			 + "values (?,?,?)";
		 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setString(1, source.getSourceTitle());
		      pstm.setDouble(2, source.getSourceComplexRate());
		      pstm.setDouble(3, source.getSourceGeneralRate());
		      
		      
		      pstm.executeUpdate();
		}
		
		//Insert page info into PageAbstract table
		public static void insertPageAbstract(Connection conn, PageAbstract pageabs) throws SQLException {
		     String sql = "Insert into PageAbstract( web_id, class_lvl1, class_lvl2, page_org, page_ref)"
				      			 + "values (?,?,?,?,?)";
				 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setString(1, pageabs.getWebId());
		      pstm.setString(2, pageabs.getClassLvl1());
		      pstm.setString(3, pageabs.getClassLvl2());
		      pstm.setString(4, pageabs.getPageOrg());
		      pstm.setInt(5, pageabs.getPageRef());
				      
		      pstm.executeUpdate();
		}
		
		//Insert college info from qingta.com to CollegeDetail table
		public static void insertCollegeDetail(Connection conn, Qingta qt) throws SQLException {
		     String sql = "Insert into CollegeDetailNew( college_id, college_title, master_num, doc_num, yuanshi_num,  "
		     			+ "changjiang_num, qingnian_num, good_sub_num, nature_prize_num, social_prize_num, total_prize_num, lab_num,create_date)"
		     			+ "values (?,?,?,?,?,?,?,?,?,?,?,?,CURDATE())";
				 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setInt(1, qt.getQingtaId());
		      pstm.setString(2, qt.getQingtaCollegeTitle());
		      pstm.setInt(3, qt.getMaster());
		      pstm.setInt(4, qt.getDoc());
		      pstm.setInt(5, qt.getYuanshi());
		      pstm.setInt(6, qt.getChangjiang());
		      pstm.setInt(7, qt.getQingnian());
		      pstm.setInt(8, qt.getGoodSubject());
		      pstm.setInt(9, qt.getNaturePrize());
		      pstm.setInt(10, qt.getSocialPrize());
		      pstm.setInt(11, qt.getTotalPrize());
		      pstm.setInt(12, qt.getLab());
		      pstm.executeUpdate();
		}
		
		//Insert of update master info from baidubaike.com to CollegeDetail table	
		public static void insertBaiduMS(Connection conn, int clg_id, String clg_name, int clg_master) throws SQLException {
		     String sql = "Insert into CollegeDetailNew(college_id, college_title, master_num)"		     			
		     			+ "values (?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE  college_title= ?, master_num =?";
				 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setInt(1, clg_id);
		      pstm.setString(2, clg_name);
		      pstm.setInt(3, clg_master);
		      //pstm.setInt(4, clg_id);
		      pstm.setString(4, clg_name);
		      pstm.setInt(5, clg_master);
		      
		      pstm.executeUpdate();
		}		
		
		//Insert of update doc info from baidubaike.com to CollegeDetail table
		public static void insertBaiduDoc(Connection conn, int clg_id, String clg_name, int clg_doc) throws SQLException {
		     String sql = "Insert into CollegeDetailNew(college_id, college_title, doc_num)"		     			
		     			+ "values (?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE  college_title= ?, doc_num =?";
				 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setInt(1, clg_id);
		      pstm.setString(2, clg_name);
		      pstm.setInt(3, clg_doc);
		      //pstm.setInt(4, clg_id);
		      pstm.setString(4, clg_name);
		      pstm.setInt(5, clg_doc);
		      
		      pstm.executeUpdate();
		}		
		
		//Insert of update post doc info from baidubaike.com to CollegeDetail table
		public static void insertBaiduPostDoc(Connection conn, int clg_id, String clg_name, int clg_post_doc) throws SQLException {
		     String sql = "Insert into CollegeDetailNew(college_id, college_title, post_doc_num)"		     			
		     			+ "values (?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE college_title= ?,post_doc_num =?";
				 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setInt(1, clg_id);
		      pstm.setString(2, clg_name);
		      pstm.setInt(3, clg_post_doc);
		      //pstm.setInt(4, clg_id);
		      pstm.setString(4, clg_name);
		      pstm.setInt(5, clg_post_doc);
		      
		      pstm.executeUpdate();
		}		
				
		
		// Insert DBinfo of new college including paper_DB name, patent_DB name, paper_score_DB name
		public static void insertNewDBinfo(int clg_id){
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			try {			
				conn  = ConnectionUtils.getConnection();
				String db_name = "Paper"+ Integer.toString(clg_id);
				String db_patent_name = "Patent"+ Integer.toString(clg_id);
				String paper_score_name = "PaperScore" + Integer.toString(clg_id);
				String sql = "Insert into DBinfo(college_id, db_name, db_patent_name, paper_score_name )"		     			
		     			+ "values (?,?,?,?) ";
				pstm = conn.prepareStatement(sql);
			    pstm.setInt(1, clg_id);
			    pstm.setString(2, db_name);
			    pstm.setString(3, db_patent_name);
			    pstm.setString(4, paper_score_name);
			    
			    pstm.executeUpdate();
			}catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }
			
		}
		
		// Create new Paper table for college
		public static void createNewPaperDB(String db_name){
			Connection conn = null;
			PreparedStatement pstm = null;				
			try {			
				conn  = ConnectionUtils.getConnection();
				String sql = "Create table " + db_name + "("+
							"paper_id bigint NOT NULL AUTO_INCREMENT,"+
							"paper_author_lvl1 varchar(255) NOT NULL,"+
							"paper_author_lvl2 varchar(255),"+
							"paper_author_lvl3 varchar(255),"+
							"paper_title varchar(255) NOT NULL,"+
							"paper_date date,"+
							"paper_class_lvl1 varchar(255),"+
							"paper_class_lvl2 varchar(255),"+
							"paper_source varchar(255),"+
							"paper_record varchar(255),"+
							"paper_funds varchar(255),"+
							"paper_web_id varchar(255) NOT NULL,"+
							"paper_references int,"+
							"UNIQUE (paper_web_id),"+
							"PRIMARY KEY(paper_id)"+
							");";
				pstm = conn.prepareStatement(sql);
				
				pstm.executeUpdate();
			}catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }			
		}

		// Create new Patent table for college
		public static void createNewPatentDB(String db_name){
			Connection conn = null;
			PreparedStatement pstm = null;				
			try {			
				conn  = ConnectionUtils.getConnection();
				String sql = "Create table " + db_name + "("+
							"patent_id int NOT NULL AUTO_INCREMENT,"+
							"patent_title varchar(255) NOT NULL,"+
							"patent_date date,"+
							"patent_web_id varchar(255) NOT NULL,"+
							"patent_class_lvl1 varchar(255),"+
							"patent_class_lvl2 varchar(255),"+
							"patent_org varchar(255), "+
							"UNIQUE (patent_web_id), "+
							"PRIMARY KEY(patent_id)"+								
							");";
				pstm = conn.prepareStatement(sql);
				
				pstm.executeUpdate();
			}catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }			
		}
		
		// Create new PaperScore table for college    <----------------还没改完
		public static void createNewPaperScoreDB(String db_name){
			Connection conn = null;
			PreparedStatement pstm = null;				
			try {			
				conn  = ConnectionUtils.getConnection();
				String sql = "Create table " + db_name + "("+
							"patent_id int NOT NULL AUTO_INCREMENT,"+
							"patent_title varchar(255) NOT NULL,"+
							"patent_date date,"+
							"patent_web_id varchar(255) NOT NULL,"+
							"patent_class_lvl1 varchar(255),"+
							"patent_class_lvl2 varchar(255),"+
							"patent_org varchar(255), "+
							"UNIQUE (patent_web_id), "+
							"PRIMARY KEY(patent_id)"+								
							");";
				pstm = conn.prepareStatement(sql);
				
				pstm.executeUpdate();
			}catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }			
		}
		
	 //-------------------------------------------------Query Part-----------------------------------------------------------
	
		// Get page info from PageAbstract table
		public static PageAbstract getPageAbstract(Connection conn, String web_id_in) throws SQLException {
		     String sql = "Select a.web_id, a.class_lvl1, a.class_lvl2, a.page_org, a.page_ref "
	      		        + "from PageAbstract a where a.web_id = ?";
				 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setString(1, web_id_in);

		      ResultSet rs = pstm.executeQuery();
		      PageAbstract page_abs = new PageAbstract();
		      while (rs.next()) {
		          String abs_web_id = rs.getString("web_id");
		          String abs_class_lvl1 = rs.getString("class_lvl1");
		          String abs_class_lvl2 = rs.getString("class_lvl2");
		          String abs_org = rs.getString("page_org");
		          int abs_ref = rs.getInt("page_ref");
		          page_abs.setWebId(abs_web_id);
		          page_abs.setClassLvl1(abs_class_lvl1);
		          page_abs.setClassLvl2(abs_class_lvl2);
		          page_abs.setPageOrg(abs_org);
		          page_abs.setPageRef(abs_ref);
		      }
		      return page_abs;
		}
		
		// Query if College record exists by college name
		public static int GetCollegeIdByName(Connection conn, String clg_name) throws SQLException {
			 int ret = -1;
		     String sql = "Select a.college_id from College a where a.college_title = ? ";
			 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      pstm.setString(1, clg_name);

		      ResultSet rs = pstm.executeQuery();

		      while (rs.next()) {
		          ret = rs.getInt("college_id");
		      }
		      return ret;
		}
		
		// Check if a college exists in DBinfo table
		public static College getCollegeByName(String  clg_name) throws SQLException{
			College clg = null;
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			try {			
				conn  = ConnectionUtils.getConnection();
				String sql = "Select college_id, college_city_lvl1, college_city_lvl2, college_title, "
						+ "college_type, college_grade "
						+ " from College where college_title = ?";
				pstm = conn.prepareStatement(sql);
				pstm.setString(1, clg_name);
				rs = pstm.executeQuery();
				//if (!rs.next() ) return null;
				while (rs.next()) {
			          int college_id = rs.getInt("college_id");
			         // System.out.println("colelgeid is :"+college_id);
			          String college_city_lvl1 = rs.getString("college_city_lvl1");
			          String college_city_lvl2 = rs.getString("college_city_lvl2");
			          String college_title = rs.getString("college_title");
			          String college_type = rs.getString("college_type");
			          String college_grade = rs.getString("college_grade");
			          clg = new College();
			          clg.setCollegeId(college_id);
			          clg.setCollegeCityLvl1(college_city_lvl1);
			          clg.setCollegeCityLvl2(college_city_lvl2);
			          clg.setCollegeTitle(college_title);
			          clg.setCollegeType(college_type);
			          clg.setCollegeGrade(college_grade);
			    }
			}catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }
			
			return clg;
		}
				
		
		// Get all college ids form Table CollegeDetailNew
		public static List<String> GetCollegeNameAll(Connection conn) throws SQLException {
			 List<String> ret = new ArrayList<String>();
		     String sql = "Select college_title from CollegeDetailNew";
			 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		      ResultSet rs = pstm.executeQuery();

		      while (rs.next()) {
		          String temp = rs.getString("college_title").trim();
		          ret.add(temp);
		      }
		      return ret;
		}
		
		// Get DB info given college id
		public static DBinfo getDBinfoById(int clg_id){
			DBinfo info = null;
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			try {			
				conn  = ConnectionUtils.getConnection();
				String sql = "Select db_name, db_patent_name, paper_score_name "
	      		           + "from DBinfo  where college_id = ?";	
				pstm = conn.prepareStatement(sql);			
				pstm.setInt(1, clg_id);
				System.out.println(pstm);
		        rs = pstm.executeQuery();
		        //if (!rs.next() ) return null;
		        while (rs.next()) {
		        	String db_name = rs.getString("db_name");
		        	String ptt_name = rs.getString("db_patent_name");
		        	String ps_name = rs.getString("paper_score_name");
		        	info = new DBinfo();
		        	info.setCollegeId(clg_id);
		        	info.setDBName(db_name);
		        	info.setDBPatentName(ptt_name);
		        	info.setPSName(ps_name);
		        }	        
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }
			return info;
		}
		
		// Get DB paper name given college id, for checking paper table name if cannot find it in request field
		public static String getPaperDBNameByCollegeId(int clg_id){
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			String ret = null;
			try {			
				conn  = ConnectionUtils.getConnection();
				String sql = "Select db_name "
	      		           + "from DBinfo  where college_id = ?";	
				pstm = conn.prepareStatement(sql);			
				pstm.setInt(1, clg_id);
				//System.out.println(pstm);
		        rs = pstm.executeQuery();
		        while (rs.next()) {
		        	ret = rs.getString("db_name");
		        }	        
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }
			return ret;
		}
	  /*
	  public static UserAccount findUser(Connection conn, String userName, String password) throws SQLException {
	 
	      String sql = "Select a.User_Name, a.Password, a.Gender from User_Account a "
	              + " where a.User_Name = ? and a.password= ?";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	      pstm.setString(1, userName);
	      pstm.setString(2, password);
	      ResultSet rs = pstm.executeQuery();
	 
	      if (rs.next()) {
	          String gender = rs.getString("Gender");
	          UserAccount user = new UserAccount();
	          user.setUserName(userName);
	          user.setPassword(password);
	          user.setGender(gender);
	          return user;
	      }
	      return null;
	  }
	 
	  public static UserAccount findUser(Connection conn, String userName) throws SQLException {
	 
	      String sql = "Select a.User_Name, a.Password, a.Gender from User_Account a " + " where a.User_Name = ? ";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	      pstm.setString(1, userName);
	 
	      ResultSet rs = pstm.executeQuery();
	 
	      if (rs.next()) {
	          String password = rs.getString("Password");
	          String gender = rs.getString("Gender");
	          UserAccount user = new UserAccount();
	          user.setUserName(userName);
	          user.setPassword(password);
	          user.setGender(gender);
	          return user;
	      }
	      return null;
	  }
	 
	  public static List<Product> queryProduct(Connection conn) throws SQLException {
	      String sql = "Select a.Code, a.Name, a.Price from Product a ";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	 
	      ResultSet rs = pstm.executeQuery();
	      List<Product> list = new ArrayList<Product>();
	      while (rs.next()) {
	          String code = rs.getString("Code");
	          String name = rs.getString("Name");
	          float price = rs.getFloat("Price");
	          Product product = new Product();
	          product.setCode(code);
	          product.setName(name);
	          product.setPrice(price);
	          list.add(product);
	      }
	      return list;
	  }
	 
	  public static Product findProduct(Connection conn, String code) throws SQLException {
	      String sql = "Select a.Code, a.Name, a.Price from Product a where a.Code=?";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	      pstm.setString(1, code);
	 
	      ResultSet rs = pstm.executeQuery();
	 
	      while (rs.next()) {
	          String name = rs.getString("Name");
	          float price = rs.getFloat("Price");
	          Product product = new Product(code, name, price);
	          return product;
	      }
	      return null;
	  }
	 
	  public static void updateProduct(Connection conn, Product product) throws SQLException {
	      String sql = "Update Product set Name =?, Price=? where Code=? ";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	 
	      pstm.setString(1, product.getName());
	      pstm.setFloat(2, product.getPrice());
	      pstm.setString(3, product.getCode());
	      pstm.executeUpdate();
	  }
	 
	  public static void insertProduct(Connection conn, Product product) throws SQLException {
	      String sql = "Insert into Product(Code, Name,Price) values (?,?,?)";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	 
	      pstm.setString(1, product.getCode());
	      pstm.setString(2, product.getName());
	      pstm.setFloat(3, product.getPrice());
	 
	      pstm.executeUpdate();
	  }
	 
	  public static void deleteProduct(Connection conn, String code) throws SQLException {
	      String sql = "Delete from Product where Code= ?";
	 
	      PreparedStatement pstm = conn.prepareStatement(sql);
	 
	      pstm.setString(1, code);
	 
	      pstm.executeUpdate();
	  }
	 
	 */
		
		//--------------------DELETE LATER

		//Insert items in Paper Table BUPT
		public static void insertPaperXBGD(Connection conn, Paper paper) throws SQLException {
		      String sql = "Insert into PaperXBGD( paper_author_lvl1, paper_author_lvl2, paper_author_lvl3, paper_title, paper_date,"
		      		     + "paper_class_lvl1, paper_class_lvl2, paper_source, paper_record, paper_funds, paper_web_id, paper_references) "
		      			 + "values (?,?,?,?,?,?,?,?,?,?,?,?)";
		 
		      PreparedStatement pstm = conn.prepareStatement(sql);
		 	      
		      pstm.setString(1, paper.getPaperAuthorLvl1());
		      pstm.setString(2, paper.getPaperAuthorLvl2());
		      pstm.setString(3, paper.getPaperAuthorLvl3());
		      pstm.setString(4, paper.getPaperTitle());
		      pstm.setString(5, paper.getPaperDate());  //20080102 format
		      pstm.setString(6, paper.getPaperClassLvl1());
		      pstm.setString(7, paper.getPaperClassLvl2());
		      pstm.setString(8, paper.getPaperSource());
		      pstm.setString(9, paper.getPaperRecord());
		      pstm.setString(10, paper.getPaperFunds());
		      pstm.setString(11, paper.getPaperWebId());
		      pstm.setInt(12, paper.getPaperReferences());
		      
		      pstm.executeUpdate();
		 }
		
		
		//Insert patents into Patent table
				public static void insertPatentXBGD(Connection conn, Patent patent) throws SQLException {
				      String sql = "Insert into PatentXBGD( patent_title, patent_date, patent_web_id, patent_class_lvl1, patent_class_lvl2, "
				    		  	 + "patent_org)"
				      			 + "values (?,?,?,?,?,?)";
				 
				      PreparedStatement pstm = conn.prepareStatement(sql);
				      pstm.setString(1, patent.getPatentTitle());
				      pstm.setString(2, patent.getPatentDate());
				      pstm.setString(3, patent.getPatentWebId());
				      pstm.setString(4, patent.getPatentClassLvl1());
				      pstm.setString(5, patent.getPatentClassLvl2());
				      pstm.setString(6, patent.getPatentOrg());
				      
				      pstm.executeUpdate();
				}
	}
