cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "id": "com-cordova-ble.ble",
        "file": "plugins/com-cordova-ble/www/ble.js",
        "pluginId": "com-cordova-ble",
        "clobbers": [
            "ble"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-whitelist": "1.3.2-dev",
    "com-cordova-ble": "2.0.1-dev"
};
// BOTTOM OF METADATA
});