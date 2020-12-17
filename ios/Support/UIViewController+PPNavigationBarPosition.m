

#import "UIViewController+PPNavigationBarPosition.h"
#import "UIColor+StringColor.h"

@implementation UIViewController (PPNavigationBarPosition)

- (void)createBarButtonItemAtPosition:(PPNavigationBarPosition)position normalImage:(UIImage *)normalImage highlightImage:(UIImage *)highlightImage action:(SEL)action {
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    UIEdgeInsets insets = UIEdgeInsetsZero;
    switch (position) {
        case PPNavigationBarPositionLeft:
            insets = UIEdgeInsetsMake(0, -20, 0, 20);
            break;
        case PPNavigationBarPositionRight:
            insets = UIEdgeInsetsMake(0, 13, 0, -13);
            break;
        default:
            break;
    }
    button.imageEdgeInsets = insets;
    [button addTarget:self action:action forControlEvents:UIControlEventTouchUpInside];
    button.frame = CGRectMake(0, 0, 44, 44);
    [button setImage:normalImage forState:UIControlStateNormal];
    [button setImage:highlightImage forState:UIControlStateHighlighted];
    [button.titleLabel setAdjustsFontSizeToFitWidth:YES];
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithCustomView:button];
    switch (position) {
        case PPNavigationBarPositionLeft:
            self.navigationItem.leftBarButtonItem = item;
            break;
    case PPNavigationBarPositionRight:
            self.navigationItem.rightBarButtonItem = item;
            break;
        default:
            break;
    }
}

- (void)createBarButtonItemAtPosition:(PPNavigationBarPosition)position text:(NSString *)title action:(SEL)action {
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    UIEdgeInsets insets = UIEdgeInsetsZero;
    switch (position) {
        case PPNavigationBarPositionLeft:
            insets = UIEdgeInsetsMake(0, -49+26, 0, 19);
            break;
        case PPNavigationBarPositionRight:
            insets = UIEdgeInsetsMake(0, 49-26, 0, -19);
        default:
            break;
    }
    button.imageEdgeInsets = insets;
    [button addTarget:self action:action forControlEvents:UIControlEventTouchUpInside];
    button.frame = CGRectMake(0, 0, 44, 30);
    [button setTitle:title forState:UIControlStateNormal];
    button.titleLabel.font = [UIFont systemFontOfSize:16.0];
    [button setTitleColor:[UIColor grayColor] forState:UIControlStateHighlighted];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [button.titleLabel setAdjustsFontSizeToFitWidth:YES];
    UIBarButtonItem *barButtonItem = [[UIBarButtonItem alloc] initWithCustomView:button];
    switch (position) {
        case PPNavigationBarPositionLeft:
            self.navigationItem.leftBarButtonItem = barButtonItem;
            break;
        case PPNavigationBarPositionRight: {
            UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
            item.width = -10;
            self.navigationItem.rightBarButtonItems = @[item, barButtonItem];
        }
            break;
        default:
            break;
    }
}

- (void)createBackBarButtonItemStatusNormalImage:(UIImage *)normalImage highlightImage:(UIImage *)highlightImage backTitle:(NSString *)backTitle action:(SEL)action {
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    button.frame = CGRectMake(0, 0, 84, 44);
    [button setTitleColor:[UIColor pp_colorWithHexString:@"#808080"] forState:UIControlStateHighlighted];
    [button setTitleColor:[UIColor lightGrayColor] forState:UIControlStateNormal];
    [button setTitle:backTitle forState:UIControlStateNormal];
    [button setTitle:backTitle forState:UIControlStateHighlighted];
    [button setImage:normalImage forState:UIControlStateNormal];
    [button setImage:highlightImage forState:UIControlStateHighlighted];
    UIEdgeInsets imageInsets = UIEdgeInsetsMake(0, -20, 0, 60);
    UIEdgeInsets titleInsets = UIEdgeInsetsMake(0, -45, 0, -15);
    button.imageEdgeInsets = imageInsets;
    button.titleEdgeInsets = titleInsets;
    [button addTarget:self action:action forControlEvents:UIControlEventTouchUpInside];
    button.frame = CGRectMake(0, 0, 64, 30);
    button.titleLabel.font = [UIFont systemFontOfSize:16];
    button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    
    UIBarButtonItem *barButtonItem = [[UIBarButtonItem alloc] initWithCustomView: button];
    self.navigationItem.leftBarButtonItem = barButtonItem;
}

@end
