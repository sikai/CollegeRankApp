package crawler_test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.vess.edu.beans.*;
import org.vess.edu.conn.*;
import org.vess.edu.utils.DBUtils;
import org.vess.edu.conn.*;

//Update PaperScore table for each college according to the data in Papaer table (PaperBUPT e.g.)
public class UpdateCollegeScore {

	
	public static void main(String[] args) throws SQLException {
		String DBName = "中国科学技术大学";
		String DBName1 = "北京邮电大学";
		String DBName2 = "西北工业大学";
		String DBName3 = "中国石油大学(北京)";
		//updatePaperScore(DBName1);		
		//updatePaperScore(DBName3);
		//String s = DBUtils.getPaperDBNameByCollegeId(32);
		//updateMeanDev();
		//updateCollegePprPttNormZscore(DBName1);
		//updateMeanDevTA();
		//updateTalentInputNormZscore(DBName2);
		//convertXLS();
		updateOverallScore(DBName);
		updateOverallScore(DBName1);
		updateOverallScore(DBName2);
		//updateOverallScore(DBName3);
		//updateMeanDevSub();
		//updateSubPprPttNormZscore(DBName);
	
	}
	
	// weighted value for each academic index e.g. SCI,EI
	public static final int SCI = 10;
	public static final int EI = 10;
	public static final int ISTIC = 5;
	public static final int CSSCI = 5;
	public static final int PKU = 2;
	// weighted value for each academic ability 
	public static final double REF = 0.4;
	public static final double FUNDS = 0.5;
	public static final double SCI_= 0.4;
	public static final double PKU_ = 0.2;
	
	
	//Get paperScore table name for each college e.g. paperScoreBUPT
	public static String getPaperScoreName(String dbName) throws SQLException {
		String PaperScoreDBName = null;
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {			
			conn  = ConnectionUtils.getConnection();
			String sql = "Select  paper_score_name "
      		           + "from DBinfo  where db_name = ?";
			
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, dbName);
	        rs = pstm.executeQuery();
	        while (rs.next()) {
	        	PaperScoreDBName = rs.getString("paper_score_name");
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
		return PaperScoreDBName;
	}
	
	// Get collegId by college name
	public static int getCollegIdByName(String Name) throws SQLException {
		int ret = -1;
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {			
			conn  = ConnectionUtils.getConnection();
			String sql = "Select  college_id "
      		           + "from College  where college_title = ?";			
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, Name);
	        rs = pstm.executeQuery();
	        while (rs.next()) {
	        	ret = rs.getInt("college_id");
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
	
	//Update overall score for each college based on lvl1 lv2 lvl3 score
	public static void updateOverallScore(String Name) throws SQLException{
		int clg_id = getCollegIdByName(Name);
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		double ref_score = 0;
		double sci_score = 0;
		double pku_score = 0;
		double patent_score = 0;
		double paper_score = 0;
		
		// get data from paper and patent table
		try {			
			conn  = ConnectionUtils.getConnection();
			String sql = "Select ref_score, sci_score, pku_score, patent_score, paper_score "	
      		           + "from CollegePaperAndPatentScore where college_id = ? ORDER BY create_date DESC LIMIT 1 ";
   			
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, clg_id);
			System.out.println(pstm);
	        rs = pstm.executeQuery();
	        while (rs.next()) {
	        	ref_score = rs.getDouble("ref_score");
	        	sci_score = rs.getDouble("sci_score");
	        	pku_score = rs.getDouble("pku_score");
	        	patent_score = rs.getDouble("patent_score");
	        	paper_score = rs.getDouble("paper_score");
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
		
		// get data from CollegeTalentAndInputScore table
		double yuanshi_score = 0;
		double changjiang_score = 0;
		double qingnian_score = 0;
		double master_score = 0;
		double doc_score = 0;
		double post_doc_score = 0;
		double lab_score = 0;
		double nature_prize_score = 0;
		double social_prize_score = 0;
		double total_prize_score = 0;
		try {			
			conn  = ConnectionUtils.getConnection();
			String sql = "Select yuanshi_score, changjiang_score, qingnian_score, master_score, doc_score, post_doc_score, "
					   + "lab_score, nature_prize_score, social_prize_score, total_prize_score "	
      		           + "from CollegeTalentAndInputScore  where college_id = ? ORDER BY create_date DESC LIMIT 1 ";
   			
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, clg_id);
	        rs = pstm.executeQuery();
	        while (rs.next()) {
	        	yuanshi_score = rs.getDouble("yuanshi_score");
	        	changjiang_score = rs.getDouble("changjiang_score");
	        	qingnian_score = rs.getDouble("qingnian_score");
	        	master_score = rs.getDouble("master_score");
	        	doc_score = rs.getDouble("doc_score");
	        	post_doc_score = rs.getDouble("post_doc_score");
	        	lab_score = rs.getDouble("lab_score");
	        	nature_prize_score = rs.getDouble("nature_prize_score");
	        	social_prize_score = rs.getDouble("social_prize_score");
	        	total_prize_score = rs.getDouble("total_prize_score");
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
		
		// get data from CollegeOtherScore table
		double rdPctg_score = 0;
		double snrPctg_score = 0;
		double rdMoney_score = 0;
		double rdMoneyPp_score = 0;
		double projectNum_score = 0;
		double dealNum_score = 0;
		double dealMoney_score = 0;
		
		if (rs != null) rs.close();
		if (pstm != null) pstm.close();
		if (conn != null) conn.close();
		
		try {			
			conn  = ConnectionUtils.getConnection();
			String sql = "Select rdPctg_score, snrPctg_score, rdMoney_score, rdMoneyPp_score, projectNum_score, dealNum_score, "
					   + "dealMoney_score "	
      		           + "from CollegeOtherScore  where college_id = ? ORDER BY create_date DESC LIMIT 1 ";
   			
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, clg_id);
	        rs = pstm.executeQuery();
	        while (rs.next()) {
	        	rdPctg_score = rs.getDouble("rdPctg_score");
	        	snrPctg_score = rs.getDouble("snrPctg_score");
	        	rdMoney_score = rs.getDouble("rdMoney_score");
	        	rdMoneyPp_score = rs.getDouble("rdMoneyPp_score");
	        	projectNum_score = rs.getDouble("projectNum_score");
	        	dealNum_score = rs.getDouble("dealNum_score");
	        	dealMoney_score = rs.getDouble("dealMoney_score");
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
		
		//calculate all lvl2 score 
		double D_rdPctg_score = 0.1;
		double D_yuanshi_score = 0.7;
		double D_snrPctg_score = 0.2;
		double talent_score = D_rdPctg_score*rdPctg_score + D_yuanshi_score*(yuanshi_score+changjiang_score+qingnian_score)/3
				              + D_snrPctg_score* snrPctg_score;
		
		double D_master_score = 0.55;
		double D_lab_score = 0.45;
		double platform_score = D_master_score*(master_score+doc_score+post_doc_score)/3 + D_lab_score*lab_score;
		
		double D_rd_score = 0.13;
		double D_rdpp_score = 0.14;
		double D_nat_prize_score = 0.3;
		double D_soc_prize_score = 0.3;
		double D_proj_num_score = 0.13;
		double input_score = D_rd_score*rdMoney_score + D_rdpp_score*rdMoneyPp_score + D_nat_prize_score*nature_prize_score 
				           + D_soc_prize_score*social_prize_score + D_proj_num_score*projectNum_score;
		
		double prize_score = total_prize_score;
		
		double D_deal_num = 0.45;
		double D_deal_amount = 0.55;
		double transform_score = D_deal_num*dealNum_score + D_deal_amount*dealMoney_score;
		
		//calculate all lvl1 score 
		double D_talent_score = 0.4;
		double D_platform_score = 0.6;
		double basic_score = D_talent_score* talent_score + D_platform_score* platform_score;
		
		double D_paper_score = 0.3;
		double D_prize_score = 0.2;
		double D_patent_score = 0.3;
		double D_transform_score = 0.2;
		double output_score = D_paper_score*paper_score + D_prize_score*prize_score+ D_patent_score*patent_score
				            + D_transform_score*transform_score;
		
		//calculate overall score
		double D_basic = 0.15;
		double D_input = 0.3;
		double D_output = 0.55;
		double overall_score = D_basic*basic_score + D_input*input_score + D_output*output_score;
		
		if (rs != null) rs.close();
		if (pstm != null) pstm.close();
		if (conn != null) conn.close();
		// Insert into table
		try {	
			conn  = ConnectionUtils.getConnection();
			String sql = "Insert into CollegeOverallScore (college_id, talent_score, platform_score, input_score, "
					+ "paper_score, prize_score, patent_score, transform_score, basic_score, output_score, overall_score) "		     			
	     			+ "values (?,?,?,?,?,?,?,?,?,?,?) "
	     			+ "ON DUPLICATE KEY UPDATE talent_score= ?,platform_score =?, input_score=?,paper_score=?, prize_score=?, "
	     			+ "patent_score=?, transform_score=?, basic_score=?, output_score=?, overall_score=? ";
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, clg_id);
			pstm.setDouble(2, talent_score);
			pstm.setDouble(3, platform_score);
			pstm.setDouble(4,input_score);
			pstm.setDouble(5,paper_score);
			pstm.setDouble(6,prize_score);
			pstm.setDouble(7,patent_score);
			pstm.setDouble(8,transform_score);
			pstm.setDouble(9,basic_score);
			pstm.setDouble(10,output_score);
			pstm.setDouble(11,overall_score);
			pstm.setDouble(12, talent_score);
			pstm.setDouble(13, platform_score);
			pstm.setDouble(14,input_score);
			pstm.setDouble(15,paper_score);
			pstm.setDouble(16,prize_score);
			pstm.setDouble(17,patent_score);
			pstm.setDouble(18,transform_score);
			pstm.setDouble(19,basic_score);
			pstm.setDouble(20,output_score);
			pstm.setDouble(21,overall_score);
		
			pstm.executeUpdate();
        } catch (SQLException | ClassNotFoundException  e) {
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
	
	//Update PaperScore table for each college
	public static void updatePaperScore(String Name) throws SQLException {
		int clg_id = getCollegIdByName(Name);
		System.out.println(clg_id);
		DBinfo dbinfo = DBUtils.getDBinfoById(clg_id);
		String paperDbName = dbinfo.getDBName();
		System.out.println(paperDbName);
		String scoreDbName = dbinfo.getPSName();
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		//List<PaperScore> pScoreList = new ArrayList<PaperScore>();
		try {		
			conn  = ConnectionUtils.getConnection();
			String sql = "Select paper_web_id, paper_references, paper_funds, paper_record, paper_source "
	   		        + "from "+ paperDbName;			
			pstm = conn.prepareStatement(sql);
			System.out.println(pstm);
	        rs = pstm.executeQuery();
	        while (rs.next()) {
	        	String ppr_web_id = rs.getString("paper_web_id");
	        	int ppr_ref = rs.getInt("paper_references");
	        	String ppr_funds_str = rs.getString("paper_funds");
	        	String ppr_record_str = rs.getString("paper_record");
	        	//String ppr_source_str = rs.getString("paper_source");
	        	//Handling funds content
	        	int ppr_funds = 0;
	        	if(ppr_funds_str!=null && !ppr_funds_str.equals("")) ppr_funds = ppr_funds_str.split("，").length;
	        	
	        	//Handling record
	        	int ppr_sci = 0;
	        	int ppr_ei = 0;
	        	int ppr_istic = 0;
	        	int ppr_cssi = 0;
	        	int ppr_pku = 0;
	        	if(ppr_record_str!=null && !ppr_record_str.equals("")){
	        		String[] record_arr =  ppr_record_str.split(",");
		        	for(int i=0;i<record_arr.length;i++){
		        		String item = record_arr[i].trim();
		        		if(item.equals("SCI")) ppr_sci+= 1;
		        		if(item.equals("EI")) ppr_ei+= 1;
		        		if(item.equals("ISTIC")) ppr_istic+= 1;
		        		if(item.equals("CSSCI")) ppr_cssi+= 1;
		        		if(item.equals("PKU")) ppr_pku+= 1;
		        	}
	        	}
	        	
	        	//Handling source   <--LEGACY
	        	//int ppr_source = getSourceRate(ppr_source_str.trim());
	        	
	        	//Store content in PaperScore class
	        	PaperScore ps = new PaperScore();
	        	ps.setWebId(ppr_web_id);
	        	ps.setRefRate(ppr_ref);
	        	ps.setSciNum(ppr_sci);
	        	ps.setEiNum(ppr_ei);
	        	ps.setIsticNum(ppr_istic);
	        	ps.setCssciNum(ppr_cssi);
	        	ps.setPkuNum(ppr_pku);
	        	ps.setFundsRate(ppr_funds);	        	
	        	
	        	// Store into DB
	        	insertPaperScore(scoreDbName,ps);
	        	
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
	}	
	
	// Get Source rate from Source table  <-------LEGACY
	/*
	public static int getSourceRate(String sName) throws SQLException {
		int ret = 0;
		Connection conn1 = null;
		PreparedStatement pstm1 = null;
		ResultSet rs1 = null;
		try {	
			conn1  = ConnectionUtils.getConnection();
			String sql = "Select  source_complex_rate, source_general_rate "
      		           + "from Source  where source_title = ?";			
			pstm1 = conn1.prepareStatement(sql);
			pstm1.setString(1, sName);
	        rs1 = pstm1.executeQuery();
	        while (rs1.next()) {
	        	float f1 = rs1.getFloat("source_complex_rate");
	        	float f2 = rs1.getFloat("source_general_rate");
	        	float f = (f1+f2)*500;
	        	ret = (int) f;
	        }	        
        } catch (SQLException | ClassNotFoundException  e) {
            e.printStackTrace();           
        }finally { 
        	try { if (rs1 != null) rs1.close(); } catch (Exception e) {};
            try { if (pstm1 != null) pstm1.close(); } catch (Exception e) {};
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (SQLException e) { }
            }
        }
		return ret;
	}
	*/
	
	// Insert into PaperScore table
		public static void insertPaperScore(String psDbName, PaperScore ps) throws SQLException {
			Connection conn2 = null;
			PreparedStatement pstm2 = null;
			try {	
				conn2  = ConnectionUtils.getConnection();
				String sql = "Insert into "+ psDbName +" (web_id, ref_rate, funds_rate, sci_num, ei_num, "
						+ "istic_num, cssci_num, pku_num) "		     			
		     			+ "values (?,?,?,?,?,?,?,?) ";
		     			//+ "ON DUPLICATE KEY UPDATE ref_rate= ?, funds_rate= ?,record_rate =?, source_rate=?";
				pstm2 = conn2.prepareStatement(sql);
				pstm2.setString(1,ps.getWebId());
				pstm2.setInt(2,ps.getRefRate());
				pstm2.setInt(3,ps.getFundsRate());
				pstm2.setInt(4,ps.getSciNum());
				pstm2.setInt(5,ps.getEiNum());
				pstm2.setInt(6,ps.getIsticNum());
				pstm2.setInt(7,ps.getCssciNum());
				pstm2.setInt(8,ps.getPkuNum());
				pstm2.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm2 != null) pstm2.close(); } catch (Exception e) {};
	            if (conn2 != null) {
	                try {
	                    conn2.close();
	                } catch (SQLException e) { /* ignored */}
	            }
	        }
		}
		
		
		//Calculate normalized score and insert into CollegePaperAndPatentScore table
		public static void updateCollegePprPttNormZscore(String Name) throws SQLException {
			int clg_id = getCollegIdByName(Name);
			DBinfo dbinfo = DBUtils.getDBinfoById(clg_id);
			String psDbName = dbinfo.getPSName();
			String pttDBName = dbinfo.getDBPatentName();
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			double ref_mean = -1;
			double ref_dev = -1;
			double funds_mean = -1;
			double funds_dev = -1;
			double sci_mean = -1;
			double sci_dev = -1;
			double pku_mean = -1;
			double pku_dev = -1;
			double patent_mean = -1;
			double patent_dev = -1;
			double ref_norm = 0;
			double funds_norm = 0;
			double sci_norm = 0;
			double pku_norm = 0;
			double paper_norm = 0;
			double patent_norm = 0;

			// Get Mean and standard deviation value from table MeanDevRate
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select ref_mean, ref_dev, funds_mean, funds_dev, sci_mean, sci_dev, pku_mean, pku_dev, patent_mean, patent_dev "
	      		           + " from MeanDevRate";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					ref_mean = rs.getDouble("ref_mean");
					ref_dev = rs.getDouble("ref_dev");
					funds_mean = rs.getDouble("funds_mean");
					funds_dev = rs.getDouble("funds_dev");
					sci_mean = rs.getDouble("sci_mean");
					sci_dev = rs.getDouble("sci_dev");
					pku_mean = rs.getDouble("pku_mean");
					pku_dev = rs.getDouble("pku_dev");
					patent_mean = rs.getDouble("patent_mean");
					patent_dev = rs.getDouble("patent_dev");
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
			
			// Processing paper norm facts
			NormalDistribution dist = new NormalDistribution();
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select SUM(ref_rate) as ref_sum, SUM(funds_rate) as funds_sum, "
						   + " SUM(sci_num)+SUM(ei_num)  as sci_sum, "
						   + " SUM(istic_num)+SUM(cssci_num)+SUM(pku_num)  as pku_sum "
	      		           + " from "+ psDbName;
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					int ref_rate = rs.getInt("ref_sum");
					int funds_rate = rs.getInt("funds_sum");
					int sci_rate = rs.getInt("sci_sum");
					int pku_rate = rs.getInt("pku_sum");
					
					ref_norm = dist.cumulativeProbability( getZscore((double)ref_rate,ref_mean,ref_dev)) *100;
					funds_norm = dist.cumulativeProbability(getZscore((double)funds_rate,funds_mean,funds_dev))*100;
					sci_norm = dist.cumulativeProbability(getZscore((double)sci_rate,sci_mean,sci_dev))*100;
					pku_norm = dist.cumulativeProbability(getZscore((double)pku_rate,pku_mean,pku_dev))*100;
					paper_norm = REF*ref_norm + SCI_*sci_norm + PKU_*pku_norm;
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
			
			if (rs != null) rs.close();
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();
			//Processing patent norm facts  
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select COUNT(*) as count "
	      		           + "from "+pttDBName;
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					int ptt_num = rs.getInt("count");
					patent_norm = dist.cumulativeProbability(getZscore((double)ptt_num,patent_mean,patent_dev))*100;
				}
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) {}
	            }
	        }
			
	        
	        
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();
			// Insert normalized scores into CollegePaperAndPatentScore table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into CollegePaperAndPatentScore (college_id, create_date, ref_score, "
						+ "funds_score, sci_score, pku_score, paper_score, patent_score)"		     			
		     			+ "values (?,CURDATE(),?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE ref_score= ?, funds_score= ?,sci_score =?, pku_score=?,paper_score=?,patent_score=?";
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, clg_id);
				pstm.setDouble(2,ref_norm);
				pstm.setDouble(3,funds_norm);
				pstm.setDouble(4,sci_norm);
				pstm.setDouble(5,pku_norm);
				pstm.setDouble(6,paper_norm);
				pstm.setDouble(7,patent_norm);
				pstm.setDouble(8,ref_norm);
				pstm.setDouble(9,funds_norm);
				pstm.setDouble(10,sci_norm);
				pstm.setDouble(11,pku_norm);
				pstm.setDouble(12,paper_norm);
				pstm.setDouble(13,patent_norm);
				
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
		
		//Calculate normalized score and insert into CollegePaperAndPatentScore table
		public static void updateTalentInputNormZscore(String Name) throws SQLException {
			int clg_id = getCollegIdByName(Name);
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			double[] mean_arr = new double[11];
			double[] dev_arr = new double[11];
			double[] val_arr = new double[11];
			double[] norm_arr = new double[11];
			
			// Get Mean and standard deviation value from table MeanDevRateTA
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select yuanshi_mean, yuanshi_dev, changjiang_mean, changjiang_dev, qingnian_mean,"
						+ " qingnian_dev, master_mean, master_dev, doc_mean, doc_dev, post_doc_mean, post_doc_dev, "
						+ " lab_mean, lab_dev, good_subject_mean, good_subject_dev, nature_prize_mean, nature_prize_dev,"
						+ " social_prize_mean, social_prize_dev, total_prize_mean, total_prize_dev "
	      		        + " from MeanDevRateTA";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					for(int i=0;i<11;i++){
						mean_arr[i] = rs.getDouble(i*2+1);
						dev_arr[i] = rs.getDouble(i*2+2);
					}
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
			
			System.out.println("mean is: "+mean_arr[0]);
			System.out.println("dev is: "+dev_arr[0]);
			// Processing collegedetail info 
			NormalDistribution dist = new NormalDistribution();
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select yuanshi_num, changjiang_num, qingnian_num, master_num, doc_num, "
				 		   + "post_doc_num, lab_num, good_sub_num, nature_prize_num, social_prize_num, "
				 		   + "total_prize_num "
	      		           + " from CollegeDetailNew where college_id = ?";				
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, clg_id);
				rs = pstm.executeQuery();
				while (rs.next()) {
					for(int i=0;i<11;i++){
						val_arr[i] = (double) rs.getInt(i+1);						
						norm_arr[i] = dist.cumulativeProbability( getZscore(val_arr[i], mean_arr[i], dev_arr[i])) *100;
					}
					
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
        
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();
			// Insert normalized scores into  CollegeTalentAndInputScore table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into CollegeTalentAndInputScore (college_id, create_date, yuanshi_score, "
						+ "changjiang_score, qingnian_score, master_score, doc_score, post_doc_score, "
						+ "lab_score, good_subject_score, nature_prize_score, social_prize_score, "
						+ "total_prize_score )"		     			
		     			+ "values (?,CURDATE(),?,?,?,?,?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE yuanshi_score= ?, changjiang_score= ?,qingnian_score =?, master_score=?, "
		     			+ "doc_score=?,post_doc_score=?, lab_score=?, good_subject_score=?, nature_prize_score=?, "
		     			+ "social_prize_score=?, total_prize_score=?";
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, clg_id);
				for(int i=0;i<11;i++){
					pstm.setDouble(i+2, norm_arr[i]);
					pstm.setDouble(i+13, norm_arr[i]);
				}
				
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
						
		
		public static void updateSubPprPttNormZscore(String Name) throws SQLException {
			int clg_id = getCollegIdByName(Name);
			DBinfo dbinfo = DBUtils.getDBinfoById(clg_id);
			String psDbName = dbinfo.getPSName();
			String pprDBName = dbinfo.getDBName();
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			
			Map<String, double[]> map = new HashMap<String,double[]>();

			// Get Mean and standard deviation value from table MeanDevRateSub
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select paper_class_lvl2, ref_mean, ref_dev, sci_mean, sci_dev, "
						   + " pku_mean, pku_dev "
	      		           + " from MeanDevRateSub";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					String sub2 = rs.getString("paper_class_lvl2");
					double ref_mean = rs.getDouble("ref_mean");
					double ref_dev = rs.getDouble("ref_dev");			
					double sci_mean = rs.getDouble("sci_mean");
					double sci_dev = rs.getDouble("sci_dev");
					double pku_mean = rs.getDouble("pku_mean");
					double pku_dev = rs.getDouble("pku_dev");
					// if it is a new sub2, create items in map
					if(!map.containsKey(sub2)){
						double[] arr = new double[6];
						arr[0] = ref_mean;
						arr[1] = ref_dev;
						arr[2] = sci_mean;
						arr[3] = sci_dev;
						arr[4] = pku_mean;
						arr[5] = pku_dev;
						map.put(sub2, arr);
					}
					// find sub2 exists
					else{
						double[] arr = map.get(sub2);
						arr[0] = ref_mean;
						arr[1] = ref_dev;
						arr[2] = sci_mean;
						arr[3] = sci_dev;
						arr[4] = pku_mean;
						arr[5] = pku_dev;
					}
					
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
			
			// Processing paper norm facts
			NormalDistribution dist = new NormalDistribution();
			Map<String,double[]> map_norm = new HashMap<String,double[]>();
			Map<String,String> map_12 = new HashMap<String,String>();
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "select b.paper_class_lvl1 as sub1, b.paper_class_lvl2 as sub2, "
						  + " sum(a.ref_rate) as sum_ref, sum(a.sci_num)+sum(a.ei_num) as sum_sci, "
						  + " SUM(a.istic_num)+SUM(a.cssci_num)+SUM(a.pku_num)  as sum_pku "  
						  + " from "+ pprDBName + " b inner join " + psDbName + " a on a.web_id = b.paper_web_id "
						  + " group by b.paper_class_lvl2";
				pstm = conn.prepareStatement(sql);
				System.out.println(pstm);
				rs = pstm.executeQuery();
				while (rs.next()) {
					String sub1 = rs.getString("sub1");
					String sub2 = rs.getString("sub2");
					if(sub2==null) continue;	
					int ref_rate = rs.getInt("sum_ref");		
					int sci_rate = rs.getInt("sum_sci");
					int pku_rate = rs.getInt("sum_pku");
					
					// Put sub1 and sub2 name in map
					map_12.put(sub2, sub1);
					
					// get mean and dev from the pre-stored array
					double[] arr = map.get(sub2);
					
					// calculate each norms, Store those norm values into double array and push into map_norm
					double[] new_arr = new double[4];
					new_arr[0] = dist.cumulativeProbability( getZscore((double)ref_rate,arr[0],arr[1])) *100;					
					new_arr[1] = dist.cumulativeProbability(getZscore((double)sci_rate,arr[2],arr[3]))*100;
					new_arr[2] = dist.cumulativeProbability(getZscore((double)pku_rate,arr[4],arr[5]))*100;
					new_arr[3] = REF*new_arr[0] + SCI_*new_arr[1] + PKU_*new_arr[2];
					
					map_norm.put(sub2, new_arr);				
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
			
			if (rs != null) rs.close();
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();

			// Insert normalized scores into SubPaperScore table
			for (Entry<String,double[]> entry : map_norm.entrySet()) {
			    String sub2 = entry.getKey();
			    double[] arr = entry.getValue();
			    String sub1 = map_12.get(sub2);
				try {	
					conn  = ConnectionUtils.getConnection();
					String sql = "Insert into SubPaperScore (college_id, create_date, paper_class_lvl1, paper_class_lvl2, ref_score, "
							+ "sci_score, pku_score, overall_score) "		     			
			     			+ "values (?,CURDATE(),?,?,?,?,?,?) "
			     			+ "ON DUPLICATE KEY UPDATE ref_score= ?,sci_score =?, pku_score=?,overall_score=?";
					pstm = conn.prepareStatement(sql);
					pstm.setInt(1, clg_id);
					pstm.setString(2, sub1);
					pstm.setString(3, sub2);
					pstm.setDouble(4,arr[0]);
					pstm.setDouble(5,arr[1]);
					pstm.setDouble(6,arr[2]);
					pstm.setDouble(7,arr[3]);
					pstm.setDouble(8,arr[0]);
					pstm.setDouble(9,arr[1]);
					pstm.setDouble(10,arr[2]);
					pstm.setDouble(11,arr[3]);
				
					pstm.executeUpdate();
		        } catch (SQLException | ClassNotFoundException  e) {
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
			
		}
		
		
		
		
		//Update MeanDev table
		public static void updateMeanDev() throws SQLException {
			double ref_mean = 0;
			double ref_dev = 0;
			double funds_mean = 0;
			double funds_dev = 0;
			double sci_mean = 0;
			double sci_dev = 0;
			double pku_mean = 0;
			double pku_dev = 0;
			double patent_mean = 0;
			double patent_dev = 0;
			
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			// Get all paperScore table name for each college
			List<String> psDBnameList = new ArrayList<String>();
			List<String> pttDBnameList = new ArrayList<String>();
			List<Double> cache_ref = new ArrayList<Double>();
			List<Double> cache_funds = new ArrayList<Double>();
			List<Double> cache_sci = new ArrayList<Double>();
			List<Double> cache_pku = new ArrayList<Double>();
			List<Double> cache_patent = new ArrayList<Double>();
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select  paper_score_name, db_patent_name  "
	      		           + "from DBinfo";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					String psDBname = rs.getString("paper_score_name");
					String pttDBname = rs.getString("db_patent_name");
					psDBnameList.add(psDBname);
					pttDBnameList.add(pttDBname);
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
			
			// For each college, get all paper records and store them in according list
			for(int i=0;i<psDBnameList.size();i++){
				String psDBname = psDBnameList.get(i);
				String pttDBname = pttDBnameList.get(i);
				// Get Paper
				try {	
					conn  = ConnectionUtils.getConnection();
					String sql = "Select sum(ref_rate) AS sum_ref, sum(funds_rate) AS sum_funds, "
							  + " SUM(sci_num)+SUM(ei_num)  as sum_sci, "
							  + " SUM(istic_num)+SUM(cssci_num)+SUM(pku_num)  as sum_pku "  
							  + " from "+psDBname;
					pstm = conn.prepareStatement(sql);
					System.out.println(pstm);
					rs = pstm.executeQuery();
					while (rs.next()) {
						int sum_ref = rs.getInt("sum_ref");
						int sum_funds = rs.getInt("sum_funds");
						int sum_sci = rs.getInt("sum_sci");
						int sum_pku = rs.getInt("sum_pku");
						cache_ref.add((double)sum_ref);
						cache_funds.add((double)sum_funds);
						cache_sci.add((double)sum_sci);
						cache_pku.add((double)sum_pku);
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
				
				if (rs != null) rs.close();
				if (pstm != null) pstm.close();
				if (conn != null) conn.close();
				
				//Get patent      
				try {	
					conn  = ConnectionUtils.getConnection();
					String sql = "Select count(*) AS count "		      		    
							+ "from "+pttDBname;
					pstm = conn.prepareStatement(sql);
					rs = pstm.executeQuery();
					while (rs.next()) {
						int sum_ptt = rs.getInt("count");						
						cache_patent.add((double)sum_ptt);
					}
		        } catch (SQLException | ClassNotFoundException  e) {
		            e.printStackTrace();           
		        }finally { 
		        	try { if (rs != null) rs.close(); } catch (Exception e) {};
		            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
		            if (conn != null) {
		                try {
		                    conn.close();
		                } catch (SQLException e) { }
		            }
		        }
		        
		        
			}
			
			if (rs != null) rs.close();
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();
			
			//Convert double arraylist to double array
			double[] ref_arr = new double[cache_ref.size()];
			for(int i=0;i<cache_ref.size();i++) ref_arr[i] = cache_ref.get(i);
			
			double[] funds_arr = new double[cache_funds.size()];
			for(int i=0;i<cache_funds.size();i++) funds_arr[i] = cache_funds.get(i);
			
			double[] sci_arr = new double[cache_sci.size()];
			for(int i=0;i<cache_sci.size();i++) sci_arr[i] = cache_sci.get(i);
			
			double[] pku_arr = new double[cache_pku.size()];
			for(int i=0;i<cache_pku.size();i++) pku_arr[i] = cache_pku.get(i);
			
			double[] patent_arr = new double[cache_patent.size()];
			for(int i=0;i<cache_patent.size();i++) patent_arr[i] = cache_patent.get(i);
			
			//Calculate median and standard deviation
			Mean mean_ref_obj = new Mean();
			StandardDeviation dev_ref_obj = new StandardDeviation();			
			ref_mean = mean_ref_obj.evaluate(ref_arr);
			ref_dev  = dev_ref_obj.evaluate(ref_arr);
			
			Mean mean_funds_obj = new Mean();
			StandardDeviation dev_funds_obj = new StandardDeviation();			
			funds_mean = mean_funds_obj.evaluate(funds_arr);
			funds_dev  = dev_funds_obj.evaluate(funds_arr);
			
			Mean mean_sci_obj = new Mean();
			StandardDeviation dev_sci_obj = new StandardDeviation();			
			sci_mean = mean_sci_obj.evaluate(sci_arr);
			sci_dev  = dev_sci_obj.evaluate(sci_arr);
			
			Mean mean_pku_obj = new Mean();
			StandardDeviation dev_pku_obj = new StandardDeviation();			
			pku_mean = mean_pku_obj.evaluate(pku_arr);
			pku_dev  = dev_pku_obj.evaluate(pku_arr);
			
			Mean mean_patent_obj = new Mean();
			StandardDeviation dev_patent_obj = new StandardDeviation();			
			patent_mean = mean_patent_obj.evaluate(patent_arr);
			patent_dev  = dev_patent_obj.evaluate(patent_arr);

			//Insert Into MeanDev table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into MeanDevRate (id, ref_mean, ref_dev, funds_mean, funds_dev, sci_mean, sci_dev, pku_mean, pku_dev, patent_mean, patent_dev)"		     			
		     			+ "values (1,?,?,?,?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE ref_mean= ?, ref_dev= ?,funds_mean =?, funds_dev=?, sci_mean=?,sci_dev=?,pku_mean=?,pku_dev=?,patent_mean=?,patent_dev=?";
				pstm = conn.prepareStatement(sql);
				pstm.setDouble(1, ref_mean);
				pstm.setDouble(2, ref_dev);
				pstm.setDouble(3, funds_mean);
				pstm.setDouble(4, funds_dev);
				pstm.setDouble(5, sci_mean);
				pstm.setDouble(6, sci_dev);
				pstm.setDouble(7, pku_mean);
				pstm.setDouble(8, pku_dev);	
				pstm.setDouble(9, patent_mean);
				pstm.setDouble(10, patent_dev);
				pstm.setDouble(11, ref_mean);
				pstm.setDouble(12, ref_dev);
				pstm.setDouble(13, funds_mean);
				pstm.setDouble(14, funds_dev);
				pstm.setDouble(15, sci_mean);
				pstm.setDouble(16, sci_dev);
				pstm.setDouble(17, pku_mean);
				pstm.setDouble(18, pku_dev);	
				pstm.setDouble(19, patent_mean);
				pstm.setDouble(20, patent_dev);
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) {}
	            }
	        }
	
						
		}		
		
		//Update MeanDev table for ColelgeTalentAndInput table
		public static void updateMeanDevTA() throws SQLException {
			double[] mean_arr = new double[11];
			double[] dev_arr = new double[11];
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			
			List<Double> cache_yuanshi = new ArrayList<Double>();
			List<Double> cache_changjiang = new ArrayList<Double>();
			List<Double> cache_qingnian = new ArrayList<Double>();
			List<Double> cache_master = new ArrayList<Double>();
			List<Double> cache_doc = new ArrayList<Double>();
			List<Double> cache_post_doc = new ArrayList<Double>();
			List<Double> cache_lab = new ArrayList<Double>();
			List<Double> cache_good_sub = new ArrayList<Double>();
			List<Double> cache_nature_prize = new ArrayList<Double>();
			List<Double> cache_social_prize = new ArrayList<Double>();
			List<Double> cache_total_prize = new ArrayList<Double>();
			
			// For each college, get all related talent and input score
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select yuanshi_num, changjiang_num, qingnian_num, master_num, doc_num, "
				 		   + "post_doc_num, lab_num, good_sub_num, nature_prize_num, social_prize_num, "
				 		   + "total_prize_num "
	      		           + " from CollegeDetailNew";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					int sum_yuanshi = rs.getInt("yuanshi_num");
					int sum_changjiang = rs.getInt("changjiang_num");
					int sum_qingnian = rs.getInt("qingnian_num");
					int sum_master = rs.getInt("master_num");
					int sum_doc = rs.getInt("doc_num");
					int sum_post_doc = rs.getInt("post_doc_num");
					int sum_lab = rs.getInt("lab_num");
					int sum_good_subject = rs.getInt("good_sub_num");
					int sum_nature_prize = rs.getInt("nature_prize_num");
					int sum_social_prize = rs.getInt("social_prize_num");
					int sum_total_prize = rs.getInt("total_prize_num");
					cache_yuanshi.add((double)sum_yuanshi);
					cache_changjiang.add((double)sum_changjiang);
					cache_qingnian.add((double)sum_qingnian);
					cache_master.add((double)sum_master);
					cache_doc.add((double)sum_doc);
					cache_post_doc.add((double)sum_post_doc);
					cache_lab.add((double)sum_lab);
					cache_good_sub.add((double)sum_good_subject);
					cache_nature_prize.add((double)sum_nature_prize);
					cache_social_prize.add((double)sum_social_prize);
					cache_total_prize.add((double)sum_total_prize);
					
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
			
			//Convert double arraylist to double array
			double[] yuanshi_arr = new double[cache_yuanshi.size()];
			for(int i=0;i<cache_yuanshi.size();i++) yuanshi_arr[i] = cache_yuanshi.get(i);
			
			double[] changjiang_arr = new double[cache_changjiang.size()];
			for(int i=0;i<cache_changjiang.size();i++) changjiang_arr[i] = cache_changjiang.get(i);
			
			double[] qingnian_arr = new double[cache_qingnian.size()];
			for(int i=0;i<cache_qingnian.size();i++) qingnian_arr[i] = cache_qingnian.get(i);
			
			double[] master_arr = new double[cache_master.size()];
			for(int i=0;i<cache_master.size();i++) master_arr[i] = cache_master.get(i);
		
			double[] doc_arr = new double[cache_doc.size()];
			for(int i=0;i<cache_doc.size();i++) doc_arr[i] = cache_doc.get(i);
			
			double[] post_doc_arr = new double[cache_post_doc.size()];
			for(int i=0;i<cache_post_doc.size();i++) post_doc_arr[i] = cache_post_doc.get(i);
			
			double[] lab_arr = new double[cache_lab.size()];
			for(int i=0;i<cache_lab.size();i++) lab_arr[i] = cache_lab.get(i);
			
			double[] good_sub_arr = new double[cache_good_sub.size()];
			for(int i=0;i<cache_good_sub.size();i++) good_sub_arr[i] = cache_good_sub.get(i);
			
			double[] nature_prize_arr = new double[cache_nature_prize.size()];
			for(int i=0;i<cache_nature_prize.size();i++) nature_prize_arr[i] = cache_nature_prize.get(i);
			
			double[] social_prize_arr = new double[cache_social_prize.size()];
			for(int i=0;i<cache_social_prize.size();i++) social_prize_arr[i] = cache_social_prize.get(i);
			
			double[] total_prize_arr = new double[cache_total_prize.size()];
			for(int i=0;i<cache_total_prize.size();i++) total_prize_arr[i] = cache_total_prize.get(i);
			
			
			//Calculate median and standard deviation
			Mean mean_obj= new Mean();
			StandardDeviation dev_obj = new StandardDeviation();
			mean_arr[0] = mean_obj.evaluate(yuanshi_arr);
			dev_arr[0] = dev_obj.evaluate(yuanshi_arr);
			
			mean_arr[1] = mean_obj.evaluate(changjiang_arr);
			dev_arr[1] = dev_obj.evaluate(changjiang_arr);
			
			mean_arr[2] = mean_obj.evaluate(qingnian_arr);
			dev_arr[2] = dev_obj.evaluate(qingnian_arr);
			
			mean_arr[3] = mean_obj.evaluate(master_arr);
			dev_arr[3] = dev_obj.evaluate(master_arr);
			
			mean_arr[4] = mean_obj.evaluate(doc_arr);
			dev_arr[4] = dev_obj.evaluate(doc_arr);
			
			mean_arr[5] = mean_obj.evaluate(post_doc_arr);
			dev_arr[5] = dev_obj.evaluate(post_doc_arr);
			
			mean_arr[6] = mean_obj.evaluate(lab_arr);
			dev_arr[6] = dev_obj.evaluate(lab_arr);
			
			mean_arr[7] = mean_obj.evaluate(good_sub_arr);
			dev_arr[7] = dev_obj.evaluate(good_sub_arr);
			
			mean_arr[8] = mean_obj.evaluate(nature_prize_arr);
			dev_arr[8] = dev_obj.evaluate(nature_prize_arr);
			
			mean_arr[9] = mean_obj.evaluate(social_prize_arr);
			dev_arr[9] = dev_obj.evaluate(social_prize_arr);
			
			mean_arr[10] = mean_obj.evaluate(total_prize_arr);
			dev_arr[10] = dev_obj.evaluate(total_prize_arr);
			
		//Insert Into MeanDev table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into MeanDevRateTA (id, yuanshi_mean, yuanshi_dev, changjiang_mean, changjiang_dev,"
						+ " qingnian_mean, qingnian_dev, master_mean, master_dev, doc_mean, doc_dev, post_doc_mean, post_doc_dev, "
						+ " lab_mean, lab_dev, good_subject_mean, good_subject_dev, nature_prize_mean, nature_prize_dev, "
						+ " social_prize_mean, social_prize_dev, total_prize_mean, total_prize_dev) "		     			
		     			+ " values (1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
		     			+ " ON DUPLICATE KEY UPDATE yuanshi_mean= ?, yuanshi_dev= ?,changjiang_mean =?, changjiang_dev=?, "
		     			+ " qingnian_mean=?,qingnian_dev=?,master_mean=?,master_dev=?,doc_mean=?,doc_dev=?, "
		     			+ " post_doc_mean=?, post_doc_dev=?, lab_mean=?, lab_dev=?, good_subject_mean=?, good_subject_dev=?, "
		     			+ " nature_prize_mean=?, nature_prize_dev=?, social_prize_mean=?, social_prize_dev=?, total_prize_mean=?, total_prize_dev=? ";
				pstm = conn.prepareStatement(sql);
				for(int i=0;i<11;i++){
					pstm.setDouble(i*2+1, mean_arr[i]);
					pstm.setDouble(i*2+2, dev_arr[i]);
					pstm.setDouble(i*2+23, mean_arr[i]);
					pstm.setDouble(i*2+24, dev_arr[i]);
				}			
				System.out.println(pstm);
				pstm.executeUpdate();
				
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) {}
	            }
	        }
	
						
		}
		
	//Update MeanDevSUB table
	public static void updateMeanDevSub() throws SQLException {
		double ref_mean = 0;
		double ref_dev = 0;
		double sci_mean = 0;
		double sci_dev = 0;
		double pku_mean = 0;
		double pku_dev = 0;
		
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		// Get all paperScore table name for each college
		List<String> psDBnameList = new ArrayList<String>();
		List<String> pprDBnameList = new ArrayList<String>();
		try {	
			conn  = ConnectionUtils.getConnection();
			String sql = "Select  paper_score_name, db_name "
      		           + "from DBinfo";
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				String psDBname = rs.getString("paper_score_name");
				String pprDBname = rs.getString("db_name");
				psDBnameList.add(psDBname);
				pprDBnameList.add(pprDBname);
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
		Map<String,ArrayList<ArrayList<Double>>> bigMap = new HashMap<String,ArrayList<ArrayList<Double>>>();
		// For each college, get all paper records and store them in according list
		for(int i=0;i<psDBnameList.size();i++){
			String psDBname = psDBnameList.get(i);
			String pprDBname = pprDBnameList.get(i);
			// Get Paper for each subject 
			try {	
				conn  = ConnectionUtils.getConnection();
				//select b.paper_class_lvl1 as sub1, b.paper_class_lvl2 as sub2, sum(a.ref_rate) as ref, sum(a.sci_num)+sum(a.ei_num) as sci, SUM(a.istic_num)+SUM(a.cssci_num)+SUM(a.pku_num)  as sum_pku				
				//from PaperBUPT b inner join PaperScoreBUPT a on a.web_id = b.paper_web_id group by b.paper_class_lvl2;
				String sql = "select b.paper_class_lvl1 as sub1, b.paper_class_lvl2 as sub2, "
						  + " sum(a.ref_rate) as sum_ref, sum(a.sci_num)+sum(a.ei_num) as sum_sci, "
						  + " SUM(a.istic_num)+SUM(a.cssci_num)+SUM(a.pku_num)  as sum_pku "  
						  + " from "+ pprDBname + " b inner join " + psDBname + " a on a.web_id = b.paper_web_id "
						  + " group by b.paper_class_lvl2";
						
				pstm = conn.prepareStatement(sql);
				System.out.println(pstm);
				rs = pstm.executeQuery();
				while (rs.next()) {
					String sub2 = rs.getString("sub2");
					if(sub2==null) continue;
					int sum_ref = rs.getInt("sum_ref");
					int sum_sci = rs.getInt("sum_sci");
					int sum_pku = rs.getInt("sum_pku");
					// If this is a new subject create new map entry
					if(!bigMap.containsKey(sub2)){
						ArrayList<ArrayList<Double>> bigList = new ArrayList<ArrayList<Double>>();
						ArrayList<Double> list1 = new ArrayList<Double>();
						list1.add((double)sum_ref);
						bigList.add(list1);
						System.out.println("Adding ");
						ArrayList<Double> list2 = new ArrayList<Double>();
						list2.add((double)sum_sci);
						bigList.add(list2);
						ArrayList<Double> list3 = new ArrayList<Double>();
						list3.add((double)sum_pku);
						bigList.add(list3);
						System.out.println("Adding ");
						
						bigMap.put(sub2, bigList);
					}
					// If not a new subject, fetch list from Map entry
					else{
						ArrayList<ArrayList<Double>> bigList = bigMap.get(sub2);
						ArrayList<Double> list1 = bigList.get(0);
						list1.add((double)sum_ref);
						ArrayList<Double> list2 = bigList.get(1);
						list2.add((double)sum_sci);
						ArrayList<Double> list3 = bigList.get(2);
						list3.add((double)sum_pku);
					}
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
			
			if (rs != null) rs.close();
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();	        
		}
		
		//For each subject 2 , calculate the norm scores
		for (Entry<String, ArrayList<ArrayList<Double>>> entry : bigMap.entrySet()) {
		    String sub2 = entry.getKey();
		    ArrayList<ArrayList<Double>> bigList = entry.getValue();
		    ArrayList<Double> list1 = bigList.get(0);
		    ArrayList<Double> list2 = bigList.get(1);
		    ArrayList<Double> list3 = bigList.get(2);
		    
		    //Convert double arraylist to double array
			double[] ref_arr = new double[list1.size()];
			for(int i=0;i<list1.size();i++) ref_arr[i] = list1.get(i);
			
			double[] sci_arr = new double[list2.size()];
			for(int i=0;i<list2.size();i++) sci_arr[i] = list2.get(i);
			
			double[] pku_arr = new double[list3.size()];
			for(int i=0;i<list3.size();i++) pku_arr[i] = list3.get(i);
			
			//Calculate median and standard deviation
			Mean mean_ref_obj = new Mean();
			StandardDeviation dev_ref_obj = new StandardDeviation();
			
			ref_mean = mean_ref_obj.evaluate(ref_arr);
			ref_dev  = dev_ref_obj.evaluate(ref_arr);
			sci_mean = mean_ref_obj.evaluate(sci_arr);
			sci_dev  = dev_ref_obj.evaluate(sci_arr);
			pku_mean = mean_ref_obj.evaluate(pku_arr);
			pku_dev  = dev_ref_obj.evaluate(pku_arr);
			
			//Insert Into MeanDevSub table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into MeanDevRateSub (paper_class_lvl2, ref_mean, ref_dev, "
						+ "sci_mean, sci_dev, pku_mean, pku_dev) "		     			
		     			+ "values (?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE ref_mean= ?, ref_dev= ?, "
		     			+ "sci_mean=?,sci_dev=?,pku_mean=?,pku_dev=?";
				pstm = conn.prepareStatement(sql);
				pstm.setString(1,sub2);
				pstm.setDouble(2, ref_mean);
				pstm.setDouble(3, ref_dev);
				pstm.setDouble(4, sci_mean);
				pstm.setDouble(5, sci_dev);
				pstm.setDouble(6, pku_mean);
				pstm.setDouble(7, pku_dev);	
				pstm.setDouble(8, ref_mean);
				pstm.setDouble(9, ref_dev);
				pstm.setDouble(10, sci_mean);
				pstm.setDouble(11, sci_dev);
				pstm.setDouble(12, pku_mean);
				pstm.setDouble(13, pku_dev);	
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) {}
	            }
	        }	
		}
					
	}
		
	
	public static double getZeroOneNorm(int value, int max, int min){
		if(max==min) return 0.0;
		return (double)(value-min)/(max-min);
	}
	
	public static double getZscore(double value, double mean, double dev){
		if(dev==0) return 1.0;
		return (value-mean)/dev;
	}
	
	
	
	
	//-------------------------------------------LECACY---------------------------------------------------------------
	
	//Convert table from xls to mysql
	public static void convertXLS() throws SQLException{
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		ArrayList<String> ql = new ArrayList<String>();
		ArrayList<Integer> bl = new ArrayList<Integer>();
		ArrayList<Integer> cl = new ArrayList<Integer>();
		ArrayList<Integer> dl = new ArrayList<Integer>();
		ArrayList<Integer> el = new ArrayList<Integer>();
		ArrayList<Integer> fl = new ArrayList<Integer>();
		ArrayList<Integer> gl = new ArrayList<Integer>();
		ArrayList<Integer> hl = new ArrayList<Integer>();
		ArrayList<Integer> jl = new ArrayList<Integer>();
		ArrayList<Integer> kl = new ArrayList<Integer>();
		ArrayList<Integer> ll = new ArrayList<Integer>();
		ArrayList<Integer> pl = new ArrayList<Integer>();
		ArrayList<Integer> idl = new ArrayList<Integer>();
		
		try {	
			conn  = ConnectionUtils.getConnection();
			String sql = "Select t2.college_id, t1.q ,t1.b, t1.c ,t1.d ,t1.e ,t1.f ,t1.g ,t1.h, t1.j, t1.k, t1.l, t1.p "
      		           + " from test1 t1 "
      		           + " INNER JOIN College t2 "
      		           + " ON t1.q = t2.college_title";
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				int cid = rs.getInt("college_id");
				idl.add(cid);
				System.out.println("Find collegid : "+cid);
				ql.add(rs.getString("q").trim());
				bl.add(rs.getInt("b")); 
				cl.add(rs.getInt("c"));
				dl.add(rs.getInt("d"));
				el.add(rs.getInt("e"));
				fl.add(rs.getInt("f"));
				gl.add(rs.getInt("g"));
				hl.add(rs.getInt("h"));
				jl.add(rs.getInt("j"));
				kl.add(rs.getInt("k"));
				ll.add(rs.getInt("l"));
				pl.add(rs.getInt("p"));
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
		
		
	    
	   
		
		double[] rdPctg_score_arr = new double[idl.size()];
		double[] snrPctg_score_arr = new double[idl.size()];
		double[] rdMoney_score_arr = new double[idl.size()];
		double[] rdMoneyPp_score_arr = new double[idl.size()];
		double[] projectNum_score_arr = new double[idl.size()];
		double[] dealNum_score_arr = new double[idl.size()];
		double[] dealMoney_score_arr = new double[idl.size()];
		
		for(int i=0;i<idl.size();i++){
			int clg_id = idl.get(i);
			double rdPctg_score = el.get(i)>0? (double) hl.get(i)/ (double)el.get(i) : 0;
			double snrPctg_score = el.get(i)>0? (double) gl.get(i)/ (double)el.get(i) : 0;
			double rdMoney_score = (double) jl.get(i);
			double rdMoneyPp_score = hl.get(i) >0? (double) jl.get(i)/ (double)hl.get(i): 0;
			double projectNum_score = (double) kl.get(i);
			double dealNum_score = (double) ll.get(i);
			double dealMoney_score = (double) pl.get(i);
			rdPctg_score_arr[i] = rdPctg_score;
			snrPctg_score_arr[i] = snrPctg_score;
			rdMoney_score_arr[i] = rdMoney_score;
			rdMoneyPp_score_arr[i] = rdMoneyPp_score;
			projectNum_score_arr[i] = projectNum_score;
			dealNum_score_arr[i] = dealNum_score;
			dealMoney_score_arr[i] = dealMoney_score;
			
			
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into CollegeOtherPre (college_id, create_date, rdPctg_score, "
						+ "snrPctg_score, rdMoney_score, rdMoneyPp_score, projectNum_score, dealNum_score, dealMoney_score)"		     			
		     			+ "values (?,CURDATE(),?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE rdPctg_score= ?, snrPctg_score= ?,rdMoney_score =?, rdMoneyPp_score=?,projectNum_score=?, "
		     			+ "dealNum_score=?, dealMoney_score=? ";
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, clg_id);
				pstm.setDouble(2,rdPctg_score);
				pstm.setDouble(3,snrPctg_score);
				pstm.setDouble(4,rdMoney_score);
				pstm.setDouble(5,rdMoneyPp_score);
				pstm.setDouble(6,projectNum_score);
				pstm.setDouble(7,dealNum_score);
				pstm.setDouble(8,dealMoney_score);
				pstm.setDouble(9,rdPctg_score);
				pstm.setDouble(10,snrPctg_score);
				pstm.setDouble(11,rdMoney_score);
				pstm.setDouble(12,rdMoneyPp_score);
				pstm.setDouble(13,projectNum_score);
				pstm.setDouble(14,dealNum_score);
				pstm.setDouble(15,dealMoney_score);
				
				System.out.println(pstm);
				
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
		
		double[] mean_arr = new double[7];
		double[] dev_arr = new double[7];
		
		Mean mean_obj= new Mean();
		StandardDeviation dev_obj = new StandardDeviation();
		mean_arr[0] = mean_obj.evaluate(rdPctg_score_arr);
		dev_arr[0] = dev_obj.evaluate(rdPctg_score_arr);
		
		mean_arr[1] = mean_obj.evaluate(snrPctg_score_arr);
		dev_arr[1] = dev_obj.evaluate(snrPctg_score_arr);
		
		mean_arr[2] = mean_obj.evaluate(rdMoney_score_arr);
		dev_arr[2] = dev_obj.evaluate(rdMoney_score_arr);
		
		mean_arr[3] = mean_obj.evaluate(rdMoneyPp_score_arr);
		dev_arr[3] = dev_obj.evaluate(rdMoneyPp_score_arr);
		
		mean_arr[4] = mean_obj.evaluate(projectNum_score_arr);
		dev_arr[4] = dev_obj.evaluate(projectNum_score_arr);
		
		mean_arr[5] = mean_obj.evaluate(dealNum_score_arr);
		dev_arr[5] = dev_obj.evaluate(dealNum_score_arr);
		
		mean_arr[6] = mean_obj.evaluate(dealMoney_score_arr);
		dev_arr[6] = dev_obj.evaluate(dealMoney_score_arr);
		
		
		//Insert Into table
		NormalDistribution dist = new NormalDistribution();
		for(int i=0;i<idl.size();i++){
			int clg_id = idl.get(i);
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into CollegeOtherScore (college_id, create_date, rdPctg_score, "
						+ "snrPctg_score, rdMoney_score, rdMoneyPp_score, projectNum_score, dealNum_score, dealMoney_score)"		     			
		     			+ "values (?,CURDATE(),?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE rdPctg_score= ?, snrPctg_score= ?,rdMoney_score =?, rdMoneyPp_score=?,projectNum_score=?, "
		     			+ "dealNum_score=?, dealMoney_score=? ";
				pstm = conn.prepareStatement(sql);
				
				pstm.setInt(1, clg_id);
				
				
				System.out.println("mean1 =:" + mean_arr[0] );
				System.out.println("dev =: " + dev_arr[0] );
				System.out.println("val =: " + rdPctg_score_arr[i] );
				
				double val_0 = dist.cumulativeProbability( getZscore(rdPctg_score_arr[i], mean_arr[0], dev_arr[0])) *100;
				double val_1 = dist.cumulativeProbability( getZscore(snrPctg_score_arr[i], mean_arr[1], dev_arr[1])) *100;
				double val_2 = dist.cumulativeProbability( getZscore(rdMoney_score_arr[i], mean_arr[2], dev_arr[2])) *100;
				double val_3 = dist.cumulativeProbability( getZscore(rdMoneyPp_score_arr[i], mean_arr[3], dev_arr[3])) *100;
				double val_4 = dist.cumulativeProbability( getZscore(projectNum_score_arr[i], mean_arr[4], dev_arr[4])) *100;
				double val_5 = dist.cumulativeProbability( getZscore(dealNum_score_arr[i], mean_arr[5], dev_arr[5])) *100;
				double val_6 = dist.cumulativeProbability( getZscore(dealMoney_score_arr[i], mean_arr[6], dev_arr[6])) *100;
				
				pstm.setDouble(2, val_0);
				pstm.setDouble(3, val_1);
				pstm.setDouble(4, val_2);
				pstm.setDouble(5, val_3);
				pstm.setDouble(6, val_4);
				pstm.setDouble(7, val_5);
				pstm.setDouble(8, val_6);
				pstm.setDouble(9, val_0);
				pstm.setDouble(10, val_1);
				pstm.setDouble(11, val_2);
				pstm.setDouble(12, val_3);
				pstm.setDouble(13, val_4);
				pstm.setDouble(14, val_5);
				pstm.setDouble(15, val_6);
							
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
		
		
		
	}
	
	//Calculate normalized score and insert into CollegePaperAndPatentScore table
		public static void updateNorm(String Name) throws SQLException {
			int clg_id = getCollegIdByName(Name);
			DBinfo dbinfo = DBUtils.getDBinfoById(clg_id);
			String psDbName = dbinfo.getPSName();
			String pttDBName = dbinfo.getDBPatentName();
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			int ref_max = -1;
			int ref_min = -1;
			int funds_max = -1;
			int funds_min = -1;
			int record_max = -1;
			int record_min = -1;
			int source_max = -1;
			int source_min = -1;
			int patent_max = -1;
			int patent_min = -1;
			double ref_norm = 0;
			double funds_norm = 0;
			double record_norm = 0;
			double source_norm = 0;
			double total_norm = 0;
			double patent_norm = 0;
	
			// Get MAX min value from table MAXMINRATE
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select ref_max, ref_min, funds_max, funds_min, record_max, record_min, source_max, source_min, ptt_max, ptt_min"
	      		           + " from MaxMinRate";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					ref_max = rs.getInt("ref_max");
					ref_min = rs.getInt("ref_min");
					funds_max = rs.getInt("funds_max");
					funds_min = rs.getInt("funds_min");
					record_max = rs.getInt("record_max");
					record_min = rs.getInt("record_min");
					source_max = rs.getInt("source_max");
					source_min = rs.getInt("source_min");
					patent_max = rs.getInt("ptt_max");
					patent_min = rs.getInt("ptt_min");
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
			
			// Processing paper norm facts
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select SUM(ref_rate) as ref_sum, SUM(funds_rate) as funds_sum, SUM(record_rate) as record_sum, SUM(source_rate) as source_sum "
	      		           + "from "+ psDbName;
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					int ref_rate = rs.getInt("ref_sum");
					int funds_rate = rs.getInt("funds_sum");
					int record_rate = rs.getInt("record_sum");
					int source_rate = rs.getInt("source_sum");
					System.out.println("value is: "+ref_rate);
					System.out.println("max is: "+ref_max);
					System.out.println("min is: "+ref_min);
					System.out.println(" after value is: "+getZeroOneNorm(ref_rate,ref_max,ref_min));
					ref_norm = getZeroOneNorm(ref_rate,ref_max,ref_min)*100;
					funds_norm = getZeroOneNorm(funds_rate,funds_max,funds_min)*100;
					record_norm = getZeroOneNorm(record_rate,record_max,record_min)*100;
					source_norm = 0;   //No source given
					//total_norm = REF*ref_norm + FUNDS*funds_norm + RECORD*record_norm + SOURCE*source_norm;
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
			
			
			//Processing patent norm facts  
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select COUNT(*) as count "
	      		           + "from "+pttDBName;
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					int ptt_num = rs.getInt("count");
					patent_norm = getZeroOneNorm(ptt_num,patent_max,patent_min)*100;
				}
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) {}
	            }
	        }
			
	        
	        
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();
			// Insert normalized scores into CollegePaperAndPatentScore table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into CollegePaperAndPatentScore (college_id, create_date, ref_score, "
						+ "funds_score, record_score, source_score, paper_score, patent_score)"		     			
		     			+ "values (?,CURDATE(),?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE ref_score= ?, funds_score= ?,record_score =?, source_score=?,paper_score=?,patent_score=?";
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, clg_id);
				pstm.setDouble(2,ref_norm);
				pstm.setDouble(3,funds_norm);
				pstm.setDouble(4,record_norm);
				pstm.setDouble(5,source_norm);
				pstm.setDouble(6,total_norm);
				pstm.setDouble(7,patent_norm);
				pstm.setDouble(8,ref_norm);
				pstm.setDouble(9,funds_norm);
				pstm.setDouble(10,record_norm);
				pstm.setDouble(11,source_norm);
				pstm.setDouble(12,total_norm);
				pstm.setDouble(13,patent_norm);
				
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
		
		//Calculate normalized score and insert into CollegeTalentAndInputScore table
		public static void updateNormTA(String Name) throws SQLException {
			int clg_id = getCollegIdByName(Name);
			DBinfo dbinfo = DBUtils.getDBinfoById(clg_id);
			String psDbName = dbinfo.getPSName();
			String pttDBName = dbinfo.getDBPatentName();
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			int[] max_arr = new int[11];
			int[] min_arr = new int[11];
			int[] val_arr = new int[11];
			double[] norm_arr = new double[11];
			
			// Get Max Min value from table itself
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select max(yuanshi_num),min(yuanshi_num), max(changjiang_num), min(changjiang_num), max(qingnian_num),min(qingnian_num), max(master_num), min(master_num),max(doc_num),min(doc_num), "
				 		   + "max(post_doc_num), min(post_doc_num),max(lab_num),min(lab_num), max(good_sub_num), min(good_sub_num),max(nature_prize_num),min(nature_prize_num), max(social_prize_num),min(social_prize_num), "
				 		   + "max(total_prize_num), min(total_prize_num) "
	      		           + " from CollegeDetailNew";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {											
					List<Integer> rsList = new ArrayList<Integer>();
					for(int i=0;i<22;i++){
						rsList.add(rs.getInt(i+1));  //SQL getInt() starts from index 1
					}
					// put odd elements into max_arr, even elements into min_arr
					for(int i=0;i<11;i++){
						max_arr[i] = rsList.get(i*2);
						min_arr[i] = rsList.get(i*2+1);
					}			
							
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

			/*   <-----LEGACY
			// Get MAX min value from table MAXMINRATETA
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select yuanshi_max, yuanshi_min, changjiang_max, changjiang_min, qingnian_max, qingnian_min, master_max, master_min, doc_max, doc_min, "
				 		   + "post_doc_max, post_doc_min, lab_max, lab_min, good_subject_max, good_subject_min, nature_prize_max, nature_prize_min, social_prize_max, social_prize_min, "
				 		   + "total_prize_max, total_prize_min "
	      		           + " from MaxMinRateTA";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					List<Integer> rsList = new ArrayList<Integer>();
					for(int i=0;i<22;i++){
						rsList.add(rs.getInt(i+1));  //SQL getInt() starts from index 1
					}
					// put odd elements into max_arr, even elements into min_arr
					for(int i=0;i<11;i++){
						max_arr[i] = rsList.get(i*2);
						min_arr[i] = rsList.get(i*2+1);
					}						
				}
	        } catch (SQLException | ClassNotFoundException  e) {
	            e.printStackTrace();           
	        }finally { 
	        	try { if (rs != null) rs.close(); } catch (Exception e) {};
	            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
	            if (conn != null) {
	                try {
	                    conn.close();
	                } catch (SQLException e) { }
	            }
	        }
	        */
			
			// Processing individual norm facts
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select yuanshi_num, changjiang_num, qingnian_num, master_num,doc_num, "
				 		   + "post_doc_num, lab_num, good_sub_num, nature_prize_num, social_prize_num, "
				 		   + "total_prize_num "
	      		           + "from CollegeDetailNew where college_id= "+clg_id;
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					for(int i=0;i<11;i++){
						val_arr[i] = rs.getInt(i+1);//SQL getInt() starts from index 1
						System.out.println("val is: "+val_arr[i]);
						System.out.println("max is: "+max_arr[i]);
						System.out.println("min is: "+min_arr[i]);
						norm_arr[i] = getZeroOneNorm(val_arr[i],max_arr[i],min_arr[i])*100;
					}
					
					//total_norm = REF*ref_norm + FUNDS*funds_norm + RECORD*record_norm + SOURCE*source_norm;
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
			      
			if (pstm != null) pstm.close();
			if (conn != null) conn.close();
			// Insert normalized scores into CollegePaperAndPatentScore table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into CollegeTalentAndInputScore (college_id, create_date, yuanshi_score, "
						+ "changjiang_score, qingnian_score, master_score, doc_score, post_doc_score, lab_score, "
						+ "good_subject_score, nature_prize_score, social_prize_score, total_prize_score )"		     			
		     			+ "values (?,CURDATE(),?,?,?,?,?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE yuanshi_score= ?, changjiang_score= ?,qingnian_score =?, master_score=?,doc_score=?,post_doc_score=?,"
		     			+ "lab_score=?, good_subject_score=?, nature_prize_score=?, social_prize_score=?,total_prize_score=? ";
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, clg_id);
				for(int i=2;i<13;i++){
					pstm.setDouble(i, norm_arr[i-2]);
				}
				for(int i=13;i<24;i++){
					pstm.setDouble(i, norm_arr[i-13]);
				}					
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
		
		//Update MaxMin table
		public static void updateMaxMin() throws SQLException {
			int ref_max = 0;
			int ref_min = Integer.MAX_VALUE;
			int funds_max = 0;
			int funds_min = Integer.MAX_VALUE;
			int record_max = 0;
			int record_min = Integer.MAX_VALUE;
			int source_max = 0;
			int source_min = 0;
			int ptt_max = 0;
			int ptt_min = Integer.MAX_VALUE;
			
			Connection conn = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;
			// Get all paperScore table name for each college
			List<String> psDBnameList = new ArrayList<String>();
			List<String> pttDBnameList = new ArrayList<String>();
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Select  paper_score_name, db_patent_name  "
	      		           + "from DBinfo";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					String psDBname = rs.getString("paper_score_name");
					String pttDBname = rs.getString("db_patent_name");
					psDBnameList.add(psDBname);
					pttDBnameList.add(pttDBname);
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
			
			// For each college, get all paper records and compare with max,min value
			for(int i=0;i<psDBnameList.size();i++){
				String psDBname = psDBnameList.get(i);
				String pttDBname = pttDBnameList.get(i);
				// Get Paper
				try {	
					conn  = ConnectionUtils.getConnection();
					String sql = "Select sum(ref_rate) AS sum_ref, sum(funds_rate) AS sum_funds, "
		      		        + "sum(record_rate) AS sum_record, sum(source_rate) AS sum_source "  
							+ "from "+psDBname;
					pstm = conn.prepareStatement(sql);
					System.out.println(pstm);
					rs = pstm.executeQuery();
					while (rs.next()) {
						int sum_ref = rs.getInt("sum_ref");
						int sum_funds = rs.getInt("sum_funds");
						int sum_record = rs.getInt("sum_record");
						int sum_source = rs.getInt("sum_source");
						ref_max = sum_ref>ref_max? sum_ref : ref_max;
						ref_min = sum_ref<ref_min? sum_ref : ref_min;
						funds_max = sum_funds>funds_max? sum_funds : funds_max;
						funds_min = sum_funds<funds_min? sum_funds : funds_min;
						record_max = sum_record>ref_max? sum_record : record_max;
						record_min = sum_record<record_min? sum_record : record_min;
						source_max = sum_source>source_max? sum_source : source_max;
						source_min = sum_source<source_min? sum_source : source_min;
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
				
				
				//Get patent      
				try {	
					conn  = ConnectionUtils.getConnection();
					String sql = "Select count(*) AS count "		      		    
							+ "from "+pttDBname;
					pstm = conn.prepareStatement(sql);
					rs = pstm.executeQuery();
					while (rs.next()) {
						int sum_ptt = rs.getInt("count");						
						ptt_max = sum_ptt>ptt_max? sum_ptt : ptt_max;
						ptt_min = sum_ptt<ptt_min? sum_ptt : ptt_min;
					}
		        } catch (SQLException | ClassNotFoundException  e) {
		            e.printStackTrace();           
		        }finally { 
		        	try { if (rs != null) rs.close(); } catch (Exception e) {};
		            try { if (pstm != null) pstm.close(); } catch (Exception e) {};
		            if (conn != null) {
		                try {
		                    conn.close();
		                } catch (SQLException e) { }
		            }
		        }
		        
		        
			}
			
			//Insert Into MAXMIN table
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "Insert into MaxMinRate (id, ref_max, ref_min, funds_max, funds_min, record_max, record_min, source_max, source_min, ptt_max, ptt_min)"		     			
		     			+ "values (1,?,?,?,?,?,?,?,?,?,?) "
		     			+ "ON DUPLICATE KEY UPDATE ref_max= ?, ref_min= ?,funds_max =?, funds_min=?, record_max=?,record_min=?,source_max=?,source_min=?,ptt_max=?,ptt_min=?";
				pstm = conn.prepareStatement(sql);
				pstm.setInt(1, ref_max);
				pstm.setInt(2, ref_min);
				pstm.setInt(3, funds_max);
				pstm.setInt(4, funds_min);
				pstm.setInt(5, record_max);
				pstm.setInt(6, record_min);
				pstm.setInt(7, source_max);
				pstm.setInt(8, source_min);	
				pstm.setInt(9, ptt_max);
				pstm.setInt(10, ptt_min);
				pstm.setInt(11, ref_max);
				pstm.setInt(12, ref_min);
				pstm.setInt(13, funds_max);
				pstm.setInt(14, funds_min);
				pstm.setInt(15, record_max);
				pstm.setInt(16, record_min);
				pstm.setInt(17, source_max);
				pstm.setInt(18, source_min);	
				pstm.setInt(19, ptt_max);
				pstm.setInt(20, ptt_min);
				pstm.executeUpdate();
	        } catch (SQLException | ClassNotFoundException  e) {
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
				
	//Update MaxMinTA table for CollegeTalentAndInputScore
	public static void updateMaxMinTA() throws SQLException {
		int yuanshi_max = 0;
		int yuanshi_min = Integer.MAX_VALUE;
		int changjiang_max = 0;
		int changjiang_min = Integer.MAX_VALUE;
		int qingnian_max = 0;
		int qingnian_min = Integer.MAX_VALUE;
		int master_max = 0;
		int master_min = Integer.MAX_VALUE;
		int doc_max = 0;
		int doc_min = Integer.MAX_VALUE;
		int post_doc_max = 0;
		int post_doc_min = Integer.MAX_VALUE;
		int lab_max = 0;
		int lab_min = Integer.MAX_VALUE;
		int good_subject_max = 0;
		int good_subject_min = Integer.MAX_VALUE;
		int nature_prize_max = 0;
		int nature_prize_min = Integer.MAX_VALUE;
		int social_prize_max = 0;
		int social_prize_min = Integer.MAX_VALUE;
		int total_prize_max = 0;
		int total_prize_min = Integer.MAX_VALUE;
		
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		// For each college, get all paper records and compare with max,min value
			try {	
				conn  = ConnectionUtils.getConnection();
				String sql = "select a.* from CollegePaperAndPatentScore a where a.create_date  = "
	      		        + "(select MAX(b.create_date) from CollegePaperAndPatentScore b "  
						+ "where b.college_id = a.college_id) ";
				pstm = conn.prepareStatement(sql);
				System.out.println(pstm);
				rs = pstm.executeQuery();
				while (rs.next()) {
					int sum_yuanshi = rs.getInt("yuanshi_num");
					int sum_changjiang = rs.getInt("changjiang_num");
					int sum_qingnian = rs.getInt("qingnian_num");
					int sum_master = rs.getInt("master_num");
					int sum_doc = rs.getInt("doc_num");
					int sum_post_doc = rs.getInt("post_doc_num");
					int sum_lab = rs.getInt("lab_num");
					int sum_good_subject = rs.getInt("good_sub_num");
					int sum_nature_prize = rs.getInt("nature_prize_num");
					int sum_social_prize = rs.getInt("social_prize_num");
					int sum_total_prize = rs.getInt("total_prize_num");
					yuanshi_max = sum_yuanshi>yuanshi_max? sum_yuanshi : yuanshi_max;
					yuanshi_min = sum_yuanshi<yuanshi_min? sum_yuanshi : yuanshi_min;
					changjiang_max = sum_changjiang>changjiang_max? sum_changjiang : changjiang_max;
					changjiang_min = sum_changjiang<changjiang_min? sum_changjiang : changjiang_min;
					qingnian_max = sum_qingnian>qingnian_max? sum_qingnian : qingnian_max;
					qingnian_min = sum_qingnian<qingnian_min? sum_qingnian : qingnian_min;
					master_max = sum_master>master_max? sum_master : master_max;
					master_min = sum_master<master_min? sum_master : master_min;
					doc_max = sum_doc>doc_max? sum_doc : doc_max;
					doc_min = sum_doc<doc_min? sum_doc : doc_min;
					post_doc_max = sum_post_doc>post_doc_max? sum_post_doc : post_doc_max;
					post_doc_min = sum_post_doc<post_doc_min? sum_post_doc : post_doc_min;
					lab_max = sum_lab>lab_max? sum_lab : lab_max;
					
					lab_min = sum_lab<lab_min? sum_lab : lab_min;
					
					good_subject_max = sum_good_subject>good_subject_max? sum_good_subject : good_subject_max;
					
					good_subject_min = sum_good_subject<good_subject_min? sum_good_subject : good_subject_min;
					
					nature_prize_max = sum_nature_prize>nature_prize_max? sum_nature_prize : nature_prize_max;
					
					nature_prize_min = sum_nature_prize<nature_prize_min? sum_nature_prize : nature_prize_min;
					
					social_prize_max = sum_social_prize>social_prize_max? sum_social_prize : social_prize_max;
					
					social_prize_min = sum_social_prize<social_prize_min? sum_social_prize : social_prize_min;
					
					total_prize_max = sum_total_prize>total_prize_max? sum_total_prize : total_prize_max;
					
					total_prize_min = sum_total_prize<total_prize_min? sum_total_prize : total_prize_min;
					
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
							
		//Insert Into MAXMINTA table
		try {	
			conn  = ConnectionUtils.getConnection();
			String sql = "Insert into MaxMinRateTA (id, yuanshi_max, yuanshi_min, changjiang_max, changjiang_min, qingnian_max, qingnian_min, master_max, master_min, doc_max, doc_min, "		     			
	     			+ "post_doc_max, post_doc_min, lab_max, lab_min, good_subject_max, good_subject_min, nature_prize_max, nature_prize_min, social_prize_max, social_prize_min, "
	     			+ "total_prize_max, total_prize_min) "
					+ "values (1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
	     			+ "ON DUPLICATE KEY UPDATE yuanshi_max= ?, yuanshi_min= ?,changjiang_max =?, changjiang_min=?, qingnian_max=?,qingnian_min=?,master_max=?,master_min=?,doc_max=?,doc_min=?, "
	     			+ "post_doc_max= ?,post_doc_min=?,lab_max=?,lab_min=?,good_subject_max=?,good_subject_min=?,nature_prize_max=?,nature_prize_min=?,social_prize_max=?,social_prize_min=?, "
	     			+ "total_prize_max=?,total_prize_min=? ";
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, yuanshi_max);
			pstm.setInt(2, yuanshi_min);
			pstm.setInt(3, changjiang_max);
			pstm.setInt(4, changjiang_min);
			pstm.setInt(5, qingnian_max);
			pstm.setInt(6, qingnian_min);
			pstm.setInt(7, master_max);
			pstm.setInt(8, master_min);	
			pstm.setInt(9, doc_max);
			pstm.setInt(10, doc_min);
			pstm.setInt(11, post_doc_max);
			pstm.setInt(12, post_doc_min);
			pstm.setInt(13, lab_max);
			pstm.setInt(14, lab_min);
			pstm.setInt(15, good_subject_max);
			pstm.setInt(16, good_subject_min);
			pstm.setInt(17, nature_prize_max);
			pstm.setInt(18, nature_prize_min);	
			pstm.setInt(19, social_prize_max);
			pstm.setInt(20, social_prize_min);
			pstm.setInt(21, total_prize_max);
			pstm.setInt(22, total_prize_min);
			pstm.setInt(23, yuanshi_max);
			pstm.setInt(24, yuanshi_min);
			pstm.setInt(25, changjiang_max);
			pstm.setInt(26, changjiang_min);
			pstm.setInt(27, qingnian_max);
			pstm.setInt(28, qingnian_min);
			pstm.setInt(29, master_max);
			pstm.setInt(30, master_min);	
			pstm.setInt(31, doc_max);
			pstm.setInt(32, doc_min);
			pstm.setInt(33, post_doc_max);
			pstm.setInt(34, post_doc_min);
			pstm.setInt(35, lab_max);
			pstm.setInt(36, lab_min);
			pstm.setInt(37, good_subject_max);
			pstm.setInt(38, good_subject_min);
			pstm.setInt(39, nature_prize_max);
			pstm.setInt(40, nature_prize_min);	
			pstm.setInt(41, social_prize_max);
			pstm.setInt(42, social_prize_min);
			pstm.setInt(43, total_prize_max);
			pstm.setInt(44, total_prize_min);
			pstm.executeUpdate();
        } catch (SQLException | ClassNotFoundException  e) {
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
}
