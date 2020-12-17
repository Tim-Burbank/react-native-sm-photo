
#import "SXAlbumCell.h"
#import "UIImage+Operation.h"
#import "SXUtil.h"

@interface SXAlbumCell()

@end

@implementation SXAlbumCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)setup {
    self.separatorInset = UIEdgeInsetsMake(0, 60, 0, 0);
    [self.contentView addSubview:self.posterImageView];
    [self.contentView addSubview:self.titleLabel];
    [self.contentView addSubview:self.countLabel];
    
    self.posterImageView.translatesAutoresizingMaskIntoConstraints = NO;
    self.titleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    self.countLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    NSDictionary *viewBingsDict = @{
                                    @"posterImageView": _posterImageView,
                                    @"titleLabel": _titleLabel,
                                    @"countLabel": _countLabel
                                    };
    NSDictionary *mertic = @{ @"imageLength": @60 };
    NSString *vflh = @"H:|-0-[posterImageView(imageLength)]-10-[titleLabel(10@750)]-5-[countLabel]-0-|";
    NSString *imageVFLV = @"V:|-0-[posterImageView(imageLength)]";
    NSArray *contstraintsH = [NSLayoutConstraint constraintsWithVisualFormat:vflh options:NSLayoutFormatAlignAllCenterY metrics:mertic views:viewBingsDict];
    NSArray *imageContstraintsV = [NSLayoutConstraint constraintsWithVisualFormat:imageVFLV options:NSLayoutFormatDirectionLeadingToTrailing metrics:mertic views:viewBingsDict];
    NSLayoutConstraint *titleLaybelHeight = [NSLayoutConstraint constraintWithItem:self.titleLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:40];
    NSLayoutConstraint *countLabelHeight = [NSLayoutConstraint constraintWithItem:self.countLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self.titleLabel attribute:NSLayoutAttributeHeight multiplier:1.0 constant:0];
    [self.contentView addConstraints:contstraintsH];
    [self.contentView addConstraints:imageContstraintsV];
    [self.contentView addConstraints:@[titleLaybelHeight, countLabelHeight]];
    
}

#pragma mark - lazy
- (UIImageView *)posterImageView {
    if (!_posterImageView) {
        _posterImageView = [[UIImageView alloc] initWithImage: [UIImage imageBundleNamed:assets_placeholder_picture]];
        _posterImageView.contentMode = UIViewContentModeScaleAspectFill;
    }
    return _posterImageView;
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectZero];
        _titleLabel.backgroundColor = UIColor.whiteColor;
        _titleLabel.textColor = [UIColor darkTextColor];
        _titleLabel.textAlignment = NSTextAlignmentLeft;
        _titleLabel.font = [UIFont systemFontOfSize:16.0];
    }
    return _titleLabel;
}

- (UILabel *)countLabel {
    if (!_countLabel) {
        _countLabel = [[UILabel alloc] initWithFrame:CGRectZero];
        _countLabel.backgroundColor = [UIColor whiteColor];
        _countLabel.textColor = [UIColor darkTextColor];
        _countLabel.textAlignment = NSTextAlignmentLeft;
        _countLabel.font = [UIFont systemFontOfSize:16.0];
    }
    return _countLabel;
}

@end
