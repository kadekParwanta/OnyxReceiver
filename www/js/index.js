/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function () {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);

        var btnInitSdk = document.getElementById("btnInitSdk");
        btnInitSdk.addEventListener('click', this.initSDK.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function () {
        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function (id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },

    initSDK: function () {
        var ble = window.ble;

        var i = 0;

        ble.onWebRequested(function (info) {
            console.log(info);
        });

        ble.didRangeBeaconsInRegion(function (beacons) {
            console.log("didRangeBeaconsInRegion " + JSON.stringify(beacons) + " i= " + i);
            if (i === 0) {
                console.log("buzzBeacon");
                ble.buzzBeacon(function (s) {
                    console.log(s);
                }, function (f) {
                    console.log("buzzBeacon error: " + f);
                }, JSON.stringify(beacons[0]));
                i++;
            }
        });

        ble.onError(function (error) {
            console.log("error " + error);
        });

        ble.onCouponReceived(function (coupon) {
            console.log("onCouponReceived " + JSON.stringify(coupon));
            ble.showCoupon(function (s) {
                console.log(s);
            }, function (f) {
                console.log(f);
            }, JSON.stringify(coupon[0]))
        });

        ble.onTagsReceived(function (tags) {
            console.log("onTagsReceived " + JSON.stringify(tags));
        })

        ble.onDeliveredCouponsReceived(function (coupons) {
            console.log("onDeliveredCouponsReceived " + coupons);
        });

        ble.initSDK(function (sucess) {
            console.log("Success initSDK");
            ble.getDeliveredCoupons();
            ble.getTags(function (c) {
                console.log(c);
            });
        }, function (err) {
            console.log("failed init sdk");
        })
    }
};

app.initialize();
