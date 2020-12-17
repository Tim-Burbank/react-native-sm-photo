
#import "UICollectionView+IndexPath.h"

@implementation UICollectionView (IndexPath)

- (NSArray *)aapl_indexPathsForElementsInRect:(CGRect)rect {
    NSArray *allLayoutAttributes = [self.collectionViewLayout layoutAttributesForElementsInRect:rect];
    if (allLayoutAttributes.count == 0) {
        return nil;
    }
    NSMutableArray *indexPaths = [[NSMutableArray alloc] init];
    for (NSInteger index = 0; index<allLayoutAttributes.count; index++) {
        NSIndexPath  *indexPath = [allLayoutAttributes[index] indexPath];
        [indexPaths addObject:indexPath];
    }
    return [indexPaths copy];
}

@end
