package com.gao.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *  <p>描述：工况报警设置 实体类  T_WORKSTATUSALARM</p>
 *  
 * @author gao  2014-06-10
 *
 */
@Entity
@Table(name = "T_WORKSTATUSALARM")
public class WorkStatusAlarm implements java.io.Serializable {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer workingconditioncode;
	private Integer alarmtype;
	private Integer alarmlevel;
	private Integer alarmsign;
	private String remark;

	// Constructors

	/** default constructor */
	public WorkStatusAlarm() {
	}

	/** minimal constructor */
	public WorkStatusAlarm(Integer id, Integer workingconditioncode, Integer alarmtype,
			Integer alarmlevel) {
		this.id = id;
		this.workingconditioncode = workingconditioncode;
		this.alarmtype = alarmtype;
		this.alarmlevel = alarmlevel;
	}

	/** full constructor */
	public WorkStatusAlarm(Integer id, Integer workingconditioncode, Integer alarmtype,
			Integer alarmlevel, Integer alarmsign, String remark) {
		this.id = id;
		this.workingconditioncode = workingconditioncode;
		this.alarmtype = alarmtype;
		this.alarmlevel = alarmlevel;
		this.alarmsign = alarmsign;
		this.remark = remark;
	}

	// Property accessors
	@Id
	@Column(name = "id", unique = true, nullable = false, precision = 22, scale = 0)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "workingconditioncode", nullable = false, precision = 22, scale = 0)
	public Integer getWorkingconditioncode() {
		return this.workingconditioncode;
	}

	public void setWorkingconditioncode(Integer workingconditioncode) {
		this.workingconditioncode = workingconditioncode;
	}

	@Column(name = "alarmtype", nullable = false, precision = 22, scale = 0)
	public Integer getAlarmtype() {
		return this.alarmtype;
	}

	public void setAlarmtype(Integer alarmtype) {
		this.alarmtype = alarmtype;
	}

	@Column(name = "alarmlevel", nullable = false, precision = 22, scale = 0)
	public Integer getAlarmlevel() {
		return this.alarmlevel;
	}

	public void setAlarmlevel(Integer alarmlevel) {
		this.alarmlevel = alarmlevel;
	}

	@Column(name = "alarmsign", precision = 22, scale = 0)
	public Integer getAlarmsign() {
		return this.alarmsign;
	}

	public void setAlarmsign(Integer alarmsign) {
		this.alarmsign = alarmsign;
	}

	@Column(name = "remark", length = 200)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}