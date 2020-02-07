package com.gao.service.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.gao.model.User;
import com.gao.model.data.DataDictionary;
import com.gao.model.data.DataitemsInfo;
import com.gao.service.base.BaseService;
import com.gao.utils.DataDicUtils;
import com.gao.utils.DateUtils;
import com.gao.utils.Page;

/**
 * 系统数据字典数据项值管理
 * 
 * @author 钱邓水
 * @data 2014-4-10
 */
@Service
public class DataitemsInfoService extends BaseService<DataitemsInfo> {
	/**
	 * 根据字典ID查询数据项值信息
	 * 
	 * @author 钱邓水
	 * @throws Exception
	 * @parem typeName 类型 name 字典名称
	 */
	public List<DataitemsInfo> findDataitemsInfoPageListById(Page pager, User userInfo, String sysId, String tab, String dataName) throws Exception {
		List<DataitemsInfo> list = null;

		// 检索条件
		if (StringUtils.isNotBlank(sysId)) {
			pager.setWhere("sysdataid='" + sysId + "'");
		}
		if (StringUtils.isNotBlank(tab)) {
			if (StringUtils.isNotBlank(dataName) && tab.equals("0")) {
				pager.setWhere("ename  like'%" + dataName + "%'");
			}
			if (StringUtils.isNotBlank(dataName) && tab.equals("1")) {
				pager.setWhere("cname  like'%" + dataName + "%'");
			}
		}

		pager.setSort(" sorts  asc");
		list = findAllPageByEntity(pager);

		return list;
	}

	/**
	 * 创建数据项值信息
	 * 
	 * @throws Exception
	 * @parem Object
	 */
	public boolean saveDataitemsInfo(DataitemsInfo obj) throws Exception {
		boolean result = false;

		if (null != obj && !"".equals(obj)) {
			this.save(obj);
			result = true;
		}

		return result;
	}

	public void updateDataitemsInfoById(String dataId, String valStr) {
		String upSql = "update tbl_dataitemsinfo dup set dup.status=" + valStr + " where dup.dataitemid='" + dataId + "'";
		try {
			this.updateSql(upSql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 增加某个模块字典的数据项值信息
	 * 
	 * @throws Exception
	 * @parem Object
	 */
	public String saveDataitemsInfo(DataitemsInfo dinfo, User userInfo, String sysId) throws Exception {
		String jsonaddstr = "";
		if (StringUtils.isNotBlank(sysId)) {
			dinfo.setSysdataid(sysId);
			this.saveDataitemsInfo(dinfo);
			jsonaddstr = "{success:true,msg:true}";
		} else {
			jsonaddstr = "{success:true,msg:false,error:'此用户已创建了该数据项！'}";
		}
		return jsonaddstr;
	}

	/**
	 * 增加某个模块字典的数据项值信息
	 * 
	 * @throws Exception
	 * @parem Object
	 */
	public String editDataitemsInfo(DataitemsInfo dinfo, User userInfo) throws Exception {
		String jsonaddstr = "";
		if (dinfo != null) {
			dinfo.setUpdateuser(userInfo.getUserName());
			dinfo.setCreatedate(DateUtils.getTime());
			dinfo.setUpdatetime(DateUtils.getTime());
			//this.edit(dinfo);
			getBaseDao().updateObject(dinfo);
			jsonaddstr = "{success:true,msg:true}";
		} else {
			jsonaddstr = "{success:true,msg:false,error:'此数据项修改失败!'}";
		}
		return jsonaddstr;
	}

	/**
	 * 删除数据项值信息
	 * 
	 * @author 钱邓水 2012-3-13
	 * @throws Exception
	 * @parem String
	 */
	public boolean deleteDataitemsInfoById(User userInfo, String dataid) throws Exception {
		boolean result = false;
		if (StringUtils.isNotBlank(dataid)) {
			String sql = "delete DataitemsInfo ditem   where ditem.dataitemid in(:val)";
			this.deleteHqlQueryParameter(sql, "val", dataid.split(","));
			result = true;
		}

		return result;
	}

	/**
	 * 根据字典的英文查询域值集合信息
	 * 
	 * @param dataEname
	 * @return
	 */
	public List<DataitemsInfo> findTableHeaderByListFaceId(String dataEname) {
		String sqlData = "from DataitemsInfo dtm where dtm.status=1 and dtm.sysdataid in (select sys.sysdataid from SystemdataInfo sys where sys.status=0 and sys.ename=? ) ";
		List<DataitemsInfo> getDataitemsInfoList = null;
		try {
			getDataitemsInfoList = this.find(sqlData.toString(), new Object[] { dataEname });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getDataitemsInfoList;
	}

	/**
	 * 查询表头列信息集合
	 * 
	 * @param dataEname
	 * @return
	 */
	public String findTableHeaderByColumnsFaceId(String dataEname) {
		String sqlData = "from DataitemsInfo dtm where dtm.status=1 and dtm.sysdataid in (select sys.sysdataid from SystemdataInfo sys where sys.status=0 and sys.ename=? ) order by sorts ";

		StringBuffer strBuf = new StringBuffer();
		strBuf.append("[");
		try {
			List<DataitemsInfo> dataColumnsList = this.find(sqlData.toString(), new Object[] { dataEname });
			if (null != dataColumnsList && dataColumnsList.size() > 0) {
				for (DataitemsInfo dataInfo : dataColumnsList) {
					String _Ename = dataInfo.getEname();
					if (!_Ename.equals("table") && !_Ename.equals("where") && !_Ename.equals("order") && !_Ename.equals("group") && !_Ename.equals("params")) {
						strBuf.append("{ ");
						strBuf.append("\"header\":\"" + dataInfo.getCname() + "\",");
						strBuf.append("\"dataIndex\":\"" + dataInfo.getEname() + "\"");
						if (StringUtils.isNotBlank(dataInfo.getDatavalue())) {
							strBuf.append("," + dataInfo.getDatavalue());
						}
						strBuf.append("},");
					}
				}
				strBuf.deleteCharAt(strBuf.length() - 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		strBuf.append("]");
		return strBuf.toString();
	}

	/**
	 * <p>
	 * 描述：字典SQL查询语句,查询数据字典构建数据字典对象信息
	 * </p>
	 * 
	 * @param dataEname
	 *            数据字典英文名称
	 * @return 数据字典信息对象
	 */
	public DataDictionary findTableSqlWhereByListFaceId(String dataEname) {
		StringBuffer sqlColumn = new StringBuffer();
		StringBuffer strBuf = new StringBuffer();
		String sqlTable = "";
		String sqlWhere = "";
		String sqlOrderBy = "";
		String sqlGroupBy="";
		List<String> dynamics = new ArrayList<String>();
		List<String> headers = new ArrayList<String>();
		List<String> fields = new ArrayList<String>();
		DataDictionary ddic = new DataDictionary();
		strBuf.append("[");
		sqlColumn.append("select ");
		String sqlData = "from DataitemsInfo dtm where dtm.status=1 and dtm.sysdataid in (select sys.sysdataid from SystemdataInfo sys where sys.status=0 and sys.ename=? ) order by dtm.sorts asc ";
		try {
			// 根据模块字典英文名称从数据库中找出该模块的字典数据信息
			List<DataitemsInfo> dataWhereList = this.find(sqlData.toString(), new Object[] { dataEname });

			if (null != dataWhereList && dataWhereList.size() > 0) {
				Map<String, List<DataDictionary>> map = DataDicUtils.initData(dataWhereList);// 把集合中含有多表头信息的数据封装在Map
																							// 集合了里
			TreeMap<String, List<DataDictionary>> treemap = new TreeMap<String, List<DataDictionary>>(map);
				for (DataitemsInfo dataInfo : dataWhereList) {
					String _Ename = dataInfo.getEname();// 字典英文名称对应数据库中的字段信息
					String enameField = _Ename.trim();
					String dataValueString = dataInfo.getDatavalue();
					if (!StringUtils.isNotBlank(dataValueString)) {
						dataValueString = "";
					}

					if (_Ename.indexOf("root") == -1) { // 过滤掉根节点
						if (_Ename.indexOf(" as ") > 0) {
							/* 当该字段中含有 as 时，截取as后的字符串作为字段数据 */
							enameField = _Ename.substring(_Ename.indexOf(" as ") + 3).trim();

						} else if (_Ename.indexOf("#") > 0) {
							enameField = _Ename.substring(_Ename.lastIndexOf("#") + 1);// 如果字段中含有“#”截取最后一段作为字段数据
						}
					}
					if (!_Ename.equals("table") && !_Ename.equals("where") && !_Ename.equals("order") && !_Ename.equals("group") && !_Ename.equals("params")) {

						int index = _Ename.indexOf("root");
						if (index >= 0) {
							String[] rootVal = _Ename.split("_");// 判断当前节点是根节点
							String key = rootVal[0];
							strBuf.append(" { header: \"" + dataInfo.getCname() + "\",dataIndex:\"" + key + "\",children:[");
							DataDicUtils.emptyBuffer();
							String resultString = DataDicUtils.createChildHeader(key, treemap);// 调用递归函数创建子节点数据信息
							strBuf.append(resultString);
							strBuf.append(" ]},");
						} else {
							// 该节点不含有root字符串代表为普通的字段
							int nowIndex = _Ename.indexOf("#");// 不能存在“#”字符信息
							if (nowIndex == -1) {
								strBuf.append("{ ");
								strBuf.append("\"header\":\"" + dataInfo.getCname() + "\",");
								strBuf.append("\"dataIndex\":\"" + enameField.trim() + "\"");
								if (StringUtils.isNotBlank(dataValueString) &&!"null".equals(dataValueString)) {
									strBuf.append("," + dataValueString);
								}
								strBuf.append(" ,children:[] },");
							}
						}

						if (_Ename.indexOf("root") == -1) {
							if (_Ename.indexOf("#") > 0) {
								_Ename = _Ename.substring(_Ename.lastIndexOf("#") + 1);
							}
							if (_Ename.indexOf("child") == -1) {
								sqlColumn.append(_Ename + ",");
								headers.add(dataInfo.getCname());
							}

						}
						if (_Ename.indexOf("root") == -1 && _Ename.indexOf("child") == -1) {
							fields.add(enameField.trim());
						}
					} else if (_Ename.equals("table")) {
						sqlTable = "  from  " + dataValueString;
					} else if (_Ename.equals("where")) {
						sqlWhere = " where  1=1 " + dataValueString;
					} else if (_Ename.equals("order")) {
						sqlOrderBy = "  " + dataValueString;
					} else if(_Ename.equals("group")){
						sqlGroupBy="  "+dataValueString;
					}
					else if (_Ename.equals("params")) {
						dynamics.add(dataValueString);
					}
				}
				strBuf.deleteCharAt(strBuf.length() - 1);//去掉最后一个逗号
				sqlColumn.deleteCharAt(sqlColumn.length() - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		strBuf.append("]");
		String xWhere = sqlColumn.toString()  + sqlTable + " " + sqlWhere;//sql语句拼到where 1=1
		ddic.setTableHeader(strBuf.toString());
		ddic.setHeaders(headers);
		ddic.setFields(fields);
		ddic.setSql(xWhere);
		ddic.setParams(dynamics);
		ddic.setOrder(sqlOrderBy);
		ddic.setGroup(sqlGroupBy);
		return ddic;
	}
}
