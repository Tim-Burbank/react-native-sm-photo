
#import <UIKit/UIKit.h>
#import "SXPhotoPickerController.h"

NS_ASSUME_NONNULL_BEGIN

@protocol AlbumControllerDelegate <NSObject>

- (void)inprogress;

- (void)imagePaths:(NSArray *)paths;

- (void)videoPath:(NSString *)path thumbnailPath:(NSString *)tbpath duration:(NSInteger)d;

@end


@interface AlbumController : NSObject

@property (nonatomic, weak) id<AlbumControllerDelegate> delegate;

- (void)present:(BOOL)isAvatar mediaType:(MediaType)type videoDuration:(CGFloat)d maxCount:(NSInteger)count minCount:(NSInteger)minCount viewController:(UIViewController *)vc width:(CGFloat)w quality:(NSInteger)q videoQuality:(NSInteger)vq;

- (void)present:(NSDictionary *)config width:(UIViewController *)vc;

- (UIImage *)getAsstThumbail:(PHAsset *)asset size:(CGSize)size;

- (UIImage *)getVideoPreViewImage:(NSURL *)path;

+ (void)showErrorAlert:(UIViewController *)vc error:(NSString *)errorStr;

+ (UIImage *)getVideoPreViewImage:(NSURL *)path;

@end



NS_ASSUME_NONNULL_END
