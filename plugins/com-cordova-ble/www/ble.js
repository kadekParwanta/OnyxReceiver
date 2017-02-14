/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

var exec = require('cordova/exec'),
    cordova = require('cordova'),
    channel = require('cordova/channel'),
    utils = require('cordova/utils');

var MODULE_NAME = 'Ble';

function Ble() { }

/**
 * initSDK
 * @param {Function} successCallback
 * @param {Function} errorCallback
 */
Ble.prototype.initSDK = function (clientId, secret, success, fail) {
    exec(success, fail, MODULE_NAME, 'initSDK', [clientId, secret]);
};

Ble.prototype.onWebRequested = function (success) {
    if (typeof (success) !== 'function') {
        throw "A callback must be provided";
    }

    exec(success, function(){}, MODULE_NAME, 'addWebListener', []);
}

Ble.prototype.didRangeBeaconsInRegion = function (success) {
    if (typeof (success) !== 'function') {
        throw "A callback must be provided";
    }

    exec(success, function(){}, MODULE_NAME, 'addOnyxBeaconsListener', []);
}

Ble.prototype.onTagsReceived = function (success) {
    if (typeof (success) !== 'function') {
        throw "A callback must be provided";
    }

    exec(success, function(){}, MODULE_NAME, 'addTagsListener', []);
}

var me = new Ble();

if (cordova.platformId === 'android' || cordova.platformId === 'ios' || cordova.platformId === 'windowsphone') {
    channel.createSticky('onBlePluginReady');
    channel.waitForInitialization('onBlePluginReady');

    channel.onCordovaReady.subscribe(function () {
        // me.initSDK("cd8d429b30f58c577f2bc0dbfd07d03b07de7d2f","ce7ada82d2b3917dd35520747bf6492b4f5e1d4c",function(success){
        //     channel.onBlePluginReady.fire();
        // }, function(e){
        //     console.log('failed to init SDK');
        //     channel.onBlePluginReady.fire();
        // })
        channel.onBlePluginReady.fire();
    });
}

module.exports = me;