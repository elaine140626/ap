package com.gao.controller.graphicalupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.hibernate.engine.jdbc.SerializableClobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.gao.controller.base.BaseController;
import com.gao.model.calculate.CalculateRequestData;
import com.gao.model.calculate.CalculateResponseData;
import com.gao.model.calculate.FSDiagramModel;
import com.gao.model.calculate.WellAcquisitionData;
import com.gao.service.base.CommonDataService;
import com.gao.service.graphicalupload.GraphicalUploadService;
import com.gao.utils.Config;
import com.gao.utils.Constants;
import com.gao.utils.DataModelMap;
import com.gao.utils.OracleJdbcUtis;
import com.gao.utils.Page;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import oracle.sql.CLOB;

/**<p>描述：后台管理-报警设置Action</p>
 * 
 * @author gao 2014-06-06
 * @version 1.0
 *
 */
@Controller
@RequestMapping("/graphicalUploadController")
@Scope("prototype")
public class GraphicalUploadController extends BaseController {
	private static Log log = LogFactory.getLog(GraphicalUploadController.class);
	private static final long serialVersionUID = -281275682819237996L;
	private List<File> SurfaceCardFile;
	private List<String> SurfaceCardFileFileName;// 上传文件的名字 ,FileName 固定的写法  
	private List<String> SurfaceCardFileContentType ; //上传文件的类型， ContentType 固定的写法
	
	
	
//	private DistreteAlarmLimit limit;

	@Autowired
	private GraphicalUploadService<?> raphicalUploadService;
	@Autowired
	private CommonDataService commonDataService;
	
	/**<p>描述：显示功图类型列表</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getSurfaceCardTypeList")
	public String getSurfaceCardTypeList() throws Exception {
		String json = "";
		this.pager = new Page("pagerForm", request);
		json = raphicalUploadService.getSurfaceCardTrpeList();
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset="
				+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**<p>描述：保存上传功图文件</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({"unchecked" })
	@RequestMapping("/saveUploadSurfaceCardFile")
	public String saveUploadSurfaceCardFile() throws Exception {
		Map<String, Object> map = DataModelMap.getMapObject();
		Map<String,String> surfaceCardFileMap=(Map<String, String>) map.get("surfaceCardFileMap");//从内存中将上传的功图取出来
		String json = "{success:true,flag:true}";
		String uploadSurfaceCardListStr = ParamUtils.getParameter(request, "uploadSurfaceCardListStr");
		String uploadAll = ParamUtils.getParameter(request, "uploadAll");
		Gson gson=new Gson();
		java.lang.reflect.Type type = null;
		if(surfaceCardFileMap!=null){
			if("1".equals(uploadAll)){
				for(String key : surfaceCardFileMap.keySet()){
					try{
						String diagramData =surfaceCardFileMap.get(key);
						type = new TypeToken<FSDiagramModel>() {}.getType();
						FSDiagramModel FSDiagramModel = gson.fromJson(diagramData, type);
						raphicalUploadService.saveSurfaceCard(FSDiagramModel);
					}catch(Exception e){
						continue;
					}	
				}
				
			}else{
				String uploadSurfaceCardListStrArr[]=uploadSurfaceCardListStr.split(",");
				for(int i=0;i<uploadSurfaceCardListStrArr.length;i++){
					try{
						for(String key : surfaceCardFileMap.keySet()){
							if(key.equals(uploadSurfaceCardListStrArr[i])){
								String diagramData =surfaceCardFileMap.get(key);
								type = new TypeToken<FSDiagramModel>() {}.getType();
								FSDiagramModel FSDiagramModel = gson.fromJson(diagramData, type);
								raphicalUploadService.saveSurfaceCard(FSDiagramModel);
							}
						}
					}catch(Exception e){
						continue;
					}
				}
			}
		}
		response.setContentType("application/json;charset="
				+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/getSurfaceCardGraphicalData")
	public String getSurfaceCardGraphicalData() throws Exception {
		StringBuffer result_json = new StringBuffer();
		Map<String, Object> map = DataModelMap.getMapObject();
		Map<String,String> surfaceCardFileMap=(Map<String, String>) map.get("surfaceCardFileMap");//从内存中将上传的功图取出来
		String json = "{}";
		String param = ParamUtils.getParameter(request, "param");
		if(surfaceCardFileMap!=null){
			for(String key : surfaceCardFileMap.keySet()){
				if(key.equals(param)){
					json =surfaceCardFileMap.get(key);
					break;
				}
			}
		}
		response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**<p>描述：解析上传功图文件</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getUploadSurfaceCardFile")
	public String getUploadSurfaceCardFile(@RequestParam("file") CommonsMultipartFile[] files,HttpServletRequest request) throws Exception {
		StringBuffer result_json = new StringBuffer();
		String surfaceCardType = ParamUtils.getParameter(request, "surfaceCardType");
		String json="";
		if("101".equals(surfaceCardType)){
			getUploadSurfaceCardFile101(files);
		}else if("121".equals(surfaceCardType)){
			getUploadSurfaceCardFile121(files);
		}else{
			String columns = "[{ \"header\":\"序号\",\"dataIndex\":\"id\",width:50},{ \"header\":\"井名\",\"dataIndex\":\"wellname\"},{ \"header\":\"功图采集时间\",\"dataIndex\":\"cjsj\"}]";
			result_json.append("{ \"success\":true,\"flag\":true,\"columns\":"+columns+",");
			result_json.append("\"totalCount\":"+SurfaceCardFileFileName.size()+",");
			result_json.append("\"totalRoot\":[]}");
			json=result_json.toString();
			//HttpServletResponse response = ServletActionContext.getResponse();
			response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
			response.setHeader("Cache-Control", "no-cache");
			PrintWriter pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		}
		return null;
	}
	
	
	
	@RequestMapping("/upload2"  )  
    public String upload2(HttpServletRequest request,HttpServletResponse response) throws IllegalStateException, IOException {
        //创建一个通用的多部分解析器  
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());  
        //判断 request 是否有文件上传,即多部分请求  
        if(multipartResolver.isMultipart(request)){  
            //转换成多部分request    
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;  
            //取得request中的所有文件名  
            Iterator<String> iter = multiRequest.getFileNames();  
            while(iter.hasNext()){
                //记录上传过程起始时的时间，用来计算上传时间  
                int pre = (int) System.currentTimeMillis();  
                //取得上传文件  
                MultipartFile file = multiRequest.getFile(iter.next());  
                if(file != null){  
                    //取得当前上传文件的文件名称  
                    String myFileName = file.getOriginalFilename();  
                    //如果名称不为“”,说明该文件存在，否则说明该文件不存在  
                    if(myFileName.trim() !=""){  
//                        System.out.println(myFileName);  
                        //重命名上传后的文件名  
                        String fileName = "demoUpload" + file.getOriginalFilename();  
                        //定义上传路径  
                        String path = "H:/" + fileName;  
                        File localFile = new File(path);  
                        file.transferTo(localFile);  
                    }  
                }  
                //记录上传该文件后的时间  
                int finaltime = (int) System.currentTimeMillis();  
//                System.out.println(finaltime - pre);  
            }  
              
        }  
        return "/success";  
    }
	
	/**<p>蚌埠</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({"unchecked"})
	@RequestMapping("/getUploadSurfaceCardFile101")
	public String getUploadSurfaceCardFile101(CommonsMultipartFile[] files) throws Exception {
		StringBuffer result_json = new StringBuffer();
		Map<String, Object> map = DataModelMap.getMapObject();
		Map<String,String> surfaceCardFileMap=(Map<String, String>) map.get("surfaceCardFileMap");
		if(surfaceCardFileMap!=null){
			map.remove("surfaceCardFileMap",surfaceCardFileMap);
		}
		surfaceCardFileMap=new HashMap<String,String>();
		String json = "";
		String columns = "[{ \"header\":\"序号\",\"dataIndex\":\"id\",width:50},{ \"header\":\"井名\",\"dataIndex\":\"wellName\"},{ \"header\":\"功图采集时间\",\"dataIndex\":\"acquisitionTime\"},{ \"header\":\"冲程\",\"dataIndex\":\"cch\"},{ \"header\":\"冲次\",\"dataIndex\":\"cci\"}]";
		result_json.append("{ \"success\":true,\"flag\":true,\"columns\":"+columns+",");
		result_json.append("\"totalCount\":"+files.length+",");
		result_json.append("\"totalRoot\":[");
		for(int i=0;i<files.length;i++){
			if(!files[i].isEmpty()){
				try{
					byte[] buffer = files[i].getBytes();
					String diagramData = new String(buffer);
					diagramData=diagramData.replaceAll("\r", "").replaceAll("\n", "\r\n");
			        String diagramDataStrArr[]=diagramData.split("\r\n");
			        String fileName=files[i].getOriginalFilename();
			        String fileNameArr[]=fileName.split("\\.")[0].split("%");
			        String wellName=fileNameArr[0];
//			        String acquisitionTimeStr=fileNameArr[1].replaceAll("-", "").replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
			        String acquisitionTimeStr=diagramDataStrArr[0]+diagramDataStrArr[1];
			        if(acquisitionTimeStr.length()==6){
			        	acquisitionTimeStr="00000000"+acquisitionTimeStr;
			        }else if(acquisitionTimeStr.length()==8){
			        	acquisitionTimeStr+="000000";
			        }
			        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
					SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
					Date date = df2.parse(acquisitionTimeStr);
					String acquisitionTime=df.format(date);
					
					
					StringBuffer diagramDataBuff = new StringBuffer();
					String spm=diagramDataStrArr[3];
					String stroke=diagramDataStrArr[4];
					int pointCount=StringManagerUtils.stringToInteger(diagramDataStrArr[2]);
					String sData="",fData="",wattData="",iData="";
					for(int j=0;j<pointCount;j++){
						sData+=diagramDataStrArr[j*2+5];
						fData+=diagramDataStrArr[j*2+6];
						if(j<pointCount-1){
							sData+=",";
							fData+=",";
						}
			        }
		        	diagramDataBuff.append("{\"wellName\":\""+wellName+"\",\"acquisitionTime\":\""+acquisitionTime+"\",\"stroke\":"+stroke+","+"\"spm\":"+spm+",");
		        	diagramDataBuff.append("\"S\":["+sData+"],");
		        	diagramDataBuff.append("\"F\":["+fData+"],");
		        	diagramDataBuff.append("\"Watt\":["+wattData+"],");
		        	diagramDataBuff.append("\"I\":["+iData+"]");
		        	diagramDataBuff.append("}");
		        	
			        result_json.append("{\"id\":"+(i+1)+",");
					result_json.append("\"wellName\":\""+wellName+"\",");
					result_json.append("\"acquisitionTime\":\""+acquisitionTime+"\",");
					result_json.append("\"stroke\":\""+stroke+"\",");
					result_json.append("\"spm\":\""+spm+"\"},");
					surfaceCardFileMap.put(wellName+"@"+acquisitionTime,diagramDataBuff.toString());
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}
			}
		}
		if(result_json.toString().endsWith(",")){
			result_json.deleteCharAt(result_json.length() - 1);
		}
		result_json.append("]}");
		json=result_json.toString();
		
		map.put("surfaceCardFileMap", surfaceCardFileMap);//将上传的功图文件放到内存中
		
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**<p>上传的功图Excel文件</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({"unchecked"})
	@RequestMapping("/getUploadSurfaceCardFile121")
	public String getUploadSurfaceCardFile121(CommonsMultipartFile[] files) throws Exception {
		StringBuffer result_json = new StringBuffer();
		Map<String, Object> map = DataModelMap.getMapObject();
		Map<String,String> surfaceCardFileMap=(Map<String, String>) map.get("surfaceCardFileMap");
		if(surfaceCardFileMap!=null){
			map.remove("surfaceCardFileMap",surfaceCardFileMap);
		}
		surfaceCardFileMap=new HashMap<String,String>();
		String json = "";
		String tablecolumns = "[{ \"header\":\"序号\",\"dataIndex\":\"id\",width:50},{ \"header\":\"井名\",\"dataIndex\":\"wellName\"},{ \"header\":\"功图采集时间\",\"dataIndex\":\"acquisitionTime\"},{ \"header\":\"冲程\",\"dataIndex\":\"stroke\"},{ \"header\":\"冲次\",\"dataIndex\":\"spm\"}]";
		result_json.append("{ \"success\":true,\"flag\":true,\"columns\":"+tablecolumns+",");
		result_json.append("\"totalCount\":"+files.length+",");
		result_json.append("\"totalRoot\":[");
		for(int i=0;i<files.length;i++){
			if(!files[i].isEmpty()){
				try{
					Workbook rwb=Workbook.getWorkbook(files[i].getInputStream());
					rwb.getNumberOfSheets();
					Sheet oFirstSheet = rwb.getSheet(0);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称  
			        int rows = oFirstSheet.getRows();//获取工作表中的总行数  
			        int columns = oFirstSheet.getColumns();//获取工作表中的总列数  
			        for (int j = 1; j < rows; j++) {
			        	try{
			        		StringBuffer diagramDataBuff = new StringBuffer();
				        	String wellName= oFirstSheet.getCell(1,j).getContents().replaceAll(" ", "");
				        	String acquisitionTimeStr=StringManagerUtils.delOutsideSpace(oFirstSheet.getCell(2,j).getContents()).replaceAll("/", "-");
				        	String sData=oFirstSheet.getCell(3,j).getContents().replaceAll(" ", "").replaceAll("；", ";").replaceAll("，", ",").replaceAll(";", ",");
				        	String fData=oFirstSheet.getCell(4,j).getContents().replaceAll(" ", "").replaceAll("；", ";").replaceAll("，", ",").replaceAll(";", ",");
				        	String wattData=oFirstSheet.getCell(5,j).getContents().replaceAll(" ", "").replaceAll("；", ";").replaceAll("，", ",").replaceAll(";", ",");
				        	String iData=oFirstSheet.getCell(6,j).getContents().replaceAll(" ", "").replaceAll("；", ";").replaceAll("，", ",").replaceAll(";", ",");
				        	String stroke=oFirstSheet.getCell(7,j).getContents().replaceAll(" ", "");
				        	String spm=oFirstSheet.getCell(8,j).getContents().replaceAll(" ", "");
				        	
				        	diagramDataBuff.append("{\"wellName\":\""+wellName+"\",\"acquisitionTime\":\""+acquisitionTimeStr+"\",\"stroke\":"+stroke+","+"\"spm\":"+spm+",");
				        	diagramDataBuff.append("\"S\":["+sData+"],");
				        	diagramDataBuff.append("\"F\":["+fData+"],");
				        	diagramDataBuff.append("\"Watt\":["+wattData+"],");
				        	diagramDataBuff.append("\"I\":["+iData+"]");
				        	diagramDataBuff.append("}");
							
							result_json.append("{\"id\":"+j+",");
							result_json.append("\"wellName\":\""+wellName+"\",");
							result_json.append("\"acquisitionTime\":\""+acquisitionTimeStr+"\",");
							result_json.append("\"stroke\":\""+stroke+"\",");
							result_json.append("\"spm\":\""+spm+"\"},");
							surfaceCardFileMap.put(wellName+"@"+acquisitionTimeStr,diagramDataBuff.toString());
			        	}catch(Exception e){
							continue;
						}
			        }
				}catch(Exception e){
					continue;
				}
			}
		}
		if(result_json.toString().endsWith(",")){
			result_json.deleteCharAt(result_json.length() - 1);
		}
		result_json.append("]}");
		json=result_json.toString();
		
		map.put("surfaceCardFileMap", surfaceCardFileMap);//将上传的功图文件放到内存中
		
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@RequestMapping("/getPSToFSElectricReaultData")
	public String getPSToFSElectricReaultData(@RequestParam("file") CommonsMultipartFile[] files,HttpServletRequest request) throws Exception {
		StringBuffer result_json = new StringBuffer();
		String json="";
		String cjsj="";
		String wellName="";
		int pointCount=0;
		List<Float> currentAList=new ArrayList<Float>();
		List<Float> currentBList=new ArrayList<Float>();
		List<Float> currentCList=new ArrayList<Float>();
		List<Float> voltageAList=new ArrayList<Float>();
		List<Float> voltageBList=new ArrayList<Float>();
		List<Float> voltageCList=new ArrayList<Float>();
		List<Float> activePowerAList=new ArrayList<Float>();
		List<Float> activePowerBList=new ArrayList<Float>();
		List<Float> activePowerCList=new ArrayList<Float>();
		for(int i=0;i<files.length;i++){
			if(!files[i].isEmpty()){
				try{
					if(files[i].getFileItem().getName().lastIndexOf("CSV")>0||files[i].getFileItem().getName().lastIndexOf("csv")>0){
						System.out.println("CSV文件:"+files[i].getFileItem().getName());
					}else if(files[i].getFileItem().getName().lastIndexOf("XLSX")>0||files[i].getFileItem().getName().lastIndexOf("xlsx")>0){
						System.out.println("xlsx文件:"+files[i].getFileItem().getName());
					}else if(files[i].getFileItem().getName().lastIndexOf("XLS")>0||files[i].getFileItem().getName().lastIndexOf("xls")>0){
						System.out.println("xls文件:"+files[i].getFileItem().getName());
						Workbook rwb=Workbook.getWorkbook(files[i].getInputStream());
						rwb.getNumberOfSheets();
						Sheet oFirstSheet = rwb.getSheet(0);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称  
				        int rows = oFirstSheet.getRows();//获取工作表中的总行数  
				        int columns = oFirstSheet.getColumns();//获取工作表中的总列数  
				        
				        for (int j = 1; j < 10; j++) {
				        	if(j==1){
				        		cjsj = oFirstSheet.getCell(1,j).getContents();
				        		wellName= oFirstSheet.getCell(0,j).getContents();
				        	}
				        	String cjsjtemp=oFirstSheet.getCell(1,j).getContents();
				        	if(cjsjtemp.equals(cjsj)){//只解析第一组数据
				        		String bsid=oFirstSheet.getCell(2,j).getContents();
					        	if("1".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*0.02);
						        		currentAList.add(dataReault);
					        		}
					        	}else if("2".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*0.1);
						        		voltageAList.add(dataReault);
					        		}
					        	}else if("3".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*5*0.001);
						        		activePowerAList.add(dataReault);
					        		}
					        	}else if("4".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*0.02);
						        		currentBList.add(dataReault);
					        		}
					        	}else if("5".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*0.1);
						        		voltageBList.add(dataReault);
					        		}
					        	}else if("6".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*5*0.001);
						        		activePowerBList.add(dataReault);
					        		}
					        	}else if("7".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*0.02);
						        		currentCList.add(dataReault);
					        		}
					        	}else if("8".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		if(dataList.size()>pointCount){
					        			pointCount=dataList.size();
					        		}
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*0.1);
						        		voltageCList.add(dataReault);
					        		}
					        	}else if("9".equals(bsid)){
					        		String dataStr=oFirstSheet.getCell(3,j).getContents();
					        		List<String> dataList=StringManagerUtils.SubStringToList(dataStr, 4);
					        		for(int k=0;k<dataList.size();k++){
						        		int data=Integer.valueOf(dataList.get(k).trim(),16).shortValue();
						        		float dataReault=(float) (data*5*0.001);
						        		activePowerCList.add(dataReault);
					        		}
					        	}
				        	}
				        }
				        rwb.close();
					}
					
					
				}catch(Exception e){
					e.printStackTrace();
					result_json = new StringBuffer();
					result_json.append("{\"success\":false}");
					continue;
				}
			}
		}
		System.out.println(result_json.length());
		if(result_json.length()==0){
			result_json.append("{\"success\":true,\"cjsj\":\""+cjsj+"\",\"wellName\":\""+wellName+"\",");
			result_json.append("\"totalRoot\":[");
			for(int i=0;i<pointCount;i++){
				float activePowerSum=0;
				String data="{";
				data+="\"id\":\""+(i+1)+"\",";
				if(currentAList.size()>i){
					data+="\"currentA\":\""+currentAList.get(i)+"\",";
				}
				if(currentBList.size()>i){
					data+="\"currentB\":\""+currentBList.get(i)+"\",";
				}
				if(currentCList.size()>i){
					data+="\"currentC\":\""+currentCList.get(i)+"\",";
				}
				if(voltageAList.size()>i){
					data+="\"voltageA\":\""+voltageAList.get(i)+"\",";
				}
				if(voltageBList.size()>i){
					data+="\"voltageB\":\""+voltageBList.get(i)+"\",";
				}
				if(voltageCList.size()>i){
					data+="\"voltageC\":\""+voltageCList.get(i)+"\",";
				}
				if(activePowerAList.size()>i){
					data+="\"activePowerA\":\""+activePowerAList.get(i)+"\",";
					activePowerSum+=activePowerAList.get(i);
				}
				if(activePowerBList.size()>i){
					data+="\"activePowerB\":\""+activePowerBList.get(i)+"\",";
					activePowerSum+=activePowerBList.get(i);
				}
				if(activePowerCList.size()>i){
					data+="\"activePowerC\":\""+activePowerCList.get(i)+"\",";
					activePowerSum+=activePowerCList.get(i);
				}
				data+="\"activePowerSum\":\""+StringManagerUtils.stringToFloat(activePowerSum+"", 3)+"\"},";
				result_json.append(data);
			}
			if(result_json.toString().endsWith(",")){
				result_json.deleteCharAt(result_json.length() - 1);
			}
			result_json.append("]}");
		}
		json=result_json.toString();
		System.out.println(json);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**<p>描述：保存上传功图文件</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@RequestMapping("/getAndsaveTestSurfaceCardData")
	public String GetAndsaveTestSurfaceCardData() throws Exception {
		String json = "{success:true,flag:true}";
		
		// 1、构造excel文件输入流对象  
        String sFilePath = "C:/Users/ThinkPad/Desktop/冀东油田数据/2017-10-22载荷数据.xls";  
        InputStream is = new FileInputStream(sFilePath);
        // 2、声明工作簿对象  
        Workbook rwb = Workbook.getWorkbook(is);
        // 3、获得工作簿的个数,对应于一个excel中的工作表个数  
        rwb.getNumberOfSheets();
  
        Sheet oFirstSheet = rwb.getSheet(3);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称  
        int rows = oFirstSheet.getRows();//获取工作表中的总行数  
        int columns = oFirstSheet.getColumns();//获取工作表中的总列数  
        for (int i = 1; i < rows; i++) {
        	String wellName=oFirstSheet.getCell(0,i).getContents();
        	
        	DateCell dc = (DateCell) oFirstSheet.getCell(1,i);   
            Date date = dc.getDate();   
              
            TimeZone zone = TimeZone.getTimeZone("GMT");  
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            sdf.setTimeZone(zone);  
            String acquisitionTimeStr = sdf.format(date);
        	
//        	String acquisitionTimeStr=oFirstSheet.getCell(1,i).getContents();//2017-10-22 01:00:00
        	String dateStr=acquisitionTimeStr.split(" ")[0].replaceAll("-", "");
        	String timeStr=acquisitionTimeStr.split(" ")[1].replaceAll(":", "");
        	String cch=oFirstSheet.getCell(2,i).getContents();
        	String cci=oFirstSheet.getCell(3,i).getContents();
        	String gtstr=oFirstSheet.getCell(7,i).getContents();
        	List<String> gtDataList=StringManagerUtils.SubStringToList(gtstr, 9);
        	int gtCount=gtDataList.size();
        	StringBuffer gtBuf = new StringBuffer();
        	gtBuf.append(dateStr).append("\r\n");
        	gtBuf.append(timeStr).append("\r\n");
        	gtBuf.append(gtCount).append("\r\n");
        	gtBuf.append(cci).append("\r\n");
        	gtBuf.append(cch).append("\r\n");
        	for(int j=0;j<gtCount;j++){
        		float sData=Float.parseFloat(gtDataList.get(j).substring(0, 4))/1000;
        		float fData=Float.parseFloat(gtDataList.get(j).substring(4, 9))/100;
        		gtBuf.append(sData).append("\r\n");
        		gtBuf.append(fData);
        		if(j<gtCount-1){
        			gtBuf.append("\r\n");
        		}
        	}
//        	System.out.println(gtBuf.toString());
//        	raphicalUploadService.saveSurfaceCard(wellName,acquisitionTimeStr,gtBuf.toString());
        }
		response.setContentType("application/json;charset="
				+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@RequestMapping("/getAndsaveDQSurfaceCardData")
	public String GetAndsaveDQSurfaceCardData() throws Exception {
		String json = "{success:true,flag:true}";
		
		// 1、构造excel文件输入流对象  
        String sFilePath = "C:/Users/ThinkPad/Desktop/daqingqichangshuju.xls";  
        InputStream is = new FileInputStream(sFilePath);
        // 2、声明工作簿对象  
        Workbook rwb = Workbook.getWorkbook(is);
        // 3、获得工作簿的个数,对应于一个excel中的工作表个数  
        rwb.getNumberOfSheets();
  
        Sheet oFirstSheet = rwb.getSheet(0);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称  
        int rows = oFirstSheet.getRows();//获取工作表中的总行数  
        int columns = oFirstSheet.getColumns();//获取工作表中的总列数  
        for (int i = 4; i < 124; i++) {
        	String wellName=oFirstSheet.getCell(1,i).getContents();
        	
        	String dateStr="20180425";
        	String timeStr="080000";
        	String[] disstr=oFirstSheet.getCell(23,i).getContents().split(";");
        	String[] loadstr=oFirstSheet.getCell(24,i).getContents().split(";");
        	String cch=oFirstSheet.getCell(25,i).getContents();
        	String cci=oFirstSheet.getCell(26,i).getContents();
        	
        	StringBuffer gtBuf = new StringBuffer();
        	gtBuf.append(dateStr).append("\r\n");
        	gtBuf.append(timeStr).append("\r\n");
        	gtBuf.append(disstr.length).append("\r\n");
        	gtBuf.append(cci).append("\r\n");
        	gtBuf.append(cch).append("\r\n");
        	
        	for(int j=0;j<disstr.length;j++){
        		gtBuf.append(disstr[j]).append("\r\n");
        		gtBuf.append(loadstr[j]);
        		if(j<disstr.length){
        			gtBuf.append("\r\n");
        		}
        	}
        	
//        	System.out.println(gtBuf.toString());
//        	raphicalUploadService.saveSurfaceCard(wellName,"2018-04-25 08:00:00",gtBuf.toString());
        }
		response.setContentType("application/json;charset="
				+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@RequestMapping("/getAndsaveTempSurfaceCardData")
	public String GetAndsaveTempSurfaceCardData() throws Exception {
		String json = "{success:true,flag:true}";
		
		// 1、构造excel文件输入流对象  
        String sFilePath = "C:/Users/ThinkPad/Desktop/采油二厂数据.xls";  
        InputStream is = new FileInputStream(sFilePath);
        // 2、声明工作簿对象  
        Workbook rwb = Workbook.getWorkbook(is);
        // 3、获得工作簿的个数,对应于一个excel中的工作表个数  
        rwb.getNumberOfSheets();
  
        Sheet oFirstSheet = rwb.getSheet(0);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称  
        int rows = oFirstSheet.getRows();//获取工作表中的总行数  
        int columns = oFirstSheet.getColumns();//获取工作表中的总列数  
        String wellName="J1-1";
        String dateStr="20180426";
    	String timeStr="080000";
    	
    	String cch="4.79";
    	String cci="2";
    	
    	StringBuffer gtBuf = new StringBuffer();
    	gtBuf.append(dateStr).append("\r\n");
    	gtBuf.append(timeStr).append("\r\n");
    	gtBuf.append(144).append("\r\n");
    	gtBuf.append(cci).append("\r\n");
    	gtBuf.append(cch).append("\r\n");
        
        for (int i = 1; i < 145; i++) {
        	
        	String disstr=oFirstSheet.getCell(0,i).getContents();
        	String loadstr=oFirstSheet.getCell(2,i).getContents();
        	
        	
        	gtBuf.append(disstr).append("\r\n");
    		gtBuf.append(loadstr);
    		if(i<144){
    			gtBuf.append("\r\n");
    		}
        	
//        	System.out.println(gtBuf.toString());
        	
        }
//        raphicalUploadService.saveSurfaceCard(wellName,"2018-04-26 08:00:00",gtBuf.toString());
		response.setContentType("application/json;charset="
				+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**
	 * 描述：导出功图数据
	 * @throws SQLException 
	 */
	@RequestMapping("/exportSurfaceCardData")
	public String exportSurfaceCardData() throws IOException, SQLException {
		String sql="select t2.jh,to_char(t.cjsj,'yyyymmdd_hh24miss'),t.gtsj "
				+ " from tbl_rpc_diagram_hist t,tbl_wellinformation t2 "
				+ " where t.jbh=t2.jlbh order by jh,t.cjsj";
		String filecontent="";
		String fileName="";
		String json = "{success:true,flag:true}";
		List<?> list=commonDataService.findCallSql(sql);
		
		for(int i=0;i<list.size();i++){
			fileName="C:\\Users\\ThinkPad\\Desktop\\export\\";
			Object[] obj = (Object[]) list.get(i);
			fileName+=obj[0]+"%"+obj[1]+".t";
			SerializableClobProxy   proxy = (SerializableClobProxy)Proxy.getInvocationHandler(obj[2]);
			CLOB realClob = (CLOB) proxy.getWrappedClob(); 
			filecontent=StringManagerUtils.CLOBtoString(realClob);
			StringManagerUtils.createFile(fileName,filecontent);
		}
		
		response.setContentType("application/json;charset=" + Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		this.pager = new Page("pagerForm", request);
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@RequestMapping("/saveRTUAcquisitionData")
	public String saveRTUAcquisitionData() throws Exception {
		String FSdiagramCalculateHttpServerURL[]=Config.getCalculateHttpServerURL().split(",");
		String ScrewPumpCalculateHttpServerURL[]=Config.getScrewPumpCalculateHttpServerURL().split(",");
		Gson gson=new Gson();
		String totalDate = StringManagerUtils.getCurrentTime();
		String totalUrl=Config.getProjectAccessPath()+"/calculateDataController/FSDiagramDailyCalculation?date="+totalDate;
		ServletInputStream ss = request.getInputStream();
		String data=convertStreamToString(ss,"utf-8");
		java.lang.reflect.Type type = new TypeToken<WellAcquisitionData>() {}.getType();
		WellAcquisitionData wellAcquisitionData = gson.fromJson(data, type);
		if(wellAcquisitionData!=null){
			String requestData="";
			String[] calculateHttpServerURL=null;
			if(wellAcquisitionData.getLiftingType()>=400 && wellAcquisitionData.getLiftingType()<400){//螺杆泵
				requestData=raphicalUploadService.getScrewPumpRPMCalculateRequestData(wellAcquisitionData);
				calculateHttpServerURL=ScrewPumpCalculateHttpServerURL;
			}else if(wellAcquisitionData.getLiftingType()>=200 && wellAcquisitionData.getLiftingType()<300){//抽油机
				requestData=raphicalUploadService.getFSdiagramCalculateRequestData(wellAcquisitionData);
				calculateHttpServerURL=FSdiagramCalculateHttpServerURL;
			}
			String responseData=StringManagerUtils.sendPostMethod(calculateHttpServerURL[0], requestData,"utf-8");
			type = new TypeToken<CalculateResponseData>() {}.getType();
			CalculateResponseData calculateResponseData=gson.fromJson(responseData, type);
			raphicalUploadService.saveAcquisitionAndCalculateData(wellAcquisitionData,calculateResponseData);
			if(calculateResponseData.getCalculationStatus().getResultStatus()==1){
				totalUrl+="&wellId="+wellAcquisitionData.getWellId();
				StringManagerUtils.sendPostMethod(totalUrl, "","utf-8");
			}
			
		}
		
		String json = "{success:true,flag:true}";
		response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	public static String convertStreamToString(InputStream is,String encoding) {      
		StringBuilder sb = new StringBuilder();
       try {
    	   BufferedReader reader = new BufferedReader(new InputStreamReader(is,encoding));
           String line = null;    
           while ((line = reader.readLine()) != null) {      
                sb.append(line + "\n");      
            }      
        } catch (IOException e) {      
            e.printStackTrace();      
        } finally {      
           try {      
                is.close();      
            } catch (IOException e) {      
                e.printStackTrace();      
            }      
        }      
    
       return sb.toString();      
    }

	
	@RequestMapping("/downLoadFSdiagramUploadExcelModel")
    public void downLoadFSdiagramUploadExcelModel(){
		StringManagerUtils stringManagerUtils=new StringManagerUtils();
		String path=stringManagerUtils.getFilePath("功图上传Excel模板.xls","download/");
        File file = new File(path);
        try {
        	response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String fileName = "功图上传Excel模板.xls";
            response.setHeader("content-disposition", "attachment;filename="+URLEncoder.encode(fileName, "UTF-8"));
            InputStream in = new FileInputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024];
            OutputStream out = response.getOutputStream();
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer,0,len);
            }
            in.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	/**<p>获取对接数据库的功图文件文件</p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getOuterSurfaceCardData")
	public String getOuterSurfaceCardData(){
		String localSql="select t.wellName,t2.pumpsettingdepth,to_char(t3.acquisitionTime,'yyyy-mm-dd hh24:mi:ss') "
				+ " from tbl_wellinformation t  "
				+ " left outer join tbl_rpc_productiondata_latest t2 on t2.wellid=t.id "
				+ " left outer join tbl_rpc_diagram_latest t3 on t3.wellid=t.id "
				+ " where 1=1 "
				+ " and t.liftingtype >=200 and t.liftingtype<300 "
				+ " order by t.sortnum";
		Connection outerConn= OracleJdbcUtis.getOuterConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		if(outerConn!=null){
//			System.out.println("connection success");
			List<?> localList=commonDataService.findCallSql(localSql);
			for(int i=0;i<localList.size();i++){
				try{
					Object[] obj = (Object[]) localList.get(i);
					String wellName=obj[0]+"";
					String pumpSettingSepth=obj[1]+"";
					String acquisitionTime=obj[2]+"";
					int record=100;
//					acquisitionTime=StringManagerUtils.isNotNull(acquisitionTime)?acquisitionTime:"1970-01-01 00:00:00";
					if(StringManagerUtils.isNotNull(pumpSettingSepth)){//如果生产数据不是空
						pstmt=null;
						rs=null;
						String outerSql=""
								+ " select t.well_common_name,to_char(t.dyna_create_time,'yyyy-mm-dd hh24:mi:ss'),"
								+ " t.stroke,t.frequency,t.dyna_points,t.displacement,t.disp_load,t.disp_current,t.active_power "
								+ " from a11prod.pc_fd_pumpjack_dyna_dia_t t  "
								+ " where 1=1 ";
						
						if(!StringManagerUtils.isNotNull(acquisitionTime)){
//							acquisitionTime="1970-01-01 00:00:00";
							record=1;
							outerSql+=" and t.dyna_create_time > to_date('"+StringManagerUtils.getCurrentTime()+"','yyyy-mm-dd')-30 ";
						}else{
							outerSql+= " and t.dyna_create_time > to_date('"+acquisitionTime+"','yyyy-mm-dd hh24:mi:ss') ";
						}
						outerSql+= " and t.dyna_create_time < to_date('2020-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') ";
						outerSql+=" and t.well_common_name='"+wellName+"' "
								+ " order by t.dyna_create_time ";
//								+ " ) v where rownum<="+record+"";
//						System.out.println("outerSql-"+wellName+":"+outerSql);
						pstmt = outerConn.prepareStatement(outerSql);
						rs=pstmt.executeQuery();
//						System.out.println("outerSql-"+wellName+"查询完成!!!");
						
						while(rs.next()){
							try{
								wellName=rs.getString(1)==null?"":rs.getString(1);
								String acquisitionTimeStr=rs.getString(2)==null?"":rs.getString(2);
								float stroke;
								float frequency;
								if(rs.getObject(3)==null){
									stroke=0;
								}else{
									stroke=rs.getFloat(3);
								}
								if(rs.getObject(4)==null){
									frequency=0;
								}else{
									frequency=rs.getFloat(4);
								}
								
								int point;
								String SStr=rs.getString(6)==null?"":rs.getString(6).replaceAll(";", ",");
								String FStr=rs.getString(7)==null?"":rs.getString(7).replaceAll(";", ",");
								String AStr=rs.getString(8)==null?"":rs.getString(8).replaceAll(";", ",");
								String PStr=rs.getString(9)==null?"":rs.getString(9).replaceAll(";", ",");
								point=SStr.split(",").length;
								System.out.println(acquisitionTimeStr);
								raphicalUploadService.saveSurfaceCard(wellName,acquisitionTimeStr,point,stroke,frequency,SStr,FStr,AStr,PStr);
							}catch(Exception ee){
								ee.printStackTrace();
								continue;
							}
							
							
						}
						if(pstmt!=null)
	                		pstmt.close();  
	                	if(rs!=null)
	                		rs.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		//关闭数据库连接
		OracleJdbcUtis.closeDBConnection(outerConn, pstmt, rs);
		
		try {
			String json = "{success:true,flag:true}";
			response.setContentType("application/json;charset="+ Constants.ENCODING_UTF8);
			response.setHeader("Cache-Control", "no-cache");
			PrintWriter pw;
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public List<File> getSurfaceCardFile() {
		return SurfaceCardFile;
	}

	public void setSurfaceCardFile(List<File> surfaceCardFile) {
		SurfaceCardFile = surfaceCardFile;
	}

	public List<String> getSurfaceCardFileFileName() {
		return SurfaceCardFileFileName;
	}

	public void setSurfaceCardFileFileName(List<String> surfaceCardFileFileName) {
		SurfaceCardFileFileName = surfaceCardFileFileName;
	}

	public List<String> getSurfaceCardFileContentType() {
		return SurfaceCardFileContentType;
	}

	public void setSurfaceCardFileContentType(List<String> surfaceCardFileContentType) {
		SurfaceCardFileContentType = surfaceCardFileContentType;
	}

	
}