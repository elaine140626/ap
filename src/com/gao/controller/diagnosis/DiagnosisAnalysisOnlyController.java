package com.gao.controller.diagnosis;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gao.controller.base.BaseController;
import com.gao.model.DiagnosisAnalysisOnly;
import com.gao.model.DiagnosisAnalysisStatistics;
import com.gao.model.User;
import com.gao.service.base.CommonDataService;
import com.gao.service.diagnosis.DiagnosisAnalysisOnlyService;
import com.gao.tast.EquipmentDriverServerTast;
import com.gao.utils.I18NConfig;
import com.gao.utils.OracleJdbcUtis;
import com.gao.utils.Page;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;
import com.gao.utils.UnixPwdCrypt;
import com.google.gson.Gson;

/**
 * 工况诊断 （单张功图诊断分析）- action类
 * 
 * @author gao 2014-05-09
 * @version 1.0
 * 
 */
@Controller
@RequestMapping("/diagnosisAnalysisOnlyController")
@Scope("prototype")
public class DiagnosisAnalysisOnlyController extends BaseController {
	private static final long serialVersionUID = 1L;
	@Autowired
	private DiagnosisAnalysisOnlyService<DiagnosisAnalysisOnly> diagnosisAnalysisOnlyService;
	private DiagnosisAnalysisOnly diagnosisAnalysisOnly;
	@Autowired
	private CommonDataService service;
	private List<DiagnosisAnalysisOnly> list = null;
	private int page;
	private int limit;
	private int totals;
	private String wellName;
	private String orgId;
	private int id;
	
	/**
	 * <P>
	 * 描述:采出井实时评价数据表
	 * </p>
	 * 
	 * @author zhao 2018-10-15
	 */
	@RequestMapping("/getProductionWellRTAnalysisWellList")
	public String getProductionWellRTAnalysisWellList() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		orgId = findCurrentUserOrgIdInfo(orgId);
		wellName = ParamUtils.getParameter(request, "wellName");
		
		String type = ParamUtils.getParameter(request, "type");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = ParamUtils.getParameter(request, "statValue");
		this.pager = new Page("pagerForm", request);
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(StringManagerUtils.isNotNull(wellName)&&!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from tbl_rpc_diagram_hist t where t.wellId=( select t2.id from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = diagnosisAnalysisOnlyService.getProductionWellRTAnalysisWellList(orgId, wellName, pager,type,wellType,startDate,endDate,statValue);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/exportProductionWellRTAnalysisDataExcel")
	public String exportProductionWellRTAnalysisDataExcel() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		orgId = findCurrentUserOrgIdInfo(orgId);
		wellName = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "wellName"),"utf-8");
		
		String type = ParamUtils.getParameter(request, "type");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String statValue = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "statValue"),"utf-8");
		this.pager = new Page("pagerForm", request);
		String heads = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "heads"),"utf-8");
		String fields = ParamUtils.getParameter(request, "fields");
		String fileName = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "fileName"),"utf-8");
		String title = java.net.URLDecoder.decode(ParamUtils.getParameter(request, "title"),"utf-8");
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(StringManagerUtils.isNotNull(wellName)&&!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.jssj),'yyyy-mm-dd') from tbl_rpc_diagram_hist t where t.jbh=( select t2.jlbh from tbl_wellinformation t2 where t2.wellName='"+wellName+"' ) ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		String data = diagnosisAnalysisOnlyService.exportProductionWellRTAnalysisDataExcel(orgId, wellName, pager,type,wellType,startDate,endDate,statValue);
		
		this.service.exportGridPanelData(response,fileName,title, heads, fields,data);
		return null;
	}
	
	/**
	 * <p>
	 * 描述：工况统计饼图、柱状图json数据
	 * </p>
	 * 
	 * @author zhao 2016-06-23
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/statisticsData")
	public String statisticsData() throws Exception {
		orgId=ParamUtils.getParameter(request, "orgId");
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			User user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		String type = ParamUtils.getParameter(request, "type");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String json = "";
		
		/******
		 * 饼图及柱状图需要的data信息
		 * ***/
		json = diagnosisAnalysisOnlyService.statisticsData(orgId,type,wellType);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@RequestMapping("/getAnalysisAndAcqAndControlData")
	public String getAnalysisAndAcqAndControlData() throws Exception {
		HttpSession session=request.getSession();
		User user = (User) session.getAttribute("userLogin");
		String recordId = ParamUtils.getParameter(request, "id");
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		this.pager = new Page("pagerForm", request);
		
		String json =diagnosisAnalysisOnlyService.getAnalysisAndAcqAndControlData(recordId,wellName,selectedWellName,user.getUserNo());
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	@SuppressWarnings("rawtypes")
	@RequestMapping("/getDiagnosisDataCurveData")
	public String getDiagnosisDataCurveData() throws Exception {
		String json = "";
		wellName = ParamUtils.getParameter(request, "wellName");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String itemName = ParamUtils.getParameter(request, "itemName");
		String itemCode = ParamUtils.getParameter(request, "itemCode");
		
		this.pager = new Page("pagerForm", request);
		if(!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from viw_rpc_diagram_hist t  where wellName= '"+wellName+"' ";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-10);
		}
		
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		json =  this.diagnosisAnalysisOnlyService.getDiagnosisDataCurveData(wellName, startDate,endDate,itemName,itemCode);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.write(json);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping("/showIndex")
	public String showIndex() throws Exception {
		return "app/page/home";
	}
	@RequestMapping("/showBackIndex")
	public String showBackIndex() throws Exception {
		return "app/page/backHome";
	}
	@RequestMapping("/toLogin")
	public String toLogin() throws Exception {
//		return "HYTXLogin";
		return "Login";
	}
	@RequestMapping("/toTouchLogin")
	public String toTouchLogin() throws Exception {
		return "touchLogin";
	}
	@RequestMapping("/toMain")
	public String toMain() throws Exception {
		return "app/main";
	}
	@RequestMapping("/toTouchMain")
	public String toTouchMain() throws Exception {
		return "toTouchMain";
	}
	@RequestMapping("/toBackLogin")
	public String toBackLogin() throws Exception {
		return "AdminLogin";
	}
	@RequestMapping("/toBackMain")
	public String toBackMain() throws Exception {
		return "app/back";
	}
	
	/**
	 * 描述：查询单井工况诊断的泵功图
	 */
	@RequestMapping("/querySinglePumpCard")
	public String querySinglePumpCard()throws Exception{
		id = Integer.parseInt(ParamUtils.getParameter(request, "id"));
		wellName = ParamUtils.getParameter(request, "wellName");
		String selectedWellName = ParamUtils.getParameter(request, "selectedWellName");
		String json = "";
		json = this.diagnosisAnalysisOnlyService.queryPumpCard(id,wellName,selectedWellName);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**
	 * 描述：查询单井工况诊断的杆柱应力
	 */
	@RequestMapping("/querySingleRodPress")
	public String querySingleRodPress()throws Exception{
		id = Integer.parseInt(ParamUtils.getParameter(request, "id"));
		String json = "";
		json = this.diagnosisAnalysisOnlyService.queryRodPress(id);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**
	 * 描述：查询单井工况诊断的泵效组成
	 */
	@RequestMapping("/querySinglePumpEfficiency")
	public String querySinglePumpEfficiency()throws Exception{
		id = Integer.parseInt(ParamUtils.getParameter(request, "id"));
		String json = "";
		json = this.diagnosisAnalysisOnlyService.queryPumpEfficiency(id);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	@RequestMapping("/wellControlOperation")
	public String WellControlOperation() throws Exception {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		String wellName = request.getParameter("wellName");
		String password = request.getParameter("password");
		String controlType = request.getParameter("controlType");
		String controlValue = request.getParameter("controlValue");
		String jsonLogin = "";
		String clientIP=StringManagerUtils.getIpAddr(request);
		User userInfo = (User) request.getSession().getAttribute("userLogin");
		// 用户不存在
		if (null != userInfo) {
			String getUpwd = userInfo.getUserPwd();
			String getOld = UnixPwdCrypt.crypt("dogVSgod", password);
			if (getOld.equals(getUpwd)&&(int)StringManagerUtils.stringToFloat(controlValue)>0) {
				for(int i=0;i<EquipmentDriverServerTast.units.size();i++){
					if(wellName.equals(EquipmentDriverServerTast.units.get(i).getWellName())){
						if("startOrStopWell".equalsIgnoreCase(controlType)){//启停井控制
							EquipmentDriverServerTast.units.get(i).setRunStatusControl(StringManagerUtils.stringToInteger(controlValue));
						}else if("frequencySetValue".equalsIgnoreCase(controlType)){//设置变频频率
							EquipmentDriverServerTast.units.get(i).setFrequencyControl(StringManagerUtils.stringToFloat(controlValue));
						}else if("setGTCycle".equalsIgnoreCase(controlType)){//设置功图采集周期
							EquipmentDriverServerTast.units.get(i).setFSDiagramIntervalControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceControlMode".equalsIgnoreCase(controlType)){//设置平衡调节远程触发控制
							EquipmentDriverServerTast.units.get(i).setBalanceControlModeControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceCalculateMode".equalsIgnoreCase(controlType)){//设置平衡计算模式
							EquipmentDriverServerTast.units.get(i).setBalanceCalculateModeControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceAwayTime".equalsIgnoreCase(controlType)){//设置平衡远离支点调节时间
							EquipmentDriverServerTast.units.get(i).setBalanceAwayTimeControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceCloseTime".equalsIgnoreCase(controlType)){//设置平衡接近支点调节时间
							EquipmentDriverServerTast.units.get(i).setBalanceCloseTimeControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceAwayTimePerBeat".equalsIgnoreCase(controlType)){//设置平衡远离支点每拍调节时间
							EquipmentDriverServerTast.units.get(i).setBalanceAwayTimePerBeatControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceCloseTimePerBeat".equalsIgnoreCase(controlType)){//设置平衡接近支点每拍调节时间
							EquipmentDriverServerTast.units.get(i).setBalanceCloseTimePerBeatControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceStrokeCount".equalsIgnoreCase(controlType)){//设置参与平衡计算冲程次数
							EquipmentDriverServerTast.units.get(i).setBalanceStrokeCountControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceOperationUpLimit".equalsIgnoreCase(controlType)){//设置平衡调节上限
							EquipmentDriverServerTast.units.get(i).setBalanceOperationUpLimitControl(StringManagerUtils.stringToInteger(controlValue));
						}
						else if("balanceOperationDownLimit".equalsIgnoreCase(controlType)){//设置平衡调节下限
							EquipmentDriverServerTast.units.get(i).setBalanceOperationDownLimitControl(StringManagerUtils.stringToInteger(controlValue));
						}
						break;
					}
				}
				jsonLogin = "{success:true,flag:true,error:true,msg:'<font color=blue>命令发送成功。</font>'}";
			}else if(!((int)StringManagerUtils.stringToFloat(controlValue)>0)){
				jsonLogin = "{success:true,flag:true,error:false,msg:'<font color=red>数据有误，请检查输入数据！</font>'}";
			} else {
				jsonLogin = "{success:true,flag:true,error:false,msg:'<font color=red>您输入的密码有误！</font>'}";
			}

		} else {
			jsonLogin = "{success:true,flag:false}";
		}
		// 处理乱码。
		response.setCharacterEncoding("utf-8");
		// 输出json数据。
		out.print(jsonLogin);
		return null;
	}
	
	@RequestMapping("/getScrewPumpRTAnalysiCurveData")
	public String getScrewPumpRTAnalysiCurveData()throws Exception{
		@SuppressWarnings("unused")
		String cjsj = ParamUtils.getParameter(request, "cjsj");
		wellName = ParamUtils.getParameter(request, "wellName");
		String json = "";
		json = this.diagnosisAnalysisOnlyService.getScrewPumpRTAnalysiCurveData(cjsj,wellName);
		//HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
		pw.flush();
		pw.close();
		return null;
	}
	
	public DiagnosisAnalysisOnly getDiagnosisAnalysisOnly() {
		return diagnosisAnalysisOnly;
	}

	public void setDiagnosisAnalysisOnly(DiagnosisAnalysisOnly diagnosisAnalysisOnly) {
		this.diagnosisAnalysisOnly = diagnosisAnalysisOnly;
	}

	public List<DiagnosisAnalysisOnly> getList() {
		return list;
	}

	public void setList(List<DiagnosisAnalysisOnly> list) {
		this.list = list;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getTotals() {
		return totals;
	}

	public void setTotals(int totals) {
		this.totals = totals;
	}

	public String getWellName() {
		return wellName;
	}

	public void setWellName(String wellName) {
		this.wellName = wellName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	public int getid() {
		return id;
	}

	public void setid(int id) {
		this.id = id;
	}
}
