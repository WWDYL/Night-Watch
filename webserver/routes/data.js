/**
 * Created by BarryGates on 2016/5/21.
 */
var express = require('express');
var router = express.Router();
var mongo = require('mongodb');
var co = require('co');

router.get('/', function(req, res) {
    var MongoClient = mongo.MongoClient;
    var assert = require('assert');
    var url = "mongodb://localhost:27017/mydb";
    co(function*(){
        var db = yield MongoClient.connect(url);
        var collection = db.collection('traffic');
        var documents = yield collection.find().toArray();
        var jsondata = { "data": [] };

        for (var i = 0; i < documents.length; ++i) {
            jsondata.data.push(documents[i]);
        }
        res.send(jsondata);
        db.close();
    });
});

module.exports = router;