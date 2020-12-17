
#import "SXAlbumTableViewController.h"
#import "SXUtil.h"
#import "UIViewController+PPNavigationBarPosition.h"
#import "SXAlbumCell.h"
#import "SXAlbum.h"
#import "DXPickerHelper.h"
#import "SXUnAuthorizedTipsView.h"
#import "SXPhotoPickerController.h"
#import "SXImageFlowViewController.h"

@interface SXAlbumTableViewController ()

@property (nonatomic, strong) NSMutableArray *assetsCollection;

@end

@implementation SXAlbumTableViewController

static NSString *const dxalbumTableViewCellReuseIdentifier = @"dxalbumTableViewCellReuseIdentifier";

- (void)viewDidLoad {
    [super viewDidLoad];
    self.assetsCollection = [[NSMutableArray alloc] init];
    self.title = [SXUtil SXLocalizedString:@"albumTitle" comment:@""];
    [self createBarButtonItemAtPosition:PPNavigationBarPositionRight text:[SXUtil SXLocalizedString:@"cancel" comment:@""] action: @selector(cancelAction)];
    [_assetsCollection addObjectsFromArray:[DXPickerHelper fetchAlbumList]];
    [self.tableView registerClass:[SXAlbumCell class] forCellReuseIdentifier:dxalbumTableViewCellReuseIdentifier];
    self.tableView.tableFooterView = [UIView new];
}

#pragma mark - view life style
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (_assetsCollection.count > 0) {
        NSArray *albums = [_assetsCollection copy];
        for (SXAlbum *item in albums) {
            if ([item.name isEqualToString:@"Recently Deleted"] || [item.name isEqualToString:@"最近删除"]) {
                [_assetsCollection removeObject:item];
                [self.tableView reloadData];
                break;
            }
        }
    }
}

- (void)cancelAction {
    SXPhotoPickerController *pp = (SXPhotoPickerController *)self.navigationController;
    [pp.photoPickerDelegate photoPickerDidCancel:pp];
}

- (void)reloadTableView {
    [_assetsCollection removeAllObjects];
    [_assetsCollection addObjectsFromArray:[DXPickerHelper fetchAlbumList]];
    [self.tableView reloadData];
}

- (void)showUnAuthorizedTipsView {
    self.tableView.backgroundView = [[SXUnAuthorizedTipsView alloc] initWithFrame: self.tableView.bounds];
}

#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.assetsCollection.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    SXAlbumCell *cell = [tableView dequeueReusableCellWithIdentifier:dxalbumTableViewCellReuseIdentifier forIndexPath:indexPath];
    SXAlbum *album = self.assetsCollection[indexPath.row];
    cell.backgroundColor = [UIColor whiteColor];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.titleLabel.text = album.name;
    cell.countLabel.text = [NSString stringWithFormat:@"%ld", (long)album.count];
    [DXPickerHelper fetchImageWithAsset:[album.results lastObject] targetSize:CGSizeMake(60, 60) imageResultHanlder:^(UIImage * _Nonnull image) {
        cell.posterImageView.image = image;
    }];
    [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    SXAlbum *album = _assetsCollection[indexPath.row];
    SXImageFlowViewController *photoListViewController = [[SXImageFlowViewController alloc] initWithAlbum:album];
    [self.navigationController pushViewController:photoListViewController animated:YES];
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}


@end
