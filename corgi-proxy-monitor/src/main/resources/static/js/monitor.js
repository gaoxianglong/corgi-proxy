$(function(){

    show_nodes();

    //渲染图
    function show_nodes(){
        var myChart = echarts.init(document.getElementById('service-nodes'));
        myChart.showLoading();
        $.ajax({
            url : "/corgi/monitor/getCorgiNodes.do",
            type : "post",
            contentType: "application/json;charset=UTF-8",
            success : function(result) {
                myChart.hideLoading();
                var obj = JSON.parse(result);
                if(0  == obj.errorCode){
                    var colorArr = new Array('#FF5722','#FF6600','#FFB800','#5FB878','#01AAED','#3974CB','#6666CC');
                    var data = obj.data,nodes = new Array();
                    for (var i = 0; i < data.length; i++) {
                        var node_label = {
                            color:'#FFFFFF',
                            padding:10,
                            borderRadius:5,
                            backgroundColor:colorArr[(i + 1) % colorArr.length]
                        };
                        var temp = {
                            name : data[i],
                            label : node_label
                        };
                        nodes.push(temp);
                    }
                    var data = {
                        "name": "Corgi-proxy",
                        "label":{
                            color:'#FFFFFF',
                            backgroundColor:colorArr[0],
                            padding:10,
                            borderRadius:5,
                            shadowColor:'#fff',
                            shadowBlur:5,
                            shadowOffsetX:5,
                            shadowOffsetY:5
                        },
                        "children": nodes
                    };

                    echarts.util.each(data.children, function (datum, index) {
                        index % 2 === 0 && (datum.collapsed = true);
                    });
                    myChart.setOption(option = {
                        tooltip: {
                            trigger: 'item',
                            triggerOn: 'mousemove'
                        },
                        series: [
                            {
                                type: 'tree',
                                expandAndCollapse:false,
                                data: [data],
                                top: '1%',
                                left: '15%',
                                bottom: '1%',
                                right: '30%',
                                symbolSize: 10,
                                label: {
                                    normal: {
                                        position: 'left',
                                        verticalAlign: 'middle',
                                        align: 'right',
                                        fontSize: 16,
                                    }
                                },
                                lineStyle:{
                                    color:'#01AAED'
                                },

                                leaves: {
                                    label: {
                                        normal: {
                                            position: 'right',
                                            verticalAlign: 'middle',
                                            align: 'left'
                                        }
                                    }
                                },

                                expandAndCollapse: true,
                                animationDuration: 550,
                                animationDurationUpdate: 750
                            }
                        ]
                    });
                }else{
                    console.log("---request error:" + result)
                }
            },
            error : function(e){
                console.log(e.status);
                console.log(e.responseText);
            }
        });
        myChart.on("click", clickFun);
        function clickFun(param) {
            console.log("---click now--");
            if (typeof param.seriesIndex == 'undefined') {
                return;
            }
            if (param.type == 'click') {
                console.log(param.name);
            }
        }

    }
});