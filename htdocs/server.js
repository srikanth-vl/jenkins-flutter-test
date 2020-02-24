var express = require('express');
var morgan = require('morgan');
var app = express();
var compression = require('compression');
app.use(morgan('dev'));
app.use(compression());


app.use("/styles", express.static(__dirname + '/dist/styles'));
app.use("/scripts", express.static(__dirname + '/dist/scripts'));
app.use("/images", express.static(__dirname + '/dist/images'));
app.use("/fonts", express.static(__dirname + '/dist/fonts'));
app.use("/data", express.static(__dirname + '/dist/data'));
app.use("/", express.static(__dirname + '/dist/'));

app.get('*', function(req, res){
    res.sendfile(__dirname + '/dist/index.html');
});

app.listen(process.env.PORT || 5005);
