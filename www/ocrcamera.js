var exec = require('cordova/exec');

exports.openCameraOCR = function (arg0, success, error) {
    exec(success, error, 'ocrcamera', 'openCameraOCR', [arg0]);
};
