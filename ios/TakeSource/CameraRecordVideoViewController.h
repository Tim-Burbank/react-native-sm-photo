
#import "CameraBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, DisruptType) {
    DisruptTypeNone,
    DisruptTypeCancel,
    DisruptTypeAlert,
};

@protocol CameraRecordVideoViewControllerDelegate <NSObject>

- (void)videoURL:( NSURL * _Nullable )url;

@end

@interface CameraRecordVideoViewController : CameraBaseViewController

@property (nonatomic, weak) id<CameraRecordVideoViewControllerDelegate> delegate;

- (void)displayCode:(NSString *)code;

@end

NS_ASSUME_NONNULL_END
