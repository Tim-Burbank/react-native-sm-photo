
#import <UIKit/UIKit.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@interface AssetCheckButton : UIButton

@property (nonatomic, assign) NSInteger order;

- (void)checkBackground;

@end

@class SXAssetCell;
typedef NSInteger (^SelectItemBlock)(BOOL selectItem, PHAsset *asset, SXAssetCell *cell);

@interface SXAssetCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *imageView;

@property (nonatomic, assign) BOOL assetSelected;

@property (nonatomic, assign) BOOL selectable;

@property (nonatomic, strong) PHAsset *asset;

@property (nonatomic, assign) NSInteger videoDuration;

@property (nonatomic, strong) NSNumber *videoDurationN;

@property (nonatomic, copy) SelectItemBlock selectItemBlock;

- (void)checkButton:(NSInteger)selectIndex;

- (void)fillWithAsset:(PHAsset *)asset selectIndex:(NSInteger)index;

- (void)setDuration:(NSString *)time duration:(int)d;

@end



NS_ASSUME_NONNULL_END
