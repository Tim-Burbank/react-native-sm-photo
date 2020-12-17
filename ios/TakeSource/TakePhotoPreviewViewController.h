
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol TakePhotoPreviewViewControllerDelegate <NSObject>

- (void)confirmPhoto:(UIImage *)image;

@end

@interface TakePhotoPreviewViewController : UIViewController

@property (nonatomic, strong) UIImage *image;

@property (nonatomic, weak) id<TakePhotoPreviewViewControllerDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
