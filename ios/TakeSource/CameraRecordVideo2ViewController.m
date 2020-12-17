
#import "CameraRecordVideo2ViewController.h"
#import "CameraRecordVideoViewController.h"
#import "RecordVideoPreviewViewController.h"
#import "UIColor+StringColor.h"
#import "UIImage+Operation.h"
#import "SXUtil.h"

@interface CameraRecordVideo2ViewController ()<RecordVideoPreviewViewControllerDelegate>

@property (nonatomic, assign) DisruptType disruptType;
@property (nonatomic, assign) BOOL isRecording;

@property (nonatomic, strong) UIButton *backButton;
@property (nonatomic, strong) UIButton *recordButton;
@property (nonatomic, strong) UIButton *flipButton;
@property (nonatomic, strong) UILabel *timeLabel;

@property (nonatomic, assign) NSInteger second;
@property (nonatomic, assign) NSInteger minute;

@property (nonatomic, strong) NSTimer *timer;

@end

@implementation CameraRecordVideo2ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    CGFloat fixHeight = 167;
    if (self.view.bounds.size.height > 800) {
        fixHeight = 201;
    }
    UIImageView *gl2 = [[UIImageView alloc] init];
    gl2.frame = CGRectMake(0, self.view.frame.size.height-fixHeight, self.view.frame.size.width, fixHeight);
    [self.view addSubview:gl2];
    
    [self.view addSubview:self.backButton];
    [_backButton addTarget:self action:@selector(cancelTapped) forControlEvents:UIControlEventTouchUpInside];
    
    self.recordButton.center = CGPointMake(self.view.frame.size.width*0.5, self.view.frame.size.height-106);
    [self.view addSubview:_recordButton];
    [_recordButton addTarget:self action:@selector(recordTapped) forControlEvents:UIControlEventTouchUpInside];
    
    self.flipButton.center = CGPointMake(self.view.frame.size.width-70, _recordButton.center.y);
    [self.view addSubview:_flipButton];
    [_flipButton addTarget:self action:@selector(flip) forControlEvents:UIControlEventTouchUpInside];
    
    [self.view addSubview:self.timeLabel];
    
    CGFloat width = self.view.frame.size.width;
    CGFloat height = width / 9 * 16;
    self.previewLayer.frame = CGRectMake(0, 0, width, height);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    _minute = 0;
    _second = 0;
    [self stopRecording];
}

- (void)cancelTapped {
    [[UIApplication sharedApplication] setStatusBarStyle:self.startBarStyle animated:YES];
    [self.delegate videoURL:nil];
}

- (void)recordTapped {
    if (self.isRecording) {
        [self stopRecording];
    } else {
        [self startRecording:_maxDuration];
        __weak typeof(self) weakSelf = self;
        if (@available(iOS 10.0, *)) {
            self.timer = [NSTimer scheduledTimerWithTimeInterval:1 repeats:YES block:^(NSTimer * _Nonnull timer) {
                weakSelf.second += 1;
                if (weakSelf.second == 60) {
                    weakSelf.second = 0;
                    weakSelf.minute += 1;
                }
                weakSelf.timeLabel.text = [NSString stringWithFormat:@"%.2ld:%.2ld", (long)weakSelf.minute, (long)weakSelf.second];
            }];
        }
    }
}

- (void)flip {
    [self switchCamera];
}

- (void)startRecording:(NSTimeInterval)maxDuration {
    self.disruptType = DisruptTypeNone;
    [super startRecording:_maxDuration];
    self.timeLabel.hidden = NO;
    self.backButton.hidden = YES;
    self.flipButton.hidden = YES;
    [self.recordButton setImage:[UIImage imageBundleNamed:record_button_pause] forState:UIControlStateNormal];
    self.isRecording = YES;
}

- (void)stopRecording {
    [super stopRecording];
    [self stopUI];
}

- (void)stopUI {
    _timeLabel.text = @"00:00";
    _timeLabel.hidden = YES;
    [_timer invalidate];
    _timer = nil;
    _backButton.hidden = NO;
    _flipButton.hidden = NO;
    [_recordButton setImage:[UIImage imageBundleNamed:record_button_active] forState:UIControlStateNormal];
    _isRecording = NO;
    
}

- (void)captureOutput:(AVCaptureFileOutput *)output didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL fromConnections:(NSArray<AVCaptureConnection *> *)connections error:(NSError *)error {
    [super captureOutput:output didFinishRecordingToOutputFileAtURL:outputFileURL fromConnections:connections error:error];
    switch (_disruptType) {
        case DisruptTypeNone:
            {
                RecordVideoPreviewViewController *c = [[RecordVideoPreviewViewController alloc] init];
                c.duration = self.minute * 60 + self.second;
                c.videoURL = outputFileURL;
                c.delegate = self;
                [self.navigationController pushViewController:c animated:YES];
            }
            break;
            
        default:
            break;
    }
}

- (void)confirmVideo:(NSURL *)url {
    [[UIApplication sharedApplication] setStatusBarStyle:self.startBarStyle animated:YES];
    [self.delegate videoURL:url];
}

#pragma mark - lazy

- (UIButton *)backButton {
    if (!_backButton) {
        _backButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_backButton setImage:[UIImage imageBundleNamed:back_white_arrow] forState:UIControlStateNormal];
        _backButton.frame = CGRectMake(0, [UIApplication sharedApplication].statusBarFrame.size.height, 44, 44);
    }
    return _backButton;
}

- (UIButton *)recordButton {
    if (!_recordButton) {
        _recordButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _recordButton.frame = CGRectMake(0, 0, 81, 81);
        [_recordButton setImage:[UIImage imageBundleNamed:record_button_active] forState:UIControlStateNormal];
        [_recordButton setImage:[UIImage imageBundleNamed:record_button_normal] forState:UIControlStateNormal];
    }
    return _recordButton;
}

- (UIButton *)flipButton {
    if (!_flipButton) {
        _flipButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _flipButton.frame = CGRectMake(0, 0, 65, 65);
        [_flipButton setImage:[UIImage imageBundleNamed:camera_flip_button] forState:UIControlStateNormal];
    }
    return _flipButton;
}

- (UILabel *)timeLabel {
    if (!_timeLabel) {
        _timeLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, UIScreen.mainScreen.bounds.size.height-50, UIScreen.mainScreen.bounds.size.width, 20)];
        _timeLabel.backgroundColor = [UIColor clearColor];
        _timeLabel.textAlignment = NSTextAlignmentCenter;
        _timeLabel.textColor = [SXUtil colorWithKey:TimeLabelColor];
        _timeLabel.font = [UIFont systemFontOfSize:16.0];
        _timeLabel.text = @"00:00";
        _timeLabel.hidden = YES;
    }
    return _timeLabel;
}

@end
