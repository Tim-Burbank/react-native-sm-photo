
#import "SXImageFlowViewController.h"
#import <Photos/Photos.h>
#import "SXPhotoPickerController.h"
#import "SXAssetCell.h"
#import "SXSendButton.h"
#import "DXPickerHelper.h"
#import "PhotoModel.h"
#import "SXUtil.h"
#import "DownloadProgress.h"
#import "AlbumController.h"
#import "RecordVideoPreviewViewController.h"
#import "SXPhotoBrowser.h"
#import "UIViewController+PPNavigationBarPosition.h"
#import "UIView+Frame.h"
#import "UICollectionView+IndexPath.h"
#import "UIImage+Operation.h"

@interface SXImageFlowViewController ()<UIScrollViewDelegate, UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout, RecordVideoPreviewViewControllerDelegate, SXPhotoBrowserDelegate>
{
    CGFloat kThumbSizeLength;
    NSInteger selectedVideoDuration;
    BOOL isFullImage;
    CGRect previousPreheatRect;
    MediaType selectMediaType;
}

@property (nonatomic, strong) SXAlbum *currentAlbum;

@property (nonatomic, strong) NSArray *assetsArray;

@property (nonatomic, strong) NSMutableArray *selectedAssetsArray;

@property (nonatomic, strong) PHCachingImageManager *imageManager;

@property (nonatomic, strong) UICollectionView *imageFlowCollectionView;

@property (nonatomic, strong) SXSendButton *sendButton;

@property (nonatomic, copy) NSString *ablumIdentifier;

@property (nonatomic, strong) NSMutableArray *imageAssets;

@end

@implementation SXImageFlowViewController

static NSString *const dxAssetCellReuseIdentifier = @"dxAssetCellReuseIdentifier";

- (void)dealloc {
    _imageFlowCollectionView.dataSource = nil;
    _imageFlowCollectionView.delegate = nil;
}

-(void)initConfiguration {
    kThumbSizeLength = ([UIScreen mainScreen].bounds.size.width - 10) / 4;
    selectedVideoDuration = 0;
    isFullImage = NO;
    previousPreheatRect = CGRectZero;
    selectMediaType = MediaTypeAll;

}

- (instancetype)initWithAlbum:(SXAlbum *)album {
    if (self = [super init]) {
        [self initConfiguration];
        _currentAlbum = album;
        _assetsArray = [[NSMutableArray alloc] init];
        _selectedAssetsArray = [[NSMutableArray alloc] init];
    }
    return self;
}

- (instancetype)initWithIdentifier:(NSString *)identifier
{
    self = [super init];
    if (self) {
        [self initConfiguration];
        _assetsArray = [[NSMutableArray alloc] init];
        _selectedAssetsArray = [[NSMutableArray alloc] init];
        _ablumIdentifier = identifier;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupView];
    [self setupData];
}

- (void)setupView {
    self.view.backgroundColor = [UIColor whiteColor];
    [self createBarButtonItemAtPosition:PPNavigationBarPositionLeft normalImage:[UIImage imageBundleNamed:BackButton] highlightImage:[UIImage imageBundleNamed:BackButton] action:@selector(backButtonAction)];
    [self createBarButtonItemAtPosition:PPNavigationBarPositionRight text:[SXUtil SXLocalizedString:@"cancel" comment:@"取消"] action:@selector(cancelAction)];
    UIBarButtonItem *item1 = [[UIBarButtonItem alloc] initWithTitle:[SXUtil SXLocalizedString:@"preview" comment:@"预览"] style:UIBarButtonItemStylePlain target:self action:@selector(previewAction)];
    item1.tintColor = [UIColor whiteColor];
    item1.enabled = NO;
    UIBarButtonItem *item2 = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    UIBarButtonItem *item3 = [[UIBarButtonItem alloc] initWithCustomView:self.sendButton];
    UIBarButtonItem *item4 = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    item4.width = -10;
    [self setToolbarItems:@[item1, item2, item4, item3] animated:YES];
    [self.view addSubview:self.imageFlowCollectionView];
    NSDictionary *viewBindDict = @{@"imageFlowCollectionView" : _imageFlowCollectionView};
    NSString *vflH = @"H:|-0-[imageFlowCollectionView]-0-|";
    NSString *vflV = @"V:|-0-[imageFlowCollectionView]-0-|";
    NSArray *constraintsH = [NSLayoutConstraint constraintsWithVisualFormat:vflH options:0 metrics:nil views:viewBindDict];
    NSArray *constraintsV = [NSLayoutConstraint constraintsWithVisualFormat:vflV options:0 metrics:nil views:viewBindDict];
    [self.view addConstraints:constraintsH];
    [self.view addConstraints:constraintsV];

}

- (void)setupData {
    if (!_currentAlbum && _ablumIdentifier) {
        _currentAlbum = [DXPickerHelper fetchAlbum];
    }
    self.title = _currentAlbum.name;
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        weakSelf.assetsArray = [DXPickerHelper fetchImageAssetsViaCollectionResults:weakSelf.currentAlbum.results];
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.imageManager = [[PHCachingImageManager alloc] init];
            [weakSelf.imageFlowCollectionView reloadData];
            NSInteger item =  [weakSelf.imageFlowCollectionView numberOfItemsInSection:0];
            if (item == 0) {
                return ;
            }
            NSIndexPath *lastIndex = [NSIndexPath indexPathForItem:item-1 inSection:0];
            [weakSelf.imageFlowCollectionView scrollToItemAtIndexPath:lastIndex atScrollPosition:UICollectionViewScrollPositionBottom animated:NO];
        });
    });
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (SXPhotoPickerController.isAvatar) {
        self.navigationController.toolbarHidden = YES;
    } else {
        self.navigationController.toolbarHidden = NO;
    }
  _sendButton.badgeValue = [NSString stringWithFormat:@"%lu", (unsigned long)_selectedAssetsArray.count];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
//    [self updateCachedAssets];
}

- (void)viewWillDisappear:(BOOL)animated {
    self.navigationController.toolbarHidden = YES;
    [super viewWillDisappear:animated];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self resetCachedAssets];
    });
}

- (void)checkSelectedAssets {
    selectMediaType = MediaTypeAll;
    PHAsset *asset = [_selectedAssetsArray firstObject];
    if (asset) {
        if (asset.mediaType == PHAssetMediaTypeVideo) {
            selectMediaType = MediaTypeVideo;
        } else {
            selectMediaType = MediaTypePhoto;
        }
    }
    [self checkCellSelectable:_imageFlowCollectionView.visibleCells];
}

- (void)checkCellSelectable:(NSArray *)cells {
    for (SXAssetCell *cell in cells) {
        if (cell.asset) {
            if (selectMediaType == MediaTypeVideo && _selectedAssetsArray.count > 0 && ![_selectedAssetsArray containsObject:cell.asset]) {
                cell.selectable = NO;
            } else {
                cell.selectable = YES;
                if (cell.asset.mediaType == PHAssetMediaTypeVideo && selectMediaType == MediaTypePhoto) {
                    cell.selectable = NO;
                } else if (cell.asset.mediaType == PHAssetMediaTypeImage && selectMediaType == MediaTypeVideo){
                    cell.selectable = NO;
                }
            }
        }
        if (cell.assetSelected) {
            [cell checkButton:[_selectedAssetsArray indexOfObject:cell.asset]];
        } else {
            [cell checkButton:-1];
        }
    }
}

- (void)sendImage {
    SXPhotoPickerController *picker = (SXPhotoPickerController *)self.navigationController;
    // 点击完成判断最少选择参数显示弹窗
    if (selectMediaType != MediaTypeVideo &&
        SXPhotoPickerController.minSelectedNumber > 0 &&
        _selectedAssetsArray.count < SXPhotoPickerController.minSelectedNumber) {
        [self showTips:[SXUtil SXLocalizedString:@"alertTipContent" comment:@""] selectNumber:@(SXPhotoPickerController.minSelectedNumber)];
        return;
    }
    if (picker) {
        [DXPickerHelper saveIndentifier:self.currentAlbum.name];
        NSLog(@"current name -- %@", self.currentAlbum.name);
    }

    if (selectedVideoDuration > (int)[PhotoModel shared].VideoDuration) {
        [SXImageFlowViewController showAlert:self defaultHandler:^{
            [self sendThem:picker];
        }];
    } else {
        [self sendThem:picker];
    }
}

- (void)sendThem:(SXPhotoPickerController *)picker {
    [DXPickerHelper saveIndentifier:_currentAlbum.identifier];
    [picker.photoPickerDelegate photoPickerController:picker sendImages:_selectedAssetsArray isFullImage:isFullImage];
}

- (void)sendThem:(SXPhotoPickerController *)picker sendAssets:(NSArray *)assets {
    [DXPickerHelper saveIndentifier:_currentAlbum.identifier];
    [picker.photoPickerDelegate photoPickerController:picker sendImages:assets isFullImage:isFullImage];
}

- (void)resetCachedAssets {
    [_imageManager stopCachingImagesForAllAssets];
    previousPreheatRect = CGRectZero;
}

- (void)updateCachedAssets {
    BOOL isViewVisible = self.viewLoaded && self.view.window != nil;
    if (!isViewVisible) {
        return ;
    }
    CGRect preheatRect = _imageFlowCollectionView.bounds;
    preheatRect = CGRectInset(preheatRect, 0.0, -0.5*preheatRect.size.height);
    CGFloat delta = fabs(CGRectGetMidY(preheatRect)-CGRectGetMidY(previousPreheatRect));
    if (delta > _imageFlowCollectionView.bounds.size.height/3) {
        NSMutableArray *addedIndexPaths = [[NSMutableArray alloc] init];
        NSMutableArray *removedIndexPaths = [[NSMutableArray alloc] init];
        [self computeDifferenceBetweenRect:previousPreheatRect nweRect:preheatRect removeHandler:^(CGRect removeRect) {
            NSArray *indexPaths = [self.imageFlowCollectionView aapl_indexPathsForElementsInRect:removeRect];
            if (indexPaths) {
                [removedIndexPaths addObjectsFromArray:indexPaths];
            }
        } addHandler:^(CGRect addRect) {
            NSArray *indexPaths = [self.imageFlowCollectionView aapl_indexPathsForElementsInRect:addRect];
            if (indexPaths) {
                [addedIndexPaths addObjectsFromArray:indexPaths];
            }
        }];
        NSArray *assetsToStartCaching = [self assetsIndexPaths:addedIndexPaths];
        NSArray *assetsToStopCaching = [self assetsIndexPaths:removedIndexPaths];
        PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
        options.resizeMode = PHImageRequestOptionsResizeModeExact;
        CGFloat scale = UIScreen.mainScreen.scale;
        CGSize size = CGSizeMake(kThumbSizeLength*scale, kThumbSizeLength*scale);
        if (assetsToStartCaching && assetsToStartCaching.count>0) {
            [_imageManager startCachingImagesForAssets:assetsToStartCaching targetSize:size contentMode:PHImageContentModeAspectFill options:options];
        }
        if (assetsToStopCaching && assetsToStopCaching.count>0) {
            [_imageManager stopCachingImagesForAssets:assetsToStopCaching targetSize:size contentMode:PHImageContentModeAspectFill options:options];
        }
        previousPreheatRect = preheatRect;
    }
}

- (void)computeDifferenceBetweenRect:(CGRect)oldRect nweRect:(CGRect)newRect removeHandler:(void(^)(CGRect removeRect))removeH addHandler:(void(^)(CGRect addRect))addH {
    CGFloat oldMaxY = CGRectGetMaxX(oldRect);
    CGFloat oldMinY = CGRectGetMinY(oldRect);
    CGFloat newMaxY = CGRectGetMaxY(newRect);
    CGFloat newMinY = CGRectGetMinY(newRect);
    if (CGRectIntersectsRect(newRect, oldRect)) {
        if (newMaxY > oldMaxY) {
            CGRect rectToAdd = CGRectMake(newRect.origin.x, oldMaxY, newRect.size.width, newMaxY-oldMaxY);
            addH(rectToAdd);
        }
        if (oldMinY > newMinY) {
            CGRect rectToRemove = CGRectMake(newRect.origin.x, newMinY, newRect.size.width, oldMinY-newMinY);
            addH(rectToRemove);
        }
        if (newMaxY < oldMaxY) {
            CGRect rectToRemove = CGRectMake(newRect.origin.x, newMaxY, newRect.size.width, oldMaxY -newMaxY);
            removeH(rectToRemove);
        }
        if (oldMinY < newMinY) {
            CGRect rectToRemove = CGRectMake(newRect.origin.x, oldMinY, newRect.size.width, newMinY-oldMinY);
            removeH(rectToRemove);
        }
    } else {
        addH(newRect);
        removeH(oldRect);
    }
}

#pragma mark - tap events

- (void)backButtonAction {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)cancelAction {
    SXPhotoPickerController *nav = (SXPhotoPickerController *)self.navigationController;
    [nav.photoPickerDelegate photoPickerDidCancel:nav];
}

- (void)previewAction {
    [self browserPhotoAssets:_selectedAssetsArray pageIndex:0];
}

- (void)browserPhotoAssets:(NSArray *)assets pageIndex:(NSInteger)index {
    PHAsset *originAsset = assets[index];
    if (originAsset.duration > 1) {
        DownloadAlert *progressView = [[DownloadAlert alloc] initWithFrame: UIScreen.mainScreen.bounds];
        progressView.title = [SXUtil SXLocalizedString:@"iCloudVideo" comment:@""];
        progressView.subTitle = @"";
        progressView.hidden = YES;
        [self.navigationController.view addSubview:progressView];
        PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
        [options setNetworkAccessAllowed:YES];
        options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
        options.progressHandler = ^(double progress, NSError * _Nullable error, BOOL * _Nonnull stop, NSDictionary * _Nullable info) {
            if (info) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    progressView.hidden = NO;
                    NSNumber *key = info[PHImageResultRequestIDKey];
                    progressView.progress = progress;
                    if (error) {
                        [[PHImageManager defaultManager] cancelImageRequest:(int)key.integerValue];
                        [AlbumController showErrorAlert:self error:[SXUtil SXLocalizedString:@"NetworkNotWork" comment:@""]];
                    }
                });
            }
        };
        [[PHImageManager defaultManager] requestAVAssetForVideo:originAsset options:options resultHandler:^(AVAsset * _Nullable asset, AVAudioMix * _Nullable audioMix, NSDictionary * _Nullable info) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (asset) {
                    NSInteger duration = CMTimeGetSeconds(asset.duration);
                    AVPlayerItem *item = [AVPlayerItem playerItemWithAsset:asset];
                    RecordVideoPreviewViewController *cc = [[RecordVideoPreviewViewController alloc] init];
                    cc.assset = originAsset;
                    cc.player = [[AVPlayer alloc] initWithPlayerItem:item];
                    cc.delegate = self;
                    cc.duration = duration;
                    if (self.navigationController) {
                        [progressView removeFromSuperview];
                        [self.navigationController pushViewController:cc animated:YES];
                    }
                }
            });
        }];
    } else {
        SXPhotoBrowser *browser = [[SXPhotoBrowser alloc] initWithPhotos:assets currentIndex:index isFullImage:isFullImage];
        browser.delegate = self;
        browser.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:browser animated:YES];
    }
}

- (BOOL)addAsset:(PHAsset *)asset {
    if (_selectedAssetsArray.count >= SXPhotoPickerController.maxSelectedNumber) {
        [self showTips:[SXUtil SXLocalizedString:@"alertContent" comment:@""] selectNumber:@(SXPhotoPickerController.maxSelectedNumber)];
        return NO;
    }

    if ([_selectedAssetsArray containsObject:asset]) {
        return NO;
    }

    if ([PhotoModel shared].SelectGIF != GIFTYPEUnuse) {
        [DXPickerHelper gifCanuse:asset resultHandler:^(BOOL canUse) {
            if (!canUse) {
                [self.selectedAssetsArray removeObject: asset];
              self.sendButton.badgeValue = [NSString stringWithFormat:@"%lu", (unsigned long)self.selectedAssetsArray.count];
                if (self.selectedAssetsArray.count == 0) {
                    [self.toolbarItems firstObject].enabled = NO;
                }
                [self checkSelectedAssets];
                [self showLimit];
            }
        }];
        [self appendAsset:asset];
        return YES;
    } else {
        [self appendAsset:asset];
        return YES;
    }
}

- (void)appendAsset:(PHAsset *)asset {
    [_selectedAssetsArray addObject:asset];
    _sendButton.badgeValue = [NSString stringWithFormat:@"%lu", (unsigned long)_selectedAssetsArray.count];
    if (_selectedAssetsArray.count > 0) {
        [self.toolbarItems firstObject].enabled = YES;
    }
    [self checkSelectedAssets];
    if (SXPhotoPickerController.isAvatar) {
        [self sendImage];
    }
}

- (BOOL)deleteAsset:(PHAsset *)asset {
    if ([_selectedAssetsArray containsObject:asset]) {
        NSInteger index = [_selectedAssetsArray indexOfObject:asset];
        if (index > _selectedAssetsArray.count - 1) {
            return NO;
        }
        [_selectedAssetsArray removeObjectAtIndex:index];
      _sendButton.badgeValue = [NSString stringWithFormat:@"%lu", (unsigned long)_selectedAssetsArray.count];
        if (_selectedAssetsArray.count <= 0) {
            [self.toolbarItems firstObject].enabled = NO;
        }
        [self checkSelectedAssets];
    }
    return NO;
}

- (void)showLimit {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"GifLimit" object:nil];
    NSString *alertString = [NSString stringWithFormat:[SXUtil SXLocalizedString:@"GifLimit" comment:@""], [DXPickerHelper limitSizeString]];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"alertTitle" comment:@""] message:alertString preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"alertTitle" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [alert dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:action];
    [self.navigationController presentViewController:alert animated:YES completion:nil];
}

- (void)showTips:(NSString *)tipTextContent selectNumber:(NSNumber *)selectNumber {
    NSString *alertString = [NSString stringWithFormat:tipTextContent, selectNumber];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"alertTitle" comment:@""] message:alertString preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"alertButton" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [alert dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:action];
    [self.navigationController presentViewController:alert animated:YES completion:nil];
}

- (void)displayImageInCell:(SXAssetCell *)cell indexPath:(NSIndexPath *)indexPath {
    PHAsset *asset = _assetsArray[indexPath.row];
    NSInteger index = [_selectedAssetsArray indexOfObject:asset];
    if (index > _selectedAssetsArray.count) {
        index = -1;
    }
    [cell fillWithAsset:asset selectIndex:index];
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    options.resizeMode = PHImageRequestOptionsResizeModeExact;
    CGFloat scale = UIScreen.mainScreen.scale;
    CGSize size = CGSizeMake(kThumbSizeLength*scale, kThumbSizeLength*scale);
    [_imageManager requestImageForAsset:_assetsArray[indexPath.row] targetSize:size contentMode:PHImageContentModeAspectFill options:options resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
        dispatch_async(dispatch_get_main_queue(), ^{
            cell.imageView.image = result;
        });
    }];
    if (asset.mediaType == PHAssetMediaTypeVideo) {
        [_imageManager requestAVAssetForVideo:asset options:nil resultHandler:^(AVAsset * _Nullable asset1, AVAudioMix * _Nullable audioMix, NSDictionary * _Nullable info) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (asset1) {
                    Float64 current = CMTimeGetSeconds(asset1.duration);
                    int cm = (int)(current/60.0);
                    int cs = (int)(current - (Float64)(60*cm) + 0.5);
                    int d = cm * 60 + cs;
                    [cell setDuration:[NSString stringWithFormat:@"%.2d:%.2d", cm, cs] duration:d];
//                    cell.selectable = d >= 1;
                }
            });
        }];
    } else {
        [cell setDuration:@"00:00" duration:0];
    }

    [self checkCellSelectable:@[cell]];
    __weak typeof(self) weakSelf = self;
    cell.selectItemBlock = ^NSInteger(BOOL selectItem, PHAsset * _Nonnull asset, SXAssetCell * _Nonnull cell) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (selectItem == YES) {
            BOOL r = [strongSelf addAsset:asset];
            strongSelf->selectedVideoDuration = cell.videoDuration;
            if (r) {
                return strongSelf.selectedAssetsArray.count - 1;
            } else {
                return -1;
            }
        } else {
            [strongSelf deleteAsset:asset];
            strongSelf->selectedVideoDuration  = 0;
            return -1;
        }
    };
}

- (void)sendImagesFromPhotoBrowser:(SXPhotoBrowser *)photoBrowser currentAsset:(PHAsset *)ca {
    [self sendImage];
}

- (NSInteger)selectedPhotosNumberInPhotoBrowser:(SXPhotoBrowser *)photoBrowser {
    return _selectedAssetsArray.count;
}

- (BOOL)photoBrowser:(SXPhotoBrowser *)photoBrowser currentPhotoAssetIsSelected:(PHAsset *)asset {
    if (asset) {
        return [_selectedAssetsArray containsObject:asset];
    }
    return NO;
}

- (BOOL)photobrowser:(SXPhotoBrowser *)photoBrowser selectedAssset:(PHAsset *)asset {
    if (!asset) {
        return NO;
    }
    NSInteger index = [_assetsArray indexOfObject:asset];
    if (index < 0 || index > _assetsArray.count) {
        return NO;
    }
    BOOL success = [self addAsset:asset];
    [_imageFlowCollectionView reloadItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:index inSection:0]]];
    return success;
}

- (void)photoBrowser:(SXPhotoBrowser *)photoBrowser deselectedAsset:(PHAsset *)asset {
    if (asset) {
        NSInteger index = [_assetsArray indexOfObject:asset];
        if (index >_assetsArray.count) {
            return ;
        }
        [self deleteAsset:asset];
        [_imageFlowCollectionView reloadItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:index inSection:0]]];
    }
}

- (void)photoBrowser:(SXPhotoBrowser *)photoBrowser selectedFullImage:(BOOL)isfull {
    isFullImage = isfull;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return _assetsArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    SXAssetCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:dxAssetCellReuseIdentifier forIndexPath:indexPath];
    [self displayImageInCell:cell indexPath:indexPath];
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(kThumbSizeLength, kThumbSizeLength);
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(2, 2, 2, 2);
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    PHAsset *asset = [_selectedAssetsArray firstObject];
    if (_selectedAssetsArray.count > 0 && asset.mediaType != PHAssetMediaTypeVideo) {
        NSInteger currentIndex = [self.imageAssets indexOfObject:[_assetsArray objectAtIndex:indexPath.row]];
        [self browserPhotoAssets:_imageAssets pageIndex:currentIndex];
    } else {
        [self browserPhotoAssets:_assetsArray pageIndex:indexPath.row];
    }
}

- (NSArray *)assetsIndexPaths:(NSArray *)indexPaths {
    if (indexPaths.count == 0) {
        return nil;
    }
    NSMutableArray *assets = [[NSMutableArray alloc] init];
    for (NSInteger index=0; index<indexPaths.count; index++) {
        NSIndexPath *indexPath = indexPaths[index];
        PHAsset *asset = _assetsArray[indexPath.item];
        [assets addObject:asset];
    }
    return assets;
}

- (void)confirmVideo:(NSString *)localPath thumbNailPath:(NSString *)tnp duration:(double)time {
    SXPhotoPickerController *picker = (SXPhotoPickerController *)self.navigationController;
    if (picker) {
        [DXPickerHelper saveIndentifier: _currentAlbum.identifier];
        [picker.photoPickerDelegate didSendLocalVideo:picker localPath:localPath thumbnailPath:tnp duration:(int)time];
    }
}

- (void)confirmVideo:(NSURL *)url {

}

- (void)confirAsset:(PHAsset *)asset {
    SXPhotoPickerController *picker = (SXPhotoPickerController *)self.navigationController;
    [self sendThem:picker sendAssets:@[asset]];
}

+ (void)showAlert:(UIViewController *)viewController defaultHandler:(void (^)(void))hanlder {
    NSString *message = @"";
    if ([PhotoModel shared].VideoDuration < 60) {
        message = [NSString stringWithFormat:[SXUtil SXLocalizedString:@"VideoAlertSeconds" comment:@""], (int)[PhotoModel shared].VideoDuration];
    } else {
        message = [NSString stringWithFormat:[SXUtil SXLocalizedString:@"VideoAlertMinutes" comment:@""], (int)[PhotoModel shared].VideoDuration/60];
    }
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"VideoTooLong" comment:@""] message:message preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"Cancel" comment:@""] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
    }]];

    [alert addAction:[UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"OK" comment:@""] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        hanlder();
    }]];
    [viewController presentViewController:alert animated:YES completion:nil];
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
//    [self updateCachedAssets];
    [self checkSelectedAssets];
}

#pragma mark - lazy

- (UICollectionView *)imageFlowCollectionView {
    if (!_imageFlowCollectionView) {
        UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
        flowLayout.minimumLineSpacing = 2.0;
        flowLayout.minimumInteritemSpacing = 2.0;
        flowLayout.scrollDirection = UICollectionViewScrollDirectionVertical;
        UICollectionView *collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:flowLayout];
        collectionView.translatesAutoresizingMaskIntoConstraints = NO;
        collectionView.delegate = self;
        collectionView.dataSource = self;
        collectionView.alwaysBounceVertical = YES;
        collectionView.showsHorizontalScrollIndicator = YES;
        [collectionView registerClass:[SXAssetCell class] forCellWithReuseIdentifier:dxAssetCellReuseIdentifier];
        _imageFlowCollectionView = collectionView;
    }
    return _imageFlowCollectionView;
}

- (SXSendButton *)sendButton {

    if (!_sendButton) {
        _sendButton = [[SXSendButton alloc] initWithFrame:CGRectZero];
        [_sendButton sizeToFit];
        [_sendButton addTarget:self action:@selector(sendImage)];
    }
    return _sendButton;
}

- (NSMutableArray *)imageAssets {
    if (!_imageAssets) {
        NSMutableArray *marr = [[NSMutableArray alloc] init];
        for (NSInteger index = 0; index < _assetsArray.count; index++) {
            PHAsset *asset = _assetsArray[index];
            if (asset.mediaType != MediaTypeVideo) {
                [marr addObject:asset];
            }
        }
        _imageAssets = marr;
    }
    return _imageAssets;
}

@end
