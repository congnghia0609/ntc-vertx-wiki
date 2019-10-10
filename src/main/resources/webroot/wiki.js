'use strict';

function generateUUID() { // Public Domain/MIT
    var d = new Date().getTime();//Timestamp
    var d2 = (performance && performance.now && (performance.now()*1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16;//random number between 0 and 16
        if(d > 0){//Use timestamp until depleted
            r = (d + r)%16 | 0;
            d = Math.floor(d/16);
        } else {//Use microseconds since page-load if supported
            r = (d2 + r)%16 | 0;
            d2 = Math.floor(d2/16);
        }
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

angular.module("wikiApp", [])
    .controller("WikiController", ["$scope", "$http", "$timeout", function ($scope, $http, $timeout) {
            var DEFAULT_PAGENAME = "Example page";
            var DEFAULT_MARKDOWN = "# Example page\n\nSome text _here_.\n";
            var clientUuid = generateUUID();
            var eb = new EventBus(window.location.protocol + "//" + window.location.host + "/eventbus");
            
            $scope.newPage = function () {
                $scope.pageId = undefined;
                $scope.pageName = DEFAULT_PAGENAME;
                $scope.pageMarkdown = DEFAULT_MARKDOWN;
                $scope.client = clientUuid;
            };
            $scope.reload = function () {
                $http.get("/api/pages").then(function (response) {
                    $scope.pages = response.data.pages;
                });
            };
            $scope.pageExists = function () {
                return $scope.pageId !== undefined;
            };
            $scope.load = function (id) {
                $http.get("/api/pages/" + id).then(function (response) {
                    var page = response.data.page;
                    $scope.pageId = page.id;
                    $scope.pageName = page.name;
                    $scope.pageMarkdown = page.markdown;
                    $scope.updateRendering(page.html);
                });
            };
            $scope.updateRendering = function (html) {
                document.getElementById("rendering").innerHTML = html;
            };
            $scope.save = function () {
                var payload;
                if ($scope.pageId === undefined) {
                    payload = {
                        "name": $scope.pageName,
                        "markdown": $scope.pageMarkdown
                    };
                    $http.post("/api/pages", payload).then(function (ok) {
                        $scope.reload();
                        $scope.success("Page created");
                        var guessMaxId = _.maxBy($scope.pages, function (page) {
                            return page.id;
                        });
                        $scope.load(guessMaxId.id || 0);
                    }, function (err) {
                        $scope.error(err.data.error);
                    });
                } else {
                    var payload = {
                        "markdown": $scope.pageMarkdown,
                        "client": $scope.client
                    };
                    $http.put("/api/pages/" + $scope.pageId, payload).then(function (ok) {
                        $scope.success("Page saved");
                    }, function (err) {
                        $scope.error(err.data.error);
                    });
                }
            };
            $scope.delete = function () {
                $http.delete("/api/pages/" + $scope.pageId).then(function (ok) {
                    $scope.reload();
                    $scope.newPage();
                    $scope.success("Page deleted");
                }, function (err) {
                    $scope.error(err.data.error);
                });
            };
            $scope.success = function (message) {
                $scope.alertMessage = message;
                var alert = document.getElementById("alertMessage");
                alert.classList.add("alert-success");
                alert.classList.remove("invisible");
                $timeout(function () {
                    alert.classList.add("invisible");
                    alert.classList.remove("alert-success");
                }, 3000);
            };
            $scope.error = function (message) {
                $scope.alertMessage = message;
                var alert = document.getElementById("alertMessage");
                alert.classList.add("alert-danger");
                alert.classList.remove("invisible");
                $timeout(function () {
                    alert.classList.add("invisible");
                    alert.classList.remove("alert-danger");
                }, 5000);
            };
            $scope.reload();
            $scope.newPage();
            var markdownRenderingPromise = null;
            $scope.$watch("pageMarkdown", function (text) {
                if (markdownRenderingPromise !== null) {
                    $timeout.cancel(markdownRenderingPromise);
                }
                markdownRenderingPromise = $timeout(function () {
                    markdownRenderingPromise = null;
                    // Update edit content to all User.
                    // Case1: HTTP POST
                    $http.post("/app/markdown", text).then(function (response) {
                        $scope.updateRendering(response.data);
                    });
                    // Case2: WebSocket
//                    eb.send("app.markdown", text, function (err, reply) {
//                        if (err === null) {
//                            $scope.$apply(function () {
//                                $scope.updateRendering(reply.body);
//                            });
//                        } else {
//                            console.warn("Error rendering Markdown content: " + JSON.stringify(err));
//                        }
//                    });
                }, 300);
            });
            
            eb.onopen = function () {
                eb.registerHandler("page.saved", function (error, message) {
                    console.log(message);
                    if (message.body && $scope.pageId === message.body.id && clientUuid !== message.body.client) {
                        $scope.$apply(function () {
                            $scope.pageModified = true;
                        });
                    }
                });
            };
            
        }]);