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

public class GetQingta implements PageProcessor {
	
	public static final String List_URL =  "\\S+%3a\\S+%3a%22\\w{2}\\W\\S+";


    private Site site = Site
            .me()
            .setDomain("www.cingta.com")
            .setSleepTime(3000)
            .setTimeOut(20000)
            .setCycleRetryTimes(5)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
    	String page_url = page.getUrl().toString();
    	String url_prefix = "http://www.cingta.com";
    	
    	// If page is a list page
    	if(!page_url.contains("data_axis")){
    		// Page is the first list page
    		if(!page_url.contains("key")){
    			List<String> url_list = page.getHtml().xpath("//ul[@class='lists provinces']").links().all();
        		for(String url_province:url_list){
        			page.addTargetRequest(url_province);
        		}
    		}
    		
    		// Page is a individual province list page
    		else{
    			List<String> url_list = page.getHtml().xpath("//ul[@class='lists schools']").links().all();
    			for(String url_college:url_list){
    				page.addTargetRequest(url_college);
    			}
    		}
    	}
    	// Page is a College info page
    	else{
    		// Get college name
    		String clg_name = page.getHtml().xpath("//div[@class='data-title'][1]/div[@class='word-left']/text()").toString().trim();
    		
    		//If the college name contains (), replace it with English version
    		if(clg_name.contains("（")){
    			int idx_left = clg_name.indexOf("（");
    			clg_name = clg_name.substring(0, idx_left) + "(" + clg_name.substring(idx_left+1);
    		}
    		if(clg_name.contains("）")){
    			int idx_right = clg_name.indexOf("）");
    			clg_name = clg_name.substring(0, idx_right) + ")"+ clg_name.substring(idx_right+1);
    		}
    		
    		// Check if this college name exists in the College Table

    		int clg_id = -1;
    		Connection conn  = null;
    		try {
    			conn  = ConnectionUtils.getConnection();
                clg_id = DBUtils.GetCollegeIdByName(conn, clg_name);
  
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();           
            }
    		System.out.println(clg_name);
    		System.out.println(clg_id);
    		if(clg_id>0){
    			// Get master and doc numbers
    			int master = 0;
    			int doc = 0;
    			
    			String master_str = page.getHtml().xpath("//div[@class='data-box'][1]//div[@class='incontent'][4]/text()").all().get(1).trim();
    			if(master_str.matches(".*\\d+.*")){
    				master_str= master_str.replaceAll("[^0-9]", "").trim();
    				master = Integer.parseInt(master_str);
        		}
        	
    			String doc_str = page.getHtml().xpath("//div[@class='data-box'][1]//div[@class='incontent'][3]/text()").all().get(1).trim();
    			if(doc_str.matches(".*\\d+.*")){
    				doc_str= doc_str.replaceAll("[^0-9]", "").trim();
    				doc = Integer.parseInt(doc_str);
        		}
    			
    			System.out.println("硕士点："+master);
        		System.out.println("博士点："+doc);
        		
    			// Get changjiang and yuanshi numbers
        		int yuanshi = 0;
        		int changjiang = 0;
        		int qingnian = 0;

        		String yuanshi_str = page.getHtml().xpath("//div[@id='scoll1']//div[@class='amount'][1]/text()").toString().trim();
        		String changjiang_str = page.getHtml().xpath("//div[@id='scoll1']//td[2]/div[@class='amount']/text()").toString().trim();
        		String qingnian_str = page.getHtml().xpath("//div[@id='scoll1']//td[3]/div[@class='amount']/text()").toString().trim();

        		if(yuanshi_str.matches(".*\\d+.*")){
        			//yuanshi_str=yuanshi_str.substring(0, yuanshi_str.indexOf("人")).trim();
        			yuanshi_str= yuanshi_str.replaceAll("[^0-9]", "").trim();
        			yuanshi = Integer.parseInt(yuanshi_str);
        		}
        		if(changjiang_str.matches(".*\\d+.*")){
        			//changjiang_str=changjiang_str.substring(0, changjiang_str.indexOf("人")).trim();
        			changjiang_str= changjiang_str.replaceAll("[^0-9]", "").trim();
        			changjiang = Integer.parseInt(changjiang_str);
        		}
        		if(qingnian_str.matches(".*\\d+.*")){
        			//qingnian_str=qingnian_str.substring(0, qingnian_str.indexOf("人")).trim();
        			qingnian_str= qingnian_str.replaceAll("[^0-9]", "").trim();
        			qingnian = Integer.parseInt(qingnian_str);
        		}
        		System.out.println("院士数量："+yuanshi);
        		System.out.println("长江学者数量："+changjiang);
        		System.out.println("杰出青年数量："+qingnian);
        		
        		// Get prizes and labs numbers
        		int nature_prize = 0;
        		int social_prize = 0;
        		int total_prize = 0;
        		int lab = 0;
        		
        		String nature_prize_str = page.getHtml().xpath("//div[@id='scoll2']//div[@class='amount'][1]/text()").toString().trim();
        		String social_prize_str = page.getHtml().xpath("//div[@id='scoll2']//td[2]/div[@class='amount']/text()").toString().trim();
        		String total_prize_str = page.getHtml().xpath("//div[@id='scoll2']//td[3]/div[@class='amount']/text()").toString().trim();
        		String lab_str = page.getHtml().xpath("//div[@id='scoll2']//td[4]/div[@class='amount']/text()").toString().trim();
        		
        		if(nature_prize_str.matches(".*\\d+.*")){
        			nature_prize_str= nature_prize_str.replaceAll("[^0-9]", "").trim();
        			//nature_prize_str=nature_prize_str.substring(0, nature_prize_str.indexOf("项")).trim();
        			nature_prize = Integer.parseInt(nature_prize_str);
        		}
        		
        		if(social_prize_str.matches(".*\\d+.*")){
        			//social_prize_str=social_prize_str.substring(0, social_prize_str.indexOf("项")).trim();
        			social_prize_str= social_prize_str.replaceAll("[^0-9]", "").trim();
        			social_prize = Integer.parseInt(social_prize_str);
        		}
        		
        		if(total_prize_str.matches(".*\\d+.*")){
        			//total_prize_str=total_prize_str.substring(0, total_prize_str.indexOf("项")).trim();
        			total_prize_str= total_prize_str.replaceAll("[^0-9]", "").trim();
        			total_prize = Integer.parseInt(total_prize_str);
        		}
        		
        		if(lab_str.matches(".*\\d+.*")){
        			//lab_str=lab_str.substring(0, lab_str.indexOf("个")).trim();
        			lab_str= lab_str.replaceAll("[^0-9]", "").trim();
        			lab = Integer.parseInt(lab_str);
        		}
        		System.out.println("国家自然科学基金数量："+nature_prize);
        		System.out.println("国家社会科学基金数量："+social_prize);
        		System.out.println("国家大奖数量："+total_prize);
        		System.out.println("省部以上科研平台数量："+lab);
        		
        		//Get good subject number
        		int good_subject = 0;
        		String good_subject_str = page.getHtml().xpath("//div[@id='scoll4']//div[@class='amount'][1]/text()").toString().trim();
        		if(good_subject_str.matches(".*\\d+.*")){
        			String[] good_subject_str_arr = good_subject_str.split("/");
        			String good_1_str = "";
        			String good_2_str = "";
        			// If there is no lvl2 good subject
        			if(!good_subject_str.contains("/")){
        				good_1_str= good_subject_str.replaceAll("[^0-9]", "").trim();
        				good_subject = Integer.parseInt(good_1_str);
        			}
        			// Else if it has a lvl2 good subject
        			else{
            			good_1_str = good_subject_str_arr[0].replaceAll("[^0-9]", "").trim();
            			good_2_str = good_subject_str_arr[1].replaceAll("[^0-9]", "").trim();
            			int good_1 = Integer.parseInt(good_1_str);
            			int good_2 = Integer.parseInt(good_2_str);
            			good_subject = good_1+good_2;
        			}
        			
        		}
        		System.out.println("国家重点科目："+good_subject);
        		
        		//Fill info into Qingta object
        		Qingta qt = new Qingta();
        		qt.setQingtaId(clg_id);
        		qt.setQingtaCollegeTitle(clg_name);
        		qt.setYuanshi(yuanshi);
        		qt.setChangjiang(changjiang);
        		qt.setQingnian(qingnian);
        		qt.setGoodSubject(good_subject);
        		qt.setNaturePrize(nature_prize);
        		qt.setSocialPrize(social_prize);
        		qt.setTotalPrize(total_prize);
        		qt.setLab(lab);
        		qt.setMaster(master);
        		qt.setDoc(doc);
        		
        		//Insert Qingta object into DB
        		Connection conn2  = null;
        		try {
        			conn2  = ConnectionUtils.getConnection();
                    DBUtils.insertCollegeDetail(conn2, qt);      
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
    		
    		
    	}
    	
    	
    	
		
  
   
        
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
    	BasicConfigurator.configure();
    	//String clg_url ="http://www.cingta.com/data_axis.html?p=173";
    	String clg_url ="http://www.cingta.com/data.html";
    	Spider.create(new GetQingta()).addUrl(clg_url)
    	//.setScheduler(new FileCacheQueueScheduler("/Users/sikai/Documents/workspace/crawler_test/Logs/test_qingta"))
    	.thread(3)
        .run(); 
        
    }
    
 
}
