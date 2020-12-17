
#import "UIColor+StringColor.h"

@implementation UIColor (StringColor)

+ (UIColor *)pp_colorWithHexString:(NSString *)hexColor {
    CGFloat red = 0.0;
    CGFloat green = 0.0;
    CGFloat blue = 0.0;
    CGFloat alpha = 1.0;
    
    NSString *hexColorString = hexColor;
    
    if (![hexColorString hasPrefix:@"#"]) {
        hexColorString = [NSString stringWithFormat:@"#%@", hexColorString];
    }
    
    NSString *hex = [hexColorString substringFromIndex:1];
    
    NSScanner *scanner = [[NSScanner alloc] initWithString:hex];
    
    unsigned hexValue = 0;
    if ([scanner scanHexInt:&hexValue]){
        switch (hex.length) {
            case 3:
            {
                red   = ((hexValue & 0xF00) >> 8)      / 15.0;
                green = ((hexValue & 0x0F0) >> 4)    / 15.0;
                blue  = (hexValue & 0x00F )             / 15.0;
            }
                break;
            case 4:
            {
                red   = ((hexValue & 0xF000) >> 12)     / 15.0;
                green = ((hexValue & 0x0F00) >> 8)      / 15.0;
                blue  = ((hexValue & 0x00F0) >> 4)      / 15.0;
                alpha = (hexValue & 0x000F)             / 15.0;
            }
                break;
            case 6:
            {
                red   = ((hexValue & 0xFF0000) >> 16)   / 255.0;
                green = ((hexValue & 0x00FF00) >> 8)    / 255.0;
                blue  = (hexValue & 0x0000FF)           / 255.0;
            }
                break;
            case 8:
            {
                red   = ((hexValue & 0xFF000000) >> 24) / 255.0;
                green = ((hexValue & 0x00FF0000) >> 16) / 255.0;
                blue  = ((hexValue & 0x0000FF00) >> 8)  / 255.0;
                alpha = (hexValue & 0x000000FF)         / 255.0;
            }
                break;
            default:
                NSLog(@"Invalid RGB string, number of characters after '#' should be either 3, 4, 6 or 8");
                break;
        }
    } else {
        NSLog(@"Scan hex error");
    }
    UIColor *color = [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
    return color;
}

@end
