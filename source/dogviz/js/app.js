function addMarker(id, lat, lng) {
    return new RichMarker({
        map: map,
        draggable: false,
        animation: google.maps.Animation.DROP,
        position: new google.maps.LatLng(lat, lng),
        content: '<img id="dog' + id + '" class="gMarker" src="img/dog.svg"/>',
        anchor: RichMarkerPosition.MIDDLE,
        shadow: 0,
    });
}

function img2svg(id) {
    $('img.gMarker[id="dog' + id + '"]').each(function(){
        var $img = $(this);
        var imgID = $img.attr('id');
        var imgClass = $img.attr('class');
        var imgURL = $img.attr('src');
        var imgClr = $img.attr('clr');

        $.get(imgURL, function(data) {
            var $svg = $(data).find('svg');

            if(imgID !== undefined) 
                $svg = $svg.attr('id', imgID);
            if(imgClass !== undefined)
                $svg = $svg.attr('class', imgClass);
            if(imgClr !== undefined)
                $svg = $svg.attr('clr', imgClr);

            $svg = $svg.removeAttr('xmlns:a');
            $img.replaceWith($svg);
        }, 'xml');
    });
}

function guid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0;
        var v = c=='x' ? r : r&0x3|0x8;
        return v.toString(16);
    });
}

var app = angular.module('DSApp', ['ngAnimate']).run(['$rootScope', function($rs) {
    
    var colors = ['red', 'blue', 'green', 'orange', 'cyan', 'purple'];
    var availClr = colors.slice(0);
    var markers = [];

    var client = new Paho.MQTT.Client(config.host, config.mqtt_port, '', guid());
    
    client.onConnectionLost = function(e) {
        console.log('MQTT connection closed: ' + e.errorMessage);
    };

    client.onMessageArrived = function(e) {
        var jsondata;
        try {
            jsondata = JSON.parse(e.payloadString);
        } catch(err) { 
            return;
        }
            updateData(jsondata)
    };

    client.connect({
        useSSL: config.isSSL,
        onSuccess: function() {
            console.log("connected to MQTT server");
            client.subscribe(config.topic);
        }
    });

    $rs.newRegistered = false;
    $rs.showPanel = false;
    $rs.selDogs = [];
    
    function updateData(data) {
        for (var i in data) {
            if (markers[data[i].id] === undefined) {
                var m = addMarker(data[i].id, data[i].lat, data[i].long);
                m.data = data[i];
                markers[data[i].id] = m;

                google.maps.event.addListener(m, 'click', function() {
                    var thiz = this;
                    var id = this.data.id;

                    $rs.$apply(function() {
                        var i = $rs.selDogs.indexOf(thiz);
                        if (i == -1) {
                            if (availClr.length == 0)
                                availClr = colors.slice(0);
                            var color = thiz.data.color = availClr.pop();
                            $('.gMarker[id="dog' + id + '"]').attr('clr', color);
                            $rs.selDogs.push(thiz);
                            img2svg(id);
                        } else {
                            if (availClr.length == colors.length)
                                availClr = [];
                            var color = $rs.selDogs.splice(i, 1)[0].data.color;
                            availClr.push(color);
                            $('.gMarker[id="dog' + id + '"]').removeAttr('clr');
                        }
                    });

                    var c = $('.scr-container')[0];
                    c.scrollTop = c.scrollHeight;
                });

                if ($rs.newRegistered) {
                    google.maps.event.addListener(m, 'ready', function() {
                        google.maps.event.trigger(m, 'click');
                    });
                    $rs.newRegistered = false;
                }
            } else {
                $rs.$apply(function() {
                    var m = markers[data[i].id];
                    var color = m.data.color;
                    m.data = data[i];
                    m.data.color = color;
                    m.setPosition(new google.maps.LatLng(m.data.lat, m.data.long));
                });
            }
        }
    }
}]);

app.controller('addDogCtrl', function($rootScope, $scope, $http) {
    function reset() {
        $scope.ADModel = [
            {title: "Name", vn: "name", val: ""},
            {title: "Weight", vn: "weight", val: ""},
            {title: "Heartbeat", vn: "heartBeat", val: ""},
            {title: "Temperature", vn: "temperature", val: ""},
            {title: "Latitude", vn: "lat", val: "51.043540"},
            {title: "Longitude", vn: "long", val: "-114.128314"},
        ];
    }
    $scope.newDog = function() {
        var postObj = {};
        for (var i in $scope.ADModel)
            postObj[$scope.ADModel[i].vn] = $scope.ADModel[i].val;
            
        $http({
            method: 'POST',
            url: config.getHTTPString() + '/dogs',
            data: JSON.stringify(postObj),
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(function(result) {
            $rootScope.newRegistered = true;
            reset(); 
        }, function(error) {
           console.log(error);
        });
    };
    reset();
});

app.controller('showClusteringCtrl', function($scope, $http) {
    function reset() {
        $scope.k = '';
        $scope.res = [];
    }
    $scope.show = function() {
        $http.get(config.getHTTPString() + '/dogClusters?showWeight&k=' + $scope.k).success(function (data) {
            var d = data.value;
            var k = data.numberOfClusters;
            var lens = d.map(function(x) { return x.length });
            var maxLen = lens.reduce(function(x, y) { return Math.max(x, y) });
            var res = [];

            d.sort(function(a, b) {
                var add = function(x, y) { return {w: x.w + y.w} };
                var avg_a = a.reduce(add).w / a.length;
                var avg_b = b.reduce(add).w / b.length;
                return avg_a > avg_b;
            });

            for (var i = 0; i < maxLen; i++) {
                var row = [];
                for (var j = 0; j < k; j++) {
                    var o = d[j][i];
                    if (o === undefined)
                        row.push({str: ''});
                    else 
                        row.push({str: o.id + ' - ' + o.w.toFixed(1)});
                }
                res.push(row);
            }
            $scope.res = res;
        });
        reset();
    };
    reset();
});
