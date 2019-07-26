function initStuff(scope) {

    scope.serverUrl = "localhost";
    scope.protocol = "ws";
    scope.port = "8080";
    scope.openWSUrl = scope.protocol + "://" + scope.serverUrl + ":" + scope.port + "/v1/open";

    scope.dev1lat = "1";
    scope.dev2lat = "1";
    scope.dev3lat = "1";
    scope.dev4lat = "1";

    scope.dev1lon = "1";
    scope.dev2lon = "3";
    scope.dev3lon = "2";
    scope.dev4lon = "5";

}