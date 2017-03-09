var express = require('express');
var router = express.Router();
var redis = require('redis'),
    client = redis.createClient();

client.on('error', function(err) {
    console.log('redis is error!');
});

// {
//     cols: ["","",""]
//     series: [{
//         "name": "",
//         "data": []
//     }]
// }

router.get('/', function(req, res) {
    var timedata = [];
    var typedata = [];
    var data = {};
    var jsondata = {"cols" : [], "series" : []};
    client.get("attack:", function (err, reply) {

        var timer = 1, timer_end = reply;

        for (var i = 1; i <= reply; ++i) {
            client.hgetall("attack:" + i, function(err, obj) {

                // var item = {};
                // item.Time = Date.parse(obj.BeginTime).getMinutes();
                // item.Type = obj.Description;

                var d = new Date(Date.parse(obj.BeginTime));
                stime = d.getMinutes();
                stype = obj.Description;

                // data.push(item);

                if (timedata.indexOf(stime) == -1) timedata.push(stime);
                if (typedata.indexOf(stype) == -1) typedata.push(stype);


                if (data[stime]) {
                    if (data[stime][stype]) {
                        data[stime][stype] += 1;
                    } else {
                        data[stime][stype] = 1;
                    }
                } else {
                    data[stime] = {};
                }

                if (timer == timer_end) {
                    jsondata.cols = timedata.sort();
                    for (var i = 0; i < typedata.length; ++i) {
                        jsondata.series.push({"name": typedata[i]});
                    }

                    for (var j = 0; j < jsondata.series.length; ++j) {
                        var name = jsondata.series[j].name;
                        jsondata.series[j].data = [];
                        for (var k = 0; k < timedata.length; ++k) {
                            var time = timedata[k];
                            var val = 0;
                            console.log(data);
                            if (data[time]) {
                                if (data[time][name]) {
                                    val = data[time][name];
                                }
                            }
                            jsondata.series[j].data.push(val);
                        }
                    }

                    res.send(jsondata);
                }
                timer++;
            });
        }
    });

});

module.exports = router;