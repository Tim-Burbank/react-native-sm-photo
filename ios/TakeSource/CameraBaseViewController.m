
#import "CameraBaseViewController.h"
#import <MobileCoreServices/MobileCoreServices.h>
#import <sys/utsname.h>
#import "PhotoModel.h"

@interface CameraBaseViewController ()

@property (nonatomic, strong) AVCaptureSession *captureSession;

@property (nonatomic, strong) AVCaptureMovieFileOutput *videoOutput;

@property (nonatomic, strong) AVCaptureStillImageOutput *imageOutput;

@property (nonatomic, strong) AVCaptureDeviceInput *audioInput;

@end

@implementation CameraBaseViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.captureSession = [[AVCaptureSession alloc] init];
    self.previewLayer = [[AVCaptureVideoPreviewLayer alloc] init];
    self.view.backgroundColor = [UIColor blackColor];
    self.startBarStyle = [[UIApplication sharedApplication] statusBarStyle];
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent animated:YES];
    if (self.outputType == OutputTypeVideo) {
        if ([self recordHighDefinition]) {
            _captureSession.sessionPreset = AVCaptureSessionPreset1920x1080;
        } else {
            _captureSession.sessionPreset = AVCaptureSessionPreset1280x720;
        }
    } else if (_outputType == OutputTypeImage) {
        _captureSession.sessionPreset = AVCaptureSessionPresetPhoto;
    }
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    for (AVCaptureDevice *device in devices) {
        if (device.position == [PhotoModel shared].StartDevicePostition) {
            NSError *error;
            AVCaptureInput *input = [AVCaptureDeviceInput deviceInputWithDevice:device error:&error];
            if (!error) {
                if ([_captureSession canAddInput:input]) {
                    [_captureSession addInput:input];
                    if (_outputType == OutputTypeVideo) {
                        AVCaptureMovieFileOutput *output = [[AVCaptureMovieFileOutput alloc] init];
                        // 防止视频录制超过十秒时没有声音
                        output.movieFragmentInterval = kCMTimeInvalid;
                        AVCaptureConnection *captureConnection = [output connectionWithMediaType:AVMediaTypeVideo];
                        if (captureConnection.supportsVideoStabilization) {
                            captureConnection.preferredVideoStabilizationMode = AVCaptureVideoStabilizationModeAuto;
                        }
                        _videoOutput = output;
                        if ([_captureSession canAddOutput:_videoOutput]) {
                            [_captureSession addOutput:_videoOutput];
                            AVCaptureDevice *audioDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeAudio];
                            NSError *err;
                            AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:audioDevice error:&err];
                            if (err) {
                                NSLog(@"error -- %@", err.localizedDescription);
                            }
                            if (!err) {
                                self.audioInput = input;
                            }
                        }
                    } else if (_outputType == OutputTypeImage) {
                        self.imageOutput = [[AVCaptureStillImageOutput alloc] init];
                        if ([_captureSession canAddOutput:_imageOutput]) {
                            [_captureSession addOutput:_imageOutput];
                        }
                    }
                    _previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:_captureSession];
                    _previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
                    _previewLayer.connection.videoOrientation = AVCaptureVideoOrientationPortrait;
                    [self.view.layer addSublayer:_previewLayer];
                    _previewLayer.frame = self.view.bounds;
                }
            }
        }
    }
    [_captureSession startRunning];
    UITapGestureRecognizer *tapRec = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapToFocus:)];
    tapRec.cancelsTouchesInView = NO;
    tapRec.numberOfTapsRequired = 1;
    tapRec.numberOfTouchesRequired = 1;
    [self.view addGestureRecognizer:tapRec];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (_audioInput) {
        [_captureSession addInput:_audioInput];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if (_audioInput) {
        [_captureSession removeInput:_audioInput];
    }
}

- (void)tapToFocus:(UITapGestureRecognizer *)gesture {
    CGPoint touchPoint = [gesture locationInView:self.view];
    CGPoint convertedPoint = [_previewLayer captureDevicePointOfInterestForPoint:touchPoint];
    AVCaptureDeviceInput *currentCameraInput = [_captureSession.inputs firstObject];
    if (currentCameraInput) {
        AVCaptureDevice *currentDevice = currentCameraInput.device;
        if (currentDevice.focusPointOfInterestSupported && [currentDevice isFocusModeSupported:AVCaptureFocusModeAutoFocus]) {
            NSError *error;
            [currentDevice lockForConfiguration: &error];
            if (!error) {
                currentDevice.focusPointOfInterest = convertedPoint;
                currentDevice.focusMode = AVCaptureFocusModeAutoFocus;
                [currentDevice unlockForConfiguration];
            }
        }
    }
}

- (void)startRecording:(NSTimeInterval)maxDuration {
    _videoOutput.maxRecordedDuration =CMTimeMakeWithSeconds(maxDuration, 30);
    NSString *cache = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    NSString *filePath = [NSString stringWithFormat:@"%@%@.mp4", cache, [NSUUID UUID].UUIDString];
    NSURL *url = [NSURL fileURLWithPath:filePath];
    [_videoOutput startRecordingToOutputFileURL:url recordingDelegate:self];
}

- (void)stopRecording {
    [_videoOutput stopRecording];
}

- (void)takePhoto {
    AVCaptureConnection *videoConnection = [_imageOutput connectionWithMediaType:AVMediaTypeVideo];
    [_imageOutput captureStillImageAsynchronouslyFromConnection:videoConnection completionHandler:^(CMSampleBufferRef  _Nullable imageDataSampleBuffer, NSError * _Nullable error) {
        if (imageDataSampleBuffer) {
            NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
            CGDataProviderRef dataProvider = CGDataProviderCreateWithCFData(CFBridgingRetain(imageData));
            CGImageRef cgImageRef = CGImageCreateWithJPEGDataProvider(dataProvider, nil, YES, kCGRenderingIntentDefault);
            UIImageOrientation orientation = UIImageOrientationRight;
            AVCaptureDeviceInput *input = [self.captureSession.inputs firstObject];
            if (input) {
                if (input.device.position == AVCaptureDevicePositionFront) {
                    orientation = UIImageOrientationLeftMirrored;
                }
            }
            UIImage *image = [UIImage imageWithCGImage:cgImageRef scale:1.0 orientation:orientation];
            UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
            UIGraphicsBeginImageContext(imageView.frame.size);
            [imageView.layer renderInContext:UIGraphicsGetCurrentContext()];
            UIImage *drawImage = UIGraphicsGetImageFromCurrentImageContext();

            TakePhotoPreviewViewController *c = [[TakePhotoPreviewViewController alloc] init];
            c.image = drawImage;
            c.delegate = self;
            [self.navigationController pushViewController:c animated:YES];
        }
    }];
}

- (void)switchCamera {
    [_captureSession beginConfiguration];
    NSArray *inputs = self.captureSession.inputs;
    AVCaptureInput *currentCameraInput = [inputs firstObject];
    if (!currentCameraInput) {
        return ;
    }
    NSMutableArray *otherInputs = [[NSMutableArray alloc] init];
    for (AVCaptureInput *input in inputs) {
        [_captureSession removeInput:input];
        if (input != currentCameraInput) {
            [otherInputs addObject:input];
        }
    }
    
    AVCaptureDevice *newCamera = nil;
    AVCaptureDeviceInput *input = (AVCaptureDeviceInput *)currentCameraInput;
    if (input) {
        if (input.device.position == AVCaptureDevicePositionBack) {
            newCamera = [self cameraWithPosition:AVCaptureDevicePositionFront];
        } else {
            newCamera = [self cameraWithPosition:AVCaptureDevicePositionBack];
        }
    }
    NSError *error;
    AVCaptureDeviceInput *newVideoInput = [AVCaptureDeviceInput deviceInputWithDevice:newCamera error:&error];
    if (newVideoInput == nil || error != nil) {
        NSLog(@"Error creating capture device input: %@", error.localizedDescription);
    } else {
        [_captureSession addInput:newVideoInput];
        for (AVCaptureInput *input in otherInputs) {
            [_captureSession addInput:input];
        }
    }
    
    [_captureSession commitConfiguration];
}

- (AVCaptureDevice *)cameraWithPosition:(AVCaptureDevicePosition)position {
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    if (devices) {
        NSMutableArray *newDevices = [[NSMutableArray alloc] init];
        for (NSInteger index =0; index < devices.count; index++) {
            AVCaptureDevice *device = devices[index];
            if (device.position == position) {
                [newDevices addObject:device];
                return [newDevices firstObject];
            }
        }
    }
    return nil;
}

#pragma mark - AVCaptureFileOutputRecordingDelegate

- (void)captureOutput:(AVCaptureFileOutput *)output didStartRecordingToOutputFileAtURL:(NSURL *)fileURL fromConnections:(NSArray<AVCaptureConnection *> *)connections {
    NSLog(@"didStartRecordingToOutputFileAt");
}

- (void)captureOutput:(AVCaptureFileOutput *)output didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL fromConnections:(NSArray<AVCaptureConnection *> *)connections error:(NSError *)error {
    NSLog(@"didFinishRecordingToOutputFileAt");
}

- (void)confirmPhoto:(UIImage *)image {
    NSLog(@"confirmPhoto");
}

- (BOOL)isiPhoneX {
    CGFloat screenHight = UIScreen.mainScreen.nativeBounds.size.height;
    if (screenHight == 2436 || screenHight == 1792 || screenHight == 2688 || screenHight == 1624) {
        return YES;
    }
    return NO;
}

- (NSString *)iphoneType {
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString*phoneType = [NSString stringWithCString: systemInfo.machine encoding:NSASCIIStringEncoding];
    
    if([phoneType  isEqualToString:@"iPhone1,1"])  return @"iPhone 2G";
    if([phoneType  isEqualToString:@"iPhone1,2"])  return @"iPhone 3G";
    if([phoneType  isEqualToString:@"iPhone2,1"])  return @"iPhone 3GS";
    if([phoneType  isEqualToString:@"iPhone3,1"])  return @"iPhone 4";
    if([phoneType  isEqualToString:@"iPhone3,2"])  return @"iPhone 4";
    if([phoneType  isEqualToString:@"iPhone3,3"])  return @"iPhone 4";
    if([phoneType  isEqualToString:@"iPhone4,1"])  return @"iPhone 4S";
    if([phoneType  isEqualToString:@"iPhone5,1"])  return @"iPhone 5";
    if([phoneType  isEqualToString:@"iPhone5,2"])  return @"iPhone 5";
    if([phoneType  isEqualToString:@"iPhone5,3"])  return @"iPhone 5c";
    if([phoneType  isEqualToString:@"iPhone5,4"])  return @"iPhone 5c";
    if([phoneType  isEqualToString:@"iPhone6,1"])  return @"iPhone 5s";
    if([phoneType  isEqualToString:@"iPhone6,2"])  return @"iPhone 5s";
    if([phoneType  isEqualToString:@"iPhone7,1"])  return @"iPhone 6 Plus";
    if([phoneType  isEqualToString:@"iPhone7,2"])  return @"iPhone 6";
    if([phoneType  isEqualToString:@"iPhone8,1"])  return @"iPhone 6s";
    if([phoneType  isEqualToString:@"iPhone8,2"])  return @"iPhone 6s Plus";
    if([phoneType  isEqualToString:@"iPhone8,4"])  return @"iPhone SE";
    if([phoneType  isEqualToString:@"iPhone9,1"])  return @"iPhone 7";
    if([phoneType  isEqualToString:@"iPhone9,2"])  return @"iPhone 7 Plus";
    if([phoneType  isEqualToString:@"iPhone10,1"]) return @"iPhone 8";
    if([phoneType  isEqualToString:@"iPhone10,4"]) return @"iPhone 8";
    if([phoneType  isEqualToString:@"iPhone10,2"]) return @"iPhone 8 Plus";
    if([phoneType  isEqualToString:@"iPhone10,5"]) return @"iPhone 8 Plus";
    if([phoneType  isEqualToString:@"iPhone10,3"]) return @"iPhone X";
    if([phoneType  isEqualToString:@"iPhone10,6"]) return @"iPhone X";
    if([phoneType  isEqualToString:@"iPhone11,8"]) return @"iPhone XR";
    if([phoneType  isEqualToString:@"iPhone11,2"]) return @"iPhone XS";
    if([phoneType  isEqualToString:@"iPhone11,4"]) return @"iPhone XS Max";
    if([phoneType  isEqualToString:@"iPhone11,6"]) return @"iPhone XS Max";
    
    if([phoneType  isEqualToString:@"iPod1,1"]) return @"iPod Touch 1G";
    if([phoneType  isEqualToString:@"iPod2,1"]) return @"iPod Touch 2G";
    if([phoneType  isEqualToString:@"iPod3,1"]) return @"iPod Touch 3G";
    if([phoneType  isEqualToString:@"iPod4,1"]) return @"iPod Touch 4G";
    if([phoneType  isEqualToString:@"iPod5,1"]) return @"iPod Touch 4G";
    
    if([phoneType  isEqualToString:@"iPad1,1"]) return @"iPad 1";
    if([phoneType  isEqualToString:@"iPad2,1"]) return @"iPad 2";
    if([phoneType  isEqualToString:@"iPad2,2"]) return @"iPad 2";
    if([phoneType  isEqualToString:@"iPad2,3"]) return @"iPod 2";
    if([phoneType  isEqualToString:@"iPad2,4"]) return @"iPad 2";
    if([phoneType  isEqualToString:@"iPad2,5"]) return @"iPad Mini 1";
    if([phoneType  isEqualToString:@"iPad2,6"]) return @"iPod Mini 1";
    
    if([phoneType  isEqualToString:@"iPad2,7"]) return @"iPad Mini 1";
    if([phoneType  isEqualToString:@"iPad3,1"]) return @"iPad 3";
    if([phoneType  isEqualToString:@"iPad3,2"]) return @"iPad 3";
    if([phoneType  isEqualToString:@"iPad3,3"]) return @"iPad 3";
    if([phoneType  isEqualToString:@"iPad3,4"]) return @"iPad 4";
    if([phoneType  isEqualToString:@"iPad3,5"]) return @"iPad 4";
    if([phoneType  isEqualToString:@"iPad3,6"]) return @"iPad 4";
    if([phoneType  isEqualToString:@"iPad4,1"]) return @"iPad Air";
    if([phoneType  isEqualToString:@"iPad4,2"]) return @"iPad Air";
    if([phoneType  isEqualToString:@"iPad4,3"]) return @"iPad Air";
    if([phoneType  isEqualToString:@"iPad4,4"]) return @"iPad Mini 2";
    if([phoneType  isEqualToString:@"iPad4,5"]) return @"iPad Mini 2";
    if([phoneType  isEqualToString:@"iPad4,6"]) return @"iPad Mini 2";
    if([phoneType  isEqualToString:@"iPad4,7"]) return @"iPad Mini 3";
    if([phoneType  isEqualToString:@"iPad4,8"]) return @"iPad Mini 3";
    if([phoneType  isEqualToString:@"iPad4,9"]) return @"iPad Mini 3";
    if([phoneType  isEqualToString:@"iPad5,1"]) return @"iPad Mini 4";
    if([phoneType  isEqualToString:@"iPad5,2"]) return @"iPad Mini 4";
    if([phoneType  isEqualToString:@"iPad5,3"]) return @"iPad Air 2";
    if([phoneType  isEqualToString:@"iPad5,4"]) return @"iPad Air 2";
    if([phoneType  isEqualToString:@"iPad6,3"]) return @"iPad Pro 9.7";
    if([phoneType  isEqualToString:@"iPad6,4"]) return @"iPad Pro 9.7";
    if([phoneType  isEqualToString:@"iPad6,7"]) return @"iPad Pro 12.9";
    if([phoneType  isEqualToString:@"iPad6,8"]) return @"iPad Pro 12.9";
    if([phoneType  isEqualToString:@"i386"]) return @"iPhone Simulator";
    if([phoneType  isEqualToString:@"x86_64"]) return @"iPhone Simulator";
    return @"";
}

- (BOOL)recordHighDefinition {
    if ([self isiPhoneX]) {
        return YES;
    } else {
        NSString *deviceType = [self iphoneType];
        if ([deviceType isEqualToString:@"iPhone 7"]) {
            return YES;
        }
        if ([deviceType isEqualToString:@"iPhone 7 Plus"]) {
            return YES;
        }
        if ([deviceType isEqualToString:@"iPhone 8"]) {
            return YES;
        }
        if ([deviceType isEqualToString:@"iPhone 8 Plus"]) {
            return YES;
        }
    }
    return NO;
}

@end
