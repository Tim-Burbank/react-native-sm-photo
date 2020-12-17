
#import "SXBrowserCell.h"
#import "UIView+Frame.h"
#import "SXTapDetectingImageView.h"
#import "Hyphenate.h"
#import "DownloadProgress.h"
#import "DXPickerHelper.h"
#import "UIImage+Operation.h"
#import "SXUtil.h"

@interface SXBrowserCell ()<UIScrollViewDelegate, SXTapDetectingImageViewDelegate>


@property (nonatomic, assign) PHImageRequestID requestId;

@property (nonatomic, strong) UIScrollView *zoomingScrollView;

@property (nonatomic, strong) SXTapDetectingImageView *photoImageView;

@property (nonatomic, strong) UIActivityIndicatorView *loadingIndicator;

@property (nonatomic, strong) UIButton *playBUtton;

@end

@implementation SXBrowserCell

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self setupView];
    }
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)coder
{
    self = [super initWithCoder:coder];
    if (self) {
        [self setupView];
    }
    return self;
}

- (void)setAssset:(PHAsset *)assset {
    _assset = assset;
    [self displayImage];
}

- (void)prepareForReuse {
    [super prepareForReuse];
    self.photoImageView.image = nil;
    _photoImageView.animationImages = nil;
    if (self.requestId) {
        [[PHImageManager defaultManager] cancelImageRequest:_requestId];
        _requestId = nil;
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.photoImageView.center = CGPointMake(self.photoImageView.width/2, self.photoImageView.height/2);
    // Center the image as it becomes smaller than the size of the screen
    CGSize boundSize = self.zoomingScrollView.frame.size;
    CGRect frameToCenter = _photoImageView.frame;
    
    // Horizontally
    if (frameToCenter.size.width < boundSize.width) {
        frameToCenter.origin.x = (boundSize.width - frameToCenter.size.width) / 2.0;
    } else {
        frameToCenter.origin.x = 0;
    }
    
    // Vertically
    if (frameToCenter.size.height < boundSize.height) {
        frameToCenter.origin.y = (boundSize.height - frameToCenter.size.height) / 2.0;
    } else {
        frameToCenter.origin.y = 0;
    }
    
    // center
    if (!CGRectEqualToRect(_photoImageView.frame, frameToCenter)) {
        _photoImageView.frame = frameToCenter;
    }
}

#pragma mark - methods

- (void)play {
    if (self.assset) {
        UIViewController *vc = [Hyphenate getRootViewController];
        DownloadAlert *progressView = [[DownloadAlert alloc] initWithFrame:UIScreen.mainScreen.bounds];
        progressView.hidden = NO;
        [vc.navigationController.view addSubview:progressView];
        PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
        [options setNetworkAccessAllowed:YES];
        [[PHImageManager defaultManager] requestPlayerItemForVideo:_assset options:options resultHandler:^(AVPlayerItem * _Nullable playerItem, NSDictionary * _Nullable info) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [progressView removeFromSuperview];
                AVPlayer *player = [[AVPlayer alloc] initWithPlayerItem:playerItem];
                AVPlayerViewController *playerViewController = [[AVPlayerViewController alloc] init];
                playerViewController.player = player;
                playerViewController.modalPresentationStyle = UIModalPresentationFullScreen;
                [vc presentViewController:playerViewController animated:YES completion:^{
                    [playerViewController.player play];
                }];
            });
        }];
        
    }
}

- (void)setupView {
    [self.contentView addSubview:self.zoomingScrollView];
    [_zoomingScrollView addSubview:self.photoImageView];
}

- (void)displayImage {
    _zoomingScrollView.maximumZoomScale = 1;
    _zoomingScrollView.minimumZoomScale = 1;
    _zoomingScrollView.zoomScale = 1;
    _zoomingScrollView.contentSize = CGSizeMake(0, 0);
    _photoImageView.frame = _zoomingScrollView.bounds;
    [self.loadingIndicator startAnimating];
    [self addSubview:_loadingIndicator];
    self.loadingIndicator.center = CGPointMake(self.frame.size.width/2, self.frame.size.height/2);
    self.playBUtton.hidden = _assset.mediaType == PHAssetMediaTypeVideo ? NO : YES;
    __weak typeof(self) weakSelf = self;
    _requestId = [DXPickerHelper fetchImageWithAsset:_assset targetSize:_zoomingScrollView.size needHighQuality:YES imageResultHandler:^(UIImage * _Nonnull image, NSArray<UIImage *> * _Nonnull images, NSTimeInterval duration) {
        if (!image) {
            return ;
        }
        weakSelf.photoImageView.image = image;
        // 实况预览不能这样处理
        if (images && images.count > 1) {
            weakSelf.photoImageView.animationImages = images;
            weakSelf.photoImageView.animationDuration = duration;
            weakSelf.photoImageView.animationRepeatCount = 0;
            [weakSelf.photoImageView startAnimating];
        }
        weakSelf.photoImageView.hidden = NO;
        CGRect photoImageViewFrame = CGRectZero;
        photoImageViewFrame.origin = CGPointZero;
        photoImageViewFrame.size = image.size;
        weakSelf.photoImageView.frame = photoImageViewFrame;
        weakSelf.zoomingScrollView.contentSize = photoImageViewFrame.size;
        [weakSelf setMaxMinZoomScalesForCurrentBounds];
        [weakSelf setNeedsLayout];
        [weakSelf.loadingIndicator stopAnimating];
    }];
}

- (void)setMaxMinZoomScalesForCurrentBounds {
    _zoomingScrollView.maximumZoomScale = 1;
    _zoomingScrollView.minimumZoomScale = 1;
    _zoomingScrollView.zoomScale = 1;
    if (!_photoImageView.image) {
        return ;
    }
    _photoImageView.frame = CGRectMake(0, 0, _photoImageView.frame.size.width, _photoImageView.frame.size.height);
    CGSize boundSize = _zoomingScrollView.bounds.size;
    CGSize imageSize = _photoImageView.image.size;
    CGFloat xScale = boundSize.width / imageSize.width;
    CGFloat yScale = boundSize.height / imageSize.height;
    CGFloat minScale = xScale < yScale ? xScale : yScale;
    CGFloat maxScale = 1.5;
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        maxScale = 3;
    }
    
    if (xScale >= 1 && yScale >= 1) {
        minScale = 1.0;
    }
    
    _zoomingScrollView.maximumZoomScale = maxScale;
    _zoomingScrollView.minimumZoomScale = minScale;
    _zoomingScrollView.zoomScale = [self initialZoomScaleWidthMinScale];
    [self setNeedsLayout];
}

- (CGFloat)initialZoomScaleWidthMinScale {
    CGFloat zoomScale = _zoomingScrollView.minimumZoomScale;
    CGSize boundsSize = _zoomingScrollView.bounds.size;
    CGSize imageSize = _photoImageView.image.size;
    CGFloat boundsAR = boundsSize.width / boundsSize.height;
    CGFloat imageAR = imageSize.width / imageSize.height;
    CGFloat xScale = boundsSize.width / imageSize.width;
    CGFloat yScale = boundsSize.height / imageSize.height;
    if (ABS(boundsAR-imageAR) < 0.17) {
        zoomScale = xScale > yScale ? xScale : yScale;
        CGFloat tempScale = _zoomingScrollView.minimumZoomScale > zoomScale ? _zoomingScrollView.minimumZoomScale : zoomScale;
        zoomScale = tempScale < _zoomingScrollView.maximumZoomScale ? tempScale : _zoomingScrollView.maximumZoomScale;
    }
    return zoomScale;
}

#pragma mark - delegate

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return _photoImageView;
}

- (void)scrollViewWillBeginZooming:(UIScrollView *)scrollView withView:(UIView *)view {
    _zoomingScrollView.scrollEnabled = YES;
}

- (void)scrollViewDidZoom:(UIScrollView *)scrollView {
    [self setNeedsLayout];
    [self layoutIfNeeded];
}

- (void)handleSingleTap:(CGPoint)touchPoint {
    if (self.photoBrowser) {
        [_photoBrowser performSelector:NSSelectorFromString(@"toggleControls") withObject:nil afterDelay:0.2];
    }
}

- (void)imageView:(SXTapDetectingImageView *)imageView singleTapDetected:(UITouch *)touch {
    [self handleSingleTap:[touch locationInView:imageView]];
}

- (void)handleDoubleTap:(CGPoint)touchPoint {
    if (self.photoBrowser) {
        [NSObject cancelPreviousPerformRequestsWithTarget:_photoBrowser];
        if (_zoomingScrollView.zoomScale != _zoomingScrollView.minimumZoomScale && _zoomingScrollView.zoomScale != [self initialZoomScaleWidthMinScale]) {
            [_zoomingScrollView setZoomScale:_zoomingScrollView.minimumZoomScale animated:YES];
        } else {
            CGFloat newZoomScale = (_zoomingScrollView.maximumZoomScale + _zoomingScrollView.minimumZoomScale) / 2;
            CGFloat xSize = _zoomingScrollView.frame.size.width / newZoomScale;
            CGFloat ySize = _zoomingScrollView.frame.size.height / newZoomScale;
            [_zoomingScrollView zoomToRect:CGRectMake(touchPoint.x-xSize/2, touchPoint.y-ySize/2, xSize, ySize) animated:YES];
        }
    }
}

- (void)imageView:(SXTapDetectingImageView *)imageView doubleTapDetected:(UITouch *)touch {
    [self handleDoubleTap:[touch locationInView:imageView]];
}

#pragma mark - lazy

- (UIScrollView *)zoomingScrollView {
    if (!_zoomingScrollView) {
        _zoomingScrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(10, 0, self.width-20, self.height)];
        _zoomingScrollView.delegate = self;
        _zoomingScrollView.showsVerticalScrollIndicator = NO;
        _zoomingScrollView.showsHorizontalScrollIndicator = NO;
        _zoomingScrollView.decelerationRate = UIScrollViewDecelerationRateFast;
    }
    return _zoomingScrollView;
}

- (SXTapDetectingImageView *)photoImageView {
    if (!_photoImageView) {
        _photoImageView = [[SXTapDetectingImageView alloc] initWithFrame:CGRectZero];
        _photoImageView.tapDelegate = self;
        _photoImageView.contentMode = UIViewContentModeScaleAspectFit;
        _photoImageView.backgroundColor = [UIColor blackColor];
    }
    return _photoImageView;
}

- (UIActivityIndicatorView *)loadingIndicator {
    if (!_loadingIndicator) {
        _loadingIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
        _loadingIndicator.hidesWhenStopped = YES;
    }
    return _loadingIndicator;
}

- (UIButton *)playBUtton {
    if (!_playBUtton) {
        _playBUtton = [UIButton buttonWithType:UIButtonTypeCustom];
        [self addSubview:_playBUtton];
        _playBUtton.frame = CGRectMake(0, 0, 72, 72);
        [_playBUtton setImage:[UIImage imageBundleNamed:@"cell_play_button"] forState:UIControlStateNormal];
        _playBUtton.center = CGPointMake(self.bounds.size.width/2, self.bounds.size.height/2);
        [_playBUtton addTarget:self action:@selector(play) forControlEvents:UIControlEventTouchUpInside];
    }
    return _playBUtton;
}

@end
