
#if __has_include("RCTBridgeModule.h")
//#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#else
//#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#endif


@interface PhotoPlugin : RCTEventEmitter

@property (class, nonatomic, strong) NSBundle *currentBundle;

@end
  
