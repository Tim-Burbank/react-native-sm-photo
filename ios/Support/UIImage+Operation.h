
#import <UIKit/UIKit.h>
#import "SXUtil.h"

NS_ASSUME_NONNULL_BEGIN

@interface UIImage (Operation)

- (UIImage *)imageByScalingAndCroppingForSize:(CGSize)targetSize shortEdge:(BOOL)isSE;

- (UIImage *)fixOrientation;

+ (UIImage *)imageBundleNamed:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
