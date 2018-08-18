//
//  ItemScan.m
//  OracleExpenses
//
//  Created by ThanhLN on 7/23/18.
//

#import "ItemScan.h"

@implementation ItemScan

/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect {
 // Drawing code
 }
 */
- (void)awakeFromNib{
    [super awakeFromNib];
    [self.buttonAction addTarget:self action:@selector(touchButtonAction) forControlEvents:UIControlEventTouchUpInside];
    [self.labelValue setText:@""];
    [self.labelValue setFont:[UIFont systemFontOfSize:10]];
    [self.labelValue setFont:[UIFont systemFontOfSize:15]];
    confirmText = @"";
}
- (void)touchButtonAction{
    if (self.isSelected) {
        self.isSelected = NO;
        confirmText = draftText;
        [self.delegate didTouchToDeSelect:self];
    }else{
        self.isSelected = YES;
        [self.delegate didTouchToSelected:self];
    }
    [self updateGUI];
}

- (void)updateGUI{
    [self.labelTitle setText:[self titleName]];
    if (self.isSelected) {
        [self.icon setImage:[UIImage imageNamed:@"ic_done_white.png"]];
    }else{
        [self.icon setImage:[UIImage imageNamed:@"ic_ocr_white.png"]];
    }
}
- (NSString *)titleName{
    return [NSString stringWithFormat:@"%@:",[self nameType]];
}

- (NSString *)nameType{
    switch (self.viewType) {
        case TypeItemScanMerchant:
            return @"MerchantName";
            break;
        case TypeItemScanDate:
            return @"Date";
            break;
        case TypeItemScanAmount:
            return @"Amount";
            break;
            
        default:
            break;
    }
    return @"";
}

- (void)setType:(TypeItemScan)type{
    self.viewType = type;
    [self updateGUI];
}
- (void)setValueScaned:(NSString *)text{
    if (!text) {
        draftText = @"";
    }else{
        draftText = text;
    }
    
    [self.labelValue setText:[draftText stringByReplacingOccurrencesOfString:@"\n" withString:@" "]];
}
- (void)setSelected:(BOOL)select{
    self.isSelected = select;
    [self updateGUI];
}
- (NSDictionary *)data{
   /*/ if(!confirmText){
        confirmText = @"";
    }*/
    if(!draftText)
        draftText =@"";
    return [NSDictionary dictionaryWithObject:draftText forKey:[self nameType]];
}
@end
