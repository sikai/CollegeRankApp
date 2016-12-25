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

public class GetCollegePaper implements PageProcessor {
	
	public static final String List_URL =  "\\S+%3a\\S+%3a%22\\w{2}\\W\\S+";


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
    	//current subject in the queue
    	String curr_lvl1_node;
    	String curr_lvl2_node;
    	boolean belong_to_clg = false;
    	System.out.println("URL "+page_url);
    	// If current page is not a level2 list page
    	if(!page.getUrl().regex(List_URL).match()){
    		
    		// Page is a content page, start extracting paper info...
    		if(page_url.contains("Periodical")||page_url.contains("Conference")||page_url.contains("NSTLQK")||page_url.contains("NSTLHY")){
    			
    			if(page_url.contains("NSTLQK") || page_url.contains("NSTLHY")) {
    	    		System.out.println("Skipping...");
    	    		page.setSkip(true);
    	    	}
    	    	// Extract previously stored field info from paper_pass object in Request
    			
    			
    			Request req_get = page.getRequest();
        		Object obj_get = req_get.getExtra("paper_pass");
        		Paper ppr_get = (Paper) obj_get;
        		String ppr_class_lvl1 = ppr_get.getPaperClassLvl1();
        		String ppr_class_lvl2 = ppr_get.getPaperClassLvl2();
        		String ppr_webId = ppr_get.getPaperWebId();
        		int ppr_ref = ppr_get.getPaperReferences();
        		Object obj_ppr_org =  req_get.getExtra("paper_org");
    			String ppr_org = (String) obj_ppr_org;
    			
    	    	// Extract author name
    	    	String ppr_auth1=null;
    	    	String ppr_auth2=null;
    	    	String ppr_auth3=null;
    	    	List<String> auth_list  = ExtractAuthor(page);
    	    	
    	    	if(auth_list.size()>0)  ppr_auth1 = auth_list.get(0);
    			if(auth_list.size()>1)  ppr_auth2 = auth_list.get(1);
    			if(auth_list.size()>2)  ppr_auth3 = auth_list.get(2);
    			//System.out.println("~~~~~~~~~~~~Athor1: "+ppr_auth1);
    			//System.out.println("Athor2: "+ppr_auth2);
    			//System.out.println("Athor3: "+ppr_auth3);
    			

    			// Extract paper Orgnizations for validating if paper belongs to proper college
    			int paper_orgs_idx = -1;
    			List<String> paper_orgs_list = new ArrayList<String>();
    			List<String> paper_orgs_idx_pre_list = page.getHtml().xpath("//div[@class='fixed-width-wrap fixed-width-wrap-feild']//div[@class='row']/span[@class='pre']/text()").all(); 
    			for(String paper_orgs_idx_pre: paper_orgs_idx_pre_list){
    				if(paper_orgs_idx_pre.equals("作者单位：")){
    					paper_orgs_idx = paper_orgs_idx_pre_list.indexOf(paper_orgs_idx_pre);
    					break;
    				}
    			}
    			            //System.out.println("idx is"+paper_orgs_idx);
    			if(paper_orgs_idx>0){
    				paper_orgs_idx+=1;  //xpath index starts from 1
    				paper_orgs_list = page.getHtml().xpath("//div[@class='fixed-width-wrap fixed-width-wrap-feild']//div[@class='row']["+paper_orgs_idx+"]/span[@class='text']/span/text()").all();
    				if(paper_orgs_list.isEmpty()){
    					paper_orgs_list = page.getHtml().xpath("//div[@class='fixed-width-wrap fixed-width-wrap-feild']//div[@class='row']["+paper_orgs_idx+"]/span[@class='text']/text()").all();
    				}
    			}
    			for(String paper_org_pre: paper_orgs_list){    				
    				String[] paper_org_pre_arr = paper_org_pre.split(","); 
    				String paper_org_temp = paper_org_pre_arr[0].trim();
    				    System.out.println("!!!!资料页的院校名称 : "+paper_org_temp);	
    				    System.out.println("!!!!搜索的院校: "+ppr_org);
    				if((ppr_org!=null && ppr_org!="") && (paper_org_temp.equals(ppr_org) || paper_org_temp.contains(ppr_org) || ppr_org.contains(paper_org_temp))){
    					belong_to_clg = true;
    					break;
    				}
    				if(paper_org_pre_arr.length>1){
    					String paper_org_temp_2 = paper_org_pre_arr[1].trim();
    					System.out.println("!!!!资料页的院校名称 : "+paper_org_temp_2);
    					if((ppr_org!=null && ppr_org!="") && (paper_org_temp_2.equals(ppr_org) || paper_org_temp_2.contains(ppr_org) || ppr_org.contains(paper_org_temp_2))){
        					belong_to_clg = true;
        					break;
        				}  
    				}
    				if(paper_org_pre_arr.length>2){
    					String paper_org_temp_3 = paper_org_pre_arr[2].trim();
    					System.out.println("!!!!资料页的院校名称 : "+paper_org_temp_3);
    					if((ppr_org!=null && ppr_org!="") && (paper_org_temp_3.equals(ppr_org) || paper_org_temp_3.contains(ppr_org) || ppr_org.contains(paper_org_temp_3))){
        					belong_to_clg = true;
        					break;
        				}  
    				} 
    			}
    			     if(belong_to_clg){
    			    	 System.out.println("!!!!已找到 ");
    			     }else{
    			    	 System.out.println("@@@不符合");
    			     }
    				
    			
    	 		// Extract Title
    			String ppr_title = ExtractTitle(page);			
    	    	//System.out.println("Title: "+ppr_title);
    	     			   			
    	    	// Extract Date
    	    	String ppr_date = null;
    	    	int idx_of_date_in_list = -1; //For future referenece, idx of funds = idx of date+1
    	    	List<String>  date_ls = page.getHtml().xpath("//div[@class='fixed-width-wrap fixed-width-wrap-feild']//div[@class='row']/span[@class='text']/text()").all();
    	    	for(String date_str:date_ls){
    	    		if(Pattern.matches("\\d{4}[\u4e00-\u9fa5]\\d+[\u4e00-\u9fa5]\\d+[\u4e00-\u9fa5]", date_str)){
    	    			idx_of_date_in_list = date_ls.indexOf(date_str);
    	    			if(date_str.length()==9){
    	    				ppr_date = date_str.substring(0,4)+"0"+date_str.substring(5,6)+"0"+date_str.substring(7,8);
    	    			}else if(date_str.length()==10 && Character.isDigit(date_str.charAt(6))) {
    	    				ppr_date = date_str.substring(0,4) +date_str.substring(5,7)+"0"+date_str.substring(8,9);
    	    			}else if(date_str.length()==10 && !Character.isDigit(date_str.charAt(6))) {
    	    				ppr_date = date_str.substring(0,4) +"0"+date_str.substring(5,6)+date_str.substring(7,9);
    	    			}else{
    	    				ppr_date = date_str.substring(0,4) +date_str.substring(5,7)+date_str.substring(8,10);
    	    			}
    	    			break;
    	    		}
    	    	}
    	    	System.out.println("Date: "+ppr_date);
    	    			
    	    	// Extract Source
    	    	String ppr_source = null;
    	    	if(page_url.contains("Conference")){
    	    		ppr_source = page.getHtml().xpath("//div[@class='fixed-width baseinfo-feild']/div[@class='row'][4]/span[@class='text']/text()").toString();
    	    	}else{
    	    		ppr_source = page.getHtml().xpath("//div[@class='row row-magazineName']//a/text()").toString();
    	    	} 
    	    	System.out.println("Source: "+ppr_source);
    	    			
    	    			
    	    	// Extract Records and Funds for Periodical journal
    	    	String ppr_record = "";
    	    	String ppr_funds = null;
    	    	if(page_url.contains("Periodical")){
    	    		
    	    		//Extract Records 
    	    		List<String> ppr_record_list = page.getHtml().xpath("//div[@class='row row-magazineName']//span[@class='core-box']//span/text()").all();    				
    				for(String record_items: ppr_record_list){
    					ppr_record+=record_items;
    					ppr_record+=",";
    				}
    				if(!ppr_record_list.isEmpty()){
    					ppr_record=ppr_record.substring(0, ppr_record.length()-1);
    				}    				
    				System.out.println("Record: "+ppr_record);
    				
    				//Extract Funds   				
    				if(idx_of_date_in_list<0 || idx_of_date_in_list+1>=date_ls.size()) ppr_funds=null;
    				else{
    					ppr_funds = date_ls.get(idx_of_date_in_list+1);
    				}
    				System.out.println("Funds: "+ppr_funds);
    	    	}
    	    	
    	    	// Set other field of ppr_get
    	    	ppr_get.setPaperAuthorLvl1(ppr_auth1);
    	    	ppr_get.setPaperAuthorLvl2(ppr_auth2);
    	    	ppr_get.setPaperAuthorLvl3(ppr_auth3);
    	    	ppr_get.setPaperTitle(ppr_title);
    	    	ppr_get.setPaperDate(ppr_date);
    	    	ppr_get.setPaperSource(ppr_source);
    	    	ppr_get.setPaperRecord(ppr_record);
    	    	ppr_get.setPaperFunds(ppr_funds);
    	    	
    	    	//System.out.println("Author1 is: "+ppr_get.getPaperAuthorLvl1());
    	    	//System.out.println("Author2 is: "+ppr_get.getPaperAuthorLvl2());
    	    	//System.out.println("Author3 is: "+ppr_get.getPaperAuthorLvl3());
    	    	System.out.println("Title is: "+ppr_get.getPaperTitle());
    	    	//System.out.println("date is: "+ppr_get.getPaperDate());
    	    	//System.out.println("Source is: "+ppr_get.getPaperSource());
    	    	System.out.println("record is: "+ppr_get.getPaperRecord());
    	    	//System.out.println("Funds is: "+ppr_get.getPaperFunds());
    	    	//System.out.println("class1 is: "+ppr_get.getPaperClassLvl1());
    	    	//System.out.println("class2 is: "+ppr_get.getPaperClassLvl2());
    	    	//System.out.println("webid is: "+ppr_get.getPaperWebId());
    	    	//System.out.println("ref is: "+ppr_get.getPaperReferences());
    	    	
    	    	// Insert ppr_get info into DB
    	    	Connection conn  = null;
    	    	if(belong_to_clg){
    	    		try {
            			conn  = ConnectionUtils.getConnection();
                        DBUtils.insertPaper(conn, ppr_get, "Name"); //<--------MODIFY HERE !!!!!!!!!
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
  		
    		}else{
    			//page is a lvl0 or lvl1 list page
	    		page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-0 subjectCatagory']").links().all());
	    		page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-1 subjectCatagory']").links().all());
	    		// Get all hidden lvl1 and lvl2 page links into the queue
	    		//page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-0 more']").links().all());
	    		//page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-1 more']").links().all());
    		}
    		
    	// current page is a level 2 list page
    	}else{
    		// Load next list pages into webDownloader
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
                		//System.out.println("!!!!!!!!!!!Processing page:" + i);
                		page.addTargetRequest(url_next);
                	}  		
            	}
        	}
    		
    		
    		// Extracting lvl1 and lvl2 class info
    		curr_lvl1_node = page.getHtml().xpath("//p[@class='cluster-item tree-0 tree-top']/a/span[@class='text']/text()").toString();
    		curr_lvl2_node = page.getHtml().xpath("//p[@class='cluster-item tree-1 tree-top active']//span[@class='text']/text()").toString();
    		//System.out.println("Curren lvl1 node is: "+curr_lvl1_node);
    		//System.out.println("Curren lvl2 node is: "+curr_lvl2_node);
    		
    		// Extracting Reference #
		    List<String> ref_list_pre = page.getHtml().xpath("//div[@class='record-item']//div[@class='left-record']"
					         + "//div[@class='record-title']//span[@class='cited']/text()").all();
			List<Integer> ref_list = new ArrayList<Integer>();
			for(String ref_pre: ref_list_pre){
				if(ref_pre!=null && !ref_pre.equals("")){
					ref_pre = ref_pre.replaceAll("[^0-9]", "");
					ref_list.add(Integer.parseInt(ref_pre));
				}else{
					ref_list.add(0);	
				}
			}
    		
			// Extracting paper orgnizations
			String paper_orgniztion = page.getHtml().xpath("//form[@id='filterForm']//input[@id='filterBaseQuery']/@value").toString();
	    	int org_name_idx1 = paper_orgniztion.indexOf(":");
	    	int org_name_idx2 = paper_orgniztion.indexOf("分类号");
	    	paper_orgniztion = org_name_idx2==-1?paper_orgniztion.substring(org_name_idx1).trim():paper_orgniztion.substring(org_name_idx1+1, org_name_idx2).trim();
	    	//System.out.println("Paper belongs to orgnization: "+ppr_org);
			
			// Extracting WebId 
			List<String> webID_list_pre = page.getHtml().xpath("//div[@class='record-item']//"
                                      + "div[@class='left-record']//div[@class='record-title']//a[@class='title']").links().all();
			List<String> webID_list = new ArrayList<String>();
			for(String webID_pre: webID_list_pre){
				String[] webID_arr = webID_pre.split(".com.cn");
				webID_list.add(webID_arr[1].substring(1));
			}
			
			// Extracting individual content page link for future use
			List<String> content_url_list = page.getHtml().xpath("//div[@class='record-item']//"
                    + "div[@class='left-record']//div[@class='record-title']//a[@class='title']").links().all();
			
			// For each individual content page url, add paper object to its request
			for(int i=0;i<webID_list.size();i++){
				
				//Create new Paper with WebID, Ref, Class1 and Class2 field filled
				Paper ppr = new Paper();
				ppr.setPaperWebId(webID_list.get(i));
				ppr.setPaperReferences(ref_list.get(i));
    			ppr.setPaperClassLvl1(curr_lvl1_node);
    			ppr.setPaperClassLvl2(curr_lvl2_node);
    			
    			// Load individual content page link to webDownloader
    			Request req = new Request(content_url_list.get(i));
        		req.putExtra("paper_pass", ppr);
        		req.putExtra("paper_org", paper_orgniztion);
        		page.addTargetRequest(req);
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
    	String clg_url ="http://s.wanfangdata.com.cn/Paper.aspx?q=%E4%BD%9C%E8%80%85%E5%8D%95%E4%BD%8D%3a%E4%B8%AD%E5%9B%BD%E7%A7%91%E6%8A%80%E5%A4%A7%E5%AD%A6&f=top";
    	Spider.create(new GetCollegePaper()).addUrl(clg_url)
    	.setScheduler(new FileCacheQueueScheduler("/Users/sikai/Documents/workspace/crawler_test/Logs/test_cloth"))
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
