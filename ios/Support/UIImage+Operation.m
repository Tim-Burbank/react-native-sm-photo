
#import "UIImage+Operation.h"
#import <ImageIO/ImageIO.h>
#import "PhotoModel.h"
#import "PhotoPlugin.h"


@implementation UIImage (Operation)

+ (UIImage *)imageBundleNamed:(NSString *)name {
    return [SXUtil bunldeName:name];
}

- (UIImage *)imageByScalingAndCroppingForSize:(CGSize)targetSize shortEdge:(BOOL)isSE {
    UIImage *sourceImage = self;
    UIImage *newImage = nil;
    CGSize imageSize = sourceImage.size;
    CGFloat width = imageSize.width;
    CGFloat height = imageSize.height;
    CGFloat targetWidth = targetSize.width;
    CGFloat targetHeight = targetSize.height;
    CGFloat scaleFactor = 0.0;
    CGFloat scaleWidth = targetWidth;
    CGFloat scaleHeight = targetHeight;
    CGPoint thumbnailPoint = CGPointZero;
    BOOL shortEdge = YES;
    if (!isSE) {
        shortEdge = NO;
    }
    if (!CGSizeEqualToSize(imageSize, targetSize)) {
        CGFloat widthFactor = targetWidth / width;
        CGFloat heightFactor = targetHeight / height;
        if (widthFactor > heightFactor) {
            scaleFactor = shortEdge ? widthFactor : heightFactor;
        } else {
            scaleFactor = shortEdge ? heightFactor : widthFactor;
        }
        scaleWidth = width * scaleFactor;
        scaleHeight = height * scaleFactor;
        if (widthFactor > heightFactor) {
            if (shortEdge) {
                thumbnailPoint.y = (targetHeight - scaleHeight) * 0.5;
            } else {
                thumbnailPoint.x = (targetWidth - scaleWidth) * 0.5;
            }
        } else {
            if (widthFactor < heightFactor) {
                if (shortEdge) {
                    thumbnailPoint.x = (targetWidth - scaleWidth) * 0.5;
                } else {
                    thumbnailPoint.y = (targetHeight - scaleHeight) * 0.5;
                }
            }
        }
    }
    UIGraphicsBeginImageContext(targetSize);
    
    CGRect thumbnailRect = CGRectZero;
    thumbnailRect.origin = thumbnailPoint;
    thumbnailRect.size.width = scaleWidth;
    thumbnailRect.size.height = scaleHeight;
    
    [sourceImage drawInRect:thumbnailRect];
    
    newImage = UIGraphicsGetImageFromCurrentImageContext();
    if (!newImage) {
        NSLog(@"could not scale image");
    }
    UIGraphicsEndImageContext();
    return newImage;
}

- (UIImage *)fixOrientation {
    if (self.imageOrientation == UIImageOrientationUp) {
        return self;
    }
    if (!self.CGImage) {
        return self;
    }
    
    CGAffineTransform transform = CGAffineTransformIdentity;
    switch (self.imageOrientation) {
        case UIImageOrientationDown:
        case UIImageOrientationDownMirrored:
            {
                transform = CGAffineTransformTranslate(transform, self.size.width, self.size.height);
                transform = CGAffineTransformRotate(transform, M_PI);
            }
            break;
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
        {
            transform = CGAffineTransformTranslate(transform, self.size.width, 0);
            transform = CGAffineTransformRotate(transform, M_PI/2);
        }
            break;
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
        {
            transform = CGAffineTransformTranslate(transform, 0, self.size.height);
            transform = CGAffineTransformRotate(transform, -M_PI/2);
        }
            break;
        default:
            break;
    }
    switch (self.imageOrientation) {
        case UIImageOrientationUpMirrored:
        case UIImageOrientationDownMirrored:
        {
            transform = CGAffineTransformTranslate(transform, self.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, -1);
        }
            break;
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRightMirrored:
        {
            transform = CGAffineTransformTranslate(transform, self.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, -1);
        }
            break;
        default:
            break;
    }
//    CGBitmapContextCreateWithData(nil, self.size.width, self.size.height, CGImageGetBitsPerComponent(self.CGImage), 0, CGImageGetColorSpace(self.CGImage), CGImageGetBitmapInfo(self.CGImage), CGBitmapContextReleaseDataCallback  _Nullable releaseCallback, <#void * _Nullable releaseInfo#>)
    CGContextRef context = CGBitmapContextCreate(nil, self.size.width, self.size.height, CGImageGetBitsPerComponent(self.CGImage), 0, CGImageGetColorSpace(self.CGImage), CGImageGetBitmapInfo(self.CGImage));
    if (context) {
        switch (self.imageOrientation) {
            case UIImageOrientationLeft:
            case UIImageOrientationLeftMirrored:
            case UIImageOrientationRight:
            case UIImageOrientationRightMirrored:
                CGContextDrawImage(context, CGRectMake(0, 0, self.size.width, self.size.height), self.CGImage);
                break;
                
            default:
                CGContextDrawImage(context, CGRectMake(0, 0, self.size.width, self.size.height), self.CGImage);
                break;
        }
       CGImageRef cgImage =  CGBitmapContextCreateImage(context);
        if (cgImage) {
            return [UIImage imageWithCGImage:cgImage];
        } else {
            return self;
        }
    } else {
        return self;
    }
}

@end
