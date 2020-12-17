
#import "PhotoModel.h"
#import "UIColor+StringColor.h"

@implementation PhotoModel
static PhotoModel *_instance  = nil;

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    return [PhotoModel shared];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _JpgCompressShortEdge = 828.0;
        _JpgCompressLevel = 0.7;
        _VideoCompressLevel = 50;
        _VideoDuration = 30;
        _StartDevicePostition = AVCaptureDevicePositionFront;
        _SelectGIF = GIFTYPEAll;
        _GifSizeLimit = 10.0*1024.0*1024.0;
        _VideoSizeLimit = 25.0;
        _statusBarLight = YES;
    }
    return self;
}

+ (instancetype)shared {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (_instance == nil) {
            _instance = [[super allocWithZone:NULL] init];
        }
    });
    return _instance;
}

+ (NSString *)exportPresetQuality {
    NSInteger q = [PhotoModel shared].VideoCompressLevel;
    if (q>=0 && q<=25) {
        return AVAssetExportPreset640x480;
    } else if (q>25 && q<=50) {
        return AVAssetExportPresetMediumQuality;
    } else if (q>50 && q<=75) {
        return AVAssetExportPreset960x540;
    } else {
        return AVAssetExportPresetHighestQuality;
    }
}

+ (NSString *)exportPresetQuality:(AVAsset *)asset {
    NSString *exportPreset = [self exportPresetQuality];
    if ([asset isKindOfClass:[AVURLAsset class]]) {
        AVURLAsset *urlAsset = (AVURLAsset *)asset;
        NSMutableArray *set = [[NSMutableArray alloc] init];
        [set addObject:NSURLFileSizeKey];
        NSError *error;
        NSDictionary *resource = [urlAsset.URL resourceValuesForKeys:set error:&error];
        if (error) {
            return exportPreset;
        } else {
            NSInteger fileSize = [resource[@"NSURLFileSizeKey"] integerValue];
            CGFloat size = 0.0;
            if (fileSize) {
                size = fileSize/1000.0/1000.0;
            }
            if (size < [PhotoModel shared].VideoSizeLimit) {  //如果视频小于25MB(默认，也可以根据web传值)，直接上传原视频，不压缩
                exportPreset = AVAssetExportPresetHighestQuality;
                return exportPreset;
            }
        }
    }
    return exportPreset;
}

@end
