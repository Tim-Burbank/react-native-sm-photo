
#import "CameraTakePhotoViewController.h"
#import "UIColor+StringColor.h"
#import "PhotoModel.h"
#import "UIImage+Operation.h"
#import "SXUtil.h"

@interface CameraTakePhotoViewController ()

@property (nonatomic, strong) UIButton *backButton;

@property (nonatomic, strong) UIButton *recordButton;

@property (nonatomic, strong) UIButton *flipButton;

@property (nonatomic, assign) BOOL hasTakePhoto;

@end

@implementation CameraTakePhotoViewController

- (instancetype)init
{
    self = [super init];
    if (self) {
        _hasTakePhoto = NO;
    }
    return self;
}

- (void)viewDidLoad {
    self.outputType = OutputTypeImage;
    [super viewDidLoad];
    CGFloat fixY = 0.0;
    BOOL isiPhoneX = NO;
    if (self.view.frame.size.height > 800) {
        fixY = 34.0;
        isiPhoneX = YES;
    }
    [self.view addSubview:self.backButton];
    [_backButton addTarget:self action:@selector(cancelTapped) forControlEvents:UIControlEventTouchUpInside];
    self.recordButton.center = CGPointMake(self.view.frame.size.width*0.5, self.view.frame.size.height-90.0-fixY);
    [self.view addSubview:_recordButton];
    [_recordButton addTarget:self action:@selector(recordTapped) forControlEvents:UIControlEventTouchUpInside];
    
    UIImageView *bottom = [[UIImageView alloc] init];
    bottom.backgroundColor = [UIColor pp_colorWithHexString:@"00000050"];
    bottom.frame = CGRectMake(0, self.view.frame.size.height-167.0-fixY, self.view.frame.size.width, 167+fixY);
    [self.view addSubview:bottom];
    
    self.flipButton.center = CGPointMake(self.view.frame.size.width-70, self.recordButton.center.y);
    [self.view addSubview:_flipButton];
    [_flipButton addTarget:self action:@selector(flip) forControlEvents:UIControlEventTouchUpInside];
    
    CGFloat width = self.view.frame.size.width;
    CGFloat height = width / 3 * 4;
    self.previewLayer.frame = CGRectMake(0, (isiPhoneX ? 111.0 : 0), width, height);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    self.hasTakePhoto = NO;
}

- (void)cancelTapped {
    [[UIApplication sharedApplication] setStatusBarStyle:self.startBarStyle animated:YES];
    [self.delegate imageURL:nil];
}

- (void)recordTapped {
    if (!_hasTakePhoto) {
        [self takePhoto];
        _hasTakePhoto = YES;
    }
}

- (void)flip {
    [self switchCamera];
}

- (void)confirmPhoto:(UIImage *)image {
    [[UIApplication sharedApplication] setStatusBarStyle:self.startBarStyle animated:YES];
    NSData *data = UIImageJPEGRepresentation(image, [PhotoModel shared].JpgCompressLevel);
    NSString *path = [NSString stringWithFormat:@"%@/Documents/%@.jpeg", NSHomeDirectory(), [NSUUID UUID].UUIDString];
    NSURL *fullpath = [NSURL fileURLWithPath:path];
    NSError *error;
    [data writeToURL:fullpath options:NSDataWritingAtomic error:&error];
    if (error) {
        [self.delegate imageURL:nil];
    } else {
        [self.delegate imageURL:fullpath];
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

- (UIButton *)recordButton {
    if (!_recordButton) {
        _recordButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _recordButton.frame = CGRectMake(0, 0, 81, 81);
        [_recordButton setImage:[UIImage imageBundleNamed:record_button_normal] forState:UIControlStateNormal];
        [_recordButton setImage:[UIImage imageBundleNamed:record_button_active] forState:UIControlStateHighlighted];
    }
    return _recordButton;
}

- (UIButton *)flipButton {
    if (!_flipButton) {
        _flipButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _flipButton.frame = CGRectMake(0, 0, 81, 81);
        [_flipButton setImage:[UIImage imageBundleNamed:camera_flip_button] forState:UIControlStateNormal];
    }
    return _flipButton;
}

@end
