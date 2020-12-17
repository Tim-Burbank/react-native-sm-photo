
#import <Foundation/Foundation.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol CameraControllerDelegate <NSObject>

- (void)takePhotoPath:(NSString *)path;

- (void)takeVideoPat:(NSString *)path thumbnailPath:(NSString *)tnp duration:(NSInteger)time;

@end

@interface CameraController : NSObject

@property (nonatomic, weak) id<CameraControllerDelegate> delegate;

- (void)presentCustomTakePhotoController:(UIViewController *)vc;

- (void)present:(NSString *)code vc:(UIViewController *)vc;

- (void)presentTakePhotoController:(UIViewController *)vc;

- (void)present2:(NSTimeInterval)minDuration maxDuration:(NSTimeInterval)maxDuration vc:(UIViewController *)vc;

@end

NS_ASSUME_NONNULL_END
