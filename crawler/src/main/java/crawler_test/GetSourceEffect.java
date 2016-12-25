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

public class GetSourceEffect implements PageProcessor {
	
	
    private Site site = Site
            .me()
            .setDomain("wanfangdata.com.cn")
            .setSleepTime(30000)
            .setTimeOut(20000)
            .setCycleRetryTimes(5)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
    	String page_url = page.getUrl().toString();
    	//Get the book we searched
    	String source_search = page.getHtml().xpath("//div[@id='tblSearch']//input[@name='txtValue']/@value").toString().trim();
    	
    	//Get the result we search ---book name
    	List<String> source_list = page.getHtml().xpath("//span[@id='lblList']//div[@class='colPic']/p/a/text()").all();
    	
    	//Get the result we search ---book effective rate
    	List<String> effect_list = page.getHtml().xpath("//span[@id='lblList']//div[@class='colPic']/p/text()").all();
    	
    	//Check if our result matches with our search
    	for(int i=0;i<source_list.size();i++){
    		String source_name = source_list.get(i).trim();
    		if(source_name!=null && source_search!=null && source_name!="" && source_search!="" &&((source_search.equals(source_name)))){
    			System.out.println("!!已找到: "+source_name);   			
    			String effect_str = page.getHtml().xpath("//span[@id='lblList']//div[@class='colPic']/p["+i+1+"]/text()").toString();
    			double complex_rate = 0.001;
    			double general_rate =  0.001;
    			if(effect_str!=null && effect_str!=""){
    				int idx1 = effect_str.indexOf("：");
    				int idx2 = effect_str.indexOf("综合");
    				String complex = effect_str.substring(idx1+1, idx2).trim();
    				int idx3 = effect_str.indexOf("：", idx2);
    				String general = effect_str.substring(idx3+1);
   
    				complex_rate = Double.parseDouble(complex);
    				general_rate = Double.parseDouble(general);
    			}
    			System.out.println("complex is: "+complex_rate);
    			System.out.println("gernal is: "+general_rate);
    			
    			//Insert Source into DB
    			Source src = new Source();
    			src.setSourceTitle(source_name);
    			src.setSourceComplexRate(complex_rate);
    			src.setSourceGeneralRate(general_rate);
    			
    			try {
        			Connection conn  = ConnectionUtils.getConnection();
                    DBUtils.insertSource(conn, src);
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }
    			break;
    		}
    	}
    	
        
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
 
    	String clg_url ="http://navi.cnki.net/KNavi/JournalDetail?pcode=CJFD&pykm=ZGFX";
    	//String clg_url= "http://epub.cnki.net/kns/oldnavi/n_list.aspx?NaviID=1&Field=cykm$%25=%22%7B0%7D%22&Value=%E6%96%B0%E7%96%86%E8%81%8C%E4%B8%9A%E6%95%99%E8%82%B2%E7%A0%94%E7%A9%B6&selectIndex=0&NaviLink=%E6%A3%80%E7%B4%A2%3a%E6%96%B0%E7%96%86%E8%81%8C%E4%B8%9A%E6%95%99%E8%82%B2%E7%A0%94%E7%A9%B6&ListSearchFlag=1&Flg=";
    	Spider.create(new GetSourceEffect()).addUrl(clg_url)
    	//.setScheduler(new FileCacheQueueScheduler("/Users/sikai/Documents/workspace/crawler_test/Logs/patent"))
    	.thread(3)
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
