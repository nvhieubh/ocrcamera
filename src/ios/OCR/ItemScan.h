//
//  ItemScan.h
//  OracleExpenses
//
//  Created by ThanhLN on 7/23/18.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, TypeItemScan) {
    TypeItemScanMerchant,
    TypeItemScanDate,
    TypeItemScanAmount
};

@protocol ItemScanDelegate;

@interface ItemScan : UIView{
    NSString *draftText;
    NSString *confirmText;
}
@property (nonatomic, weak) IBOutlet UIImageView *icon;
@property (nonatomic, weak) IBOutlet UILabel *labelTitle;
@property (nonatomic, weak) IBOutlet UILabel *labelValue;
@property (nonatomic, weak) IBOutlet UIButton *buttonAction;
@property (nonatomic) BOOL isSelected;
@property (nonatomic) TypeItemScan viewType;
@property (nonatomic, weak) NSObject<ItemScanDelegate> *delegate;

- (void)setType:(TypeItemScan)type;
- (void)setSelected:(BOOL)select;
- (void)setValueScaned:(NSString *)text;
- (NSString *)getText;
- (NSDictionary *)data;
@end

@protocol ItemScanDelegate
- (void)didTouchToSelected:(ItemScan *)itemScan;
- (void)didTouchToDeSelect:(ItemScan *)itemScan;
@end
