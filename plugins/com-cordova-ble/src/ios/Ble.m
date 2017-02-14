//
//  Ble.m
//  onyxB
//
//  Created by Nomi on 21/11/2015.
//
//

#import "Ble.h"
#import "AFNetworkActivityLogger.h"
#import <Cordova/CDVConfigParser.h>
#import <Cordova/NSDictionary+CordovaPreferences.h>

@interface Ble ()

@property (nonatomic, copy) void (^rangeBeaconsHandler)(NSArray *beacons, OBBeaconRegion *region);

@end


@implementation Ble

NSMutableArray *rangeBeaconsListeners;
NSDictionary *preferences;

/*
 NSDictionary *jsonObj = [ [NSDictionary alloc]
 initWithObjectsAndKeys :
 dateStr, @"dateStr",
 @"true", @"success",
 nil
 ];
 */

- (void)pluginInitialize {
    _rangeBeaconsHandler = [self createRangeBeaconsHandler];
    CDVConfigParser *parser = [[CDVConfigParser alloc] init];
    preferences = parser.settings;
}

#pragma mark - Coupan View

- (void)getContent:(CDVInvokedUrlCommand *)command{
    
    NSArray *coupons = [[OnyxBeacon sharedInstance] getContent];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray: coupons];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


-(void)addOnyxBeaconsListener:(CDVInvokedUrlCommand *)command {
    if (rangeBeaconsListeners == nil) {
        rangeBeaconsListeners = [[NSMutableArray alloc] init];
    }
    [rangeBeaconsListeners addObject:command.callbackId];
}

-(void) addWebListener:(CDVInvokedUrlCommand *)command {
    [[OnyxBeacon sharedInstance] setLogger:^(NSString *message) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
    [[AFNetworkActivityLogger sharedLogger] setLoggerBlock:^(NSString *message) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
    [[AFNetworkActivityLogger sharedLogger] setLevel:AFLoggerLevelDebug];
    [[AFNetworkActivityLogger sharedLogger] startLogging];
}

-(void (^)(NSArray *beacons, OBBeaconRegion *region)) createRangeBeaconsHandler {
    return ^(NSArray *beacons, OBBeaconRegion *region) {
        NSMutableArray * results = [[NSMutableArray alloc] init];
        for ( int i = 0, size = beacons.count; i< size; i++) {
            OBBeacon* beacon = [beacons objectAtIndex:i];
            NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
            [dict setValue:beacon.major forKey:@"major"];
            [dict setValue:beacon.minor forKey:@"minor"];
            [dict setValue:[NSNumber numberWithInteger:beacon.rssi] forKey:@"rssi"];
            [dict setValue:[NSNumber numberWithInteger:beacon.proximity] forKey:@"proximity"];
            [results addObject:dict];
        }
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
        [result setKeepCallbackAsBool:YES];
        for (NSString *callbackId in rangeBeaconsListeners) {
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    };
}

#pragma mark - OnyxBeaconCouponDelegate Methods
- (void)didRangeBeacons:(NSArray *)beacons inRegion:(OBBeaconRegion *)region {
    _rangeBeaconsHandler(beacons, region);
}


- (void)locationManagerDidEnterRegion:(CLRegion *)region{

    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@\");", @"window.cordova.plugins.Ble.locationManagerDidEnterRegion", region];
    [self.commandDelegate evalJs:jsString];
    
}




- (void)locationManagerDidExitRegion:(CLRegion *)region{
    
    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@\");", @"window.cordova.plugins.Ble.locationManagerDidExitRegion", region];
    [self.commandDelegate evalJs:jsString];
    
}





- (void)didReceiveContent:(NSArray *)coupons {
    /*
     for (OBContent *coupon in coupons) {
     UILocalNotification *notification = [[UILocalNotification alloc] init];
     notification.alertBody = coupon.message;
     notification.userInfo = @{@"uuid": coupon.uuid};
     notification.soundName = UILocalNotificationDefaultSoundName;
     [[UIApplication sharedApplication] presentLocalNotificationNow:notification];
     }
     */
    //Send Coupan to Cordova
    
    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@\");", @"window.cordova.plugins.Ble.didReceiveContent", coupons];
    [self.commandDelegate evalJs:jsString];
}

- (void)didRequestInfo:(OBContent *)content inViewController:(UIViewController *)viewController {
    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@,%@\");", @"window.cordova.plugins.Ble.didRequestInfo", content,viewController.restorationIdentifier];
    [self.commandDelegate evalJs:jsString];
}

- (void)contentOpened:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];

    
    
    [[OnyxBeacon sharedInstance] contentOpened:content];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"contentOpened Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getTags:(CDVInvokedUrlCommand *)command {
    NSArray * result  = [[OnyxBeacon sharedInstance] getTags];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray:result];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)getSelectedTags:(CDVInvokedUrlCommand *)command {
    NSArray * result  = [[[OnyxBeacon sharedInstance] getSelectedTags] allObjects];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray:result];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)clearCoupons:(CDVInvokedUrlCommand *)command {
    [[OnyxBeacon sharedInstance] clearCoupons];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"clearCoupons Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)setTags:(CDVInvokedUrlCommand *)command {
    
    NSSet *set = [[NSSet alloc] init];
    [set setByAddingObjectsFromArray:command.arguments];
    [[OnyxBeacon sharedInstance] setTags:set];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"setTags Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)contentTapped:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    [[OnyxBeacon sharedInstance] contentTapped:content];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"contentTapped Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showContentInfo:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    UIViewController *rootViewController = window.rootViewController;

    [[OnyxBeacon sharedInstance] showContentInfo:content inViewController:rootViewController ];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"showContentInfo Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)viewControllerForContent:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    UIViewController *c = [[OnyxBeacon sharedInstance] viewControllerForContent:content];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: c.restorationIdentifier];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)deleteContent:(CDVInvokedUrlCommand *)command {
    OBContent *content = [OBContent alloc];
    [[OnyxBeacon sharedInstance] deleteContent:content];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"deleteContent Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


#pragma mark - Main OnyxBeacon

- (void) version: (CDVInvokedUrlCommand *)command {
    
    [[OnyxBeacon sharedInstance] version];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"Version Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

- (void)sendUserMetrics : (CDVInvokedUrlCommand *)command {
    NSMutableDictionary* user = [command.arguments objectAtIndex:0];

    [[OnyxBeacon sharedInstance] sendUserMetrics:user];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"sendUserMetrics Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)registerForPushNotificationWithDeviceToken : (CDVInvokedUrlCommand *)command {
    NSData* token = [[command.arguments objectAtIndex:0] dataUsingEncoding:NSUTF8StringEncoding];
    NSString* provider = [command.arguments objectAtIndex:0];
    
    [[OnyxBeacon sharedInstance] registerForPushNotificationWithDeviceToken:token forProvider:provider handler:^(NSDictionary *d, NSError *e) {
        
    }];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"registerForPushNotificationWithDeviceToken Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}





- (void)sendPushNotificationProviderDeviceToken : (CDVInvokedUrlCommand *)command {
    NSString* token = [command.arguments objectAtIndex:0] ;
    
    [[OnyxBeacon sharedInstance] sendPushNotificationProviderDeviceToken:token];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"sendPushNotificationProviderDeviceToken Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)sendReport :(CDVInvokedUrlCommand *)command {
    
    NSData* data = [[command.arguments objectAtIndex:0] dataUsingEncoding:NSUTF8StringEncoding];
    NSString* reporter = [command.arguments objectAtIndex:1];
    NSString* message = [command.arguments objectAtIndex:2];
    
    [[OnyxBeacon sharedInstance] sendReport:data reporter:reporter message:message handler:^(NSError *error) {

    }];
    
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"sendLogReport Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)viewControllerForTags:(CDVInvokedUrlCommand *)command
{
    UIViewController *c =  [[OnyxBeacon sharedInstance] viewControllerForTags];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: c.restorationIdentifier];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    
}

- (void)requestWhenInUseAuthorization:(CDVInvokedUrlCommand *)command
{
    return [[OnyxBeacon sharedInstance] requestWhenInUseAuthorization];
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"requestWhenInUseAuthorization Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}



- (void)onyxBeaconError:(NSError *)error {
    NSString* jsString = nil;
    jsString = [NSString stringWithFormat:@"%@(\"%@\");", @"window.cordova.plugins.Ble.onyxBeaconError", error];
    [self.commandDelegate evalJs:jsString];
    
}



-(void) requestAlwaysAuthorization :(CDVInvokedUrlCommand *)command {
    [[OnyxBeacon sharedInstance] requestAlwaysAuthorization];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"requestAlwaysAuthorization Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void) resetService:(CDVInvokedUrlCommand *)command {
    
    [[OnyxBeacon sharedInstance] resetService];
    
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"resetService Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

-(void) setLogger:(CDVInvokedUrlCommand *)command {
    
    [[OnyxBeacon sharedInstance] setLogger:^(NSString *message) {
        
    }];
    
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"setLogger Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

-(void) initSDK:(CDVInvokedUrlCommand *)command {
    NSString* SA_CLIENTID = [command.arguments objectAtIndex:0];
    NSString* SA_SECRET = [command.arguments objectAtIndex:1];
    NSString* clientIDPref = [preferences cordovaSettingForKey:@"com-cordova-ble-clientId"];
    NSString* secretPref = [preferences cordovaSettingForKey:@"com-cordova-ble-secret"];
    NSLog(@"initSDK clientIDPref = %@, secretPref=%@",clientIDPref, secretPref);
    
    if (![clientIDPref isEqualToString:@""] && clientIDPref.length > 0){
        SA_CLIENTID = clientIDPref;
    }
    
    if (![secretPref isEqualToString:@""] && secretPref.length > 0){
        SA_SECRET = secretPref;
    }
    
    
    NSLog(@"initSDK SA_CLIENTID = %@, SA_SECRET=%@",SA_CLIENTID, SA_SECRET);
    
    
    [[OnyxBeacon sharedInstance] requestAlwaysAuthorization];
    [[OnyxBeacon sharedInstance] startServiceWithClientID:SA_CLIENTID secret:SA_SECRET];
    [[OnyxBeacon sharedInstance] setContentDelegate:self];
    [[OnyxBeacon sharedInstance] setDelegate:self];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"startServiceWithClientID Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)applicationDidEnterBackground:(CDVInvokedUrlCommand *)command
{
    [[OnyxBeacon sharedInstance] didEnterBackground];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"didEnterBackground Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)applicationWillEnterForeground:(CDVInvokedUrlCommand *)command
{
    [[OnyxBeacon sharedInstance] willEnterForeground];
    
    CDVPluginResult* pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsString: @"didEnterBackground Invoked"];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

@end