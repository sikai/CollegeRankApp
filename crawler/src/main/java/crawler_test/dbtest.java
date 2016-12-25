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

public class dbtest implements PageProcessor {
	
	public static final String List_URL =  "\\S+%3a\\S+%3a%22\\w{2}\\W\\S+";
	public static final String DomainName = "wanfangdata.com.cn";
	public static final int SleepTime = 30000;
	public static final int TimeOutTime = 20000;
	public static final int RetryTimes = 5;


    private Site site = Site
            .me()
            .setDomain(DomainName)
            .setSleepTime(SleepTime)
            .setTimeOut(TimeOutTime)
            .setCycleRetryTimes(RetryTimes)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
    	String page_url = page.getUrl().toString();
    	String ppr_auth1=null;
    	String ppr_auth2=null;
    	String ppr_auth3=null;
    	List<String> auth_list  = ExtractAuthor(page);
    	    	
    	if(auth_list.size()>0)  ppr_auth1 = auth_list.get(0);
    	if(auth_list.size()>1)  ppr_auth2 = auth_list.get(1);
    	if(auth_list.size()>2)  ppr_auth3 = auth_list.get(2);
  
    	System.out.println(ppr_auth1);
    	System.out.println(ppr_auth2);
    	System.out.println(ppr_auth3);
   
        
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
    	BasicConfigurator.configure();
    	
	
		/*
		try {
			conn  = ConnectionUtils.getConnection();			
			List<College> clg_list = DBUtils.queryCollegeWithinIdRange(conn,31,45);
			for(College clg:clg_list){
				int clg_id = clg.getCollegeId();
				String clg_title = clg.getCollegeTitle();
				//loop through lvl1 class
				
			}
		
			
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();           
        }
        
        */
      
   	
    	//-------------FOR TESTING-------------------------------
    	
    	//String clg_url="http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%"
    	//		+ college_city1_list.get(0)+"*%22+DBID%3ajydx"
    	//		+"+%E5%8A%9E%E5%AD%A6%E7%B1%BB%E5%9E%8B%3a%"
    	//		+college_type_list.get(0);
    	//String clg_url = "http://d.wanfangdata.com.cn/Periodical/zgxzgl200303010";
    	String clg_url ="http://d.wanfangdata.com.cn/Periodical/hxgc201209010";
    	Spider.create(new dbtest()).addUrl(clg_url)
    	//.setScheduler(new FileCacheQueueScheduler("/Users/sikai/Documents/workspace/crawler_test/Logs/test_demo/BUPT"))
    	.thread(4)
        .run(); 
        
    }
    
    // Extract author name
    public List<String> ExtractAuthor(Page page){
		List<String> auth_list = page.getHtml().xpath("//div[@class='row row-author']//a/text()").all();
		return auth_list;
    }
    
    // Extract Title
    public String ExtractTitle(Page page){
    	return page.getHtml().xpath("//head/title/text()").toString();
    }
}
