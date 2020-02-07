Ext.define('AP.store.device.StrokefrequencySccjStore', {
    extend: 'Ext.data.Store',
    alias: 'widget.strokefrequencySccjStore',
    model: 'AP.model.device.StrokefrequencySccjModel',
    autoLoad: true,
    pageSize: 100000,
    proxy: {
        type: 'ajax',
        url: context + '/strokefrequencyController/findByLiIdst',
        actionMethods: {
            read: 'POST'
        },
        start: 0,
        limit: 100000,
        reader: {
            type: 'json',
            rootProperty: 'list',
            totalProperty: 'totals',
            keepRawData: true
        }
    },
    listeners: {
        beforeload: function (store, options) {
            var strokefrequencyPanel_sccj_Id = Ext.getCmp('strokefrequencyPanel_sccj_Id');
            if (!Ext.isEmpty(strokefrequencyPanel_sccj_Id)) {
                strokefrequencyPanel_sccj_Id = strokefrequencyPanel_sccj_Id.rawValue;
            }
            var new_params = {
                sccj: strokefrequencyPanel_sccj_Id

            };
            Ext.apply(store.proxy.extraParams, new_params);

        }
    }
});