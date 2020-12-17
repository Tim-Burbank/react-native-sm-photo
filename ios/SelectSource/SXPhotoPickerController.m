
#import "SXPhotoPickerController.h"
#import "SXAlbumTableViewController.h"
#import "SXImageFlowViewController.h"
#import "DXPickerHelper.h"

@interface SXPhotoPickerController ()<UINavigationControllerDelegate, UIGestureRecognizerDelegate>
{
    UIStatusBarStyle _barStyle;
}

@end

@implementation SXPhotoPickerController

static NSInteger _maxSelectedNumber = 3;
static NSInteger _minSelectedNumber = 0;
static BOOL _isAvatar = NO;
static MediaType _mediaType = MediaTypePhoto;

- (void)dealloc {
    NSLog(@"PhotoPciker des");
}

- (void)viewDidLoad {
    [super viewDidLoad];
    _barStyle = UIApplication.sharedApplication.statusBarStyle;
    self.interactivePopGestureRecognizer.delegate = self;
    self.interactivePopGestureRecognizer.enabled = YES;
    self.navigationBar.barStyle = UIBarStyleBlack;
    self.navigationBar.backgroundColor = [UIColor blackColor];
    self.toolbar.barStyle = UIBarStyleDefault;
    self.toolbar.barTintColor = [[UIColor blackColor] colorWithAlphaComponent:0.9];
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent animated:YES];
    __weak typeof(self) weakSelf = self;
    [[NSNotificationCenter defaultCenter] addObserverForName:@"dismissViewController" object:nil queue:NSOperationQueue.mainQueue usingBlock:^(NSNotification * _Nonnull note) {
        [weakSelf dismissViewControllerAnimated:YES completion:nil];
    }];
    if ([DXPickerHelper fetchAlbumIdentifier] == nil) {
        [self showAlbumList];
        [self chargeAuthorizationStatus:PHPhotoLibrary.authorizationStatus];
    } else {
        if ([DXPickerHelper fetchAlbumIdentifier].length <= 0) {
            [self showAlbumList];
            [self chargeAuthorizationStatus:PHPhotoLibrary.authorizationStatus];
        } else {
            [self showImageFlow];
        }
        
    }
}

#pragma mark - view life style

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[UIApplication sharedApplication] setStatusBarStyle:_barStyle animated:YES];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)showAlbumList {
    SXAlbumTableViewController *viewController = [[SXAlbumTableViewController alloc] init];
    self.viewControllers = @[viewController];
}

- (void)showImageFlow {
    SXAlbumTableViewController *rootVC = [[SXAlbumTableViewController alloc] init];
    SXImageFlowViewController *imageFlowVC = [[SXImageFlowViewController alloc] initWithIdentifier:[DXPickerHelper fetchAlbumIdentifier]];
    self.viewControllers = @[rootVC, imageFlowVC];
}

- (void)chargeAuthorizationStatus:(PHAuthorizationStatus)status {
    SXAlbumTableViewController *vc = [self.viewControllers firstObject];
    if (!vc) {
        [self showAlbumList];
        return ;
    }
    switch (status) {
        case PHAuthorizationStatusAuthorized:
            [vc reloadTableView];
            break;
        case PHAuthorizationStatusDenied:
            [vc showUnAuthorizedTipsView];
            break;
        case PHAuthorizationStatusRestricted:
            [vc showUnAuthorizedTipsView];
            break;
        case PHAuthorizationStatusNotDetermined:
            [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
                if (status == PHAuthorizationStatusNotDetermined) {
                    return ;
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self chargeAuthorizationStatus:status];
                });
            }];
            break;
    }
}

#pragma mark - class property

+ (NSInteger)maxSelectedNumber {
    return _maxSelectedNumber;
}

+ (NSInteger)minSelectedNumber {
    return _minSelectedNumber;
}

+ (void)setMinSelectedNumber:(NSInteger)minSelectedNumber {
    _minSelectedNumber = minSelectedNumber;
}

+ (void)setMaxSelectedNumber:(NSInteger)maxSelectedNumber {
    _maxSelectedNumber = maxSelectedNumber;
}

+ (BOOL)isAvatar {
    return  _isAvatar;
}

+ (void)setIsAvatar:(BOOL)isAvatar {
    _isAvatar = isAvatar;
}

+ (MediaType)mediaType {
    return _mediaType;
}

+ (void)setMediaType:(MediaType)mediaType {
    _mediaType = mediaType;
}


@end
