
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN


@protocol RecordVideoPreviewViewControllerDelegate;

@interface RecordVideoPreviewViewController : UIViewController

@property (nonatomic, weak) id<RecordVideoPreviewViewControllerDelegate> delegate;

@property (nonatomic, strong) AVPlayer *player;

@property (assign) double duration;

@property (nonatomic, strong) NSURL *videoURL;

@property (nonatomic, strong) PHAsset *assset;

@end

@protocol RecordVideoPreviewViewControllerDelegate <NSObject>

- (void)confirmVideo:(NSURL *)url;

@optional

- (void)confirAsset:(PHAsset *)asset;

- (void)confirmVideo:(NSString *)localPath thumbNailPath:(NSString *)tnp duration:(double)time;

@end

NS_ASSUME_NONNULL_END
