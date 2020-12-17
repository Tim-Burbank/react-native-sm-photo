
#import "CameraRecordVideoViewController.h"
#import "RecordButton.h"
#import "LineTimer.h"
#import "SXUtil.h"
#import "UIImage+Operation.h"

@interface CameraRecordVideoViewController ()<LineTimerDelegate, RecordButtonDelegate>

@property (nonatomic, assign) NSTimeInterval duration;

@property (nonatomic, assign) NSTimeInterval minDuration;

@property (nonatomic, assign) DisruptType disruptType;

@property (nonatomic, strong) UIButton *backButton;

@property (nonatomic, strong) UILabel *codeDescLabel;

@property (nonatomic, strong) UILabel *codeLabel;

@property (nonatomic, strong) RecordButton *recordButton;

@property (nonatomic, strong) LineTimer *lineTimer;

@property (nonatomic, strong) UILabel *operationLabel;

@end

@implementation CameraRecordVideoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self addingOverlay];
    _duration = 10;
    _minDuration = 2;
    _disruptType = DisruptTypeNone;
}

- (void)displayCode:(NSString *)code {
    _codeDescLabel.text = [SXUtil SXLocalizedString:@"Repeat the verification code" comment:@"Repeat the verification code"];
    NSMutableAttributedString *s = [[NSMutableAttributedString alloc] initWithString:code];
    [s addAttribute:NSKernAttributeName value:@8 range:NSMakeRange(0, code.length)];
    _codeLabel.attributedText = s;
}

- (void)addingOverlay {
    CGFloat topHeight = 108;
    if ([UIScreen mainScreen].bounds.size.height > 810) {
        topHeight += 20;
    }
    UIView *top = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, topHeight)];
    top.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
    [self.view addSubview:top];
    
    UIView *bottom = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height-120, self.view.frame.size.width, 120)];
    bottom.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
    [self.view addSubview:bottom];
    
    [self.view addSubview:self.codeDescLabel];
    [self.view addSubview:self.codeLabel];
    
    [self.view addSubview:self.backButton];
    
    [self.backButton addTarget:self action:@selector(cancelTapped) forControlEvents:UIControlEventTouchUpInside];
    
    self.recordButton.center = CGPointMake(self.view.frame.size.width/2, self.view.frame.size.height-60);
    [self.view addSubview:self.recordButton];
    self.recordButton.delegate = self;
    
    self.lineTimer.center = CGPointMake(self.view.frame.size.width/2, self.view.frame.size.height-120);
    [self.view addSubview: self.lineTimer];
    
    self.operationLabel.center = CGPointMake(self.view.frame.size.width/2, self.view.frame.size.height-120-self.operationLabel.frame.size.height);
    [self.view addSubview:self.operationLabel];
}

- (void)cancelTapped {
    [[UIApplication sharedApplication] setStatusBarStyle:self.startBarStyle animated:YES];
    [self.delegate videoURL:nil];
}

- (void)start {
    NSLog(@"start record");
    _disruptType = DisruptTypeNone;
    _operationLabel.text = [SXUtil SXLocalizedString:@"Slide up to cancel" comment:@"Slide up to cancel"];
    [_lineTimer start:_duration];
    [super startRecording:_duration+1];
}

- (void)stop:(BOOL)cancel {
    [super stopRecording];
    
    _operationLabel.text = [SXUtil SXLocalizedString:@"hold to start" comment:@"hold to start"];
    NSLog(@"end record");
    NSTimeInterval t = [self.lineTimer stop];
    NSLog(@"duration  %lf", t);
    if (cancel) {
        _disruptType = DisruptTypeCancel;
    } else if (t < _minDuration) {
        _disruptType = DisruptTypeAlert;
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:[SXUtil SXLocalizedString:@"video is too short" comment:@"video is too short"] preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"OK" comment:@"OK"] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        }];
        [alert addAction:action];
        [self presentViewController:alert animated:YES completion:nil];
    }
}

- (void)timeout {
    _operationLabel.text = [SXUtil SXLocalizedString:@"video is too short" comment:@"video is too short"];
    [super stopRecording];
}

- (void)captureOutput:(AVCaptureFileOutput *)output didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL fromConnections:(NSArray<AVCaptureConnection *> *)connections error:(nullable NSError *)error {
    [super captureOutput:output didFinishRecordingToOutputFileAtURL:outputFileURL fromConnections:connections error:error];
    [[UIApplication sharedApplication] setStatusBarStyle:self.startBarStyle animated:YES];
    switch (_disruptType) {
        case DisruptTypeNone:
            [self.delegate videoURL:outputFileURL];
            break;
            
        default:
            break;
    }
}

#pragma makr - lazy

- (UIButton *)backButton {
    if (!_backButton) {
        _backButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_backButton setImage:[UIImage imageBundleNamed:@"BackButton"] forState:UIControlStateNormal];
        _backButton.frame = CGRectMake(0, 20, 44, 44);
    }
    return _backButton;
}

- (UILabel *)codeDescLabel {
    if (!_codeDescLabel) {
        _codeDescLabel = [[UILabel alloc] init];
        CGFloat originY = 20;
        if ([UIScreen mainScreen].bounds.size.height > 810) {
            originY = 20 + 20;
        }
        _codeDescLabel.frame = CGRectMake(0, originY, self.view.frame.size.width, 44);
        _codeDescLabel.textAlignment = NSTextAlignmentCenter;
        _codeDescLabel.font = [UIFont systemFontOfSize:18.0];
        _codeDescLabel.textColor = [UIColor whiteColor];
    }
    return _codeDescLabel;
}

- (UILabel *)codeLabel {
    if (!_codeLabel) {
        _codeLabel = [[UILabel alloc] init];
        CGFloat originY = 58;
        if ([UIScreen mainScreen].bounds.size.height > 810) {
            originY = 58 + 20;
        }
        _codeLabel.frame = CGRectMake(0, originY, self.view.frame.size.width, 44);
        _codeLabel.textAlignment = NSTextAlignmentCenter;
        _codeLabel.textColor = [SXUtil colorWithKey:CameraRecordVideoVCCodeLabelTextColor];
        _codeLabel.font = [UIFont boldSystemFontOfSize:30];
    }
    return _codeLabel;
}

- (RecordButton *)recordButton {
    if (!_recordButton) {
        _recordButton = [RecordButton buttonWithType:UIButtonTypeCustom];
        _recordButton.frame = CGRectMake(0, 0, 70, 70);
        _recordButton.layer.borderWidth = 2;
        _recordButton.layer.borderColor = [SXUtil colorWithKey:CameraRecordVideoVCRecordButtonColor].CGColor;
        _recordButton.layer.cornerRadius = 35;
    }
    return _recordButton;
}

- (LineTimer *)lineTimer {
    if (!_lineTimer) {
        _lineTimer = [[LineTimer alloc] init];
        _lineTimer.frame = CGRectMake(0, 0, self.view.frame.size.width, 2);
        _lineTimer.delegate = self;
    }
    return _lineTimer;
}

- (UILabel *)operationLabel {
    if (!_operationLabel) {
        _operationLabel = [[UILabel alloc] init];
        _operationLabel.text = [SXUtil SXLocalizedString:@"Slide up to cancel" comment:@"Slide up to cancel"];
        _operationLabel.frame = CGRectMake(0, 0, self.view.frame.size.width, 20);
        _operationLabel.textAlignment = NSTextAlignmentCenter;
        _operationLabel.textColor = [UIColor whiteColor];
        _operationLabel.font = [UIFont boldSystemFontOfSize:13];
    }
    return _operationLabel;
}

@end
