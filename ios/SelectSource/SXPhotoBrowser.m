
#import "SXPhotoBrowser.h"
#import "UIView+Frame.h"
#import "SXBrowserCell.h"
#import "SXSendButton.h"
#import "UIViewController+PPNavigationBarPosition.h"
#import "UIImage+Operation.h"
#import "SXUtil.h"

@interface SXPhotoBrowser ()<UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout>

@property (nonatomic, assign) BOOL statusBarShouldBeHidden;

@property (nonatomic, assign) BOOL didSavePreviousStateOfNavBar;

@property (nonatomic, assign) BOOL viewIsActive;

@property (nonatomic, assign) BOOL viewHasAppearedInitially;

@property (nonatomic, assign) BOOL previousNavBarHidden;

@property (nonatomic, assign) BOOL previousNavBarTranslucent;

@property (nonatomic, assign) UIBarStyle previousNavBarStyle;

@property (nonatomic, assign) UIStatusBarStyle previousStatusBarStyle;

@property (nonatomic, strong) UIColor *previousNavBarTintColor;

@property (nonatomic, strong) UIColor *previousNavBarBarTintColor;

@property (nonatomic, strong) UIBarButtonItem *previousViewControllerBackButton;

@property (nonatomic, strong) UIImage *previousNavigationBarBackgroundImageDefault;

@property (nonatomic, strong) UIImage *previousNavigationBarBackgroundImageLandscapePhone;

@property (nonatomic, strong) NSMutableArray *photosDataSource;

@property (nonatomic, assign) NSInteger currentIndx;

@property (nonatomic, assign) BOOL isFullImage;

@property (nonatomic, assign) PHImageRequestID requestId;

@property (nonatomic, strong) UICollectionView *browserCollectionView;

@property (nonatomic, strong) UIToolbar *toolBar;

@property (nonatomic, strong) UIButton *checkButton;

@property (nonatomic, strong) SXSendButton *sendButton;

@end

@implementation SXPhotoBrowser

static NSString *const browserCellReuseIdntifier = @"DXBrowserCell";

- (instancetype)initWithPhotos:(NSArray *)photos currentIndex:(NSInteger)index isFullImage:(BOOL)isFull {
    if (self = [super init]) {
        self.currentIndx = index;
        _isFullImage = isFull;
        _photosDataSource = [NSMutableArray arrayWithArray:photos];
    }
    return self;
}

#pragma mark - view life circle

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupViews];
    [self updateNavigationBarAndToolBar];
    [self updateSelectedNumber];
    [[NSNotificationCenter defaultCenter] addObserverForName:@"GifLimit" object:nil queue:NSOperationQueue.mainQueue usingBlock:^(NSNotification * _Nonnull note) {
        [self updateSelectedNumber];
        [self updateNavigationBarAndToolBar];
    }];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    _previousStatusBarStyle = UIApplication.sharedApplication.statusBarStyle;
    [UIApplication.sharedApplication setStatusBarStyle:UIStatusBarStyleDefault animated:animated];
    if (_viewIsActive == NO && self.navigationController.viewControllers.firstObject) {
        [self storePreviousNavBarAppearance];
    }
    [self setNavBarAppearance:animated];
    if (_viewHasAppearedInitially == NO) {
        _viewHasAppearedInitially = YES;
    }
    _browserCollectionView.contentOffset = CGPointMake(_browserCollectionView.frame.size.width*_currentIndx, 0);
}

- (void)viewDidAppear:(BOOL)animated {
    if (self.navigationController) {
        if (![self.navigationController.viewControllers.firstObject isKindOfClass:[self class]] && [self.navigationController.viewControllers containsObject:self] == NO) {
            _viewIsActive = NO;
            [self restorePreviousNavBarAppearance:animated];
            [self.navigationController.navigationBar.layer removeAllAnimations];
        }
    }
    [self.navigationController.navigationBar.layer removeAllAnimations];
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    [self setControliShidden:NO animated:NO];
    [UIApplication.sharedApplication setStatusBarStyle:_previousStatusBarStyle animated:animated];
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    _viewIsActive = YES;
}

- (void)restorePreviousNavBarAppearance:(BOOL)animated {
    if (_didSavePreviousStateOfNavBar) {
        [self.navigationController setNavigationBarHidden:_previousNavBarHidden animated:animated];
        UINavigationBar *navBar = self.navigationController.navigationBar;
        navBar.tintColor = _previousNavBarTintColor;
        navBar.translucent = _previousNavBarTranslucent;
        navBar.barTintColor = _previousNavBarBarTintColor;
        navBar.barStyle = _previousNavBarStyle;
        [navBar setBackgroundImage:_previousNavigationBarBackgroundImageDefault forBarMetrics:UIBarMetricsDefault];
        [navBar setBackgroundImage:_previousNavigationBarBackgroundImageLandscapePhone forBarMetrics:UIBarMetricsCompact];
        if (_previousViewControllerBackButton) {
            UIViewController *previousViewController = self.navigationController.topViewController;
            previousViewController.navigationItem.backBarButtonItem = _previousViewControllerBackButton;
            _previousViewControllerBackButton = nil;
        }
    }
}

- (void)setNavBarAppearance:(BOOL)animated {
    if (self.navigationController) {
        [self.navigationController setNavigationBarHidden:NO animated:animated];
        UINavigationBar *navBar = self.navigationController.navigationBar;
        navBar.tintColor = [UIColor whiteColor];
        navBar.translucent = YES;
        navBar.barStyle = UIBarStyleBlack;
        [navBar setBackgroundImage:nil forBarMetrics:UIBarMetricsDefault];
        [navBar setBackgroundImage:nil forBarMetrics:UIBarMetricsCompact];
    }
}

- (void)storePreviousNavBarAppearance {
    _didSavePreviousStateOfNavBar = true;
    _previousNavBarBarTintColor = self.navigationController.navigationBar.barTintColor;
    _previousNavBarTranslucent = self.navigationController.navigationBar.isTranslucent;
    _previousNavBarTintColor = self.navigationController.navigationBar.tintColor;
    _previousNavBarHidden = self.navigationController.isNavigationBarHidden;
    _previousNavBarStyle = self.navigationController.navigationBar.barStyle;
    _previousNavigationBarBackgroundImageDefault = [self.navigationController.navigationBar backgroundImageForBarMetrics:UIBarMetricsDefault];
    _previousNavigationBarBackgroundImageLandscapePhone = [self.navigationController.navigationBar backgroundImageForBarMetrics:UIBarMetricsCompact];
}

- (void)setupConfirmButton {
    CGFloat width = 58;
    CGFloat height = 44;
    CGFloat fixY = 0;
    if ([UIScreen mainScreen].bounds.size.height > 800) {
        fixY = 34.0;
    }
    CGFloat originX = [UIScreen mainScreen].bounds.size.width - 16 - 58;
    CGFloat originY = [UIScreen mainScreen].bounds.size.height - 44 - fixY;
    UIView *confirmContentView = [[UIView alloc] initWithFrame:CGRectMake(originX, originY, width, height)];
    [confirmContentView addSubview:self.sendButton];
    self.sendButton.center = CGPointMake(29, 22);
    [self.view addSubview:confirmContentView];
}

- (void)setupViews {
    self.automaticallyAdjustsScrollViewInsets = NO;
    self.view.clipsToBounds = YES;
    [self.view addSubview:self.browserCollectionView];
    [self setupConfirmButton];
    UIBarButtonItem *rightBarItem = [[UIBarButtonItem alloc] initWithCustomView:self.checkButton];
    self.navigationItem.rightBarButtonItem = rightBarItem;
    [self createBarButtonItemAtPosition:PPNavigationBarPositionLeft normalImage:[UIImage imageBundleNamed:BackButton] highlightImage:[UIImage imageBundleNamed:BackButton] action:@selector(backButtonAction)];
}

- (void)updateNavigationBarAndToolBar {
    if (_photosDataSource) {
        self.title = [NSString stringWithFormat:@"%lu/%lu", _currentIndx+1, _photosDataSource.count];
        BOOL selected = [self.delegate photoBrowser:self currentPhotoAssetIsSelected:_photosDataSource[_currentIndx]];
        _checkButton.selected = selected;
    }
}

- (void)updateSelectedNumber {
    NSInteger number = [self.delegate selectedPhotosNumberInPhotoBrowser:self];
    self.sendButton.badgeValue = [NSString stringWithFormat:@"%ld", (long)number];
}

- (void)didScrollToPage:(int)page {
    _currentIndx = page;
    [self updateNavigationBarAndToolBar];
}

- (void)checkButtonAction {
    if (_checkButton.selected) {
        [_delegate photoBrowser:self deselectedAsset:_photosDataSource[_currentIndx]];
        _checkButton.selected = NO;
        [self updateSelectedNumber];
    } else {
        BOOL selected = [_delegate photobrowser:self selectedAssset:_photosDataSource[_currentIndx]];
        _checkButton.selected = selected;
        if (_checkButton.selected) {
            [self updateSelectedNumber];
        }
    }
}

- (void)sendButtonAction {
    [_delegate sendImagesFromPhotoBrowser:self currentAsset:_photosDataSource[_currentIndx]];
}

- (void)backButtonAction {
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - delegate

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    if (scrollView.contentOffset.x >= 0) {
        CGFloat page = scrollView.contentOffset.x / _browserCollectionView.frame.size.width;
        [self didScrollToPage:(int)page];
    }
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(self.view.bounds.size.width+20, self.view.bounds.size.height);
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return _photosDataSource.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    SXBrowserCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:browserCellReuseIdntifier forIndexPath:indexPath];
    cell.assset = _photosDataSource[indexPath.row];
    cell.photoBrowser = self;
    return cell;
}

- (void)setControliShidden:(BOOL)hidden animated:(BOOL)animated {
    BOOL hide = hidden;
    if (_photosDataSource.count == 0) {
        hide = NO;
    }
    CGFloat animationOffset = 20;
    CGFloat animationDuration = animated ? 0.35 : 0.0;
    _statusBarShouldBeHidden = hide;
    [UIView animateWithDuration:animationDuration animations:^{
        [self setNeedsStatusBarAppearanceUpdate];
    }];
    
    CGRect frame = CGRectIntegral(CGRectMake(0, self.view.frame.size.height-44, self.view.frame.size.width, 44));
    if ([self areControlsHidden] && hide == false && animated) {
        _toolBar.frame = CGRectOffset(frame, 0, animationOffset);
    }
    [UIView animateWithDuration:animationDuration animations:^{
        CGFloat alpha = hide ? 0 : 1;
        self.navigationController.navigationBar.alpha = alpha;
        self.toolBar.frame = frame;
        if (hide) {
            self.toolBar.frame = CGRectOffset(self.toolBar.frame, 0, animationOffset);
        }
        self.sendButton.alpha = alpha;
    }];
}

- (BOOL)areControlsHidden {
    return _sendButton.alpha == 0;
}

- (BOOL)prefersStatusBarHidden {
    return _statusBarShouldBeHidden;
}

- (UIStatusBarAnimation)preferredStatusBarUpdateAnimation {
    return UIStatusBarAnimationSlide;
}

- (void)hideControlsHiden {
    [self setControliShidden:YES animated:YES];
}

- (void)toggleControls {
    [self setControliShidden:![self areControlsHidden] animated:YES];
}

#pragma mark - lazy

- (UICollectionView *)browserCollectionView {
    if (!_browserCollectionView) {
        UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
        layout.minimumInteritemSpacing = 0;
        layout.minimumLineSpacing = 0;
        layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        _browserCollectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(-10, 0, self.view.width+20, self.view.height) collectionViewLayout:layout];
        _browserCollectionView.backgroundColor = [UIColor blackColor];
        [_browserCollectionView registerClass:[SXBrowserCell class] forCellWithReuseIdentifier:browserCellReuseIdntifier];
        _browserCollectionView.delegate = self;
        _browserCollectionView.dataSource = self;
        _browserCollectionView.pagingEnabled = YES;
        _browserCollectionView.showsVerticalScrollIndicator = NO;
        _browserCollectionView.showsHorizontalScrollIndicator = NO;
    }
    return _browserCollectionView;
}

- (UIButton *)checkButton {
    if (!_checkButton) {
        _checkButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _checkButton.frame = CGRectMake(0, 0, 25, 25);
        [_checkButton setBackgroundImage:[UIImage imageBundleNamed:NaviSelected] forState:UIControlStateSelected];
        [_checkButton setBackgroundImage:[UIImage imageBundleNamed:NaviDeselected] forState:UIControlStateNormal];
        [_checkButton addTarget:self action:@selector(checkButtonAction) forControlEvents:UIControlEventTouchUpInside];
    }
    return _checkButton;
}

- (SXSendButton *)sendButton {
    if (!_sendButton) {
        _sendButton = [[SXSendButton alloc] initWithFrame:CGRectZero];
        [_sendButton addTarget:self action:@selector(sendButtonAction)];
    }
    return _sendButton;
}

@end
