
#import "AlbumController.h"
#import "PhotoModel.h"
#import "SXUtil.h"
#import "SXPhotoPickerController.h"
#import "DownloadProgress.h"
#import <MobileCoreServices/MobileCoreServices.h>
#import "VideoCompress.h"

@interface AlbumController() <SXPhotoPickerControllerDelegate, UINavigationControllerDelegate>

@property (nonatomic, strong) NSString *vid;

@property (nonatomic, strong) UIViewController *currentVc;

@property (nonatomic, strong) NSNumber *minDuration;

@property (nonatomic, copy) NSDictionary *config;

@end

@implementation AlbumController

- (void)present:(BOOL)isAvatar mediaType:(MediaType)type videoDuration:(CGFloat)d maxCount:(NSInteger)count minCount:(NSInteger)minCount viewController:(UIViewController *)vc width:(CGFloat)w quality:(NSInteger)q videoQuality:(NSInteger)vq {
    CGFloat qual = q / 100.0;
    if (qual > 1.0) {
        qual = 1.0;
    }
    PhotoModel *model = [PhotoModel shared];
    model.JpgCompressShortEdge = w;
    model.VideoDuration = d;
    model.JpgCompressLevel = qual;
    model.VideoCompressLevel = vq;
    _minDuration = @1;
    [self showPhotoPickerWith:vc maxCount:count minCount:minCount avatar:isAvatar mediaType:type];
}

- (void)present:(NSDictionary *)config width:(UIViewController *)vc {
    PhotoModel *model = [PhotoModel shared];
    CGFloat qual = [config[@"picQuality"] floatValue] / 100.0;
    if (qual > 1.0) {
        qual = 1.0;
    }
    _minDuration = config[@"minDuration"];
    if (!_minDuration) {
        _minDuration = @1;
    }
    model.VideoDuration = [config[@"maxDuration"] integerValue];
    model.JpgCompressLevel = qual;
    model.VideoCompressLevel = [config[@"videoQuality"] floatValue];
    _config = config;
    _vid = config[@"vid"];
    NSInteger mixCount = [config[@"minCount"] integerValue] > 0 ? [config[@"minCount"] integerValue] : 0;
    if (config[@"maxCount"]) {
        [self showPhotoPickerWith:vc maxCount:[config[@"maxCount"] integerValue] minCount:mixCount avatar:NO mediaType:MediaTypeAll];
        model.JpgCompressShortEdge = [config[@"width"] floatValue];
    } else {
        [self showPhotoPickerWith:vc maxCount:1 minCount: 0 avatar:NO mediaType:MediaTypeVideo];
        model.JpgCompressShortEdge = 828;
    }
}

- (void)showPhotoPickerWith:(UIViewController *)vc maxCount:(NSInteger)maxCount minCount:(NSInteger)minCount avatar:(BOOL)isAvatar mediaType:(MediaType)type {
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (status == PHAuthorizationStatusNotDetermined) {
                return ;
            }
            if (status == PHAuthorizationStatusDenied || status == PHAuthorizationStatusRestricted) {
                UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"No album privileges" comment:@""] message:[SXUtil SXLocalizedString:@"Allow App to access your album" comment:@""] preferredStyle:UIAlertControllerStyleAlert];
                UIAlertAction *jumpToSetting = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Set" comment:@""] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                    [[UIApplication  sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString] options:nil completionHandler:nil];
                    [alert dismissViewControllerAnimated:YES completion:nil];
                }];
                [alert addAction:jumpToSetting];
                UIAlertAction *cancel = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Cancel" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                    [alert dismissViewControllerAnimated:YES completion:nil];
                }];
                [alert addAction:cancel];
                [vc presentViewController:alert animated:YES completion:nil];
            } else {
                SXPhotoPickerController *picker = [[SXPhotoPickerController alloc] init];
                SXPhotoPickerController.maxSelectedNumber = maxCount;
                SXPhotoPickerController.minSelectedNumber = minCount;
                SXPhotoPickerController.isAvatar = isAvatar;
                SXPhotoPickerController.mediaType = type;
                picker.photoPickerDelegate = self;
                picker.modalPresentationStyle = UIModalPresentationFullScreen;
                [vc presentViewController:picker animated:YES completion:nil];
                _currentVc = picker;
            }
        });
    }];
}

- (UIImage *)getAsstThumbail:(PHAsset *)asset size:(CGSize)size {
    CGFloat retinaScale = [UIScreen mainScreen].scale;
    CGSize retinaSquare = CGSizeMake(size.width * retinaScale, size.height * retinaScale);
    PHImageManager *manager = [PHImageManager defaultManager];
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    __block UIImage *thumbnail = [[UIImage alloc] init];
    [options setSynchronous:YES];
    options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
    options.resizeMode = PHImageRequestOptionsResizeModeExact;
    [manager requestImageForAsset:asset targetSize:retinaSquare contentMode:PHImageContentModeAspectFill options:options resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
        if (result) {
            thumbnail = result;
        }
    }];
    return thumbnail;
}

+ (UIImage *)getVideoPreViewImage:(NSURL *)path {
    AVURLAsset *asset = [AVURLAsset assetWithURL:path];
    AVAssetImageGenerator *assetGen = [AVAssetImageGenerator assetImageGeneratorWithAsset:asset];
    assetGen.appliesPreferredTrackTransform = YES;
    CMTime time = CMTimeMakeWithSeconds(1.0, 600);
    CMTime actualTime = kCMTimeZero;
    NSError *error = nil;
    CGImageRef image = [assetGen copyCGImageAtTime:time actualTime:&actualTime error:&error];
    if (error) {
        return [[UIImage alloc] init];
    } else {
        UIImage *videoImage = [UIImage imageWithCGImage:image];
        return videoImage;
    }
}

+ (NSString *)pathWithSaveImage:(UIImage *)image {
    NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    NSString *imagePath = [NSString stringWithFormat:@"%@/%@", cachePath, [NSUUID UUID].UUIDString];
    NSURL *url = [NSURL fileURLWithPath:imagePath];
    NSError *error = nil;
    NSData *data = [SXUtil compressImage:image shortEdge:[PhotoModel shared].JpgCompressShortEdge level:1.0];
    [data writeToURL:url options:0 error:&error];
    if (error) {
        return error.localizedDescription;
    } else {
        return imagePath;
    }
}

- (void)didSendLocalVideo:(SXPhotoPickerController *)photoPicker localPath:(NSString *)path thumbnailPath:(NSString *)tbpath duration:(NSInteger)time {
    [self.delegate videoPath:path thumbnailPath:tbpath duration:(int)time];
}

- (void)compressVideoForVideo:(PHAsset *)a options:(nullable PHVideoRequestOptions *)options asset:(AVAsset *)asset photoPicker:(SXPhotoPickerController *)photoPicker {
    AVURLAsset *avurlAsset = (AVURLAsset *)asset;
    //取出asset中的视频文件
    AVAssetTrack *videoTrack = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
    //压缩前原视频宽高
    NSInteger videoWidth = videoTrack.naturalSize.width;
    NSInteger videoHeight = videoTrack.naturalSize.height;
   // NSLog(@"\noriginalVideo666 ,videoWidth = %ld, videoHeight = %ld", videoWidth, videoHeight );
    NSNumber *bitRate = @(3000 * 1024);
    NSNumber *vw = @(960);
    NSNumber *vh = @(544);
    NSInteger minWidth = 640;
    CGFloat WHrate = (CGFloat)videoWidth / videoHeight;
    BOOL isBiggherThan100 = NO;
    if (_config[@"vq_bitRate"]) {
        bitRate = _config[@"vq_bitRate"];
        vw = _config[@"vq_width"];
        vh = _config[@"vq_height"];
    } else {
        NSMutableArray *set = [[NSMutableArray alloc] init];
        [set addObject:NSURLFileSizeKey];
        NSError *error;
        NSDictionary *resource = [avurlAsset.URL resourceValuesForKeys:set error:&error];
        if (!error) {
            NSInteger fileSize = [resource[@"NSURLFileSizeKey"] integerValue];
            CGFloat size = 0.0;
            CGFloat realSize = 0;
            if (fileSize) {
                size = fileSize/1000.0/1000.0;
            }
            if (a.duration > [PhotoModel shared].VideoDuration) {
                realSize = size * [PhotoModel shared].VideoDuration / a.duration;
            } else {
                realSize = size;
            }
            if (realSize > 0 && realSize <= 25) {
                bitRate = @(6000 * 1024);
                CGFloat _initWidtha = 960.0 / WHrate;
                if(videoWidth < 960) {
                    vw = @(videoWidth);
                    vh = @(videoHeight);
                } else {
                    NSInteger _num = (NSInteger) (floor(_initWidtha));
                    if(_num % 2 != 0){
                        _num = _num + 1;
                    }
                   // NSLog(@"\nrate666 ,_num = %ld, _initWidtha = %f", _num, WHrate);
                    vh = @(_num);
                };
            } else if (realSize > 25 && realSize <= 50) {
               bitRate = @(3000 * 1024);
               vw = @(960);
               CGFloat _initWidtha = 960.0 / WHrate;
               NSInteger _num = (NSInteger) (floor(_initWidtha));
               if(_num % 2 != 0){
                   _num = _num + 1;
               }
              //  NSLog(@"\nrate777 ,_num = %ld, _initWidtha = %f", _num, _initWidtha);
               vh = @(_num);
             }else if (realSize > 50 && realSize <= 100) {
              bitRate = @(1500 * 1024);
              vw = @(640);
              CGFloat _initWidtha = 640.0 / WHrate;
              NSInteger _num = (NSInteger) (floor(_initWidtha));
              if(_num % 2 != 0){
                  _num = _num + 1;
              }
             //  NSLog(@"\nrate777 ,_num = %ld, _initWidtha = %f", _num, _initWidtha);
              vh = @(_num);
            } else if (realSize > 100) {
                isBiggherThan100 = YES;
            }
        }
    }
    if(videoWidth < minWidth){
        vw = @(videoWidth);
        vh = @(videoHeight);
    }
    if (videoTrack.estimatedDataRate < bitRate.floatValue) {
        bitRate = @(videoTrack.estimatedDataRate);
    }
    if (_vid) {
        if ([_config[@"customCompress"] integerValue] == 0) {
            [self exportVideoForVideo:a options:options asset:asset photoPicker:photoPicker isAsync:YES];
        } else {
            if (isBiggherThan100) {
                [self exportVideoForVideo:a options:options asset:asset photoPicker:photoPicker isAsync:YES];
            } else {
                [self compressVideoWithVideoUrl:(AVURLAsset *)asset asset: a bieRate:bitRate videoWidth:vw videoHeight:vh isAsync:YES photoPicker:photoPicker];
            }
        }
    } else {
        if ([_config[@"customCompress"] integerValue] == 0) {
            [self exportVideoForVideo:a options:options asset:asset photoPicker:photoPicker];
        } else {
            [self compressVideoWithVideoUrl:(AVURLAsset *)asset asset: a bieRate:bitRate videoWidth:vw videoHeight:vh isAsync:NO photoPicker:photoPicker];
        }
    }
}

- (void)compressVideoWithVideoUrl:(AVURLAsset *)orgAsset asset:(PHAsset *)asset bieRate:(NSNumber *)rate videoWidth:(NSNumber *)vw videoHeight:(NSNumber *)vh isAsync:(BOOL)needAsync photoPicker:(SXPhotoPickerController *)photoPicker {
    dispatch_async(dispatch_get_main_queue(), ^{
        BOOL haveExc = NO;
        @try {
            [VideoCompress compressVideoWithVideoUrl:orgAsset.URL
                                        withBiteRate:rate
                                       withFrameRate:@(30)
                                      withVideoWidth:vw
                                     withVideoHeight:vh
                                    compressComplete:^(id responseObjc) {
                                        NSString *filePathStr = [responseObjc objectForKey:@"urlStr"];
                                        AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:filePathStr]];
                                        AVAssetTrack *videoTrack = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
                                        //视频大小 MB
                                        unsigned long long fileSize = [[NSFileManager defaultManager] attributesOfItemAtPath:filePathStr error:nil].fileSize;
                                        float fileSizeMB = fileSize / (1024.0*1024.0);
                                        //视频宽高
                                        NSInteger videoWidth = videoTrack.naturalSize.width;
                                        NSInteger videoHeight = videoTrack.naturalSize.height;
                                        //比特率
                                        NSInteger kbps = videoTrack.estimatedDataRate / 1024;
                                        //帧率
                                        NSInteger frameRate = [videoTrack nominalFrameRate];
                                        NSLog(@"\nfileSize after compress = %.2f MB,\n videoWidth = %ld,\n videoHeight = %ld,\n video bitRate = %ld\n, video frameRate = %ld", fileSizeMB, videoWidth, videoHeight, kbps, frameRate);
                                        NSLog(@"===filePathStr%@===", filePathStr);
                                        NSString *imagePath = @"";
                                        NSInteger duration = 0;
                                        if (!needAsync) {
                                            NSURL *vurl = [NSURL fileURLWithPath:filePathStr];
                                            UIImage *image = [AlbumController getVideoPreViewImage:vurl];
                                            imagePath = [AlbumController pathWithSaveImage:image];
                                            NSURL *videoURL = [NSURL fileURLWithPath:filePathStr];
                                            AVURLAsset *assets = [AVURLAsset assetWithURL:videoURL];
                                            float current = CMTimeGetSeconds(assets.duration);
                                            if (current > [PhotoModel shared].VideoDuration) {
                                                duration = (int)[PhotoModel shared].VideoDuration;
                                            } else {
                                                duration = (int)current;
                                            }
                                        }
                                        if (_vid) {
                                            [[NSNotificationCenter defaultCenter] postNotificationName:@"PhotoPlugin" object:nil userInfo:@{
                                                                                                                                            @"vid": _vid,
                                                                                                                                            @"videoPath": filePathStr,
                                                                                                                                            @"error":@""
                                                                                                                                            }];
                                        } else {
                                            dispatch_async(dispatch_get_main_queue(), ^{
                                                [photoPicker dismissViewControllerAnimated:YES completion:nil];
                                                [self.delegate videoPath:filePathStr thumbnailPath:imagePath duration:duration];
                                            });
                                        }
                                    }];
        } @catch (NSException *exception) {
            if (exception) {
                haveExc = YES;
            }
        } @finally {
            if (haveExc) {
                PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
                [options setNetworkAccessAllowed:YES];
                options.deliveryMode = PHVideoRequestOptionsDeliveryModeHighQualityFormat;
                [self exportVideoForVideo:asset options:options asset:orgAsset photoPicker:photoPicker];
            }
        }
    });
    if (_vid) {
        UIImage *image = [AlbumController getVideoPreViewImage:orgAsset.URL];
        NSString *imagePath = [AlbumController pathWithSaveImage:image];
        CGFloat duration = 0.0;
        CGFloat current = CMTimeGetSeconds(orgAsset.duration);
        if (current > [PhotoModel shared].VideoDuration) {
            duration = (int)[PhotoModel shared].VideoDuration;
        } else {
            duration = (int)current;
        }
        [photoPicker dismissViewControllerAnimated:YES completion:nil];
        [self.delegate videoPath:@"" thumbnailPath:imagePath duration:duration];
    }
}

- (void)compressVideoWithVideoUrl:(AVURLAsset *)orgAsset bieRate:(NSNumber *)rate videoWidth:(NSNumber *)vw videoHeight:(NSNumber *)vh isAsync:(BOOL)needAsync {
    dispatch_async(dispatch_get_main_queue(), ^{
        [VideoCompress compressVideoWithVideoUrl:orgAsset.URL
                                    withBiteRate:rate
                                   withFrameRate:@(30)
                                  withVideoWidth:vw
                                 withVideoHeight:vh
                                compressComplete:^(id responseObjc) {
                                    NSString *filePathStr = [responseObjc objectForKey:@"urlStr"];
                                    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:filePathStr]];
                                    AVAssetTrack *videoTrack = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
                                    //视频大小 MB
                                    unsigned long long fileSize = [[NSFileManager defaultManager] attributesOfItemAtPath:filePathStr error:nil].fileSize;
                                    float fileSizeMB = fileSize / (1024.0*1024.0);
                                    //视频宽高
                                    NSInteger videoWidth = videoTrack.naturalSize.width;
                                    NSInteger videoHeight = videoTrack.naturalSize.height;
                                    //比特率
                                    NSInteger kbps = videoTrack.estimatedDataRate / 1024;
                                    //帧率
                                    NSInteger frameRate = [videoTrack nominalFrameRate];
                                    NSLog(@"\nfileSize after compress = %.2f MB,\n videoWidth = %ld,\n videoHeight = %ld,\n video bitRate = %ld\n, video frameRate = %ld", fileSizeMB, videoWidth, videoHeight, kbps, frameRate);
                                    NSLog(@"===filePathStr%@===", filePathStr);
                                    NSString *imagePath = @"";
                                    NSInteger duration = 0;
                                    if (!needAsync) {
                                        NSURL *vurl = [NSURL fileURLWithPath:filePathStr];
                                        UIImage *image = [AlbumController getVideoPreViewImage:vurl];
                                        imagePath = [AlbumController pathWithSaveImage:image];
                                        NSURL *videoURL = [NSURL fileURLWithPath:filePathStr];
                                        AVURLAsset *assets = [AVURLAsset assetWithURL:videoURL];
                                        float current = CMTimeGetSeconds(assets.duration);
                                        if (current > [PhotoModel shared].VideoDuration) {
                                            duration = (int)[PhotoModel shared].VideoDuration;
                                        } else {
                                            duration = (int)current;
                                        }
                                    }
                                    if (_vid) {
                                        [[NSNotificationCenter defaultCenter] postNotificationName:@"PhotoPlugin" object:nil userInfo:@{
                                                                                                                                        @"vid": _vid,
                                                                                                                                        @"videoPath": filePathStr,
                                                                                                                                        @"error":@""
                                                                                                                                        }];
                                    } else {
                                        dispatch_async(dispatch_get_main_queue(), ^{
                                            [self.delegate videoPath:filePathStr thumbnailPath:imagePath duration:duration];
                                        });
                                    }
                                }];
    });
    if (_vid) {
        UIImage *image = [AlbumController getVideoPreViewImage:orgAsset.URL];
        NSString *imagePath = [AlbumController pathWithSaveImage:image];
        CGFloat duration = 0.0;
        CGFloat current = CMTimeGetSeconds(orgAsset.duration);
        if (current > [PhotoModel shared].VideoDuration) {
            duration = (int)[PhotoModel shared].VideoDuration;
        } else {
            duration = (int)current;
        }
        [self.delegate videoPath:@"" thumbnailPath:imagePath duration:duration];
    }
}

- (void)exportVideoForVideo:(PHAsset *)a options:(nullable PHVideoRequestOptions *)options asset:(AVAsset *)asset photoPicker:(SXPhotoPickerController *)photoPicker {
    NSString *exportPreset = [PhotoModel exportPresetQuality:asset];
    [[PHImageManager defaultManager] requestExportSessionForVideo:a options:options exportPreset:exportPreset resultHandler:^(AVAssetExportSession * _Nullable exportSession, NSDictionary * _Nullable info) {
        dispatch_async(dispatch_get_main_queue(), ^{
            CircleProgressAlert *progressing = [[CircleProgressAlert alloc] initWithFrame:UIScreen.mainScreen.bounds];
            [photoPicker.view addSubview:progressing];
            if (exportSession) {
                exportSession.shouldOptimizeForNetworkUse  = YES;
                NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
                __block NSString *path = [NSString stringWithFormat:@"%@/%@.mp4", cachePath, [NSUUID UUID].UUIDString];
                exportSession.outputURL = [NSURL fileURLWithPath:path];
                exportSession.outputFileType = AVFileTypeMPEG4;
                if (a.duration > [PhotoModel shared].VideoDuration) {
                    CMTime time = CMTimeMakeWithSeconds(0.0, 30);
                    CMTime duration = CMTimeMakeWithSeconds([PhotoModel shared].VideoDuration, 30);
                    CMTimeRange range = CMTimeRangeMake(time, duration);
                    exportSession.timeRange = range;
                }
                [exportSession exportAsynchronouslyWithCompletionHandler:^{
                    dispatch_async(dispatch_get_main_queue(), ^{
                        switch (exportSession.status) {
                            case AVAssetExportSessionStatusFailed:
                                [self.delegate imagePaths:@[]];
                                break;
                            case AVAssetExportSessionStatusCancelled:
                                [self.delegate imagePaths:@[]];
                                break;
                            case AVAssetExportSessionStatusCompleted:
                            {
                                NSURL *vurl = [NSURL fileURLWithPath:path];
                                UIImage *image = [AlbumController getVideoPreViewImage:vurl];
                                NSString *imagePath = [NSString stringWithFormat:@"%@/%@", cachePath, [NSUUID UUID].UUIDString];
                                NSURL *url = [NSURL fileURLWithPath:imagePath];
                                NSError *error = nil;
                                NSData *data = [SXUtil compressImage:image shortEdge:[PhotoModel shared].JpgCompressShortEdge level:1.0];
                                [data writeToURL:url options:0 error:&error];
                                if (error) {
                                    NSLog(@"%@", error);
                                }
                                NSURL *videoURL = [NSURL fileURLWithPath:path];
                                AVURLAsset *asset = [AVURLAsset assetWithURL:videoURL];
                                float current = CMTimeGetSeconds(asset.duration);
                                int duration = 0;
                                if (current > [PhotoModel shared].VideoDuration) {
                                    duration = (int)[PhotoModel shared].VideoDuration;
                                } else {
                                    duration = (int)current;
                                }
                                if (_vid) {
                                    [[NSNotificationCenter defaultCenter] postNotificationName:@"PhotoPlugin" object:nil userInfo:@{
                                                                                                                                    @"vid": _vid,
                                                                                                                                    @"videoPath": path,
                                                                                                                                    @"error":@""
                                                                                                                                    }];
                                } else {
                                    [self.delegate videoPath:path thumbnailPath:imagePath duration:duration];
                                }
                            }
                                break;

                            default:
                                break;
                        }
                    });
                }];
            }
        });
    }];
}

- (void)exportVideoForVideo:(PHAsset *)a options:(nullable PHVideoRequestOptions *)options asset:(AVAsset *)asset photoPicker:(SXPhotoPickerController *)photoPicker isAsync:(BOOL)needAsync {
    AVURLAsset *originAsset = (AVURLAsset *)asset;
    if (needAsync) {
        UIImage *image = [AlbumController getVideoPreViewImage:originAsset.URL];
        NSString *imagePath = [AlbumController pathWithSaveImage:image];
        CGFloat duration = 0.0;
        CGFloat current = CMTimeGetSeconds(originAsset.duration);
        if (current > [PhotoModel shared].VideoDuration) {
            duration = (int)[PhotoModel shared].VideoDuration;
        } else {
            duration = (int)current;
        }
        [self.delegate videoPath:@"" thumbnailPath:imagePath duration:duration];
    }
    [self exportVideoForVideo:a options:options asset:asset photoPicker:photoPicker];
}

- (void)showVideoDurationMinLimitAlert {
    NSString *message = [NSString stringWithFormat:@"请您选择不少于%@秒的视频", _minDuration];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"alertTitle" comment:@""] message:message preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"OK" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
    }]];
    [_currentVc presentViewController:alert animated:YES completion:nil];
}

- (int)getSecond:(double)value {
    double dd = round(value);
    int num1 = (int)(dd * 1000);
    return (int)(num1/1000);
}

- (void)photoPickerController:(SXPhotoPickerController *)photoPicker sendImages:(NSArray<PHAsset *> *)sendImages isFullImage:(BOOL)isFull {
    PHAsset *a = [sendImages firstObject];
    if (_minDuration) {
        if (a && a.mediaType == PHAssetMediaTypeVideo && [self getSecond:a.duration] < [_minDuration integerValue]) {
            [self showVideoDurationMinLimitAlert];
            return ;
        }
    }
    DownloadAlert *progressView = [[DownloadAlert alloc] initWithFrame: UIScreen.mainScreen.bounds];
    progressView.hidden = YES;
    [photoPicker.view addSubview:progressView];
    if (a && [self getSecond:a.duration] >= 1) {
        [self.delegate inprogress];
        if (photoPicker) {
            progressView.title = [SXUtil SXLocalizedString:@"iCloudVideo" comment:@""];
            progressView.subTitle = @"";
            PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
            [options setNetworkAccessAllowed:YES];
            options.deliveryMode = PHVideoRequestOptionsDeliveryModeHighQualityFormat;
            options.progressHandler = ^(double progress, NSError * _Nullable error, BOOL * _Nonnull stop, NSDictionary * _Nullable info) {
                if (info) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        progressView.hidden = NO;
                        NSNumber *key = info[PHImageResultRequestIDKey];
                        progressView.progress = progress;
                        if (error) {
                            [[PHImageManager defaultManager] cancelImageRequest:(int)key.integerValue];
                            [AlbumController showErrorAlert:photoPicker error:[SXUtil SXLocalizedString:@"NetworkNotWork" comment:@""]];
                        }
                    });
                }
            };

            [[PHImageManager defaultManager] requestAVAssetForVideo:a options:options resultHandler:^(AVAsset * _Nullable asset, AVAudioMix * _Nullable audioMix, NSDictionary * _Nullable info) {
                if (asset) {
                    if (_config) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [progressView removeFromSuperview];
                            CircleProgressAlert *progressing = [[CircleProgressAlert alloc] initWithFrame:UIScreen.mainScreen.bounds];
                            [photoPicker.view addSubview:progressing];
                            [self compressVideoForVideo:a options:options asset:asset photoPicker:photoPicker];
                        });
                    } else {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [progressView removeFromSuperview];
                            [self exportVideoForVideo:a options:options asset:asset photoPicker:photoPicker];
                        });
                    }
                }
            }];
        }
    } else {
        if (sendImages && photoPicker) {
            NSMutableArray *paths = [self repeatObject:@"" count:sendImages.count];
            __block NSMutableDictionary *downloadProgress = [[NSMutableDictionary alloc] init];
            PHImageManager *imageManager = [PHImageManager defaultManager];
            NSString *cache = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
            double totalProgress = (double)sendImages.count;
            progressView.subTitle = [NSString stringWithFormat:@"%@ %lu%lu", [SXUtil SXLocalizedString:@"Download" comment:@""], (unsigned long)sendImages.count, (unsigned long)sendImages.count];
            CircleProgressAlert *progressing = [[CircleProgressAlert alloc] initWithFrame:UIScreen.mainScreen.bounds];
            progressing.title = [SXUtil SXLocalizedString:@"Image processing" comment:@""];
            [photoPicker.view addSubview:progressing];
            for (NSInteger i = 0; i < sendImages.count; i++) {
                PHAsset *photo = sendImages[i];
                __block NSString *path = [NSString stringWithFormat:@"%@/%@", cache, [NSUUID UUID].UUIDString];
                NSURL *url = [NSURL fileURLWithPath:path];
                dispatch_async(dispatch_get_main_queue(), ^{
                    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
                    [options setNetworkAccessAllowed:YES];
                    options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
                    if ([photo valueForKey:@"filename"] && [[photo valueForKey:@"filename"] hasSuffix:@"GIF"]) {
                        // 尝试修复部分动图发送之后不会动的情况
                        options.version = PHImageRequestOptionsVersionOriginal;
                    }
                    options.progressHandler = ^(double progress, NSError * _Nullable error, BOOL * _Nonnull stop, NSDictionary * _Nullable info) {
                        if (info) {
                            dispatch_async(dispatch_get_main_queue(), ^{
                                [progressing removeFromSuperview];
                                progressView.hidden = NO;
                            });
                            NSNumber *key = info[PHImageResultRequestIDKey];
                            downloadProgress[key] = @(progress);
                            double total = 0.0;
                            for (NSInteger i = 0; i < downloadProgress.allValues.count; i++) {
                                total += [downloadProgress.allValues[i] doubleValue];
                            }
                            NSLog(@"key: (%@) progress: %lf  total: %lf", key, progress, total);
                            if (progressView) {
                                progressView.progress = total / totalProgress;
                                progressView.subTitle = [NSString stringWithFormat:@"%@ %d/%lu", [SXUtil SXLocalizedString:@"Download" comment:@""], (int)total, (unsigned long)sendImages.count];
                            }
                            if (error) {
                                NSLog(@"Key: %@, error: %@", key, error);
                                [imageManager cancelImageRequest:key.intValue];
                                dispatch_async(dispatch_get_main_queue(), ^{
                                    [progressView removeFromSuperview];
                                    [AlbumController showErrorAlert:photoPicker error:[SXUtil SXLocalizedString:@"NetworkNotWork" comment:@""]];
                                });
                            }
                        }
                    };

                    PHImageRequestID requestId = [imageManager requestImageDataForAsset:photo options:options resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
                        if (imageData && info) {
                            UIImage *img = [[UIImage alloc] initWithData: imageData];
                            if (img) {
                                NSData *data = imageData;
                                NSNumber *key = info[PHImageResultRequestIDKey];
                                downloadProgress[key] = @(1.0);
                                if ([dataUTI isEqualToString:(NSString *)kUTTypeGIF]) {
                                    path = [NSString stringWithFormat:@"%@-gif", path];
                                } else {
                                    data = [SXUtil compressImage:img shortEdge:[PhotoModel shared].JpgCompressShortEdge level:[PhotoModel shared].JpgCompressLevel];
                                }
                                NSError *error;
                                [data writeToURL:url options:0 error:&error];
                                if (!error) {
                                    paths[i] = path;
                                    NSLog(@"$$$%ld %@", (long)i, path);
                                    if (![paths containsObject:@""]) {
                                        dispatch_async(dispatch_get_main_queue(), ^{
                                            [progressing removeFromSuperview];
                                            [photoPicker dismissViewControllerAnimated:YES completion:^{
                                                [self.delegate imagePaths:paths];
                                            }];
                                        });
                                    }
                                } else {
                                    NSLog(@"---- error : %@", error);
                                    dispatch_async(dispatch_get_main_queue(), ^{
                                        [progressView removeFromSuperview];
                                        [AlbumController showErrorAlert:photoPicker error:error.localizedDescription];
                                    });
                                }
                            }
                        } else {
                            dispatch_async(dispatch_get_main_queue(), ^{
                                [progressing removeFromSuperview];
                                [progressView removeFromSuperview];
                                NSString *str = [SXUtil SXLocalizedString:@"Image Deleted" comment:@""];
                                UIAlertController *alert = [UIAlertController alertControllerWithTitle:str message:@"" preferredStyle:UIAlertControllerStyleAlert];
                                [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"OK" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                                }]];
                                [photoPicker presentViewController:alert animated:YES completion:nil];
                            });
                        }
                    }];
                    downloadProgress[@(requestId)] = @(0.0);
                });
            }
        }
    }
}

- (NSMutableArray *)repeatObject:(id)obj count:(NSInteger)count {
    NSMutableArray *temp = [[NSMutableArray alloc] init];
    for (NSInteger i = 0; i < count; i++) {
        [temp addObject:obj];
    }
    return temp;
}

+ (void)showErrorAlert:(UIViewController *)vc error:(NSString *)errorStr {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:errorStr message:@"" preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"OK" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [vc dismissViewControllerAnimated:NO completion:nil];
    }]];
    [vc presentViewController:alert animated:YES completion:nil];
}

- (void)photoPickerDidCancel:(SXPhotoPickerController *)photoPicker {
    [photoPicker dismissViewControllerAnimated:YES completion:nil];
    [self.delegate imagePaths:@[]];
}

@end
