package crawler_test;
import org.apache.log4j.BasicConfigurator;
import java.net.URLDecoder;
import java.net.URLEncoder;


import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import org.vess.edu.beans.*;
import org.vess.edu.conn.*;
import org.vess.edu.utils.*;

public class GetBaidubaike implements PageProcessor {
	
	public static final String List_URL =  "\\S+%3a\\S+%3a%22\\w{2}\\W\\S+";


    private Site site = Site
            .me()
            .setDomain("baike.baidu.com")
            .setSleepTime(3000)
            .setTimeOut(20000)
            .setCycleRetryTimes(5)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
    	String page_url = page.getUrl().toString();
    	String clg_title="";
    	int clg_grad = -1;
    	int clg_doc = -1;
    	int clg_post_doc = -1;
    	int clg_id = -1;
    	
    	// Get College name
    	List<String> item_name_list = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-left']/dt[@class='basicInfo-item name']/text()").all();
    	for(int i=0;i<item_name_list.size();i++){  		
    		int item_idx = i+1; // xpath index starts from 1
    		String item_name = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-left']/dt[@class='basicInfo-item name']["+item_idx+"]/text()").toString();		
    		if(item_name!=null && item_name.equals("中文名")){
    			clg_title = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-left']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString().trim();
    			
    			//If the college name contains (), replace it with English version
        		if(clg_title.contains("（")){
        			int idx_left = clg_title.indexOf("（");
        			clg_title = clg_title.substring(0, idx_left) + "(" + clg_title.substring(idx_left+1);
        		}
        		if(clg_title.contains("）")){
        			int idx_right = clg_title.indexOf("）");
        			clg_title = clg_title.substring(0, idx_right) + ")"+ clg_title.substring(idx_right+1);
        		}
        		System.out.println("中文名1："+clg_title);
        		
    			//Check from DB if college name exists
    			Connection conn  = null;
        		try {
        			conn  = ConnectionUtils.getConnection();
                    clg_id = DBUtils.GetCollegeIdByName(conn, clg_title); 
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }finally {                      
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException e) { /* ignored */}
                    }
                }
        		System.out.println("cid: "+clg_id);
        		//If not found break the loop
        		if(clg_id<0) break;
        		
    		}
    		if(item_name!=null && item_name.equals("硕士点")){
    			String clg_grad_str = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-left']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			clg_grad = getInt(clg_grad_str);
    			//System.out.println("硕士点1："+clg_grad);
    		}
    		if(item_name!=null && item_name.equals("博士点")){
    			String clg_doc_str = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-left']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			clg_doc = getInt(clg_doc_str);
    			//System.out.println("博士点1："+clg_doc);
    		}
    		if(item_name!=null && item_name.equals("博士后流动站")){
    			String clg_post_doc_str = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-left']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			clg_post_doc = getInt(clg_post_doc_str);
    			//System.out.println("博士后流动站1："+clg_post_doc);
    		}
    		
    	}
    	
    	
    	for(int i=0;i<item_name_list.size();i++){  		
    		int item_idx = i+1; // xpath index starts from 1
    		String item_name = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-right']/dt[@class='basicInfo-item name']["+item_idx+"]/text()").toString();		
    		if(item_name!=null && item_name.equals("中文名")){
    			clg_title = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-right']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			//System.out.println("中文名2："+clg_title);
    			//Check from DB if college name exists
        		try {
        			Connection conn  = ConnectionUtils.getConnection();
                    clg_id = DBUtils.GetCollegeIdByName(conn, clg_title); 
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }
        		
        		//If not found break the loop
        		if(clg_id<0) break;
    		}
    		if(item_name!=null &&item_name.equals("硕士点")){
    			String clg_grad_str = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-right']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			clg_grad = getInt(clg_grad_str);
    			//System.out.println("硕士点2："+clg_grad);
    		}
    		if(item_name!=null &&item_name.equals("博士点")){
    			String clg_doc_str = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-right']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			clg_doc = getInt(clg_doc_str);
    			//System.out.println("博士点2："+clg_doc);
    		}
    		if(item_name!=null &&item_name.equals("博士后流动站")){
    			String clg_post_doc_str = page.getHtml().xpath("//dl[@class='basicInfo-block basicInfo-right']/dd[@class='basicInfo-item value']["+item_idx+"]/text()").toString();
    			clg_post_doc = getInt(clg_post_doc_str);
    			//System.out.println("博士后流动站2："+clg_post_doc);
    		}
 
    	}
    	
    	System.out.println("中文名："+clg_title);
    	System.out.println("硕士点："+clg_grad);
    	System.out.println("博士点："+clg_doc);
    	System.out.println("博士后流动站："+clg_post_doc);
    	
    	// Insert into DB if found valid college name
    	if(clg_id>0){
    		//Insert or update master info
    		if(clg_grad>0){
    			Connection conn1 = null;
    			try {
        			conn1  = ConnectionUtils.getConnection();
                    DBUtils.insertBaiduMS(conn1, clg_id, clg_title, clg_grad); 
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }finally {                      
                    if (conn1 != null) {
                        try {
                            conn1.close();
                        } catch (SQLException e) { /* ignored */}
                    }
                }
    		}
    		
    		//Insert or update doc info
    		if(clg_doc>0){
    			Connection conn2 = null;
    			try {
        			conn2  = ConnectionUtils.getConnection();
        			DBUtils.insertBaiduDoc(conn2, clg_id, clg_title, clg_doc); 
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }finally {                      
                    if (conn2 != null) {
                        try {
                            conn2.close();
                        } catch (SQLException e) { /* ignored */}
                    }
                }
    		}
    		
    		//Insert or update post_doc info
    		if(clg_post_doc>0){
    			Connection conn3 = null;
    			try {
        			conn3  = ConnectionUtils.getConnection();
        			DBUtils.insertBaiduPostDoc(conn3, clg_id, clg_title, clg_post_doc);  
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }finally {                      
                    if (conn3 != null) {
                        try {
                            conn3.close();
                        } catch (SQLException e) { /* ignored */}
                    }
                }
    		}
    	}
    	
    
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
    	BasicConfigurator.configure();
    	//String clg_url ="http://baike.baidu.com/item/%E6%B2%B3%E5%8C%97%E5%B7%A5%E4%B8%9A%E5%A4%A7%E5%AD%A6";
    	
    	// Get all college title from DB
    	Connection conn = null;
    	List<String> cname_list = null;
		try {
			conn  = ConnectionUtils.getConnection();
			cname_list = DBUtils.GetCollegeNameAll(conn); 
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();           
        }finally {                      
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {}
            }
        }
		
		for(String clg_name: cname_list){
			//If the college name contains (), replace it with Chinese version
			if(clg_name.contains("(")){
				int idx_left = clg_name.indexOf("(");
				clg_name = clg_name.substring(0, idx_left) + "（" + clg_name.substring(idx_left+1);
			}
			if(clg_name.contains(")")){
				int idx_right = clg_name.indexOf(")");
				clg_name = clg_name.substring(0, idx_right) + "）"+ clg_name.substring(idx_right+1);
			}
			String clg_url = "http://baike.baidu.com/item/"+ clg_name;
		
			// RUN
			Spider.create(new GetBaidubaike()).addUrl(clg_url)
		  //.setScheduler(new FileCacheQueueScheduler("/Users/sikai/Documents/workspace/crawler_test/Logs/test_baidu"))
			.thread(3)
			.run(); 
		}
		
    	
    	
        
    }
    
    //Extract integer from string
    public int getInt(String in){
    	// Pure number
    	if(!in.contains("个")){
    		return Integer.parseInt(in.trim());
    	}
    	
    	in = in.trim();
    	// 131（一级22个） 个 <--like this
    	if(in.contains("（")){
    		int idx1 = in.indexOf("（");
    		int idx2 = in.indexOf("）");
    		in = in.substring(0, idx1) + in.substring(idx2+1);
    		return Integer.parseInt(in.replaceAll("[^0-9]", ""));
    	}
    	if(in.contains("(")){
    		int idx1 = in.indexOf("(");
    		int idx2 = in.indexOf(")");
    		in = in.substring(0, idx1) + in.substring(idx2+1);
    		return Integer.parseInt(in.replaceAll("[^0-9]", ""));
    	}
    	
    	//一级36个，二级126 个 <--like this
    	int idx = in.indexOf("个");
    	if(idx!=in.length()-1){
    		String in_1 = in.substring(0,idx);
    		String in_2 = in.substring(idx+1);
    		return Integer.parseInt(in_1.replaceAll("[^0-9]", "")) + Integer.parseInt(in_2.replaceAll("[^0-9]", ""));
    	// Other case
    	}else{
    		return Integer.parseInt(in.replaceAll("[^0-9]", ""));
    	}
    	
    }

    
}
