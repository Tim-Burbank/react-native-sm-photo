
#import "CameraBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@protocol CameraRecordVideo2ViewControllerDelegate <NSObject>

- (void)videoURL:(NSURL * _Nullable)url;

@end

@interface CameraRecordVideo2ViewController : CameraBaseViewController

@property (nonatomic, weak) id<CameraRecordVideo2ViewControllerDelegate> delegate;

@property (nonatomic, assign) NSTimeInterval minDuration;
@property (nonatomic, assign) NSTimeInterval maxDuration;

@end

NS_ASSUME_NONNULL_END
