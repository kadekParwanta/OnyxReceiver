//
//  Ble.h
//  onyxB
//
//  Created by Nomi on 21/11/2015.
//
//

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>

#import <OnyxBeaconLib/OnyxBeacon.h>

@interface Ble : CDVPlugin<OnyxBeaconContentDelegate, OnyxBeaconDelegate>

- (void) addWebListener:(CDVInvokedUrlCommand *)command;
- (void) addOnyxBeaconsListener: (CDVInvokedUrlCommand*)command;
- (void) initSDK: (CDVInvokedUrlCommand*)command;

@end