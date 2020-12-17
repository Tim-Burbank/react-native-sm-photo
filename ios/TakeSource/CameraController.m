
#import "CameraController.h"
#import "SXUtil.h"
#import "CameraTakePhotoViewController.h"
#import "CameraRecordVideoViewController.h"
#import "CameraRecordVideo2ViewController.h"
#import <Photos/Photos.h>
#import "PhotoModel.h"
#import "AlbumController.h"
#import "UIImage+Operation.h"

@interface CameraController ()<CameraTakePhotoViewControllerDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate, CameraRecordVideoViewControllerDelegate, CameraRecordVideo2ViewControllerDelegate>

@property (nonatomic, strong) UIViewController *vc;

@property (nonatomic, strong) UIImagePickerController *cameraVC;


@end

@implementation CameraController

- (void)requeseCameraPermission:(UIViewController *)vc handler:(void(^)(UIViewController *vc, CameraController *w))result{
    [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
        dispatch_async(dispatch_get_main_queue(), ^{
            AVAuthorizationStatus cameraPermission = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
            if (cameraPermission != AVAuthorizationStatusNotDetermined) {
                if (cameraPermission == AVAuthorizationStatusDenied || cameraPermission == AVAuthorizationStatusRestricted) {
                    [self noCameraPrivileges:vc];
                } else {
                    [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            result(vc, self);
                        });
                    }];
                }
            }
        });
    }];
}

- (void)presentCustomTakePhotoController:(UIViewController *)vc {
    [self requeseCameraPermission:vc handler:^(UIViewController *vc, CameraController *w) {
        CameraTakePhotoViewController *c = [[CameraTakePhotoViewController alloc] init];
        w.vc = [[UINavigationController alloc] initWithRootViewController:c];
        c.delegate = self;
        w.vc.modalPresentationStyle = UIModalPresentationFullScreen;
        [vc presentViewController:w.vc animated:YES completion:nil];
    }];
}

- (void)presentTakePhotoController:(UIViewController *)vc {
    [self requeseCameraPermission:vc handler:^(UIViewController *vc, CameraController *w) {
        w.cameraVC = [[UIImagePickerController alloc] init];
        w.cameraVC.sourceType = UIImagePickerControllerSourceTypeCamera;
        w.cameraVC.editing = NO;
        w.cameraVC.delegate = w;
        w.cameraVC.modalPresentationStyle = UIModalPresentationFullScreen;
        [vc presentViewController:w.cameraVC animated:YES completion:nil];
    }];
}

- (void)present:(NSString *)code vc:(UIViewController *)vc {
    [self requeseCameraPermission:vc handler:^(UIViewController *vc, CameraController *w) {
        AVAudioSessionRecordPermission micPermission = [[AVAudioSession sharedInstance] recordPermission];
        if (micPermission == AVAudioSessionRecordPermissionUndetermined) {
            return ;
        }
        if (micPermission == AVAudioSessionRecordPermissionDenied) {
            [w noMicrophonePrivileges:vc];
        } else {
            w.vc = [[CameraRecordVideoViewController alloc] init];
            w.vc.modalPresentationStyle = UIModalPresentationFullScreen;
            ((CameraRecordVideoViewController *)(w.vc)).delegate = w;
            [vc presentViewController:w.vc animated:YES completion:nil];
            [((CameraRecordVideoViewController *)(w.vc)) displayCode:code];
        }
    }];
}

- (void)present2:(NSTimeInterval)minDuration maxDuration:(NSTimeInterval)maxDuration vc:(UIViewController *)vc {
    [self requeseCameraPermission:vc handler:^(UIViewController *vc, CameraController *w) {
        AVAudioSessionRecordPermission micPermission = [[AVAudioSession sharedInstance] recordPermission];
        if (micPermission == AVAudioSessionRecordPermissionUndetermined) {
            return ;
        }
        if (micPermission == AVAudioSessionRecordPermissionDenied) {
            [w noMicrophonePrivileges:vc];
        } else {
            CameraRecordVideo2ViewController *c = [[CameraRecordVideo2ViewController alloc] init];
            w.vc = [[UINavigationController alloc] initWithRootViewController:c];
            c.delegate = w;
            c.minDuration = minDuration;
            c.maxDuration = maxDuration;
            w.vc.modalPresentationStyle = UIModalPresentationFullScreen;
            [vc presentViewController:w.vc animated:YES completion:nil];

        }
    }];
}

- (void)encodeVideo:(NSURL *)videoURL {
    AVURLAsset *avAsset = [AVURLAsset URLAssetWithURL:videoURL options:nil];
    NSInteger videoDuration = (NSInteger)CMTimeGetSeconds(avAsset.duration);
    NSDate *startDate = [[NSDate alloc] init];
    AVAssetExportSession *exportSession = [AVAssetExportSession exportSessionWithAsset:avAsset presetName:[PhotoModel exportPresetQuality]];
    NSString *cache = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    NSString *filePath = [NSString stringWithFormat:@"%@/%@.mp4", cache, [NSUUID UUID].UUIDString];
    exportSession.outputURL = [NSURL fileURLWithPath:filePath];
    exportSession.outputFileType = AVFileTypeMPEG4;
    [exportSession setShouldOptimizeForNetworkUse:YES];
    CMTime start = CMTimeMakeWithSeconds(0.0, 0);
    CMTimeRange range = CMTimeRangeMake(start, avAsset.duration);
    exportSession.timeRange = range;
    [exportSession exportAsynchronouslyWithCompletionHandler:^{
        switch (exportSession.status) {
            case AVAssetExportSessionStatusFailed:
                NSLog(@"%@", exportSession.error.debugDescription);
                break;
            case AVAssetExportSessionStatusCancelled:
                NSLog(@"Export canceled");
                break;
            case AVAssetExportSessionStatusCompleted:
            {
                NSURL *vurl = [NSURL fileURLWithPath:filePath];
                UIImage *image = [AlbumController getVideoPreViewImage:vurl];
                NSString *imagePath = [NSString stringWithFormat:@"%@/%@",cache, [NSUUID UUID].UUIDString];
                NSURL *url = [NSURL fileURLWithPath:imagePath];
                NSError *error = nil;
                NSData *data = [SXUtil compressImage:image shortEdge:[PhotoModel shared].JpgCompressShortEdge level:1.0];
                [data writeToURL:url options:NSDataWritingAtomic error:&error];
                if (error) {
                    NSLog(@"%@", error.localizedDescription);
                }
                NSDate *endDate = [[NSDate alloc] init];
                NSTimeInterval time = [endDate timeIntervalSinceDate:startDate];
                NSLog(@"successful %lf", time);
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[NSFileManager defaultManager] removeItemAtURL:videoURL error:nil];
                    [self.delegate takeVideoPat:filePath thumbnailPath:imagePath duration:videoDuration];
                });
            }
                break;

            default:
                break;
        }
    }];
}

- (void)noCameraPrivileges:(UIViewController *)vc {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"No camera privileges" comment:@"no cmera privileges"] message:[SXUtil SXLocalizedString:@"Allow App to access your camera" comment:@"Allow App to access your camera"] preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *jumpToSetting = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Set" comment:@"Set"] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        [alert dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:jumpToSetting];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Cancel" comment:@"Cancel"] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [alert dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:cancel];
    [vc presentViewController:alert animated:YES completion:nil];
}

- (void)noMicrophonePrivileges:(UIViewController *)vc {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"No microphone privileges" comment:@"No microphone privileges"] message:[SXUtil SXLocalizedString:@"Allow App to access your microphone" comment:@"Allow App to access your microphone"] preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *jumpToSetting = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Set" comment:@"Set"] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        [alert dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:jumpToSetting];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Cancel" comment:@"Cancel"] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [alert dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:cancel];
    [vc presentViewController:alert animated:YES completion:nil];
}

- (void)videoURL:(NSURL *)url {
    [self.vc dismissViewControllerAnimated:YES completion:nil];
    if (url) {
        [self encodeVideo:url];
    } else {
        [self.delegate takeVideoPat:@"" thumbnailPath:@"" duration:0];
    }
}

- (void)imageURL:(NSURL * _Nullable)url {
    [self.vc dismissViewControllerAnimated:YES completion:nil];
    NSString *path = url.absoluteString;
    if (path) {
        NSArray *arr = [path componentsSeparatedByString:@"file://"];
        if (arr.count == 2) {
            [self.delegate takePhotoPath:arr[1]];
        } else {
            [self.delegate takePhotoPath:path];
        }
    } else {
        [self.delegate takePhotoPath:@""];
    }
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
    [self.delegate takePhotoPath:@""];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey,id> *)info {
    [picker dismissViewControllerAnimated:YES completion:nil];
    UIImage *image = info[UIImagePickerControllerOriginalImage];
    if (image) {
        [self saveImage:image];
    }
}

- (void)saveImage:(UIImage *)image {
    NSData *data = UIImageJPEGRepresentation(image, [PhotoModel shared].JpgCompressLevel);
    NSString *fullPath = [NSString stringWithFormat:@"%@/Documents/%@.jpeg", NSHomeDirectory(), [NSUUID UUID].UUIDString];
    NSError *error;
    [data writeToURL:[NSURL URLWithString:fullPath] options:NSDataWritingAtomic error:&error];
    if (error) {
        [self.delegate takePhotoPath:fullPath];
    } else {
        [self.delegate takePhotoPath:@""];
    }
}

@end
