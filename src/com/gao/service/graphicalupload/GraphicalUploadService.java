package com.gao.service.graphicalupload;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.engine.jdbc.SerializableClobProxy;
import org.springframework.stereotype.Service;

import com.gao.model.DistreteAlarmLimit;
import com.gao.model.WellInformation;
import com.gao.model.calculate.CalculateRequestData;
import com.gao.model.calculate.CalculateResponseData;
import com.gao.model.calculate.FSDiagramModel;
import com.gao.model.calculate.WellAcquisitionData;
import com.gao.service.base.BaseService;
import com.gao.utils.Page;
import com.gao.utils.StringManagerUtils;
import com.google.gson.Gson;

import oracle.sql.CLOB;

/**
 * <p>
 * 功图上传Service
 * </p>
 * 
 * @author zhao 2016-10-31
 * 
 * @param <T>
 */
@Service("graphicalUploadService")
public class GraphicalUploadService<T> extends BaseService<T> {
	public String getSurfaceCardTrpeList(){
		StringBuffer result_json = new StringBuffer();
		String sql="select id,itemname,itemvalue,remark from tbl_code t where t.itemcode='GTLX' order by t.itemvalue ";
		int totals=this.getTotalCountRows(sql);
		List<?> list = this.findCallSql(sql);
		String columns = "[{ \"header\":\"序号\",\"dataIndex\":\"id\",width:50 ,children:[] },{ \"header\":\"功图类型\",\"dataIndex\":\"gtlxName\" ,children:[] }]";
		result_json.append("{ \"success\":true,\"columns\":"+columns+",");
		result_json.append("\"totalCount\":"+totals+",");
		result_json.append("\"totalRoot\":[");
		for(int i=0;i<list.size();i++){
			Object[] obj=(Object[]) list.get(i);
			result_json.append("{\"id\":"+obj[0]+",");
			result_json.append("\"gtlxName\":\""+obj[1]+"\",");
			result_json.append("\"gtlx\":"+obj[2]+",");
			result_json.append("\"filetype\":\""+obj[3]+"\"},");
		}
		if(list.size()>0){
			result_json.deleteCharAt(result_json.length() - 1);
		}
		result_json.append("]}");
		return result_json.toString();
	}
	
	public boolean saveSurfaceCard(FSDiagramModel FSDiagramModel) throws SQLException, ParseException{
		return this.getBaseDao().saveSurfaceCard(FSDiagramModel);
	}
	
	public boolean saveSurfaceCard(WellAcquisitionData wellAcquisitionData) throws SQLException, ParseException{
		return this.getBaseDao().saveSurfaceCard(wellAcquisitionData);
	}
	
	public boolean saveSurfaceCard(String wellName,String cjsjstr,int point,float stroke,float frequency,String SStr,String FStr,String AStr,String PStr) throws SQLException, ParseException{
		return this.getBaseDao().saveSurfaceCard(wellName,cjsjstr,point,stroke,frequency,SStr,FStr,AStr,PStr);
	}
	
	public String getFSdiagramCalculateRequestData(WellAcquisitionData wellAcquisitionData) throws SQLException, IOException, ParseException{
		Gson gson = new Gson();
		CalculateRequestData calculateRequestData=null;
		String requsetdata="";
		try{
			String prodDataSql="select t.id,"
					+ " t.crudeoildensity,t.waterdensity,t.naturalgasrelativedensity,t.saturationpressure,"
					+ " t.reservoirdepth,t.reservoirtemperature,"
					+ " t.rodstring,"
					+ " t.tubingstringinsidediameter,"
					+ " t.pumptype,t.pumpgrade,t.plungerlength,t.pumpborediameter,"
					+ " t.casingstringinsidediameter,"
					+ " t.watercut,t.productiongasoilratio,t.tubingpressure,t.casingpressure,t.wellheadfluidtemperature,t.producingfluidlevel,t.pumpsettingdepth,"
					+ " t.netgrossratio,"
					+ " t.wellid "
					+ " from tbl_rpc_productiondata_hist t,tbl_rpc_productiondata_latest t2,  tbl_wellinformation t3 "
					+ " where t.wellid=t2.wellid and t.acquisitiontime=t2.acquisitiontime and t.wellid=t3.id"
					+ " and t3.wellName='"+wellAcquisitionData.getWellName()+"'";
			List<?> prodDataList = this.findCallSql(prodDataSql);
			if(prodDataList.size()>0){
				calculateRequestData=new CalculateRequestData();
				Object[] object=(Object[])prodDataList.get(0);
				wellAcquisitionData.setProdDataId(StringManagerUtils.stringToInteger(object[0]+""));
				wellAcquisitionData.setWellId(StringManagerUtils.stringToInteger(object[object.length-1]+""));
				calculateRequestData.setAKString("");
				calculateRequestData.setWellName(wellAcquisitionData.getWellName());
				calculateRequestData.setAcquisitionTime(wellAcquisitionData.getAcquisitionTime());
				
				//流体PVT物性
				calculateRequestData.setFluidPVT(new CalculateRequestData.FluidPVT());
				calculateRequestData.getFluidPVT().setCrudeOilDensity(StringManagerUtils.stringToFloat(object[1]+""));
				calculateRequestData.getFluidPVT().setWaterDensity(StringManagerUtils.stringToFloat(object[2]+""));
				calculateRequestData.getFluidPVT().setNaturalGasRelativeDensity(StringManagerUtils.stringToFloat(object[3]+""));
				calculateRequestData.getFluidPVT().setSaturationPressure(StringManagerUtils.stringToFloat(object[4]+""));
				
				//油藏物性
				calculateRequestData.setReservoir(new CalculateRequestData.Reservoir());
				calculateRequestData.getReservoir().setDepth(StringManagerUtils.stringToFloat(object[5]+""));
				calculateRequestData.getReservoir().setTemperature(StringManagerUtils.stringToFloat(object[6]+""));
				
				//井深轨迹
				calculateRequestData.setWellboreTrajectory(new CalculateRequestData.WellboreTrajectory());
				
				//抽油杆参数
				calculateRequestData.setRodString(new CalculateRequestData.RodString());
				calculateRequestData.getRodString().setEveryRod(new ArrayList<CalculateRequestData.EveryRod>());
				String rodStringArr[]=(object[7]+"").split(";");
				for(int i=0;i<rodStringArr.length;i++){
					if(StringManagerUtils.isNotNull(rodStringArr[i])){
						String everyRodStringArr[]=rodStringArr[i].split(",");
						if(StringManagerUtils.stringToFloat(everyRodStringArr[3]+"")>0){
							String rodGrade=everyRodStringArr[0];
							String rodOutsideDiameter=everyRodStringArr[1];
							String rodInsideDiameter=everyRodStringArr[2];
							String rodLength=everyRodStringArr[3];
							CalculateRequestData.EveryRod everyRod=new CalculateRequestData.EveryRod();
							everyRod.setType(1);
							everyRod.setGrade(rodGrade);
							everyRod.setLength(StringManagerUtils.stringToFloat(rodLength));
							everyRod.setOutsideDiameter(StringManagerUtils.stringToFloat(rodOutsideDiameter)/1000);
							everyRod.setInsideDiameter(StringManagerUtils.stringToFloat(rodInsideDiameter)/1000);
							calculateRequestData.getRodString().getEveryRod().add(everyRod);
						}
					}
				}
				
				//油管参数
				CalculateRequestData.EveryTubing everyTubing=new CalculateRequestData.EveryTubing();
				everyTubing.setInsideDiameter(StringManagerUtils.stringToFloat(object[8]+"")/1000);
				calculateRequestData.setTubingString(new CalculateRequestData.TubingString());
				calculateRequestData.getTubingString().setEveryTubing(new ArrayList<CalculateRequestData.EveryTubing>());
				calculateRequestData.getTubingString().getEveryTubing().add(everyTubing);
				
				//抽油泵参数
				calculateRequestData.setPump(new CalculateRequestData.Pump());
				calculateRequestData.getPump().setPumpType(object[9]+"");
				calculateRequestData.getPump().setBarrelType("L");
				calculateRequestData.getPump().setPumpGrade(StringManagerUtils.stringToInteger(object[10]+""));
				calculateRequestData.getPump().setPlungerLength(StringManagerUtils.stringToFloat(object[11]+""));
				calculateRequestData.getPump().setPumpBoreDiameter(StringManagerUtils.stringToFloat(object[12]+"")/1000);
				
				
				//套管数据
				CalculateRequestData.EveryCasing  everyCasing=new CalculateRequestData.EveryCasing();
				everyCasing.setInsideDiameter(StringManagerUtils.stringToFloat(object[13]+"")/1000);
				calculateRequestData.setCasingString(new CalculateRequestData.CasingString());
				calculateRequestData.getCasingString().setEveryCasing(new ArrayList<CalculateRequestData.EveryCasing>());
				calculateRequestData.getCasingString().getEveryCasing().add(everyCasing);
				
				//生产数据
				calculateRequestData.setProductionParameter(new CalculateRequestData.ProductionParameter());
				calculateRequestData.getProductionParameter().setWaterCut(StringManagerUtils.stringToFloat(object[14]+""));
				calculateRequestData.getProductionParameter().setProductionGasOilRatio(StringManagerUtils.stringToFloat(object[15]+""));
				calculateRequestData.getProductionParameter().setTubingPressure(StringManagerUtils.stringToFloat(object[16]+""));
				calculateRequestData.getProductionParameter().setCasingPressure(StringManagerUtils.stringToFloat(object[17]+""));
				calculateRequestData.getProductionParameter().setWellHeadFluidTemperature(StringManagerUtils.stringToFloat(object[18]+""));
				calculateRequestData.getProductionParameter().setProducingfluidLevel(StringManagerUtils.stringToFloat(object[19]+""));
				calculateRequestData.getProductionParameter().setPumpSettingDepth(StringManagerUtils.stringToFloat(object[20]+""));
				calculateRequestData.getProductionParameter().setSubmergence(StringManagerUtils.stringToFloat(object[20]+"")-StringManagerUtils.stringToFloat(object[19]+""));
				
				//功图数据
				if(wellAcquisitionData.getDiagram()!=null){
			        calculateRequestData.setFSDiagram(new CalculateRequestData.FSDiagram());
			        calculateRequestData.getFSDiagram().setAcquisitionTime(wellAcquisitionData.getAcquisitionTime());
			        calculateRequestData.getFSDiagram().setStroke(wellAcquisitionData.getDiagram().getStroke());
			        calculateRequestData.getFSDiagram().setSPM(wellAcquisitionData.getDiagram().getSPM());
			        List<List<Float>> F=new ArrayList<List<Float>>();
			        List<List<Float>> S=new ArrayList<List<Float>>();
			        List<Float> Watt=new ArrayList<Float>();
			        List<Float> I=new ArrayList<Float>();
			        for(int i=0;i<wellAcquisitionData.getDiagram().getF().size();i++){
			        	List<Float> FList=new ArrayList<Float>();
			        	FList.add(wellAcquisitionData.getDiagram().getF().get(i));
			        	F.add(FList);
			        }
			        for(int i=0;i<wellAcquisitionData.getDiagram().getS().size();i++){
			        	List<Float> SList=new ArrayList<Float>();
			        	SList.add(wellAcquisitionData.getDiagram().getS().get(i));
			        	S.add(SList);
			        }
			        for(int i=0;i<wellAcquisitionData.getDiagram().getP().size();i++){
			        	Watt.add(wellAcquisitionData.getDiagram().getP().get(i));
			        }
			        for(int i=0;i<wellAcquisitionData.getDiagram().getA().size();i++){
			        	I.add(wellAcquisitionData.getDiagram().getA().get(i));
			        }
			        
			        calculateRequestData.getFSDiagram().setF(F);
			        calculateRequestData.getFSDiagram().setS(S);
			        calculateRequestData.getFSDiagram().setWatt(Watt);
			        calculateRequestData.getFSDiagram().setI(I);
				}
		        calculateRequestData.setSystemEfficiency(new CalculateRequestData.SystemEfficiency());
		        
		      //人工干预
		        calculateRequestData.setManualIntervention(new CalculateRequestData.ManualIntervention());
		        calculateRequestData.getManualIntervention().setNetGrossRatio(StringManagerUtils.stringToFloat(object[21]+""));
			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		if(calculateRequestData!=null){
			requsetdata=gson.toJson(calculateRequestData);
		}
		return requsetdata;
	}
	
	public String getScrewPumpRPMCalculateRequestData(WellAcquisitionData wellAcquisitionData) throws SQLException, IOException, ParseException{
		Gson gson = new Gson();
		CalculateRequestData calculateRequestData=null;
		String requsetdata="";
		try{
			String prodDataSql="select t.id,"
					+ " t.crudeoildensity,t.waterdensity,t.naturalgasrelativedensity,t.saturationpressure,"
					+ " t.reservoirdepth,t.reservoirtemperature,"
					+ " t.rodstring,"
					+ " t.tubingstringinsidediameter,"
					+ " t.barrellength,t.barrelseries,t.rotordiameter,t.qpr,"
					+ " t.casingstringinsidediameter,"
					+ " t.watercut,t.productiongasoilratio,t.tubingpressure,t.casingpressure,t.wellheadfluidtemperature,t.producingfluidlevel,t.pumpsettingdepth,"
					+ " t.netgrossratio,"
					+ " t.wellid "
					+ " from tbl_pcp_productiondata_hist t,tbl_pcp_productiondata_latest t2,  tbl_wellinformation t3 "
					+ " where t.wellid=t2.wellid and t.acquisitiontime=t2.acquisitiontime and t.wellid=t3.id"
					+ " and t3.wellName='"+wellAcquisitionData.getWellName()+"'";
			List<?> prodDataList = this.findCallSql(prodDataSql);
			if(prodDataList.size()>0){
				calculateRequestData=new CalculateRequestData();
				Object[] object=(Object[])prodDataList.get(0);
				wellAcquisitionData.setProdDataId(StringManagerUtils.stringToInteger(object[0]+""));
				wellAcquisitionData.setWellId(StringManagerUtils.stringToInteger(object[object.length-1]+""));
				calculateRequestData.setAKString("");
				calculateRequestData.setWellName(wellAcquisitionData.getWellName());
				calculateRequestData.setAcquisitionTime(wellAcquisitionData.getAcquisitionTime());
				
				//转速
				calculateRequestData.setRPM(wellAcquisitionData.getScrewPump().getRPM());
				
				//流体PVT物性
				calculateRequestData.setFluidPVT(new CalculateRequestData.FluidPVT());
				calculateRequestData.getFluidPVT().setCrudeOilDensity(StringManagerUtils.stringToFloat(object[1]+""));
				calculateRequestData.getFluidPVT().setWaterDensity(StringManagerUtils.stringToFloat(object[2]+""));
				calculateRequestData.getFluidPVT().setNaturalGasRelativeDensity(StringManagerUtils.stringToFloat(object[3]+""));
				calculateRequestData.getFluidPVT().setSaturationPressure(StringManagerUtils.stringToFloat(object[4]+""));
				
				//油藏物性
				calculateRequestData.setReservoir(new CalculateRequestData.Reservoir());
				calculateRequestData.getReservoir().setDepth(StringManagerUtils.stringToFloat(object[5]+""));
				calculateRequestData.getReservoir().setTemperature(StringManagerUtils.stringToFloat(object[6]+""));
				
				//井深轨迹
				calculateRequestData.setWellboreTrajectory(new CalculateRequestData.WellboreTrajectory());
				
				//抽油杆参数
				calculateRequestData.setRodString(new CalculateRequestData.RodString());
				calculateRequestData.getRodString().setEveryRod(new ArrayList<CalculateRequestData.EveryRod>());
				String rodStringArr[]=(object[7]+"").split(";");
				for(int i=0;i<rodStringArr.length;i++){
					if(StringManagerUtils.isNotNull(rodStringArr[i])){
						String everyRodStringArr[]=rodStringArr[i].split(",");
						if(StringManagerUtils.stringToFloat(everyRodStringArr[3]+"")>0){
							String rodGrade=everyRodStringArr[0];
							String rodOutsideDiameter=everyRodStringArr[1];
							String rodInsideDiameter=everyRodStringArr[2];
							String rodLength=everyRodStringArr[3];
							CalculateRequestData.EveryRod everyRod=new CalculateRequestData.EveryRod();
							everyRod.setType(1);
							everyRod.setGrade(rodGrade);
							everyRod.setLength(StringManagerUtils.stringToFloat(rodLength));
							everyRod.setOutsideDiameter(StringManagerUtils.stringToFloat(rodOutsideDiameter)/1000);
							everyRod.setInsideDiameter(StringManagerUtils.stringToFloat(rodInsideDiameter)/1000);
							calculateRequestData.getRodString().getEveryRod().add(everyRod);
						}
					}
				}
				
				//油管参数
				CalculateRequestData.EveryTubing everyTubing=new CalculateRequestData.EveryTubing();
				everyTubing.setInsideDiameter(StringManagerUtils.stringToFloat(object[8]+"")/1000);
				calculateRequestData.setTubingString(new CalculateRequestData.TubingString());
				calculateRequestData.getTubingString().setEveryTubing(new ArrayList<CalculateRequestData.EveryTubing>());
				calculateRequestData.getTubingString().getEveryTubing().add(everyTubing);
				
				//抽油泵参数
				calculateRequestData.setPump(new CalculateRequestData.Pump());
				calculateRequestData.getPump().setBarrelLength(StringManagerUtils.stringToFloat(object[9]+""));
				calculateRequestData.getPump().setBarrelSeries((int)(StringManagerUtils.stringToFloat(object[10]+"")+0.5));
				calculateRequestData.getPump().setRotorDiameter(StringManagerUtils.stringToFloat(object[11]+"")/1000);
				calculateRequestData.getPump().setQPR(StringManagerUtils.stringToFloat(object[12]+"")/1000000);
				
				
				//套管数据
				CalculateRequestData.EveryCasing  everyCasing=new CalculateRequestData.EveryCasing();
				everyCasing.setInsideDiameter(StringManagerUtils.stringToFloat(object[13]+"")/1000);
				calculateRequestData.setCasingString(new CalculateRequestData.CasingString());
				calculateRequestData.getCasingString().setEveryCasing(new ArrayList<CalculateRequestData.EveryCasing>());
				calculateRequestData.getCasingString().getEveryCasing().add(everyCasing);
				
				//生产数据
				calculateRequestData.setProductionParameter(new CalculateRequestData.ProductionParameter());
				calculateRequestData.getProductionParameter().setWaterCut(StringManagerUtils.stringToFloat(object[14]+""));
				calculateRequestData.getProductionParameter().setProductionGasOilRatio(StringManagerUtils.stringToFloat(object[15]+""));
				calculateRequestData.getProductionParameter().setTubingPressure(StringManagerUtils.stringToFloat(object[16]+""));
				calculateRequestData.getProductionParameter().setCasingPressure(StringManagerUtils.stringToFloat(object[17]+""));
				calculateRequestData.getProductionParameter().setWellHeadFluidTemperature(StringManagerUtils.stringToFloat(object[18]+""));
				calculateRequestData.getProductionParameter().setProducingfluidLevel(StringManagerUtils.stringToFloat(object[19]+""));
				calculateRequestData.getProductionParameter().setPumpSettingDepth(StringManagerUtils.stringToFloat(object[20]+""));
				calculateRequestData.getProductionParameter().setSubmergence(StringManagerUtils.stringToFloat(object[20]+"")-StringManagerUtils.stringToFloat(object[19]+""));
				
				//系统效率
		        calculateRequestData.setSystemEfficiency(new CalculateRequestData.SystemEfficiency());
		        
		      //人工干预
		        calculateRequestData.setManualIntervention(new CalculateRequestData.ManualIntervention());
		        calculateRequestData.getManualIntervention().setNetGrossRatio(StringManagerUtils.stringToFloat(object[21]+""));
			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		if(calculateRequestData!=null){
			requsetdata=gson.toJson(calculateRequestData);
		}
		return requsetdata;
	}
	
	
	public boolean saveAcquisitionAndCalculateData(WellAcquisitionData wellAcquisitionData,CalculateResponseData calculateResponseData) throws SQLException, ParseException{
		boolean result=false;
		if(wellAcquisitionData.getLiftingType()>=400 && wellAcquisitionData.getLiftingType()<400){//螺杆泵
			result=this.getBaseDao().saveScrewPumpRPMAndCalculateData(wellAcquisitionData,calculateResponseData);
		}else if(wellAcquisitionData.getLiftingType()>=200 && wellAcquisitionData.getLiftingType()<300){//抽油机
			result=this.getBaseDao().saveFSDiagramAndCalculateData(wellAcquisitionData,calculateResponseData);
		}
		
		
		return result;
	}
}
