var wsconn=new WebSocket("wss://api.v1.ws.chicdn.cn/");
wsconn.onmessage=function(event){
    console.log(event);
}
setInterval(function(){wsconn.send("HB~")},60000);