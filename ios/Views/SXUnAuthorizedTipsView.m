
#import "SXUnAuthorizedTipsView.h"
#import "SXUtil.h"
#import "UIImage+Operation.h"

@interface SXUnAuthorizedTipsView ()

@property (nonatomic, strong) UIImageView *imageView;

@property (nonatomic, strong) UILabel *label;

@end

@implementation SXUnAuthorizedTipsView

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)setup {
    [self addSubview:self.imageView];
    [self addSubview:self.label];
    _imageView.translatesAutoresizingMaskIntoConstraints = NO;
    _label.translatesAutoresizingMaskIntoConstraints = NO;
    NSDictionary *viewBindingsDict = @{
                                       @"label": _label,
                                       @"imageView" : _imageView
                                       };
    NSDictionary *mertic = @{
                             @"imageLength" : @130,
                             @"labelHeight" : @60
                             };
    NSString *vflV = @"V:|-120-[imageView(imageLength)]-30-[label(<=labelHeight@750)]";
    NSString *vflH = @"H:|-33-[label]-33-|";
    NSArray *contstraintsV = [NSLayoutConstraint constraintsWithVisualFormat:vflV options:NSLayoutFormatAlignAllCenterX metrics:mertic views:viewBindingsDict];
    NSArray *contstraintsH = [NSLayoutConstraint constraintsWithVisualFormat:vflH options:NSLayoutFormatDirectionLeadingToTrailing metrics:mertic views:viewBindingsDict];
    NSLayoutConstraint *imageViewConttraintsWidth = [NSLayoutConstraint constraintWithItem:_imageView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:0 constant:130.0];
    [self addConstraints:contstraintsV];
    [self addConstraints:contstraintsH];
    [self addConstraint:imageViewConttraintsWidth];
}


#pragma mark - lazy
- (UIImageView *)imageView {
    if (_imageView) {
        _imageView = [[UIImageView alloc] initWithImage:[UIImage imageBundleNamed:image_unAuthorized]];
        _imageView.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _imageView;
}

- (UILabel *)label {
    if (!_label) {
        _label = [[UILabel alloc] initWithFrame:CGRectZero];
        NSString *text = [SXUtil SXLocalizedString:@"UnAuthorizedTip" comment:@""];
        NSDictionary *infoDict = [[NSBundle mainBundle] infoDictionary];
        NSString *displayName = infoDict[@"CFBundleDisplayName"];
        if (!displayName) {
            displayName = infoDict[@"CFBundleName"];
        }
        NSString *tipString = [NSString stringWithFormat:@"%@%@",text, displayName];
        _label.text = tipString;
        _label.textColor = [UIColor blackColor];
        _label.font = [UIFont systemFontOfSize:14.0f];
        _label.textAlignment = NSTextAlignmentCenter;
        _label.numberOfLines = 0;
        _label.lineBreakMode = NSLineBreakByTruncatingTail;
    }
    return _label;
}

@end
