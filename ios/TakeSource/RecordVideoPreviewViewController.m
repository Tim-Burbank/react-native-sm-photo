
#import "RecordVideoPreviewViewController.h"
#import <Photos/Photos.h>
#import "SXUtil.h"
#import "UIColor+StringColor.h"
#import "PhotoModel.h"
#import "SXImageFlowViewController.h"
#import "DownloadProgress.h"
#import "AlbumController.h"
#import "UIImage+Operation.h"

@interface RecordVideoPreviewViewController ()

@property (nonatomic, copy) NSString *localPath;

@property (nonatomic, copy) NSString *thumbnailPath;

@property (nonatomic, strong) UIButton *backButton;

@property (nonatomic, strong) UIButton *doneButton;

@property (nonatomic, strong) UIButton *playButton;

@property (nonatomic, strong) UISlider *progress;

@property (nonatomic, strong) UILabel *leftLabel;

@property (nonatomic, strong) UILabel *rightLabel;

@property (assign) BOOL dragSlider;

@property (nonatomic, strong) id observer;

@end

@implementation RecordVideoPreviewViewController

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

#pragma mark - view lift style

- (void)viewDidLoad {
    [super viewDidLoad];
    AVPlayerLayer *layer = [AVPlayerLayer playerLayerWithPlayer:self.player];
    layer.frame = self.view.bounds;
    layer.videoGravity = AVLayerVideoGravityResizeAspect;
    [self.view.layer addSublayer:layer];
    [self.player play];

    [self.view addSubview:self.backButton];
    [_backButton addTarget:self action:@selector(cancelTapped) forControlEvents:UIControlEventTouchUpInside];

    self.view.backgroundColor = [UIColor blackColor];

    [self.view addSubview:self.doneButton];
    [self.doneButton addTarget:self action:@selector(doneOptimize) forControlEvents:UIControlEventTouchUpInside];

    UILabel *l = [[UILabel alloc] init];
    l.text = [SXUtil SXLocalizedString:@"preview" comment:@"预览"];
    l.textColor = [UIColor whiteColor];
    l.font = [UIFont systemFontOfSize:18];
    [self.view addSubview:l];
    l.frame = CGRectMake(100, UIApplication.sharedApplication.statusBarFrame.size.height, self.view.frame.size.width-200, 44);
    l.textAlignment = NSTextAlignmentCenter;

    [self.view addSubview:self.playButton];
    [_playButton addTarget:self action:@selector(play) forControlEvents:UIControlEventTouchUpInside];

    self.leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(CGRectGetMaxX(_playButton.frame), 0, 44, 44)];
    self.leftLabel.textColor = [UIColor whiteColor];
    self.leftLabel.font = [UIFont systemFontOfSize:12];
    self.leftLabel.text = @"0:00";
    self.leftLabel.textAlignment = NSTextAlignmentCenter;
    self.leftLabel.backgroundColor = [UIColor clearColor];
    self.leftLabel.center = CGPointMake(self.leftLabel.center.x, self.playButton.center.y);
    [self.view addSubview: self.leftLabel];

    int m = (int)(_duration/60);
    int s = (int)(_duration - (Float64)(60*m));
    self.rightLabel = [[UILabel alloc] initWithFrame:CGRectMake(UIScreen.mainScreen.bounds.size.width-50, 0, 50, 16)];
    self.rightLabel.textColor = [UIColor whiteColor];
    self.rightLabel.font = [UIFont systemFontOfSize:12];
    self.rightLabel.text = [NSString stringWithFormat:@"%d:%.2d", m, s];
    self.rightLabel.textAlignment = NSTextAlignmentCenter;
    self.rightLabel.backgroundColor = [UIColor clearColor];
    self.rightLabel.center = CGPointMake(self.rightLabel.center.x, self.playButton.center.y);
    [self.view addSubview: self.rightLabel];

    [self.view addSubview:self.progress];
    _progress.frame = CGRectMake(CGRectGetMaxX(_leftLabel.frame), _progress.frame.origin.y, UIScreen.mainScreen.bounds.size.width-50-CGRectGetMaxX(_leftLabel.frame), 10);
    _progress.center = CGPointMake(_progress.center.x, _playButton.center.y);
    [self addPlayerObserver];
    __weak typeof(self) weakSelf = self;
    [[NSNotificationCenter defaultCenter] addObserverForName:AVPlayerItemDidPlayToEndTimeNotification object:_player.currentItem queue:NSOperationQueue.mainQueue usingBlock:^(NSNotification * _Nonnull note) {
        AVPlayerItem *playeriItem = (AVPlayerItem *)note.object;
        if (playeriItem) {
            [playeriItem seekToTime:kCMTimeZero completionHandler:^(BOOL finished) {
                [weakSelf.player play];
            }];
        }
    }];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (self.assset) {
        [self.navigationController setNavigationBarHidden:YES animated:NO];
    }
    [UIApplication.sharedApplication setStatusBarStyle:UIStatusBarStyleLightContent animated:YES];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:_observer];
    if (self.assset) {
        [self.navigationController setNavigationBarHidden:NO animated:NO];
    }
}


- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

#pragma mark - touch events

- (void)done {
    if (self.navigationController) {
        [self.delegate confirmVideo:self.videoURL];
    } else {
        if (self.duration > [PhotoModel shared].VideoDuration) {

        } else {
            [self progreeVideo];
        }
    }
}

- (void)doneOptimize {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.player pause];
    });
    if (self.assset) {
        if (self.duration > [PhotoModel shared].VideoDuration) {
            [SXImageFlowViewController showAlert:self defaultHandler:^{
                [self.delegate confirAsset:_assset];
            }];
        } else {
            [self.delegate confirAsset:_assset];;
        }
    } else if (self.videoURL) {
        [self.delegate confirmVideo:_videoURL];
    }
}

- (void)progreeVideo {

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.player pause];
        CircleProgressAlert *progressing = [[CircleProgressAlert alloc] initWithFrame:UIScreen.mainScreen.bounds];
        [self.view addSubview:progressing];
        NSString *exportPreset = [PhotoModel exportPresetQuality:self.player.currentItem.asset];
        AVAssetExportSession *exportSeesion = [AVAssetExportSession exportSessionWithAsset:self.player.currentItem.asset presetName:exportPreset];
        if (exportSeesion) {
            NSString *cache = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
            self.localPath = [NSString stringWithFormat:@"%@/%@.mp4", cache, [NSUUID UUID].UUIDString];
            exportSeesion.outputURL = [NSURL fileURLWithPath:self.localPath];
            exportSeesion.outputFileType = AVFileTypeMPEG4;
            if (self.duration > [PhotoModel shared].VideoDuration) {
                CMTime time = CMTimeMakeWithSeconds(0.0, 30);
                CMTime d = CMTimeMakeWithSeconds([PhotoModel shared].VideoDuration, 30);
                CMTimeRange range = CMTimeRangeMake(time, d);
                exportSeesion.timeRange = range;
                self.duration = [PhotoModel shared].VideoDuration;
            }
            __weak typeof(self) weakSelf = self;
            [exportSeesion exportAsynchronouslyWithCompletionHandler:^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    switch (exportSeesion.status) {
                        case AVAssetExportSessionStatusFailed:
                            break;
                        case AVAssetExportSessionStatusCancelled:
                            break;
                        case AVAssetExportSessionStatusCompleted:
                        {
                            NSURL *vurl = [NSURL fileURLWithPath:weakSelf.localPath];
                            UIImage *image = [AlbumController getVideoPreViewImage:vurl];
                            weakSelf.thumbnailPath = [NSString stringWithFormat:@"%@/%@", cache, [NSUUID UUID].UUIDString];
                            NSURL *url = [NSURL fileURLWithPath:weakSelf.thumbnailPath];
                            NSData *data = [SXUtil compressImage:image shortEdge:[PhotoModel shared].JpgCompressShortEdge level:1.0];
                            NSError *error;
                            [data writeToURL:url options:0 error:&error];
                            if (error) {
                                NSLog(@"record video -- %@", error);
                            }
                            [progressing removeFromSuperview];
                            [weakSelf.delegate confirmVideo:weakSelf.localPath thumbNailPath:weakSelf.thumbnailPath duration:weakSelf.duration];
                            [weakSelf dismissViewControllerAnimated:YES completion:nil];
                        }
                            break;

                        default:
                            break;
                    }
                });
            }];
        }
    });
}

- (void)cancelTapped {
    if (self.videoURL) {
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"discard" comment:@""] message:[SXUtil SXLocalizedString:@"sureReturn" comment:@""] preferredStyle:UIAlertControllerStyleAlert];
        [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"back" comment:@"返回"] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self.navigationController popViewControllerAnimated:YES];
        }]];
        [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"cancel" comment:@"取消"] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        }]];
        [self presentViewController:alert animated:YES completion:nil];
    } else if (self.assset) {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

- (void)play {
    if (_player) {
        if (_player.rate != 0 && _player.error == nil) {
            [_player pause];
            [self.playButton setImage:[UIImage imageBundleNamed:video_preview_play_button] forState:UIControlStateNormal];
        } else {
            [_player play];
            [self.playButton setImage:[UIImage imageBundleNamed:video_preview_pause_button] forState:UIControlStateNormal];
        }
    }
}

- (void)addPlayerObserver {
    __weak typeof(self) weakSelf = self;
    _observer = [_player addPeriodicTimeObserverForInterval:CMTimeMake(1, 30) queue:dispatch_get_main_queue() usingBlock:^(CMTime time) {
        if (weakSelf) {
            if (weakSelf.dragSlider) {
                return ;
            }
            Float64 current = CMTimeGetSeconds(time);
            int cm = (int)(current/60.0);
            int cs = (int)(current-(Float64)(60*cm));
            weakSelf.leftLabel.text = [NSString stringWithFormat:@"%d:%.2d", cm, cs];
            weakSelf.progress.value = (Float64)current/(Float64)(weakSelf.duration);

        }
    }];
}

- (void)sliderChange {
    self.dragSlider = YES;
    [self.player pause];
    int time = (int)(self.duration * self.progress.value);
    int m = time / 60;
    int s = time - m * 60;
    self.leftLabel.text = [NSString stringWithFormat:@"%d:%.2d", m, s];
    [self.player seekToTime:CMTimeMake(time, 1)];
    [[NSNotificationCenter defaultCenter] removeObserver:_observer];
}

- (void)sliderChangeEnd {
    _dragSlider = NO;
    if (_player) {
        [_player play];
    }
    [self addPlayerObserver];
}

#pragma mark - set

- (void)setVideoURL:(NSURL *)videoURL {
    _videoURL = videoURL;
    if (_videoURL) {
        self.player = [[AVPlayer alloc] initWithURL: videoURL];
    }
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

- (UIButton *)doneButton {
    if (!_doneButton) {
        _doneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_doneButton setTitle:[SXUtil SXLocalizedString:@"send" comment:@"发送"] forState:UIControlStateNormal];
        [_doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _doneButton.titleLabel.font = [UIFont systemFontOfSize:16.0];
        _doneButton.frame = CGRectMake(self.view.frame.size.width-60, [UIApplication sharedApplication].statusBarFrame.size.height, 44, 44);
    }
    return _doneButton;
}

- (UIButton *)playButton {
    if (!_playButton) {
        _playButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_playButton setImage:[UIImage imageBundleNamed:video_preview_pause_button] forState:UIControlStateNormal];
        if (self.view.frame.size.height > 800) {
            _playButton.frame = CGRectMake(0, self.view.frame.size.height-84, 44, 44);
        } else {
            _playButton.frame = CGRectMake(0, self.view.frame.size.height-50, 44, 44);
        }
    }
    return _playButton;
}

- (UISlider *)progress {
    if (!_progress) {
        _progress = [[UISlider alloc] initWithFrame:CGRectMake(0, [UIScreen mainScreen].bounds.size.height-50, 220, 10)];
        [_progress setContinuous:YES];
        _progress.thumbTintColor = [UIColor whiteColor];
        _progress.minimumTrackTintColor = [SXUtil colorWithKey:RecordVideoProgressColor];
        _progress.maximumTrackTintColor = [UIColor whiteColor];
        _progress.minimumValue = 0.0;
        _progress.maximumValue = 1.0;
        [_progress setThumbImage:[UIImage imageBundleNamed:point] forState:UIControlStateNormal];
        [_progress setThumbImage:[UIImage imageBundleNamed:point] forState:UIControlStateHighlighted];
        [_progress addTarget:self action:@selector(sliderChange) forControlEvents:UIControlEventValueChanged | UIControlEventTouchDown];
        [_progress addTarget:self action:@selector(sliderChangeEnd) forControlEvents:UIControlEventTouchUpInside |UIControlEventTouchCancel | UIControlEventTouchUpOutside];

    }
    return _progress;
}

@end
