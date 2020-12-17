
#import "PhotoPlugin.h"
#import <Foundation/Foundation.h>
#import "CameraController.h"
#import "AlbumController.h"
#import "Hyphenate.h"
#import "PhotoModel.h"
#import "SXPhotoPickerController.h"

@interface PhotoPlugin ()<CameraControllerDelegate, AlbumControllerDelegate>

@property (nonatomic, strong) UIViewController *vc;
@property (nonatomic, strong) CameraController *cc;
@property (nonatomic, strong) AlbumController *ac;

@property (nonatomic, strong) RCTPromiseResolveBlock resolver;

@end

@implementation PhotoPlugin

static NSBundle *_currentBundle = nil;

+ (NSBundle *)currentBundle {
    if (!_currentBundle) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"PhotoPluginResources" ofType:@"bundle"];
        _currentBundle = [NSBundle bundleWithPath:path];
    }
    return _currentBundle;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (NSString *)moduleName {
    return @"PhotoPlugin";
}

- (NSArray *)supportedEvents {
    return @[@"InProgress", @"PhotoPluginVideo"];
}

NSString *notiName = @"PhotoPlugin";

- (void)startObserving {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(compressorFinish:) name:notiName object:nil];
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:notiName object:nil];
}


- (void)compressorFinish:(NSNotification *)noti {
    [self sendEventWithName:@"PhotoPluginVideo" body:noti.userInfo];
}


RCT_EXPORT_METHOD(changeLanguage:(NSString *)language) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSUserDefaults standardUserDefaults] setValue:language forKey:@"language"];
        [[NSUserDefaults standardUserDefaults] synchronize];
    });
}

RCT_EXPORT_METHOD(takePhoto:(NSInteger)quality isFront:(BOOL)isFront resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        CGFloat q = quality / 100.0;
        if (q > 1.0) {
            q = 1.0;
        }
        [PhotoModel shared].JpgCompressLevel = q;
        if (!isFront) {
            [PhotoModel shared].StartDevicePostition = AVCaptureDevicePositionBack;
        } else {
            [PhotoModel shared].StartDevicePostition = AVCaptureDevicePositionFront;
        }
        
        self.cc = [[CameraController alloc] init];
        self.cc.delegate = self;
        [self.cc presentCustomTakePhotoController:vc];
    });
}

RCT_EXPORT_METHOD(recordVideo:(NSString *)code quality:(NSInteger)quality resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        [PhotoModel shared].VideoCompressLevel = quality;
        self.cc = [[CameraController alloc] init];
        self.cc.delegate = self;
        if (code.length >= 4) {
            [PhotoModel shared].StartDevicePostition = AVCaptureDevicePositionFront;
            [self.cc present:code vc:vc];
        } else {
            [self.cc presentTakePhotoController:vc];
        }
    });
}

RCT_EXPORT_METHOD(recordVideo2:(CGFloat)minDuration maxDuration:(CGFloat)maxDuration quality:(NSInteger)quality isFront:(BOOL)isFront resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        [PhotoModel shared].VideoCompressLevel = quality;
        if (!isFront) {
            [PhotoModel shared].StartDevicePostition = AVCaptureDevicePositionBack;
        } else {
            [PhotoModel shared].StartDevicePostition = AVCaptureDevicePositionFront;
        }
        self.cc = [[CameraController alloc] init];
        self.cc.delegate = self;
        [self.cc present2:minDuration maxDuration:maxDuration vc:vc];
    });
}

RCT_EXPORT_METHOD(selectAlbumPhotoAndVideo:(NSInteger)maxCount width:(CGFloat)width quality:(NSInteger)quality videoQuality:(NSInteger)videoQuality duration:(CGFloat)duration minCount:(NSInteger)minCount resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].SelectGIF = GIFTYPEAll;
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        self.ac.delegate = self;
        [self.ac present:NO mediaType:MediaTypeAll videoDuration:duration maxCount:maxCount minCount:minCount viewController:vc width:width quality:quality videoQuality:videoQuality];
    });
}

RCT_EXPORT_METHOD(selectAlbum:(NSInteger)maxCount width:(CGFloat)width quality:(NSInteger)quality minCount:(NSInteger)minCount resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].SelectGIF = GIFTYPEUnuse;
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        self.ac.delegate = self;
        if (width>0 && quality>0) {
            [self.ac present:NO mediaType:MediaTypePhoto videoDuration:0 maxCount:maxCount minCount:minCount viewController:vc width:width quality:quality videoQuality:50];
        } else {
            [self.ac present:NO mediaType:MediaTypePhoto videoDuration:0 maxCount:maxCount minCount:minCount viewController:vc width:828 quality:70 videoQuality:50];
        }
    });
}

RCT_EXPORT_METHOD(selectAlbumVideo:(CGFloat)duration quality:(NSInteger)quality resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        self.ac.delegate = self;
        [self.ac present:NO mediaType:MediaTypeVideo videoDuration:duration maxCount:1 minCount:0 viewController:vc width:828 quality:70 videoQuality:quality];
    });
}


RCT_EXPORT_METHOD(selectAlbumVideoOption:(NSDictionary *)config resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        self.ac.delegate = self;
        [self.ac present:config width:vc];
    });
}


RCT_EXPORT_METHOD(selectAvatar:(CGFloat)width quality:(NSInteger)quality resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].SelectGIF = GIFTYPEUnuse;
        UIViewController *vc = [[UIViewController alloc] init];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        if (width > 0 && quality > 0) {
            [self.ac present:YES mediaType:MediaTypePhoto videoDuration:0 maxCount:1 minCount:0 viewController:vc width:width quality:quality videoQuality:50];
        } else {
            [self.ac present:YES mediaType:MediaTypePhoto videoDuration:0 maxCount:1 minCount:0 viewController:vc width:828 quality:70 videoQuality:50];
        }
    });
}

RCT_EXPORT_METHOD(setLangSrc:(NSDictionary *)src) {
    [PhotoModel shared].langDic = src;
}

RCT_EXPORT_METHOD(setCss:(NSDictionary *)src) {
    [PhotoModel shared].colorDic = src;
}

RCT_EXPORT_METHOD(setImageSrc:(NSDictionary *)src) {
    [PhotoModel shared].imgDic = src;
}

RCT_EXPORT_METHOD(setStatusBarLight:(BOOL)src) {
    [PhotoModel shared].statusBarLight = src;
}

RCT_EXPORT_METHOD(selectGifAndPic:(NSInteger)maxCount width:(CGFloat)width quality:(NSInteger)quality resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].SelectGIF = GIFTYPEAll;
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        self.ac.delegate = self;
        [self.ac present:NO mediaType:MediaTypePhoto videoDuration:0 maxCount:maxCount minCount:0 viewController:vc width:width quality:quality videoQuality:50];
    });
}

RCT_EXPORT_METHOD(selectGIF:(NSInteger)maxCount resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    self.resolver = resolver;
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].SelectGIF = GIFTYPEUse;
        UIViewController *vc = [Hyphenate getRootViewController];
        self.vc = vc;
        self.ac = [[AlbumController alloc] init];
        self.ac.delegate = self;
        [self.ac present:NO mediaType:MediaTypePhoto videoDuration:0 maxCount:maxCount minCount:0 viewController:vc width:828 quality:70 videoQuality:50];
    });
}

RCT_EXPORT_METHOD(selectAlbumPhotoAndVideoCanAsync:(NSDictionary *)config resolver:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
  self.resolver = resolve;
  dispatch_async(dispatch_get_main_queue(), ^{
    [PhotoModel shared].SelectGIF = GIFTYPEAll;
    UIViewController *vc = [Hyphenate getRootViewController];
    self.vc = vc;
    self.ac = [[AlbumController alloc] init];
    self.ac.delegate = self;
    [self.ac present:config width:vc];
  });
}


RCT_EXPORT_METHOD(gifSizeLimit:(CGFloat)size) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].GifSizeLimit = size * 1024 * 1024;
    });
}

RCT_EXPORT_METHOD(videoSizeLimit:(CGFloat)size) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [PhotoModel shared].VideoDuration = size;
    });
}

- (void)imagePaths:(NSArray *)paths {
    [self.vc dismissViewControllerAnimated:YES completion:nil];
    if (SXPhotoPickerController.isAvatar && SXPhotoPickerController.mediaType != MediaTypePhoto) {
        NSString *s = @"";
        if (paths.count > 0) {
            s = [paths firstObject];
        }
        self.resolver(@{@"image_paths":@[s], @"videos":@[]});
    } else {
        self.resolver(@{@"image_paths":paths, @"videos":@[]});
    }
    self.resolver = nil;
    self.ac = nil;
    self.vc = nil;
}

- (void)videoPath:(NSString *)path thumbnailPath:(NSString *)tbpath duration:(NSInteger)d {
    if (!self.resolver) return ;
    [self.vc dismissViewControllerAnimated:YES completion:nil];
    self.resolver(@{@"image_paths":@[], @"videos":@[path, tbpath, [NSString stringWithFormat:@"%ld", d*1000]]});
    self.resolver = nil;
    self.vc = nil;
    self.ac = nil;
}

- (void)inprogress {
    [self sendEventWithName:@"InProgress" body:@""];
}

- (void)takePhotoPath:(NSString *)path {
    [self.vc dismissViewControllerAnimated:YES completion:nil];
    if (path.length == 0) {
        self.resolver(@{@"image_paths":@[], @"videos":@[]});
    } else {
        self.resolver(@{@"image_paths":@[path], @"videos":@[]});
    }
    self.resolver = nil;
    self.vc = nil;
    self.ac = nil;
}

- (void)takeVideoPat:(NSString *)path thumbnailPath:(NSString *)tnp duration:(NSInteger)time {
    [self.vc dismissViewControllerAnimated:YES completion:nil];
    if (time > 0) {
        self.resolver(@{@"image_paths":@[], @"videos":@[path, tnp, [NSString stringWithFormat:@"%ld", time*1000]]});
    } else {
        self.resolver(@{@"image_paths":@[], @"video":@[]});
    }
    self.resolver = nil;
    self.vc = nil;
    self.cc = nil;
}

RCT_EXPORT_MODULE()

@end
  
