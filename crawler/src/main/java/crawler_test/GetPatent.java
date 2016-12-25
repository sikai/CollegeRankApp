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

public class GetPatent implements PageProcessor {
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
    	//current subject in the queue
    	String curr_lvl1_node;
    	String curr_lvl2_node;
    	boolean belong_to_ptt = false;
    	System.out.println("URL "+page_url);
    	
    	// Get page lvl info from extra field
    	Request req_get = page.getRequest();
    	Object obj_get = req_get.getExtra("page_lvl");
    	
    	// Page is a lvl0 page, keep downloading sub links
    	if(obj_get==null){
    		// Get link URl
    		List<String> url_lvl1_list = page.getHtml().xpath("//p[@class='cluster-item tree-0 classsortCatagory']").links().all();
    		for(String url_lvl1: url_lvl1_list){
    			Request req = new Request(url_lvl1);
    			req.putExtra("page_lvl", "lvl1");
        		page.addTargetRequest(req);
    		}
    		
    		// Get hidden lvl1 page sub links
    		List<String> url_lvl1_list_hid = page.getHtml().xpath("//p[@class='cluster-item tree-0 more']").links().all();
    		for(String url_lvl1: url_lvl1_list_hid){
    			Request req = new Request(url_lvl1);
    			req.putExtra("page_lvl", "lvl1");
        		page.addTargetRequest(req);
    		}
    	}
    	else{
    	// Page is a lvl1 or 2 page
    		String obj_get_str = (String) obj_get;
    		
    		// Page is a lvl 1 page, keep downloading sublinks
    		if(obj_get_str.equals("lvl1")){
    			List<String> url_lvl2_list = page.getHtml().xpath("//p[@class='cluster-item tree-1 classsortCatagory']").links().all();
    			for(String url_lvl2: url_lvl2_list){
        			Request req = new Request(url_lvl2);
        			req.putExtra("page_lvl", "lvl2");
            		page.addTargetRequest(req);
        		}
    			
    			// Get hidden lvl2 page sub links
        		List<String> url_lvl2_list_hid = page.getHtml().xpath("//p[@class='cluster-item tree-1 more']").links().all();
        		for(String url_lvl2: url_lvl2_list_hid){
        			Request req = new Request(url_lvl2);
        			req.putExtra("page_lvl", "lvl2");
            		page.addTargetRequest(req);
        		}
    			
    		// Page is a lvl2 page, start extracting patent information		
    		}else if(obj_get_str.equals("lvl2")){
    			
    			// Load next lvl2 list pages into webDownloader
        		//Get next pages
            	String page_num_s = page.getHtml().xpath("//p[@class='pager']/span[@class='page_link']/text()").toString();
            	// find where / appears first
            	if(page_num_s!=null) {
            		int idx_slash = page_num_s.indexOf('/');
                	page_num_s = page_num_s.substring(idx_slash+1,page_num_s.length());
                	int page_num = Integer.parseInt(page_num_s);
                	if(!page_url.contains("&p=")){
                		for(int i=2;i<=page_num;i++){
                    		String url_next = page.getUrl().toString()+"&p="+i;
                    			System.out.println("!!!!!!!!!!!Processing page:" + i);
                    		Request req = new Request(url_next);
                    		req.putExtra("page_lvl", "lvl2");
                    		page.addTargetRequest(req);
                    	}  		
                	}
            	}
    			
    			//Extracting lvl1 and lvl2 category 
    			curr_lvl1_node = page.getHtml().xpath("//p[@class='cluster-item tree-0 tree-top']/a/span[@class='text']/text()").toString();
        		curr_lvl2_node = page.getHtml().xpath("//p[@class='cluster-item tree-1 tree-top active']//span[@class='text']/text()").toString();
        		
        		//Extracting title
        		List<String> ppt_title_list = page.getHtml().xpath("//div[@class='record-title']/a[@class='title']/text()").all();
        			
        		//Extracting orgs in our search query
        		String org_search_pre = page.getHtml().xpath("//form[@class='narraw-search narrow-item clear narraw-patent']//input[@id='filterBaseQuery']/@value").toString();
            	int idx_org_start = org_search_pre.indexOf("classsort");
            	String org_search = idx_org_start>0? org_search_pre.substring(0, idx_org_start).trim():org_search_pre.trim();

            	//Extracting patent info 
        		List<String> ptt_list = page.getHtml().xpath("//div[@class='record-item']//div[@class='record-subtitle']/text()").all();
            	for(int ptt_idx=0;ptt_idx<ptt_list.size();ptt_idx++){
            		String s = ptt_list.get(ptt_idx);
            		// Set up a flag for the validation of a patent
            		boolean ptt_isValid = false;
            		String ptt_org_name = "";
            		
            		String[] ptt_str_arr = s.trim().split(" ");
                		//for(int i=0;i<ptt_str_arr.length;i++){
                		//	System.out.println(i+" : "+ptt_str_arr[i]);
                		//}
            		
            		//Extracting web_id
                	String ptt_web_id = ptt_str_arr[1].split("_")[0];
                		//System.out.println("WEBID is: "+ptt_web_id);
   
                	//Extracting patent orgs name, orgs names are split by "、"
                	String[] ptt_orgs_info = ptt_str_arr[1].split("_")[1].split("、");
                	
                	//Checking if the orgs name matches with our query
                	for(int i=0;i<ptt_orgs_info.length;i++){
                		String ptt_org = ptt_orgs_info[i].trim();
                			System.out.println("org is:"+ptt_orgs_info[i].trim());
                		if((ptt_org!=null && ptt_org!="") && (org_search.equals(ptt_org) || org_search.contains(ptt_org) || ptt_org.contains(org_search))){
                			ptt_isValid = true;
                			ptt_org_name = org_search;
        					break;				
        				}
                	}
                			System.out.println("title is: "+ppt_title_list.get(ptt_idx));
			                	if(ptt_isValid){
			       		    	 System.out.println("!!!!已找到 ");
			       		     }else{
			       		    	 System.out.println("@@@不符合");
			       		     }
			                	
                	
                	//Extracting date
                	String ppt_date = ptt_str_arr[2].trim();
                	String ppt_date_unif = "";
                	
                	//convert 1999年9月9日 to 19990909 form
                	if(Pattern.matches("\\d{4}[\u4e00-\u9fa5]\\d+[\u4e00-\u9fa5]\\d+[\u4e00-\u9fa5]", ppt_date)){
                		
            			if(ppt_date.length()==9){
            				ppt_date_unif = ppt_date.substring(0,4)+"0"+ppt_date.substring(5,6)+"0"+ppt_date.substring(7,8);
            			}else if(ppt_date.length()==10 && Character.isDigit(ppt_date.charAt(6))) {
            				ppt_date_unif = ppt_date.substring(0,4) +ppt_date.substring(5,7)+"0"+ppt_date.substring(8,9);
            			}else if(ppt_date.length()==10 && !Character.isDigit(ppt_date.charAt(6))) {
            				ppt_date_unif = ppt_date.substring(0,4) +"0"+ppt_date.substring(5,6)+ppt_date.substring(7,9);
            			}else{
            				ppt_date_unif = ppt_date.substring(0,4) +ppt_date.substring(5,7)+ppt_date.substring(8,10);
            			}
            		}
                		//System.out.println("date is: "+ppt_date);
                		//System.out.println("date uniform is: "+ppt_date_unif);
                	
                	// If this is a valid patent, insert it into DB
                	if(ptt_isValid){
                		Patent ptt = new Patent();
                		ptt.setPatentTitle(ppt_title_list.get(ptt_idx));
                		ptt.setPatentWebId(ptt_web_id);
                		ptt.setPatentDate(ppt_date_unif);
                		ptt.setPatentClassLvl1(curr_lvl1_node);
                		ptt.setPatentClassLvl2(curr_lvl2_node);
                		ptt.setPatentOrg(ptt_org_name);
                		
                		Connection conn = null;
                		try {
                			conn  = ConnectionUtils.getConnection();
                            DBUtils.insertPatent(conn, ptt);
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();           
                        }finally {                      
                            if (conn != null) {
                                try {
                                    conn.close();
                                } catch (SQLException e) { /* ignored */}
                            }
                        }
                	}
            	}
    		}else{
    			System.out.println("WARNING: Unknown page extracted!");
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
    	//String clg_url = "http://d.wanfangdata.com.cn/Periodical/zgxzgl200303010";
    	String clg_url ="http://s.wanfangdata.com.cn/patent.aspx?q=%E5%8C%97%E4%BA%AC%E6%9C%8D%E8%A3%85%E5%AD%A6%E9%99%A2&f=top";
    	Spider.create(new GetPatent()).addUrl(clg_url)
    	.setScheduler(new FileCacheQueueScheduler("/Users/sikai/Documents/workspace/crawler_test/Logs/patent"))
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
