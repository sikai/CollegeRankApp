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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import org.vess.edu.beans.*;
import org.vess.edu.conn.*;
import org.vess.edu.utils.*;

public class test_demo implements PageProcessor {
	//要抓取的学校名称
	public static final String Name = "中国石油大学(北京)";
	//用于支持断点续抓的缓存文件的路径，需提前新建文件夹，
	public static final String CacheFile = "/Users/sikai/Documents/workspace/crawler_test/Logs/test_demo/sydx";
	//每个抓取页面间的时间间隔
	public static final int SleepTime = 30000;
	//抓取请求发出后无响应多久判定为Timeout
	public static final int TimeOutTime = 20000;
	//对抓取失败的页面重试次数
	public static final int RetryTimes = 5;
	//同时运行的线程数
	public static final int ThreadsNum = 4;
	
	public static final String List_URL =  "\\S+%3a\\S+%3a%22\\w{2}\\W\\S+";
	public static final String DomainName = "wanfangdata.com.cn";
	


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
    	boolean belong_to_clg = false;
    	System.out.println("URL: "+page_url);
    	
    	// Get paper DB name from extra
    	Request req_must = page.getRequest();
		Object obj_must = req_must.getExtra("db_name");
		String DBName = (String) obj_must;
		System.out.println("this page belongs to table: "+DBName);
    	
    	// If current page is not a level2 list page
    	if(!page.getUrl().regex(List_URL).match()){
    		
    		// ------------------------Page is a content page, start extracting paper info...------------------------
    		if(page_url.contains("Periodical")||page_url.contains("Conference")||page_url.contains("NSTLQK")||page_url.contains("NSTLHY")){
    			
    			if(page_url.contains("NSTLQK") || page_url.contains("NSTLHY")) {
    	    		System.out.println("Skipping...");
    	    		page.setSkip(true);
    	    	}
    	    	// Extract previously stored field info from paper_pass object in Request
    			
    			
    			Request req_get = page.getRequest();
        		Object obj_get = req_get.getExtra("paper_pass");
        		Paper ppr_get = (Paper) obj_get;
        		
        		
        		// Initializing page info variables
        		String ppr_class_lvl1 = "Unkown";
        		String ppr_class_lvl2 = "Unkown";
        		String ppr_webId = "Unkown";
        		int ppr_ref = -1;
        		String ppr_org = "Unkown";
        		
        		// If cannot find class info go to DB for searching
        		if(obj_get==null || obj_get==null || ppr_get==null){
        			System.out.println("Cannot find page request information, searching in PageAbstract table...");
        			// Get webId from url string
        			String web_id_get_from_url = page_url.split(".com.cn")[1].substring(1);  
        			
        			Connection conn = null;
        			try {
            			conn  = ConnectionUtils.getConnection();
            			PageAbstract page_abs = DBUtils.getPageAbstract(conn, web_id_get_from_url);
            			ppr_class_lvl1 = page_abs.getClassLvl1();
                		ppr_class_lvl2 = page_abs.getClassLvl2();
                		ppr_webId = page_abs.getWebId();
                		ppr_ref = page_abs.getPageRef();
                		ppr_org = page_abs.getPageOrg();
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
        		// Else just get info from the paper class we get
        		else{
        			System.out.println("Find page request information");
        			ppr_class_lvl1 = ppr_get.getPaperClassLvl1();
            		ppr_class_lvl2 = ppr_get.getPaperClassLvl2();
            		ppr_webId = ppr_get.getPaperWebId();
            		ppr_ref = ppr_get.getPaperReferences();
            		Object obj_ppr_org =  req_get.getExtra("paper_org");
        			ppr_org = (String) obj_ppr_org;
        		}
        		
    			// If paper Organizations name contains quotes, remove white spaces in the string
        		ppr_org = ppr_org.replaceAll("\\s+","");

    			// Extract paper Organizations for validating if paper belongs to proper college
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
    				if((ppr_org!=null && ppr_org!="") && (paper_org_temp.equals(ppr_org) || paper_org_temp.contains(ppr_org))){
    					belong_to_clg = true;
    					break;
    				}
    				if(paper_org_pre_arr.length>1){
    					String paper_org_temp_2 = paper_org_pre_arr[1].trim();
    					System.out.println("!!!!资料页的院校名称 : "+paper_org_temp_2);
    					if((ppr_org!=null && ppr_org!="") && (paper_org_temp_2.equals(ppr_org) || paper_org_temp_2.contains(ppr_org))){
        					belong_to_clg = true;
        					break;
        				}  
    				}
    				if(paper_org_pre_arr.length>2){
    					String paper_org_temp_3 = paper_org_pre_arr[2].trim();
    					System.out.println("!!!!资料页的院校名称 : "+paper_org_temp_3);
    					if((ppr_org!=null && ppr_org!="") && (paper_org_temp_3.equals(ppr_org) || paper_org_temp_3.contains(ppr_org))){
        					belong_to_clg = true;
        					break;
        				}  
    				} 
    			}
    			
    			//If this is a valid paper, then we continue extracting detail info and save them to DB
    			if(belong_to_clg){
    				
    				// Extract author name
        	    	String ppr_auth1=null;
        	    	String ppr_auth2=null;
        	    	String ppr_auth3=null;
        	    	//List<String> auth_list  = ExtractAuthor(page);
        	    	List<String> auth_list = page.getHtml().xpath("//div[@class='row row-author']//a/text()").all();
        	    	    	
        	    	if(auth_list.size()>0)  ppr_auth1 = auth_list.get(0);
        	    	if(auth_list.size()>1)  ppr_auth2 = auth_list.get(1);
        	    	if(auth_list.size()>2)  ppr_auth3 = auth_list.get(2);
        	    	System.out.println("Athor1: "+ppr_auth1);
        	    	System.out.println("Athor2: "+ppr_auth2);
        	    	System.out.println("Athor3: "+ppr_auth3);
         
        	 		// Extract Title
        			//String ppr_title = ExtractTitle(page);	
        	    	String ppr_title = page.getHtml().xpath("//head/title/text()").toString();
        	    	System.out.println("Title: "+ppr_title);
        	     			   			
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
        	    	Paper ppr_sent = new Paper();
        	    	ppr_sent.setPaperAuthorLvl1(ppr_auth1);
        	    	ppr_sent.setPaperAuthorLvl2(ppr_auth2);
        	    	ppr_sent.setPaperAuthorLvl3(ppr_auth3);
        	    	ppr_sent.setPaperTitle(ppr_title);
        	    	ppr_sent.setPaperDate(ppr_date);
        	    	ppr_sent.setPaperSource(ppr_source);
        	    	ppr_sent.setPaperRecord(ppr_record);
        	    	ppr_sent.setPaperFunds(ppr_funds);
        	    	ppr_sent.setPaperClassLvl1(ppr_class_lvl1);
        	    	ppr_sent.setPaperClassLvl2(ppr_class_lvl2);
        	    	ppr_sent.setPaperReferences(ppr_ref);
        	    	ppr_sent.setPaperWebId(ppr_webId);

        	    	//System.out.println("Author1 is: "+ppr_get.getPaperAuthorLvl1());
        	    	//System.out.println("Author2 is: "+ppr_get.getPaperAuthorLvl2());
        	    	//System.out.println("Author3 is: "+ppr_get.getPaperAuthorLvl3());
        	    	//System.out.println("Title is: "+ppr_get.getPaperTitle());
        	    	//System.out.println("date is: "+ppr_get.getPaperDate());
        	    	//System.out.println("Source is: "+ppr_get.getPaperSource());
        	    	//System.out.println("record is: "+ppr_get.getPaperRecord());
        	    	//System.out.println("Funds is: "+ppr_get.getPaperFunds());
        	    	//System.out.println("class1 is: "+ppr_get.getPaperClassLvl1());
        	    	//System.out.println("class2 is: "+ppr_get.getPaperClassLvl2());
        	    	//System.out.println("webid is: "+ppr_get.getPaperWebId());
        	    	//System.out.println("ref is: "+ppr_get.getPaperReferences());
    				System.out.println("!!!!已找到 ");
    				
    				// If cannot find Dbname in the request extra field, get it from database 
    				if(obj_must==null || DBName ==null || DBName.equals("")){
    					System.out.println("Cannot find content page paper db table name");
    					College college;
						try {
							college = DBUtils.getCollegeByName(ppr_org.trim());
							int college_ID = college.getCollegeId();
	    					DBName = DBUtils.getPaperDBNameByCollegeId(college_ID);
						} catch (SQLException e) {
							System.out.println("SQL EXception executing DBUtils.getCollegeByName");
							e.printStackTrace();
						} 
						System.out.println("Find DBname in DBinfo table: "+ DBName);
    				}
    				
    				// Insert ppr_get info into DB
        			Connection conn  = null;
        			try {
            			conn  = ConnectionUtils.getConnection();
                        DBUtils.insertPaper(conn, ppr_sent,DBName);
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();           
                    }finally {                      
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException e) { /* ignored */}
                        }
                    }
    			}else{
    				System.out.println("@@@不符合");
    			}
    	    	 		
    		}else{
    	//--------------------------page is a lvl0 or lvl1 list page------------------------
    			// If database table name is not accessible, search in DBinfo by college name
        		if(obj_must==null || DBName ==null || DBName.equals("")){
        			System.out.println("Cannot find lvl1 or lvl0 page paper db table name");
        			// Extracting paper orgnizations
        			String paper_orgniztion = page.getHtml().xpath("//form[@id='filterForm']//input[@id='filterBaseQuery']/@value").toString();
        	    	int org_name_idx1 = paper_orgniztion.indexOf(":");
        	    	int org_name_idx2 = paper_orgniztion.indexOf("分类号");
        	    	paper_orgniztion = org_name_idx2==-1?paper_orgniztion.substring(org_name_idx1).trim():paper_orgniztion.substring(org_name_idx1+1, org_name_idx2).trim();
        	    	
        	    	// If paper Organizations name contains quotes, remove white spaces in the string
        	    	paper_orgniztion = paper_orgniztion.replaceAll("\\s+","");
            		
    				College college;
    				try {
    					college = DBUtils.getCollegeByName(paper_orgniztion.trim());
    					int college_ID = college.getCollegeId();
    					DBName = DBUtils.getPaperDBNameByCollegeId(college_ID);
    				} catch (SQLException e) {
    					System.out.println("SQL EXception executing DBUtils.getCollegeByName");
    					e.printStackTrace();
    				}   
    				System.out.println("Find DBname in DBinfo table: "+ DBName);
    			}
	    		//page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-0 subjectCatagory']").links().all());
	    		//page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-1 subjectCatagory']").links().all());
	    		List<String> lvl_0_sub_list = page.getHtml().xpath("//p[@class='cluster-item tree-0 subjectCatagory']").links().all();
	    		List<String> lvl_1_sub_list = page.getHtml().xpath("//p[@class='cluster-item tree-1 subjectCatagory']").links().all();
	    		if(!lvl_0_sub_list.isEmpty()){
	    			for(String lvl_0_sub:lvl_0_sub_list){
	    				Request req = new Request(lvl_0_sub);
	    				req.putExtra("db_name", DBName);
	    				page.addTargetRequest(req);
	    			}
	    		}
	    		if(!lvl_1_sub_list.isEmpty()){
	    			for(String lvl_1_sub:lvl_1_sub_list){
	    				Request req = new Request(lvl_1_sub);
	    				req.putExtra("db_name", DBName);
	    				page.addTargetRequest(req);
	    			}
	    		}
	    		
	    		// Get all hidden lvl1 and lvl2 page links into the queue
	    		//page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-0 more']").links().all());
	    		//page.addTargetRequests(page.getHtml().xpath("//p[@class='cluster-item tree-1 more']").links().all());
	    		List<String> lvl_0_hidden_list = page.getHtml().xpath("//p[@class='cluster-item tree-0 more']").links().all();
	    		List<String> lvl_1_hidden_list = page.getHtml().xpath("//p[@class='cluster-item tree-1 more']").links().all();
	    		if(!lvl_0_sub_list.isEmpty()){
	    			for(String lvl_0_hidden:lvl_0_hidden_list){
	    				Request req = new Request(lvl_0_hidden);
	    				req.putExtra("db_name", DBName);
	    				page.addTargetRequest(req);
	    			}
	    		}
	    		if(!lvl_1_sub_list.isEmpty()){
	    			for(String lvl_1_hidden:lvl_1_hidden_list){
	    				Request req = new Request(lvl_1_hidden);
	    				req.putExtra("db_name", DBName);
	    				page.addTargetRequest(req);
	    			}
	    		}
    		}
    		
    	// ------------------------current page is a level 2 list page----------------------------------
    	}else{
    		// If database table name is not accessible, search in DBinfo by college name
    		if(obj_must==null || DBName ==null || DBName.equals("")){
    			System.out.println("Cannot find lvl2 page paper db table name");
    			// Extracting paper orgnizations
    			String paper_orgniztion = page.getHtml().xpath("//form[@id='filterForm']//input[@id='filterBaseQuery']/@value").toString();
    	    	int org_name_idx1 = paper_orgniztion.indexOf(":");
    	    	int org_name_idx2 = paper_orgniztion.indexOf("分类号");
    	    	paper_orgniztion = org_name_idx2==-1?paper_orgniztion.substring(org_name_idx1).trim():paper_orgniztion.substring(org_name_idx1+1, org_name_idx2).trim();
    	    	
    	    	// If paper Organizations name contains quotes, remove white spaces in the string
    	    	paper_orgniztion = paper_orgniztion.replaceAll("\\s+","");
        		
				College college;
				try {
					college = DBUtils.getCollegeByName(paper_orgniztion.trim());
					int college_ID = college.getCollegeId();
					DBName = DBUtils.getPaperDBNameByCollegeId(college_ID);
				} catch (SQLException e) {
					System.out.println("SQL EXception executing DBUtils.getCollegeByName");
					e.printStackTrace();
				} 
				System.out.println("Find DBname in DBinfo table: "+ DBName);
			}
    		
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
                		Request req = new Request(url_next);
	    				req.putExtra("db_name", DBName);
	    				page.addTargetRequest(req);
                		//page.addTargetRequest(url_next);
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
				
				// Create new Paper with WebID, Ref, Class1 and Class2 field filled
				Paper ppr = new Paper();
				ppr.setPaperWebId(webID_list.get(i));
				ppr.setPaperReferences(ref_list.get(i));
    			ppr.setPaperClassLvl1(curr_lvl1_node);
    			ppr.setPaperClassLvl2(curr_lvl2_node);
    			
    			// Load individual content page link to webDownloader
    			Request req = new Request(content_url_list.get(i));
        		req.putExtra("paper_pass", ppr);
        		req.putExtra("paper_org", paper_orgniztion);
        		req.putExtra("db_name", DBName);
        		page.addTargetRequest(req);
        		
        		// Insert page abstract info into the DB
        		PageAbstract pageabs = new PageAbstract();
        		pageabs.setWebId(ppr.getPaperWebId());
        		pageabs.setClassLvl1(ppr.getPaperClassLvl1());
        		pageabs.setClassLvl2(ppr.getPaperClassLvl2());
        		pageabs.setPageOrg(paper_orgniztion);
        		pageabs.setPageRef(ppr.getPaperReferences());
        		
        		
        		/*
        		Connection conn4  = null;
        		try {
        			conn4  = ConnectionUtils.getConnection();
                    PageAbstract ppp = DBUtils.getPageAbstract(conn4, ppr.getPaperWebId());
                    if(ppp.getClassLvl1()!=pageabs.getClassLvl1()) System.out.println("!!!!!!!!!!!class1 orignal is: "+ppp.getClassLvl1() +  " ,class1 new is: "+pageabs.getClassLvl1());
                    if(ppp.getClassLvl2()!=pageabs.getClassLvl2()) System.out.println("!!!!!!!!!!!class2 orignal is: "+ppp.getClassLvl2() +  " ,class2 new is: "+pageabs.getClassLvl2());
                    if(ppp.getPageOrg()!=pageabs.getPageOrg())System.out.println("!!!!!!!!!!!orgs orignal is: "+ppp.getPageOrg() +  " ,orgs new is: "+pageabs.getPageOrg());
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();           
                }finally {                      
                    if (conn4 != null) {
                        try {
                            conn4.close();
                        } catch (SQLException e) { }
                    }
                }
        		*/
        		
        		Connection conn  = null;
        		try {
        			conn  = ConnectionUtils.getConnection();
                    DBUtils.insertPageAbstract(conn, pageabs);
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
       
  
   
        
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
    	BasicConfigurator.configure();
    	
    	DBinfo dbinfo = null;
		try {
			College college = DBUtils.getCollegeByName(Name);
			int clg_id = college.getCollegeId();
			System.out.println("College ID for "+Name+ " in college table is: "+ clg_id);
			if(DBUtils.getDBinfoById(clg_id)!=null){
				System.out.println("WARNING: this college already exists in the database");
				dbinfo = DBUtils.getDBinfoById(clg_id);
			}else{
				System.out.println("NOTICE: this is a new college, no previous record in DB");
				System.out.println("NOTICE: Initializing new tables for the college...");
				
				DBUtils.insertNewDBinfo(clg_id);
				System.out.println("Inserting new records in DB info table complete...");
				
				dbinfo = DBUtils.getDBinfoById(clg_id);
				System.out.println("Getting dbinfo...");
				
				DBUtils.createNewPaperDB(dbinfo.getDBName());
				System.out.println("Creating new paper table for" + Name +"complete...");
				
				DBUtils.createNewPatentDB(dbinfo.getDBPatentName());
				System.out.println("Creating new patent table for" + Name +"complete...");
				// to be implemented: create paperScore table
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
    	
    	String clg_url_prefix = "http://s.wanfangdata.com.cn/Paper.aspx?q=";
    	String clg_url_suffix = "&f=top";
    	String clg_url_content_part1 = "作者单位";
    	String encoded_content_part1 = null;
    	String encoded_name = null;
		try {
			encoded_content_part1 = URLEncoder.encode(clg_url_content_part1, "UTF-8");
			encoded_name = URLEncoder.encode(Name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String final_url = clg_url_prefix+encoded_content_part1+"%3A"+encoded_name+clg_url_suffix;
		System.out.println("Start crawling from url: "+ final_url);
		
		if(dbinfo!=null){
			String db_name = dbinfo.getDBName();
			Request req = new Request(final_url);
			req.putExtra("db_name", db_name);
			Spider.create(new test_demo()).addRequest(req)
	    	.setScheduler(new FileCacheQueueScheduler(CacheFile))
	    	.thread(ThreadsNum)
	        .run(); 
		}
        
    }
    
    /*
    // Extract author name
    public List<String> ExtractAuthor(Page page){
		List<String> auth_list = page.getHtml().xpath("//div[@class='row row-author']//a/text()").all();
		return auth_list;
    }
    
    // Extract Title
    public String ExtractTitle(Page page){
    	return page.getHtml().xpath("//head/title/text()").toString();
    }
    */
}
