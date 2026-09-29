/**
 * 燃气档案 省—市—区—街道—片区 五级联动公共组件。
 * 依赖 layui 的 jquery、form；页面需存在五个 <select>（id 固定）。
 *
 * 用法：
 *   GasRegionLinkage.init({
 *     form: form, $: $,
 *     ids: {province:'province_select', city:'city_select', area:'area_select', street:'street_select', zone:'zone_select'},
 *     placeholders: ['全部省', ...],          // 表单查询页传“全部X”，建档页传“请选择X”
 *     selected: {provinceCode,cityCode,areaCode,streetCode,zoneId}, // 编辑回显（可空）
 *     onDone: function(sel){...}              // 回显加载完成回调（可空）
 *   });
 */
var GasRegionLinkage = (function () {

    function fillSelect($sel, list, codeKey, nameKey, placeholder, selectedVal) {
        $sel.empty().append("<option value=''>" + placeholder + "</option>");
        for (var i = 0; i < list.length; i++) {
            var sel = (selectedVal != null && String(list[i][codeKey]) === String(selectedVal)) ? " selected" : "";
            $sel.append("<option value='" + list[i][codeKey] + "'" + sel + ">" + list[i][nameKey] + "</option>");
        }
    }

    function get(url) {
        return $.ajax({type: 'get', dataType: 'json', url: url});
    }

    function init(opts) {
        var form = opts.form, $ = opts.$;
        var ids = $.extend({
            province: 'province_select', city: 'city_select', area: 'area_select',
            street: 'street_select', zone: 'zone_select'
        }, opts.ids || {});
        var ph = $.extend({
            province: '请选择省', city: '请选择市', area: '请选择区', street: '请选择街道', zone: '请选择片区'
        }, opts.placeholders || {});
        var sel = opts.selected || {};

        var $prov = $('#' + ids.province), $city = $('#' + ids.city), $area = $('#' + ids.area),
            $street = $('#' + ids.street), $zone = $('#' + ids.zone);

        function loadZones(streetCode, selectedZoneId) {
            $zone.empty().append("<option value=''>" + ph.zone + "</option>");
            if (!streetCode) {
                form.render('select');
                return;
            }
            get(rootPath + "/gasZone/options?streetCode=" + streetCode).done(function (list) {
                for (var i = 0; i < list.length; i++) {
                    var picked = (selectedZoneId != null && String(list[i].id) === String(selectedZoneId)) ? " selected" : "";
                    $zone.append("<option value='" + list[i].id + "'" + picked + ">" + list[i].zoneName + "</option>");
                }
                form.render('select');
            });
        }

        // 省 → 市 → 区 → 街道 → 片区（按选中值逐层回显）
        get(rootPath + "/gasZone/allProvinces").done(function (list) {
            fillSelect($prov, list, 'provinceCode', 'provinceName', ph.province, sel.provinceCode);
            form.render('select');
            if (!sel.provinceCode) { if (opts.onDone) opts.onDone(); return; }
            get(rootPath + "/ProvinceLinkageController/getCity?pid=" + sel.provinceCode).done(function (cities) {
                fillSelect($city, cities, 'cityCode', 'cityName', ph.city, sel.cityCode);
                form.render('select');
                if (!sel.cityCode) { if (opts.onDone) opts.onDone(); return; }
                get(rootPath + "/ProvinceLinkageController/getArea?pid=" + sel.cityCode).done(function (areas) {
                    fillSelect($area, areas, 'areaCode', 'areaName', ph.area, sel.areaCode);
                    form.render('select');
                    if (!sel.areaCode) { if (opts.onDone) opts.onDone(); return; }
                    get(rootPath + "/ProvinceLinkageController/getStreet?pid=" + sel.areaCode).done(function (streets) {
                        fillSelect($street, streets, 'streetCode', 'streetName', ph.street, sel.streetCode);
                        form.render('select');
                        loadZones(sel.streetCode, sel.zoneId);
                        if (opts.onDone) opts.onDone();
                    });
                });
            });
        });

        // 用户手动切换
        form.on('select(' + ids.province + ')', function (data) {
            $city.empty().add($area).add($street).add($zone);
            $city.empty().append("<option value=''>" + ph.city + "</option>");
            $area.empty().append("<option value=''>" + ph.area + "</option>");
            $street.empty().append("<option value=''>" + ph.street + "</option>");
            $zone.empty().append("<option value=''>" + ph.zone + "</option>");
            if (data.value) {
                get(rootPath + "/ProvinceLinkageController/getCity?pid=" + data.value).done(function (list) {
                    fillSelect($city, list, 'cityCode', 'cityName', ph.city);
                    form.render('select');
                });
            }
            form.render('select');
        });
        form.on('select(' + ids.city + ')', function (data) {
            $area.empty().append("<option value=''>" + ph.area + "</option>");
            $street.empty().append("<option value=''>" + ph.street + "</option>");
            $zone.empty().append("<option value=''>" + ph.zone + "</option>");
            if (data.value) {
                get(rootPath + "/ProvinceLinkageController/getArea?pid=" + data.value).done(function (list) {
                    fillSelect($area, list, 'areaCode', 'areaName', ph.area);
                    form.render('select');
                });
            }
            form.render('select');
        });
        form.on('select(' + ids.area + ')', function (data) {
            $street.empty().append("<option value=''>" + ph.street + "</option>");
            $zone.empty().append("<option value=''>" + ph.zone + "</option>");
            if (data.value) {
                get(rootPath + "/ProvinceLinkageController/getStreet?pid=" + data.value).done(function (list) {
                    fillSelect($street, list, 'streetCode', 'streetName', ph.street);
                    form.render('select');
                });
            }
            form.render('select');
        });
        form.on('select(' + ids.street + ')', function (data) {
            loadZones(data.value, null);
        });
    }

    return {init: init};
})();
