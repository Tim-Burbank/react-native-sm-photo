
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, GIFTYPE) {
    GIFTYPEUse = 1,
    GIFTYPEUnuse = 2,
    GIFTYPEAll = 3,
};

@interface PhotoModel : NSObject

+ (instancetype)shared;

@property (nonatomic, assign) CGFloat JpgCompressShortEdge;

@property (nonatomic, assign) CGFloat JpgCompressLevel;

@property (nonatomic, assign) CGFloat VideoDuration;

@property (nonatomic, assign) BOOL statusBarLight;

@property (nonatomic, copy) NSDictionary *langDic;

@property (nonatomic, copy) NSDictionary *imgDic;

@property (nonatomic, copy) NSDictionary *colorDic;

@property (nonatomic, assign) NSInteger VideoCompressLevel;

@property (nonatomic, assign) AVCaptureDevicePosition StartDevicePostition;

@property (nonatomic, assign) GIFTYPE SelectGIF;

@property (nonatomic, assign) CGFloat GifSizeLimit;

@property (nonatomic, assign) CGFloat VideoSizeLimit;

+ (NSString *)exportPresetQuality;

+ (NSString *)exportPresetQuality:(AVAsset *)asset;

@end

NS_ASSUME_NONNULL_END
