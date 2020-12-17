
#import "SXAssetCell.h"
#import "UIColor+StringColor.h"
#import "UIImage+Operation.h"
#import "SXUtil.h"

@interface AssetCheckButton ()

@property (nonatomic, strong) UIImageView *bottom;

@property (nonatomic, strong) UIImageView *top;

@property (nonatomic, strong) UILabel *label;

@end

@implementation AssetCheckButton

- (void)setOrder:(NSInteger)order {
    _order = order;
    if (_order < 0) {
        self.bottom.hidden = YES;
        self.top.hidden = YES;
        self.label.hidden = YES;
    } else if (order == 0) {
        self.bottom.hidden = NO;
        self.top.hidden = NO;
        self.label.hidden = YES;
    } else {
        self.bottom.hidden = NO;
        self.top.hidden = YES;
        self.label.hidden = NO;
        self.label.text = [NSString stringWithFormat:@"%ld", _order];
    }
}

-(void)checkBackground {
    _bottom = [[UIImageView alloc] initWithImage:[UIImage imageBundleNamed:photo_check_selected]];
    _top = [[UIImageView alloc] initWithImage:[UIImage imageBundleNamed:photo_check_selected_check]];
    _label = [[UILabel alloc] init];

    UIImageView *i = [[UIImageView alloc] initWithImage:[UIImage imageBundleNamed:photo_check_default]];
    i.center = CGPointMake(self.frame.size.width-12, self.frame.size.height-30);
    [self addSubview:i];
    [i addSubview:_bottom];
    _bottom.center = CGPointMake(i.frame.size.width/2, i.frame.size.height/2);
    [i addSubview:_top];
    _top.center = CGPointMake(i.frame.size.width/2, i.frame.size.height/2);
    [i addSubview:_label];
    _label.textColor = [UIColor blackColor];
    _label.font = [UIFont systemFontOfSize:12];
    _label.frame = i.bounds;
    _label.textAlignment = NSTextAlignmentCenter;
}

@end

@interface SXAssetCell ()

@property (nonatomic, strong) UIImageView *videaoOverlay;

@property (nonatomic, strong) CAGradientLayer *gradient;

@property (nonatomic, strong) UILabel *duration;

@property (nonatomic, strong) UIImageView *videoIcon;

@property (nonatomic, strong) UIImageView *selectMask;

@property (nonatomic, strong) AssetCheckButton *checkButton;

@end

@implementation SXAssetCell

-(instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setupView];
    }
    return self;
}

- (void)setupView {
    [self.contentView addSubview:self.imageView];
    [self.contentView addSubview:self.videaoOverlay];
    [self.contentView addSubview:self.checkButton];

    NSDictionary *viewBindingsDict = @{
                                       @"posterImageView":self.imageView
                                       };
    NSString *imageViewVFLV = @"V:|-0-[posterImageView]-0-|";
    NSString *imageViewVFLH = @"H:|-0-[posterImageView]-0-|";
    NSArray *imageViewContraintsV = [NSLayoutConstraint constraintsWithVisualFormat:imageViewVFLV options:NSLayoutFormatAlignAllLastBaseline metrics:nil views:viewBindingsDict];
    NSArray *imageViewContraintsH = [NSLayoutConstraint constraintsWithVisualFormat:imageViewVFLH options:NSLayoutFormatAlignAllLastBaseline metrics:nil views:viewBindingsDict];
    [self addConstraints:imageViewContraintsH];
    [self addConstraints:imageViewContraintsV];
}

- (void)prepareForReuse {
    self.assetSelected = NO;
    if (self.imageView.image) {
        self.imageView.image = nil;
    }
    if (self.asset) {
        self.asset = nil;
    }
}

- (void)fillWithAsset:(PHAsset *)asset selectIndex:(NSInteger)index {
    self.asset = asset;
    [self checkButton: index];
    if (self.asset.mediaType == PHAssetMediaTypeVideo) {
        if (self.gradient == nil) {
            self.gradient = [[CAGradientLayer alloc] init];
            self.gradient.frame = CGRectMake(0, self.videaoOverlay.frame.size.height-20, self.videaoOverlay.frame.size.width, 20);
            self.gradient.colors = @[(__bridge id)[UIColor pp_colorWithHexString:@"00000000"].CGColor, (__bridge id)[UIColor pp_colorWithHexString:@"0000007f"].CGColor];

            self.duration = [[UILabel alloc] initWithFrame:CGRectMake(5, self.videaoOverlay.frame.size.height-20, self.videaoOverlay.frame.size.width-10, 20)];
            self.duration.textColor = [UIColor whiteColor];
            self.duration.textAlignment = NSTextAlignmentRight;
            self.duration.text = @"00:00";
            self.duration.font = [UIFont systemFontOfSize:11];
            [self.videaoOverlay addSubview:self.duration];

            self.videoIcon = [[UIImageView alloc] initWithImage:[UIImage imageBundleNamed:videoicon]];
            [self.videaoOverlay addSubview:self.videoIcon];
            self.videoIcon.center = CGPointMake(12, self.videaoOverlay.frame.size.height-20);
        }
        if (self.asset.duration) {
            double dd = round(self.asset.duration);
            self.duration.text = [NSString stringWithFormat:@"%02d:%02d", [self getMinute:dd], [self getSecond:dd]];
            self.videoDuration = dd;
        }
        self.gradient.hidden = NO;
        self.duration.hidden = NO;
        self.videoIcon.hidden = NO;
    } else {
        self.gradient.hidden = YES;
        self.duration.hidden = YES;
        self.videoIcon.hidden = YES;
    }
}

- (void)setDuration:(NSString *)time duration:(int)d {
    self.duration.text = time;
    self.videoDuration = d;
    self.videoDurationN = @(d);
}

- (int)getMinute:(double)value {
    int num1 = (int)(value * 1000);
    int hour = 3600 * 1000;
    double temp = num1 % hour / 1000;
    return (int)(temp/60);
}

- (int)getSecond:(double)value {
    int num1 = (int)(value * 1000);
    int hour = 3600 * 1000; // 单位：毫秒
    int result1 = num1 % hour;
    int result2 = result1 % (60 * 1000);
    return (int)(result2/1000);
}

- (void)setSelectable:(BOOL)selectable {
    _selectable = selectable;
    if (_selectable) {
        self.selectMask.hidden = YES;
        self.userInteractionEnabled = YES;
    } else {
        if(!_selectMask) {
            _selectMask = [[UIImageView alloc] initWithFrame:self.videaoOverlay.bounds];
            _selectMask.backgroundColor = [UIColor pp_colorWithHexString:@"ffffff99"];
            [_videaoOverlay addSubview:_selectMask];
        }
        _selectMask.hidden = NO;
        self.userInteractionEnabled = NO;

    }
}

- (void)checkButtonAction {
    _assetSelected = !_assetSelected;
    if (_selectItemBlock) {
        NSInteger r = _selectItemBlock(_assetSelected, _asset, self);
        if (r >= 0) {
            _assetSelected = YES;
        }
        [self checkButton:r];
    }

}

- (void)setSelectItemBlock:(SelectItemBlock)selectItemBlock {
    _selectItemBlock = selectItemBlock;
}

- (void)checkButton:(NSInteger)selectIndex {
    if (selectIndex >= 0) {
        if (self.asset.mediaType == PHAssetMediaTypeVideo) {
            self.checkButton.order = 0;
        } else {
            self.checkButton.order = selectIndex + 1;
        }
        self.assetSelected = YES;
    } else {
        self.checkButton.order = -1;
        self.assetSelected = NO;
    }
}

#pragma mark - lazy

- (UIImageView *)imageView {
    if (!_imageView) {
        _imageView = [[UIImageView alloc] initWithImage:[UIImage imageBundleNamed:assets_placeholder_picture]];
        _imageView.translatesAutoresizingMaskIntoConstraints = NO;
        _imageView.contentMode = UIViewContentModeScaleAspectFill;
        _imageView.clipsToBounds = YES;
    }
    return _imageView;
}

- (UIImageView *)videaoOverlay {
    if (!_videaoOverlay) {
        _videaoOverlay = [[UIImageView alloc] initWithFrame: CGRectMake(0, 0, ceil(self.contentView.frame.size.width), ceil(self.contentView.frame.size.height))];
    }
    return _videaoOverlay;
}

- (AssetCheckButton *)checkButton {
    if (!_checkButton) {
        _checkButton = [AssetCheckButton buttonWithType:UIButtonTypeCustom];
        _checkButton.frame = CGRectMake(self.contentView.frame.size.width-44, 0, 44, 44);
        [_checkButton checkBackground];
        [_checkButton addTarget:self action:@selector(checkButtonAction) forControlEvents:UIControlEventTouchUpInside];
    }
    return _checkButton;
}

@end
