Ext.define("AP.view.electricDailyAnalysis.ElectricAnalysisDailyHistoryCurveOnclickWindow", {
    extend: 'Ext.window.Window',
    alias: 'widget.ElectricAnalysisDailyHistoryCurveOnclickWindow',
    layout: 'fit',
    border: false,
    hidden: false,
    collapsible: true,
    constrainHeader:true,//True表示为将window header约束在视图中显示， false表示为允许header在视图之外的地方显示（默认为false）
//  constrain: true,
    closable: 'sides',
    closeAction: 'destroy',
    maximizable: true,
    minimizable: true,
    width: 900,
    minWidth: 500,
    height: 350,
    draggable: true, // 是否可拖曳
    modal: true, // 是否为模态窗口
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
        	tbar:[
        		{
                    xtype: 'datefield',
                    anchor: '100%',
                    fieldLabel: cosog.string.startDate,
                    labelWidth: 58,
                    width: 178,
                    format: 'Y-m-d ',
                    id: 'ElectricAnalysisDailyHistoryCurveStartDate_Id',
                    value: 'new',
                    listeners: {
                    	select: function (combo, record, index) {
                    		Ext.create("AP.store.electricDailyAnalysis.ElectricAnalysisDailyHistoryCurveStore");
                        }
                    }
                }, {
                    xtype: 'datefield',
                    anchor: '100%',
                    fieldLabel: cosog.string.endDate,
                    labelWidth: 58,
                    width: 178,
                    format: 'Y-m-d',
                    id: 'ElectricAnalysisDailyHistoryCurveEndDate_Id',
                    value: new Date(),
                    listeners: {
                    	select: function (combo, record, index) {
                    		Ext.create("AP.store.electricDailyAnalysis.ElectricAnalysisDailyHistoryCurveStore");
                        }
                    }
                }
        	],
        	html: '<div id="ElectricAnalysisDailyHistoryCurveDiv_Id" style="width:100%;height:100%;"></div>',
            listeners: {
                resize: function (abstractcomponent, adjWidth, adjHeight, options) {
                    if ($("#ElectricAnalysisDailyHistoryCurveDiv_Id").highcharts() != undefined) {
                        $("#ElectricAnalysisDailyHistoryCurveDiv_Id").highcharts().setSize($("#ElectricAnalysisDailyHistoryCurveDiv_Id").offsetWidth, $("#ElectricAnalysisDailyHistoryCurveDiv_Id").offsetHeight, true);
                    }else{
                    	Ext.create("AP.store.electricDailyAnalysis.ElectricAnalysisDailyHistoryCurveStore");
                    }
                },
                minimize: function (win, opts) {
                    win.collapse();
                }
            }
        });
        me.callParent(arguments);
    }
});