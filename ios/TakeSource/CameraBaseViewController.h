
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "TakePhotoPreviewViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, OutputType) {
    OutputTypeVideo,
    OutputTypeImage
};

@interface CameraBaseViewController : UIViewController <AVCaptureFileOutputRecordingDelegate, TakePhotoPreviewViewControllerDelegate>

@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;

@property (nonatomic, assign) OutputType outputType;

@property (nonatomic, assign) UIStatusBarStyle startBarStyle;

- (void)startRecording:(NSTimeInterval)maxDuration;

- (void)stopRecording;

- (void)switchCamera;

- (void)takePhoto;

@end

NS_ASSUME_NONNULL_END
