const myApp = angular.module('app', []);

myApp.controller('paramTable', ['$scope', '$log', function ($scope, $log) {
    $scope.$log = $log;
    initStuff($scope);

    $scope.$watchCollection("[protocol,serverUrl,port]", function () {
        $scope.openWSUrl = $scope.protocol + "://" + $scope.serverUrl + ":" + $scope.port + "/connect"
    });

    let myWebSocket1, myWebSocket2, myWebSocket3, myWebSocket4;

    // controls the disabling / enabling of the buttons
    $scope.isDisabled1 = false;
    $scope.isDisabled2 = false;
    $scope.isDisabled3 = false;
    $scope.isDisabled4 = false;

    // sending messages stuff
    const sendGen = function (socket, msg) {
        socket.send(msg);
    };

    $scope.send1 = function () {
        sendGen(myWebSocket1, $scope.msg1);
        console.log("sending msg 1: " + $scope.msg1);
    };

    $scope.send2 = function () {
        sendGen(myWebSocket2, $scope.msg2);
        console.log("sending msg 2: " + $scope.msg2);
    };

    $scope.send3 = function () {
        sendGen(myWebSocket3, $scope.msg3);
        console.log("sending msg 3: " + $scope.msg3);
    };

    $scope.send4 = function () {
        sendGen(myWebSocket4, $scope.msg4);
        console.log("sending msg 4: " + $scope.msg4);
    };

    // websockets connection stuff
    function onM1(evt) {
        console.log("dev1 got data: " + evt.data);
        $scope.dev1msg = evt.data;
        $scope.$apply();
    }

    function onM2(evt) {
        console.log("dev2 got data: " + evt.data);
        $scope.dev2msg = evt.data;
        $scope.$apply();
    }

    function onM3(evt) {
        console.log("dev3 got data: " + evt.data);
        $scope.dev3msg = evt.data;
        $scope.$apply();
    }

    function onM4(evt) {
        console.log("dev4 got data: " + evt.data);
        $scope.dev4msg = evt.data;
        $scope.$apply();
    }

    function onO(id, evt) {
        // here "id" is a string. equivalent to
        //   $scope.isDisabled1 === $scope["isDisabled1"]
        //$scope[id] = false;
        console.log("Connection open ...");
        $scope.$apply();
    }

    function onC(id, evt) {
        $scope[id] = false;
        console.log("Connection closed.");
        $scope.$apply();
    }

    $scope.getConnectUrl = function (lat, lon) {
        return $scope.openWSUrl + "?lat=" + lat + "&lon=" + lon
    };

    $scope.connect1 = function () {
        $scope.isDisabled1 = true;
        myConnectUrl = $scope.getConnectUrl($scope.dev1lat, $scope.dev1lon);
        myWebSocket1 = new WebSocket(myConnectUrl);
        myWebSocket1.onmessage = onM1;

        // onO.bind creates a new function with only one parameter,
        //  and the first one will be set as "isDisabled1"
        myWebSocket1.onopen = onO.bind(null, "isDisabled1");
        myWebSocket1.onclose = onC.bind(null, "isDisabled1");
    };

    $scope.connect2 = function () {
        $scope.isDisabled2 = true;
        myConnectUrl = $scope.getConnectUrl($scope.dev2lat, $scope.dev2lon);
        myWebSocket2 = new WebSocket(myConnectUrl);
        myWebSocket2.onmessage = onM2;
        myWebSocket2.onopen = onO.bind(null, "isDisabled2");
        myWebSocket2.onclose = onC.bind(null, "isDisabled2");
    };

    $scope.connect3 = function () {
        $scope.isDisabled3 = true;
        myConnectUrl = $scope.getConnectUrl($scope.dev3lat, $scope.dev3lon);
        myWebSocket3 = new WebSocket(myConnectUrl);
        myWebSocket3.onmessage = onM3;
        myWebSocket3.onopen = onO.bind(null, "isDisabled3");
        myWebSocket3.onclose = onC.bind(null, "isDisabled3");
    };

    $scope.connect4 = function () {
        $scope.isDisabled4 = true;
        myConnectUrl = $scope.getConnectUrl($scope.dev4lat, $scope.dev4lon);
        myWebSocket4 = new WebSocket(myConnectUrl);
        myWebSocket4.onmessage = onM4;
        myWebSocket4.onopen = onO.bind(null, "isDisabled4");
        myWebSocket4.onclose = onC.bind(null, "isDisabled4");
    };

    $scope.deliver1 = function() {myWebSocket1.send($scope.dev1pl);};
    $scope.deliver2 = function() {myWebSocket2.send($scope.dev2pl);};
    $scope.deliver3 = function() {myWebSocket3.send($scope.dev3pl);};
    $scope.deliver4 = function() {myWebSocket4.send($scope.dev4pl);};

}]);
