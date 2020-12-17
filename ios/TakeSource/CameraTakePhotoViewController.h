
#import "CameraBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@protocol CameraTakePhotoViewControllerDelegate <NSObject>

- (void)imageURL:( NSURL * _Nullable )url;

@end

@interface CameraTakePhotoViewController : CameraBaseViewController

@property (nonatomic, weak) id<CameraTakePhotoViewControllerDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
