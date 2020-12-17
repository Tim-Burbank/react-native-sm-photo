
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SXTapDetectingImageViewDelegate;

@interface SXTapDetectingImageView : UIImageView

@property (nonatomic, weak) id<SXTapDetectingImageViewDelegate> tapDelegate;

@end

@protocol SXTapDetectingImageViewDelegate <NSObject>

@optional

- (void)imageView:(SXTapDetectingImageView *)imageView singleTapDetected:(UITouch *)touch;

- (void)imageView:(SXTapDetectingImageView *)imageView doubleTapDetected:(UITouch *)touch;

- (void)imageView:(SXTapDetectingImageView *)imageView tripleTapDetected:(UITouch *)touch;

@end

NS_ASSUME_NONNULL_END
