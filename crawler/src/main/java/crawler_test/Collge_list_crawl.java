package crawler_test;
import org.apache.log4j.BasicConfigurator;


import us.codecraft.webmagic.Page;
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

import org.vess.edu.beans.*;
import org.vess.edu.conn.*;
import org.vess.edu.utils.*;

public class Collge_list_crawl implements PageProcessor {
	//高校列表按地区URL
	//http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%E6%A1%88*%22+DBID%3ajydx
	//用于支持断点续抓的缓存文件的路径，需提前新建文件夹，
	public static final String CacheFile = "/Users/sikai/Documents/workspace/crawler_test/Logs/college_list";
	//每个抓取页面间的时间间隔
	public static final int SleepTime = 30000;
	//抓取请求发出后无响应多久判定为Timeout
	public static final int TimeOutTime = 20000;
	//对抓取失败的页面重试次数
	public static final int RetryTimes = 5;
	//同时运行的线程数
	public static final int ThreadsNum = 3;
	
	public static final String url_a = "http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%E6%A1%88*%22+DBID%3ajydx";

    public static final String URL_LIST = "http://blog\\.sina\\.com\\.cn/s/articlelist_1487828712_0_\\d+\\.html";

  
    //      http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%  E6%A1%88  *%22+DBID%3ajydx+%E5%8A%9E%E5%AD%A6%E7%B1%BB%E5%9E%8B%3a%  E6%99%AE%E9%80%9A%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1&f=Col.Type&p=2 
    //北京   http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%E6%A1%88*%22+DBID%3ajydx
    //天津   E6%9A%97*%22+DBID%3acecdb
    //河北   E6%98%82*%22+DBID%3acecdb
    //山西   E5%87%B9*%22+DBID%3acecdb
    //内蒙   E7%86%AC*%22+DBID%3acecdb
    //辽宁   E8%8A%AD*%22+DBID%3acecdb
    //吉林   E6%8D%8C*%22+DBID%3acecdb
    //黑龙   E6%8B%94*%22+DBID%3acecdb
    //上海   E6%96%91*%22+DBID%3acecdb
    //江苏   E6%89%B3*%22+DBID%3acecdb
    //浙江   E8%88%AC*%22+DBID%3acecdb
    //安徽   E6%90%AC*%22+DBID%3acecdb
    //福建   E7%89%88*%22+DBID%3acecdb
    //江西   E5%8D%8A*%22+DBID%3acecdb
    //山东   E6%89%AE*%22+DBID%3acecdb
    //河南   E6%A6%9C*%22+DBID%3acecdb
    //湖北   E8%9A%8C*%22+DBID%3acecdb
    //湖南   E6%A3%92*%22+DBID%3acecdb
    //广东   E7%A3%85*%22+DBID%3acecdb
    //广西   E5%8C%85*%22+DBID%3acecdb
    //海南   E8%83%9E*%22+DBID%3acecdb
    //重庆   E6%8A%A5*%22+DBID%3acecdb
    //四川   E8%B1%B9*%22+DBID%3acecdb
    //贵州   E6%9A%B4*%22+DBID%3acecdb
    //云南   E6%9D%AF*%22+DBID%3acecdb
    //西藏   E7%A2%91*%22+DBID%3acecdb
    //陕西   E5%A5%94*%22+DBID%3acecdb
    //甘肃   E8%8B%AF*%22+DBID%3acecdb
    //青海   E5%B4%A9*%22+DBID%3acecdb
    //宁夏   E7%94%AD*%22+DBID%3acecdb
    //新疆   E8%BF%B8*%22+DBID%3acecdb
    
    public static final List<String> college_city1_list = new ArrayList<>(Arrays.asList("E6%A1%88","E6%9A%97","E6%98%82","E5%87%B9","E7%86%AC",
    								"E8%8A%AD","E6%8D%8C","E6%8B%94","E6%96%91","E6%89%B3","E8%88%AC","E6%90%AC","E7%89%88","E5%8D%8A",
    								"E6%89%AE","E6%A6%9C","E8%9A%8C","E6%A3%92","E7%A3%85","E5%8C%85","E8%83%9E","E6%8A%A5","E8%B1%B9",
    								"E6%9A%B4","E6%9D%AF","E7%A2%91","E5%A5%94","E8%8B%AF","E5%B4%A9","E7%94%AD","E8%BF%B8"));
    			
    public static final List<String> college_city1_list_cn = new ArrayList<>(Arrays.asList("北京","天津","河北","山西","内蒙古","辽宁","吉林","黑龙江",
    																					   "上海","江苏","浙江","安徽","福建","江西","山东","河南",
    																					   "湖北","湖南","广东","广西","海南","重庆","四川","贵州",
    																					   "云南","西藏","陕西","甘肃","青海","宁夏","新疆"								
    																		));
   	
    /*
    public static final List<String> college_city1_list = new ArrayList<>(Arrays.asList("案","暗","昂","凹","熬","芭","捌","拔","斑","扳","般","搬",
    																					"版","半","扮","榜","蚌","棒","磅","包","胞","报","豹","暴",
    																					"杯","碑","奔","苯","崩","甭","迸"));
    */
    
    //http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%E6%A1%88*%22+DBID%3ajydx+%E5%8A%9E%E5%AD%A6%E7%B1%BB%E5%9E%8B%3a%E6%99%AE%E9%80%9A%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1&f=Col.Type
    //普通高等学校: E6%99%AE%E9%80%9A%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1&f=Col.Type
    //中等专业学校: E4%B8%AD%E7%AD%89%E4%B8%93%E4%B8%9A%E5%AD%A6%E6%A0%A1&f=Col.Type
    //成人高等学校: E6%88%90%E4%BA%BA%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1&f=Col.Type
    //普通高等学校(民办): E6%99%AE%E9%80%9A%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1(%E6%B0%91%E5%8A%9E)&f=Col.Type
    //独立学院: E7%8B%AC%E7%AB%8B%E5%AD%A6%E9%99%A2&f=Col.Type
    //培养研究生的科研机构: E5%9F%B9%E5%85%BB%E7%A0%94%E7%A9%B6%E7%94%9F%E7%9A%84%E7%A7%91%E7%A0%94%E6%9C%BA%E6%9E%84&f=Col.Type
    //其他教学点: E5%85%B6%E4%BB%96%E6%95%99%E5%AD%A6%E7%82%B9&f=Col.Type
                                                                                            
    public static final List<String> college_type_list = new ArrayList<>(Arrays.asList("E6%99%AE%E9%80%9A%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1&f=Col.Type", 
    																				   "E4%B8%AD%E7%AD%89%E4%B8%93%E4%B8%9A%E5%AD%A6%E6%A0%A1&f=Col.Type",
    																				   "E6%88%90%E4%BA%BA%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1&f=Col.Type",
    																				   "E6%99%AE%E9%80%9A%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1(%E6%B0%91%E5%8A%9E)&f=Col.Type",
    																				   "E7%8B%AC%E7%AB%8B%E5%AD%A6%E9%99%A2&f=Col.Type",
    																				   "E5%9F%B9%E5%85%BB%E7%A0%94%E7%A9%B6%E7%94%9F%E7%9A%84%E7%A7%91%E7%A0%94%E6%9C%BA%E6%9E%84&f=Col.Type",
    																				   "E5%85%B6%E4%BB%96%E6%95%99%E5%AD%A6%E7%82%B9&f=Col.Type"));
    
    public static final List<String> college_type_list_cn = new ArrayList<>(Arrays.asList("普通高等学校","中等专业学校","成人高等学校","普通高等学校(民办)",
    																					  "独立学院","培养研究生的科研机构","其他教学点"));

    private Site site = Site
            .me()
            .setDomain("s.wanfangdata.com.cn")
            .setTimeOut(TimeOutTime)
            .setCycleRetryTimes(RetryTimes)
            .setSleepTime(SleepTime)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        /*
        if (page.getUrl().regex(URL_LIST).match()) {
            page.addTargetRequests(page.getHtml().xpath("//div[@class=\"articleList\"]").links().regex(URL_POST).all());
            page.addTargetRequests(page.getHtml().links().regex(URL_LIST).all());
            //文章页
        } else {
            page.putField("title", page.getHtml().xpath("//div[@class='articalTitle']/h2"));
            page.putField("content", page.getHtml().xpath("//div[@id='articlebody']//div[@class='articalContent']"));
            page.putField("date",
                    page.getHtml().xpath("//div[@id='articlebody']//span[@class='time SG_txtc']").regex("\\((.*)\\)"));
        }
        */
    
    	List<String> col_title_list = page.getHtml().xpath("//div[@class='record-item']//div[@class='left-record']//div[@class='record-title']/a/text()").all();
    	List<String> col_lvl_list = page.getHtml().xpath("//div[@class='record-item']//div[@class='left-record']//div[@class='record-title']//span[@class='type-span']/text()").all();
    	//List<String> col_city2_list = page.getHtml().xpath("//div[@class='record-item']//div[@class='left-record']//div[@class='record-subtitle']//span[last()]/text()").all();
    	List<String> col_city2_list_temp = page.getHtml().xpath("//div[@class='record-item']//div[@class='left-record']//div[@class='record-subtitle']//span/text()").all();
    	
    	//Process(clean) lvl2 city name
    	List<String> col_city2_list = new ArrayList<String>();
    	for(int i=0;i<col_city2_list_temp.size();i++){
    		if(i%2==0) continue;
    		String city_temp = (String) col_city2_list_temp.get(i);
    		city_temp = city_temp.substring(4,city_temp.length());
    		col_city2_list.add(city_temp);
    	}
    	
    	//Get next pages
    	String page_url = page.getUrl().toString();
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
            		page.addTargetRequest(url_next);
            	}  		
        	}
    	}
    	
    	
    	
    	//Get city lvl1 from URL
    	int idx_temp1 = page_url.indexOf("%22%")+4;
    	String city1_seg_cn = page_url.substring(idx_temp1, idx_temp1+8);
    	int idx_city_in_list = college_city1_list.indexOf(city1_seg_cn);
    	String city_name = college_city1_list_cn.get(idx_city_in_list);
    	System.out.println("city name is: "+ city_name);
    	
    	//Get college type from URL
    	int idx_temp2 = idx_temp1+65;
    	String type_seg_cn = page_url.substring(idx_temp2);
    	if(type_seg_cn.contains("&p=")){
    		type_seg_cn = type_seg_cn.substring(0,type_seg_cn.indexOf("&p="));
    	}
    	System.out.println("type msg is: "+ type_seg_cn);
    	
    	int idx_type_in_list = college_type_list.indexOf(type_seg_cn);
    	String type_name = college_type_list_cn.get(idx_type_in_list);
    	System.out.println("type name is: "+ type_name);
    	
    	//INSERT INTO SQL 	
    	for(int i=0;i<col_title_list.size();i++){
    		College col = new College();
        	col.setCollegeCityLvl1(city_name);
        	col.setCollegeCityLvl2(col_city2_list.get(i));
        	col.setCollegeTitle(col_title_list.get(i).trim());
        	col.setCollegeGrade(col_lvl_list.get(i));
    		col.setCollegeType(type_name);
    		
    		Connection conn = null;
    		try {
    			conn  = ConnectionUtils.getConnection();
                DBUtils.insertProduct(conn, col);
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

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
    	BasicConfigurator.configure();
      
    	for(String clg_city1:college_city1_list){
    		for(String clg_type:college_type_list){
    			String clg_url="http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%"
    					       +clg_city1+"*%22+DBID%3ajydx"
    					       +"+%E5%8A%9E%E5%AD%A6%E7%B1%BB%E5%9E%8B%3a%"
    					       +clg_type;
    			Spider.create(new Collge_list_crawl()).addUrl(clg_url)
    			.setScheduler(new FileCacheQueueScheduler(CacheFile))
        		.thread(ThreadsNum)
                .run(); 
    		}
    	}
    	
    	
    	/*
    	-------------FOR TESTING-------------------------------
    	String clg_url="http://s.wanfangdata.com.cn/Cecdb.aspx?q=%E5%9C%B0%E5%8C%BA%E4%BB%A3%E7%A0%81%3a%22%"
    			+ college_city1_list.get(0)+"*%22+DBID%3ajydx"
    			+"+%E5%8A%9E%E5%AD%A6%E7%B1%BB%E5%9E%8B%3a%"
    			+college_type_list.get(6);
    	Spider.create(new Collge_list_crawl()).addUrl(clg_url)
		.thread(5)
        .run(); 
        */
    }
}