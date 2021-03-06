package com.gao.controller.calculateManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gao.controller.base.BaseController;
import com.gao.model.User;
import com.gao.model.gridmodel.CalculateManagerHandsontableChangedData;
import com.gao.service.base.CommonDataService;
import com.gao.service.calculateManager.CalculateManagerService;
import com.gao.utils.Page;
import com.gao.utils.ParamUtils;
import com.gao.utils.StringManagerUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 计算结果管理controller
 * 
 * @author zhao 2018-11-30
 * @version 1.0
 * 
 */
@Controller
@RequestMapping("/calculateManagerController")
@Scope("prototype")
public class CalculateManagerController extends BaseController {
	private static final long serialVersionUID = 1L;
	@Autowired
	private CalculateManagerService<?> calculateManagerService;
	@Autowired
	private CommonDataService service;
	private int page;
	private int limit;
	private int totals;
	private String wellName;
	private String orgId;
	
	@RequestMapping("/getCalculateResultData")
	public String getCalculateResultData() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		wellName = ParamUtils.getParameter(request, "wellName");
		
		String wellType = ParamUtils.getParameter(request, "wellType");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String calculateSign = ParamUtils.getParameter(request, "calculateSign");
		this.pager = new Page("pagerForm", request);
		User user=null;
		if (!StringManagerUtils.isNotNull(orgId)) {
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserorgids();
			}
		}
		if(!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from tbl_rpc_diagram_hist t";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),0);
		}
//		startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),-120);
		pager.setStart_date(startDate);
		pager.setEnd_date(endDate);
		
		String json = calculateManagerService.getCalculateResultData(orgId, wellName, pager,wellType,startDate,endDate,calculateSign);
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
	
	@SuppressWarnings("unused")
	@RequestMapping("/saveRecalculateData")
	public String saveRecalculateData() throws Exception {

		String data = ParamUtils.getParameter(request, "data").replaceAll("&nbsp;", "");
		
		Gson gson = new Gson();
		java.lang.reflect.Type type = new TypeToken<CalculateManagerHandsontableChangedData>() {}.getType();
		CalculateManagerHandsontableChangedData calculateManagerHandsontableChangedData=gson.fromJson(data, type);
		this.calculateManagerService.saveRecalculateData(calculateManagerHandsontableChangedData);
		String json ="{success:true}";
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
	 * <p>
	 * 描述：计算维护模块模块计算标志下拉框
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getCalculateStatusList")
	public String getCalculateStatusList() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		String welName = ParamUtils.getParameter(request, "welName");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		if (!StringManagerUtils.isNotNull(orgId)) {
			User user = null;
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserOrgid();
			}
		}
		if(!StringManagerUtils.isNotNull(endDate)){
			String sql = " select to_char(max(t.acquisitionTime),'yyyy-mm-dd') from tbl_rpc_diagram_hist t";
			List list = this.service.reportDateJssj(sql);
			if (list.size() > 0 &&list.get(0)!=null&&!list.get(0).toString().equals("null")) {
				endDate = list.get(0).toString();
			} else {
				endDate = StringManagerUtils.getCurrentTime();
			}
		}
		
		if(!StringManagerUtils.isNotNull(startDate)){
			startDate=StringManagerUtils.addDay(StringManagerUtils.stringToDate(endDate),0);
		}
		String json = this.calculateManagerService.getCalculateStatusList(orgId,welName,wellType,startDate,endDate);
//		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
//		log.warn("jh json is ==" + json);
		pw.flush();
		pw.close();
		return null;
	}
	
	/**
	 * <p>
	 * 描述：关联当前生产数据重新计算历史
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/recalculateByProductionData")
	public String recalculateByProductionData() throws Exception {
		orgId = ParamUtils.getParameter(request, "orgId");
		String wellName = ParamUtils.getParameter(request, "wellName");
		String wellType = ParamUtils.getParameter(request, "wellType");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		String calculateSign = ParamUtils.getParameter(request, "calculateSign");
		if (!StringManagerUtils.isNotNull(orgId)) {
			User user = null;
			HttpSession session=request.getSession();
			user = (User) session.getAttribute("userLogin");
			if (user != null) {
				orgId = "" + user.getUserOrgid();
			}
		}
		this.calculateManagerService.recalculateByProductionData(orgId,wellName,wellType,startDate,endDate,calculateSign);
		String json ="{success:true}";
//		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.print(json);
//		log.warn("jh json is ==" + json);
		pw.flush();
		pw.close();
		return null;
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
}
